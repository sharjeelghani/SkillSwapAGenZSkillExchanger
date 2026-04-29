package com.sharjeelsoft.skillswapagenzskillexchanger.auth;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AnalyticsService {
    private final FirebaseAnalytics mFirebaseAnalytics;
    private final DatabaseReference analyticsRef;
    private static AnalyticsService instance;
    private String currentUsername;
    private long sessionStartTime;
    private AnalyticsService(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        analyticsRef = FirebaseDatabase.getInstance().getReference("analytics_data");
        
        MySharedprefsClass prefs = new MySharedprefsClass(context);
        currentUsername = prefs.getStringValue("username");
    }

    public static synchronized AnalyticsService getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsService(context.getApplicationContext());
        }
        return instance;
    }

    public void startSession(String username) {
        this.currentUsername = username;
        this.sessionStartTime = System.currentTimeMillis();
        
        // Log user activity for DAU/WAU/MAU
        if (username != null) {
            analyticsRef.child("user_activity").child(username)
                    .child("last_active_timestamp").setValue(sessionStartTime);
        }
    }

    public void endSession() {
        if (sessionStartTime == 0 || currentUsername == null) return;

        long duration = System.currentTimeMillis() - sessionStartTime;
        String sessionKey = analyticsRef.child("sessions").push().getKey();
        
        if (sessionKey != null) {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("username", currentUsername);
            sessionData.put("duration", duration);
            sessionData.put("timestamp", System.currentTimeMillis());
            sessionData.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            
            analyticsRef.child("sessions").child(sessionKey).setValue(sessionData);
        }
        sessionStartTime = 0;
    }

    public void logScreen(String screenName, String screenClass) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
        
        logEventToDatabase("screen_view", screenName);
    }

    public void logClick(String buttonName) {
        Bundle bundle = new Bundle();
        bundle.putString("button_name", buttonName);
        mFirebaseAnalytics.logEvent("button_click", bundle);
        
        logEventToDatabase("button_click", buttonName);
    }

    public void logCustomEvent(String name, Bundle parameters) {
        mFirebaseAnalytics.logEvent(name, parameters);
        logEventToDatabase(name, null);
    }

    private void logEventToDatabase(String eventName, String value) {
        String key = analyticsRef.child("events").push().getKey();
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_name", eventName);
        eventData.put("value", value);
        eventData.put("username", currentUsername != null ? currentUsername : "anonymous");
        eventData.put("timestamp", System.currentTimeMillis());

        if (key != null) {
            analyticsRef.child("events").child(key).setValue(eventData);
        }
    }

    public void setUserProperty(String name, String value) {
        mFirebaseAnalytics.setUserProperty(name, value);
    }

    public void logPurchase(double value, String currency, String itemName, String itemId) {
        Bundle params = new Bundle();
        params.putDouble(FirebaseAnalytics.Param.VALUE, value);
        params.putString(FirebaseAnalytics.Param.CURRENCY, currency);
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        params.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, params);
    }
}
