package com.example.language_alarm.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.language_alarm.models.Flashcard;

import java.util.ArrayList;
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
        return this.flashcards;
    }

    public void setFlashcards(List<Flashcard> flashcards) {
        List<Flashcard> newCards = new ArrayList<>();
        for (Flashcard flash : flashcards) {
            newCards.add(flash.clone());
        }
        Log.d("ViewModel", "Setting new flashcards list: " + flashcards.hashCode());
        this.flashcards.setValue(newCards);
    }

    public LiveData<List<String>> getHeaders() {
        return this.headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers.setValue(headers);
    }
}
