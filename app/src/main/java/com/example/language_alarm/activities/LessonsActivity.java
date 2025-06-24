package com.example.language_alarm.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;

public class LessonsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        RecyclerView recyclerView = findViewById(R.id.lesson_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new AlarmAdapter(this, new ArrayList<>());
//        recyclerView.setAdapter(adapter);
//
//        AlarmViewModel alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
//        alarmViewModel.getAllAlarms().observe(this, alarms -> {
//            adapter.setAlarms(alarms);
//        });

    }
}
