package com.example.language_alarm.utils;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.language_alarm.AlarmReceiver;
import com.example.language_alarm.activities.NewAlarmActivity;
import com.example.language_alarm.database.AlarmDao;
import com.example.language_alarm.database.AlarmDatabase;
import com.example.language_alarm.models.Alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;


public class AlarmScheduler {
    private static final String RINGTONE_STR = "ringtone";

    private static AlarmDao getAlarmDao(Context ctx) {
        return AlarmDatabase.getDatabase(ctx).alarmDao();
    }

    public static void saveAlarm(Context ctx, Alarm alarm) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (alarm.getId() == 0) {
                // new alarm
                long id = getAlarmDao(ctx).insert(alarm);
                alarm.setId((int) id);
                scheduleAlarm(ctx, alarm);
            } else {
                getAlarmDao(ctx).update(alarm);
                rescheduleAlarm(ctx, alarm);
            }
        });
    }

    public static void scheduleAlarm(Context ctx, Alarm alarm) {
        if (!alarm.isEnabled()) return;

        int requestCode = alarm.getId();

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
        intent.putExtra("alarm_id", requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create showIntent for the alarm clock Info (what happens when user taps notif);
        Intent showIntent = new Intent(ctx, NewAlarmActivity.class);
        PendingIntent showPendingIntent = PendingIntent.getActivity(
                ctx,
                requestCode,
                showIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager newAlarmManager = ctx.getSystemService(AlarmManager.class);
            if (!newAlarmManager.canScheduleExactAlarms()) {
                // Launch intent to request permission
                Intent permissionIntent = new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                ctx.startActivity(permissionIntent);
                return;
            }
        }

        // For API 21+ use setAlarmClock which shows in status bar and gives priority
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                calendar.getTimeInMillis(),
                showPendingIntent
        );
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
    }

    public static void cancelAlarm(Context ctx, Alarm alarm) {
        int requestCode = alarm.getId();
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.putExtra(RINGTONE_STR, alarm.getRingtone());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private static void rescheduleAlarm(Context ctx, Alarm alarm) {
        cancelAlarm(ctx, alarm);
        scheduleAlarm(ctx, alarm);
    }

    public static void deleteAlarm(Context ctx, Alarm alarm) {
        cancelAlarm(ctx, alarm);
        Executors.newSingleThreadExecutor().execute(() -> {
            // Runnable
            if (alarm.getId() == 0) {
                // no id
                return;
            } else {
                getAlarmDao(ctx).delete(alarm);
            }
        });
    }

}
