package com.example.language_alarm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;

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

        this.flashcards = new ArrayList<>();
        for (Flashcard flash : flashcards) {
            this.flashcards.add(flash.clone());
        }
        this.editListener = listener;
    }

    public void setFlashcards(List<Flashcard> newFlashcards) {
        if (flashcards == null || flashcards.isEmpty()) {
            flashcards = newFlashcards;
            notifyDataSetChanged();
            return;
        }

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
            public boolean areItemsTheSame(int oldPos, int newPos) {
                Flashcard oldItem = flashcards.get(oldPos);
                Flashcard newItem = newList.get(newPos);
                if (oldItem.originalIndex == -1 || newItem.originalIndex == -1) {
                    return oldPos == newPos;
                }
                return oldItem.originalIndex == newItem.originalIndex;
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Flashcard oldItem = flashcards.get(oldPos);
                Flashcard newItem = newFlashcards.get(newPos);
                return oldItem.equals(newItem);
            }
        };

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        flashcards = newFlashcards;
        diffResult.dispatchUpdatesTo(this);
    }

    public void setHeaders(List<String> newHeaders) {
        if ((newHeaders == null || newHeaders.isEmpty()) && (this.headers == null || !this.headers.isEmpty())) {
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
        holder.menuButton.setOnClickListener(view -> {
            if (editListener != null) {
                editListener.onFlashcardEdit(flashcard, position);
            }
        });

        Button starButton = holder.starButton;
        starButton.setOnClickListener(view -> {
            if (editListener != null) {
                editListener.onFlashcardStar(flashcard, position, view);
                // starButton gets highlighted?
            }
        });

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
        void onFlashcardStar(Flashcard flashcard, int position, View view);
    }

    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        public TextView txtValues;
        // public Button editButton;
        public ImageButton menuButton;
        public Button starButton;

        public FlashcardViewHolder(View itemView) {
            super(itemView);
            txtValues = itemView.findViewById(R.id.txtValues);
            // editButton = itemView.findViewById(R.id.editButton);
            starButton = itemView.findViewById(R.id.starButton);
            menuButton = itemView.findViewById(R.id.menuButton);
        }
    }
}
