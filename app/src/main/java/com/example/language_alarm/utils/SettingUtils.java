package com.example.language_alarm.utils;

import android.content.SharedPreferences;

public class SettingUtils {
    private static final String PREFS_NAME = "preferences????";
    private static final String SNOOZE_COUNT = "snoozeCount";
    private static final String THEME = "isNightMode";

    private static SharedPreferences settings = null;

    private static SharedPreferences getSavedSettings() {
        if (settings == null) {
            settings = getSharedPreferences(PREFS_NAME, 0);
        }
        return settings;
    }

    // for MainActivity (Theme)
    public static boolean getIsDarkTheme() {
        return getSavedSettings().getBoolean(THEME, false)
    }

    public static boolean setIsDarkTheme(boolean isNight) {
        SharedPreferences.Editor editor = getSavedSettings().edit();
        editor.putBoolean(THEME, isNight);
        editor.apply();
    }

    // for AlarmRingingActivity (SnoozeCount)
    public static int getSnoozeCount() {
        return getSavedSettings().getInt(SNOOZE_COUNT, 0);
    }

    private static void setSnoozeCount(int i) {
        SharedPreferences.Editor editor = getSavedSettings().edit();
        editor.putInt(SNOOZE_COUNT, i);
        editor.apply();
    }
    
    public static void incrementSnoozeCount() {
        setSnoozeCount(getSnoozeCount() + 1);
    }

    public static void resetSnoozeCount() {
        setSnoozeCount(0);
    }
}