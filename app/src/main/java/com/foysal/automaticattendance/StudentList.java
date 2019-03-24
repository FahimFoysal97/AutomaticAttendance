package com.foysal.automaticattendance;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class StudentList extends AppCompatActivity {

    String groupName;
    ListView listView;
    ArrayList<String> studentsInfo = new ArrayList<>();
    ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,studentsInfo);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        listView = findViewById(R.id.listView_studentList_studentList);
        listView.setAdapter(adapter);
        groupName = getIntent().getExtras().getString("groupName");
        showList();
    }

    void showList(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        String str = "Select * from " + groupName + " order by id";
        Cursor c = sqLiteDatabase.rawQuery(str,null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String str1 = "ID : " + c.getString(c.getColumnIndex("id"))+
                        "\nName : " + c.getString(c.getColumnIndex("name")) +
                        "\nDevice Address : " + c.getString(c.getColumnIndex("deviceAddress"));
                studentsInfo.add(str1);
                c.moveToNext();
            }
            c.close();

        }

        listView.setAdapter(adapter);
        listView.post(()->adapter.notifyDataSetChanged());
    }
}
