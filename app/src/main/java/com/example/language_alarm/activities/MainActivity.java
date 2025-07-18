package com.example.language_alarm.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.adapter.AlarmAdapter;
import com.example.language_alarm.utils.ToolbarHelper;
import com.example.language_alarm.viewmodel.AlarmViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.language_alarm.utils.SettingUtils;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatDelegate;
import com.shrikanthravi.library.NightModeButton;

public class MainActivity extends AppCompatActivity {
    private AlarmAdapter adapter = null;
    private static final String THEME = "isNightMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAppTheme();
        setContentView(R.layout.activity_main);
        setupToolbar();

        findViewById(R.id.nightModeButton).setOnSwitchListener(new NightModeButton.OnSwitchListener() {
            @Override
            public void onSwitchListener(boolean isNight) {
                onToggleDarkMode(isNight);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.alarm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlarmAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        AlarmViewModel alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        alarmViewModel.getAllAlarms().observe(this, alarms -> adapter.setAlarms(alarms));

        findViewById(R.id.addAlarmFab).setOnClickListener(v -> onToggleNewAlarm());
        findViewById(R.id.addLessonFab).setOnClickListener(v -> onToggleLessons());
    }

    private void setAppTheme() {
        AppCompatDelegate.setDefaultNightMode(SettingUtils.getIsDarkTheme() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void onToggleDarkMode(boolean isNight) {
        SettingUtils.setIsDarkTheme(isNight);
        setAppTheme();
        recreate(); 
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
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(toolbar, "");
        setSupportActionBar(toolbar);
    }
}