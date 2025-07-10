package com.example.language_alarm.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.models.Flashcard;

import java.util.ArrayList;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {
    private final OnFlashcardEditListener editListener;
    private List<Flashcard> flashcards;
    private List<String> headers;

    public FlashcardAdapter(List<String> headers, List<Flashcard> flashcards, OnFlashcardEditListener listener) {
        this.headers = headers;
        this.flashcards = flashcards;
        this.editListener = listener;
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
        } else if (this.headers == null || this.headers.isEmpty() || !this.headers.equals(newHeaders)) {
            // if headers change, all of them need to change as all have headers
            this.headers = new ArrayList<>(newHeaders);
            notifyDataSetChanged();
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
            if (editListener != null) {
                editListener.onFlashcardEdit(flashcard, position);
            }
        });

//        holder.starButton.setOnClickListener(view -> {
//
//        });
    }

    @Override
    public int getItemCount() {
        if (this.flashcards == null) {
            return 0;
        }
        return this.flashcards.size();
    }

    public interface OnFlashcardEditListener {
        void onFlashcardEdit(Flashcard flashcard, int position);
        // TODO: set something up for the star button
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
