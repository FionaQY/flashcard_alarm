package com.example.language_alarm.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.language_alarm.R;
import com.example.language_alarm.models.ActivityResultHelper;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.models.Lesson;
import com.example.language_alarm.models.LessonViewModel;
import com.example.language_alarm.utils.AlarmHandler;
import com.example.language_alarm.utils.PermissionUtils;
import com.example.language_alarm.utils.ToolbarHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

public class NewAlarmActivity extends AppCompatActivity {
    private MaterialToolbar header;
    private TimePicker alarmTimePicker;
    private AlarmManager alarmManager;
    private Alarm alarmToEdit = null; // if editing existing alarm instead of creating new alarm
    private Alarm pendingAlarmToSave = null; // if permissions not granted
    private ToggleButton[] buttons = null;
    private CheckBox oneTimeCheckBox;
    private Uri selectedAudio;
    private ActivityResultHelper audioPickerHelper = null;
    private Lesson selectedLesson = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_alarm);

        setupToolbar();
        initializeViews();

        this.alarmToEdit = getIntent().getParcelableExtra("alarm");
        if (alarmToEdit != null) {
            populateAlarmData();
            FloatingActionButton deleteButt = findViewById(R.id.deleteButton);
            deleteButt.setVisibility(View.VISIBLE);
            deleteButt.setOnClickListener(v -> {
                AlarmHandler.deleteAlarm(this, alarmToEdit);
                finishAfterTransition();
            });
        }

        setupListeners();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        setupDropdown();
    }

    private void setupDropdown() {
        LessonViewModel lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
        AutoCompleteTextView lessonsDropdown = findViewById(R.id.lesson_dropdown);
        ArrayAdapter<Lesson> lessonAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                Lesson selectedLesson = getItem(position);
                view.setText(selectedLesson == null ? "Null lesson" : selectedLesson.getLessonName());
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                Lesson selectedLesson = getItem(position);
                view.setText(selectedLesson == null ? "Null lesson" : selectedLesson.getLessonName());
                return view;
            }
        };

        lessonsDropdown.setAdapter(lessonAdapter);
        lessonsDropdown.setOnItemClickListener((p, v, pos, id) -> {
            selectedLesson = lessonAdapter.getItem(pos);
            if (selectedLesson != null) {
                lessonsDropdown.setText(selectedLesson.getLessonName(), false);
            }
        });

        lessonViewModel.getAllLessons().observe(this, lessons -> {
            lessonAdapter.clear();
            if (lessons == null || lessons.isEmpty()) {
                lessonsDropdown.setHint("No lessons available");
                return;
            }
            lessonAdapter.addAll(lessons);
            if (alarmToEdit != null && alarmToEdit.getLessonId() > 0) {
                for (Lesson les : lessons) {
                    if (les.getId() == alarmToEdit.getLessonId()) {
                        lessonsDropdown.post(() -> {
                            lessonsDropdown.setText(les.getLessonName(), false);
                            selectedLesson = les;
                        });
                        break;
                    }
                }
            }
        });
    }

    private void initializeViews() {
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
        header = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(header, "New Alarm", true, this::showExitDialog);
        setSupportActionBar(header);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void setupListeners() {
        findViewById(R.id.saveButton).setOnClickListener(v -> checkPermissionAndSave());
        findViewById(R.id.oneTime).setOnClickListener(this::onClickOneTime);
        findViewById(R.id.selectToneButton).setOnClickListener(v -> selectAlarmTone());
        alarmTimePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> updateToolbarTitle(hourOfDay, minute));

        audioPickerHelper = new ActivityResultHelper(this, this::handleAudioSelection);

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        showExitDialog();
                    }
                });
    }

    private void handleAudioSelection(Uri uri) {
        selectedAudio = uri;
        String filename = getFileName(selectedAudio);
//                TextView toneText = findViewById(R.id.selectToneButton);
//                toneText.setText(String.format("Selected: %s", filename));
    }

    private void populateAlarmData() {
        Objects.requireNonNull(buttons);
        alarmTimePicker.setHour(alarmToEdit.getHour());
        alarmTimePicker.setMinute(alarmToEdit.getMinute());

        updateToolbarTitle(alarmToEdit.getHour(), alarmToEdit.getMinute());
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

        if (alarmToEdit.getRingtone() != null) {
            selectedAudio = Uri.parse(alarmToEdit.getRingtone());
        }
    }

    private void updateToolbarTitle(int hourOfDay, int minute) {
        String amPm = hourOfDay < 12 ? "AM" : "PM";
        int displayHour = hourOfDay % 12 == 0 ? 12 : hourOfDay % 12;
        String title = String.format(Locale.US, "Alarm set for %d:%02d %s", displayHour, minute, amPm);

        TextView titleView = header.findViewById(R.id.toolbar_title);
        if (titleView != null) {
            titleView.setText(title);
        }
    }

    private void checkPermissionAndSave() {

        Alarm newAlarm = createAlarm();
        if (newAlarm == null) {
            return;
        }

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

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AlarmHandler.saveAlarm(this, newAlarm);

                runOnUiThread(() -> {
                    AlarmHandler.scheduleAlarm(this, newAlarm);
                    Toast.makeText(this,
                            String.format(Locale.US, "Alarm set for %02d:%02d", newAlarm.getHour(), newAlarm.getMinute()),
                            Toast.LENGTH_SHORT).show();
                    supportFinishAfterTransition();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Failed to set alarm: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private Alarm createAlarm() {
        int hour = alarmTimePicker.getHour();
        int minute = alarmTimePicker.getMinute();

        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            Toast.makeText(this, "Invalid time",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        boolean noneSelected = true;
        boolean[] daysChecked = new boolean[7];
        for (int i = 0; i < this.buttons.length; i++) {
            if (this.buttons[i].isChecked()) {
                noneSelected = false;
            }
            daysChecked[i] = this.buttons[i].isChecked() && this.buttons[i].isEnabled();
        }

        boolean isOneTime = oneTimeCheckBox.isChecked() || noneSelected;

        String ringtone = selectedAudio == null ? "" : selectedAudio.toString();
        Alarm newAlarm = new Alarm(
                hour, minute,
                0, 5, isOneTime,
                daysChecked[0], daysChecked[1], daysChecked[2], daysChecked[3],
                daysChecked[4], daysChecked[5], daysChecked[6], ringtone
        );
        if (this.alarmToEdit != null) {
            newAlarm.setId(this.alarmToEdit.getId());
        }
        if (selectedLesson != null) {
            newAlarm.setLessonId(selectedLesson.getId());
        }
        return newAlarm;
    }

    protected void onClickOneTime(View v) {
        if (v instanceof CheckBox) {
            CheckBox box = (CheckBox) v;

            for (ToggleButton button : buttons) {
                button.setEnabled(!box.isChecked());
            }
        }
    }


    private void selectAlarmTone() {
        if (!PermissionUtils.hasStoragePermission(this)) {
            PermissionUtils.requestStoragePermission(this);
            return;
        }
        openAudioPicker();
    }

    private void openAudioPicker() {
        audioPickerHelper.launchAudioPicker();
    }

    private String getFileName(Uri uri) {
        String res = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    res = index >= 0 ? cursor.getString(index) : null;
                }
            }

        }
        return res != null ? res : uri.getPath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtils.REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAudioPicker();
            } else {
                Toast.makeText(this, "Permission required to select alarm tones",
                        Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(this)
                        .setTitle("No Permission")
                        .setMessage("Use default alarm tone instead?")
                        .setPositiveButton("Yes", (d, w) -> selectedAudio = null)
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    public void showExitDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage("Cancel this alarm?")
                .setPositiveButton("Confirm",
                        (dialog, which) -> this.supportFinishAfterTransition())
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
