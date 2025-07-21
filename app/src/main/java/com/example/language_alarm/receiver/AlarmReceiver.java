package com.example.language_alarm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.example.language_alarm.activities.AlarmRingingActivity;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.utils.AlarmForegroundService;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_TRIGGER = "com.example.language_alarm.ACTION_ALARM_TRIGGER";
    public static final String ACTION_STOP_ALARM = "com.example.language_alarm.ACTION_STOP_ALARM";
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(TAG, "onReceive triggered: action=" + intent.getAction());
        String action = intent.getAction();
        Alarm alarm = intent.getParcelableExtra("alarm");

        if (alarm == null) {
            Log.w(TAG, "Null alarm received");
            return;
        }

        PowerManager powerManager = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ON_AFTER_RELEASE,
                "LanguageAlarm:AlarmWakeLock"
        );
        wakeLock.acquire(60 * 1000L);

        if (ACTION_ALARM_TRIGGER.equals(action)) {
            Intent serviceIntent = new Intent(ctx, AlarmForegroundService.class);
            serviceIntent.setAction(action);
            serviceIntent.putExtra("alarm", alarm);
            ctx.startForegroundService(serviceIntent);

            Intent alarmIntent = new Intent(ctx, AlarmRingingActivity.class);
            alarmIntent.putExtra("alarm", alarm);
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            );
            ctx.startActivity(alarmIntent);

        } else if (ACTION_STOP_ALARM.equals(action)) {
            Intent serviceIntent = new Intent(ctx, AlarmForegroundService.class);
            serviceIntent.setAction(action);
            ctx.stopService(serviceIntent);
        }

    }

}
