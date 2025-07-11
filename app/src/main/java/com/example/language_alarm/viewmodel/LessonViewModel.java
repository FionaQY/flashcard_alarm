package com.example.language_alarm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.language_alarm.database.LessonDao;
import com.example.language_alarm.database.LessonDatabase;
import com.example.language_alarm.models.Lesson;

import java.util.List;

public class LessonViewModel extends AndroidViewModel {
    private final LessonDao lessonDao;
    private LiveData<List<Lesson>> allLessons;

    public LessonViewModel(@NonNull Application application) {
        super(application);
        lessonDao = LessonDatabase.getDatabase(application).lessonDao();
    }

    public LiveData<List<Lesson>> getAllLessons() {
        if (this.allLessons == null) this.allLessons = lessonDao.getAllLessons();
        return this.allLessons;
    }

    public LiveData<Lesson> getLesson(int id) {
        return lessonDao.getLessonById(id);
    }
}
