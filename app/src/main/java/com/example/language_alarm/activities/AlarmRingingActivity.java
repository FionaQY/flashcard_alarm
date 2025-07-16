package com.example.language_alarm.activities;

import android.app.KeyguardManager;
import android.content.Intent;
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

public class AlarmRingingActivity extends AppCompatActivity {
    Alarm alarm;

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
        MaterialButton snoozeButton = findViewById(R.id.snoozeButton);
        snoozeButton.setVisibility(alarm.allowSnooze() ? View.VISIBLE : View.GONE);

        snoozeButton.setOnClickListener(v -> {
            stopRinging();
            AlarmHandler.snoozeAlarm(this, alarm);
            alarm.incrementSnoozeCount();
            finish();
        });

        findViewById(R.id.stopButton).setOnClickListener(v -> {
            stopRinging();

            AlarmHandler.cancelAlarm(this, alarm);
            alarm.resetSnoozeCount();

            Intent intent = new Intent(this, MemorisationActivity.class);
            intent.putExtra("lessonId", alarm.getLessonId());
            intent.putExtra("qnCount", alarm.getQnNum());
            intent.putExtra("isAlarm", true);
            startActivity(intent);
            finish();
            if (!alarm.isOneTime()) {
                AlarmHandler.rescheduleAlarm(this, alarm);
            }
        });

    }

    private void stopRinging() {
        Intent stopIntent = new Intent(this, AlarmForegroundService.class);
        stopIntent.setAction(AlarmReceiver.ACTION_STOP_ALARM);
        startService(stopIntent);
    }
}
