package com.example.language_alarm.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.language_alarm.models.Alarm;

public class AlarmRingingActivity extends AppCompatActivity {
    Alarm alarm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        alarm = getIntent().getParcelableExtra("alarm");
        if (alarm == null) {
            Log.w("Alarm", "Attempted to ring null alarm");
            finish();
        }

        // TODO: new layout
        // TODO: get lesson from lessonID in background
        // TODO: snooze -> snoozeAlarm. snooze till alarm count done
        // TODO: do lesson button -> new activity and layout
    }
}
