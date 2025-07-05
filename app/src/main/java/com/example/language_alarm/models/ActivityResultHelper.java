package com.example.language_alarm.models;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityResultHelper {
    private final ActivityResultLauncher<Intent> launcher;

    public ActivityResultHelper(AppCompatActivity activity, ActivityResultCallback callback) {
        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                res -> {
                    if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                        Uri uri = res.getData().getData();
                        if (uri != null) {
                            callback.onActivityResult(uri);
                        }
                    }
                }
        );
    }

    public void launchAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("audio/*")
                .addCategory(Intent.CATEGORY_OPENABLE);
        launcher.launch(Intent.createChooser(intent, "Select Alarm Tone"));
    }

    public void launchCsvPicker() {
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .setType("text/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        launcher.launch(Intent.createChooser(intent, "Select CSV file"));
    }

    public interface ActivityResultCallback {
        void onActivityResult(Uri uri);
    }
}
