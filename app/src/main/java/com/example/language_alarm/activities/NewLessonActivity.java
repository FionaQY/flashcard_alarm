package com.example.language_alarm.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.utils.AlarmScheduler;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

public class NewLessonActivity extends AppCompatActivity {
    Toolbar header;
    TimePicker alarmTimePicker;
    AlarmManager alarmManager;
    Alarm alarmToEdit = null; // if editing alarm instead

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_alarm);

        this.header = findViewById(R.id.toolbar);
        setSupportActionBar(header);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Next alarm not yet set.");

        alarmTimePicker = findViewById(R.id.timePicker);

        Alarm alarmToEdit = getIntent().getParcelableExtra("alarm");
        if (alarmToEdit != null) {
            alarmTimePicker.setHour(alarmToEdit.getHour());
            alarmTimePicker.setMinute(alarmToEdit.getMinute());
            getSupportActionBar().setTitle(String.format(Locale.US, "Edit Alarm %02d:%02d", alarmToEdit.getHour(), alarmToEdit.getMinute()));
            this.alarmToEdit = alarmToEdit;
        }

        alarmTimePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            String amPm = hourOfDay < 12 ? "AM" : "PM";
            int displayHour = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
            getSupportActionBar().setTitle(String.format(Locale.US, "Alarm set for %d:%02d %s", displayHour, minute, amPm));
        });

        findViewById(R.id.saveButton).setOnClickListener(v -> saveAlarm());
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitDialog();
                    }
                });
    }

    private void saveAlarm() {
        int hour = alarmTimePicker.getHour();
        int minute = alarmTimePicker.getMinute();

        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            Toast.makeText(this, "Invalid time",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ToggleButton isSun = findViewById(R.id.isSun);
        ToggleButton isMon = findViewById(R.id.isMon);
        ToggleButton isTues = findViewById(R.id.isTues);
        ToggleButton isWed = findViewById(R.id.isWed);
        ToggleButton isThurs = findViewById(R.id.isThurs);
        ToggleButton isFri = findViewById(R.id.isFri);
        ToggleButton isSat = findViewById(R.id.isSat);

        Alarm newAlarm = new Alarm(
                hour, minute,
                0, 5, true,
                isSun.isChecked(), isMon.isChecked(), isTues.isChecked(), isWed.isChecked(),
                isThurs.isChecked(), isFri.isChecked(), isSat.isChecked()
        );
        if (this.alarmToEdit != null) {
            newAlarm.setId(this.alarmToEdit.getId());
        }

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
                runOnUiThread(() -> Toast.makeText(this,
                            "Failed to set alarm: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
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

}
