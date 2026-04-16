package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

public class ProfileFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvName, tvLocation, tvJob;
    private ImageView profilePic;
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        tvName = view.findViewById(R.id.name);
        tvLocation = view.findViewById(R.id.location);
        tvJob = view.findViewById(R.id.tv_job);
        profilePic = view.findViewById(R.id.profile_pic);

        sharedPrefs = new MySharedprefsClass(requireContext());
        String username = sharedPrefs.getStringValue("username");

        if (username != null && !username.equals("new_user")) {
            userRef = FirebaseDatabase.getInstance().getReference("user").child(username);
            loadUserData();
        }

        setupViewPager();
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HelperClass user = snapshot.getValue(HelperClass.class);
                    if (user != null) {
                        tvName.setText(user.getFullName());
                        tvLocation.setText(user.getCountry());
                        tvJob.setText(user.getCurrentJob());

                        if (isAdded()) {
                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                Glide.with(requireContext())
                                        .load(user.getProfileImageUrl())
                                        .placeholder(R.drawable.man)
                                        .into(profilePic);
                            } else {
                                profilePic.setImageResource(R.drawable.man);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupViewPager() {
        viewPager.setAdapter(new ProfilePagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Skills");
                    break;
                case 1:
                    tab.setText("Sessions");
                    break;
                case 2:
                    tab.setText("Badges");
                    break;
                case 3:
                    tab.setText("Certificates");
                    break;
            }
        }).attach();
    }

    private static class ProfilePagerAdapter extends FragmentStateAdapter {

        public ProfilePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new SkillsFragment();
                case 1:
                    return new SessionsFragment();
                case 2:
                    return new BadgesFragment();
                case 3:
                    return new CertificatesFragment();
                default:
                    return new SkillsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
