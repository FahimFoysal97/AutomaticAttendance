package com.foysal.automaticattendance;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class IndividualStudentAttendance extends AppCompatActivity {

    String sheetName,groupName,id;
    ArrayList<String> attendanceDetails = new ArrayList<>();
    ArrayAdapter adapter;
    ListView listview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_student_attendance);
        sheetName = getIntent().getExtras().getString("sheetName");
        groupName = getIntent().getExtras().getString("groupName");
        id = getIntent().getExtras().getString("id");
        listview = findViewById(R.id.listView_individualStudentAttendanceList);
        adapter = new ArrayAdapter(IndividualStudentAttendance.this.getApplicationContext(), android.R.layout.simple_list_item_1, attendanceDetails);
        listview.setAdapter(adapter);

        setData();
    }

    void setData(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        String str = "Select name from " + groupName + " where  id = " + id;
        Cursor c =sqLiteDatabase.rawQuery(str,null);
        if(c.moveToFirst() && !c.isAfterLast()){
            ((TextView)findViewById(R.id.textView_nameId)).setText("Name : " + c.getString(c.getColumnIndex("name")) + "\nID : " + id);
        }
        str = "Select dateTime,id" + id + " from " + sheetName;
        c = sqLiteDatabase.rawQuery(str,null);
        if(c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String string = "Date Time : "+c.getString(c.getColumnIndex("dateTime")) +
                        "\nAttendance : ";
                if(Integer.decode(c.getString(c.getColumnIndex("id"+id))) == 1 )string=string+"Present";
                else string=string+"Absent";
                attendanceDetails.add(string);
                adapter.notifyDataSetChanged();
                c.moveToNext();
            }
        }
    }
}
