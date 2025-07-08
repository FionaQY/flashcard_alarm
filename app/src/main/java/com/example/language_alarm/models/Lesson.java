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
import java.util.Objects;

@TypeConverters(Lesson.Converters.class)
@Entity(tableName = "lessons")
public class Lesson implements Parcelable {
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
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String lessonName;
    private boolean isPunctSensitive = true;
    private boolean isCaseSensitive = true;
    private List<String> headers = new ArrayList<>();
    private List<Boolean> foreignIndexes = new ArrayList<>();
    // TODO: set hint
    @TypeConverters(Converters.class)
    private List<Flashcard> flashcards = null;

    public Lesson() {
    }

    public Lesson(String name, List<Flashcard> les, boolean punctuation, boolean capt, List<String> headers, List<Boolean> foreignVals) {
        this.lessonName = name;
        this.flashcards = les;
        this.isCaseSensitive = capt;
        this.isPunctSensitive = punctuation;
        this.headers = headers;
        this.foreignIndexes = foreignVals;
    }

    protected Lesson(Parcel in) {
        id = in.readInt();
        lessonName = in.readString();
        flashcards = in.createTypedArrayList(Flashcard.CREATOR);
        isPunctSensitive = in.readByte() != 0;
        isCaseSensitive = in.readByte() != 0;
        headers = in.createStringArrayList();
        foreignIndexes = new ArrayList<>();
        in.readList(foreignIndexes, Integer.class.getClassLoader());
    }

    public void addFlashcards(List<Flashcard> cards) {
        if (this.flashcards == null) {
            this.flashcards = cards;
        } else {
            this.flashcards.addAll(cards);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLessonName() {
        return this.lessonName;
    }

    public void setLessonName(String name) {
        this.lessonName = name == null ? "" : name;
    }

    public List<Flashcard> getFlashcards() {
        return this.flashcards;
    }

    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public void setIsCaseSensitive(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    public boolean isPunctSensitive() {
        return isPunctSensitive;
    }

    public void setIsPunctSensitive(boolean isPunctSensitive) {
        this.isPunctSensitive = isPunctSensitive;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Boolean> getForeignIndexes() {
        return foreignIndexes;
    }

    public void setForeignIndexes(List<Boolean> foreignIndexes) {
        this.foreignIndexes = foreignIndexes;
    }

    public String getHeadersString() {
        return this.headers == null ? "" : String.join(", ", this.getHeaders());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Lesson otherLesson = (Lesson) obj;
        return Objects.equals(this.lessonName, otherLesson.lessonName) &&
                this.isPunctSensitive == otherLesson.isPunctSensitive() &&
                this.isCaseSensitive == otherLesson.isCaseSensitive() &&
                Objects.equals(this.flashcards, otherLesson.flashcards) &&
                Objects.equals(this.headers, otherLesson.getHeaders()) &&
                Objects.equals(this.foreignIndexes, otherLesson.getForeignIndexes());
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
        dest.writeByte((byte) (isPunctSensitive ? 1 : 0));
        dest.writeByte((byte) (isCaseSensitive ? 1 : 0));
        dest.writeStringList(headers);
        dest.writeList(foreignIndexes);
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
            Type listType = new TypeToken<ArrayList<Flashcard>>() {
            }.getType();
            return gson.fromJson(data, listType);
        }

        @TypeConverter
        public static String fromStringList(List<String> list) {
            return new Gson().toJson(list);
        }

        @TypeConverter
        public static List<String> toStringList(String data) {
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            return new Gson().fromJson(data, listType);
        }

        @TypeConverter
        public static String fromBooleanList(List<Boolean> list) {
            return new Gson().toJson(list);
        }

        @TypeConverter
        public static List<Boolean> toBooleanList(String data) {
            Type listType = new TypeToken<List<Boolean>>() {
            }.getType();
            return new Gson().fromJson(data, listType);
        }
    }
}
