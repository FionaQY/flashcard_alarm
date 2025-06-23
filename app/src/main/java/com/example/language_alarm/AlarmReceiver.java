package com.example.language_alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
//import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver{
    private static final String CHANNEL_ID = "alarm_channel";
    private static Ringtone ringtone = null;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        createNotificationChannel(ctx);
        vibrate(ctx);

        Toast.makeText(ctx, "Alarm! Waky waky", Toast.LENGTH_LONG).show();
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(ctx, alarmUri);
        ringtone.play();
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

    private void playRingtone(Context ctx, Intent intent) {
        String ringtoneUriString = intent.getStringExtra("ringtone");
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

    private void showNotification(Context ctx) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_add_alarm_24)
                .setContentTitle("Alarm")
                .setContentText("yo wake your ass up")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notifManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(1, builder.build());
    }

    public void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}
