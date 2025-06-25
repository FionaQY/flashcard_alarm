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
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String lessonName;
    private boolean careAboutPunctuation = true;
    private boolean careAboutCapitalisation = true;
    private List<String> headers;
    private List<Integer> englishIndexes;
    private List<Integer> germanIndexes;
    @TypeConverters(Converters.class)
    private List<Flashcard> flashcards = null;
    public Lesson() {
        this.headers = new ArrayList<>();
        this.englishIndexes = new ArrayList<>();
        this.germanIndexes = new ArrayList<>();
    }

    public Lesson(String name, List<Flashcard> les, boolean punct, boolean capt, List<String> headers, List<Integer> engIdx, List<Integer> deInd) {
        this.lessonName = name;
        this.flashcards = les;
        this.careAboutCapitalisation = capt;
        this.careAboutPunctuation = punct;
        this.headers = headers;
        this.englishIndexes = engIdx;
        this.germanIndexes = deInd;
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

    protected Lesson(Parcel in) {
        id = in.readInt();
        lessonName = in.readString();
        flashcards = in.createTypedArrayList(Flashcard.CREATOR);
        careAboutPunctuation = in.readByte() != 0;
        careAboutCapitalisation = in.readByte() != 0;
        headers = in.createStringArrayList();
        englishIndexes = new ArrayList<>();
        in.readList(englishIndexes, Integer.class.getClassLoader());
        germanIndexes = new ArrayList<>();
        in.readList(germanIndexes, Integer.class.getClassLoader());
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

    public boolean isCareAboutCapitalisation() {
        return careAboutCapitalisation;
    }

    public void setCareAboutCapitalisation(boolean careAboutCapitalisation) {
        this.careAboutCapitalisation = careAboutCapitalisation;
    }

    public boolean isCareAboutPunctuation() {
        return careAboutPunctuation;
    }

    public void setCareAboutPunctuation(boolean careAboutPunctuation) {
        this.careAboutPunctuation = careAboutPunctuation;
    }

    public List<Integer> getEnglishIndexes() {
        return englishIndexes;
    }

    public void setEnglishIndexes(List<Integer> englishIndexes) {
        this.englishIndexes = englishIndexes;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Integer> getGermanIndexes() {
        return germanIndexes;
    }

    public void setGermanIndexes(List<Integer> germanIndexes) {
        this.germanIndexes = germanIndexes;
    }

    public String getHeadersString() {
        return this.headers == null ? "" : String.join(",", this.getHeaders());
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
                this.careAboutPunctuation == otherLesson.isCareAboutPunctuation() &&
                this.careAboutCapitalisation == otherLesson.isCareAboutCapitalisation() &&
                Objects.equals(this.flashcards, otherLesson.flashcards) &&
                Objects.equals(this.englishIndexes, otherLesson.getEnglishIndexes()) &&
                Objects.equals(this.headers, otherLesson.getHeaders()) &&
                Objects.equals(this.germanIndexes, otherLesson.getGermanIndexes());
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
        dest.writeByte((byte) (careAboutPunctuation ? 1 : 0));
        dest.writeByte((byte) (careAboutCapitalisation ? 1 : 0));
        dest.writeStringList(headers);
        dest.writeList(englishIndexes);
        dest.writeList(germanIndexes);
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

        @TypeConverter
        public static String fromStringList(List<String> list) {
            return new Gson().toJson(list);
        }

        @TypeConverter
        public static List<String> toStringList(String data) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            return new Gson().fromJson(data, listType);
        }

        @TypeConverter
        public static String fromIntegerList(List<Integer> list) {
            return new Gson().toJson(list);
        }

        @TypeConverter
        public static List<Integer> toIntegerList(String data) {
            Type listType = new TypeToken<List<Integer>>() {}.getType();
            return new Gson().fromJson(data, listType);
        }
    }
}
