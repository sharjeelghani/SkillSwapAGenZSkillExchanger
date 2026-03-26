package com.sharjeelsoft.skillswapagenzskillexchanger.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedprefsClass {

    private static final String PREF_NAME = "MyPrefs";
    private static final String KEY_STRING = "string_key";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public MySharedprefsClass(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setBoolean(String name, boolean value) {
        editor.putBoolean(name, value);
        editor.apply();
    }

    public boolean getBoolean(String name) {
        return sharedPreferences.getBoolean(name, false); // Default value is false
    }

    public void saveStringValue(String name ,String value) {
        editor.putString(name, value);
        editor.apply();

    }

    public String getStringValue(String name) {
        return sharedPreferences.getString(name, "Sharjeel Ghani");
    }
}
