package com.example.language_alarm.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

@Entity(tableName = "alarms")
public class Alarm implements Parcelable {
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
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int hour;
    private int minute;
    private String ringtone;
    private String wallpaper;
    private int snoozeNum;
    private int snoozeDuration; // in minutes
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
    private int qnNum;

    public Alarm() {
    }

    public Alarm(int hour, int minute, int numSnoozes, int lenSnooze,
                 boolean isOneTime, boolean sunday, boolean monday, boolean tuesday, boolean wednesday,
                 boolean thursday, boolean friday, boolean saturday, String uri, int qnNum) {
        this.hour = hour;
        this.minute = minute;
        this.snoozeNum = numSnoozes;
        this.snoozeDuration = lenSnooze;
        this.isOneTime = isOneTime;
        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.ringtone = uri;
        this.qnNum = qnNum;
    }

    protected Alarm(Parcel in) {
        id = in.readInt();
        hour = in.readInt();
        minute = in.readInt();
        ringtone = in.readString();
        wallpaper = in.readString();
        snoozeNum = in.readInt();
        snoozeDuration = in.readInt();
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
        qnNum = in.readInt();
    }

    // Getters and setters for all fields
    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public String getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(String wallpaper) {
        this.wallpaper = wallpaper;
    }

    public int getSnoozeNum() {
        return snoozeNum;
    }

    public void setSnoozeNum(int snoozeNum) {
        this.snoozeNum = snoozeNum;
    }

    public int getSnoozeDuration() {
        return snoozeDuration;
    }

    public void setSnoozeDuration(int snoozeDuration) {
        this.snoozeDuration = snoozeDuration;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isOneTime() {
        return isOneTime;
    }

    public void setOneTime(boolean oneTime) {
        isOneTime = oneTime;
    }

    public String getTime() {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    public int getLessonId() {
        return this.lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public void deleteLesson() {
        this.lessonId = 0;
    }

    public int getQnNum() {
        return this.qnNum;
    }

    public void setQnNum(int qnNum) {
        this.qnNum = qnNum;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();

        boolean[] days = {sunday, monday, tuesday, wednesday, thursday, friday, saturday};
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                sb.append(dayNames[i]).append(" ");
            }
        }

        if (isOneTime) {
            sb.insert(0, sb.length() > 0 ? "One time alarm: " : "One time alarm");
        }

        if (lessonId == 0) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("Lesson not yet set");
        }
        return sb.toString().trim();
    }

    public String getLogDesc() {
        return String.format("Alarm %s (ID: %d)", this.getTime(), this.getId());
    }


    public boolean hasDaysSelected() {
        return this.isSaturday() || this.isFriday() || this.isThursday() || this.isWednesday()
                || this.isTuesday() || this.isMonday() || this.isSunday();
    }

    public void forEachEnabledDay(DayConsumer consumer) {
        if (isSunday()) consumer.accept(Calendar.SUNDAY);
        if (isMonday()) consumer.accept(Calendar.MONDAY);
        if (isTuesday()) consumer.accept(Calendar.TUESDAY);
        if (isWednesday()) consumer.accept(Calendar.WEDNESDAY);
        if (isThursday()) consumer.accept(Calendar.THURSDAY);
        if (isFriday()) consumer.accept(Calendar.FRIDAY);
        if (isSaturday()) consumer.accept(Calendar.SATURDAY);
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
                snoozeNum == otherAlarm.getSnoozeNum() &&
                snoozeDuration == otherAlarm.getSnoozeDuration() &&
                isEnabled == otherAlarm.isEnabled() &&
                isOneTime == otherAlarm.isOneTime() &&
                monday == otherAlarm.isMonday() &&
                tuesday == otherAlarm.isTuesday() &&
                wednesday == otherAlarm.isWednesday() &&
                thursday == otherAlarm.isThursday() &&
                friday == otherAlarm.isFriday() &&
                saturday == otherAlarm.isSaturday() &&
                sunday == otherAlarm.isSunday() &&
                lessonId == otherAlarm.getLessonId() &&
                qnNum == otherAlarm.getQnNum();
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
        dest.writeInt(snoozeNum);
        dest.writeInt(snoozeDuration);
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
        dest.writeInt(qnNum);
    }

    public interface DayConsumer {
        void accept(int dayOfWeek);
    }
}
