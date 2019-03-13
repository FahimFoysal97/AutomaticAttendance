package com.foysal.automaticattendance;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class TakeAttendance extends AppCompatActivity {

    ListView listView;
    ArrayList<String> list = new ArrayList<>();
    ArrayList<String> sheetList = new ArrayList<>();
    ArrayList<String> groupList = new ArrayList<>();
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);
        listView = findViewById(R.id.listView_take_attendance);
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        showList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getSheetNameAndGroupName(position);
            }
        });
    }

    void showList(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AttendanceSheetList (coursecode varchar, coursetitle varchar, groupname varchar, session varchar, batch varchar, sheetName varchar primary key) ");

        String str = "Select * from AttendanceSheetList";
        Cursor c = sqLiteDatabase.rawQuery(str,null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String courseTitle,courseCode,groupName,session,batch;
                courseTitle = c.getString(c.getColumnIndex("courseTitle"));
                courseCode = c.getString(c.getColumnIndex("courseCode"));
                groupName = c.getString(c.getColumnIndex("groupName"));
                session = c.getString(c.getColumnIndex("session"));
                batch = c.getString(c.getColumnIndex("batch"));

                String s = "Course Code : " + courseCode +"\n" +
                        "Course Title : " + courseTitle + "\n" +
                        "Group Name : " + groupName + "\n" +
                        "Session : " + session + "\n" +
                        "Batch : " + batch;
                list.add(s);
                adapter.notifyDataSetChanged();
                sheetList.add(c.getString(c.getColumnIndex("sheetName")));
                groupList.add(c.getString(c.getColumnIndex("groupName")));
                //s1 = "insert into " + sheetName + " " + c.getString(c.getColumnIndex("id"));
                //s1 = "ALTER TABLE "+sheetName+" ADD COLUMN "+ id +" INTEGER DEFAULT 0";
                //sqLiteDatabase.execSQL(s1);
                c.moveToNext();
            }
            c.close();
        }

    }

    void getSheetNameAndGroupName(int i){

        Intent intent = new Intent(this,TakingAttendance.class);
        intent.putExtra("sheetName",sheetList.get(i));
        intent.putExtra("groupName",groupList.get(i));
        startActivity(intent);
    }

}
