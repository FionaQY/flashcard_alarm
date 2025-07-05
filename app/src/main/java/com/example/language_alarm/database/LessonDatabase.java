package com.example.language_alarm.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.language_alarm.models.Lesson;

@Database(entities = {Lesson.class}, version = 1)
@TypeConverters({Lesson.Converters.class})
public abstract class LessonDatabase extends RoomDatabase {
    private static volatile LessonDatabase INSTANCE;

    public static LessonDatabase getDatabase(final Context ctx) {
        if (INSTANCE == null) {
            synchronized (LessonDatabase.class) {
                if (INSTANCE == null) {
                    String databaseName = "lesson_database";
                    INSTANCE = Room.databaseBuilder(
                                    ctx.getApplicationContext(),
                                    LessonDatabase.class,
                                    databaseName
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract LessonDao lessonDao();
}
