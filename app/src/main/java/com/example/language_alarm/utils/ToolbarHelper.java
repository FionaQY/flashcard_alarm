package com.example.language_alarm.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.appbar.MaterialToolbar;

import com.example.language_alarm.R;

public class ToolbarHelper {

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setupToolbar(MaterialToolbar toolbar, @Nullable String title,
                                    boolean showBackButton, @Nullable Runnable backAction) {
        TextView titleView = toolbar.findViewById(R.id.toolbar_title);
        if (titleView != null) {
            titleView.setText(title != null ? title: "");
            titleView.setVisibility(title != null ? TextView.VISIBLE: TextView.GONE);
        }

        toolbar.setNavigationIcon(showBackButton
                ? toolbar.getContext().getDrawable(R.drawable.outline_arrow_back_24)
                : null);

        toolbar.setNavigationOnClickListener(v -> {
            if (backAction != null) {
                backAction.run();
            } else if (toolbar.getContext() instanceof Activity) {
                ((Activity) toolbar.getContext()).onBackPressed();
            }
        });
    }

    public static void setupToolbar(MaterialToolbar toolbar, String title) {
        setupToolbar(toolbar, title, true, null);
    }

    public static void setupToolbar(MaterialToolbar toolbar, String title, boolean showBack) {
        setupToolbar(toolbar, title, showBack, null);
    }
}
