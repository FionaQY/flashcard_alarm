package com.example.language_alarm.activities;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.receiver.AlarmReceiver;
import com.example.language_alarm.utils.AlarmForegroundService;
import com.example.language_alarm.utils.AlarmHandler;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class AlarmRingingActivity extends AppCompatActivity {
    private static final String SNOOZE_COUNT = "snoozeCount";
    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTurnScreenOn(true);
        setShowWhenLocked(true);
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(this, null);

        setContentView(R.layout.activity_ringing);

        alarm = getIntent().getParcelableExtra("alarm");
        if (alarm == null) {
            Log.w("Alarm", "Attempted to ring null alarm");
            supportFinishAfterTransition();
        }
        SharedPreferences settings = SettingUtils.getSharedPreferences();
        int snoozeCount = settings.getInt(SNOOZE_COUNT, 0);

        MaterialButton snoozeButton = findViewById(R.id.snoozeButton);
        snoozeButton.setVisibility(alarm.getSnoozeNum() > snoozeCount ? View.VISIBLE : View.GONE);
        snoozeButton.setText(String.format(Locale.US, "%d Snoozes Left", alarm.getSnoozeNum() - snoozeCount));

        snoozeButton.setOnClickListener(v -> {
            stopRinging();
            AlarmHandler.snoozeAlarm(this, alarm);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(SNOOZE_COUNT, snoozeCount + 1);
            editor.apply();
            finish();
        });

        findViewById(R.id.stopButton).setOnClickListener(v -> {
            stopRinging();

            AlarmHandler.cancelAlarm(this, alarm);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(SNOOZE_COUNT, 0);
            editor.apply();

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

    private void stopRinging() {
        Intent stopIntent = new Intent(this, AlarmForegroundService.class);
        stopIntent.setAction(AlarmReceiver.ACTION_STOP_ALARM);
        startService(stopIntent);
    }
}
