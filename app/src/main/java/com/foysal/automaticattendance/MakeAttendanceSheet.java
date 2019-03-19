package com.foysal.automaticattendance;

import android.content.ContentValues;
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
    ArrayAdapter<String> coursesAdapter;
    ArrayAdapter<String> studentGroupAdapter;
    Spinner courseSpinner;
    Spinner studentGroupSpinner;
    boolean courseSelected = false;
    boolean studentGroupSelected = false;



    String courseTitle,courseCode,studentGroupName,session,batch;
    String courseName;
    String groupName;
    SQLiteDatabase sqLiteDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_attendance_sheet);
        coursesAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,courses);
        studentGroupAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,studentGroups);
        courseSpinner = findViewById(R.id.spinner_course);
        studentGroupSpinner = findViewById(R.id.spinner_student_group);
        studentGroupSpinner.setAdapter(studentGroupAdapter);
        courseSpinner.setAdapter(coursesAdapter);

        setCourseSpinner();
        setStudentGroupSpinner();
        sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseSelected = true;
                courseName = (String)courseSpinner.getSelectedItem();
                getCourseInfo(courseName);
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
                getStudentGroupInfo(groupName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                studentGroupSelected = false;
            }
        });

    }

    void setCourseSpinner(){
        sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (coursecode varchar, coursetitle varchar)");
        Cursor c = sqLiteDatabase.rawQuery("select * from CourseList",null);


        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {

                String str = c.getString(c.getColumnIndex("coursecode")) + " " +
                        c.getString(c.getColumnIndex("coursetitle"));

                courses.add(str);
                c.moveToNext();
            }
        }
        c.close();
        coursesAdapter.notifyDataSetChanged();

    }

    void setStudentGroupSpinner(){

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS StudentGroupList (groupname varchar PRIMARY KEY, session varchar, batch varchar) ");
        Cursor c = sqLiteDatabase.rawQuery("select * from StudentGroupList",null);


        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String str = c.getString(c.getColumnIndex("groupname")) + " " +
                        c.getString(c.getColumnIndex("session")) + " " +
                        c.getString(c.getColumnIndex("batch"));

                studentGroups.add(str);
                c.moveToNext();
            }
        }
        c.close();
        studentGroupAdapter.notifyDataSetChanged();

    }

    void getCourseInfo(String courseName){
        sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (coursecode varchar, coursetitle varchar)");
        Cursor c = sqLiteDatabase.rawQuery("select * from CourseList",null);


        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {

                String str = c.getString(c.getColumnIndex("coursecode")) + " " +
                        c.getString(c.getColumnIndex("coursetitle"));

                if(courseName.equals(str)){
                    courseCode = c.getString(c.getColumnIndex("coursecode"));
                    courseTitle = c.getString(c.getColumnIndex("coursetitle"));
                    break;
                }
                c.moveToNext();
            }
        }
        c.close();
    }


    void getStudentGroupInfo(String groupName){
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS StudentGroupList (groupname varchar PRIMARY KEY, session varchar, batch varchar) ");
        Cursor c = sqLiteDatabase.rawQuery("select * from StudentGroupList",null);


        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String str = c.getString(c.getColumnIndex("groupname")) + " " +
                        c.getString(c.getColumnIndex("session")) + " " +
                        c.getString(c.getColumnIndex("batch"));

                if(groupName.equals(str)){
                    studentGroupName = c.getString(c.getColumnIndex("groupname"));
                    session = c.getString(c.getColumnIndex("session"));
                    batch = c.getString(c.getColumnIndex("batch"));
                    break;
                }
                c.moveToNext();
            }
        }
        c.close();
    }

    public void makeSheetButtonPressed(View view){

        if(!courseSelected){
            ((TextView)findViewById(R.id.textView_warning2)).setText("Course not selected");
        }
        else if(!studentGroupSelected){
            ((TextView)findViewById(R.id.textView_warning2)).setText("Student group not selected");
        }
        else {
            String sheetName = groupName.replaceAll(" ","_")  + "_" +  courseName.replaceAll(" ","_");
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AttendanceSheetList (coursecode varchar, coursetitle varchar, groupname varchar, session varchar, batch varchar, sheetname varchar primary key) ");

            String sql = "SELECT sheetname FROM Attendancesheetlist WHERE sheetname = "+"\""+sheetName+"\"";
            Cursor c = sqLiteDatabase.rawQuery(sql, null);
            if(c.getCount()>0){
                ((TextView)findViewById(R.id.textView_warning2)).setText("Sheet already exist");
                c.close();
            }
            else {
                ContentValues values = new ContentValues();
                values.put("coursecode",courseCode);
                values.put("coursetitle",courseTitle);
                values.put("groupname",studentGroupName);
                values.put("session",session);
                values.put("batch",batch);
                values.put("sheetname",sheetName);
                sqLiteDatabase.insert("AttendanceSheetList",null,values);


                String s1 = "CREATE TABLE \""+sheetName+"\" (dateTime varchar)";
                sqLiteDatabase.execSQL(s1);
                groupName = groupName.replaceAll(" ","_");
                s1 = "Select id from " + groupName + " ORDER BY id asc";
                c = sqLiteDatabase.rawQuery(s1, null);


                if (c.moveToFirst()) {
                    while (!c.isAfterLast()) {
                        String id = c.getString(c.getColumnIndex("id"));
                        //s1 = "insert into " + sheetName + " " + c.getString(c.getColumnIndex("id"));
                        s1 = "ALTER TABLE "+sheetName+" ADD COLUMN id"+ id +" INTEGER DEFAULT 0";
                        sqLiteDatabase.execSQL(s1);
                        c.moveToNext();
                    }
                }
                c.close();
                sqLiteDatabase.close();
                finish();
            }
        }

    }

}
