package com.example.myapplication.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import com.example.myapplication.AlarmReceiver;
import com.example.myapplication.models.Alarm;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmScheduler {
    private static final String PREFS_NAME = "alarms";
    private static final String ALARMS_KEY = "alarms_list";
    private static final String RINGTONE_STR = "ringtone";

    public static void saveAlarms(Context ctx, List<Alarm> alarms) {
        SharedPreferences preferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String json = new Gson().toJson(alarms);
        editor.putString(ALARMS_KEY, json);
        editor.apply();
    }

    public static List<Alarm> loadAlarms(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String strJson = preferences.getString(ALARMS_KEY,null);
        if (strJson == null) {
            return new ArrayList<Alarm>();
        }
        return new Gson().fromJson(strJson, new TypeToken<List<Alarm>>(){}.getType());
    }

    public static void scheduleAlarm(Context ctx, Alarm alarm, int requestCode) {
        if (!alarm.isEnabled()) return;

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        // calendar is called to get current time in hour minute
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1); //set for next day
        }

        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra(RINGTONE_STR, alarm.getRingtone());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(ctx, "Exact alarms not allowed. Please enable in system settings.", Toast.LENGTH_LONG).show();
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

    }

    public static void cancelAlarm(Context ctx, Alarm alarm, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra(RINGTONE_STR, alarm.getRingtone());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    public static void rescheduleAll(Context ctx) {
        List<Alarm> alarms = loadAlarms(ctx);
        for (int i = 0; i < alarms.size(); i++) {
            Alarm alarm = alarms.get(i);
            cancelAlarm(ctx, alarm, i);
            scheduleAlarm(ctx, alarm, i);
        }
    }

}
