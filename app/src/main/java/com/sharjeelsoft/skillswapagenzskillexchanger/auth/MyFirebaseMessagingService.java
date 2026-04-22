package com.sharjeelsoft.skillswapagenzskillexchanger.auth;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.LauncherScreen;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String CHANNEL_ID = "match_requests_channel";
    private static final String PREFS_NAME = "UnreadMessagesPrefs";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;
        String navigateTo = null;
        String messageId = null;
        String senderUsername = null;

        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
            navigateTo = remoteMessage.getData().get("navigate_to");
            messageId = remoteMessage.getData().get("message_id");
            senderUsername = remoteMessage.getData().get("sender_username");
        }

        if (title != null && body != null) {
            if (messageId != null && senderUsername != null) {
                markAsDelivered(senderUsername, messageId);
            }
            showNotification(title, body, navigateTo, senderUsername);
        }
    }

    private void markAsDelivered(String senderUsername, String messageId) {
        MySharedprefsClass sharedPrefs = new MySharedprefsClass(getApplicationContext());
        String currentUsername = sharedPrefs.getStringValue("username");
        if (currentUsername == null || currentUsername.equals("new_user")) return;

        String chatId = getChatId(currentUsername, senderUsername);
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child(messageId);
        chatRef.child("delivered").setValue(true);
    }

    private String getChatId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    private void showNotification(String title, String message, String navigateTo, String senderUsername) {
        Intent intent = new Intent(this, LauncherScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        if (navigateTo != null) {
            intent.putExtra("navigate_to", navigateTo);
        }

        int notificationId;
        String contentText = message;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (senderUsername != null) {
            notificationId = senderUsername.hashCode();
            
            // Check if the notification is currently active in the panel
            boolean isShowing = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
                for (StatusBarNotification sbn : activeNotifications) {
                    if (sbn.getId() == notificationId) {
                        isShowing = true;
                        break;
                    }
                }
            }

            // If user cleared the notification manually, reset the history
            if (!isShowing) {
                resetHistory(senderUsername);
            }
            
            contentText = getAggregatedMessage(senderUsername, message);
        } else {
            notificationId = (int) System.currentTimeMillis();
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Match Requests", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private String getAggregatedMessage(String senderUsername, String newMessage) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String history = prefs.getString(senderUsername, "");
        
        if (history.isEmpty()) {
            history = newMessage;
        } else {
            history = history + "\n" + newMessage;
        }
        
        prefs.edit().putString(senderUsername, history).apply();
        return history;
    }

    private void resetHistory(String senderUsername) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(senderUsername).apply();
    }

    public static void clearHistory(Context context, String senderUsername) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(senderUsername).apply();
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(senderUsername.hashCode());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        updateTokenInFirebase(token);
    }

    private void updateTokenInFirebase(String token) {
        MySharedprefsClass sharedPrefs = new MySharedprefsClass(getApplicationContext());
        String username = sharedPrefs.getStringValue("username");
        if (username != null && !username.equals("new_user")) {
            FirebaseDatabase.getInstance().getReference("user")
                    .child(username).child("fcmToken").setValue(token);
        }
    }
}
