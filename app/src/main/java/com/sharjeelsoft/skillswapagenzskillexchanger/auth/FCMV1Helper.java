package com.sharjeelsoft.skillswapagenzskillexchanger.auth;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMV1Helper {

    private static final String TAG = "FCMV1Helper";
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";
    private final Context context;
    private String accessToken;
    private long expireTime;

    public FCMV1Helper(Context context) {
        this.context = context;
    }

    public void sendNotification(String targetToken, String title, String body, String navigateTo) {
        sendInternal(targetToken, title, body, navigateTo, null, null, false);
    }

    public void sendChatNotification(String targetToken, String title, String body, String navigateTo, String messageId, String senderUsername) {
        // For chats, we use data-only payload to ensure onMessageReceived is called even in background
        // this allows us to mark the message as "delivered" in the database.
        sendInternal(targetToken, title, body, navigateTo, messageId, senderUsername, true);
    }

    private void sendInternal(String targetToken, String title, String body, String navigateTo, String messageId, String senderUsername, boolean dataOnly) {
        new Thread(() -> {
            try {
                String token = getAccessToken();
                if (token == null) {
                    Log.e(TAG, "Notification aborted: Access token is null");
                    return;
                }

                OkHttpClient client = new OkHttpClient();
                
                JSONObject data = new JSONObject();
                if (navigateTo != null) {
                    data.put("navigate_to", navigateTo);
                }
                data.put("title", title);
                data.put("body", body);
                if (messageId != null) data.put("message_id", messageId);
                if (senderUsername != null) data.put("sender_username", senderUsername);

                JSONObject messageObject = new JSONObject();
                messageObject.put("token", targetToken);
                
                if (!dataOnly) {
                    JSONObject notification = new JSONObject();
                    notification.put("title", title);
                    notification.put("body", body);
                    messageObject.put("notification", notification);
                }
                
                messageObject.put("data", data);

                // HIGH IMPORTANCE: Android specific configuration
                JSONObject androidNotification = new JSONObject();
                androidNotification.put("notification_priority", "PRIORITY_MAX");
                androidNotification.put("sound", "default");
                androidNotification.put("channel_id", "match_requests_channel");

                JSONObject androidConfig = new JSONObject();
                androidConfig.put("priority", "high");
                if (!dataOnly) {
                    androidConfig.put("notification", androidNotification);
                }
                
                messageObject.put("android", androidConfig);

                JSONObject rootPayload = new JSONObject();
                rootPayload.put("message", messageObject);

                RequestBody requestBody = RequestBody.create(
                        rootPayload.toString(),
                        MediaType.get("application/json; charset=utf-8")
                );

                String jsonContent = loadJSONFromAsset();
                JSONObject serviceAccount = new JSONObject(jsonContent);
                String projectId = serviceAccount.getString("project_id");

                Request request = new Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send")
                        .post(requestBody)
                        .addHeader("Authorization", "Bearer " + token)
                        .build();

                Response response = client.newCall(request).execute();
                String resBody = response.body() != null ? response.body().string() : "empty";
                Log.d(TAG, "FCM V1 SUCCESS! Code: " + response.code() + " Response: " + resBody);

            } catch (Exception e) {
                Log.e(TAG, "CRITICAL ERROR sending FCM notification", e);
            }
        }).start();
    }

    private synchronized String getAccessToken() throws Exception {
        if (accessToken != null && System.currentTimeMillis() < expireTime) {
            return accessToken;
        }

        String jsonContent = loadJSONFromAsset();
        if (jsonContent.isEmpty()) {
            Log.e(TAG, "Cannot mint token: JSON content is empty");
            return null;
        }

        JSONObject serviceAccount = new JSONObject(jsonContent);
        String clientEmail = serviceAccount.getString("client_email");
        String privateKeyString = serviceAccount.getString("private_key");

        long now = System.currentTimeMillis() / 1000L;
        
        JSONObject header = new JSONObject();
        header.put("alg", "RS256");
        header.put("typ", "JWT");

        JSONObject payload = new JSONObject();
        payload.put("iss", clientEmail);
        payload.put("scope", MESSAGING_SCOPE);
        payload.put("aud", TOKEN_SERVER_URL);
        payload.put("exp", now + 3600);
        payload.put("iat", now);

        String encodedHeader = base64UrlEncode(header.toString().getBytes(StandardCharsets.UTF_8));
        String encodedPayload = base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));
        
        String assertion = encodedHeader + "." + encodedPayload;
        
        PrivateKey privateKey = getPrivateKey(privateKeyString);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(assertion.getBytes(StandardCharsets.UTF_8));
        byte[] signedBytes = signature.sign();
        
        String signedAssertion = assertion + "." + base64UrlEncode(signedBytes);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                .add("assertion", signedAssertion)
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_SERVER_URL)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            accessToken = jsonResponse.getString("access_token");
            expireTime = System.currentTimeMillis() + (jsonResponse.getLong("expires_in") * 1000);
            return accessToken;
        } else {
            Log.e(TAG, "OAuth2 Token Exchange Failed: " + (response.body() != null ? response.body().string() : "empty"));
        }

        return null;
    }

    private String loadJSONFromAsset() {
        String[] variants = {"service_account.json", "service_account.json.json"};
        for (String name : variants) {
            try (InputStream is = context.getAssets().open(name)) {
                Scanner s = new Scanner(is).useDelimiter("\\A");
                String content = s.hasNext() ? s.next() : "";
                if (!content.isEmpty()) {
                    Log.d(TAG, "Successfully loaded credentials from: " + name);
                    return content;
                }
            } catch (Exception ignored) {}
        }
        return "";
    }

    private PrivateKey getPrivateKey(String key) throws Exception {
        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "")
                .trim();

        byte[] encoded = Base64.decode(privateKeyPEM, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    private String base64UrlEncode(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE);
    }
}
