package com.example.language_alarm.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @Ignore
    public int originalIndex = -1;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStringList(values);
        dest.writeByte((byte) (isImportant ? 1 : 0));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Flashcard otherFlashcard = (Flashcard) obj;
        if (this.isImportant != otherFlashcard.isImportant) {
            return false;
        }
        if (otherFlashcard.getVals() == null) {
            return this.getVals() == null;
        }
        if (this.getVals() == null) {
            return false;
        }
        if (this.getVals().size() != otherFlashcard.getVals().size()) {
            return false;
        }
        for (int i = 0; i < this.getVals().size(); i++) {
            String thisV = this.getVals().get(i);
            String otherV = otherFlashcard.getVals().get(i);
            if (!thisV.equals(otherV)) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    public Flashcard clone() {
        List<String> clone = new ArrayList<>(this.getVals());
        Flashcard newCard = new Flashcard(clone);
        newCard.markImportance(this.isImportant);
        newCard.originalIndex = this.originalIndex;
        return newCard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getVals());
    }
}
