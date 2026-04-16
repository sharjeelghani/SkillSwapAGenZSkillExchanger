package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;

public class Help_Support_Activity extends AppCompatActivity {

    private LinearLayout faq1, faq2, faq3, faq4, faq5;
    private TextView ans1, ans2, ans3, ans4, ans5;
    private View emailButton, chatButton;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        initViews();
        setupFaqs();
        setupContactButtons();
        
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        faq1 = findViewById(R.id.faq1);
        faq2 = findViewById(R.id.faq2);
        faq3 = findViewById(R.id.faq3);
        faq4 = findViewById(R.id.faq4);
        faq5 = findViewById(R.id.faq5);

        ans1 = findViewById(R.id.a1);
        ans2 = findViewById(R.id.a2);
        ans3 = findViewById(R.id.a3);
        ans4 = findViewById(R.id.a4);
        ans5 = findViewById(R.id.a5);

        emailButton = findViewById(R.id.email_button);
        chatButton = findViewById(R.id.chat_button);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupFaqs() {
        faq1.setOnClickListener(v -> toggleFaq(ans1));
        faq2.setOnClickListener(v -> toggleFaq(ans2));
        faq3.setOnClickListener(v -> toggleFaq(ans3));
        faq4.setOnClickListener(v -> toggleFaq(ans4));
        faq5.setOnClickListener(v -> toggleFaq(ans5));
    }

    private void toggleFaq(TextView selectedAns) {
        boolean isAlreadyVisible = selectedAns.getVisibility() == View.VISIBLE;

        // Close all first
        ans1.setVisibility(View.GONE);
        ans2.setVisibility(View.GONE);
        ans3.setVisibility(View.GONE);
        ans4.setVisibility(View.GONE);
        ans5.setVisibility(View.GONE);

        // If it wasn't visible, show it now
        if (!isAlreadyVisible) {
            selectedAns.setVisibility(View.VISIBLE);
        }
    }

    private void setupContactButtons() {
        emailButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"sharjeels354@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Skill Swap Support");
            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
            }
        });

        chatButton.setOnClickListener(v -> {
            String phoneNumber = "+923087328917";
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
