package com.example.language_alarm.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.HashMap;
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

    @NonNull
    private static SpannableString getSpannableString(NormalizedResult corrNorm, int diffIndex, String corrAns) {
        int originalStart = corrNorm.originalIndices.get(diffIndex);
        int originalEnd = corrAns.length();

        SpannableString spannableString = new SpannableString(corrAns);
        BackgroundColorSpan highlightSpan = new BackgroundColorSpan(Color.RED);
        spannableString.setSpan(highlightSpan, originalStart, originalEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableString;
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
        HashMap<Integer, SpannableString> stringy = this.isCorrect(userInput);
        if (stringy.isEmpty()) {
            Toast.makeText(this, "You got this correct!", Toast.LENGTH_LONG).show();
        } else {
            adapter.showAnswers(stringy);
            this.cardIndexes.add(currFlashcardIndex);
            Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show();
        }

        secondClick = true;
    }

    private HashMap<Integer, SpannableString> isCorrect(List<String> userInput) {
        HashMap<Integer, SpannableString> outp = new HashMap<>();
        if (lesson == null || lesson.getFlashcards() == null || this.currFlashcardIndex >= lesson.getFlashcards().size()) {
            return outp;
        }
        List<String> values = lesson.getFlashcards().get(currFlashcardIndex).getVals();
        if (userInput == null || lesson.getFlashcards() == null || userInput.size() != values.size()) {
            return outp;
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
                userAns = "";
            }
            NormalizedResult corrNorm = normalize(corrAns);
            NormalizedResult userNorm = normalize(userAns);

            if (!corrNorm.normalized.equals(userNorm.normalized)) {
                int diffIndex = StringUtils.indexOfDifference(userNorm.normalized, corrNorm.normalized);

                if (diffIndex >= 0 && diffIndex < corrNorm.originalIndices.size()) {
                    SpannableString spannableString = getSpannableString(corrNorm, diffIndex, corrAns);
                    outp.put(i, spannableString);
                }
            }
        }
        return outp;
    }

    private NormalizedResult normalize(String input) {
        NormalizedResult res = new NormalizedResult();
        res.normalized = "";
        StringBuilder sb = new StringBuilder();
        res.originalIndices = new ArrayList<>();

        if (input == null) return res;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            boolean isPunct = !lesson.isPunctSensitive() && Character.toString(ch).matches("\\p{Punct}");
            boolean isWhitespace = Character.isWhitespace(ch);
            if (isWhitespace || isPunct) continue;

            char normalizedChar = lesson.isCaseSensitive() ? ch : Character.toLowerCase(ch);
            sb.append(normalizedChar);
            res.originalIndices.add(i);
        }
        res.normalized = sb.toString();
        return res;
    }

    private void handleFinishQuiz() {
        finishAfterTransition();
    }

    private static class NormalizedResult {
        String normalized;
        List<Integer> originalIndices;
    }


}
