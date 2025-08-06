package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.adapter.AlarmAdapter;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.utils.SettingUtils;
import com.example.language_alarm.utils.ToolbarHelper;
import com.example.language_alarm.viewmodel.AlarmViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    SettingUtils prefs = null;
    private AlarmAdapter adapter = null;
    private MaterialToolbar toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new SettingUtils(this);

        setContentView(R.layout.activity_main);
        setupToolbar();

        RecyclerView recyclerView = findViewById(R.id.alarm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlarmAdapter(this, new ArrayList<>(), this::updateToolbarTitle);
        recyclerView.setAdapter(adapter);

        AlarmViewModel alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        alarmViewModel.getAllAlarms().observe(this, alarms -> adapter.setAlarms(alarms));

        findViewById(R.id.addAlarmFab).setOnClickListener(v -> onToggleNewAlarm());
        findViewById(R.id.addLessonFab).setOnClickListener(v -> onToggleLessons());
        findViewById(R.id.settingsFab).setOnClickListener(v -> showSettingsDialog());
    }

    private void updateToolbarTitle(List<Alarm> alarms) {
        TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (titleView == null) {
            return;
        }
        if (alarms == null || alarms.isEmpty()) {
            titleView.setText("");
            return;
        }
        Calendar cal = null;
        Alarm latestAlarm = null;
        for (Alarm alarm : alarms) {
            if (!alarm.isEnabled()) {
                continue;
            }
            if (cal == null || alarm.getNextAlarmTime().before(cal)) {
                latestAlarm = alarm;
                cal = latestAlarm.getNextAlarmTime();
            }
        }
        if (latestAlarm != null) {
            titleView.setText(String.format(Locale.US, "Ringing in %s", latestAlarm.getNextTimeString()));
        }
    }

    private void onToggleNewAlarm() {
        Intent intent = new Intent(this, NewAlarmActivity.class);
        startActivity(intent);
    }

    private void onToggleLessons() {
        Intent intent = new Intent(this, LessonsActivity.class);
        startActivity(intent);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(toolbar, "");
        setSupportActionBar(toolbar);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}