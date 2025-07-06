package com.example.language_alarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.models.LessonViewModel;
import com.example.language_alarm.utils.LessonAdapter;
import com.example.language_alarm.utils.ToolbarHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Objects;

public class LessonsActivity extends AppCompatActivity {
    private LessonAdapter lessonAdapter = null;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        setupToolbar();

        emptyView = findViewById(R.id.empty_view);

        RecyclerView recyclerView = findViewById(R.id.lesson_list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        lessonAdapter = new LessonAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(lessonAdapter);

        LessonViewModel lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
        lessonViewModel.getAllLessons().observe(this, lessons -> {
            lessonAdapter.setLessons(lessons);
            emptyView.setVisibility(lessons.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupToolbar() {
        MaterialToolbar header = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(header, "Lessons", true, this::supportFinishAfterTransition);
        setSupportActionBar(header);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    public void onToggleAddLesson(View view) {
        Intent intent = new Intent(this, NewLessonActivity.class);
        startActivity(intent);
    }

}
