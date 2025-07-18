package com.example.language_alarm.utils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    public static boolean hasStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(activity, READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean hasScheduleAlarmPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms()
        }
        return false;
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            List<String> permissionsToRequest = new ArrayList<>();
            if (!hasStoragePermission(activity)) {
                permissionsToRequest.add(READ_MEDIA_AUDIO);
            }
            if (!permissionsToRequest.isEmpty()) {
                activity.requestPermissions(
                        permissionsToRequest.toArray(new String[0]),
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            }
        } else {
            if (!hasStoragePermission(activity)) {
                activity.requestPermissions(
                        new String[]{READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            }
        }
    }
}