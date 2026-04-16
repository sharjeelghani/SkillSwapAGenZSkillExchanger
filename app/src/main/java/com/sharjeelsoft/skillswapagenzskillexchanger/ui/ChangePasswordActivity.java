package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrent, etNew, etMatch;
    private ImageView ivEyeCurrent, ivEyeNew, ivEyeMatch;
    private TextView btnUpdate;
    private boolean isCurrentVisible = false, isNewVisible = false, isMatchVisible = false;
    
    private DatabaseReference userRef;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initViews();
        setupEyes();

        btnUpdate.setOnClickListener(v -> attemptUpdate());
    }

    private void initViews() {
        etCurrent = findViewById(R.id.et_current_password);
        etNew = findViewById(R.id.et_new_password);
        etMatch = findViewById(R.id.et_match_password);
        ivEyeCurrent = findViewById(R.id.iv_eye_current);
        ivEyeNew = findViewById(R.id.iv_eye_new);
        ivEyeMatch = findViewById(R.id.iv_eye_match);
        btnUpdate = findViewById(R.id.btn_update_password);

        MySharedprefsClass sharedPrefs = new MySharedprefsClass(this);
        username = sharedPrefs.getStringValue("username");
        userRef = FirebaseDatabase.getInstance().getReference("user").child(username);
    }

    private void setupEyes() {
        ivEyeCurrent.setOnClickListener(v -> {
            isCurrentVisible = togglePassword(etCurrent, ivEyeCurrent, isCurrentVisible);
        });
        ivEyeNew.setOnClickListener(v -> {
            isNewVisible = togglePassword(etNew, ivEyeNew, isNewVisible);
        });
        ivEyeMatch.setOnClickListener(v -> {
            isMatchVisible = togglePassword(etMatch, ivEyeMatch, isMatchVisible);
        });
    }

    private boolean togglePassword(EditText et, ImageView iv, boolean isVisible) {
        if (isVisible) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            iv.setImageResource(R.drawable.ic_eye);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            iv.setImageResource(R.drawable.ic_eye_off);
        }
        et.setSelection(et.getText().length());
        return !isVisible;
    }

    private void attemptUpdate() {
        String current = etCurrent.getText().toString().trim();
        String newPass = etNew.getText().toString().trim();
        String matchPass = etMatch.getText().toString().trim();

        if (current.isEmpty()) { etCurrent.setError("Field required"); return; }
        if (newPass.isEmpty()) { etNew.setError("Field required"); return; }
        if (matchPass.isEmpty()) { etMatch.setError("Field required"); return; }

        if (!validateRules(newPass)) return;

        if (!newPass.equals(matchPass)) {
            etMatch.setError("Passwords do not match");
            return;
        }

        verifyAndChange(current, newPass);
    }

    private boolean validateRules(String pass) {
        if (pass.length() < 8) {
            etNew.setError("At least 8 characters");
            return false;
        }
        if (!Pattern.compile("[A-Z]").matcher(pass).find()) {
            etNew.setError("One capital letter required");
            return false;
        }
        if (!Pattern.compile("^[a-zA-Z0-9_]*$").matcher(pass).matches()) {
            etNew.setError("No symbols except _");
            return false;
        }
        return true;
    }

    private void verifyAndChange(String current, String newPass) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Updating password...");
        progress.setCancelable(false);
        progress.show();

        userRef.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dbPass = snapshot.getValue(String.class);
                if (current.equals(dbPass)) {
                    // Start 10 sec delay simulation for "Updating password" as requested
                    new Handler().postDelayed(() -> {
                        userRef.child("password").setValue(newPass).addOnCompleteListener(task -> {
                            progress.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(ChangePasswordActivity.this, "Password Updated", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Password Updation Failed", Toast.LENGTH_LONG).show();
                            }
                        });
                    }, 10000);
                } else {
                    progress.dismiss();
                    etCurrent.setError("Incorrect current password");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progress.dismiss();
            }
        });
    }
}
