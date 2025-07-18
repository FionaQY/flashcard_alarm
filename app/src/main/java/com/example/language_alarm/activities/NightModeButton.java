package com.example.language_alarm.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.example.language_alarm.R;
import com.example.language_alarm.utils.SettingUtils;


public class NightModeButton extends RelativeLayout {
    //Context
    protected Context mContext;
    protected LayoutInflater mLayoutInflater;
    //Views
    protected ImageView switchIV;
    protected CardView switchRL;
    SettingUtils prefs = null;
    boolean isNight = false;
    //Listener
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
        switchRL = rootView.findViewById(R.id.switchRL);
        switchIV = rootView.findViewById(R.id.switchIV);

        setIcons(isNight);

        switchRL.setOnClickListener(v -> {
            if (inAnimation) return; // ignore clicks during animation
            prefs.flipIsDarkTheme();

            inAnimation = true;
            boolean currentIsNight = prefs.getIsDarkTheme();

            // Animate rotation
            float startRotation = currentIsNight ? 360f : 0f;
            float endRotation = currentIsNight ? 0f : 360f;
            ObjectAnimator.ofFloat(switchIV, "rotation", startRotation, endRotation)
                    .setDuration(400)
                    .start();

            // Animate translationX
            float startTranslation = currentIsNight ? 0f : switchRL.getWidth() / 2f;
            float endTranslation = currentIsNight ? switchRL.getWidth() / 2f : 0f;
            ObjectAnimator.ofFloat(switchIV, "translationX", startTranslation, endTranslation)
                    .setDuration(400)
                    .start();

            // Animate background color
            int fromColor = currentIsNight ? Color.parseColor("#dadada") : Color.parseColor("#353535");
            int toColor = currentIsNight ? Color.parseColor("#353535") : Color.parseColor("#dadada");

            ValueAnimator colorAnimator = ValueAnimator.ofArgb(fromColor, toColor);
            colorAnimator.setDuration(400);
            colorAnimator.addUpdateListener(animation ->
                    switchRL.setCardBackgroundColor((int) animation.getAnimatedValue())
            );

            colorAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animator) {
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animator) {
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {
                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
                    int iconRes = prefs.getIsDarkTheme() ? R.drawable.night_icon : R.drawable.day_icon;
                    switchIV.setImageDrawable(AppCompatResources.getDrawable(context, iconRes));
                    inAnimation = false;
                    setAppTheme();
                }
            });

            colorAnimator.start();

        });

        switchRL.post(() -> setIcons(prefs.getIsDarkTheme()));

    }

    private void setIcons(boolean isNight) {
        int iconRes = isNight ? R.drawable.night_icon : R.drawable.day_icon;
        switchIV.setImageDrawable(AppCompatResources.getDrawable(mContext, iconRes));
        if (isNight) {
            switchIV.setTranslationX(switchRL.getWidth() / 2f);
            switchIV.setRotation(0f);
            switchRL.setCardBackgroundColor(Color.parseColor("#353535"));
        } else {
            switchIV.setTranslationX(0f);
            switchIV.setRotation(360f);
            switchRL.setCardBackgroundColor(Color.parseColor("#dadada"));
        }
    }

    private void setAppTheme() {
        AppCompatDelegate.setDefaultNightMode(prefs.getIsDarkTheme() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

}