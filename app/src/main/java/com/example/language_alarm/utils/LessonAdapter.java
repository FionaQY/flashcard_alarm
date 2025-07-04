package com.example.language_alarm.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.language_alarm.R;
import com.example.language_alarm.activities.NewLessonActivity;
import com.example.language_alarm.models.Lesson;

import java.util.ArrayList;
import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {
    private final Context ctx;
    private List<Lesson> lessonList;

    public LessonAdapter(Context ctx, List<Lesson> lessons) {
        this.ctx = ctx;
        this.lessonList = lessons;
    }

    public void setLessons(List<Lesson> newLessons) {
        if (lessonList == null) {
            this.lessonList = new ArrayList<>();
            lessonList.addAll(newLessons);
            notifyDataSetChanged();
        } else {
            DiffUtil.Callback diffCallback = new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return lessonList.size();
                }

                @Override
                public int getNewListSize() {
                    return newLessons.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return lessonList.get(oldItemPosition).getId() == newLessons.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return lessonList.get(oldItemPosition).equals(newLessons.get(newItemPosition));
                }
            };
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

            lessonList.clear();
            lessonList.addAll(newLessons);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder{
        public TextView lessonNameView;
        public Button editButton;
        public Button deleteButton;

        public LessonViewHolder(View itemView) {
            super(itemView);
            lessonNameView = itemView.findViewById(R.id.lesson_name);
            editButton = itemView.findViewById(R.id.edit_lesson);
            deleteButton = itemView.findViewById(R.id.delete_lesson);
        }
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lesson_item, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LessonViewHolder holder, int position) {
        Lesson lesson = this.lessonList.get(position);
        holder.lessonNameView.setText(lesson.getLessonName());

        holder.editButton.setOnClickListener(view -> {
            Intent intent = new Intent(ctx, NewLessonActivity.class);
            intent.putExtra("lesson", lesson);
            ctx.startActivity(intent);
        });

        // TODO: confirmation dialog
        holder.deleteButton.setOnClickListener(view -> {
            Intent intent = new Intent(ctx, NewLessonActivity.class);
            intent.putExtra("lesson", lesson);
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if (this.lessonList == null)  {
            return 0;
        }
        return this.lessonList.size();
    }
}
