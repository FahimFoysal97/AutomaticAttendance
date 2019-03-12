package com.foysal.automaticattendance;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class TeacherPanelCourseList extends AppCompatActivity {

    static ListView listView;
    static ArrayAdapter adapter;
    static ArrayList<String> courses = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_panel_course_list);
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,courses);
        listView = findViewById(R.id.listView_courseList_teacher);
        listView.setAdapter(adapter);
        showCourseList();
    }

    public static void addCourse(String courseTitle, String courseCode, String session, String batch){


        //courses.add(new ItemListCourse("Course Code : " + courseCode + ", Course Title : " +courseTitle, "Session : " + session + ", Batch : " + batch));
        String str = "Course Code : " + courseCode + "\nCourse Title : " +courseTitle+"\n"+"Session : " + session + "\nBatch : " + batch;
        courses.add(str);
        adapter.notifyDataSetChanged();

        //SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
    }


    public void addCourseButton(View view){
        Intent intent = new Intent(getApplicationContext(),TeacherPanelAddCourse.class);
        startActivity(intent);
    }

    void showCourseList(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (coursetitle varchar, coursecode varchar, session varchar, batch vrachar)");
        Cursor c = sqLiteDatabase.rawQuery("Select * from courselist",null);

        c.moveToFirst();
        while(c!=null && !c.isAfterLast()){
            try{
                String str[] = {
                        c.getString(c.getColumnIndex("coursetitle")),
                        c.getString(c.getColumnIndex("coursecode")),
                        c.getString(c.getColumnIndex("session")),
                        c.getString(c.getColumnIndex("batch"))
                };

                addCourse(str[0],str[1],str[2],str[3]);
                c.moveToNext();
            } catch (CursorIndexOutOfBoundsException e){
                break;
            }

        }
        c.close();
    }
}
