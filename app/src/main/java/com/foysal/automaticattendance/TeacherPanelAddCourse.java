package com.foysal.automaticattendance;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class TeacherPanelAddCourse extends AppCompatActivity {

    protected EditText editTextCourseCode, editTextCourseTitle, editTextSession, editTextBatch;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_panel_add_course);

        editTextCourseCode = findViewById(R.id.editText_Code);
        editTextCourseTitle = findViewById(R.id.editText_Title);
        editTextSession = findViewById(R.id.editText_Session);
        editTextBatch = findViewById(R.id.editText_Batch);



    }

    public void addButton(View view){
        if( (editTextCourseTitle.getText().toString().length()==0) || editTextCourseTitle.getText().toString().trim().equals(" ")){
            editTextCourseTitle.requestFocus();
        }
        else if( (editTextCourseCode.getText().toString().length()==0) || editTextCourseCode.getText().toString().trim().equals(" ")){
            editTextCourseCode.requestFocus();
        }
        else if( (editTextSession.getText().toString().length()==0) || editTextSession.getText().toString().trim().equals(" ")){
            editTextSession.requestFocus();
        }
        else if( (editTextBatch.getText().toString().length()==0) || editTextBatch.getText().toString().trim().equals(" ")){
            editTextBatch.requestFocus();
        }
        else TeacherPanelCourseList.addCourse(editTextCourseTitle.getText().toString(), editTextCourseCode.getText().toString(), editTextSession.getText().toString(), editTextBatch.getText().toString());
    }


}
