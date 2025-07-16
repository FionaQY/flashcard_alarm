package com.example.language_alarm.activities;

import android.content.Intent;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private AlarmAdapter adapter = null;

    // TODO: light and dark mode
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();

        RecyclerView recyclerView = findViewById(R.id.alarm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlarmAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        AlarmViewModel alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        alarmViewModel.getAllAlarms().observe(this, alarms -> adapter.setAlarms(alarms));

        findViewById(R.id.addAlarmFab).setOnClickListener(v -> onToggleNewAlarm());
        findViewById(R.id.addLessonFab).setOnClickListener(v -> onToggleLessons());
    }

    public void onToggleNewAlarm() {
        Intent intent = new Intent(this, NewAlarmActivity.class);
        startActivity(intent);
    }

    public void onToggleLessons() {
        Intent intent = new Intent(this, LessonsActivity.class);
        startActivity(intent);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(toolbar, "");
        setSupportActionBar(toolbar);
    }
}