package com.example.language_alarm.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Locale;
import java.util.Objects;

@Entity(tableName = "alarms")
public class Alarm implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int hour;  // Store hour separately
    private int minute;
    private String ringtone;
    private String wallpaper;
    private int numberOfSnoozes;
    private int lengthOfSnooze; // in minutes
    private boolean isEnabled = true;
    private boolean isOneTime;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;
    private int lessonId;

    public Alarm() {}

    public Alarm(int hour, int minute, int numSnoozes, int lenSnooze,
                 boolean isOneTime, boolean monday, boolean tuesday, boolean wednesday,
                 boolean thursday, boolean friday, boolean saturday, boolean sunday, String uri) {
        this.hour = hour;
        this.minute = minute;
        this.numberOfSnoozes = numSnoozes;
        this.lengthOfSnooze = lenSnooze;
        this.isOneTime = isOneTime;
        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.ringtone = uri;
    }

    protected Alarm(Parcel in) {
        id = in.readInt();
        hour = in.readInt();
        minute = in.readInt();
        ringtone = in.readString();
        wallpaper = in.readString();
        numberOfSnoozes = in.readInt();
        lengthOfSnooze = in.readInt();
        isEnabled = in.readByte() != 0;
        isOneTime = in.readByte() != 0;
        monday = in.readByte() != 0;
        tuesday = in.readByte() != 0;
        wednesday = in.readByte() != 0;
        thursday = in.readByte() != 0;
        friday = in.readByte() != 0;
        saturday = in.readByte() != 0;
        sunday = in.readByte() != 0;
        lessonId = in.readInt();
    }

    public static final Creator<Alarm> CREATOR = new Creator<>() {
        @Override
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        @Override
        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

    // Getters and setters for all fields
    public boolean isMonday() { return monday; }
    public void setMonday(boolean monday) { this.monday = monday; }

    public boolean isTuesday() { return tuesday; }
    public void setTuesday(boolean tuesday) { this.tuesday = tuesday; }

    public boolean isWednesday() { return wednesday; }
    public void setWednesday(boolean wednesday) { this.wednesday = wednesday; }

    public boolean isThursday() { return thursday; }
    public void setThursday(boolean thursday) { this.thursday = thursday; }

    public boolean isFriday() { return friday; }
    public void setFriday(boolean friday) { this.friday = friday; }

    public boolean isSaturday() { return saturday; }
    public void setSaturday(boolean saturday) { this.saturday = saturday; }

    public boolean isSunday() { return sunday; }
    public void setSunday(boolean sunday) { this.sunday = sunday; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }

    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }

    public String getRingtone() { return ringtone; }
    public void setRingtone(String ringtone) { this.ringtone = ringtone; }

    public String getWallpaper() { return wallpaper; }
    public void setWallpaper(String wallpaper) { this.wallpaper = wallpaper; }

    public int getNumberOfSnoozes() { return numberOfSnoozes; }
    public void setNumberOfSnoozes(int numberOfSnoozes) { this.numberOfSnoozes = numberOfSnoozes; }

    public int getLengthOfSnooze() { return lengthOfSnooze; }
    public void setLengthOfSnooze(int lengthOfSnooze) { this.lengthOfSnooze = lengthOfSnooze; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public boolean isOneTime() { return isOneTime; }
    public void setOneTime(boolean oneTime) { isOneTime = oneTime; }

    public String getTime() {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    public int getLessonId() {
        return this.lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public String getDescription() {
        if (isOneTime) return "One time alarm";
        StringBuilder sb = new StringBuilder();
        if (sunday) sb.append("Sun ");
        if (monday) sb.append("Mon ");
        if (tuesday) sb.append("Tue ");
        if (wednesday) sb.append("Wed ");
        if (thursday) sb.append("Thu ");
        if (friday) sb.append("Fri ");
        if (saturday) sb.append("Sat ");
        if (this.lessonId != 0) sb.append("Lesson not yet set");
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Alarm otherAlarm = (Alarm) obj;
        return hour == otherAlarm.getHour() &&
                minute == otherAlarm.getMinute() &&
                (Objects.equals(ringtone, otherAlarm.getRingtone())) &&
                (Objects.equals(wallpaper, otherAlarm.getWallpaper())) &&
                numberOfSnoozes == otherAlarm.getNumberOfSnoozes() &&
                lengthOfSnooze == otherAlarm.getLengthOfSnooze() &&
                isEnabled == otherAlarm.isEnabled() &&
                isOneTime == otherAlarm.isOneTime() &&
                monday == otherAlarm.isMonday() &&
                tuesday == otherAlarm.isTuesday() &&
                wednesday == otherAlarm.isWednesday() &&
                thursday == otherAlarm.isThursday() &&
                friday == otherAlarm.isFriday() &&
                saturday == otherAlarm.isSaturday() &&
                sunday == otherAlarm.isSunday() &&
                lessonId == otherAlarm.getLessonId();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(hour);
        dest.writeInt(minute);
        dest.writeString(ringtone);
        dest.writeString(wallpaper);
        dest.writeInt(numberOfSnoozes);
        dest.writeInt(lengthOfSnooze);
        dest.writeByte((byte) (isEnabled ? 1 : 0));
        dest.writeByte((byte) (isOneTime ? 1 : 0));
        dest.writeByte((byte) (monday ? 1 : 0));
        dest.writeByte((byte) (tuesday ? 1 : 0));
        dest.writeByte((byte) (wednesday ? 1 : 0));
        dest.writeByte((byte) (thursday ? 1 : 0));
        dest.writeByte((byte) (friday ? 1 : 0));
        dest.writeByte((byte) (saturday ? 1 : 0));
        dest.writeByte((byte) (sunday ? 1 : 0));
        dest.writeInt(lessonId);
    }



}
