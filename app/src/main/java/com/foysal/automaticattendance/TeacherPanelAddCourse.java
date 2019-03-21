package com.foysal.automaticattendance;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (courseTitle varchar, courseCode varchar)");
            String title,code;
            title = editTextCourseTitle.getText().toString();
            code = editTextCourseCode.getText().toString();
            String str = "Select * from CourseList where courseTitle = \"" + title +"\" collate nocase  and courseCode = \""  + code + "\" collate nocase";
            Cursor c = sqLiteDatabase.rawQuery(str,null);
            if(c.getCount()>0){
                ((TextView)findViewById(R.id.textView_warning_teacherPanelAddCourse)).setText("Course exist");
                return;
            }
            //session = editTextSession.getText().toString();

            ContentValues values = new ContentValues();
            values.put("courseTitle",title);
            values.put("courseCode",code);
            //values.put("session",session);


            sqLiteDatabase.insert("CourseList", null, values);

            finish();
        }
    }


}
