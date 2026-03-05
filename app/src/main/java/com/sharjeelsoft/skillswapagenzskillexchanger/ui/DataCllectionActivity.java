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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;

import java.util.ArrayList;
import java.util.List;

public class DataCllectionActivity extends AppCompatActivity {
    private static final String TAG = "UserOnboarding";

    private RadioGroup rgGender;
    private ChipGroup cgTeaching, cgLearning;
    private EditText etQualifications, etCurrentJob, etExperience;
    private Button btnAttach;
    private TextView tvAttachedCount, btnContinue;

    private final List<Uri> attachedUris = new ArrayList<>();

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

        // For demo show collected data via log and a toast summary:
        Log.d(TAG, "Gender: " + gender);
        Log.d(TAG, "Teaching: " + teaching.toString());
        Log.d(TAG, "Learning: " + learning.toString());
        Log.d(TAG, "Qualifications: " + qualifications);
        Log.d(TAG, "CurrentJob: " + currentJob);
        Log.d(TAG, "Experience: " + experience);
        Log.d(TAG, "Attached files: " + attachedUris.size());

        String preview = "Saved: " + gender + " · Teach: " + teaching.size() + " · Learn: " + learning.size();
        Toast.makeText(this, preview, Toast.LENGTH_LONG).show();

        // TODO: send data to server / store locally
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