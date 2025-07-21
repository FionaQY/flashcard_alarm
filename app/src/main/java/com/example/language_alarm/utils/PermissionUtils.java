package com.example.language_alarm.utils;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.content.Context.ALARM_SERVICE;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 200; // Can be any unique number

    public static boolean noStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean noNotificationPermission(Context ctx) {
        return !NotificationManagerCompat.from(ctx).areNotificationsEnabled();
    }

    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (noNotificationPermission(activity)) {
                activity.requestPermissions(
                        new String[]{POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    public static boolean hasScheduleAlarmPermission(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return false;
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            List<String> permissionsToRequest = new ArrayList<>();
            if (noStoragePermission(activity)) {
                permissionsToRequest.add(READ_MEDIA_AUDIO);
            }
            if (!permissionsToRequest.isEmpty()) {
                activity.requestPermissions(
                        permissionsToRequest.toArray(new String[0]),
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            }
        } else {
            if (noStoragePermission(activity)) {
                activity.requestPermissions(
                        new String[]{READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            }
        }
    }
}