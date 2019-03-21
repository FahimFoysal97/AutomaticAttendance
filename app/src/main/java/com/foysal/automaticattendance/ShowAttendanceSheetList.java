package com.foysal.automaticattendance;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ShowAttendanceSheetList extends AppCompatActivity {

    ListView listView;
    ArrayList<String> list = new ArrayList<>();
    ArrayList<String> sheetList = new ArrayList<>();
    ArrayList<String> groupList = new ArrayList<>();
    ArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attendance);
        listView = findViewById(R.id.listView_sheetList_showAttendanceSheetlist);
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        setData();

        listView.setOnItemClickListener((parent, view, position, id) -> showAttendance(position));

    }

    void setData(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AttendanceSheetList (courseCode varchar, courseTitle varchar, groupName varchar, session varchar, batch varchar, sheetName varchar primary key) ");

        String str = "Select * from AttendanceSheetList";
        Cursor c = sqLiteDatabase.rawQuery(str, null);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            String courseTitle, courseCode, groupName, session, batch;
            try {
                courseTitle = c.getString(c.getColumnIndex("courseTitle"));
                courseCode = c.getString(c.getColumnIndex("courseCode"));
                groupName = c.getString(c.getColumnIndex("groupName"));
                session = c.getString(c.getColumnIndex("session"));
                batch = c.getString(c.getColumnIndex("batch"));
                String s = "Group Name : " + groupName + "\n" +
                        "Session : " + session + "\n" +
                        "Batch : " + batch + "\n" +
                        "Course Code : " + courseCode + "\n" +
                        "Course Title : " + courseTitle;
                list.add(s);
                adapter.notifyDataSetChanged();
                sheetList.add(c.getString(c.getColumnIndex("sheetName")));
                String t = groupName.replaceAll(" ", "_") + "_" +
                        session.replaceAll(" ", "_") + "_" +
                        batch.replaceAll(" ", "_");
                groupList.add(t);

            } catch (CursorIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            c.moveToNext();
        }
        c.close();
        sqLiteDatabase.close();
    }

    void showAttendance(int i){
        Intent intent = new Intent(this,AttendanceSheet.class);
        intent.putExtra("sheetName",sheetList.get(i));
        intent.putExtra("groupName",groupList.get(i));
        startActivity(intent);
    }
}
