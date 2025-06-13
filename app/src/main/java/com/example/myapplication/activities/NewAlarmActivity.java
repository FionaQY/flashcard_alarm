package com.example.myapplication.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.AlarmReceiver;
import com.example.myapplication.R;

import java.util.Calendar;
import java.util.Objects;

public class NewAlarmActivity extends AppCompatActivity {
    Toolbar header;
    TimePicker alarmTimePicker;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_alarm);

        this.header = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(header);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Next alarm not yet set.");

        alarmTimePicker = (TimePicker) findViewById(R.id.timePicker);
        alarmTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                Toast.makeText(getApplicationContext(),
//                        "time changed",
//                        Toast.LENGTH_SHORT).show();
                toolbarTitle.setText(String.format("Time is :: %s : %s", hourOfDay, minute));
            }
        });
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }



    // OnToggleClicked() method implements the time functionality
    public void onToggleClicked(View view) {
        long time;
        if (((ToggleButton) view).isChecked()) {
            Toast.makeText(NewAlarmActivity.this, "ALARM ON", Toast.LENGTH_SHORT).show();
            Calendar calendar = Calendar.getInstance();

            // calendar is called to get current time in hour minute
            calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getHour());
            calendar.set(Calendar.MINUTE, alarmTimePicker.getMinute());

            // using intent i have class AlarmReceiver class which inherits BroadcasterReceiver
            Intent intent = new Intent(this, AlarmReceiver.class);

            // we call broadcast using pendingIntent
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

            time = (calendar.getTimeInMillis() - (calendar.getTimeInMillis()%60000));
            if (System.currentTimeMillis() > time) {
                int amPm = calendar.get(Calendar.AM_PM);
                time = amPm == Calendar.AM ? time + (1000 *60 * 60 * 12) : time + (1000 *60 * 60 * 24);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                } else {
                    Toast.makeText(this, "Exact alarms not allowed. Please enable in system settings.", Toast.LENGTH_LONG).show();
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        } else {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(NewAlarmActivity.this, "ALARM OFF", Toast.LENGTH_SHORT).show();
        }
    }

    public void showExitDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage("Cancel this alarm?")
                .setPositiveButton("Confirm",
                        (dialog, which) -> this.finish())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onSupportNavigateUp() {
        showExitDialog();
        return true;
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

}
