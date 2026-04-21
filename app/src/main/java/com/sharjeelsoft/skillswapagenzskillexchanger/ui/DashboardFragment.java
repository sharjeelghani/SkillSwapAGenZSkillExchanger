package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class DashboardFragment extends Fragment {

    private TextView tvMySkillsCount;
    private TextView tvMatchesCount;
    private TextView tvSessionsCount;
    private LinearLayout layoutAddSkill;
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference usersRef;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvMySkillsCount = view.findViewById(R.id.tv_count_my_skills);
        tvMatchesCount  = view.findViewById(R.id.tv_count_matches);
        tvSessionsCount = view.findViewById(R.id.tv_count_sessions);
        layoutAddSkill  = view.findViewById(R.id.layout_add_skill);

        sharedPrefs = new MySharedprefsClass(requireContext());
        usersRef = FirebaseDatabase.getInstance().getReference("user");

        loadDashboardData();

        layoutAddSkill.setOnClickListener(v -> openAddSkillActivity());

        return view;
    }

    private void loadDashboardData() {
        String currentUsername = sharedPrefs.getStringValue("username");
        if (currentUsername == null || currentUsername.equals("new_user")) return;

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                DataSnapshot meSnapshot = snapshot.child(currentUsername);
                if (!meSnapshot.exists()) return;

                HelperClass uMe = meSnapshot.getValue(HelperClass.class);
                if (uMe == null) return;

                // 1. My Skills Count
                long mySkillsCount = 0;
                if (uMe.getTeachingSkills() != null) {
                    mySkillsCount = uMe.getTeachingSkills().size();
                }
                tvMySkillsCount.setText(String.valueOf(mySkillsCount));

                // 2. Mutual Match Count (Skill Swap)
                int matchCount = 0;
                List<String> myLearning = uMe.getLearningInterests() != null ? uMe.getLearningInterests() : new ArrayList<>();
                List<String> myTeaching = uMe.getTeachingSkills() != null ? uMe.getTeachingSkills() : new ArrayList<>();
                
                List<String> sentRequests = new ArrayList<>();
                if (meSnapshot.hasChild("matchRequests/sent")) {
                    for (DataSnapshot ds : meSnapshot.child("matchRequests/sent").getChildren()) {
                        sentRequests.add(ds.getKey());
                    }
                }
                
                List<String> connections = new ArrayList<>();
                if (meSnapshot.hasChild("allConnections")) {
                    for (DataSnapshot ds : meSnapshot.child("allConnections").getChildren()) {
                        connections.add(ds.getKey());
                    }
                }

                for (DataSnapshot userDS : snapshot.getChildren()) {
                    String otherUsername = userDS.getKey();
                    if (otherUsername == null || otherUsername.equals(currentUsername)) continue;
                    
                    // Skip if already requested or connected
                    if (sentRequests.contains(otherUsername) || connections.contains(otherUsername)) continue;

                    HelperClass otherUser = userDS.getValue(HelperClass.class);
                    if (otherUser != null) {
                        boolean otherTeachesWhatILearn = false;
                        if (otherUser.getTeachingSkills() != null) {
                            for (String s : otherUser.getTeachingSkills()) {
                                if (myLearning.contains(s)) {
                                    otherTeachesWhatILearn = true;
                                    break;
                                }
                            }
                        }

                        boolean iTeachWhatOtherLearns = false;
                        if (otherUser.getLearningInterests() != null) {
                            for (String s : otherUser.getLearningInterests()) {
                                if (myTeaching.contains(s)) {
                                    iTeachWhatOtherLearns = true;
                                    break;
                                }
                            }
                        }

                        if (otherTeachesWhatILearn && iTeachWhatOtherLearns) {
                            matchCount++;
                        }
                    }
                }
                tvMatchesCount.setText(String.valueOf(matchCount));

                // 3. Sessions Count
                // Logic for upcoming sessions can be added here once session data structure is clear
                // For now, keeping it as is or setting to 0 if not implemented.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void openAddSkillActivity() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), DataCllectionActivity.class);
        startActivity(intent);
    }
}
