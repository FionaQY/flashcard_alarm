package com.example.language_alarm.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.language_alarm.R;
import com.example.language_alarm.utils.SettingUtils;


public class NightModeButton extends RelativeLayout {
    protected Context mContext;
    protected LayoutInflater mLayoutInflater;
    protected ImageView bulb;
    SettingUtils prefs = null;
    boolean isNight = false;
    private boolean inAnimation = false;

    public NightModeButton(Context context) {
        super(context);
        init(context);
    }

    public NightModeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(final Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        prefs = new SettingUtils(context);
        isNight = prefs.getIsDarkTheme();

        View rootView = mLayoutInflater.inflate(R.layout.night_mode_button_layout, this, true);
        bulb = rootView.findViewById(R.id.light_bulb);

        setBulbState(isNight);

        bulb.setOnClickListener(v -> {
            if (inAnimation) return;
            prefs.flipIsDarkTheme();
            inAnimation = true;

            boolean currentIsNight = prefs.getIsDarkTheme();

//            ObjectAnimator swing = ObjectAnimator.ofFloat(bulb, "rotation", -15f, 15f, -10f, 10f, 0f);
//            swing.setDuration(600);
//            swing.start();

            bulb.setAlpha(0f);
            bulb.setImageDrawable(AppCompatResources.getDrawable(context,
                    currentIsNight ? R.drawable.day_icon : R.drawable.night_icon));
            bulb.animate().alpha(1f).setDuration(300).start();

            bulb.postDelayed(() -> {
                inAnimation = false;
                setAppTheme();
            }, 100);
        });
    }

    private void setBulbState(boolean isNight) {
        int iconRes = isNight ? R.drawable.night_icon : R.drawable.day_icon;
        bulb.setImageDrawable(AppCompatResources.getDrawable(mContext, iconRes));
    }


    private void setAppTheme() {
        AppCompatDelegate.setDefaultNightMode(prefs.getIsDarkTheme() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

}