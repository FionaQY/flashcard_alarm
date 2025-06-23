package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.language_alarm.AlarmReceiver;
import com.example.language_alarm.R;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.utils.AlarmScheduler;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

public class NewAlarmActivity extends AppCompatActivity {
    Toolbar header;
    TimePicker alarmTimePicker;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_alarm);

//        Intent intent = getIntent();
//        String time = intent.getStringExtra("time");
//        String label = intent.getStringExtra("label");
//        boolean enabled = intent.getBooleanExtra("enabled", false);
//
//        // Update UI with the alarm details
//        TextView timeView = findViewById(R.id.detail_time);
//        TextView labelView = findViewById(R.id.detail_label);
//        TextView statusView = findViewById(R.id.detail_status);
//
//        timeView.setText(time);
//        labelView.setText(label);
//        statusView.setText(enabled ? "Enabled" : "Disabled");

        this.header = findViewById(R.id.toolbar);
        setSupportActionBar(header);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Next alarm not yet set.");

        alarmTimePicker = findViewById(R.id.timePicker);
        alarmTimePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            String amPm = hourOfDay < 12 ? "AM" : "PM";
            int displayHour = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
            getSupportActionBar().setTitle(String.format(Locale.US, "Alarm set for %d:%02d %s", displayHour, minute, amPm));
        });

        findViewById(R.id.saveButton).setOnClickListener(v -> saveAlarm());
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    private void saveAlarm() {
        Calendar calendar = Calendar.getInstance();
        int hour = alarmTimePicker.getHour();
        int minute = alarmTimePicker.getMinute();

        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            Toast.makeText(this, "Invalid time",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // calendar is called to get current time in hour minute
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);// If the time has already passed today, set it for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Alarm newAlarm = new Alarm(
                hour, minute,
                0,
                5,
                true,
                null);

        AlarmScheduler.scheduleAlarm(this, newAlarm);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Save first
                AlarmScheduler.saveAlarm(this, newAlarm);

                // Then schedule
                runOnUiThread(() -> {
                    AlarmScheduler.scheduleAlarm(this, newAlarm);
                    Toast.makeText(this,
                            String.format(Locale.US, "Alarm set for %02d:%02d", hour, minute),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "Failed to set alarm: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void showExitDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage("Cancel this alarm?")
                .setPositiveButton("Confirm",
                        (dialog, which) -> this.finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onSupportNavigateUp() {
        showExitDialog();
        return true;
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

}
