package com.example.language_alarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.AlarmAdapter;
import com.example.language_alarm.R;
import com.example.language_alarm.utils.AlarmScheduler;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmScheduler.rescheduleAll(this);
        recyclerView = findViewById(R.id.alarm_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AlarmAdapter adapter = new AlarmAdapter(this, AlarmScheduler.loadAlarms(this));
        System.out.println(AlarmScheduler.loadAlarms(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlarmAdapter adapter = new AlarmAdapter(this, AlarmScheduler.loadAlarms(this));
        recyclerView.setAdapter(adapter);
    }

    public void onToggleNewAlarm(View view) {
        Intent intent = new Intent(this, NewAlarmActivity.class);
        startActivity(intent);
    }
}