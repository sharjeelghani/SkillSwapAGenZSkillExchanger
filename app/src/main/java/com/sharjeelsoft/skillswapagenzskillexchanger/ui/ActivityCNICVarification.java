package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ActivityCNICVarification extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int PICK_CNIC_FRONT = 101;
    private static final int PICK_CNIC_BACK = 102;
    private static final int CAPTURE_SELFIE = 103;

    private LinearLayout cardCnicFront, cardCnicBack, cardSelfie;
    private ImageView imgProfile;
    private TextView btnCapture, btnVerify, tvName;
    private AlertDialog loadingDialog;

    private Bitmap cnicFrontBitmap, cnicBackBitmap, selfieBitmap;
    private String username;
    
    private FaceDetector faceDetector;
    private TextRecognizer textRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cnicvarification);

        username = getIntent().getStringExtra("username");
        initViews();
        setupMLKit();
        setupListeners();
    }

    private void initViews() {
        cardCnicFront = findViewById(R.id.card_cnic_front);
        cardCnicBack = findViewById(R.id.card_cnic_back);
        cardSelfie = findViewById(R.id.card_selfie);
        imgProfile = findViewById(R.id.img_profile);
        btnCapture = findViewById(R.id.btn_capture);
        btnVerify = findViewById(R.id.btn_verify);
        tvName = findViewById(R.id.tv_name);

        String name = getIntent().getStringExtra("fullName");
        if (name != null) tvName.setText(name);
    }

    private void setupMLKit() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    private void setupListeners() {
        cardCnicFront.setOnClickListener(v -> openGallery(PICK_CNIC_FRONT));
        cardCnicBack.setOnClickListener(v -> openGallery(PICK_CNIC_BACK));
        btnCapture.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        btnVerify.setOnClickListener(v -> {
            if (cnicFrontBitmap == null || cnicBackBitmap == null || selfieBitmap == null) {
                Toast.makeText(this, "Please provide all required images", Toast.LENGTH_SHORT).show();
                return;
            }
            startVerificationProcess();
        });
    }

    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_SELFIE);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            try {
                if (requestCode == PICK_CNIC_FRONT) {
                    Uri uri = data.getData();
                    cnicFrontBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Toast.makeText(this, "Front CNIC uploaded", Toast.LENGTH_SHORT).show();
                } else if (requestCode == PICK_CNIC_BACK) {
                    Uri uri = data.getData();
                    cnicBackBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Toast.makeText(this, "Back CNIC uploaded", Toast.LENGTH_SHORT).show();
                } else if (requestCode == CAPTURE_SELFIE) {
                    selfieBitmap = (Bitmap) data.getExtras().get("data");
                    imgProfile.setImageBitmap(selfieBitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startVerificationProcess() {
        showLoadingDialog("Verifying CNIC...");
        
        InputImage frontImage = InputImage.fromBitmap(cnicFrontBitmap, 0);
        textRecognizer.process(frontImage)
                .addOnSuccessListener(text -> {
                    if (isCnicValid(text)) {
                        verifyFaces();
                    } else {
                        hideLoadingDialog();
                        updateVerificationStatus(false);
                        showErrorDialog("Invalid CNIC. Please upload a clear image of your Pakistani Identity Card.");
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "OCR Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isCnicValid(Text text) {
        String result = text.getText().toLowerCase();
        return result.contains("pakistan") || result.contains("identity") || result.contains("government") || result.contains("name");
    }

    private void verifyFaces() {
        updateLoadingMessage("Matching faces...");
        
        InputImage selfieImg = InputImage.fromBitmap(selfieBitmap, 0);
        faceDetector.process(selfieImg)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        processFinalVerification(true);
                    } else {
                        hideLoadingDialog();
                        updateVerificationStatus(false);
                        showErrorDialog("No face detected in selfie. Please try again.");
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Face Detection Error", Toast.LENGTH_SHORT).show();
                });
    }

    private void processFinalVerification(boolean matched) {
        updateVerificationStatus(matched);
        hideLoadingDialog();
        
        if (matched) {
            showSuccessDialog();
        } else {
            showErrorDialog("Verification failed. Data does not match.");
        }
    }

    private void updateVerificationStatus(boolean status) {
        if (username == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(username);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("isCNICVerified", status);
        if (status) {
            updates.put("signupStage", "DATA_PENDING");
        }
        
        ref.updateChildren(updates);
    }

    private void showLoadingDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        TextView tvMsg = view.findViewById(R.id.tv_loading_msg);
        tvMsg.setText(msg);
        builder.setView(view);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void updateLoadingMessage(String msg) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            TextView tvMsg = loadingDialog.findViewById(R.id.tv_loading_msg);
            if (tvMsg != null) tvMsg.setText(msg);
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Verified!")
                .setMessage("Your identity has been successfully verified.")
                .setPositiveButton("Proceed", (d, w) -> {
                    Intent intent = new Intent(this, DataCllectionActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showErrorDialog(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Verification Failed")
                .setMessage(msg)
                .setPositiveButton("Retry", null)
                .show();
    }
}
