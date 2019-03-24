package com.foysal.automaticattendance;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class StudentListGroup extends AppCompatActivity {

    static ArrayAdapter arrayAdapter;
    static ArrayList<String> groupNames = new ArrayList<>();
    static ArrayList<String> groupNames2 = new ArrayList<>();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list_group);
        listView = findViewById(R.id.listView_StudentList);

        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,groupNames);
        listView.setAdapter(arrayAdapter);
        showList();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(),StudentList.class);
            intent.putExtra("groupName",groupNames2.get(position));
            startActivity(intent);
        });

    }

    public void addStudentGroupButtonClicked(View view){
        Intent intent = new Intent(getApplicationContext(),AddStudentGroup.class);
        startActivity(intent);
        //showList();
    }

    void showList(){
        groupNames.clear();
        arrayAdapter.notifyDataSetChanged();
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS StudentGroupList (groupName varchar PRIMARY KEY, session varchar, batch varchar) ");
        Cursor c = sqLiteDatabase.rawQuery("select * from StudentGroupList",null);
        //int i = c.getColumnIndex("groupname");
        c.moveToFirst();
        while(!c.isAfterLast()){
            //System.out.println(i);
            try{
                groupNames2.add(c.getString(c.getColumnIndex("groupName")).replaceAll(" ","_") + "_" +
                        c.getString(c.getColumnIndex("session")).replaceAll(" ","_") + "_" +
                        c.getString(c.getColumnIndex("batch")));
                String str = "Group name : "+c.getString(c.getColumnIndex("groupName")) + "\nSession : " +c.getString(c.getColumnIndex("session")) + "\nBatch : "+c.getString(c.getColumnIndex("batch"));
                groupNames.add(str);
                c.moveToNext();
            } catch (CursorIndexOutOfBoundsException e){
                break;
            }

        }
        listView.post(()->
        {
            listView.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
        });
        c.close();
        sqLiteDatabase.close();
    }
}
