package com.example.language_alarm.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.language_alarm.R;
import com.google.android.material.appbar.MaterialToolbar;

public class ToolbarHelper {

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setupToolbar(MaterialToolbar toolbar, @Nullable String title,
                                    boolean showBackButton, @Nullable Runnable backAction) {
        TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (titleView != null) {
            titleView.setText(title != null ? title : "");
            titleView.setVisibility(title != null ? TextView.VISIBLE : TextView.GONE);
        }

        if (showBackButton) {
            toolbar.setNavigationIcon(R.drawable.outline_arrow_back_24);
            toolbar.setNavigationContentDescription(R.string.action_back);
            toolbar.setNavigationOnClickListener(v -> {
                Log.d("TOOLBAR", "Back button clicked");
                if (backAction != null) {
                    backAction.run();
                } else if (toolbar.getContext() instanceof Activity) {
                    ((Activity) toolbar.getContext()).onBackPressed();
                }
            });
        } else {
            toolbar.setNavigationIcon(null);
        }

    }

    public static void setupToolbar(MaterialToolbar toolbar, String title) {
        setupToolbar(toolbar, title, false, null);
    }
}
