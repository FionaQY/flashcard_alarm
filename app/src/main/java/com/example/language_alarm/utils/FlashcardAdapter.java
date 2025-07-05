package com.example.language_alarm.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.activities.NewLessonActivity;
import com.example.language_alarm.models.Flashcard;

import java.util.ArrayList;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {
    private final Context ctx;
    private List<Flashcard> flashcards;

    public FlashcardAdapter(Context ctx, List<Flashcard> flashcards) {
        this.ctx = ctx;
        this.flashcards = flashcards;
    }

    public void setFlashcards(List<Flashcard> newFlashcards) {
        if (newFlashcards == null || newFlashcards.isEmpty()) {
            this.flashcards = new ArrayList<>();
            notifyDataSetChanged();
        } else if (flashcards == null || flashcards.isEmpty()) {
            this.flashcards = new ArrayList<>(newFlashcards);
            notifyDataSetChanged();
        } else {
            DiffUtil.Callback diffCallback = new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return flashcards.size();
                }

                @Override
                public int getNewListSize() {
                    return newFlashcards.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return flashcards.get(oldItemPosition) == newFlashcards.get(newItemPosition);
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return flashcards.get(oldItemPosition).equals(newFlashcards.get(newItemPosition));
                }
            };
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            flashcards.clear();
            flashcards.addAll(newFlashcards);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lesson_item, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlashcardViewHolder holder, int position) {
        Flashcard flashcard = this.flashcards.get(position);
        holder.lessonNameView.setText(flashcard.getValsString());

        holder.editButton.setOnClickListener(view -> {
            Intent intent = new Intent(ctx, NewLessonActivity.class);
            intent.putExtra("flashcard", flashcard);
            ctx.startActivity(intent);
        });

//        holder.deleteButton.setOnClickListener(view -> {
//            intent.putExtra("flashcard", flashcard);
//            ctx.startActivity(intent);
//        });
    }

    @Override
    public int getItemCount() {
        if (this.flashcards == null) {
            return 0;
        }
        return this.flashcards.size();
    }

    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        public TextView lessonNameView;
        public Button editButton;
        public Button deleteButton;

        public FlashcardViewHolder(View itemView) {
            super(itemView);
            lessonNameView = itemView.findViewById(R.id.lesson_name);
            editButton = itemView.findViewById(R.id.edit_lesson);
            deleteButton = itemView.findViewById(R.id.delete_lesson);
        }
    }
}
