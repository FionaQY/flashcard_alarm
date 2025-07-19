package com.example.language_alarm.adapter;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
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
import java.util.Objects;

public class InputFlashcardAdapter extends RecyclerView.Adapter<InputFlashcardAdapter.InputFlashcardViewHolder> {
    private final SparseArray<TextInputEditText> inputFields;
    private final SparseArray<TextView> answerViews;
    private final RecyclerView rv;
    private List<Boolean> foreignIndexes = new ArrayList<>();
    private List<String> headers = new ArrayList<>();
    private List<String> ans = new ArrayList<>();
    private boolean isNotMemo = false;
    private SparseArray<SpannableString> progress;

    public InputFlashcardAdapter(RecyclerView recyclerView) {
        this.rv = recyclerView;
        this.answerViews = new SparseArray<>();
        this.inputFields = new SparseArray<>();
    }

    public InputFlashcardAdapter(boolean isNotMemo, RecyclerView recyclerView) {
        this(recyclerView);
        this.isNotMemo = isNotMemo;
    }

    public void setLesson(Lesson lesson) { // one time per lessons
        this.headers = lesson != null && lesson.getHeaders() != null
                ? new ArrayList<>(lesson.getHeaders()) : new ArrayList<>();

        this.foreignIndexes = lesson != null && lesson.getForeignIndexes() != null
                ? new ArrayList<>(lesson.getForeignIndexes()) : new ArrayList<>();

        notifyDataSetChanged();
    }

    public void setValues(Flashcard flash, SparseArray<SpannableString> pro) {
        this.progress = pro;
        this.setValues(flash);
    }

    public void setValues(Flashcard flash) {
        this.inputFields.clear();
        this.answerViews.clear();
        if (flash == null || flash.getVals() == null) {
            this.ans = new ArrayList<>();
        } else {
            this.ans = new ArrayList<>(flash.getVals());
        }
        if (this.ans.size() < this.headers.size()) {
            for (int i = this.ans.size(); i < this.headers.size(); i++) {
                this.ans.add("");
            }
        }
        notifyDataSetChanged();
    }

    public List<String> getUserAnswers() {
        List<String> answers = new ArrayList<>();
        for (int i = 0; i < inputFields.size(); i++) {
            Editable txt = inputFields.get(i) != null ? inputFields.get(i).getText() : null;
            answers.add(txt == null ? "" : txt.toString());
        }
        return answers;
    }

    public void showAnswers(SparseArray<SpannableString> strings) {
        for (int i = 0; i < this.headers.size(); i++) {
            String valueName = this.headers.get(i);
            String valueAns = i < this.ans.size() ? this.ans.get(i) : "";
            TextView ansView = this.answerViews.get(i);
            if (ansView == null) {
                continue;
            }

            boolean isNoShow = noInputExpected(valueName, valueAns);
            ansView.setVisibility(isNoShow ? View.GONE : View.VISIBLE);

            SpannableString ansText = strings.get(i);
            if (ansText != null) {
                ansView.setText(ansText);
            }
        }
        this.progress = null;
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
        if (position >= this.ans.size()) {
            return;
        }

        String valueAns = this.ans.get(position);
        holder.valueName.setText(String.format("%s: ", valueName));

        TextView ansView = holder.displayAnswer;
        TextInputEditText input = holder.inputValue;

        ansView.setText(String.format("Answer: %s", valueAns));
        ansView.setVisibility(View.GONE);

        this.inputFields.put(position, input);
        this.answerViews.put(position, ansView);

        input.setText("");
        input.setEnabled(true);
        if (isNotMemo) { // if editing, show all
            input.setText(valueAns);
            return;
        }

        if ((this.progress != null && this.progress.get(position) == null)
                || !this.foreignIndexes.get(position)) {
            input.setText(valueAns);
            input.setEnabled(false);
        }

        boolean isNoShow = noInputExpected(valueName, valueAns);
        holder.inputContainer.setVisibility(isNoShow ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        if (this.headers == null) {
            return 0;
        }
        return this.headers.size();
    }

    public class InputFlashcardViewHolder extends RecyclerView.ViewHolder {
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

            inputValue.addTextChangedListener(new TextWatcher() {
                private int previousLength = 0;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    previousLength = s.length();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > previousLength) {
                        char lastChar = s.charAt(s.length() - 1);
                        if (lastChar == '\n') {
                            inputValue.setText(s.toString().replace("\n", ""));
                            goToNextInput();
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                private void goToNextInput() {
                    int nextPos = getAbsoluteAdapterPosition() + 1;
                    while (nextPos < getItemCount()) {
                        RecyclerView.ViewHolder temp = rv.findViewHolderForAdapterPosition(nextPos);
                        if (temp instanceof InputFlashcardViewHolder) {
                            InputFlashcardViewHolder nextHolder = (InputFlashcardViewHolder) temp;
                            if (nextHolder.inputContainer.getVisibility() == View.VISIBLE) break;
                        }
                        nextPos++;
                    }
                    if (nextPos >= getItemCount()) return;

                    rv.smoothScrollToPosition(nextPos);
                    int finalNextPos = nextPos;
                    rv.postDelayed(() -> {
                        RecyclerView.ViewHolder nextHolder = rv.findViewHolderForAdapterPosition(finalNextPos);
                        if (nextHolder instanceof InputFlashcardViewHolder) {
                            TextInputEditText textbox = ((InputFlashcardViewHolder) nextHolder).inputValue;
                            textbox.requestFocus();
                            textbox.setSelection(Objects.requireNonNull(textbox.getText()).length());
                        }
                    }, 200);
                }
            });


        }
    }
}
