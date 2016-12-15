package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gaoql.customview.RendarView;


public class RendarActivity extends AppCompatActivity {

    public static final String TAG="GAO";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RendarView rv = new RendarView(this);
        setContentView(rv);

    }

}
