package com.foysal.automaticattendance;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class AttendanceSheet extends AppCompatActivity {


    String sheetName,groupName;
    ArrayList<String> studentArrayList = new ArrayList<>();
    ArrayList<String> studentName = new ArrayList<>();
    ArrayList<String> studentId = new ArrayList<>();
    ArrayAdapter adapter;
    ListView studentListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_sheet);

        sheetName = getIntent().getExtras().getString("sheetName");
        groupName = getIntent().getExtras().getString("groupName");
        adapter = new ArrayAdapter(AttendanceSheet.this.getApplicationContext(), android.R.layout.simple_list_item_1, studentArrayList);
        studentListView = findViewById(R.id.listView_AttendanceSheetStudentList);
        studentListView.setAdapter(adapter);
        setData();
    }

    void setData(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        String str = "Select * from " + groupName + " order by id";
        Cursor c = sqLiteDatabase.rawQuery(str,null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                studentName.add(c.getString(c.getColumnIndex("name")));
                studentId.add(c.getString(c.getColumnIndex("id")));
                //studentDeviceAddress.add(c.getString(c.getColumnIndex("deviceAddress")));
                //sqLiteDatabase.execSQL(s1);
                String t = getTotalAttendance(c.getString(c.getColumnIndex("id")));
                String str1 = "ID : " + c.getString(c.getColumnIndex("id"))+
                        "\nName : " + c.getString(c.getColumnIndex("name"))+
                        "\nPresents : " +t;
                studentArrayList.add(str1);
                System.out.println(studentArrayList);

                c.moveToNext();
            }
            c.close();
            //adapter2.notifyDataSetChanged();
            /*studentListView2.post(() -> {
                //studentListView2.setAdapter(adapter2);
                adapter2.notifyDataSetChanged();
            });*/
        }
        //((TextView)findViewById(R.id.textView_absentNumber_rollCall)).setText("Absent : "+studentArrayList.size());
        //studentListView.setAdapter(adapter);
        studentListView.post(()->adapter.notifyDataSetChanged());
    }

    String getTotalAttendance(String id){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        String str = "Select id"+id+" from " + sheetName ;
        Cursor c = sqLiteDatabase.rawQuery(str,null);
        ((TextView)findViewById(R.id.textView_AttendanceSheet_totalClass)).setText("Total classes : " + c.getCount());
        String str1 = "Select id"+id+" from " + sheetName +" where id"+id+" = 1" ;
        Cursor c1 = sqLiteDatabase.rawQuery(str1,null);
        return String.valueOf(c1.getCount());
    }
}
