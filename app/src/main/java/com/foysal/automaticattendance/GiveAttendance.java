package com.foysal.automaticattendance;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class GiveAttendance extends AppCompatActivity {

    static ListView listView;
    static ArrayAdapter adapter;
    static ArrayList<String> courses = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_attendance);
        courses.clear();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,courses);
        listView = findViewById(R.id.listView_give_attendance_panel_courselist);
        listView.setAdapter(adapter);
        showCourseList();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),GivingAttendance.class);
                startActivity(intent);
            }
        });
    }

    void showCourseList(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("StudentPanel",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (coursename varchar PRIMARY KEY, name varchar, id varchar) ");
        Cursor c = sqLiteDatabase.rawQuery("Select * from courselist",null);

        c.moveToFirst();
        while(c!=null && !c.isAfterLast()){
            try{
                String str[] = {
                        c.getString(c.getColumnIndex("coursename")),
                        c.getString(c.getColumnIndex("name")),
                        c.getString(c.getColumnIndex("id")),
                };

                addCourse(str[0],str[1],str[2]);
                c.moveToNext();
            } catch (CursorIndexOutOfBoundsException e){
                break;
            }

        }
        c.close();
    }
    public static void addCourse(String coursename, String name, String id){

        String str = "Course name : " + coursename + "\nName : " +name+"\n"+"ID : " + id;
        courses.add(str);
        adapter.notifyDataSetChanged();

    }
}
