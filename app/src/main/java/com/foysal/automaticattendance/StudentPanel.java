package com.foysal.automaticattendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class StudentPanel extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_panel);
    }

    public void goToRegisterPanelButtonClicked(View view){
        Intent intent = new Intent(getApplicationContext(),RegisterPanel.class);
        startActivity(intent);
    }
}
