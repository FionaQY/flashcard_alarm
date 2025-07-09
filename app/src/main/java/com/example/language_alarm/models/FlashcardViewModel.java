package com.example.language_alarm.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class FlashcardViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Flashcard>> flashcards;

    public FlashcardViewModel(@NonNull Application application) {
        super(application);
        flashcards = new MutableLiveData<>();
    }

    public LiveData<List<Flashcard>> getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards.setValue(flashcards);
    }

}
