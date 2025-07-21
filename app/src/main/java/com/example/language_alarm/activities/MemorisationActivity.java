package com.example.language_alarm.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.adapter.InputFlashcardAdapter;
import com.example.language_alarm.models.Flashcard;
import com.example.language_alarm.models.Lesson;
import com.example.language_alarm.utils.LessonHandler;
import com.example.language_alarm.utils.ToolbarHelper;
import com.example.language_alarm.viewmodel.LessonViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;

public class MemorisationActivity extends AppCompatActivity {
    private static final String TAG = "MemorisationActivity";
    SparseArray<SparseArray<SpannableString>> progress = new SparseArray<>();
    CountDownTimer countdownTimer;
    private InputFlashcardAdapter adapter;
    private List<Flashcard> allFlashcards = new ArrayList<>();
    private int currFlashcardIndex = 0;
    private Lesson lesson;
    private Queue<Integer> cardIndexes = new LinkedList<>();
    private boolean secondClick = false;
    private boolean isAlarm = false;

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

    private void startTimer() {
        if (countdownTimer == null) {
            countdownTimer = new CountDownTimer(300000, 1000) {
                public void onTick(long m) {
                }

                public void onFinish() {
                    Toast.makeText(null, "5 minutes have passed", Toast.LENGTH_SHORT).show();
                }
            };
        }
        countdownTimer.cancel();
        countdownTimer.start();
    }

    private void cancelTimer() {
        if (countdownTimer == null) {
            return;
        }
        countdownTimer.cancel();
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
        isAlarm = getIntent().getBooleanExtra("isAlarm", false);
        setupToolbar();
        setupViews(lessonId, qnCount);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ToolbarHelper.setupToolbar(toolbar, isAlarm ? "" : "Practice Mode", !isAlarm, this::finish);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void setupViews(int lessonId, int qnCount) {
        RecyclerView recyclerView = findViewById(R.id.values_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InputFlashcardAdapter(recyclerView);
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

        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            if (secondClick) {
                nextButton.setText(R.string.check_answer);
                handleNextCard();
            } else {
                cancelTimer();
                nextButton.setText(R.string.next_flashcard);

                validateAnswers();
            }
            secondClick = !secondClick;
        });
        findViewById(R.id.editButton).setOnClickListener(v -> handleEditCard());
    }

    private void handleEditCard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_lesson, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.values_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        InputFlashcardAdapter inputAdapter = new InputFlashcardAdapter(true, recyclerView);
        recyclerView.setAdapter(inputAdapter);
        inputAdapter.setLesson(this.lesson);
        Flashcard currFlashcard = this.lesson.getFlashcards().get(currFlashcardIndex);
        inputAdapter.setValues(currFlashcard);

        MaterialButton btnCancel = dialogView.findViewById(R.id.cancelButton);
        MaterialButton btnSave = dialogView.findViewById(R.id.saveButton);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> Objects.requireNonNull(dialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM));

        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            List<String> newVals = inputAdapter.getUserAnswers();
            currFlashcard.setVals(newVals);
            lesson.getFlashcards().set(currFlashcardIndex, currFlashcard);
            if (!this.cardIndexes.contains(currFlashcardIndex)) {
                this.cardIndexes.add(currFlashcardIndex);
                this.progress.delete(currFlashcardIndex);
            }
//            adapter.setEditedValues(currFlashcard, this.progress.get(currFlashcardIndex));
            secondClick = false;
            dialog.dismiss();
            handleNextCard();
        });
    }

    private void handleNextCard() {
        if (cardIndexes.isEmpty()) {
            handleFinishQuiz();
            return;
        }
        if (isAlarm) {
            startTimer();
        }

        Integer nextInd = cardIndexes.poll();
        if (nextInd == null) {
            throw new IllegalStateException("Card Index picked is null");
        }
        this.currFlashcardIndex = nextInd;
        Flashcard nextCard = allFlashcards.get(nextInd);
        if (nextCard == null) {
            Log.e(TAG, String.format("Flashcard at index %d is null", nextInd));
            handleNextCard();
            return;
        }
        adapter.setValues(nextCard, this.progress.get(nextInd));
    }

    private void validateAnswers() {
        List<String> userInput = adapter.getUserAnswers();
        if (currFlashcardIndex >= allFlashcards.size() || currFlashcardIndex < 0) {
            handleFinishQuiz();
            return;
        }
        SparseArray<SpannableString> stringy = getWrongAnswers(userInput);
        adapter.showAnswers(stringy);
        if (stringy.size() == 0) {
            Toast.makeText(this, "You got this correct!", Toast.LENGTH_LONG).show();
        } else {
            progress.put(currFlashcardIndex, stringy);
            this.cardIndexes.add(currFlashcardIndex);
            Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show();
        }
    }

    private SparseArray<SpannableString> getWrongAnswers(List<String> userInput) {
        SparseArray<SpannableString> output = new SparseArray<>();
        if (lesson == null || lesson.getFlashcards() == null || this.currFlashcardIndex >= lesson.getFlashcards().size()) {
            return output;
        }
        List<String> values = lesson.getFlashcards().get(currFlashcardIndex).getVals();
        if (userInput == null || lesson.getFlashcards() == null || userInput.size() != values.size()) {
            return output;
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
                    output.put(i, spannableString);
                }
            }
        }
        return output;
    }

    private NormalizedResult normalize(String input) {
        NormalizedResult res = new NormalizedResult();
        StringBuilder sb = new StringBuilder();
        res.originalIndices = new ArrayList<>();

        if (input == null) return res;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            boolean isPunctuation = !lesson.isPunctSensitive() && Character.toString(ch).matches("\\p{Punct}");
            boolean isWhitespace = Character.isWhitespace(ch);
            if (isWhitespace || isPunctuation) continue;

            char normalizedChar = lesson.isCaseSensitive() ? ch : Character.toUpperCase(ch);
            sb.append(normalizedChar);
            res.originalIndices.add(i);
        }
        res.normalized = sb.toString();
        return res;
    }

    private void handleFinishQuiz() {
        if (this.lesson != null) {
            this.lesson.setFlashcards(this.allFlashcards);
            LessonHandler.saveLesson(this, this.lesson);
        }
        cancelTimer();
        finishAfterTransition();
    }

    @Override
    public void finish() {
        super.finish();
        LessonHandler.saveLesson(this, lesson);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LessonHandler.saveLesson(this, lesson);
    }

    private static class NormalizedResult {
        String normalized = "";
        List<Integer> originalIndices;
    }

}
