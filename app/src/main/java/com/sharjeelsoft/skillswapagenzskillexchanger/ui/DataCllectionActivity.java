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

        // Determine which username to check
        String candidateUsername = null;
        if (intentUsername != null && !intentUsername.isEmpty()) {
            candidateUsername = intentUsername;
        } else if (!prefsUsername.equals("new_user")) {
            candidateUsername = prefsUsername;
        }

        if (candidateUsername == null) {
            Toast.makeText(this, "User session not found. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final String finalUsername = candidateUsername;

        // --- Debug Condition ---
        if ("Abubakar Ch".equals(finalUsername)) {
            username = finalUsername;
            fetchAndInitialize(finalUsername);
            return;
        }
        // -----------------------

        // Check existence in both Realtime Database and SharedPrefs
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user").child(finalUsername);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean existsInDB = snapshot.exists();
                boolean existsInPrefs = !sharedPrefs.getStringValue("username").equals("new_user");

                // If it exists in at least one of them, then activity should not be finished
                if (existsInDB || existsInPrefs) {
                    username = finalUsername;
                    initializeActivity(snapshot);
                } else {
                    Toast.makeText(DataCllectionActivity.this, "User session not found. Please login again.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // On error, fallback to SharedPrefs check as a safety measure
                if (!sharedPrefs.getStringValue("username").equals("new_user")) {
                    username = finalUsername;
                    fetchAndInitialize(finalUsername);
                } else {
                    Toast.makeText(DataCllectionActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void fetchAndInitialize(String name) {
        FirebaseDatabase.getInstance().getReference("user").child(name)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        initializeActivity(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        initializeActivity(null);
                    }
                });
    }

    private void initializeActivity(DataSnapshot snapshot) {
        userRef = FirebaseDatabase.getInstance().getReference("user").child(username);

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

        if (snapshot != null && snapshot.exists()) {
            populateData(snapshot);
        }
    }

    private void populateData(DataSnapshot snapshot) {
        // Populate Gender
        if (snapshot.hasChild("gender")) {
            isFirstTime = false;
            String gender = snapshot.child("gender").getValue(String.class);
            if ("Male".equals(gender)) rgGender.check(R.id.rbMale);
            else if ("Female".equals(gender)) rgGender.check(R.id.rbFemale);
            else if ("Other".equals(gender)) rgGender.check(R.id.rbOther);

            // Change UI text if data exists
            TextView tvTitle = findViewById(R.id.tvTitle);
            if (tvTitle != null) tvTitle.setText("Edit Your Profile");
            btnContinue.setText("Update Profile");
        }

        // Populate Teaching Skills
        if (snapshot.hasChild("teachingSkills")) {
            List<String> teaching = new ArrayList<>();
            for (DataSnapshot child : snapshot.child("teachingSkills").getChildren()) {
                teaching.add(child.getValue(String.class));
            }
            selectChips(cgTeaching, teaching);
        }

        // Populate Learning Interests
        if (snapshot.hasChild("learningInterests")) {
            List<String> learning = new ArrayList<>();
            for (DataSnapshot child : snapshot.child("learningInterests").getChildren()) {
                learning.add(child.getValue(String.class));
            }
            selectChips(cgLearning, learning);
        }

        // Populate Background History
        if (snapshot.hasChild("education")) {
            etQualifications.setText(snapshot.child("education").getValue(String.class));
        }
        if (snapshot.hasChild("currentJob")) {
            etCurrentJob.setText(snapshot.child("currentJob").getValue(String.class));
        }
        if (snapshot.hasChild("experience")) {
            Object exp = snapshot.child("experience").getValue();
            if (exp != null) etExperience.setText(String.valueOf(exp));
        }
    }

    private void selectChips(ChipGroup group, List<String> selectedValues) {
        if (selectedValues == null) return;
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
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
        intent.setType("*/*"); // allow any; filter in server if needed
        // allow multiple selection
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickFilesLauncher.launch(intent);
    }

    private void updateAttachedCount() {
        if (attachedUris.isEmpty()) {
            tvAttachedCount.setText("No files selected");
        } else {
            tvAttachedCount.setText(attachedUris.size() + " file(s) selected");
        }
    }

    private void onSubmit() {
        // Gender
        int checkedId = rgGender.getCheckedRadioButtonId();
        String gender = "";
        if (checkedId == R.id.rbMale) gender = "Male";
        else if (checkedId == R.id.rbFemale) gender = "Female";
        else if (checkedId == R.id.rbOther) gender = "Other";

        // Teaching skills
        List<String> teaching = getSelectedChips(cgTeaching);

        // Learning skills
        List<String> learning = getSelectedChips(cgLearning);

        // Background
        String qualifications = etQualifications.getText().toString().trim();
        String currentJob = etCurrentJob.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();

        // Simple validation
        if (gender.isEmpty()) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }
        if (teaching.isEmpty()) {
            Toast.makeText(this, "Please select at least one teaching skill", Toast.LENGTH_SHORT).show();
            return;
        }
        if (learning.isEmpty()) {
            Toast.makeText(this, "Please select at least one learning skill", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("gender", gender);
        updates.put("teachingSkills", teaching);
        updates.put("learningInterests", learning);
        updates.put("education", qualifications);
        updates.put("currentJob", currentJob);
        updates.put("experience", experience);

        btnContinue.setEnabled(false);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            btnContinue.setEnabled(true);
            if (task.isSuccessful()) {
                if (isFirstTime) {
                    Toast.makeText(DataCllectionActivity.this, "Profile built successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DataCllectionActivity.this, SkillSelectionActivity.class);
                    intent.putStringArrayListExtra("teachingSkills", new ArrayList<>(teaching));
                    startActivity(intent);
                } else {
                    Toast.makeText(DataCllectionActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DataCllectionActivity.this, SkillSelectionActivity.class);
                    intent.putStringArrayListExtra("teachingSkills", new ArrayList<>(teaching));
                    startActivity(intent);
                }
                finish();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(DataCllectionActivity.this, "Failed to save: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Database Error: ", task.getException());
            }
        }).addOnFailureListener(e -> {
            btnContinue.setEnabled(true);
            Toast.makeText(DataCllectionActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private List<String> getSelectedChips(ChipGroup group) {
        List<String> result = new ArrayList<>();
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            if (group.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) group.getChildAt(i);
                if (chip.isChecked()) result.add(chip.getText().toString());
            }
        }
        return result;
    }
}
