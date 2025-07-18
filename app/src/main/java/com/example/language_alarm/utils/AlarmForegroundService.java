package com.example.language_alarm.utils;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import com.example.language_alarm.receiver.AlarmReceiver;

import java.io.IOException;

public class AlarmForegroundService extends Service {
    private static final String TAG = "AlarmForegroundService";
    private static final String CHANNEL_ID = "alarm_foreground_channel";
    private static final int NOTIFICATION_ID = 123;
    private Ringtone ringtone = null;
    private Vibrator vibrator = null;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private boolean isMediaPlayerPrepared = false;

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

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
        Log.d(TAG, "Stopping alarm...");
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
        if (mediaPlayer != null) {
            if (isMediaPlayerPrepared) {
                mediaPlayer.stop();
            } else {
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.stop();
                    mp.release();
                });
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isMediaPlayerPrepared = false;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }


    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Alarm Service Channel",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Keeps the alarm running in the foreground");
        channel.setSound(null, null); // We handle sound separately
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
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
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.outline_alarm_24, "Stop", pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .setSound(null)
                .setFullScreenIntent(pendingIntent, true)
                .build();
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] vibrationPattern = new long[]{0, 1000, 1000};
            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0));

            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(2000);
                        if (vibrator != null) {
                            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0));
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();

        }
    }

    private void playRingtone(Alarm alarm) {
        stopAlarm();

        Uri alarmUri = getValidAlarmUri(alarm.getRingtone());

        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            Log.d(TAG, "Attempting to play URI: " + alarmUri);
            Log.d(TAG, "AudioManager alarm volume: " +
                    audioManager.getStreamVolume(AudioManager.STREAM_ALARM));

            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, alarmUri);
                mediaPlayer.setAudioAttributes(audioAttributes);
                mediaPlayer.setLooping(true);
                // TODO: make volume variable
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    isMediaPlayerPrepared = true;
                    mp.start();
                });
                return;
            } catch (IOException e) {
                Log.e(TAG, "MediaPlayer failed, falling back to Ringtone", e);
            }
            ringtone = RingtoneManager.getRingtone(this, alarmUri);
            if (ringtone != null) {
                ringtone.setAudioAttributes(audioAttributes);
                ringtone.play();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing ringtone", e);
        }
    }

    private Uri getValidAlarmUri(String ringtoneUriString) {
        if (ringtoneUriString != null && !ringtoneUriString.isEmpty()) {
            try {
                return Uri.parse(ringtoneUriString);
            } catch (Exception e) {
                Log.e(TAG, "Invalid ringtone URI format", e);
            }
        }
        return getDefaultAlarmUri();
    }

    private Uri getDefaultAlarmUri() {
        int[] types = {
            RingtoneManager.TYPE_ALARM,
            RingtoneManager.TYPE_NOTIFICATION,
            RingtoneManager.TYPE_RINGTONE
        };

        for (int type : types) {
            Uri uri = RingtoneManager.getDefaultUri(type);
            if (uri != null) return uri;
        }
        return null;
    }

}
