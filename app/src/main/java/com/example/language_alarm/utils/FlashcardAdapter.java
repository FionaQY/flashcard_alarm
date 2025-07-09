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
import java.util.Objects;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {
    private final Context ctx;
    private List<Flashcard> flashcards;
    private List<String> headers;

    public FlashcardAdapter(Context ctx, List<String> headers, List<Flashcard> flashcards) {
        this.ctx = ctx;
        this.headers = headers;
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

    public void setHeaders(List<String> newHeaders) {
        if (newHeaders == null || newHeaders.isEmpty()) {
            this.headers = new ArrayList<>();
            notifyDataSetChanged();
        } else if (this.headers == null || this.headers.isEmpty()) {
            this.headers = new ArrayList<>(newHeaders);
            notifyDataSetChanged();
        } else {
            DiffUtil.Callback diffCallback = new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return headers.size();
                }

                @Override
                public int getNewListSize() {
                    return newHeaders.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return Objects.equals(headers.get(oldItemPosition), newHeaders.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return headers.get(oldItemPosition).equals(newHeaders.get(newItemPosition));
                }
            };
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            headers.clear();
            headers.addAll(newHeaders);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.flashcard_item, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = this.flashcards.get(position);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.headers.size() && i < flashcard.getVals().size(); i++) {
            String val = flashcard.getVals().get(i);
            String header = this.headers.get(i);
            if (header != null && !header.isEmpty() && val != null && !val.isEmpty()) {
                sb.append(String.format("%s: %s\n", header.trim(), val.trim()));
            }
        }
        holder.txtValues.setText(sb.toString().trim());

        // TODO: set up editing and deleting (3 dots?)
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
        public TextView txtValues;
        public Button editButton;
        public Button starButton;

        public FlashcardViewHolder(View itemView) {
            super(itemView);
            txtValues = itemView.findViewById(R.id.txtValues);
            editButton = itemView.findViewById(R.id.btnEdit);
            starButton = itemView.findViewById(R.id.btnStar);
        }
    }
}
