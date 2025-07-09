package com.example.language_alarm.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class FlashcardViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Flashcard>> flashcards;
    private final MutableLiveData<List<String>> headers;

    public FlashcardViewModel(@NonNull Application application) {
        super(application);
        flashcards = new MutableLiveData<>();
        headers = new MutableLiveData<>();
    }

    public LiveData<List<Flashcard>> getFlashcards() {
        return flashcards;
    }

    public LiveData<List<String>> getHeaders() {
        return headers;
    }

    public void setFlashcards(List<Flashcard> flashcards, List<String> headers) {
        this.flashcards.setValue(flashcards);
        this.headers.setValue(headers);
    }

}
