package com.sharjeelsoft.skillswapagenzskillexchanger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ActivityCNICVarification;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ChatActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.DashboardFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.DataCllectionActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.Help_Support_Activity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.NotificationsFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.ProfileFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.Report_User_Activity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SearchActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SessionDetailsActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.Session_Reminder_Activity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SettingsActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SkillMatchmakingFragment;
import com.sharjeelsoft.skillswapagenzskillexchanger.ui.SkillSelectionActivity;

public class MainActivity extends AppCompatActivity {

    private NavigationView nav_view;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    public ViewPager viewPager;
    View contentView;

    public ImageView drawer_icon;
    public int fragPosition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupDrawerClicks();

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.TRANSPARENT);
        gradientDrawable.setCornerRadius(50);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        contentView = findViewById(R.id.content);
        nav_view = (NavigationView) findViewById(R.id.nav_view);
        drawer_icon = findViewById(R.id.drawer_icon_home);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawer_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

//        mDrawerLayout.addDrawerListener(mDrawerToggle);
//        mDrawerLayout.setElevation(0);
        nav_view.setElevation(0f);
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
    }
    private void SetUpPager() {
        // Define the icons for the selected and unselected states
        int[] SELECTED_ICONS = {R.drawable.home_sel, R.drawable.dashboard_sel, R.drawable.notification_sel, R.drawable.user_sel};

        int[] UNSELECTED_ICONS = {R.drawable.home_unsel, R.drawable.dashboard_unsel, R.drawable.notification_unsel, R.drawable.user_unsel};

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);

            if (tab != null) {
                View customView = LayoutInflater.from(this).inflate(R.layout.tab_custom_view, null);
                ImageView tabIcon = customView.findViewById(R.id.tab_icon);
                TextView tabText = customView.findViewById(R.id.tab_text);

                tabIcon.setImageResource(UNSELECTED_ICONS[i]);
                tabText.setText(null);

                tab.setCustomView(customView);
                // Select the first tab by default
            }
            tab.select();
        }
        // Select the first tab by default

        TabLayout.Tab tab = tabLayout.getTabAt(0);
        View customView = tab.getCustomView();
        if (customView != null) {
            ImageView tabIcon = customView.findViewById(R.id.tab_icon);
            TextView tabText = customView.findViewById(R.id.tab_text);
            RelativeLayout bg_layout = customView.findViewById(R.id.bg_layout);
            bg_layout.setBackground(getDrawable(R.drawable.bg_cnic_card));

            tabIcon.setImageResource(SELECTED_ICONS[0]);
            tabText.setText(getTabTitle(0));
            tabText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
        }
        tabLayout.selectTab(tabLayout.getTabAt(0));
        viewPager.setOffscreenPageLimit(4);

        tabLayout.getTabAt(0).setIcon(R.drawable.home_unsel);
        tabLayout.getTabAt(1).setIcon(R.drawable.dashboard_unsel);
        tabLayout.getTabAt(2).setIcon(R.drawable.notification_unsel);
        tabLayout.getTabAt(3).setIcon(R.drawable.user_unsel);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                int position = tab.getPosition();
                fragPosition = position;
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
                    tabText.setText(null);
                    tabText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private String getTabTitle(int position) {
        switch (position) {
            case 1:
                return "Dashboard";
            case 2:
                return "Notifications";
            case 3:
                return "Profile";
            case 0:
            default:
                return "Home " + "" + "";
        }
    }
    public static class ViewPagerAdapter extends FragmentPagerAdapter {


        private final Context mContext;

        public ViewPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.mContext = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 1:
                    return new DashboardFragment();

                case 2:
                    return new NotificationsFragment();


                case 3:
                    return new ProfileFragment();

                case 0:
                default:
                    return new SkillMatchmakingFragment();


            }


        }

        @Override
        public int getCount() {
            return 4;
        }
    }
    private void setupDrawerClicks() {

        int[] navIds = {
                R.id.cnicvar_nav,
                R.id.help_Support_nav,
                R.id.session_rate_nav,
                R.id.report_user_nav,
                R.id.search_user_nav,
                R.id.session_details_nav,
                R.id.session_reminder_nav,
                R.id.settings_nav,
                R.id.data_collextion_nav,
                R.id.chat_system_nav,
                R.id.skill_test_nav
        };

        for (int id : navIds) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(this::onDrawerItemClick);
            }
        }

    }
    private void onDrawerItemClick(View view) {
        // close the drawer immediately for smooth UX
        mDrawerLayout.closeDrawer(GravityCompat.START);

        final int id = view.getId();

        // handle the special-case dialog first
        if (id == R.id.session_rate_nav) {
            // close drawer then show dialog after small delay so drawer closing animation doesn't overlap
            mDrawerLayout.postDelayed(() -> showSessionRateDialog(), 250);
            return;
        }

        // Otherwise prepare an Intent for other destinations
         Intent intent;
        if (id == R.id.cnicvar_nav) {
            intent = new Intent(this, ActivityCNICVarification.class);
        } else if (id == R.id.help_Support_nav) {
            intent = new Intent(this, Help_Support_Activity.class);
        } else if (id == R.id.report_user_nav) {
            intent = new Intent(this, Report_User_Activity.class);
        } else if (id == R.id.search_user_nav) {
            intent = new Intent(this, SearchActivity.class);
        } else if (id == R.id.session_details_nav) {
            intent = new Intent(this, SessionDetailsActivity.class);
        } else if (id == R.id.session_reminder_nav) {
            intent = new Intent(this, Session_Reminder_Activity.class);
        } else if (id == R.id.settings_nav) {
            intent = new Intent(this, SettingsActivity.class);
        }  else if (id == R.id.skill_test_nav) {
            intent = new Intent(this, SkillSelectionActivity.class);
        } else if (id == R.id.data_collextion_nav) {
            intent = new Intent(this, DataCllectionActivity.class);
        }  else if (id == R.id.chat_system_nav) {
            intent = new Intent(this, ChatActivity.class);
        } else {
            // unknown id — nothing to do
            return;
        }

        // start Activity after drawer animation finishes
        startActivity(intent);
    }

    private void showSessionRateDialog() {

        // Close drawer first
        mDrawerLayout.closeDrawer(GravityCompat.START);

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.activity_rate_popup, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Optional: Transparent background
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();

        View closeBtn = dialogView.findViewById(R.id.btn_close);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        }
    }


}