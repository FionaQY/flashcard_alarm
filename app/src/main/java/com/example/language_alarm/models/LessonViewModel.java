package com.example.language_alarm.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.language_alarm.database.LessonDao;
import com.example.language_alarm.database.LessonDatabase;

import java.util.List;

public class LessonViewModel extends AndroidViewModel {
    private final LiveData<List<Lesson>> allLessons;

    public LessonViewModel(@NonNull Application application) {
        super(application);
        LessonDao lessonDao = LessonDatabase.getDatabase(application).lessonDao();
        allLessons = lessonDao.getAllLessons();
    }

    public LiveData<List<Lesson>> getAllLessons() {
        return this.allLessons;
    }
}
