package com.sharjeelsoft.skillswapagenzskillexchanger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.AnalyticsService;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.ChatMessage;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ActivityCNICVarification;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ChatActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ChatListActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.DashboardFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.DataCllectionActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.Help_Support_Activity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.MatchRequestsFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.NotificationsFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ProfileFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.Report_User_Activity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SearchActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SessionDetailsActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.Session_Reminder_Activity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SettingsActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SkillMatchmakingFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SkillSelectionActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private NavigationView nav_view;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    public ViewPager viewPager;
    private View contentView;
    private TabLayout tabLayout;

    public LottieAnimationView drawer_icon, btn_search_header;
    public ImageView btn_messages_header;
    public View dotUnreadMessages;
    public int fragPosition = 0;

    private DatabaseReference connectionsRef, chatsRef, notificationsRef, matchRequestsRef;
    private String currentUsername;
    private Map<String, ValueEventListener> chatListeners = new HashMap<>();
    private Map<String, Boolean> unreadStatusMap = new HashMap<>();
    private ValueEventListener notificationsListener, matchRequestsListener;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    saveFcmToken();
                } else {
                    Log.w("MainActivity", "Notification permission denied");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MySharedprefsClass prefs = new MySharedprefsClass(this);
        currentUsername = prefs.getStringValue("username");
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        notificationsRef = FirebaseDatabase.getInstance().getReference("user").child(currentUsername).child("notifications");
        matchRequestsRef = FirebaseDatabase.getInstance().getReference("user").child(currentUsername).child("matchRequests").child("received");

        askNotificationPermission();
        saveFcmToken();
        setupDrawerClicks();
        logUserActivity();

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.TRANSPARENT);
        gradientDrawable.setCornerRadius(50);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        contentView = findViewById(R.id.content);
        nav_view = (NavigationView) findViewById(R.id.nav_view);
        drawer_icon = findViewById(R.id.drawer_icon_home);
        btn_search_header = findViewById(R.id.btn_search_header);
        btn_messages_header = findViewById(R.id.btn_messages_header);
        dotUnreadMessages = findViewById(R.id.dot_unread_messages);

        btn_search_header.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        btn_messages_header.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ChatListActivity.class)));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawer_icon.setOnClickListener(v -> mDrawerLayout.openDrawer(GravityCompat.START));
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawer, float slideOffset) {
                contentView.setX(nav_view.getWidth() * slideOffset);
                DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) contentView.getLayoutParams();
                lp.height = drawer.getHeight() - (int) (drawer.getHeight() * slideOffset * 0.3f);
                lp.topMargin = (drawer.getHeight() - lp.height) / 3;
                lp.bottomMargin = (drawer.getHeight() - lp.height) / 3;
                contentView.setLayoutParams(lp);
                gradientDrawable.setCornerRadius(50);
                contentView.setBackground(gradientDrawable);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                gradientDrawable.setCornerRadius(0);
                contentView.setBackground(gradientDrawable);
            }
        });

        mDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));

        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        SetUpPager();

        handleIntent(getIntent());
        listenForUnreadMessages();
        listenForUnreadNotifications();
        listenForNewMatchRequests();
    }

    private void listenForUnreadMessages() {
        if (currentUsername == null || currentUsername.isEmpty()) return;

        connectionsRef = FirebaseDatabase.getInstance().getReference("user")
                .child(currentUsername).child("allConnections");

        connectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String otherUser = ds.getKey();
                    if (otherUser != null) {
                        String chatId = getChatId(currentUsername, otherUser);
                        if (!chatListeners.containsKey(chatId)) {
                            setupChatListener(chatId);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupChatListener(String chatId) {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasUnread = false;
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    ChatMessage msg = msgSnap.getValue(ChatMessage.class);
                    if (msg != null && !msg.getSenderId().equals(currentUsername) && !msg.isRead()) {
                        hasUnread = true;
                        break;
                    }
                }
                unreadStatusMap.put(chatId, hasUnread);
                updateUnreadDot();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        chatsRef.child(chatId).addValueEventListener(listener);
        chatListeners.put(chatId, listener);
    }

    private void updateUnreadDot() {
        boolean overallHasUnread = false;
        for (boolean status : unreadStatusMap.values()) {
            if (status) {
                overallHasUnread = true;
                break;
            }
        }
        if (dotUnreadMessages != null) {
            dotUnreadMessages.setVisibility(overallHasUnread ? View.VISIBLE : View.GONE);
        }
    }

    private void listenForUnreadNotifications() {
        notificationsListener = notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasUnread = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Logic: Assuming "read" field in notification. If not exist, any new notification counts as unread.
                    // To properly implement "disappears when fragment is opened", we track the last time user opened it.
                    // For now, let's assume existence of un-deleted notifications for simple visibility.
                    if (!ds.hasChild("read") || Boolean.FALSE.equals(ds.child("read").getValue(Boolean.class))) {
                        hasUnread = true;
                        break;
                    }
                }
                updateTabBadge(2, hasUnread);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForNewMatchRequests() {
        matchRequestsListener = matchRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasNew = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (!ds.hasChild("read") || Boolean.FALSE.equals(ds.child("read").getValue(Boolean.class))) {
                        hasNew = true;
                        break;
                    }
                }
                updateTabBadge(3, hasNew);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateTabBadge(int position, boolean visible) {
        if (tabLayout == null) return;
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null && tab.getCustomView() != null) {
            View dot = tab.getCustomView().findViewById(R.id.tab_unread_dot);
            if (dot != null) {
                dot.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }
    private void markNotificationsAsRead() {
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    updates.put(ds.getKey() + "/read", true);
                }
                if (!updates.isEmpty()) notificationsRef.updateChildren(updates);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void markMatchRequestsAsRead() {
        matchRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    updates.put(ds.getKey() + "/read", true);
                }
                if (!updates.isEmpty()) matchRequestsRef.updateChildren(updates);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getChatId(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + "_" + u2 : u2 + "_" + u1;
    }

    private void logUserActivity() {
        MySharedprefsClass prefs = new MySharedprefsClass(this);
        String username = prefs.getStringValue("username");
        if (username != null && !username.isEmpty()) {
            DatabaseReference activityRef = FirebaseDatabase.getInstance().getReference("analytics_data").child("user_activity").child(username);
            activityRef.child("last_active_timestamp").setValue(System.currentTimeMillis());
            AnalyticsService.getInstance(this).startSession(username);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("navigate_to")) {
            String destination = intent.getStringExtra("navigate_to");
            if ("requests".equals(destination)) {
                if (viewPager != null) {
                    viewPager.setCurrentItem(3);
                } else {
                    fragPosition = 3;
                }
            } else if (destination != null && destination.startsWith("chat_")) {
                String senderUsername = destination.replace("chat_", "");
                Intent chatIntent = new Intent(this, ChatActivity.class);
                chatIntent.putExtra("targetUsername", senderUsername);
                startActivity(chatIntent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AnalyticsService.getInstance(this).endSession();
        for (Map.Entry<String, ValueEventListener> entry : chatListeners.entrySet()) {
            chatsRef.child(entry.getKey()).removeEventListener(entry.getValue());
        }
        if (notificationsRef != null && notificationsListener != null) notificationsRef.removeEventListener(notificationsListener);
        if (matchRequestsRef != null && matchRequestsListener != null) matchRequestsRef.removeEventListener(matchRequestsListener);
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void saveFcmToken() {
        MySharedprefsClass prefs = new MySharedprefsClass(this);
        String username = prefs.getStringValue("username");
        if (username == null || username.equals("new_user")) return;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                return;
            }


            String token = task.getResult();
            FirebaseDatabase.getInstance().getReference("user")
                    .child(username).child("fcmToken").setValue(token);
        });
    }

    private void SetUpPager() {
        int[] SELECTED_ICONS = {R.drawable.home_sel, R.drawable.dashboard_sel, R.drawable.notification_sel, R.drawable.ic_matches, R.drawable.user_sel};
        int[] UNSELECTED_ICONS = {R.drawable.home_unsel, R.drawable.dashboard_unsel, R.drawable.notification_unsel, R.drawable.ic_matches_unsel, R.drawable.user_unsel};

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout = findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                View customView = LayoutInflater.from(this).inflate(R.layout.tab_custom_view, null);
                ImageView tabIcon = customView.findViewById(R.id.tab_icon);
                TextView tabText = customView.findViewById(R.id.tab_text);

                tabIcon.setImageResource(UNSELECTED_ICONS[i]);
                tabText.setText("");
                tab.setCustomView(customView);
            }
        }

        // Set initial state based on current fragPosition
        TabLayout.Tab tab = tabLayout.getTabAt(fragPosition);
        if (tab != null && tab.getCustomView() != null) {
            View customView = tab.getCustomView();
            ImageView tabIcon = customView.findViewById(R.id.tab_icon);
            TextView tabText = customView.findViewById(R.id.tab_text);
            RelativeLayout bg_layout = customView.findViewById(R.id.bg_layout);
            bg_layout.setBackground(getDrawable(R.drawable.bg_cnic_card));

            tabIcon.setImageResource(SELECTED_ICONS[fragPosition]);
            tabText.setText(getTabTitle(fragPosition));
            tabText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();
                fragPosition = position;
                
                // Disappear dots when fragments are opened
                if (position == 2) markNotificationsAsRead();
                if (position == 3) markMatchRequestsAsRead();

                View customView = tab.getCustomView();
                if (customView != null) {
                    ImageView tabIcon = customView.findViewById(R.id.tab_icon);
                    TextView tabText = customView.findViewById(R.id.tab_text);
                    RelativeLayout bg_layout = customView.findViewById(R.id.bg_layout);
                    bg_layout.setBackground(getDrawable(R.drawable.bg_cnic_card));

                    tabIcon.setImageResource(SELECTED_ICONS[position]);
                    tabText.setText(getTabTitle(position));
                    tabText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));

                }
            }
            // Set the icons and their sizes when a tab is unselected
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                int position = tab.getPosition();
                View customView = tab.getCustomView();
                if (customView != null) {
                    ImageView tabIcon = customView.findViewById(R.id.tab_icon);
                    TextView tabText = customView.findViewById(R.id.tab_text);
                    RelativeLayout bg_layout = customView.findViewById(R.id.bg_layout);
                    bg_layout.setBackground(null);

                    tabIcon.setImageResource(UNSELECTED_ICONS[position]);
                    tabText.setText("");
                    tabText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                }

            }

            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager.setOffscreenPageLimit(5);
        tabLayout.getTabAt(fragPosition).select();
    }

    private String getTabTitle(int position) {
        switch (position) {
            case 1: return "Dashboard";
            case 2: return "Alerts";
            case 3: return "Requests";
            case 4: return "Profile";
            default: return "Home";
        }
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        public ViewPagerAdapter(Context context, FragmentManager fm) { super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); }
        @NonNull @Override public Fragment getItem(int position) {
            switch (position) {
                case 1: return new DashboardFragment();
                case 2: return new NotificationsFragment();
                case 3: return new MatchRequestsFragment();
                case 4: return new ProfileFragment();
                default: return new SkillMatchmakingFragment();
            }
        }
        @Override public int getCount() { return 5; }
    }

    private void setupDrawerClicks() {
        int[] navIds = {R.id.cnicvar_nav, R.id.help_Support_nav, R.id.session_rate_nav, R.id.report_user_nav, R.id.search_user_nav, R.id.session_details_nav, R.id.session_reminder_nav, R.id.settings_nav, R.id.data_collextion_nav, R.id.chat_system_nav, R.id.skill_test_nav};
        for (int id : navIds) {
            View v = findViewById(id);
            if (v != null) v.setOnClickListener(this::onDrawerItemClick);
        }
    }

    private void onDrawerItemClick(View view) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        int id = view.getId();
        if (id == R.id.session_rate_nav) { showSessionRateDialog(); return; }
        Intent intent;
        if (id == R.id.cnicvar_nav) intent = new Intent(this, ActivityCNICVarification.class);
        else if (id == R.id.help_Support_nav) intent = new Intent(this, Help_Support_Activity.class);
        else if (id == R.id.report_user_nav) intent = new Intent(this, Report_User_Activity.class);
        else if (id == R.id.search_user_nav) intent = new Intent(this, SearchActivity.class);
        else if (id == R.id.session_details_nav) intent = new Intent(this, SessionDetailsActivity.class);
        else if (id == R.id.session_reminder_nav) intent = new Intent(this, Session_Reminder_Activity.class);
        else if (id == R.id.settings_nav) intent = new Intent(this, SettingsActivity.class);
        else if (id == R.id.skill_test_nav) intent = new Intent(this, SkillSelectionActivity.class);
        else if (id == R.id.data_collextion_nav) intent = new Intent(this, DataCllectionActivity.class);
        else if (id == R.id.chat_system_nav) intent = new Intent(this, ChatListActivity.class);
        else return;
        startActivity(intent);
    }

    private void showSessionRateDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_rate_popup, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        View closeBtn = dialogView.findViewById(R.id.btn_close);
        if (closeBtn != null) closeBtn.setOnClickListener(v -> dialog.dismiss());
    }
}
