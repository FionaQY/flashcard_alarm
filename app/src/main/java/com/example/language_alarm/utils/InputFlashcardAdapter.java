package com.example.language_alarm.utils;

import android.text.Editable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Flashcard;
import com.example.language_alarm.models.Lesson;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class InputFlashcardAdapter extends RecyclerView.Adapter<InputFlashcardAdapter.InputFlashcardViewHolder> {
    private final List<TextInputEditText> inputFields = new ArrayList<>();
    private final SparseArray<LinearLayout> containers;
    private final SparseArray<TextView> answerViews;
    private List<Boolean> foreignIndexes = new ArrayList<>();
    private List<String> headers = new ArrayList<>();
    private List<String> ans = new ArrayList<>();

    public InputFlashcardAdapter() {
        containers = new SparseArray<>();
        answerViews = new SparseArray<>();
    }

    public void setLesson(Lesson lesson) { // one time per lessons
        if (lesson == null || lesson.getHeaders() == null) {
            this.headers = new ArrayList<>();
        } else if (this.headers == null || this.headers.isEmpty()) {
            this.headers = new ArrayList<>(lesson.getHeaders());
        } else {
            this.headers = lesson.getHeaders();
        }

        if (lesson == null || lesson.getHeaders() == null) {
            this.foreignIndexes = new ArrayList<>();
        } else if (this.foreignIndexes == null || this.foreignIndexes.isEmpty()) {
            this.foreignIndexes = new ArrayList<>(lesson.getForeignIndexes());
        } else {
            this.foreignIndexes = lesson.getForeignIndexes();
        }

        notifyDataSetChanged();
    }

    public void setValues(Flashcard flash) {
        this.inputFields.clear();
        if (flash == null || flash.getVals() == null) {
            this.ans = new ArrayList<>();
        } else {
            this.ans = flash.getVals();
        }
        notifyDataSetChanged();
    }

    public List<String> getUserAnswers() {
        List<String> answers = new ArrayList<>();
        for (TextInputEditText input : inputFields) {
            Editable txt = input.getText();
            answers.add(txt == null ? "" : txt.toString());
        }
        return answers;
    }

    public void showAnswers() {
        for (int i = 0; i < this.headers.size(); i++) {
            String valueName = this.headers.get(i);
            String valueAns = this.ans.get(i);
            TextView ansView = this.answerViews.get(i);
            if (ansView == null) {
                continue;
            }

            boolean isNoShow = noInputExpected(valueName, valueAns);
            ansView.setVisibility(isNoShow ? View.GONE : View.VISIBLE);
        }
    }

    private boolean noInputExpected(String valueName, String valueAns) {
        if (valueName == null || valueName.trim().isEmpty()) {
            return true;
        }
        return valueAns == null || valueAns.trim().isEmpty();
    }

    @NonNull
    @Override
    public InputFlashcardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.flashcard_input_item, parent, false);
        return new InputFlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InputFlashcardViewHolder holder, int position) {
        String valueName = this.headers.get(position);
        String valueAns = this.ans.get(position);
        holder.valueName.setText(String.format("%s: ", valueName));
        holder.displayAnswer.setText(String.format("Answer: %s", valueAns));
        if (!this.foreignIndexes.get(position)) {
            holder.inputValue.setText(valueAns);
            holder.inputValue.setEnabled(false);
        } else {
            holder.inputValue.setText("");
            holder.inputValue.setEnabled(true);
        }
        this.inputFields.add(holder.inputValue);
        this.answerViews.put(position, holder.displayAnswer);
        this.containers.put(position, holder.inputContainer);

        boolean isNoShow = noInputExpected(valueName, valueAns);
        holder.inputContainer.setVisibility(isNoShow ? View.GONE : View.VISIBLE);
        holder.displayAnswer.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (this.headers == null) {
            return 0;
        }
        return this.headers.size();
    }

    public static class InputFlashcardViewHolder extends RecyclerView.ViewHolder {
        public TextView valueName;
        public TextInputEditText inputValue;
        public LinearLayout inputContainer;
        public TextView displayAnswer;

        public InputFlashcardViewHolder(View itemView) {
            super(itemView);
            inputContainer = itemView.findViewById(R.id.inputContainer);
            valueName = itemView.findViewById(R.id.valueName);
            inputValue = itemView.findViewById(R.id.inputValue);
            displayAnswer = itemView.findViewById(R.id.answer);
        }
    }
}
