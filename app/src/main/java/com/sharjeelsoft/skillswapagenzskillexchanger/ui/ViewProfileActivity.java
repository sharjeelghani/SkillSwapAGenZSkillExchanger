package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.FCMV1Helper;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.NotificationModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView profilePic, btnBack;
    private TextView tvFullName, tvUsername, tvJob, tvLocation, tvGender, tvEducation, tvExperience, btnMatch, btnReport, btnUnmatch, btnMessage;
    private TextView btnAcceptReq, btnDeclineReq;
    private LinearLayout layoutConnectedActions, layoutRequestActions;
    private ChipGroup cgTeaching, cgLearning;
    private DatabaseReference userRef, currentUserRef, dbRef;
    private String viewedUsername, currentUsername, currentUserFullName;
    private MySharedprefsClass sharedPrefs;
    private FCMV1Helper fcmv1Helper;
    private HelperClass viewedUser;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        viewedUsername = getIntent().getStringExtra("userName");
        if (viewedUsername == null || viewedUsername.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sharedPrefs = new MySharedprefsClass(this);
        currentUsername = sharedPrefs.getStringValue("username");
        fcmv1Helper = new FCMV1Helper(this);
        dbRef = FirebaseDatabase.getInstance().getReference();

        initViews();
        
        userRef = dbRef.child("user").child(viewedUsername);
        currentUserRef = dbRef.child("user").child(currentUsername);
        
        loadCurrentUserData();
        loadViewedUserData();
        checkProfileStatus();

        btnBack.setOnClickListener(v -> finish());
        
        btnMatch.setOnClickListener(v -> sendMatchRequest());
        btnAcceptReq.setOnClickListener(v -> acceptMatchRequest());
        btnDeclineReq.setOnClickListener(v -> declineMatchRequest());

        btnUnmatch.setOnClickListener(v -> unmatchUser());

        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(ViewProfileActivity.this, ChatActivity.class);
            intent.putExtra("targetUsername", viewedUsername);
            intent.putExtra("targetFullName", tvFullName.getText().toString());
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(ViewProfileActivity.this, Report_User_Activity.class);
            intent.putExtra("reportedUsername", viewedUsername);
            startActivity(intent);
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
        btnReport = findViewById(R.id.btn_report_user);
        btnUnmatch = findViewById(R.id.btn_unmatch);
        btnMessage = findViewById(R.id.btn_message);
        btnAcceptReq = findViewById(R.id.btn_accept_req);
        btnDeclineReq = findViewById(R.id.btn_decline_req);
        layoutConnectedActions = findViewById(R.id.layout_connected_actions);
        layoutRequestActions = findViewById(R.id.layout_request_actions);
    }

    private void loadCurrentUserData() {
        currentUserRef.child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserFullName = snapshot.getValue(String.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadViewedUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    viewedUser = snapshot.getValue(HelperClass.class);
                    if (viewedUser != null) {
                        updateUI(viewedUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkProfileStatus() {
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                boolean isConnected = snapshot.child("allConnections").hasChild(viewedUsername);
                boolean requestReceived = snapshot.child("matchRequests").child("received").hasChild(viewedUsername);
                boolean requestSent = snapshot.child("matchRequests").child("sent").hasChild(viewedUsername);

                if (isConnected) {
                    btnMatch.setVisibility(View.GONE);
                    layoutRequestActions.setVisibility(View.GONE);
                    layoutConnectedActions.setVisibility(View.VISIBLE);
                } else if (requestReceived) {
                    btnMatch.setVisibility(View.GONE);
                    layoutConnectedActions.setVisibility(View.GONE);
                    layoutRequestActions.setVisibility(View.VISIBLE);
                } else if (requestSent) {
                    btnMatch.setVisibility(View.VISIBLE);
                    btnMatch.setText("Request Sent");
                    btnMatch.setEnabled(false);
                    btnMatch.setAlpha(0.6f);
                    layoutConnectedActions.setVisibility(View.GONE);
                    layoutRequestActions.setVisibility(View.GONE);
                } else {
                    btnMatch.setVisibility(View.VISIBLE);
                    btnMatch.setText("Connect");
                    btnMatch.setEnabled(true);
                    btnMatch.setAlpha(1.0f);
                    layoutConnectedActions.setVisibility(View.GONE);
                    layoutRequestActions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMatchRequest() {
        if (viewedUser == null) return;
        long timestamp = System.currentTimeMillis();

        String notiId = dbRef.child("user").child(viewedUsername).child("notifications").push().getKey();
        NotificationModel notification = new NotificationModel(
                notiId,
                "New Match Request",
                "You have a new connection request from " + (currentUserFullName != null ? currentUserFullName : currentUsername) + "!",
                currentUsername,
                timestamp,
                "match_request"
        );

        Map<String, Object> updates = new HashMap<>();
        updates.put("user/" + currentUsername + "/matchRequests/sent/" + viewedUsername, timestamp);
        updates.put("user/" + viewedUsername + "/matchRequests/received/" + currentUsername, timestamp);
        if (notiId != null) {
            updates.put("user/" + viewedUsername + "/notifications/" + notiId, notification);
        }

        dbRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showRequestSentPopup();
                String targetFcmToken = viewedUser.getFcmToken();
                if (targetFcmToken != null && !targetFcmToken.isEmpty()) {
                    fcmv1Helper.sendNotification(targetFcmToken, notification.getTitle(), notification.getMessage(), "requests");
                }
            }
        });
    }

    private void showRequestSentPopup() {
        if (isFinishing() || isDestroyed()) return;
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_request_sent, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
        new Handler().postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                dialog.dismiss();
            }
        }, 2000);
    }

    private void acceptMatchRequest() {
        if (viewedUser == null) return;
        long timestamp = System.currentTimeMillis();
        Map<String, Object> updates = new HashMap<>();
        updates.put("user/" + currentUsername + "/allConnections/" + viewedUsername, true);
        updates.put("user/" + viewedUsername + "/allConnections/" + currentUsername, true);
        updates.put("user/" + currentUsername + "/matchRequests/received/" + viewedUsername, null);
        updates.put("user/" + viewedUsername + "/matchRequests/sent/" + currentUsername, null);

        String notiId = dbRef.child("user").child(viewedUsername).child("notifications").push().getKey();
        String message = "Your match request has been accepted by " + (currentUserFullName != null ? currentUserFullName : currentUsername);
        NotificationModel notification = new NotificationModel(notiId, "Match Request Accepted", message, currentUsername, timestamp, "match_accepted");

        if (notiId != null) {
            updates.put("user/" + viewedUsername + "/notifications/" + notiId, notification);
        }

        dbRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
                String targetFcmToken = viewedUser.getFcmToken();
                if (targetFcmToken != null && !targetFcmToken.isEmpty()) {
                    fcmv1Helper.sendNotification(targetFcmToken, "Match Request Accepted", message, "alerts");
                }
            }
        });
    }

    private void declineMatchRequest() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("user/" + currentUsername + "/matchRequests/received/" + viewedUsername, null);
        updates.put("user/" + viewedUsername + "/matchRequests/sent/" + currentUsername, null);

        dbRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Request declined", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unmatchUser() {
        if (viewedUser == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("user/" + currentUsername + "/allConnections/" + viewedUsername, null);
        updates.put("user/" + viewedUsername + "/allConnections/" + currentUsername, null);

        String notiId = dbRef.child("user").child(viewedUsername).child("notifications").push().getKey();
        String message = "You are Unmatched by " + (currentUserFullName != null ? currentUserFullName : currentUsername);
        NotificationModel notification = new NotificationModel(notiId, "Unmatched", message, currentUsername, System.currentTimeMillis(), "unmatch");

        if (notiId != null) {
            updates.put("user/" + viewedUsername + "/notifications/" + notiId, notification);
        }

        dbRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "User Unmatched", Toast.LENGTH_SHORT).show();
                String targetFcmToken = viewedUser.getFcmToken();
                if (targetFcmToken != null && !targetFcmToken.isEmpty()) {
                    fcmv1Helper.sendNotification(targetFcmToken, "Unmatched", message, "alerts");
                }
            }
        });
    }

    private void updateUI(HelperClass user) {
        if (isFinishing() || isDestroyed()) return;

        tvFullName.setText(user.getFullName() != null ? user.getFullName() : "NA");
        tvUsername.setText(user.getUsername() != null ? "@" + user.getUsername() : "@NA");
        tvJob.setText(user.getCurrentJob() != null && !user.getCurrentJob().isEmpty() ? user.getCurrentJob() : "NA");
        tvLocation.setText(user.getCountry() != null && !user.getCountry().isEmpty() ? user.getCountry() : "NA");
        tvGender.setText(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "NA");
        tvEducation.setText(user.getEducation() != null && !user.getEducation().isEmpty() ? user.getEducation() : "NA");
        tvExperience.setText(user.getExperience() != null && !user.getExperience().isEmpty() ? user.getExperience() + " Experience" : "NA");

        int placeholder = getGenderPlaceholder(user.getGender());

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this).load(user.getProfileImageUrl()).placeholder(placeholder).error(placeholder).into(profilePic);
        } else {
            profilePic.setImageResource(placeholder);
        }

        populateChips(cgTeaching, user.getTeachingSkills());
        populateChips(cgLearning, user.getLearningInterests());
    }

    private int getGenderPlaceholder(String gender) {
        return (gender != null && gender.equalsIgnoreCase("Female")) ? R.drawable.avatar : R.drawable.man;
    }

    private void populateChips(ChipGroup group, List<String> skills) {
        group.removeAllViews();
        if (skills == null || skills.isEmpty()) {
            Chip chip = new Chip(this);
            chip.setText("NA");
            chip.setChipBackgroundColorResource(R.color.button_fill_trans);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setClickable(false);
            chip.setFocusable(false);
            group.addView(chip);
            return;
        }
        for (String skill : skills) {
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setChipBackgroundColorResource(R.color.teal_light);
            chip.setTextColor(getResources().getColor(R.color.bg_dark));
            chip.setClickable(false);
            chip.setFocusable(false);
            group.addView(chip);
        }
    }
}
