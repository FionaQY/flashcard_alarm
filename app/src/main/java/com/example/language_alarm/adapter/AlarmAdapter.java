package com.example.language_alarm.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.activities.NewAlarmActivity;
import com.example.language_alarm.models.Alarm;
import com.example.language_alarm.utils.AlarmHandler;

import java.util.ArrayList;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private final Context ctx;
    private List<Alarm> alarmList;

    public AlarmAdapter(Context ctx, List<Alarm> alarms) {
        this.ctx = ctx;
        this.alarmList = alarms;
    }

    public void setAlarms(List<Alarm> newAlarms) {
        if (alarmList == null) {
            this.alarmList = new ArrayList<>();
            alarmList.addAll(newAlarms);
            notifyDataSetChanged();
        } else {
            DiffUtil.Callback diffCallback = new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return alarmList.size();
                }

                @Override
                public int getNewListSize() {
                    return newAlarms.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return alarmList.get(oldItemPosition).getId() == newAlarms.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return alarmList.get(oldItemPosition).equals(newAlarms.get(newItemPosition));
                }
            };
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            alarmList.clear();
            alarmList.addAll(newAlarms);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_item, parent, false);
        return new AlarmViewHolder(view);
    }

    private void changeStatusOfAlarm(boolean isChecked, Alarm alarm) {
        alarm.setEnabled(isChecked);
        if (isChecked) {
            AlarmHandler.rescheduleAlarm(this.ctx, alarm);
        } else {
            AlarmHandler.cancelAlarm(this.ctx, alarm);
        }
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        Alarm alarm = this.alarmList.get(position);
        holder.timeTextView.setText(alarm.getTime());
        holder.labelTextView.setText(alarm.getDescription());
        holder.toggleSwitch.setChecked(alarm.isEnabled());
        holder.toggleSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> changeStatusOfAlarm(isChecked, alarm));

        // go to alarm page when click (maybe have edit button)
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(ctx, NewAlarmActivity.class);
            intent.putExtra("alarm", alarm);
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if (this.alarmList == null) {
            return 0;
        }
        return this.alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        public TextView timeTextView, labelTextView;
        public SwitchCompat toggleSwitch;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.alarm_time);
            labelTextView = itemView.findViewById(R.id.alarm_label);
            toggleSwitch = itemView.findViewById(R.id.alarm_toggle);
        }
    }
}
