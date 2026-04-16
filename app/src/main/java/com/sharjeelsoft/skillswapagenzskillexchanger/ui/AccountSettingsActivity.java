package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

import java.util.UUID;

public class AccountSettingsActivity extends AppCompatActivity {

    private TextView tvName, tvLocation, tvJob, btnChangePassword, btnSaveInfo, tvUpdatePhoto;
    private EditText etUsername, etFullName, etEmail, etContact, etDob;
    private ImageView profilePic, btnBack;
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference userRef;
    private String currentUsername;
    private HelperClass currentUserData;
    private boolean isDataChanged = false;
    
    private Uri imageUri;
    
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    uploadImageToFirebase();
                }
            }
    );

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    uploadImageToFirebase();
                }
            }
    );

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        initViews();
        loadUserData();

        btnBack.setOnClickListener(v -> finish());
        
        tvUpdatePhoto.setOnClickListener(v -> showImagePickerDialog());

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(AccountSettingsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnSaveInfo.setOnClickListener(v -> saveUserInfo());
    }

    private void initViews() {
        tvName = findViewById(R.id.name);
        tvJob = findViewById(R.id.tv_job);
        tvLocation = findViewById(R.id.location);
        etUsername = findViewById(R.id.et_user_name);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etContact = findViewById(R.id.et_contact);
        etDob = findViewById(R.id.et_dob);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnSaveInfo = findViewById(R.id.btn_save_info);
        tvUpdatePhoto = findViewById(R.id.tv_update_photo);
        profilePic = findViewById(R.id.profile_pic);
        btnBack = findViewById(R.id.btn_back);

        sharedPrefs = new MySharedprefsClass(this);
        currentUsername = sharedPrefs.getStringValue("username");
        userRef = FirebaseDatabase.getInstance().getReference("user").child(currentUsername);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkForChanges();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etUsername.addTextChangedListener(watcher);
        etContact.addTextChangedListener(watcher);
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserData = snapshot.getValue(HelperClass.class);
                    if (currentUserData != null) {
                        tvName.setText(currentUserData.getFullName());
                        tvJob.setText(currentUserData.getCurrentJob());
                        tvLocation.setText(currentUserData.getCountry());

                        etUsername.setText(currentUserData.getUsername());
                        etFullName.setText(currentUserData.getFullName());
                        etEmail.setText(currentUserData.getEmail());
                        etContact.setText(currentUserData.getContact());
                        etDob.setText(currentUserData.getDateofbirth());

                        if (currentUserData.getProfileImageUrl() != null && !currentUserData.getProfileImageUrl().isEmpty()) {
                            Glide.with(AccountSettingsActivity.this)
                                    .load(currentUserData.getProfileImageUrl())
                                    .placeholder(R.drawable.man)
                                    .into(profilePic);
                        } else {
                            profilePic.setImageResource(R.drawable.man);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountSettingsActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Image");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermission();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Profile Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        takePictureLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadImageToFirebase() {
        if (imageUri == null) return;

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
        
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://skill-swap-a-genz-skill.firebasestorage.app");
        StorageReference storageRef = storage.getReference()
                .child("profile_images/" + currentUsername + "_" + UUID.randomUUID().toString());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        userRef.child("profileImageUrl").setValue(downloadUrl).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AccountSettingsActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("UploadError", "Failed to upload", e);
                    Toast.makeText(AccountSettingsActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkForChanges() {
        if (currentUserData == null) return;

        String newUsername = etUsername.getText().toString().trim();
        String newContact = etContact.getText().toString().trim();

        isDataChanged = !newUsername.equals(currentUserData.getUsername()) ||
                        !newContact.equals(currentUserData.getContact());

        if (isDataChanged) {
            btnSaveInfo.setAlpha(1.0f);
            btnSaveInfo.setClickable(true);
        } else {
            btnSaveInfo.setAlpha(0.5f);
            btnSaveInfo.setClickable(false);
        }
    }

    private void saveUserInfo() {
        String newUsername = etUsername.getText().toString().trim();
        String newContact = etContact.getText().toString().trim();

        if (newUsername.isEmpty()) {
            etUsername.setError("Username cannot be empty");
            return;
        }

        if (newUsername.equals(currentUsername)) {
            updateContactOnly(newContact);
        } else {
            checkUsernameAndMigrate(newUsername, newContact);
        }
    }

    private void updateContactOnly(String newContact) {
        userRef.child("contact").setValue(newContact).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUserData.setContact(newContact);
                Toast.makeText(this, "Info updated", Toast.LENGTH_SHORT).show();
                checkForChanges();
            }
        });
    }

    private void checkUsernameAndMigrate(String newUsername, String newContact) {
        DatabaseReference allUsersRef = FirebaseDatabase.getInstance().getReference("user");
        allUsersRef.child(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etUsername.setError("Username already taken");
                } else {
                    migrateUser(newUsername, newContact);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void migrateUser(String newUsername, String newContact) {
        DatabaseReference newUserRef = FirebaseDatabase.getInstance().getReference("user").child(newUsername);
        
        currentUserData.setUsername(newUsername);
        currentUserData.setContact(newContact);

        newUserRef.setValue(currentUserData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userRef.removeValue(); // Delete old node
                sharedPrefs.saveStringValue("username", newUsername);
                currentUsername = newUsername;
                userRef = newUserRef;
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                checkForChanges();
            }
        });
    }
}
