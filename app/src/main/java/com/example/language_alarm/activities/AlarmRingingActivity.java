package com.example.language_alarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.utils.AlarmForegroundService;
import com.example.language_alarm.utils.AlarmHandler;
import com.example.language_alarm.utils.AlarmReceiver;

public class AlarmRingingActivity extends AppCompatActivity {
    Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringing);

        alarm = getIntent().getParcelableExtra("alarm");
        if (alarm == null) {
            Log.w("Alarm", "Attempted to ring null alarm");
            supportFinishAfterTransition();
        }

        findViewById(R.id.snoozeButton).setOnClickListener(v -> {
            AlarmHandler.snoozeAlarm(this, alarm);

            Intent stopIntent = new Intent(this, AlarmForegroundService.class);
            stopIntent.setAction(AlarmReceiver.ACTION_STOP_ALARM);
            startActivity(stopIntent);

            finish();
        });

        findViewById(R.id.stopButton).setOnClickListener(v -> {
            Intent stopIntent = new Intent(this, AlarmForegroundService.class);
            stopIntent.setAction(AlarmReceiver.ACTION_STOP_ALARM);
            startActivity(stopIntent);

            Intent intent = new Intent(this, NewAlarmActivity.class);
            startActivity(intent);
            finish();
        });

        // TODO: get lesson from lessonID in background (do this in others)
        // TODO: snooze -> snoozeAlarm. snooze till alarm count done
        // TODO: do lesson button -> new activity and layout
    }
}
