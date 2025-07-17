package com.example.language_alarm.utils;

import android.content.Context;
import android.util.Log;

import com.example.language_alarm.database.LessonDao;
import com.example.language_alarm.database.LessonDatabase;
import com.example.language_alarm.models.Lesson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LessonHandler {
    private static final String TAG = "LessonHandler";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static LessonDao getLessonDao(Context ctx) {
        return LessonDatabase.getDatabase(ctx).lessonDao();
    }

    public static void saveLesson(Context ctx, Lesson lesson) {
        Context appContext = ctx.getApplicationContext();
        executor.execute(() -> {
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
                    Log.d(TAG, String.format("New %s saved!", lesson.getLogDesc()));
                } else {
                    getLessonDao(appContext).update(lesson);
                    Log.i(TAG,
                            String.format("%s updated and rescheduled", lesson.getLogDesc()));
                }
            } catch (Exception e) {
                assert lesson != null;
                Log.e(TAG, String.format("Error saving %s", lesson.getLogDesc()), e);
            }

        });
    }

    public static void deleteLesson(Context ctx, Lesson lesson) {
        Context appContext = ctx.getApplicationContext();
        executor.execute(() -> {
            try {
                if (lesson.getId() == 0) {
                    Log.w(TAG, "Attempted to delete null lesson");
                    return;
                }
                AlarmHandler.removeLesson(ctx, lesson.getId());
                getLessonDao(appContext).delete(lesson);
                Log.i(TAG, String.format("Successfully deleted %s", lesson.getLogDesc()));
            } catch (Exception e) {
                Log.e(TAG,
                        String.format("Error deleting %s", lesson.getLogDesc()), e);
            }
        });
    }

}
