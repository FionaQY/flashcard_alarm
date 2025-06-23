package com.example.language_alarm.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.models.Alarm.Converters;

@Database(entities = {Alarm.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AlarmDatabase extends RoomDatabase{
    public abstract AlarmDao alarmDao();

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
}
