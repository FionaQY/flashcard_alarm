package com.example.language_alarm.utils;

import android.content.SharedPreferences;

public class SettingUtils {
    private static final String PREFS_NAME = "preferences????";

    private static SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

    public static SharedPreferences getSharedPreferences() {
        if (settings == null) {
            settings = getSharedPreferences(PREFS_NAME, 0);
        }
        return settings;
    }
}