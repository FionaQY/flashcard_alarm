package com.example.language_alarm.utils;

import android.content.Context;
import android.util.Log;

import com.example.language_alarm.database.LessonDao;
import com.example.language_alarm.database.LessonDatabase;
import com.example.language_alarm.models.Lesson;

import java.util.concurrent.Executors;


public class LessonHandler {
    private static final String TAG = "LessonHandler";

    private static LessonDao getLessonDao(Context ctx) {
        return LessonDatabase.getDatabase(ctx).lessonDao();
    }

    public static void saveLesson(Context ctx, Lesson lesson) {
        Context appContext = ctx.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (lesson == null) {
                    Log.w(TAG, "Attempted to save null lesson");
                    return;
                }
                if (lesson.getId() == 0) {
                    // new lesson
                    Log.d(TAG, String.format("Saving new Lesson: %s", lesson.getLessonName()));
                    long id = getLessonDao(appContext).insert(lesson);
                    lesson.setId((int) id);
                    Log.d(TAG, String.format("New lesson saved with ID: %d", id));
                } else {
                    getLessonDao(appContext).update(lesson);
//                    rescheduleAlarm(appContext, lesson);
                    Log.i(TAG, "Lesson updated and rescheduled: " + lesson.getId());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving lesson", e);
            }

        });
    }

    public static void deleteLesson(Context ctx, Lesson lesson) {
        Context appContext = ctx.getApplicationContext();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (lesson.getId() == 0) {
                    Log.w(TAG, "Attempted to delete null lesson");
                    return;
                }
//                cancelAlarm(appContext, lesson);
                // set lessonID of all alarms with this lessonId to null?
                getLessonDao(appContext).delete(lesson);
                Log.i(TAG, String.format("Successfully deleted lesson ID:%d", lesson.getId()));
            } catch (Exception e) {
                Log.e(TAG, "Error deleting lesson", e);
            }
        });
    }

    public static void deleteFlashcard(Context ctx, Lesson lesson, int index) {
        if (lesson == null || lesson.getFlashcards() == null || index >= lesson.getFlashcards().size() || index < 0) {
            Log.w(TAG, String.format("Unable to delete flashcard of index %d for lesson %s", index, lesson == null ? null : lesson.getLessonName()));
            return;
        }
        lesson.getFlashcards().remove(index);
        saveLesson(ctx, lesson);
    }

}
