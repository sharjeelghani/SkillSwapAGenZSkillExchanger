package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.List;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView profilePic, btnBack;
    private TextView tvFullName, tvUsername, tvJob, tvLocation, tvGender, tvEducation, tvExperience, btnMatch;
    private ChipGroup cgTeaching, cgLearning;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        String username = getIntent().getStringExtra("userName");
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        
        userRef = FirebaseDatabase.getInstance().getReference("user").child(username);
        loadUserData();

        btnBack.setOnClickListener(v -> finish());
        
        btnMatch.setOnClickListener(v -> {
            Toast.makeText(this, "Match Request Sent!", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        profilePic = findViewById(R.id.profile_pic);
        btnBack = findViewById(R.id.btn_back);
        tvFullName = findViewById(R.id.tv_full_name);
        tvUsername = findViewById(R.id.tv_username);
        tvJob = findViewById(R.id.tv_job_detail);
        tvLocation = findViewById(R.id.tv_location_detail);
        tvGender = findViewById(R.id.tv_gender_detail);
        tvEducation = findViewById(R.id.tv_education_detail);
        tvExperience = findViewById(R.id.tv_experience_detail);
        cgTeaching = findViewById(R.id.cg_teaching);
        cgLearning = findViewById(R.id.cg_learning);
        btnMatch = findViewById(R.id.btn_match);
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HelperClass user = snapshot.getValue(HelperClass.class);
                    if (user != null) {
                        updateUI(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(HelperClass user) {
        tvFullName.setText(user.getFullName() != null ? user.getFullName() : "NA");
        tvUsername.setText(user.getUsername() != null ? "@" + user.getUsername() : "@NA");
        tvJob.setText(user.getCurrentJob() != null && !user.getCurrentJob().isEmpty() ? user.getCurrentJob() : "NA");
        tvLocation.setText(user.getCountry() != null && !user.getCountry().isEmpty() ? user.getCountry() : "NA");
        tvGender.setText(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "NA");
        tvEducation.setText(user.getEducation() != null && !user.getEducation().isEmpty() ? user.getEducation() : "NA");
        tvExperience.setText(user.getExperience() != null && !user.getExperience().isEmpty() ? user.getExperience() + " Experience" : "NA");

        int placeholder = getGenderPlaceholder(user.getGender());

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(profilePic);
        } else {
            profilePic.setImageResource(placeholder);
        }

        populateChips(cgTeaching, user.getTeachingSkills());
        populateChips(cgLearning, user.getLearningInterests());
    }

    private int getGenderPlaceholder(String gender) {
        if (gender != null && gender.equalsIgnoreCase("Female")) {
            return R.drawable.avatar;
        } else {
            return R.drawable.man;
        }
    }

    private void populateChips(ChipGroup group, List<String> skills) {
        group.removeAllViews();
        if (skills == null || skills.isEmpty()) {
            Chip chip = new Chip(this);
            chip.setText("NA");
            chip.setChipBackgroundColorResource(R.color.button_fill_trans);
            chip.setTextColor(getResources().getColor(R.color.white));
            group.addView(chip);
            return;
        }
        for (String skill : skills) {
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setChipBackgroundColorResource(R.color.teal_light);
            chip.setTextColor(getResources().getColor(R.color.bg_dark));
            group.addView(chip);
        }
    }
}
