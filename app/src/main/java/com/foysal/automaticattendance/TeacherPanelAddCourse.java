package com.foysal.automaticattendance;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class TeacherPanelAddCourse extends AppCompatActivity {

    protected EditText editTextCourseCode, editTextCourseTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_panel_add_course);

        editTextCourseCode = findViewById(R.id.editText_Code);
        editTextCourseTitle = findViewById(R.id.editText_Title);
        //editTextSession = findViewById(R.id.editText_Session);




    }

    public void addButton(View view){
        if( (editTextCourseTitle.getText().toString().length()==0) || editTextCourseTitle.getText().toString().trim().equals(" ")){
            editTextCourseTitle.requestFocus();
        }
        else if( (editTextCourseCode.getText().toString().length()==0) || editTextCourseCode.getText().toString().trim().equals(" ")){
            editTextCourseCode.requestFocus();
        }

        else {
            TeacherPanelCourseList.addCourse(editTextCourseTitle.getText().toString(), editTextCourseCode.getText().toString());
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (coursetitle varchar, coursecode varchar)");
            String title,code,session,batch;
            title = editTextCourseTitle.getText().toString();
            code = editTextCourseCode.getText().toString();
            //session = editTextSession.getText().toString();

            ContentValues values = new ContentValues();
            values.put("coursetitle",title);
            values.put("coursecode",code);
            //values.put("session",session);


            sqLiteDatabase.insert("CourseList", null, values);

            finish();
        }
    }


}
