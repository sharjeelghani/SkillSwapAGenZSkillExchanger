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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.sharjeelsoft.skillswapagenzskillexchanger.MainActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.HelperClass;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private HelperClass userData;
    
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
        fetchUserData();
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

    private void fetchUserData() {
        if (username == null) return;
        FirebaseDatabase.getInstance().getReference("user").child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userData = snapshot.getValue(HelperClass.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupMLKit() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
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
            if (userData == null) {
                Toast.makeText(this, "Loading user data, please wait...", Toast.LENGTH_SHORT).show();
                fetchUserData();
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
        showLoadingDialog("Verifying document...");
        
        InputImage frontImage = InputImage.fromBitmap(cnicFrontBitmap, 0);
        textRecognizer.process(frontImage)
                .addOnSuccessListener(text -> {
                    String validationError = getCnicValidationError(text);
                    if (validationError == null) {
                        verifyFaces();
                    } else {
                        hideLoadingDialog();
                        updateVerificationStatus(false);
                        showErrorDialog(validationError);
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getCnicValidationError(Text text) {
        String fullText = text.getText().toLowerCase();
        
        // 1. Check for CNIC number pattern
        Pattern cnicPattern = Pattern.compile("\\d{5}[-]?\\d{7}[-]?\\d");
        Matcher matcher = cnicPattern.matcher(text.getText());
        if (!matcher.find()) {
            return "CNIC number not detected. Please upload a clear photo.";
        }

        // 2. Check for keywords
        boolean hasPakistan = fullText.contains("pakistan");
        boolean hasIdentity = fullText.contains("identity") || fullText.contains("card");
        if (!hasPakistan || !hasIdentity) {
            return "Document does not appear to be a valid Pakistani CNIC.";
        }

        // 3. Check for Full Name
        if (userData.getFullName() != null) {
            String[] nameParts = userData.getFullName().toLowerCase().split(" ");
            boolean nameMatch = false;
            for (String part : nameParts) {
                if (part.length() > 2 && fullText.contains(part)) {
                    nameMatch = true;
                    break;
                }
            }
            if (!nameMatch) {
                return "The name on the ID card does not match your profile name (" + userData.getFullName() + ").";
            }
        }

        // 4. Check for DOB
        if (userData.getDateofbirth() != null) {
            // DOB is stored as D/M/YYYY or DD/MM/YYYY. CNIC often uses DD.MM.YYYY
            String dob = userData.getDateofbirth().replace("/", ".");
            String dobAlt = userData.getDateofbirth(); // D/M/YYYY
            
            if (!fullText.contains(dob) && !fullText.contains(dobAlt)) {
                // Try to be a bit more flexible with DOB matching (e.g. check year)
                String[] dobParts = userData.getDateofbirth().split("/");
                String year = dobParts[dobParts.length - 1];
                if (!fullText.contains(year)) {
                    return "The date of birth on the ID card does not match your profile.";
                }
            }
        }

        return null; // No errors
    }

    private void verifyFaces() {
        updateLoadingMessage("Analyzing ID photo...");
        
        InputImage cnicImg = InputImage.fromBitmap(cnicFrontBitmap, 0);
        faceDetector.process(cnicImg)
                .addOnSuccessListener(cnicFaces -> {
                    if (!cnicFaces.isEmpty()) {
                        updateLoadingMessage("Analyzing selfie...");
                        InputImage selfieImg = InputImage.fromBitmap(selfieBitmap, 0);
                        faceDetector.process(selfieImg)
                                .addOnSuccessListener(selfieFaces -> {
                                    if (!selfieFaces.isEmpty()) {
                                        processFinalVerification(true);
                                    } else {
                                        hideLoadingDialog();
                                        showErrorDialog("No face detected in your selfie. Please take a clear photo of your face.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    hideLoadingDialog();
                                    Toast.makeText(this, "Selfie check failed", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        hideLoadingDialog();
                        showErrorDialog("No face detected on your CNIC image. Please upload a clearer photo.");
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoadingDialog();
                    Toast.makeText(this, "ID check failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void processFinalVerification(boolean matched) {
        updateVerificationStatus(matched);
        hideLoadingDialog();
        
        if (matched) {
            showSuccessDialog();
        } else {
            showErrorDialog("Verification failed. Document details do not match.");
        }
    }

    private void updateVerificationStatus(boolean status) {
        if (username == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(username);
        ref.child("verified").setValue(status);
    }

    private void showLoadingDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        TextView tvMsg = view.findViewById(R.id.tv_loading_msg);
        if (tvMsg != null) tvMsg.setText(msg);
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
                .setTitle("Identity Verified!")
                .setMessage("Your account has been successfully verified. Welcome to Skill Swap.")
                .setPositiveButton("Proceed", (d, w) -> {
                    startActivity(new Intent(this, MainActivity.class));
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
