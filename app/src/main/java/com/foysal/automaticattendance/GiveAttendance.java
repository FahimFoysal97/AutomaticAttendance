package com.foysal.automaticattendance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
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
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_attendance);
        courses.clear();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,courses);
        listView = findViewById(R.id.listView_give_attendance_panel_courselist);
        listView.setAdapter(adapter);
        showCourseList();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(),GivingAttendance.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    void showCourseList(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("StudentPanel",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (courseName varchar PRIMARY KEY, name varchar, id varchar) ");
        Cursor c = sqLiteDatabase.rawQuery("Select * from CourseList",null);

        c.moveToFirst();
        while( !c.isAfterLast()){
            try{

                String id =c.getString(c.getColumnIndex("id"));
                String name = c.getString(c.getColumnIndex("name"));
                String cName = c.getString(c.getColumnIndex("courseName"));
                addCourse(cName,name,id);
                listView.post(()->adapter.notifyDataSetChanged());
                c.moveToNext();
            } catch (CursorIndexOutOfBoundsException e){
                break;
            }

        }
        c.close();
    }
    public void addCourse(String courseName, String name, String id){

        String str = "Course name : " + courseName + "\nName : " +name+"\n"+"ID : " + id;
        courses.add(str);

    }
}
