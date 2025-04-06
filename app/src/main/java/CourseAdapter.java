package com.company.klassyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private ArrayList<String> courseNames;

    public CourseAdapter(ArrayList<String> courseNames) {
        this.courseNames = courseNames;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView courseName;

        public ViewHolder(View view) {
            super(view);
            courseName = view.findViewById(R.id.courseName);
        }
    }

    @Override
    public CourseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.courseName.setText(courseNames.get(position));
    }

    @Override
    public int getItemCount() {
        return courseNames.size();
    }
}
