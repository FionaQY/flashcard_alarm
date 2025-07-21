package com.example.language_alarm.activities;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.receiver.AlarmReceiver;
import com.example.language_alarm.utils.AlarmForegroundService;
import com.example.language_alarm.utils.AlarmHandler;
import com.example.language_alarm.utils.SettingUtils;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class AlarmRingingActivity extends AppCompatActivity {
    private static final String TAG = "AlarmRingingActivity";
    private Alarm alarm;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ON_AFTER_RELEASE,
                "LanguageAlarm:AlarmWakeLock"
        );
        wakeLock.acquire(30 * 1000L /*half a minute*/);
        Log.d(TAG, "WakeLock acquired");

        setTurnScreenOn(true);
        setShowWhenLocked(true);
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(this, null);

        setContentView(R.layout.activity_ringing);

        alarm = getIntent().getParcelableExtra("alarm");
        if (alarm == null) {
            Log.w(TAG, "Attempted to ring null alarm");
            supportFinishAfterTransition();
            return;
        }
        if (alarm.getWallpaper() != null) {
            setWallpaperFromUri(Uri.parse(alarm.getWallpaper()));
        }

        SettingUtils prefs = new SettingUtils(this);
        int snoozeCount = prefs.getSnoozeCount();
        // set buttons view and listeners
        MaterialButton snoozeButton = findViewById(R.id.snoozeButton);
        snoozeButton.setVisibility(alarm.getSnoozeNum() > snoozeCount ? View.VISIBLE : View.GONE);
        snoozeButton.setText(String.format(Locale.US, "%d Snoozes Left", alarm.getSnoozeNum() - snoozeCount));

        snoozeButton.setOnClickListener(v -> {
            stopRinging();
            prefs.incrementSnoozeCount();
            AlarmHandler.snoozeAlarm(this, alarm);
            finish();
        });

        findViewById(R.id.stopButton).setOnClickListener(v -> {
            stopRinging();

            AlarmHandler.cancelAlarm(this, alarm);
            prefs.resetSnoozeCount();

            Intent intent = new Intent(this, MemorisationActivity.class);
            intent.putExtra("lessonId", alarm.getLessonId());
            intent.putExtra("qnCount", alarm.getQnNum());
            intent.putExtra("isAlarm", true);
            startActivity(intent);

            if (!alarm.isOneTime()) {
                AlarmHandler.rescheduleAlarm(this, alarm);
            } else {
                alarm.setEnabled(false);
                AlarmHandler.saveAlarm(this, alarm);
            }
            finish();
        });

    }

    private void setWallpaperFromUri(Uri uri) {
        try {
            Drawable d = Drawable.createFromStream(
                    getContentResolver().openInputStream(uri),
                    null
            );
            findViewById(R.id.rl).setBackground(d);
        } catch (Exception e) {
            Log.e(TAG, "Error loading wallpaper", e);
            findViewById(R.id.rl).setBackgroundResource(R.drawable.default_background);
        }
    }

    private void stopRinging() {
        Intent stopIntent = new Intent(this, AlarmForegroundService.class);
        stopIntent.setAction(AlarmReceiver.ACTION_STOP_ALARM);
        startService(stopIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d("AlarmRingingActivity", "WakeLock released");
        }
    }

}
