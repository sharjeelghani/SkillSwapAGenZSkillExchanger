package com.sharjeelsoft.skillswapagenzskillexchanger.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;

import com.sharjeelsoft.skillswapagenzskillexchanger.MainActivity;
import com.sharjeelsoft.skillswapagenzskillexchanger.R;
import com.sharjeelsoft.skillswapagenzskillexchanger.auth.MySharedprefsClass;

public class LauncherScreen extends AppCompatActivity {
    MySharedprefsClass loginprefsClass;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_screen);

        context = getApplicationContext();
        loginprefsClass = new MySharedprefsClass(context);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loginprefsClass.getStringValue("isLogin").equals("new_user")) {
                    Intent intent = new Intent(LauncherScreen.this, SignUpActivity.class);
                    startActivity(intent);
                    finish();
                } else if (loginprefsClass.getStringValue("isLogin").equals("signed_up")) {
                    Intent intent = new Intent(LauncherScreen.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else if (loginprefsClass.getStringValue("isLogin").equals("logged_in"))
                {
                    Intent intent = new Intent(LauncherScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else if (loginprefsClass.getStringValue("isLogin").equals("admin_in"))
                {
                    Intent intent = new Intent(LauncherScreen.this, AdminMainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 2000);
    }
}