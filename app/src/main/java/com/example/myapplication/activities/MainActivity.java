package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.utils.AlarmScheduler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmScheduler.rescheduleAll(this);
    }

    public void onToggleNewAlarm(View view) {
        Intent intent = new Intent(this, NewAlarmActivity.class);
        startActivity(intent);
    }
}