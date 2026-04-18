package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

import java.util.ArrayList;
import java.util.List;

public class SkillMatchmakingFragment extends Fragment {

    private List<HelperClass> userList = new ArrayList<>();
    private List<String> myLearningInterests = new ArrayList<>();
    private int currentIndex = 0;

    private TextView tvName;
    private ImageView imgAvatar;
    private ChipGroup cgMatchingSkills;
    private TextView btnConnect;
    private TextView btnPass;
    private TextView btnViewProfile;
    private View matchCard, layoutLoading;
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_skill_matchmaking, container, false);

        sharedPrefs = new MySharedprefsClass(requireContext());
        usersRef = FirebaseDatabase.getInstance().getReference("user");

        // --- bind views ---
        tvName = view.findViewById(R.id.tv_name);
        imgAvatar = view.findViewById(R.id.img_avatar);
        cgMatchingSkills = view.findViewById(R.id.cg_matching_skills);
        btnConnect = view.findViewById(R.id.btn_connect);
        btnPass = view.findViewById(R.id.btn_pass);
        btnViewProfile = view.findViewById(R.id.btn_view_profile);
        matchCard = view.findViewById(R.id.match_card);
        layoutLoading = view.findViewById(R.id.layout_loading);

        // --- button handlers ---
        btnConnect.setOnClickListener(v -> goToNextMatch());
        btnPass.setOnClickListener(v -> goToNextMatch());

        btnViewProfile.setOnClickListener(v -> {
            if (!userList.isEmpty() && currentIndex < userList.size()) {
                HelperClass selectedUser = userList.get(currentIndex);
                if (selectedUser != null && selectedUser.getUsername() != null) {
                    Intent intent = new Intent(getActivity(), ViewProfileActivity.class);
                    intent.putExtra("userName", selectedUser.getUsername());
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening when fragment becomes visible (e.g., returning from profile update)
        loadUsersFromFirebase();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Always remove listeners in onStop to prevent memory leaks
        if (usersRef != null && usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }

    private void loadUsersFromFirebase() {
        // Show loading state
        layoutLoading.setVisibility(View.VISIBLE);
        matchCard.setVisibility(View.GONE);

        if (usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }

        String currentLoggedInUser = sharedPrefs.getStringValue("username");

        usersListener = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                
                userList.clear();
                myLearningInterests.clear();

                // 1. First Pass: Get the logged-in user's latest learning interests
                for (DataSnapshot ds : snapshot.getChildren()) {
                    HelperClass u = ds.getValue(HelperClass.class);
                    if (u != null && u.getUsername() != null && u.getUsername().equals(currentLoggedInUser)) {
                        if (u.getLearningInterests() != null) {
                            myLearningInterests.addAll(u.getLearningInterests());
                        }
                        break;
                    }
                }

                // 2. Second Pass: Populate matches list (excluding self)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HelperClass user = dataSnapshot.getValue(HelperClass.class);
                    if (user != null && user.getUsername() != null) {
                        if (!user.getUsername().equals(currentLoggedInUser)) {
                            userList.add(user);
                        }
                    }
                }

                layoutLoading.setVisibility(View.GONE);

                if (!userList.isEmpty()) {
                    matchCard.setVisibility(View.VISIBLE);
                    // If index is out of bounds after update, reset it
                    if (currentIndex >= userList.size()) currentIndex = 0;
                    showMatch(currentIndex);
                } else {
                    matchCard.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "No more users found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Sync Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showMatch(int index) {
        if (!isAdded() || userList.isEmpty() || index >= userList.size()) return;

        HelperClass user = userList.get(index);
        if (user == null) return;

        tvName.setText(user.getFullName() != null ? user.getFullName() : "NA");

        // Filter user's teaching skills by my current learning interests
        cgMatchingSkills.removeAllViews();
        List<String> matchingSkills = new ArrayList<>();
        if (user.getTeachingSkills() != null) {
            for (String skill : user.getTeachingSkills()) {
                if (myLearningInterests.contains(skill)) {
                    matchingSkills.add(skill);
                }
            }
        }

        if (!matchingSkills.isEmpty()) {
            for (String skill : matchingSkills) {
                Chip chip = new Chip(requireContext());
                chip.setText(skill);
                chip.setChipBackgroundColorResource(R.color.button_fill_trans);
                chip.setTextColor(getResources().getColor(R.color.white));
                chip.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.teal_light)));
                chip.setChipStrokeWidth(3);
                chip.setChipCornerRadius(50);
                cgMatchingSkills.addView(chip);
            }
        } else {
            Chip chip = new Chip(requireContext());
            chip.setText("NA");
            chip.setChipBackgroundColorResource(R.color.button_fill_trans);
            chip.setTextColor(getResources().getColor(R.color.white));
            cgMatchingSkills.addView(chip);
        }

        // Handle profile image with gender-specific placeholders
        int placeholder = getGenderPlaceholder(user.getGender());
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(placeholder);
        }
    }

    private int getGenderPlaceholder(String gender) {
        if (gender != null && gender.equalsIgnoreCase("Female")) {
            return R.drawable.avatar;
        } else {
            return R.drawable.man;
        }
    }

    private void goToNextMatch() {
        if (userList.isEmpty()) return;
        currentIndex = (currentIndex + 1) % userList.size();
        showMatch(currentIndex);
    }
}
