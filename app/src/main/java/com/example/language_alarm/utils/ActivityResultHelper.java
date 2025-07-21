package com.example.language_alarm.utils;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ActivityResultHelper {
    private final ActivityResultLauncher<Intent> launcher;
    private final Activity ctx;

    public ActivityResultHelper(AppCompatActivity activity, FileResultCallback callback) {
        this.ctx = activity;
        this.launcher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                res -> {
                    if (res.getResultCode() == RESULT_OK && res.getData() != null) {
                        Uri uri = res.getData().getData();
                        if (uri != null) {
                            try {
                                activity.getContentResolver().takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                                // callback is what to do with the uri after selection
                                callback.onFileSelected(uri);
                            } catch (SecurityException e) {
                                Log.w("ActivityResultHelper",
                                        String.format(Locale.US, "Permission denied for URI: %s", uri));
                            }
                        }
                    }
                }
        );
    }

    public void launchFilePicker(FileType fileType) {
        if (!PermissionUtils.hasStoragePermission(this.ctx)) {
            PermissionUtils.requestStoragePermission(this.ctx);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(fileType.mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        if (fileType.extraMimeTypes != null) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, fileType.extraMimeTypes);
        }

        launcher.launch(Intent.createChooser(intent, fileType.chooserTitle));
    }

    public enum FileType {
        IMAGE("image/*", "Select Image"),
        AUDIO("audio/*", "Select Audio"),
        CSV("text/*", "Select CSV File",
                new String[]{"text/csv", "text/comma-separated-values", "application/csv"});

        final String mimeType;
        final String chooserTitle;
        final String[] extraMimeTypes;

        FileType(String mimeType, String chooserTitle) {
            this(mimeType, chooserTitle, null);
        }

        FileType(String mimeType, String chooserTitle, String[] extraMimeTypes) {
            this.mimeType = mimeType;
            this.chooserTitle = chooserTitle;
            this.extraMimeTypes = extraMimeTypes;
        }
    }

    public interface FileResultCallback {
        void onFileSelected(Uri uri);
    }
}
