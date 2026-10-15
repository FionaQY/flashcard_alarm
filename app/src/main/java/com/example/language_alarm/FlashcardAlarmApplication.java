package com.example.language_alarm;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.language_alarm.utils.SettingUtils;

public class FlashcardAlarmApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SettingUtils settings = new SettingUtils(this);
        AppCompatDelegate.setDefaultNightMode(settings.getIsDarkTheme()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
