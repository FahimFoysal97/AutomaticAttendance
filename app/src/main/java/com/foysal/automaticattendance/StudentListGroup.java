package com.foysal.automaticattendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StudentListGroup extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_group);
    }

    public void addStudentGroupButtonClicked(View view){
        Intent intent = new Intent(getApplicationContext(),AddStudentGroup.class);
        startActivity(intent);
    }
}
