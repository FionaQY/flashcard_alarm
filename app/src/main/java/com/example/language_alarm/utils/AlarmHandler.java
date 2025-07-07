package com.example.language_alarm.utils;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.language_alarm.database.AlarmDao;
import com.example.language_alarm.database.AlarmDatabase;
import com.example.language_alarm.models.Alarm;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;


public class AlarmHandler {
    private static final String TAG = "AlarmHandler";
    private static final String RINGTONE_STR = "ringtone";

    private static AlarmDao getAlarmDao(Context ctx) {
        return AlarmDatabase.getDatabase(ctx).alarmDao();
    }

    public static void saveAlarm(Context ctx, Alarm alarm) {
        Context appContext = ctx.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (alarm == null) {
                    Log.w(TAG, "Attempted to save null alarm");
                    return;
                }
                if (alarm.getId() == 0) {
                    // new alarm
                    Log.d(TAG, String.format("Saving new Alarm: %s", alarm.getDescription()));
                    long id = getAlarmDao(appContext).insert(alarm);
                    alarm.setId((int) id);
                    scheduleAlarm(appContext, alarm);
                    Log.d(TAG, String.format("New alarm scheduled with ID: %d", id));
                } else {
                    getAlarmDao(appContext).update(alarm);
                    rescheduleAlarm(appContext, alarm);
                    Log.i(TAG, "Alarm updated and rescheduled: " + alarm.getId());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving alarm", e);
            }

        });
    }

    public static void scheduleAlarm(Context ctx, Alarm alarm) {
        if (alarm == null || !alarm.isEnabled()) return;

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        if (alarm.isOneTime()) {
            scheduleOneTimeAlarm(ctx, alarmManager, alarm);
        } else {
            scheduleRecurringAlarm(ctx, alarmManager, alarm);
        }
    }

    private static void scheduleOneTimeAlarm(Context ctx, AlarmManager alarmManager, Alarm alarm) {
        Intent intent = createAlarmIntent(ctx, alarm);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1); //set next day if time passed alr
        }
        setExactAlarm(ctx, alarmManager, calendar.getTimeInMillis(), pendingIntent);
    }

    private static void scheduleRecurringAlarm(Context ctx, AlarmManager alarmManager, Alarm alarm) {
        if (alarm.isSunday()) scheduleDayAlarm(ctx, alarmManager, alarm, Calendar.SUNDAY);
        if (alarm.isMonday()) scheduleDayAlarm(ctx, alarmManager, alarm, Calendar.MONDAY);
        if (alarm.isTuesday()) scheduleDayAlarm(ctx, alarmManager, alarm, Calendar.TUESDAY);
        if (alarm.isWednesday()) scheduleDayAlarm(ctx, alarmManager, alarm, Calendar.WEDNESDAY);
        if (alarm.isThursday()) scheduleDayAlarm(ctx, alarmManager, alarm, Calendar.THURSDAY);
        if (alarm.isFriday()) scheduleDayAlarm(ctx, alarmManager, alarm, Calendar.FRIDAY);
        if (alarm.isSaturday()) scheduleDayAlarm(ctx, alarmManager, alarm, Calendar.SATURDAY);
    }

    private static int getRequestCode(Alarm alarm, int dayOfWeek) {
        return 10 * alarm.getId() + dayOfWeek;
    }

    private static void scheduleDayAlarm(Context ctx, AlarmManager alarmManager, Alarm alarm, int dayOfWeek) {
        Intent intent = createAlarmIntent(ctx, alarm);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctx,
                getRequestCode(alarm, dayOfWeek),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }
        setExactAlarm(ctx, alarmManager, calendar.getTimeInMillis(), pendingIntent);
    }

    private static Intent createAlarmIntent(Context ctx, Alarm alarm) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_TRIGGER);
        intent.putExtra(RINGTONE_STR, alarm.getRingtone());
        intent.putExtra("alarm", alarm);
        return intent;
    }

    private static void setExactAlarm(Context ctx, AlarmManager alarmManager, long triggerAttMills, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent permissionIntent = new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                // Add flag if context is not an Activity
                if (!(ctx instanceof Activity)) {
                    permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                ctx.startActivity(permissionIntent);
                return;
            }
        }

        // For API 21+ use setAlarmClock which shows in status bar and gives priority
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                triggerAttMills,
                pendingIntent
        );
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
    }

    public static void cancelAlarm(Context ctx, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (alarm.isOneTime()) {
            cancelSingleAlarm(ctx, alarm.getId(), alarmManager, alarm);
        } else {
            if (alarm.isSunday())
                cancelSingleAlarm(ctx, getRequestCode(alarm, Calendar.SUNDAY), alarmManager, alarm);
            if (alarm.isMonday())
                cancelSingleAlarm(ctx, getRequestCode(alarm, Calendar.MONDAY), alarmManager, alarm);
            if (alarm.isTuesday())
                cancelSingleAlarm(ctx, getRequestCode(alarm, Calendar.TUESDAY), alarmManager, alarm);
            if (alarm.isWednesday())
                cancelSingleAlarm(ctx, getRequestCode(alarm, Calendar.WEDNESDAY), alarmManager, alarm);
            if (alarm.isThursday())
                cancelSingleAlarm(ctx, getRequestCode(alarm, Calendar.THURSDAY), alarmManager, alarm);
            if (alarm.isFriday())
                cancelSingleAlarm(ctx, getRequestCode(alarm, Calendar.FRIDAY), alarmManager, alarm);
            if (alarm.isSaturday())
                cancelSingleAlarm(ctx, getRequestCode(alarm, Calendar.SATURDAY), alarmManager, alarm);
        }
    }

    private static void cancelSingleAlarm(Context ctx, int requestCode, AlarmManager alarmManager, Alarm alarm) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.i(TAG, "Cancelled alarm: " + alarm.getId());
    }

    public static void rescheduleAlarm(Context ctx, Alarm alarm) {
        cancelAlarm(ctx, alarm);
        scheduleAlarm(ctx, alarm);
    }

    public static void rescheduleAllAlarms(Context ctx) {
        Context appContext = ctx.getApplicationContext();
        List<Alarm> alarmList = getAlarmDao(appContext).getAllAlarms().getValue();
        if (alarmList == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (Alarm alarm : alarmList) {
                    rescheduleAlarm(appContext, alarm);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error rescheduling all alarms", e);
            }

        });

    }

    public static void deleteAlarm(Context ctx, Alarm alarm) {
        Context appContext = ctx.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (alarm.getId() == 0) {
                    Log.w(TAG, "Attempted to delete null alarm");
                    return;
                }
                cancelAlarm(appContext, alarm);
                getAlarmDao(appContext).delete(alarm);
                Log.i(TAG, String.format("Successfully deleted alarm ID:%d", alarm.getId()));
            } catch (Exception e) {
                Log.e(TAG, "Error deleting alarm", e);
            }
        });
    }

}
