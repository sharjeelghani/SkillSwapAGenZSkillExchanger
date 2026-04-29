package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.AnalyticsService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardFragment extends Fragment {


    private static final String TAG = "AdminDashboardFragment";
    private TextView tvActiveUsers, tvTotalSessions, tvAvgSessionDuration, tvTopEvent;
    private TextView tvDailyActiveUsers, tvWeeklyActiveUsers, tvMonthlyActiveUsers;
    private DatabaseReference usersRef;
    private DatabaseReference analyticsDataRef;
    private AnalyticsService analyticsService;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            usersRef = FirebaseDatabase.getInstance().getReference("user");
            analyticsDataRef = FirebaseDatabase.getInstance().getReference("analytics_data");
            analyticsService = AnalyticsService.getInstance(requireContext());
            firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // Initialize all text views for analytics data
        View activeUsersCard = view.findViewById(R.id.card_active_users);
        if (activeUsersCard != null) {
            tvActiveUsers = activeUsersCard.findViewById(R.id.value);
        }

        tvTotalSessions = view.findViewById(R.id.value_total_sessions);
        tvAvgSessionDuration = view.findViewById(R.id.value_avg_session_duration);
        tvTopEvent = view.findViewById(R.id.value_top_event);
        tvDailyActiveUsers = view.findViewById(R.id.value_dau);
        tvWeeklyActiveUsers = view.findViewById(R.id.value_wau);
        tvMonthlyActiveUsers = view.findViewById(R.id.value_mau);

        // Setup click listeners for analytics tracking
        setupAnalyticsTracking(view);

        return view;
    }

    private void setupAnalyticsTracking(View parent) {
        // Track when admin views different analytics cards
        View activeUsersCard = parent.findViewById(R.id.card_active_users);
        if (activeUsersCard != null) {
            activeUsersCard.setOnClickListener(v -> logAnalyticsEvent("view_active_users", null));
        }

        View sessionsCard = parent.findViewById(R.id.card_sessions);
        if (sessionsCard != null) {
            sessionsCard.setOnClickListener(v -> logAnalyticsEvent("view_session_stats", null));
        }

        View engagementCard = parent.findViewById(R.id.card_engagement);
        if (engagementCard != null) {
            engagementCard.setOnClickListener(v -> logAnalyticsEvent("view_engagement_metrics", null));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set user properties for better analytics filtering
        setUserAnalyticsProperties();

        // Log screen view
        logScreenView();

        // Fetch and display various analytics data
        fetchActiveUsersCount();
        fetchSessionAnalytics();
        fetchUserEngagementMetrics();
        fetchTopEvents();
        fetchDAUWAUMAU();

        // Log that admin viewed dashboard
        logAnalyticsEvent("admin_dashboard_viewed", null);
    }

    private void setUserAnalyticsProperties() {
        // Set user properties for segmentation in Firebase Console
        firebaseAnalytics.setUserProperty("user_role", "admin");
        firebaseAnalytics.setUserProperty("app_version", "1.0");
        firebaseAnalytics.setUserProperty("platform", "Android");

        analyticsService.setUserProperty("user_role", "admin");
    }

    private void logScreenView() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "AdminDashboard");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "AdminDashboardFragment");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        analyticsService.logScreen("Admin Dashboard", "AdminDashboardFragment");
    }

    private void logAnalyticsEvent(String eventName, Bundle params) {
        if (params == null) {
            params = new Bundle();
        }
        params.putLong("timestamp", System.currentTimeMillis());
        firebaseAnalytics.logEvent(eventName, params);

        // Also store in Firebase Database for real-time display
        storeAnalyticsEventInDatabase(eventName, params);
    }

    private void storeAnalyticsEventInDatabase(String eventName, Bundle params) {
        String key = analyticsDataRef.child("events").push().getKey();
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event_name", eventName);
        eventData.put("timestamp", System.currentTimeMillis());
        eventData.put("date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        if (params != null) {
            for (String key_param : params.keySet()) {
                Object value = params.get(key_param);
                if (value != null) {
                    eventData.put("param_" + key_param, value.toString());
                }
            }
        }

        if (key != null) {
            analyticsDataRef.child("events").child(key).setValue(eventData)
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to store event: " + e.getMessage()));
        }
    }

    private void fetchActiveUsersCount() {
        if (usersRef == null) return;

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalUsers = snapshot.getChildrenCount();
                if (tvActiveUsers != null) {
                    tvActiveUsers.setText(String.format(Locale.getDefault(), "%d", totalUsers));
                }

                // Log this as an analytics event
                Bundle bundle = new Bundle();
                bundle.putLong("total_users", totalUsers);
                logAnalyticsEvent("users_count_fetched", bundle);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                if (tvActiveUsers != null) tvActiveUsers.setText("--");
            }
        });
    }

    private void fetchSessionAnalytics() {
        // Query session data from analytics_data node
        analyticsDataRef.child("sessions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalSessions = 0;
                long totalDuration = 0;
                long sessionCount = 0;

                for (DataSnapshot session : snapshot.getChildren()) {
                    totalSessions++;
                    Long duration = session.child("duration").getValue(Long.class);
                    if (duration != null) {
                        totalDuration += duration;
                        sessionCount++;
                    }
                }

                if (tvTotalSessions != null) {
                    tvTotalSessions.setText(String.format(Locale.getDefault(), "%d", totalSessions));
                }

                if (tvAvgSessionDuration != null && sessionCount > 0) {
                    long avgDuration = totalDuration / sessionCount;
                    tvAvgSessionDuration.setText(formatDuration(avgDuration));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch sessions: " + error.getMessage());
            }
        });
    }

    private void fetchUserEngagementMetrics() {
        // Track user engagement through custom events
        analyticsDataRef.child("user_engagement").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> engagementMap = new HashMap<>();

                for (DataSnapshot userEngagement : snapshot.getChildren()) {
                    String userId = userEngagement.getKey();
                    Integer eventCount = userEngagement.child("total_events").getValue(Integer.class);
                    if (eventCount != null) {
                        engagementMap.put(userId, eventCount);
                    }
                }

                // Log engagement metrics
                Bundle bundle = new Bundle();
                bundle.putInt("engaged_users", engagementMap.size());
                logAnalyticsEvent("engagement_metrics_fetched", bundle);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch engagement: " + error.getMessage());
            }
        });
    }

    private void fetchTopEvents() {
        // Get most common events from analytics data
        analyticsDataRef.child("events").orderByChild("timestamp").limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Integer> eventCounts = new HashMap<>();

                        for (DataSnapshot event : snapshot.getChildren()) {
                            String eventName = event.child("event_name").getValue(String.class);
                            if (eventName != null) {
                                eventCounts.put(eventName, eventCounts.getOrDefault(eventName, 0) + 1);
                            }
                        }

                        // Find top event
                        String topEvent = "";
                        int maxCount = 0;
                        for (Map.Entry<String, Integer> entry : eventCounts.entrySet()) {
                            if (entry.getValue() > maxCount) {
                                maxCount = entry.getValue();
                                topEvent = entry.getKey();
                            }
                        }

                        if (tvTopEvent != null && !topEvent.isEmpty()) {
                            tvTopEvent.setText(String.format(Locale.getDefault(), "%s (%d times)",
                                    topEvent, maxCount));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch top events: " + error.getMessage());
                    }
                });
    }

    private void fetchDAUWAUMAU() {
        // Calculate Daily, Weekly, Monthly Active Users
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - (24 * 60 * 60 * 1000);
        long oneWeekAgo = currentTime - (7 * 24 * 60 * 60 * 1000);
        long oneMonthAgo = currentTime - (30 * 24 * 60 * 60 * 1000);

        analyticsDataRef.child("user_activity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int dau = 0, wau = 0, mau = 0;

                for (DataSnapshot userActivity : snapshot.getChildren()) {
                    Long lastActive = userActivity.child("last_active_timestamp").getValue(Long.class);
                    if (lastActive != null) {
                        if (lastActive >= oneDayAgo) dau++;
                        if (lastActive >= oneWeekAgo) wau++;
                        if (lastActive >= oneMonthAgo) mau++;
                    }
                }

                if (tvDailyActiveUsers != null) tvDailyActiveUsers.setText(String.valueOf(dau));
                if (tvWeeklyActiveUsers != null) tvWeeklyActiveUsers.setText(String.valueOf(wau));
                if (tvMonthlyActiveUsers != null) tvMonthlyActiveUsers.setText(String.valueOf(mau));

                // Log DAU/WAU/MAU metrics
                Bundle bundle = new Bundle();
                bundle.putInt("dau", dau);
                bundle.putInt("wau", wau);
                bundle.putInt("mau", mau);
                logAnalyticsEvent("active_users_metrics", bundle);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch active users: " + error.getMessage());
            }
        });
    }

    private String formatDuration(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%d min %d sec", minutes, seconds);
    }

    // Method to log custom events from anywhere in the app
    public void logUserAction(String action, String category, String label, long value) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, action);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, action);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, category);
        bundle.putString(FirebaseAnalytics.Param.CONTENT, label);
        bundle.putLong(FirebaseAnalytics.Param.VALUE, value);

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        // Also store in database
        storeAnalyticsEventInDatabase(action, bundle);
    }
}