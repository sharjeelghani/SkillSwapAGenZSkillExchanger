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
import java.util.List;

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
                cgSkillsToTest.addView(chip);
            }
        }

        btnStartTest.setOnClickListener(v -> {
            List<Integer> checkedChipIds = cgSkillsToTest.getCheckedChipIds();
            if (checkedChipIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one skill to test", Toast.LENGTH_SHORT).show();
            } else {
                ArrayList<String> selectedSkills = new ArrayList<>();
                for (Integer id : checkedChipIds) {
                    Chip chip = findViewById(id);
                    selectedSkills.add(chip.getText().toString());
                }
                
                Intent intent = new Intent(SkillSelectionActivity.this, SkillTestActivity.class);
                intent.putStringArrayListExtra("selectedSkills", selectedSkills);
                startActivity(intent);
                finish();
            }
        });
    }
}
