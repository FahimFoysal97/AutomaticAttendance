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

public class StudentListGroup extends AppCompatActivity {

    static ArrayAdapter arrayAdapter;
    static ArrayList<String> groupNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_group);

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,groupNames);
        ((ListView)findViewById(R.id.listView_StudentList)).setAdapter(arrayAdapter);
        showList();
        arrayAdapter.notifyDataSetChanged();
    }

    public void addStudentGroupButtonClicked(View view){
        Intent intent = new Intent(getApplicationContext(),AddStudentGroup.class);
        startActivity(intent);
        showList();
    }

    void showList(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("AutomaticAttendance",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS StudentGroupList (groupname varchar PRIMARY KEY, session varchar) ");
        Cursor c = sqLiteDatabase.rawQuery("select groupname from StudentGroupList",null);
        int i = c.getColumnIndex("groupname");
        c.moveToFirst();
        while(c!=null){
            //System.out.println(i);
            try{
                groupNames.add(c.getString(i));
                c.moveToNext();
            } catch (CursorIndexOutOfBoundsException e){
                break;
            }

        }
        c.close();
    }
}
