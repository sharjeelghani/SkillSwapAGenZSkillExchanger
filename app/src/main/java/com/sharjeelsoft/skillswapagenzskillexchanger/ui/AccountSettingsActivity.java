package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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
    private ProgressDialog progressDialog;
    
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri sourceUri = result.getData().getData();
                    startCrop(sourceUri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    startCrop(imageUri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri croppedUri = UCrop.getOutput(result.getData());
                    if (croppedUri != null) {
                        uploadImageToFirebase(croppedUri);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(result.getData());
                    Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
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

        etDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AccountSettingsActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        etDob.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

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
        etFullName.addTextChangedListener(watcher);
        etContact.addTextChangedListener(watcher);
        etDob.addTextChangedListener(watcher);
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
                        
                        // After loading, ensure button is disabled as initial state
                        checkForChanges();
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

    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = "cropped_image_" + UUID.randomUUID().toString() + ".jpg";
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(false);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(false);
        
        // Use theme colors
        options.setToolbarColor(ContextCompat.getColor(this, R.color.teal_light));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.teal_light));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.teal_light));

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(1000, 1000);
        uCrop.withOptions(options);
        cropImageLauncher.launch(uCrop.getIntent(this));
    }

    private void uploadImageToFirebase(Uri uri) {
        progressDialog.setMessage("Uploading Profile Picture...");
        progressDialog.show();
        
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://skill-swap-a-genz-skill.firebasestorage.app");
        
        // 1. Delete old image if exists
        if (currentUserData != null && currentUserData.getProfileImageUrl() != null && !currentUserData.getProfileImageUrl().isEmpty()) {
            try {
                StorageReference oldPhotoRef = storage.getReferenceFromUrl(currentUserData.getProfileImageUrl());
                oldPhotoRef.delete().addOnCompleteListener(task -> {
                    proceedWithUpload(storage, uri);
                });
            } catch (Exception e) {
                proceedWithUpload(storage, uri);
            }
        } else {
            proceedWithUpload(storage, uri);
        }
    }

    private void proceedWithUpload(FirebaseStorage storage, Uri uri) {
        StorageReference storageRef = storage.getReference()
                .child("profile_images/" + currentUsername + "_" + UUID.randomUUID().toString());

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String downloadUrl = downloadUri.toString();
                        userRef.child("profileImageUrl").setValue(downloadUrl).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                progressDialog.setMessage("Profile Updated");
                                new android.os.Handler().postDelayed(() -> {
                                    if (progressDialog.isShowing()) progressDialog.dismiss();
                                }, 1500);
                            } else {
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                Toast.makeText(this, "Database update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    android.util.Log.e("UploadError", "Failed to upload", e);
                    Toast.makeText(AccountSettingsActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void checkForChanges() {
        if (currentUserData == null) return;

        String newUsername = etUsername.getText().toString().trim();
        String newFullName = etFullName.getText().toString().trim();
        String newContact = etContact.getText().toString().trim();
        String newDob = etDob.getText().toString().trim();

        isDataChanged = !newUsername.equals(currentUserData.getUsername()) ||
                        !newFullName.equals(currentUserData.getFullName()) ||
                        !newContact.equals(currentUserData.getContact()) ||
                        !newDob.equals(currentUserData.getDateofbirth());

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
        String newFullName = etFullName.getText().toString().trim();
        String newContact = etContact.getText().toString().trim();
        String newDob = etDob.getText().toString().trim();

        if (newUsername.isEmpty()) {
            etUsername.setError("Username cannot be empty");
            return;
        }

        if (newUsername.equals(currentUsername)) {
            updateInfoOnly(newFullName, newContact, newDob);
        } else {
            checkUsernameAndMigrate(newUsername, newFullName, newContact, newDob);
        }
    }

    private void updateInfoOnly(String newFullName, String newContact, String newDob) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newFullName);
        updates.put("contact", newContact);
        updates.put("dateofbirth", newDob);
        
        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUserData.setFullName(newFullName);
                currentUserData.setContact(newContact);
                currentUserData.setDateofbirth(newDob);
                Toast.makeText(this, "Info updated", Toast.LENGTH_SHORT).show();
                checkForChanges();
            }
        });
    }



    private void checkUsernameAndMigrate(String newUsername, String newFullName, String newContact, String newDob) {
        DatabaseReference allUsersRef = FirebaseDatabase.getInstance().getReference("user");
        allUsersRef.child(newUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etUsername.setError("Username already taken");
                } else {
                    migrateUser(newUsername, newFullName, newContact, newDob);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void migrateUser(String newUsername, String newFullName, String newContact, String newDob) {
        DatabaseReference newUserRef = FirebaseDatabase.getInstance().getReference("user").child(newUsername);
        
        currentUserData.setUsername(newUsername);
        currentUserData.setFullName(newFullName);
        currentUserData.setContact(newContact);
        currentUserData.setDateofbirth(newDob);

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
