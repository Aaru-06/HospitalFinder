package com.arumugam.hospitalfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class HospitalView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_view);

        String s=getIntent().getStringExtra("msg");
        TextView t =findViewById(R.id.txt);
        t.setText(s);
    }
}
