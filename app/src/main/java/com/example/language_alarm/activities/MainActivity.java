package com.example.language_alarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.models.AlarmViewModel;
import com.example.language_alarm.utils.AlarmAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private AlarmAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.alarm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlarmAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        AlarmViewModel alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        alarmViewModel.getAllAlarms().observe(this, alarms -> adapter.setAlarms(alarms));
    }

    public void onToggleNewAlarm(View view) {
        Intent intent = new Intent(this, NewAlarmActivity.class);
        startActivity(intent);
    }

    public void onToggleLessons(View view) {
        Intent intent = new Intent(this, LessonsActivity.class);
        startActivity(intent);
    }
}