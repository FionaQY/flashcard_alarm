package com.example.language_alarm.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.language_alarm.R;

public class MemorisationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        int lessonId = getIntent().getIntExtra("lessonId", 0);
        int qnCount = getIntent().getIntExtra("qnCount", 3);

        // TODO: get lesson from lessonID in background (do this in others)
        // TODO: new activity and layout
    }

}
