package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
//import android.os.Vibrator;
import android.os.VibrationEffect;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
public class AlarmReceiver extends BroadcastReceiver{
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onReceive(Context context, Intent intent) {
//        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR);
        VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE);
//        vibrator.vibrate(4000);

        Toast.makeText(context, "Alarm! Waky waky", Toast.LENGTH_LONG).show();
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();
    }
}
