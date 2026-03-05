package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.models.MatchUiModel;

import java.util.ArrayList;
import java.util.List;

public class SkillMatchmakingFragment extends Fragment {

    private List<MatchUiModel> matches = new ArrayList<>();
    private int currentIndex = 0;

    private TextView tvName;
    private ImageView imgAvatar;
    private TextView tagSkillOne;
    private TextView tagSkillTwo;
    private TextView btnConnect;
    private TextView btnViewProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view =  inflater.inflate(R.layout.fragment_skill_matchmaking, container, false);

        // --- bind views ---
        tvName        = view.findViewById(R.id.tv_name);
        imgAvatar     = view.findViewById(R.id.img_avatar);
        tagSkillOne   = view.findViewById(R.id.tag_skill_one);
        tagSkillTwo   = view.findViewById(R.id.tag_skill_two);
        btnConnect    = view.findViewById(R.id.btn_connect);
        btnViewProfile = view.findViewById(R.id.btn_view_profile);

        // temp dummy data – replace with data from ViewModel / API
        initDummyMatches();

        if (!matches.isEmpty()) {
            showMatch(currentIndex);
        }

        // --- button handlers ---
        btnConnect.setOnClickListener(v -> {
            // TODO: handle "connect" with current user (e.g. API call)
            goToNextMatch();
        });

        // if you also have a Reject button, wire it the same way:
        // TextView btnReject = view.findViewById(R.id.btn_reject);
        // btnReject.setOnClickListener(v -> goToNextMatch());

        btnViewProfile.setOnClickListener(v -> {
            MatchUiModel match = matches.get(currentIndex);

            // TODO: navigate to profile screen, pass match id/name, etc.
            // Example if you use Navigation Component:
            // NavController navController =
            //         NavHostFragment.findNavController(SwapMatchFragment.this);
            // Bundle args = new Bundle();
            // args.putString("userName", match.getName());
            // navController.navigate(R.id.action_swapMatch_to_profile, args);
        });

        return view;
    }

    private void initDummyMatches() {
        matches.clear();
        matches.add(new MatchUiModel(
                "Scarlett Zhao",
                "Video Editing",
                "Learning Guitar",
                R.drawable.avatar
        ));
        matches.add(new MatchUiModel(
                "Omar Ali",
                "Python Programming",
                "UI Design",
                R.drawable.man
        ));
        matches.add(new MatchUiModel(
                "Ayesha Khan",
                "Public Speaking",
                "Data Analysis",
                R.drawable.avatar
        ));
    }

    private void showMatch(int index) {
        if (matches.isEmpty()) return;

        MatchUiModel match = matches.get(index);

        tvName.setText(match.getName());
        tagSkillOne.setText(match.getPrimarySkill());
        tagSkillTwo.setText(match.getSecondarySkill());
        imgAvatar.setImageResource(match.getPhotoRes());
    }

    private void goToNextMatch() {
        if (matches.isEmpty()) return;

        currentIndex = (currentIndex + 1) % matches.size();
        showMatch(currentIndex);
    }
}