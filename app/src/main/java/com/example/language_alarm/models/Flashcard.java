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

    private boolean isImportant;

    public Flashcard(List<String> cards) {
        this.values = new ArrayList<>(cards);
    }

    protected Flashcard(Parcel in) {
        values = in.createStringArrayList();
        isImportant = in.readByte() != 0;
    }

    public void markImportance(boolean isImportant) {
        this.isImportant = isImportant;
    }

    @NonNull
    @Override
    public String toString() {
        return android.text.TextUtils.join(" ", this.values);
    }

    public List<String> getVals() {
        return values;
    }

    public void setVals(List<String> values) {
        this.values = values;
    }

    public boolean isCorrect(List<String> userInput, boolean isPunctSensitive, boolean isCaseSensitive) {
        if (userInput == null || userInput.size() < this.values.size()) {
            return false;
        }
        for (int i = 0; i < this.values.size(); i++) {
            String corrAns = this.values.get(i);
            if (corrAns == null || corrAns.trim().isEmpty()) {
                continue;
            }
            String userAns = userInput.get(i);
            if (userAns == null || userAns.trim().isEmpty()) {
                return false;
            }
            corrAns = corrAns.replaceAll("\\s", "");
            userAns = userAns.replaceAll("\\s", "");
            if (!isPunctSensitive) {
                corrAns = corrAns.replaceAll("\\p{Punct}", "");
                userAns = userAns.replaceAll("\\p{Punct}", "");
            }
            if (!isCaseSensitive) {
                corrAns = corrAns.toLowerCase();
                userAns = userAns.toLowerCase();
            }
            if (!corrAns.equals(userAns)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStringList(values);
        dest.writeByte((byte) (isImportant ? 1 : 0));
    }
}
