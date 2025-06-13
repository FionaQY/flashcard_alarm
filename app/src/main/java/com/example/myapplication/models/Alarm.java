package com.example.myapplication.models;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Alarm {
    private LocalTime time;
    private String ringtone;
    private String wallpaper;
    // cards. possible file to list of flashcards
    private int numberOfSnoozes;
    private int lengthOfSnooze; // in minutes
    private boolean isEnabled;
    private boolean isOneTime;
    private List<Boolean> daysOfTheWeek = null;

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setStatus(boolean b) {
        this.isEnabled = b;
        // updated the storage when app closes?
    }

    public int getHour() {
        if (this.time != null) {
            return this.time.getHour();
        }
        return -1;
    }
    public int getMinute() {
        if (this.time != null) {
            return this.time.getMinute();
        }
        return -1;
    }

    public String getRingtone() {
        if (this.ringtone != null) {
            return this.ringtone;
        }
        return null;
    }

    public String getTime() {
        return this.time.toString();
    }
}
