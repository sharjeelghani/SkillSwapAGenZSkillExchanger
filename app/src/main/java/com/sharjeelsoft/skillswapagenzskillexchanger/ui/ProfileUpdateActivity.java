package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
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
import com.sharjeelsoft.skillswapagenzskillexchanger.MainActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileUpdateActivity extends AppCompatActivity {

    private ImageView profilePic, btnBack;
    private TextView tvUpdatePhoto;
    private Button btnFinish;
    private MySharedprefsClass sharedPrefs;
    private DatabaseReference userRef;
    private String currentUsername;
    private HelperClass currentUserData;
    
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
        setContentView(R.layout.activity_profile_update);

        initViews();
        loadUserData();

        btnBack.setOnClickListener(v -> finish());
        tvUpdatePhoto.setOnClickListener(v -> showImagePickerDialog());
        
        btnFinish.setOnClickListener(v -> completeSignupProgress());
    }

    private void initViews() {
        profilePic = findViewById(R.id.profile_pic);
        btnBack = findViewById(R.id.btn_back);
        tvUpdatePhoto = findViewById(R.id.tv_update_photo);
        btnFinish = findViewById(R.id.btnFinish);

        sharedPrefs = new MySharedprefsClass(this);
        currentUsername = sharedPrefs.getStringValue("username");
        userRef = FirebaseDatabase.getInstance().getReference("user").child(currentUsername);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserData = snapshot.getValue(HelperClass.class);
                    if (currentUserData != null && currentUserData.getProfileImageUrl() != null && !currentUserData.getProfileImageUrl().isEmpty()) {
                        Glide.with(ProfileUpdateActivity.this)
                                .load(currentUserData.getProfileImageUrl())
                                .placeholder(R.drawable.man)
                                .into(profilePic);
                        
                        btnFinish.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileUpdateActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
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
        
        if (currentUserData != null && currentUserData.getProfileImageUrl() != null && !currentUserData.getProfileImageUrl().isEmpty()) {
            try {
                StorageReference oldPhotoRef = storage.getReferenceFromUrl(currentUserData.getProfileImageUrl());
                oldPhotoRef.delete().addOnCompleteListener(task -> proceedWithUpload(storage, uri));
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
                                btnFinish.setVisibility(View.VISIBLE);
                                new android.os.Handler().postDelayed(() -> {
                                    if (progressDialog.isShowing()) progressDialog.dismiss();
                                }, 1500);
                            } else {
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                            }
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(ProfileUpdateActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void completeSignupProgress() {
        progressDialog.setMessage("Completing Signup...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();
        updates.put("isAccountSet", true);
        updates.put("signupStage", "COMPLETED");

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Intent intent = new Intent(ProfileUpdateActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to complete setup", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
