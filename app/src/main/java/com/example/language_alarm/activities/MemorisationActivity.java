package com.example.language_alarm.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class MemorisationActivity extends AppCompatActivity {
    // TODO: countdown timer: 5mins -> alarm
    private static final String TAG = "MemorisationActivity";
    private InputFlashcardAdapter adapter;
    private List<Flashcard> allFlashcards = new ArrayList<>();
    private int currFlashcardIndex = 0;
    private Lesson lesson;
    private Queue<Integer> cardIndexes = new LinkedList<>();
    private boolean secondClick = false;

    private static Queue<Integer> generateUniqueRandomNumbers(int count, int max) {
        Queue<Integer> que = new LinkedList<>();
        if (count <= 0 || max < 0) {
            Log.w(TAG, "Invalid parameters - count: " + count + ", max: " + max);
            return que;
        }

        count = Math.min(count, max + 1);
        List<Integer> ind = new ArrayList<>();
        for (int i = 0; i < max + 1; i++) {
            ind.add(i);
        }
        Random rand = new Random();
        int n = rand.nextInt(69) + 1;
        for (int i = 0; i < n; i++) {
            Collections.shuffle(ind);
        }
        for (int i = 0; i < count; i++) {
            que.add(ind.get(i));
        }

        return que;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        int lessonId = getIntent().getIntExtra("lessonId", 0);
        if (lessonId == 0) {
            Log.w(TAG, "Attempted to perform null lesson");
            handleFinishQuiz();
            return;
        }
        int qnCount = getIntent().getIntExtra("qnCount", 3);

        RecyclerView recyclerView = findViewById(R.id.values_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InputFlashcardAdapter();
        recyclerView.setAdapter(adapter);

        LessonViewModel lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
        lessonViewModel.getLesson(lessonId).observe(this, lesson -> {
            if (lesson == null) {
                Log.w(TAG, "Attempted to perform null lesson");
                handleFinishQuiz();
                return;
            }
            adapter.setLesson(lesson);
            allFlashcards = lesson.getFlashcards();
            if (allFlashcards == null || allFlashcards.isEmpty()) {
                Log.w(TAG, String.format("No flashcards can be retrieved for lesson with id %d", lessonId));
                handleFinishQuiz();
                return;
            }
            this.lesson = lesson;

            cardIndexes = generateUniqueRandomNumbers(qnCount, this.allFlashcards.size() - 1);
            if (cardIndexes.isEmpty()) {
                Log.w(TAG, "Card indexes generate is empty");
                Log.w(TAG, String.format("No flashcards can be retrieved for lesson with id %d", lessonId));
                System.out.println(lesson.getFlashcards());
                handleFinishQuiz();
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
            handleFinishQuiz();
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
            Log.e(TAG, String.format("Flashcard at index %d is null", nextInd));
            handleNextCard();
            return;
        }
        adapter.setValues(nextCard);
    }

    private void validateAnswers() {
        List<String> userInput = adapter.getUserAnswers();
        if (currFlashcardIndex >= allFlashcards.size() || currFlashcardIndex < 0) {
            handleFinishQuiz();
            return;
        }
        // TODO: return arraylist of index. set each one in index to red
        boolean isCorrect = this.isCorrect(userInput);
        if (isCorrect) {
            Toast.makeText(this, "You got this correct!", Toast.LENGTH_LONG).show();
        } else {
            adapter.showAnswers();
            this.cardIndexes.add(currFlashcardIndex);
        }

        secondClick = true;
    }

    private boolean isCorrect(List<String> userInput) {
        if (lesson == null || lesson.getFlashcards() == null || this.currFlashcardIndex >= lesson.getFlashcards().size()) {
            return false;
        }
        List<String> values = lesson.getFlashcards().get(currFlashcardIndex).getVals();
        if (userInput == null || lesson.getFlashcards() == null || userInput.size() != values.size()) {
            return false;
        }
        for (int i = 0; i < values.size(); i++) {
            if (!lesson.getForeignIndexes().get(i)) {
                continue;
            }
            String corrAns = values.get(i);
            if (corrAns == null || corrAns.trim().isEmpty()) {
                continue;
            }
            String userAns = userInput.get(i);
            if (userAns == null || userAns.trim().isEmpty()) {
                Log.i(TAG, String.format("Answer is incorrect as user input is empty for header %s", lesson.getHeaders().get(i)));
                return false;
            }
            corrAns = corrAns.replaceAll("\\s", "");
            userAns = userAns.replaceAll("\\s", "");
            if (!lesson.isPunctSensitive()) {
                corrAns = corrAns.replaceAll("\\p{Punct}", "");
                userAns = userAns.replaceAll("\\p{Punct}", "");
            }
            if (!lesson.isCaseSensitive()) {
                corrAns = corrAns.toLowerCase();
                userAns = userAns.toLowerCase();
            }
            if (!corrAns.trim().equals(userAns.trim())) {
                Log.i(TAG, String.format("Answer is incorrect as user input is empty for header %s", lesson.getHeaders().get(i)));
                System.out.printf("Difference between input \"%s\" & answer \"%s\" - \"%s\"", userAns, corrAns, StringUtils.difference(userAns, corrAns));
                return false;
            }
        }
        return true;
    }


    private void handleFinishQuiz() {
        finishAfterTransition();
    }

}
