package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.MainActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

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
        setContentView(R.layout.activity_login);
        context = LoginActivity.this;
        loginprefsClassLog = new MySharedprefsClass(context);
        loginemail=findViewById(R.id.et_email);
        signupRedirectText=findViewById(R.id.btn_signup);
        loginpassword=findViewById(R.id.et_password);
        ivEye = findViewById(R.id.iv_eye);
        loginButton = findViewById(R.id.btn_login);

        ivEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide Password
                    loginpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivEye.setImageResource(R.drawable.ic_eye);
                } else {
                    // Show Password
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

                    // Iterate through users with this email (usually one)
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String passwordFromDB = userSnapshot.child("password").getValue(String.class);
                        String usernameFromDB = userSnapshot.child("username").getValue(String.class);

                        if(Objects.equals(passwordFromDB, Password)){
                            userFound = true;
                            loginpassword.setError(null);
                            
                            loginprefsClassLog.saveStringValue("username", usernameFromDB);

                            if (Email.equals("sharjeel@admin.com"))
                            {
                                loginprefsClassLog.saveStringValue("isLogin","admin_in");
                                Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                                startActivity(intent);
                            }
                            else
                            {
                                loginprefsClassLog.saveStringValue("isLogin","logged_in");
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            finish(); // Finish LoginActivity so user can't go back to it
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
                Toast.makeText(LoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
