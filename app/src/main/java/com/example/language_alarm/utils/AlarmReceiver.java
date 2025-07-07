package com.example.language_alarm.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.language_alarm.R;
import com.example.language_alarm.activities.AlarmRingingActivity;
import com.example.language_alarm.models.Alarm;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM_TRIGGER = "com.example.language_alarm.ACTION_ALARM_TRIGGER";
    private static final String CHANNEL_ID = "alarm_channel";
    private static final String TAG = "AlarmReceiver";
    private static Ringtone ringtone = null;

    public static void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.d(TAG, "onReceive triggered: action=" + intent.getAction());
        String action = intent.getAction();
        Alarm alarm = intent.getParcelableExtra("alarm");

        if (alarm == null) {
            Log.w(TAG, "Attempted to ring a null alarm");
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            createNotificationChannel(ctx);
        } else if (ACTION_ALARM_TRIGGER.equals(action)) {
            createNotificationChannel(ctx);
            vibrate(ctx);
            playRingtone(ctx, alarm.getRingtone());
            showNotification(ctx, alarm);

        }
        // TODO: if alarm is not one time, schedule again
        Intent alarmIntent = new Intent(ctx, AlarmRingingActivity.class);
        alarmIntent.putExtra("alarm", alarm);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(alarmIntent);

    }

    private void createNotificationChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void vibrate(Context ctx) {
        Vibrator vibr = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibr != null && vibr.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibr.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // Deprecated in API 26
                vibr.vibrate(2000);
            }
        }
    }

    private void playRingtone(Context ctx, String ringtoneUriString) {
        stopAlarm();
        Uri alarmUri;

        if (ringtoneUriString != null) {
            alarmUri = Uri.parse(ringtoneUriString);
        } else {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        ringtone = RingtoneManager.getRingtone(ctx, alarmUri);
        if (ringtone != null) {
            ringtone.play();
        }
    }

    private void showNotification(Context ctx, Alarm alarm) {
        Intent alarmIntent = new Intent(ctx, AlarmRingingActivity.class);
        alarmIntent.putExtra("alarm", alarm);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx, alarm.getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_add_alarm_24)
                .setContentTitle("Alarm")
                .setContentText("yo wake your ass up")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true);

        NotificationManager notifManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(alarm.getId(), builder.build());
    }
}
