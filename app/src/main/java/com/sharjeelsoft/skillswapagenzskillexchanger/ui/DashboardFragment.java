package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;

public class DashboardFragment extends Fragment {

    private TextView tvMySkillsCount;
    private TextView tvMatchesCount;
    private TextView tvSessionsCount;
    private FrameLayout btnAddSkill;

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

        // Set initial stats (later you can update from ViewModel)
        setStats(3, 5, 2);

        btnAddSkill.setOnClickListener(v -> openAddSkillActivity());

        return view;
    }

    private void setStats(int mySkills, int matches, int sessions) {
        tvMySkillsCount.setText(String.valueOf(mySkills));
        tvMatchesCount.setText(String.valueOf(matches));
        tvSessionsCount.setText(String.valueOf(sessions));
    }

    private void openAddSkillActivity() {
        if (getActivity() == null) return;
//        Intent intent = new Intent(getActivity(), AddSkillActivity.class);
//        startActivity(intent);
    }
}