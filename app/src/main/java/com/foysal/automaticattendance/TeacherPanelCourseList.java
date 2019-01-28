package com.foysal.automaticattendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class TeacherPanelCourseList extends AppCompatActivity {

    static RecyclerView recyclerView;
    static RecyclerView.Adapter adapter;
    static List<ItemListCourse> courses;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_panel_course_list);
        recyclerView = findViewById(R.id.recycleView_courses);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    public static void addCourse(String courseTitle, String courseCode, String session, String batch){

        courses = new ArrayList<>();
        courses.add(new ItemListCourse("Course Code : " + courseCode + ", Course Title : " +courseTitle, "Session : " + session + ", Batch : " + batch));
        recyclerView.setAdapter(adapter);
        adapter = new CourseListAdapter(courses);
        adapter.notifyDataSetChanged();
    }


    public void addCourseButton(View view){
        Intent intent = new Intent(getApplicationContext(),TeacherPanelAddCourse.class);
        startActivity(intent);
    }
}
