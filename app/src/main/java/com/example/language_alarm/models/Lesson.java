package com.example.language_alarm.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.ArrayList;

@Entity(tableName = "lessons")
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private ArrayList<Integer> flashcards = null;
    public Lesson() {}

    public Lesson(ArrayList<Integer> les) {
        this.flashcards = new ArrayList<>(les);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }


}
