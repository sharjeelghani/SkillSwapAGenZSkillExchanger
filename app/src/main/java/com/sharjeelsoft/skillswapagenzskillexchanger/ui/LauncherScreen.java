package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sharjeelsoft.skillswapagenzskillexchanger.MainActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

import java.util.ArrayList;

public class LauncherScreen extends AppCompatActivity {
    MySharedprefsClass loginprefsClass;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_screen);

        context = getApplicationContext();
        loginprefsClass = new MySharedprefsClass(context);

        new Handler().postDelayed(this::checkUserProgress, 2000);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void checkUserProgress() {
        String isLogin = loginprefsClass.getStringValue("isLogin");
        String username = loginprefsClass.getStringValue("username");

        if (isLogin.equals("new_user")) {
            navigateTo(SignUpActivity.class);
        } else if (isLogin.equals("signed_up")) {
            navigateTo(LoginActivity.class);
        } else if (isLogin.equals("logged_in") || isLogin.equals("admin_in")) {
            if (isLogin.equals("admin_in")) {
                navigateTo(AdminMainActivity.class);
                return;
            }
            
            // For regular users, check their signup stage in Firebase
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(username);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String stage = snapshot.child("signupStage").getValue(String.class);
                        
                        // If stage is missing, fallback to status-based navigation
                        if (stage == null)  {
                            handleStatusBasedNavigation(isLogin);
                            return;
                        }

                        switch (stage) {
                            case "CNIC_PENDING":
                                Intent cnicIntent = createBaseIntent(ActivityCNICVarification.class);
                                cnicIntent.putExtra("username", username);
                                cnicIntent.putExtra("fullName", snapshot.child("fullName").getValue(String.class));
                                startActivity(cnicIntent);
                                finish();
                                break;
                            case "DATA_PENDING":
                                Intent dataIntent = createBaseIntent(DataCllectionActivity.class);
                                dataIntent.putExtra("username", username);
                                startActivity(dataIntent);
                                finish();
                                break;
                            case "SKILLS_PENDING":
                                // Fetch teaching skills to pass to selection activity
                                ArrayList<String> teaching = new ArrayList<>();
                                for (DataSnapshot child : snapshot.child("teachingSkills").getChildren()) {
                                    teaching.add(String.valueOf(child.getValue()));
                                }
                                Intent skillsIntent = createBaseIntent(SkillSelectionActivity.class);
                                skillsIntent.putExtra("username", username);
                                skillsIntent.putStringArrayListExtra("teachingSkills", teaching);
                                startActivity(skillsIntent);
                                finish();
                                break;
                            case "ACCOUNT_PENDING":
                                navigateTo(ProfileUpdateActivity.class);
                                break;
                            case "COMPLETED":
                                navigateTo(MainActivity.class);
                                break;
                            default:
                                handleStatusBasedNavigation(isLogin);
                                break;
                        }
                    } else {
                        handleStatusBasedNavigation(isLogin);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleStatusBasedNavigation(isLogin);
                }
            });
        } else {
            navigateTo(SignUpActivity.class);
        }
    }

    private void handleStatusBasedNavigation(String isLogin) {
        if (isLogin.equals("new_user")) {
            navigateTo(SignUpActivity.class);
        } else if (isLogin.equals("signed_up")) {
            navigateTo(LoginActivity.class);
        } else if (isLogin.equals("logged_in")) {
            navigateTo(MainActivity.class);
        } else if (isLogin.equals("admin_in")) {
            navigateTo(AdminMainActivity.class);
        } else {
            navigateTo(SignUpActivity.class);
        }
    }

    private Intent createBaseIntent(Class<?> target) {
        Intent intent = new Intent(LauncherScreen.this, target);
        if (getIntent().hasExtra("navigate_to")) {
            intent.putExtra("navigate_to", getIntent().getStringExtra("navigate_to"));
        }
        return intent;
    }

    private void navigateTo(Class<?> target) {
        startActivity(createBaseIntent(target));
        finish();
    }
}
