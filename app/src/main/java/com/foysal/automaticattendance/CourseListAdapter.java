package com.foysal.automaticattendance;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class CourseListAdapter extends RecyclerView .Adapter<CourseListAdapter.ViewHolder>{

    private List<ItemListCourse> courseList;
    private Context context;

    public CourseListAdapter(List<ItemListCourse> courseList) {
        this.courseList = courseList;
        //this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_coarse_layout,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ItemListCourse itemListCourse = courseList.get(i);

        viewHolder.headTextView.setText(itemListCourse.getHead());
        viewHolder.tailTextView.setText(itemListCourse.getTail());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView headTextView, tailTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            headTextView = itemView.findViewById(R.id.textViewHead);
            tailTextView = itemView.findViewById(R.id.textViewTail);
        }
    }
}
