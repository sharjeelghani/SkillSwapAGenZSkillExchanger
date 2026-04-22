package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.FCMV1Helper;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.NotificationModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillMatchmakingFragment extends Fragment {

    private List<HelperClass> userList = new ArrayList<>();
    private List<String> myLearningInterests = new ArrayList<>();
    private List<String> myTeachingSkills = new ArrayList<>();
    private List<String> sentRequests = new ArrayList<>();
    private List<String> receivedRequests = new ArrayList<>();
    private List<String> connections = new ArrayList<>();
    private int currentIndex = 0;

    private TextView tvName, tvRequestStatus;
    private ImageView imgAvatar;
    private ChipGroup cgTeachingMatches, cgLearningMatches;
    private TextView btnConnect;
    private TextView btnPass;
    private TextView btnViewProfile;
    private View matchCard, layoutLoading;
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference usersRef;
    private ValueEventListener usersListener;
    private FCMV1Helper fcmv1Helper;
    private String currentUserFullName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_skill_matchmaking, container, false);

        sharedPrefs = new MySharedprefsClass(requireContext());
        usersRef = FirebaseDatabase.getInstance().getReference("user");
        fcmv1Helper = new FCMV1Helper(requireContext());

        // --- bind views ---
        tvName = view.findViewById(R.id.tv_name);
        tvRequestStatus = view.findViewById(R.id.tv_request_status);
        imgAvatar = view.findViewById(R.id.img_avatar);
        cgTeachingMatches = view.findViewById(R.id.cg_teaching_matches);
        cgLearningMatches = view.findViewById(R.id.cg_learning_matches);
        btnConnect = view.findViewById(R.id.btn_connect);
        btnPass = view.findViewById(R.id.btn_pass);
        btnViewProfile = view.findViewById(R.id.btn_view_profile);
        matchCard = view.findViewById(R.id.match_card);
        layoutLoading = view.findViewById(R.id.layout_loading);

        // --- button handlers ---
        btnConnect.setOnClickListener(v -> handleConnectAction());
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

    private void handleConnectAction() {
        if (userList.isEmpty() || currentIndex >= userList.size()) return;
        HelperClass targetUser = userList.get(currentIndex);
        String targetUsername = targetUser.getUsername();

        if (receivedRequests.contains(targetUsername)) {
            acceptMatchRequest(targetUser);
        } else {
            sendMatchRequest(targetUser);
        }
    }

    private void sendMatchRequest(HelperClass targetUser) {
        String currentUsername = sharedPrefs.getStringValue("username");
        String targetUsername = targetUser.getUsername();
        String targetFcmToken = targetUser.getFcmToken();

        if (currentUsername == null || targetUsername == null) return;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        long timestamp = System.currentTimeMillis();

        // Prepare Notification
        String notiId = dbRef.child("user").child(targetUsername).child("notifications").push().getKey();
        String senderDisplayName = (currentUserFullName != null && !currentUserFullName.isEmpty()) ? currentUserFullName : currentUsername;
        NotificationModel notification = new NotificationModel(
                notiId,
                "New Match Request",
                "You have a new connection request from " + senderDisplayName + "!",
                currentUsername,
                timestamp,
                "match_request"
        );

        Map<String, Object> updates = new HashMap<>();
        updates.put("user/" + currentUsername + "/matchRequests/sent/" + targetUsername, timestamp);
        updates.put("user/" + targetUsername + "/matchRequests/received/" + currentUsername, timestamp);
        if (notiId != null) {
            updates.put("user/" + targetUsername + "/notifications/" + notiId, notification);
        }

        dbRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showRequestSentPopup();

                // Send Real-time Push Notification via FCM V1
                if (targetFcmToken != null && !targetFcmToken.isEmpty()) {
                    fcmv1Helper.sendNotification(targetFcmToken, notification.getTitle(), notification.getMessage(), "requests");
                }
            }
        });
    }

    private void acceptMatchRequest(HelperClass targetUser) {
        String currentUsername = sharedPrefs.getStringValue("username");
        String targetUsername = targetUser.getUsername();
        String targetFcmToken = targetUser.getFcmToken();

        if (currentUsername == null || targetUsername == null) return;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();
        updates.put("user/" + currentUsername + "/allConnections/" + targetUsername, true);
        updates.put("user/" + targetUsername + "/allConnections/" + currentUsername, true);
        updates.put("user/" + currentUsername + "/matchRequests/received/" + targetUsername, null);
        updates.put("user/" + targetUsername + "/matchRequests/sent/" + currentUsername, null);

        // Prepare Notification for the original sender
        String notiId = dbRef.child("user").child(targetUsername).child("notifications").push().getKey();
        String senderDisplayName = (currentUserFullName != null && !currentUserFullName.isEmpty()) ? currentUserFullName : currentUsername;
        String message = "Your match request has been accepted by " + senderDisplayName;
        NotificationModel notification = new NotificationModel(
                notiId,
                "Match Request Accepted",
                message,
                currentUsername,
                timestamp,
                "match_accepted"
        );

        if (notiId != null) {
            updates.put("user/" + targetUsername + "/notifications/" + notiId, notification);
        }

        dbRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Connected with " + targetUser.getFullName(), Toast.LENGTH_SHORT).show();

                // Trigger Real-time Push Notification via FCM V1
                if (targetFcmToken != null && !targetFcmToken.isEmpty()) {
                    fcmv1Helper.sendNotification(targetFcmToken, "Match Request Accepted", message, "alerts");
                }
            }
        });
    }

    private void showRequestSentPopup() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_request_sent, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();

        new Handler().postDelayed(dialog::dismiss, 2000);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadUsersFromFirebase();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (usersRef != null && usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }

    private void loadUsersFromFirebase() {
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
                
                // Track currently shown user to maintain position if possible
                String shownUsername = (userList != null && !userList.isEmpty() && currentIndex < userList.size()) ? userList.get(currentIndex).getUsername() : null;

                userList.clear();
                myLearningInterests.clear();
                myTeachingSkills.clear();
                sentRequests.clear();
                receivedRequests.clear();
                connections.clear();

                DataSnapshot me = snapshot.child(currentLoggedInUser);
                if (me.exists()) {
                    HelperClass uMe = me.getValue(HelperClass.class);
                    if (uMe != null) {
                        currentUserFullName = uMe.getFullName();
                        if (uMe.getLearningInterests() != null) {
                            myLearningInterests.addAll(uMe.getLearningInterests());
                        }
                        if (uMe.getTeachingSkills() != null) {
                            myTeachingSkills.addAll(uMe.getTeachingSkills());
                        }
                    }
                    // Get already sent requests
                    if (me.hasChild("matchRequests/sent")) {
                        for (DataSnapshot ds : me.child("matchRequests/sent").getChildren()) {
                            sentRequests.add(ds.getKey());
                        }
                    }
                    // Get received requests
                    if (me.hasChild("matchRequests/received")) {
                        for (DataSnapshot ds : me.child("matchRequests/received").getChildren()) {
                            receivedRequests.add(ds.getKey());
                        }
                    }
                    // Get already connected users
                    if (me.hasChild("allConnections")) {
                        for (DataSnapshot ds : me.child("allConnections").getChildren()) {
                            connections.add(ds.getKey());
                        }
                    }
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HelperClass user = dataSnapshot.getValue(HelperClass.class);
                    if (user != null && user.getUsername() != null) {
                        String uName = user.getUsername();
                        // Filter: Not self, Not already sent request, Not already connected
                        if (!uName.equals(currentLoggedInUser) && !sentRequests.contains(uName) && !connections.contains(uName)) {

                            // --- Mutual Skill-based Filtering (Skill Swap) ---

                            // 1. Does other user teach what I want to learn?
                            boolean otherTeachesWhatILearn = false;
                            if (user.getTeachingSkills() != null) {
                                for (String skill : user.getTeachingSkills()) {
                                    if (myLearningInterests.contains(skill)) {
                                        otherTeachesWhatILearn = true;
                                        break;
                                    }
                                }
                            }

                            // 2. Do I teach what the other user wants to learn?
                            boolean iTeachWhatOtherLearns = false;
                            if (user.getLearningInterests() != null) {
                                for (String interest : user.getLearningInterests()) {
                                    if (myTeachingSkills.contains(interest)) {
                                        iTeachWhatOtherLearns = true;
                                        break;
                                    }
                                }
                            }
                            
                            // It's only a "Swap" match if both conditions are met
                            if (otherTeachesWhatILearn && iTeachWhatOtherLearns) {
                                userList.add(user);
                            }
                        }
                    }
                }

                layoutLoading.setVisibility(View.GONE);

                if (!userList.isEmpty()) {
                    matchCard.setVisibility(View.VISIBLE);
                    
                    // Try to restore position
                    if (shownUsername != null) {
                        for (int i = 0; i < userList.size(); i++) {
                            if (userList.get(i).getUsername().equals(shownUsername)) {
                                currentIndex = i;
                                break;
                            }
                        }
                    }
                    
                    if (currentIndex >= userList.size()) currentIndex = 0;
                    showMatch(currentIndex);
                } else {
                    matchCard.setVisibility(View.GONE);
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

        // Requested you for connection text logic
        if (receivedRequests.contains(user.getUsername())) {
            tvRequestStatus.setVisibility(View.VISIBLE);
            tvRequestStatus.setText("Requested you for conneciton!");
            btnConnect.setText("Accept Request");
        } else {
            tvRequestStatus.setVisibility(View.GONE);
            btnConnect.setText("Connect");
        }

        // --- Matched Teaching Skills ---
        cgTeachingMatches.removeAllViews();
        if (user.getTeachingSkills() != null) {
            for (String skill : user.getTeachingSkills()) {
                if (myLearningInterests.contains(skill)) {
                    addChipToGroup(cgTeachingMatches, skill);
                }
            }
        }

        // --- Matched Learning Interests ---
        cgLearningMatches.removeAllViews();
        if (user.getLearningInterests() != null) {
            for (String interest : user.getLearningInterests()) {
                if (myTeachingSkills.contains(interest)) {
                    addChipToGroup(cgLearningMatches, interest);
                }
            }
        }

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

    private void addChipToGroup(ChipGroup group, String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.button_fill_trans);
        chip.setTextColor(getResources().getColor(R.color.white));
        chip.setChipStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.teal_light)));
        chip.setChipStrokeWidth(3);
        chip.setChipCornerRadius(50);
        chip.setClickable(false);
        chip.setFocusable(false);
        group.addView(chip);
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
