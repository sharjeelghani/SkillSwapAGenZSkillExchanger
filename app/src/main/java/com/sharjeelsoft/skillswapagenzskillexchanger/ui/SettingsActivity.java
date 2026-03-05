package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

public class SettingsActivity extends AppCompatActivity {
    MySharedprefsClass SettingprefsClassLog;
    FrameLayout logoutbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        logoutbtn = findViewById(R.id.logout_container);
        SettingprefsClassLog = new MySharedprefsClass(SettingsActivity.this);
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingprefsClassLog.saveStringValue("isLogin","signed_up");
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}