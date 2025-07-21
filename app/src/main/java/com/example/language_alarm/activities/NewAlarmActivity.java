package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.models.Lesson;
import com.example.language_alarm.utils.ActivityResultHelper;
import com.example.language_alarm.utils.AlarmHandler;
import com.example.language_alarm.utils.PermissionUtils;
import com.example.language_alarm.utils.ToolbarHelper;
import com.example.language_alarm.viewmodel.LessonViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class NewAlarmActivity extends AppCompatActivity {
    private static final Integer[] NUMBER_OF_SNOOZES = new Integer[]{
            0, 1, 3, 5, 10, 1000
    };
    private static final Integer[] SNOOZE_DURATION = new Integer[]{
            1, 5, 10, 15, 20, 25, 30
    };
    AutoCompleteTextView snoozeNumDropdown;
    Integer selectedSnoozeNum;
    AutoCompleteTextView snoozeDurationDropdown;
    Integer selectedSnoozeDuration;
    private EditText numOfQns;
    private MaterialToolbar toolbar;
    private TimePicker alarmTimePicker;
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
        setContentView(R.layout.activity_new_alarm);

        setupToolbar();
        initializeViews();

        this.alarmToEdit = getIntent().getParcelableExtra("alarm");
        if (alarmToEdit != null) {
            populateAlarmData();
        }

        setupListeners();
        setupDropdown();
    }

    private void setupDropdown() {
        snoozeNumDropdown.setAdapter(
                new ArrayAdapter<>(this, R.layout.dropdown_item, NUMBER_OF_SNOOZES)
        );
        snoozeNumDropdown.setOnItemClickListener((p, v, pos, id) -> selectedSnoozeNum = NUMBER_OF_SNOOZES[pos]);

        snoozeDurationDropdown.setAdapter(
                new ArrayAdapter<>(this, R.layout.dropdown_item, SNOOZE_DURATION)
        );
        snoozeDurationDropdown.setOnItemClickListener((p, v, pos, id) -> selectedSnoozeDuration = SNOOZE_DURATION[pos]);

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
        numOfQns = findViewById(R.id.numOfQns);
        snoozeNumDropdown = findViewById(R.id.numOfSnoozes);
        snoozeDurationDropdown = findViewById(R.id.snoozeDuration);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(toolbar, "New Alarm", true, this::showExitDialog);
        setSupportActionBar(toolbar);
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
        try {
            try {
                getContentResolver().takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (SecurityException e) {
                Log.w("NewAlarmActivity", "Couldn't get persistable permission, will use temporary permission");
            }

            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
                if (pfd != null) {
                    selectedAudio = uri;
                    String filename = getFileName(selectedAudio);
                    ((MaterialButton) findViewById(R.id.selectToneButton)).setText(filename);
                }
            } catch (IOException e) {
                Log.e("NewAlarmActivity", "Error accessing selected audio file", e);
            }
        } catch (Exception e) {
            Log.e("NewAlarmActivity", "Error handling audio selection", e);
        }
    }

    private void populateAlarmData() {
        Objects.requireNonNull(buttons);
        alarmTimePicker.setHour(alarmToEdit.getHour());
        alarmTimePicker.setMinute(alarmToEdit.getMinute());

        updateToolbarTitle(alarmToEdit.getHour(), alarmToEdit.getMinute());
        boolean[] alarmDays = alarmToEdit.getEnabledDays();

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setChecked(alarmDays[i]);
        }

        MaterialButton selectRingtoneButton = findViewById(R.id.selectToneButton);
        if (alarmToEdit.getRingtone() != null) {
            selectedAudio = Uri.parse(alarmToEdit.getRingtone());
            selectRingtoneButton.setText(getFileName(selectedAudio));
        } else {
            selectRingtoneButton.setText(R.string.select_ringtone);
        }
        numOfQns.setText(String.valueOf(alarmToEdit.getQnNum()));

        selectedSnoozeNum = alarmToEdit.getSnoozeNum();
        snoozeNumDropdown.setText(String.valueOf(selectedSnoozeNum));

        selectedSnoozeDuration = alarmToEdit.getSnoozeDuration();
        snoozeDurationDropdown.setText(String.valueOf(selectedSnoozeDuration));
    }

    private void updateToolbarTitle(int hourOfDay, int minute) {
        String amPm = hourOfDay < 12 ? "AM" : "PM";
        int displayHour = hourOfDay % 12 == 0 ? 12 : hourOfDay % 12;
        String title = String.format(Locale.US, "Alarm set for %d:%02d %s", displayHour, minute, amPm);

        TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (titleView != null) {
            titleView.setText(title);
        }
    }

    private void checkPermissionAndSave() {
        Alarm newAlarm = createAlarm();
        if (newAlarm == null) {
            return;
        }

        if (!PermissionUtils.hasScheduleAlarmPermission(this)) {
            pendingAlarmToSave = newAlarm;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                return;
            }
        }

        if (PermissionUtils.noNotificationPermission(this)) {
            pendingAlarmToSave = newAlarm;
            PermissionUtils.requestNotificationPermission(this);
        }

        saveAlarm(newAlarm);
    }

    private void saveAlarm(Alarm newAlarm) {
        if (newAlarm == null) return;
        AlarmHandler.saveAlarm(this, newAlarm);
        AlarmHandler.rescheduleAlarm(this, newAlarm);
        Toast.makeText(this,
                String.format(Locale.US, "Alarm set for %s", newAlarm.getNextAlarmTime().getTime()),
                Toast.LENGTH_SHORT).show();
        supportFinishAfterTransition();
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
        String selectedNumOfQns = this.numOfQns.getText().toString();
        Alarm newAlarm = new Alarm(
                hour, minute, selectedSnoozeNum == null ? 0 : selectedSnoozeNum,
                selectedSnoozeDuration == null ? 0 : selectedSnoozeDuration, isOneTime,
                daysChecked, ringtone, selectedNumOfQns.isEmpty() ? 0 : Integer.parseInt(selectedNumOfQns)
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

            boolean first = true;
            for (ToggleButton button : buttons) {
                if (box.isChecked() && button.isChecked() && first) {
                    first = false;
                    continue;
                }
                button.setEnabled(!box.isChecked());
            }

        }
    }


    private void selectAlarmTone() {
        if (PermissionUtils.noStoragePermission(this)) {
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
        Alarm tempAlarm = createAlarm();
        if (tempAlarm == null || tempAlarm.equals(alarmToEdit)) {
            this.supportFinishAfterTransition();
            return;
        }
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage(alarmToEdit == null ? "Go back?\nAlarm will not be saved." : "Go back?\nChanges will not be saved.")
                .setPositiveButton("Confirm",
                        (dialog, which) -> this.supportFinishAfterTransition())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtils.hasScheduleAlarmPermission(this)) {
            if (pendingAlarmToSave != null) {
                saveAlarm(pendingAlarmToSave);
                pendingAlarmToSave = null;
            }
        } else {
            Toast.makeText(this, "Exact alarm permission is required to schedule alarms.", Toast.LENGTH_SHORT).show();
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
