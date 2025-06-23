package com.example.language_alarm.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int hour;  // Store hour separately
    private int minute;
    private String ringtone;
    private String wallpaper;
    // cards. possible file to list of flashcards
    private int numberOfSnoozes;
    private int lengthOfSnooze; // in minutes
    private boolean isEnabled = true;
    private boolean isOneTime;
    private String daysOfTheWeek = null;

    public Alarm() {}
    public Alarm(int hour, int minute, int numSnoozes, int lenSnooze,
                 boolean isOneTime, ArrayList<Boolean> days) {
        this.hour = hour;
        this.minute = minute;
        this.numberOfSnoozes = numSnoozes;
        this.lengthOfSnooze = lenSnooze;
        this.isOneTime = isOneTime;
        setDaysOfWeek(days);
    }

    public String getDaysOfTheWeek() {
        return this.daysOfTheWeek;
    }

    public List<Boolean> getParsedDaysOfTheWeek() {
        if (daysOfTheWeek == null) return new ArrayList<>();
        return new Gson().fromJson(daysOfTheWeek, new TypeToken<List<Boolean>>(){}.getType());
    }

    public void setDaysOfTheWeek(String daysOfTheWeek) {
        this.daysOfTheWeek = daysOfTheWeek;
    }

    public void setDaysOfWeek(List<Boolean> days) {
        this.daysOfTheWeek = new Gson().toJson(days);
    }

    public Calendar getCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        return cal;
    }

    public void setId(int i) {
        this.id = i;
    }
    public int getId() {
        return this.id;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setStatus(boolean b) {
        this.isEnabled = b;
        // updated the storage when app closes?
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getHour() {
        return this.hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getMinute() {
        return this.minute;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public String getRingtone() {
        if (this.ringtone != null) {
            return this.ringtone;
        }
        return null;
    }

    public String getTime() {
        return String.format(Locale.US, "%02d:%02d", this.getHour(), this.getMinute());
    }

    public void setWallpaper(String wallpaper) {
        this.wallpaper = wallpaper;
    }

    public String getWallpaper() {
        return this.wallpaper;
    }

    public void setOneTime(boolean oneTime) {
        isOneTime = oneTime;
    }

    public boolean isOneTime() {
        return isOneTime;
    }

    public void setLengthOfSnooze(int lengthOfSnooze) {
        this.lengthOfSnooze = lengthOfSnooze;
    }

    public int getLengthOfSnooze() {
        return lengthOfSnooze;
    }

    public void setNumberOfSnoozes(int numberOfSnoozes) {
        this.numberOfSnoozes = numberOfSnoozes;
    }

    public int getNumberOfSnoozes() {
        return numberOfSnoozes;
    }

    public static class Converters {
        @TypeConverter
        public static LocalTime fromString(String val) {
            return val == null ? null : LocalTime.parse(val);
        }

        @TypeConverter
        public static String localTimeToString(LocalTime time) {
            return time == null ? null : time.toString();
        }

        @TypeConverter
        public static String fromDaysOfWeek(List<Boolean> days) {
            return new Gson().toJson(days);
        }

        @TypeConverter
        public static List<Boolean> toDaysOfWeek(String json) {
            if (json == null) return new ArrayList<>();
            return new Gson().fromJson(json, new TypeToken<List<Boolean>>(){}.getType());
        }
    }

}
