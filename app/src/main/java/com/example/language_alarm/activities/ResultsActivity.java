package com.example.language_alarm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.language_alarm.R;

public class ResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView totalQuestionsText = findViewById(R.id.text_total_questions);
        TextView correctAnswersText = findViewById(R.id.text_correct_answers);
        TextView incorrectAnswersText = findViewById(R.id.text_incorrect_answers);

        Intent intent = getIntent();
        int totalQuestions = intent.getIntExtra("totalQuestions", 0);
        int correctAnswers = intent.getIntExtra("correctAnswers", 0);
        int incorrectAnswers = totalQuestions - correctAnswers;

        totalQuestionsText.setText(getString(R.string.total_questions_format, totalQuestions));
        correctAnswersText.setText(getString(R.string.correct_answers_format, correctAnswers));
        incorrectAnswersText.setText(getString(R.string.incorrect_answers_format, incorrectAnswers));

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
