package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;

import com.gaoql.customview.BezierView;


public class BezierActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener{
    public static final String TAG="GAO";
    BezierView bezierView;
    RadioButton r1,r2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bezier);
        bezierView = (BezierView)findViewById(R.id.bezierview);
        r1=(RadioButton)findViewById(R.id.rd1);
        r2=(RadioButton)findViewById(R.id.rd2);
        bezierView.setOnClickListener(this);
        r1.setOnClickListener(this);
        r2.setOnClickListener(this);
        bezierView.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.bezierview){
            Log.i(TAG,"bezierview onClick");
        }
        if(v.getId()==R.id.rd1){
            bezierView.setPointNum(1);
        }
        if(v.getId()==R.id.rd2){
            bezierView.setPointNum(2);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId()==R.id.bezierview){
            Log.i(TAG,"bezierview onTouch");
            return v.onTouchEvent(event);
        }
        return false;
    }
}
