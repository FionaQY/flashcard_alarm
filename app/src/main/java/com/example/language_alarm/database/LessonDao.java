package com.example.language_alarm.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.language_alarm.models.Lesson;

import java.util.List;

@Dao
public interface LessonDao {
    @Insert
    long insert(Lesson lesson);

    @Update
    void update(Lesson lesson);

    @Delete
    void delete(Lesson lesson);

    @Query("SELECT * FROM lessons ORDER BY id")
    LiveData<List<Lesson>> getAllLessons();

    @Query("SELECT * FROM lessons WHERE id = :id")
    LiveData<Lesson> getLessonById(int id);

}
