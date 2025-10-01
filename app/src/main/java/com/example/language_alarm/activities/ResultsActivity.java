package com.example.language_alarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Flashcard;

import java.util.ArrayList;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {

    private ArrayList<Flashcard> flashcards;
    private ArrayList<Boolean> answerResults;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView totalQuestionsText = findViewById(R.id.text_total_questions);
        TextView correctAnswersText = findViewById(R.id.text_correct_answers);
        TextView incorrectAnswersText = findViewById(R.id.text_incorrect_answers);
        RecyclerView resultsRecyclerView = findViewById(R.id.recycler_results_list);

        Intent intent = getIntent();
        int totalQuestions = intent.getIntExtra("totalQuestions", 0);
        int correctAnswers = intent.getIntExtra("correctAnswers", 0);
        int incorrectAnswers = totalQuestions - correctAnswers;

        totalQuestionsText.setText(String.format(Locale.US, "Total Questions: %d", totalQuestions));
        correctAnswersText.setText(String.format(Locale.US, "Correct Answers: %d", correctAnswers));
        incorrectAnswersText.setText(String.format(Locale.US, "Incorrect Answers: %d", incorrectAnswers));

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Set up adapter to display question details with answers.
        // resultsRecyclerView.setAdapter(new ResultSummaryAdapter(flashcards, answerResults));

        findViewById(R.id.button_retry).setOnClickListener(v -> {
            // Retry the quiz intent
            finish();
        });

        findViewById(R.id.button_back).setOnClickListener(v -> {
            Intent backIntent = new Intent(ResultsActivity.this, MainActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(backIntent);
            finish();
        });
    }
}
