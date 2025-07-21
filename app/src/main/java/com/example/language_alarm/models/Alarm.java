package com.example.language_alarm.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

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
    // Add these constants at the top of your Alarm class
    private static final int[] WEEKDAYS = {1, 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5, 1 << 6}; // 1, 2, 4, 8, 16, 32, 64
    @Ignore
    private static final String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    @Ignore
    private static final int[] dayInt = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};
    private int daysBitmask = 0;
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
    private int lessonId;
    private int qnNum;

    public Alarm() {
    }

    public Alarm(int hour, int minute, int numSnoozes, int lenSnooze, boolean isOneTime,
                 boolean[] days, String uri, String wallpaper, int qnNum) {
        this.hour = hour;
        this.minute = minute;
        this.snoozeNum = numSnoozes;
        this.snoozeDuration = lenSnooze;
        this.isOneTime = isOneTime;
        for (int i = 0; i < days.length; i++) {
            this.setDayEnabled(WEEKDAYS[i], days[i]);
        }
        this.ringtone = uri;
        this.wallpaper = wallpaper;
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
        daysBitmask = in.readInt();
        lessonId = in.readInt();
        qnNum = in.readInt();
    }

    public boolean[] getEnabledDays() {
        boolean[] outp = new boolean[7];
        for (int i = 0; i < WEEKDAYS.length; i++) {
            outp[i] = this.isDayEnabled(WEEKDAYS[i]);
        }
        return outp;
    }

    public boolean isDayEnabled(int dayFlag) {
        return (daysBitmask & dayFlag) != 0;
    }

    public void setDayEnabled(int dayFlag, boolean enabled) {
        if (enabled) {
            daysBitmask |= dayFlag;
        } else {
            daysBitmask &= ~dayFlag;
        }
    }

    // Getters and setters for all fields
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
        return this.isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isOneTime() {
        return isOneTime;
    }

    public void setOneTime(boolean oneTime) {
        isOneTime = oneTime;
    }

    public int getLessonId() {
        return this.lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public int getQnNum() {
        return this.qnNum;
    }

    public void setQnNum(int qnNum) {
        this.qnNum = qnNum;
    }

    public int getDaysBitmask() {
        return daysBitmask;
    }

    public void setDaysBitmask(int daysBitmask) {
        this.daysBitmask = daysBitmask;
    }

    public void deleteLesson() {
        this.lessonId = 0;
    }


    public String getTime() {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }


    public String getDescription() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < dayNames.length; i++) {
            if (isDayEnabled(1 << i)) {
                sb.append(dayNames[i]).append(" ");
            }
        }

        if (isOneTime) {
            sb.insert(0, sb.length() > 0 ? "One time alarm: " : "One time alarm");
        }

        if (lessonId == 0) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("Lesson not yet set");
        }
        return sb.toString().trim();
    }

    public Calendar getNextAlarmTime() {
        if (!this.isEnabled()) {
            return null;
        }
        Calendar cal = null;
        for (int i = 0; i < dayInt.length; i++) {
            if (isDayEnabled(1 << i)) {
                Calendar temp = getAlarmTimeForDay(dayInt[i]);
                if (cal == null || temp.before(cal)) {
                    cal = temp;
                }
            }
        }

        long now = System.currentTimeMillis();
        if (cal == null) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
            calendar.setTimeInMillis(now);
            calendar.set(Calendar.HOUR_OF_DAY, this.getHour());
            calendar.set(Calendar.MINUTE, this.getMinute());
            calendar.set(Calendar.SECOND, 0);
            if (calendar.getTimeInMillis() <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1); //set next day if time passed alr
            }
            return calendar;
        }
        return cal;
    }

    private Calendar getAlarmTimeForDay(int dayOfWeek) {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, this.getHour());
        calendar.set(Calendar.MINUTE, this.getMinute());
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 7); //set next day if time passed alr
        }
        return calendar;
    }

    public String getLogDesc() {
        return String.format(Locale.US, "Alarm %s (ID: %d)", this.getTime(), this.getId());
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
                daysBitmask == otherAlarm.daysBitmask &&
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
        dest.writeInt(daysBitmask);
        dest.writeInt(lessonId);
        dest.writeInt(qnNum);
    }

}
