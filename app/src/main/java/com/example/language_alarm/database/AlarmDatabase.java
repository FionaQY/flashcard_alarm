package com.example.language_alarm.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.language_alarm.models.Alarm;

@Database(entities = {Alarm.class}, version = 1)
public abstract class AlarmDatabase extends RoomDatabase {
    private static volatile AlarmDatabase INSTANCE;

    public static AlarmDatabase getDatabase(final Context ctx) {
        if (INSTANCE == null) {
            synchronized (AlarmDatabase.class) {
                if (INSTANCE == null) {
                    String databaseName = "alarm_database";
                    INSTANCE = Room.databaseBuilder(
                                    ctx.getApplicationContext(),
                                    AlarmDatabase.class,
                                    databaseName
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract AlarmDao alarmDao();
}
