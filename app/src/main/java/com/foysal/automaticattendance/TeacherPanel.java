package com.foysal.automaticattendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class TeacherPanel extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_panel);
    }

    public void myCoursesListButtonClicked(View view){
        Intent intent = new Intent(getApplicationContext(),TeacherPanelCourseList.class);
        startActivity(intent);
    }

    public void studentListButtonClicked(View view){
        Intent intent = new Intent(getApplicationContext(),StudentListGroup.class);
        startActivity(intent);
    }

    public void makeAttendanceSheetButtonClicked(View view){
        Intent intent = new Intent(getApplicationContext(),MakeAttendanceSheet.class);
        startActivity(intent);
    }
}
