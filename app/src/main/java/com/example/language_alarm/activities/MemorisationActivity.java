package com.example.language_alarm.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Flashcard;
import com.example.language_alarm.models.Lesson;
import com.example.language_alarm.models.LessonViewModel;
import com.example.language_alarm.utils.InputFlashcardAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class MemorisationActivity extends AppCompatActivity {
    private static final String TAG = "MemorisationActivity";
    private InputFlashcardAdapter adapter;
    private List<Flashcard> allFlashcards;
    private int currFlashcardIndex = 0;
    private Lesson lesson;
    private Queue<Integer> cardIndexes = new LinkedList<>();
    private boolean secondClick = false;

    private static Queue<Integer> generateUniqueRandomNumbers(int count, int max) {
        if (count > (max + 1)) {
            Log.w(TAG, "Cannot generate more unique numbers than the available range.");
            count = max;
        }

        HashSet<Integer> uniqueNumbers = new HashSet<>();
        Random random = new Random();

        while (uniqueNumbers.size() < count) {
            Integer randomNumber = random.nextInt(max + 1);
            uniqueNumbers.add(randomNumber);
        }
        return new LinkedList<>(uniqueNumbers);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        int lessonId = getIntent().getIntExtra("lessonId", 0);
        if (lessonId == 0) {
            Log.w(TAG, "Attempted to perform null lesson");
            finish();
            return;
        }
        int qnCount = getIntent().getIntExtra("qnCount", 3);

        RecyclerView recyclerView = findViewById(R.id.values_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InputFlashcardAdapter(new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        LessonViewModel lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
        lessonViewModel.getLesson(lessonId).observe(this, lesson -> {
            if (lesson == null) {
                Log.w(TAG, "Attempted to perform null lesson");
                finish();
                return;
            }
            adapter.setHeaders(lesson.getHeaders());
            allFlashcards = lesson.getFlashcards();
            if (allFlashcards == null || allFlashcards.isEmpty()) {
                finish();
            }
            this.lesson = lesson;

            cardIndexes = generateUniqueRandomNumbers(qnCount, this.allFlashcards.size() - 1);
            if (cardIndexes.isEmpty()) {
                Log.w(TAG, "Card indexes generate is empty");
                finish();
                return;
            }
            handleNextCard();
        });

        MaterialButton submitButton = findViewById(R.id.nextButton);
        submitButton.setOnClickListener(v -> {
            if (secondClick) {
                handleNextCard();
            } else {
                validateAnswers();
            }
        });

    }

    private void handleNextCard() {
        if (cardIndexes.isEmpty()) {
            finish();
            return;
        }
        Integer nextInd = cardIndexes.poll();
        if (nextInd == null) {
            throw new IllegalStateException("Card Index picked is null");
        }
        this.currFlashcardIndex = nextInd;
        secondClick = false;
        Flashcard nextCard = allFlashcards.get(nextInd);
        if (nextCard == null) {
            Log.e(TAG, "Flashcard at index " + nextInd + " is null");
            handleNextCard(); // Skip to the next card
            return;
        }
        adapter.setValues(nextCard);
    }

    private void validateAnswers() {
        List<String> userInput = adapter.getUserAnswers();
        if (currFlashcardIndex >= allFlashcards.size() || currFlashcardIndex < 0) {
            finish();
            return;
        }
        Flashcard curr = allFlashcards.get(currFlashcardIndex);
        boolean isCorrect = curr.isCorrect(userInput, lesson.isPunctSensitive(), lesson.isCaseSensitive());
        if (isCorrect) {
            adapter.showAnswers();
        } else {
            this.cardIndexes.add(currFlashcardIndex);
        }
        secondClick = true;
    }

}
