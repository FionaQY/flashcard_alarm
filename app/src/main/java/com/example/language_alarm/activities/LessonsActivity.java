package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.adapter.LessonAdapter;
import com.example.language_alarm.models.Lesson;
import com.example.language_alarm.utils.ToolbarHelper;
import com.example.language_alarm.viewmodel.LessonViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LessonsActivity extends AppCompatActivity {
    private LessonAdapter lessonAdapter = null;
    private TextView emptyView;
    private List<Lesson> lessonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        setupToolbar();

        emptyView = findViewById(R.id.empty_view);

        RecyclerView recyclerView = findViewById(R.id.lesson_list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        lessonAdapter = new LessonAdapter(this, new ArrayList<>(),
                this::showMemoDialog);
        recyclerView.setAdapter(lessonAdapter);

        LessonViewModel lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
        lessonViewModel.getAllLessons().observe(this, lessons -> {
            lessonAdapter.setLessons(lessons);
            lessonList = lessons;
            emptyView.setVisibility(lessons.isEmpty() ? View.VISIBLE : View.GONE);
        });

        ((TextInputEditText) findViewById(R.id.searchBar).findViewById(R.id.searchEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().trim();
                if (lessonList == null || lessonList.isEmpty()) {
                    return;
                }
                if (str.isEmpty()) {
                    return;
                }
                List<Lesson> less = new ArrayList<>();
                for (Lesson lesson : lessonList) {
                    if (lesson.toString().toUpperCase().contains(str.toUpperCase())) {
                        less.add(lesson);
                    }
                }
                lessonAdapter.setLessons(less);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
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

    private void showMemoDialog(int lessonId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_memo, null);
        builder.setView(dialogView);

        EditText numOfQns = dialogView.findViewById(R.id.numOfQns);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.nextButton).setOnClickListener(v -> {
            String sTextFromET = numOfQns.getText().toString();
            int nIntFromET = Integer.parseInt(sTextFromET);
            if (nIntFromET <= 0) {
                Toast.makeText(this, "Invalid number of questions", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, MemorisationActivity.class);
            intent.putExtra("lessonId", lessonId);
            intent.putExtra("qnCount", nIntFromET);
            startActivity(intent);
            dialog.dismiss();
        });
    }

}
