package com.example.language_alarm.utils;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Alarm;

public class AlarmForegroundService extends Service {
    // to ensure the ringtone plays even if system tries to disrupt
    private static final String TAG = "AlarmForegroundService";
    private static final String CHANNEL_ID = "alarm_foreground_channel";
    private static final int NOTIFICATION_ID = 123;
    private Ringtone ringtone = null;
    private Vibrator vibrator = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }
        String action = intent.getAction();
        Alarm alarm = intent.getParcelableExtra("alarm");

        if (AlarmReceiver.ACTION_ALARM_TRIGGER.equals(action)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, createNotification(), FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTIFICATION_ID, createNotification());
            }
            assert alarm != null;
            playRingtone(alarm);
            startVibration();
        } else if (AlarmReceiver.ACTION_STOP_ALARM.equals(action)) {
            stopAlarm();
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAlarm();
        Log.d(TAG, "Service destroyed");
    }

    public void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Alarm Service Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Keeps the alarm running in the foreground");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        createNotificationChannel();
        Intent stopIntent = new Intent(this, AlarmForegroundService.class);
        stopIntent.setAction(AlarmReceiver.ACTION_STOP_ALARM);
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_add_alarm_24)
                .setContentTitle("Alarm")
                .setContentText("Time to wake up!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.outline_alarm_24, "Stop", pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .build();
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] vibrationPatterns = new long[]{0, 1000, 1000};
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPatterns, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // Deprecated in API 26
                vibrator.vibrate(2000);
            }
        }
    }

    private void playRingtone(Alarm alarm) {
        stopAlarm();
        Uri alarmUri;

        String ringtoneUriString = alarm.getRingtone();

        if (ringtoneUriString != null) {
            alarmUri = Uri.parse(ringtoneUriString);
        } else {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        ringtone = RingtoneManager.getRingtone(this, alarmUri);
        if (ringtone != null) {
            ringtone.play();
        }
    }


}
