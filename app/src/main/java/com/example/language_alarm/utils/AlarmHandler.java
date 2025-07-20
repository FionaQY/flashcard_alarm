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
import com.example.language_alarm.receiver.AlarmReceiver;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AlarmHandler {
    private static final String TAG = "AlarmHandler";
    private static final String RINGTONE_STR = "ringtone";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static AlarmDao getAlarmDao(Context ctx) {
        return AlarmDatabase.getDatabase(ctx).alarmDao();
    }

    public static void saveAlarm(Context ctx, Alarm alarm) {
        executor.execute(() -> saveAlarmInternal(ctx, alarm));
    }

    private static void saveAlarmInternal(Context ctx, Alarm alarm) {
        Context appContext = ctx.getApplicationContext();
        try {
            if (alarm == null) {
                Log.w(TAG, "Attempted to save null alarm");
                return;
            }
            if (alarm.getId() == 0) {
                // new alarm
                Log.d(TAG, String.format("Saving new %s", alarm.getLogDesc()));
                long id = getAlarmDao(appContext).insert(alarm);
                alarm.setId((int) id);
            } else {
                getAlarmDao(appContext).update(alarm);
                Log.d(TAG, String.format("Updated %s", alarm.getLogDesc()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving alarm", e);
        }
    }

    private static void scheduleAlarmInternal(Context ctx, Alarm alarm) {
        if (!alarm.isEnabled()) return;
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = createAlarmPendingIntent(ctx, alarm);

        Calendar calendar = alarm.getNextAlarmTime();
        if (calendar == null) {
            Log.w(TAG, String.format(Locale.US, "Unable to schedule %s", alarm.getLogDesc()));
            return;
        }

        setExactAlarm(ctx, alarmManager, calendar.getTimeInMillis(), pendingIntent);
        Log.d(TAG, String.format(Locale.US, "Successfully set %s on %s", alarm.getLogDesc(), calendar.getTime()));
    }

    public static void snoozeAlarm(Context ctx, Alarm alarm) {
        cancelAlarm(ctx, alarm);

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createAlarmPendingIntent(ctx, alarm);

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, alarm.getSnoozeDuration());
        calendar.set(Calendar.SECOND, 0);

        setExactAlarm(ctx, alarmManager, calendar.getTimeInMillis(), pendingIntent);
    }

    private static PendingIntent createAlarmPendingIntent(Context ctx, Alarm alarm) {
        Intent intent = new Intent(ctx, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_TRIGGER);
        intent.putExtra(RINGTONE_STR, alarm.getRingtone() != null ? alarm.getRingtone() : "");
        intent.putExtra("alarm", alarm);
        return PendingIntent.getBroadcast(
                ctx,
                alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
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
        Log.i(TAG, "Scheduled alarm at: " + triggerAttMills);
    }

    public static void cancelAlarm(Context ctx, Alarm alarm) {
        if (alarm == null) return;
        executor.execute(() -> cancelAlarmInternal(ctx, alarm));
    }

    private static void cancelAlarmInternal(Context ctx, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createAlarmPendingIntent(ctx, alarm);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.i(TAG, "Successfully cancelled " + alarm.getLogDesc());
    }

    public static void rescheduleAlarm(Context ctx, Alarm alarm) {
        executor.execute(() -> rescheduleAlarmInternal(ctx, alarm));
    }

    public static void rescheduleAlarmInternal(Context ctx, Alarm alarm) {
        if (alarm == null) return;
        cancelAlarmInternal(ctx, alarm);
        if (alarm.isEnabled()) {
            scheduleAlarmInternal(ctx, alarm);
        }
        Log.i(TAG, String.format(Locale.US, "%s has been successfully rescheduled", alarm.getLogDesc()));
    }

    public static void rescheduleAllAlarms(Context ctx) {
        Context appContext = ctx.getApplicationContext();
        List<Alarm> alarmList = getAlarmDao(appContext).getAllAlarms().getValue();
        if (alarmList == null) return;
        executor.execute(() -> {
            try {
                for (Alarm alarm : alarmList) {
                    rescheduleAlarmInternal(appContext, alarm);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error rescheduling all alarms", e);
            }

        });
    }

    public static void removeLesson(Context ctx, int lessonId) {
        Context appContext = ctx.getApplicationContext();
        List<Alarm> alarmList = getAlarmDao(appContext).getAllAlarms().getValue();
        if (alarmList == null || lessonId == 0) return;
        executor.execute(() -> {
            try {
                for (Alarm alarm : alarmList) {
                    if (alarm.getLessonId() == lessonId) {
                        alarm.deleteLesson();
                        saveAlarmInternal(ctx, alarm);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, String.format(Locale.US, "Error removing Lesson ID %d from all alarms", lessonId), e);
            }
        });
    }

    public static void deleteAlarm(Context ctx, Alarm alarm) {
        Context appContext = ctx.getApplicationContext();
        executor.execute(() -> {
            try {
                if (alarm == null || alarm.getId() == 0) {
                    Log.w(TAG, "Attempted to delete null alarm");
                    return;
                }
                cancelAlarmInternal(appContext, alarm);
                getAlarmDao(appContext).delete(alarm);
                Log.i(TAG, String.format("Successfully deleted %s", alarm.getLogDesc()));
            } catch (Exception e) {
                assert alarm != null;
                Log.e(TAG,
                        String.format(Locale.US, "Error deleting %s", alarm.getLogDesc()),
                        e);
            }
        });
    }

}
