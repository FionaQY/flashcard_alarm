package com.example.language_alarm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.language_alarm.database.AlarmDao;
import com.example.language_alarm.database.AlarmDatabase;
import com.example.language_alarm.models.Alarm;

import java.util.List;

public class AlarmViewModel extends AndroidViewModel {
    private final LiveData<List<Alarm>> allAlarms;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        AlarmDao alarmDao = AlarmDatabase.getDatabase(application).alarmDao();
        allAlarms = alarmDao.getAllAlarms();
    }

    public LiveData<List<Alarm>> getAllAlarms() {
        return this.allAlarms;
    }
}
