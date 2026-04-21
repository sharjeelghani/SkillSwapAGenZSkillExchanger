package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyUserUploadActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy_user_upload);

        mDatabase = FirebaseDatabase.getInstance().getReference("user");

        Button btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v -> uploadDummyUsers());
    }

    private void uploadDummyUsers() {
        Map<String, Object> users = new HashMap<>();

        users.put("danyal", createUserData("danyal", "Danyal Baloch", "Male", "03001234567", "danyal.baloch@gmail.com", "password123", "15/05/1998", "Software Engineer", "BSCS", "2 Years",
                Arrays.asList("Web Development (HTML/CSS/JS)", "Python Programming"),
                Arrays.asList("Adobe Photoshop", "Video Editing", "Graphic Design", "UI/UX Design")));

        users.put("noor", createUserData("noor", "Noor Fatima", "Female", "03111234567", "noor.fatima@gmail.com", "password123", "22/08/2000", "Content Strategist", "Masters in English", "3 Years",
                Arrays.asList("Content writing", "Public Speaking", "English Speaking"),
                Arrays.asList("Web Development (HTML/CSS/JS)", "WordPress Development", "Figma Designing")));

        users.put("zain", createUserData("zain", "Zain Mushtaq", "Male", "03221234567", "zain.mushtaq@gmail.com", "password123", "10/12/1997", "Video Editor", "BS Media Studies", "4 Years",
                Arrays.asList("Video Editing", "Motion Graphics", "Animation (2D/3D)"),
                Arrays.asList("AI Prompt Engineering", "SEO Basics", "Python Programming")));

        users.put("ali", createUserData("ali", "Muhammad Ali", "Male", "03331234567", "muhammad.ali@gmail.com", "password123", "05/03/1999", "UI/UX Designer", "BSIT", "3 Years",
                Arrays.asList("UI/UX Design", "Figma Designing", "App Development (Android/Kotlin)"),
                Arrays.asList("Photography", "Content writing", "Video Editing")));

        users.put("muskan", createUserData("muskan", "Muskan Fatima", "Female", "03441234567", "muskan.fatima@gmail.com", "password123", "18/11/2001", "Digital Marketer", "BBA", "2 Years",
                Arrays.asList("SEO Basics", "Social Media Marketing", "Digital Marketing"),
                Arrays.asList("Data Analysis", "Python Programming", "Web Development (HTML/CSS/JS)")));

        users.put("sabahat", createUserData("sabahat", "Sabahat Khan", "Female", "03551234567", "sabahat.khan@gmail.com", "password123", "30/01/1996", "Graphic Designer", "BFA", "5 Years",
                Arrays.asList("Graphic Design", "Adobe Photoshop", "Photography"),
                Arrays.asList("Public Speaking", "English Speaking", "Figma Designing")));

        users.put("talha", createUserData("talha", "Talha Chohdary", "Male", "03661234567", "talha.chohdary@gmail.com", "password123", "12/07/2002", "Data Scientist", "BS Data Science", "1 Years",
                Arrays.asList("Data Analysis", "AI Prompt Engineering", "Machine Learning Basics"),
                Arrays.asList("Figma Designing", "Video Editing", "App Development (Android/Kotlin)")));

        mDatabase.updateChildren(users).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DummyUserUploadActivity.this, "Dummy users uploaded successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DummyUserUploadActivity.this, "Failed to upload dummy users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Map<String, Object> createUserData(String username, String fullName, String gender, String contact, String email, String password, String dob, String job, String edu, String exp, List<String> teaching, List<String> learning) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("fullName", fullName);
        user.put("gender", gender);
        user.put("contact", contact);
        user.put("email", email);
        user.put("password", password);
        user.put("dateofbirth", dob);
        user.put("country", "Pakistan");
        user.put("currentJob", job);
        user.put("education", edu);
        user.put("experience", exp);
        user.put("teachingSkills", teaching);
        user.put("passedSkills", teaching);
        user.put("learningInterests", learning);
        user.put("isAccountSet", true);
        user.put("isCNICVerified", true);
        user.put("isSkillsTested", true);
        user.put("isDataUpdated", true);
        user.put("signupStage", "ACCOUNT_PENDING");
        user.put("verified", false);
        user.put("signedUp", true);
        return user;
    }
}
