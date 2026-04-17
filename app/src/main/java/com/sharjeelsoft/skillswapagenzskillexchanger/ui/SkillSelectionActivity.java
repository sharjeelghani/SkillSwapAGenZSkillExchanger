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
    private ArrayList<String> passedSkills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_selection);

        cgSkillsToTest = findViewById(R.id.cgSkillsToTest);
        btnStartTest = findViewById(R.id.btnStartTest);

        teachingSkills = getIntent().getStringArrayListExtra("teachingSkills");
        passedSkills = getIntent().getStringArrayListExtra("passedSkills");
        if (passedSkills == null) passedSkills = new ArrayList<>();

        if (teachingSkills != null) {
            for (String skill : teachingSkills) {
                Chip chip = new Chip(this);
                chip.setText(skill);
                chip.setCheckable(true);
                
                if (passedSkills.contains(skill)) {
                    chip.setChecked(true);
                    chip.setEnabled(false); // Disable once passed
                    chip.setText(skill + " (Verified)");
                }
                
                cgSkillsToTest.addView(chip);
            }
        }

        btnStartTest.setOnClickListener(v -> {
            List<Integer> checkedChipIds = cgSkillsToTest.getCheckedChipIds();
            ArrayList<String> selectedSkills = new ArrayList<>();
            
            for (Integer id : checkedChipIds) {
                Chip chip = findViewById(id);
                String skillName = chip.getText().toString().replace(" (Verified)", "");
                if (!passedSkills.contains(skillName)) {
                    selectedSkills.add(skillName);
                }
            }

            if (selectedSkills.isEmpty()) {
                if (teachingSkills != null && passedSkills.size() == teachingSkills.size()) {
                    // All skills already passed
                    startActivity(new Intent(this, AccountSettingsActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Please select at least one unverified skill to test", Toast.LENGTH_SHORT).show();
                }
            } else {
                Intent intent = new Intent(SkillSelectionActivity.this, SkillTestActivity.class);
                intent.putStringArrayListExtra("selectedSkills", selectedSkills);
                intent.putStringArrayListExtra("passedSkills", passedSkills);
                intent.putStringArrayListExtra("teachingSkills", teachingSkills);
                startActivity(intent);
                finish();
            }
        });
    }
}
