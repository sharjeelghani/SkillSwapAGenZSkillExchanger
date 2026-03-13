package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;

import java.util.ArrayList;

public class SkillSelectionActivity extends AppCompatActivity {

    private ChipGroup cgSkillsToTest;
    private TextView btnStartTest;
    private ArrayList<String> teachingSkills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_selection);

        cgSkillsToTest = findViewById(R.id.cgSkillsToTest);
        btnStartTest = findViewById(R.id.btnStartTest);

        teachingSkills = getIntent().getStringArrayListExtra("teachingSkills");

        if (teachingSkills != null) {
            for (String skill : teachingSkills) {
                Chip chip = new Chip(this);
                chip.setText(skill);
                chip.setCheckable(true);
                // Use the same style as in DataCollection if possible, or just standard chips
                // chip.setChipDrawable(ChipDrawable.createFromAttributes(this, null, 0, R.style.SelectableChip));
                cgSkillsToTest.addView(chip);
            }
        }

        btnStartTest.setOnClickListener(v -> {
            int checkedId = cgSkillsToTest.getCheckedChipId();
            if (checkedId == View.NO_ID) {
                Toast.makeText(this, "Please select at least one skill to test", Toast.LENGTH_SHORT).show();
            } else {
                Chip selectedChip = findViewById(checkedId);
                String selectedSkill = selectedChip.getText().toString();
                
                Intent intent = new Intent(SkillSelectionActivity.this, SkillTestActivity.class);
                intent.putExtra("selectedSkill", selectedSkill);
                startActivity(intent);
                finish();
            }
        });
    }
}
