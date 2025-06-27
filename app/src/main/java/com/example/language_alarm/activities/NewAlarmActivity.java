package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.AlarmManager;
import android.provider.Settings;
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
import java.util.concurrent.Executors;

public class NewAlarmActivity extends AppCompatActivity {
    private Toolbar header;
    private TimePicker alarmTimePicker;
    private AlarmManager alarmManager;
    private Alarm alarmToEdit = null; // if editing alarm instead
    private Alarm pendingAlarmToSave = null; // if permissions not granded
    private ToggleButton[] buttons = null;
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

        findViewById(R.id.saveButton).setOnClickListener(v -> checkPermissionAndSave());
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

        ToggleButton isSun = findViewById(R.id.isSun);
        ToggleButton isMon = findViewById(R.id.isMon);
        ToggleButton isTues = findViewById(R.id.isTues);
        ToggleButton isWed = findViewById(R.id.isWed);
        ToggleButton isThurs = findViewById(R.id.isThurs);
        ToggleButton isFri = findViewById(R.id.isFri);
        ToggleButton isSat = findViewById(R.id.isSat);
        this.buttons = new ToggleButton[]{isSun, isMon, isTues, isWed, isThurs, isFri, isSat};

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
        if (this.buttons == null) return;
        boolean[] alarmDays = {
                alarmToEdit.isSunday(),
                alarmToEdit.isMonday(),
                alarmToEdit.isTuesday(),
                alarmToEdit.isWednesday(),
                alarmToEdit.isThursday(),
                alarmToEdit.isFriday(),
                alarmToEdit.isSaturday()
        };

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setChecked(alarmDays[i]);
        }
    }

    private void updateToolbarTitle(int hourOfDay, int minute) {
        String amPm = hourOfDay < 12 ? "AM" : "PM";
        int displayHour = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
        Objects.requireNonNull(getSupportActionBar()).setTitle(String.format(Locale.US, "Alarm set for %d:%02d %s", displayHour, minute, amPm));
    }

    private void checkPermissionAndSave() {
        int hour = alarmTimePicker.getHour();
        int minute = alarmTimePicker.getMinute();

        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            Toast.makeText(this, "Invalid time",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Alarm newAlarm = createAlarm(hour, minute);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                pendingAlarmToSave = newAlarm;
                Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(permissionIntent);
                return;
            }
        }

        saveAlarm(newAlarm);
    }
    private void saveAlarm(Alarm newAlarm) {
        if (newAlarm == null) return;

        AlarmScheduler.scheduleAlarm(this, newAlarm);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AlarmScheduler.saveAlarm(this, newAlarm);

                runOnUiThread(() -> {
                    AlarmScheduler.scheduleAlarm(this, newAlarm);
                    Toast.makeText(this,
                            String.format(Locale.US, "Alarm set for %02d:%02d", newAlarm.getHour(), newAlarm.getMinute()),
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
        boolean noneSelected = true;
        for (ToggleButton btn : this.buttons) {
            if (btn.isChecked()) noneSelected = false;
        }
        boolean isOneTime =  oneTimeCheckBox.isChecked() || noneSelected;

        boolean[] daysChecked = new boolean[7];
        for (int i = 0; i < this.buttons.length; i++) {
            daysChecked[i] = this.buttons[i].isChecked() && this.buttons[i].isEnabled();
        }

        Alarm newAlarm = new Alarm(
                hour, minute,
                0, 5, isOneTime,
                daysChecked[1], daysChecked[2],  daysChecked[3], daysChecked[4],
                daysChecked[5],  daysChecked[6], daysChecked[0]
                );
        if (this.alarmToEdit != null) {
            newAlarm.setId(this.alarmToEdit.getId());
        }
        return newAlarm;
    }

    protected void onClickOneTime(View v) {
        if (v instanceof CheckBox) {
            CheckBox box = (CheckBox) v;

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
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Exact alarm permission is required to schedule alarms.", Toast.LENGTH_SHORT).show();
            } else if (pendingAlarmToSave != null) {
                saveAlarm(pendingAlarmToSave);
                pendingAlarmToSave = null;
            }
        }
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
