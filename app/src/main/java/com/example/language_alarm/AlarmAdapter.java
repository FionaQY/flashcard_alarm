package com.example.language_alarm;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.activities.NewAlarmActivity;
import com.example.language_alarm.models.Alarm;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private Context ctx;
    private List<Alarm> alarmList;

    public AlarmAdapter(Context ctx, List<Alarm> alarms) {
        this.ctx = ctx;
        this.alarmList = alarms;
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder{
        public TextView timeTextView, labelTextView;
        public SwitchCompat toggleSwitch;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            timeTextView = (TextView) itemView.findViewById(R.id.alarm_time);
            labelTextView = (TextView) itemView.findViewById(R.id.alarm_label);
            labelTextView.setText("bbb"); // set this value
            toggleSwitch = (SwitchCompat) itemView.findViewById(R.id.alarm_toggle);
        }
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_item, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        Alarm alarm = this.alarmList.get(position);
        holder.timeTextView.setText(alarm.getTime());
//        holder.labelTextView.setText(alarm.getLabel());
        holder.toggleSwitch.setChecked(alarm.isEnabled());
        holder.toggleSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> alarm.setStatus(isChecked));

        // go to alarm page when click (maybe have edit button)
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(ctx, NewAlarmActivity.class);
            intent.putExtra("time", alarm.getTime());
            intent.putExtra("enabled", alarm.isEnabled());
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if (this.alarmList == null)  {
            return 0;
        }
        return this.alarmList.size();
    }
}
