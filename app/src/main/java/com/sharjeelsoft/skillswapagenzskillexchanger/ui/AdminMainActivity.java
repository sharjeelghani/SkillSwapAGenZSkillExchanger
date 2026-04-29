package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

public class AdminMainActivity extends AppCompatActivity {
    public ViewPager viewPager;
    MySharedprefsClass AdminprefsClassLog;
    public LinearLayout logout_admin;
    public int fragPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        Toolbar toolbar = findViewById(R.id.app_bar_admin);
        logout_admin = findViewById(R.id.logout_admin);
        AdminprefsClassLog = new MySharedprefsClass(AdminMainActivity.this);
        logout_admin.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                AdminprefsClassLog.saveStringValue("isLogin","signed_up");
                Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        setSupportActionBar(toolbar);
        SetUpPager();
    }

    private void SetUpPager() {
        // Define the icons for the selected and unselected states
        int[] SELECTED_ICONS = {R.drawable.adminsel_dashboard, R.drawable.adminsel_report, R.drawable.adminsel_profile, R.drawable.adminsel_acceptance, R.drawable.adminsel_certiapprove};

        int[] UNSELECTED_ICONS = {R.drawable.adminunsel_dashboard, R.drawable.adminunsel_report, R.drawable.adminunsel_profile, R.drawable.adminunsel_acceptance, R.drawable.adminunsel_certiapprove};

        AdminMainActivity.ViewPagerAdapter viewPagerAdapter = new AdminMainActivity.ViewPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(viewPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);

            if (tab != null) {
                View customView = LayoutInflater.from(this).inflate(R.layout.tab_admin_custom_view, null);
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
            tabText.setTextColor(ContextCompat.getColor(AdminMainActivity.this, R.color.white));
        }
        tabLayout.selectTab(tabLayout.getTabAt(0));
        viewPager.setOffscreenPageLimit(5);

        tabLayout.getTabAt(0).setIcon(R.drawable.adminunsel_dashboard);
        tabLayout.getTabAt(1).setIcon(R.drawable.adminunsel_report);
        tabLayout.getTabAt(2).setIcon(R.drawable.adminunsel_profile);
        tabLayout.getTabAt(3).setIcon(R.drawable.adminunsel_acceptance);
        tabLayout.getTabAt(4).setIcon(R.drawable.adminunsel_certiapprove);

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
                    tabText.setTextColor(ContextCompat.getColor(AdminMainActivity.this, R.color.white));

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
                    tabText.setTextColor(ContextCompat.getColor(AdminMainActivity.this, R.color.white));
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
                return "Reports";
            case 2:
                return "Reported Users";
            case 3:
                return "Skill Approval";
            case 4:
                return "Certificate Approval";
            case 0:
            default:
                return "Dashboard " + "" + "";
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
                    return new AdminReportsFragment();

                case 2:
                    return new ReportedUsersFragment();

                case 3:
                    return new SkillApprovalFragment();

                case 4:
                    return new CertificateApprovalFragment();

                case 0:
                default:
                    return new AdminDashboardFragment();


            }


        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
