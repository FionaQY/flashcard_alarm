package com.example.language_alarm.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingUtils {
    private static final String PREFS_NAME = "preferences????";
    private static final String SNOOZE_COUNT = "snoozeCount";
    private static final String THEME = "isNightMode";

    private static SharedPreferences prefs = null;

    public SettingUtils(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // for MainActivity (Theme)
    public boolean getIsDarkTheme() {
        return prefs.getBoolean(THEME, false);
    }

    public void flipIsDarkTheme() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(THEME, !getIsDarkTheme());
        editor.apply();
    }

    // for AlarmRingingActivity (SnoozeCount)
    public int getSnoozeCount() {
        return prefs.getInt(SNOOZE_COUNT, 0);
    }

    private void setSnoozeCount(int i) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SNOOZE_COUNT, i);
        editor.apply();
    }

    public void incrementSnoozeCount() {
        setSnoozeCount(getSnoozeCount() + 1);
    }

    public void resetSnoozeCount() {
        setSnoozeCount(0);
    }
}