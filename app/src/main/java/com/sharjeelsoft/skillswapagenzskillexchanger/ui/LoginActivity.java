package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sharjeelsoft.skillswapagenzskillexchanger.MainActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

import java.util.ArrayList;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    EditText  loginemail,loginpassword;
    TextView signupRedirectText,loginButton;
    ImageView ivEye;
    boolean isPasswordVisible = false;

    MySharedprefsClass loginprefsClassLog;
    Context context;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = LoginActivity.this;
        loginprefsClassLog = new MySharedprefsClass(context);

        String isLogin = loginprefsClassLog.getStringValue("isLogin");
        if (isLogin.equals("logged_in") || isLogin.equals("admin_in")) {
            startActivity(new Intent(LoginActivity.this, LauncherScreen.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        loginemail=findViewById(R.id.et_email);
        signupRedirectText=findViewById(R.id.btn_signup);
        loginpassword=findViewById(R.id.et_password);
        ivEye = findViewById(R.id.iv_eye);
        loginButton = findViewById(R.id.btn_login);

        ivEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    loginpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivEye.setImageResource(R.drawable.ic_eye);
                } else {
                    loginpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivEye.setImageResource(R.drawable.ic_eye_off);
                }
                isPasswordVisible = !isPasswordVisible;
                loginpassword.setSelection(loginpassword.getText().length());
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateEmail()){
                    Toast.makeText(context, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else if (!validatePassword()) {
                    Toast.makeText(context, "Invalid Password", Toast.LENGTH_SHORT).show();
                } else{
                    checkUser();
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean validateEmail(){
        String val =loginemail.getText().toString();
        if(val.isEmpty()){
            loginemail.setError("Email cannot be empty");
            return false;
        }else{
            loginemail.setError(null);
            return true;
        }
    }

    public boolean validatePassword() {
        String val = loginpassword.getText().toString();
        if (val.isEmpty()) {
            loginpassword.setError("Password cannot be empty");
            return false;
        } else {
            loginpassword.setError(null);
            return true;
        }
    }

    public void checkUser(){
        String Email= loginemail.getText().toString().trim();
        String Password= loginpassword.getText().toString().trim();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("user");
        Query checkUserDatabase=reference.orderByChild("email").equalTo(Email);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    loginemail.setError(null);
                    boolean userFound = false;

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String passwordFromDB = String.valueOf(userSnapshot.child("password").getValue());
                        String usernameFromDB = String.valueOf(userSnapshot.child("username").getValue());
                        String stage = String.valueOf(userSnapshot.child("signupStage").getValue());
                        String fullName = String.valueOf(userSnapshot.child("fullName").getValue());

                        if(Objects.equals(passwordFromDB, Password)){
                            userFound = true;
                            loginpassword.setError(null);
                            
                            loginprefsClassLog.saveStringValue("username", usernameFromDB);
                            
                            // Save FCM Token immediately upon successful login for all versions
                            saveFcmToken(usernameFromDB);

                            if (Email.equalsIgnoreCase("sharjeel@admin.com")) {
                                loginprefsClassLog.saveStringValue("isLogin", "admin_in");
                                startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                                finish();
                            } else {
                                loginprefsClassLog.saveStringValue("isLogin", "logged_in");
                                handleNavigation(stage, usernameFromDB, fullName, userSnapshot);
                            }
                            break;
                        }
                    }

                    if (!userFound) {
                        loginpassword.setError("Invalid Password");
                        loginpassword.requestFocus();
                        Toast.makeText(LoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    loginemail.setError("Invalid Email");
                    loginemail.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFcmToken(String username) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                FirebaseDatabase.getInstance().getReference("user")
                        .child(username).child("fcmToken").setValue(token);
                Log.d("LoginActivity", "FCM Token saved successfully for: " + username);
            }
        });
    }

    private void handleNavigation(String stage, String username, String fullName, DataSnapshot snapshot) {
        if (stage == null || stage.equals("null")) stage = "SIGN_UP";
        Intent intent;

        switch (stage) {
            case "CNIC_PENDING":
                intent = new Intent(LoginActivity.this, ActivityCNICVarification.class);
                intent.putExtra("username", username);
                intent.putExtra("fullName", fullName);
                break;
            case "DATA_PENDING":
                intent = new Intent(LoginActivity.this, DataCllectionActivity.class);
                intent.putExtra("username", username);
                break;
            case "SKILLS_PENDING":
                ArrayList<String> teaching = new ArrayList<>();
                for (DataSnapshot child : snapshot.child("teachingSkills").getChildren()) {
                    teaching.add(String.valueOf(child.getValue()));
                }
                intent = new Intent(LoginActivity.this, SkillSelectionActivity.class);
                intent.putExtra("username", username);
                intent.putStringArrayListExtra("teachingSkills", teaching);
                break;
            case "ACCOUNT_PENDING":
                intent = new Intent(LoginActivity.this, ProfileUpdateActivity.class);
                intent.putExtra("username", username);
                break;
            case "COMPLETED":
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
            default:
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}
