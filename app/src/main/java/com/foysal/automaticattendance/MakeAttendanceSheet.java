package com.foysal.automaticattendance;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MakeAttendanceSheet extends AppCompatActivity {

    List<String> courses = new ArrayList<>();
    List<String> studentGroups = new ArrayList<>();
    ArrayAdapter<String> coursesAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,courses);
    ArrayAdapter<String> studentGroupAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,studentGroups);
    Spinner courseSpinner;
    Spinner studentGroupSpinner;
    boolean courseSelected = false;
    boolean studentGroupSelected = false;
    String courseName;
    String groupName;
    SQLiteDatabase sqLiteDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_attendance_sheet);
        courseSpinner = findViewById(R.id.spinner_course);
        studentGroupSpinner = findViewById(R.id.spinner_student_group);
        studentGroupSpinner.setAdapter(coursesAdapter);
        courseSpinner.setAdapter(studentGroupAdapter);
        setCourseSpinner();
        setStudentGroupSpinner();
        sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseSelected = true;
                courseName = (String)courseSpinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                courseSelected = false;
            }
        });

        studentGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                studentGroupSelected = true;
                groupName = (String)studentGroupSpinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                studentGroupSelected = false;
            }
        });

    }

    void setCourseSpinner(){
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (coursecode varchar, coursetitle varchar, session varchar, batch vrachar)");
        Cursor c = sqLiteDatabase.rawQuery("select * from CourseList",null);


        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                //String name = cursor.getString(cursor.getColumnIndex(countyname));
                String str = c.getString(c.getColumnIndex("coursecode")) + " " +
                        c.getString(c.getColumnIndex("courselist")) + " " +
                        c.getString(c.getColumnIndex("session")) + " " +
                        c.getString(c.getColumnIndex("batch"));

                courses.add(str);
                c.moveToNext();
            }
        }
        c.close();
        coursesAdapter.notifyDataSetChanged();

    }

    void setStudentGroupSpinner(){

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS StudentGroupList (groupname varchar PRIMARY KEY, session varchar) ");
        Cursor c = sqLiteDatabase.rawQuery("select * from StudentGroupList",null);


        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String str = c.getString(c.getColumnIndex("groupname")) + " " +
                        c.getString(c.getColumnIndex("session"));

                studentGroups.add(str);
                c.moveToNext();
            }
        }
        c.close();
        studentGroupAdapter.notifyDataSetChanged();

    }

    public void makeSheetButtonPressed(View view){

        if(!courseSelected){
            ((TextView)findViewById(R.id.textView_warning2)).setText("Course not selected");
        }
        else if(!studentGroupSelected){
            ((TextView)findViewById(R.id.textView_warning2)).setText("Student group not selected");
        }
        else {
            String str = courseName.replaceAll(" ","_") + "_" + groupName.replaceAll(" ","_");
            String sql = "SELECT name FROM sqlite_master WHERE type = 'table' and name = '?'";
            Cursor c = sqLiteDatabase.rawQuery(sql, new String[]{str});
            if(c.getCount()>0){
                ((TextView)findViewById(R.id.textView_warning2)).setText("Sheet already exist");
                c.close();
            }
            else {
                String s1 = "CREATE TABLE "+str+" (id varchar PRIMARY KEY)";
                sqLiteDatabase.execSQL(s1);
                s1 = "Select id from " + groupName ;
                c = sqLiteDatabase.rawQuery(s1, null);


                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {

                        s1 = "insert into " + str + " " + c.getString(c.getColumnIndex("id"));
                        sqLiteDatabase.execSQL(s1);
                        c.moveToNext();
                    }
                }


                c.close();
                finish();
            }
        }

    }

}
