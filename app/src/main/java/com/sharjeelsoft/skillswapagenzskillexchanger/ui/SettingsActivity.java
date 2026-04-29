package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

public class SettingsActivity extends AppCompatActivity {
    MySharedprefsClass SettingprefsClassLog;
    FrameLayout logoutbtn;
    LinearLayout rowAccount, rowHelp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SettingprefsClassLog = new MySharedprefsClass(SettingsActivity.this);
        
        logoutbtn = findViewById(R.id.logout_container);
        rowAccount = findViewById(R.id.row_account);
        rowHelp = findViewById(R.id.row_help);

        rowAccount.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AccountSettingsActivity.class);
            startActivity(intent);
        });

        rowHelp.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, Help_Support_Activity.class);
            startActivity(intent);
        });

        logoutbtn.setOnClickListener(v -> {
            SettingprefsClassLog.saveStringValue("isLogin", "signed_up");
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAffinity(); // Clear task stack on logout
        });
    }
}
