package com.example.language_alarm.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@TypeConverters(Lesson.Converters.class)
@Entity(tableName = "lessons")
public class Lesson implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String lessonName;
    @TypeConverters(Converters.class)
    private List<Flashcard> flashcards = null;
    public Lesson() {}

    public Lesson(String name, List<Flashcard> les) {
        this.lessonName = name;
        this.flashcards = les;
    }

    public static final Creator<Lesson> CREATOR = new Creator<>() {
        @Override
        public Lesson createFromParcel(Parcel in) {
            return new Lesson(in);
        }

        @Override
        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };

    public Lesson(Parcel in) {
        id = in.readInt();
        lessonName = in.readString();
        flashcards = in.createTypedArrayList(Flashcard.CREATOR);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLessonName() {
        return this.lessonName;
    }

    public void setLessonName(String name) {
        this.lessonName = name;
    }

    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    public List<Flashcard> getFlashcards() {
        return this.flashcards;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(lessonName);
        dest.writeTypedList(flashcards);
    }

    public static class Converters {
        @TypeConverter
        public static String fromFlashcardList(List<Flashcard> cards) {
            Gson gson = new Gson();
            return gson.toJson(cards);
        }

        @TypeConverter
        public static ArrayList<Flashcard> toFlashcardList(String data) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Flashcard>>() {}.getType();
            return gson.fromJson(data, listType);
        }
    }
}
