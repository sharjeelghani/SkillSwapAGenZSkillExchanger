package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;

public class SkillTestActivity extends AppCompatActivity {
    private Handler handler;
    ProgressBar progressBar;
    TextView progresstrack;
    int progress = 0;
    int interval = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_test);

        progressBar = findViewById(R.id.progressBar);
        progresstrack = findViewById(R.id.textView);
      handler = new Handler(Looper.getMainLooper());

        updateProgress();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                Intent intent = new Intent(SkillTestActivity.this, LoginActivity.class);
//                startActivity(intent);
//                finish();
                Toast.makeText(SkillTestActivity.this, "Next Question", Toast.LENGTH_SHORT).show();

            }
},30000);
    }

    private void updateProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progress += 1;
                progressBar.setProgress(progress*10);
                progresstrack.setText(progress + "sec");

                if (progress < 30) {

                    updateProgress();
                }
            }
        }, interval);
    }
}