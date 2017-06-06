package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.gaoql.customview.TimeScheduleView;

public class StatusActivity extends AppCompatActivity implements View.OnTouchListener,View.OnClickListener{
    public static final String TAG="StatusActivity";
    private Button addBtn,minBtn;
    private TimeScheduleView timeScheduleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
         timeScheduleView = (TimeScheduleView)findViewById(R.id.timeScheduleView);
        addBtn =(Button)findViewById(R.id.addBtn);
        minBtn=(Button)findViewById(R.id.minBtn);
        addBtn.setOnClickListener(this);
        minBtn.setOnClickListener(this);

    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.addBtn){
            float current = timeScheduleView.getCurrent();
            timeScheduleView.setCurrent(current+30);
            timeScheduleView.startProgressAnimaton();
            timeScheduleView.startFlyingAnimaton();
        }
        if(v.getId()==R.id.minBtn){
            float current = timeScheduleView.getCurrent();
            timeScheduleView.setCurrent(current-20);
            timeScheduleView.startProgressAnimaton();
            timeScheduleView.startFlyingAnimaton();
        }
    }
}
