package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCllectionActivity extends AppCompatActivity {
    private static final String TAG = "UserOnboarding";

    private RadioGroup rgGender;
    private ChipGroup cgTeaching, cgLearning;
    private EditText etQualifications, etCurrentJob, etExperience;
    private Button btnAttach;
    private TextView tvAttachedCount, btnContinue;

    private final List<Uri> attachedUris = new ArrayList<>();
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference userRef;
    private String username;
    private boolean isFirstTime = true;

    // Modern Activity Result API
    private final ActivityResultLauncher<Intent> pickFilesLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    attachedUris.clear();
                    Intent data = result.getData();
                    ClipData clip = data.getClipData();
                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            attachedUris.add(uri);
                        }
                    } else {
                        Uri uri = data.getData();
                        if (uri != null) attachedUris.add(uri);
                    }
                    updateAttachedCount();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_cllection);

        sharedPrefs = new MySharedprefsClass(this);
        String prefsUsername = sharedPrefs.getStringValue("username");
        String intentUsername = getIntent().getStringExtra("username");

        username = (intentUsername != null && !intentUsername.isEmpty()) ? intentUsername : prefsUsername;

        if (username == null || username.equals("new_user")) {
            Toast.makeText(this, "User session not found. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("user").child(username);
        initializeViews();
        fetchUserData();
    }

    private void initializeViews() {
        rgGender = findViewById(R.id.rgGender);
        cgTeaching = findViewById(R.id.cgTeaching);
        cgLearning = findViewById(R.id.cgLearning);
        etQualifications = findViewById(R.id.etQualifications);
        etCurrentJob = findViewById(R.id.etCurrentJob);
        etExperience = findViewById(R.id.etExperience);
        btnAttach = findViewById(R.id.btnAttach);
        tvAttachedCount = findViewById(R.id.tvAttachedCount);
        btnContinue = findViewById(R.id.btnContinue);

        btnAttach.setOnClickListener(v -> openFilePicker());
        btnContinue.setOnClickListener(v -> onSubmit());
    }

    private void fetchUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    populateData(snapshot);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void populateData(DataSnapshot snapshot) {
        if (snapshot.hasChild("gender")) {
            isFirstTime = false;
            String gender = snapshot.child("gender").getValue(String.class);
            if ("Male".equals(gender)) rgGender.check(R.id.rbMale);
            else if ("Female".equals(gender)) rgGender.check(R.id.rbFemale);
            else if ("Other".equals(gender)) rgGender.check(R.id.rbOther);
            
            btnContinue.setText("Update Profile");
        }

        if (snapshot.hasChild("teachingSkills")) {
            List<String> teaching = new ArrayList<>();
            for (DataSnapshot child : snapshot.child("teachingSkills").getChildren()) {
                teaching.add(child.getValue(String.class));
            }
            selectChips(cgTeaching, teaching);
        }

        if (snapshot.hasChild("learningInterests")) {
            List<String> learning = new ArrayList<>();
            for (DataSnapshot child : snapshot.child("learningInterests").getChildren()) {
                learning.add(child.getValue(String.class));
            }
            selectChips(cgLearning, learning);
        }

        if (snapshot.hasChild("education")) etQualifications.setText(snapshot.child("education").getValue(String.class));
        if (snapshot.hasChild("currentJob")) etCurrentJob.setText(snapshot.child("currentJob").getValue(String.class));
        if (snapshot.hasChild("experience")) etExperience.setText(String.valueOf(snapshot.child("experience").getValue()));
    }

    private void selectChips(ChipGroup group, List<String> selectedValues) {
        if (selectedValues == null) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) group.getChildAt(i);
                if (selectedValues.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickFilesLauncher.launch(intent);
    }

    private void updateAttachedCount() {
        tvAttachedCount.setText(attachedUris.isEmpty() ? "No files selected" : attachedUris.size() + " file(s) selected");
    }

    private void onSubmit() {
        int checkedId = rgGender.getCheckedRadioButtonId();
        String gender = checkedId == R.id.rbMale ? "Male" : checkedId == R.id.rbFemale ? "Female" : checkedId == R.id.rbOther ? "Other" : "";
        List<String> teaching = getSelectedChips(cgTeaching);
        List<String> learning = getSelectedChips(cgLearning);
        String qualifications = etQualifications.getText().toString().trim();
        String currentJob = etCurrentJob.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();

        if (gender.isEmpty() || teaching.isEmpty() || learning.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("gender", gender);
        updates.put("teachingSkills", teaching);
        updates.put("learningInterests", learning);
        updates.put("education", qualifications);
        updates.put("currentJob", currentJob);
        updates.put("experience", experience);
        
        // Progress tracking
        updates.put("isDataUpdated", true);
        updates.put("signupStage", "SKILLS_PENDING");

        btnContinue.setEnabled(false);
        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            btnContinue.setEnabled(true);
            if (task.isSuccessful()) {
                Intent intent = new Intent(this, SkillSelectionActivity.class);
                intent.putStringArrayListExtra("teachingSkills", new ArrayList<>(teaching));
                intent.putExtra("username", username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getSelectedChips(ChipGroup group) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) group.getChildAt(i);
                if (chip.isChecked()) result.add(chip.getText().toString());
            }
        }
        return result;
    }
}
