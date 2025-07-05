package com.example.language_alarm.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Flashcard implements Parcelable {
    public static final Creator<Flashcard> CREATOR = new Creator<>() {
        @Override
        public Flashcard createFromParcel(Parcel in) {
            return new Flashcard(in);
        }

        @Override
        public Flashcard[] newArray(int size) {
            return new Flashcard[size];
        }
    };
    private List<String> values;

    public Flashcard(List<String> cards) {
        this.values = new ArrayList<>(cards);
    }

    protected Flashcard(Parcel in) {
        values = in.createStringArrayList();
    }

    public String getValsString() {
        return android.text.TextUtils.join("\n", this.values);
    }

    public List<String> getVals() {
        return values;
    }

    public void setVals(List<String> values) {
        this.values = values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStringList(values);
    }
}
