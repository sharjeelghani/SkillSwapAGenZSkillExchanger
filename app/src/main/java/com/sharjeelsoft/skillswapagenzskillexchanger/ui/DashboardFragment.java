package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

public class DashboardFragment extends Fragment {

    private TextView tvMySkillsCount;
    private TextView tvMatchesCount;
    private TextView tvSessionsCount;
    private FrameLayout btnAddSkill;
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference userRef;

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
        btnAddSkill     = view.findViewById(R.id.btn_add_skill);

        sharedPrefs = new MySharedprefsClass(requireContext());
        String username = sharedPrefs.getStringValue("username");

        if (username != null && !username.equals("new_user")) {
            userRef = FirebaseDatabase.getInstance().getReference("user").child(username);
            loadUserStats();
        }

        btnAddSkill.setOnClickListener(v -> openAddSkillActivity());

        return view;
    }

    private void loadUserStats() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long teachingSkillsCount = 0;
                    if (snapshot.hasChild("teachingSkills")) {
                        teachingSkillsCount = snapshot.child("teachingSkills").getChildrenCount();
                    }
                    
                    tvMySkillsCount.setText(String.valueOf(teachingSkillsCount));
                    
                    // You can also dynamically update matches and sessions here if fields exist
                    // For now, keeping your placeholder logic for matches and sessions
                    // tvMatchesCount.setText("5");
                    // tvSessionsCount.setText("2");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void openAddSkillActivity() {
        if (getActivity() == null) return;
        // Intent intent = new Intent(getActivity(), AddSkillActivity.class);
        // startActivity(intent);
    }
}
