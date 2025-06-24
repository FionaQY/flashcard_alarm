package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.app.AlarmManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.utils.AlarmScheduler;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewAlarmActivity extends AppCompatActivity {
    Toolbar header;
    TimePicker alarmTimePicker;
    AlarmManager alarmManager;
    Alarm alarmToEdit = null; // if editing alarm instead
    private ToggleButton isSun, isMon, isTues, isWed, isThurs, isFri, isSat;
    private CheckBox oneTimeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_alarm);

        initializeViews();
        setupToolbar();

        Alarm alarmToEdit = getIntent().getParcelableExtra("alarm");
        if (alarmToEdit != null) {
            this.alarmToEdit = alarmToEdit;
            populateAlarmData();
        }

        alarmTimePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> updateToolbarTitle(hourOfDay, minute));

        findViewById(R.id.saveButton).setOnClickListener(v -> saveAlarm());
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        findViewById(R.id.oneTime).setOnClickListener(this::onClickOneTime);
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitDialog();
                    }
                });
    }

    private void initializeViews() {
        header = findViewById(R.id.toolbar);
        alarmTimePicker = findViewById(R.id.timePicker);

        // Initialize toggle buttons
        isSun = findViewById(R.id.isSun);
        isMon = findViewById(R.id.isMon);
        isTues = findViewById(R.id.isTues);
        isWed = findViewById(R.id.isWed);
        isThurs = findViewById(R.id.isThurs);
        isFri = findViewById(R.id.isFri);
        isSat = findViewById(R.id.isSat);

        oneTimeCheckBox = findViewById(R.id.oneTime);
    }

    private void setupToolbar() {
        setSupportActionBar(header);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Next alarm not yet set.");
    }

    private void populateAlarmData() {
        alarmTimePicker.setHour(alarmToEdit.getHour());
        alarmTimePicker.setMinute(alarmToEdit.getMinute());

        updateToolbarTitle(alarmToEdit.getHour(), alarmToEdit.getMinute());

        isSun.setChecked(alarmToEdit.isSunday());
        isMon.setChecked(alarmToEdit.isMonday());
        isTues.setChecked(alarmToEdit.isTuesday());
        isWed.setChecked(alarmToEdit.isWednesday());
        isThurs.setChecked(alarmToEdit.isThursday());
        isFri.setChecked(alarmToEdit.isFriday());
        isSat.setChecked(alarmToEdit.isSaturday());
    }

    private void updateToolbarTitle(int hourOfDay, int minute) {
        String amPm = hourOfDay < 12 ? "AM" : "PM";
        int displayHour = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
        Objects.requireNonNull(getSupportActionBar()).setTitle(String.format(Locale.US, "Alarm set for %d:%02d %s", displayHour, minute, amPm));
    }

    private void saveAlarm() {
        int hour = alarmTimePicker.getHour();
        int minute = alarmTimePicker.getMinute();

        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            Toast.makeText(this, "Invalid time",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Alarm newAlarm = createAlarm(hour, minute);

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

    @NonNull
    private Alarm createAlarm(int hour, int minute) {
        Alarm newAlarm = new Alarm(
                hour, minute,
                0, 5, oneTimeCheckBox.isChecked(),
                isMon.isChecked() && isMon.isEnabled(),
                isTues.isChecked() && isTues.isEnabled(),
                isWed.isChecked() && isWed.isEnabled(),
                isThurs.isChecked() && isThurs.isEnabled(),
                isFri.isChecked() && isFri.isEnabled(),
                isSat.isChecked() && isSat.isEnabled(),
                isSun.isChecked() && isSun.isEnabled()
                );
        if (this.alarmToEdit != null) {
            newAlarm.setId(this.alarmToEdit.getId());
        }
        return newAlarm;
    }

    protected void onClickOneTime(View v) {
        if (v instanceof CheckBox) {
            CheckBox box = (CheckBox) v;
            ToggleButton[] buttons = {isSat, isSun, isMon, isTues, isWed, isThurs, isFri};
            for (ToggleButton button: buttons) {
                button.setEnabled(!box.isChecked());
            }
        }
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
