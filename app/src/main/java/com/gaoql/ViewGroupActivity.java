package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.gaoql.customview.CircleButton;
import com.gaoql.customview.CustomViewGroup;


public class ViewGroupActivity extends AppCompatActivity implements View.OnTouchListener,View.OnClickListener{
    public static final String TAG="GAOVG-ViewGroupActivity";
    private CircleButton btn1,btn2,btn3;
    private CustomViewGroup customViewGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewgroup);
        customViewGroup=(CustomViewGroup)findViewById(R.id.customviewgroup);
        btn1= (CircleButton)findViewById(R.id.btn1);
        btn2= (CircleButton)findViewById(R.id.btn2);
        btn3= (CircleButton)findViewById(R.id.btn3);
        btn1.setOnTouchListener(this);
        btn2.setOnTouchListener(this);
        btn3.setOnTouchListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        switch (id){
            case R.id.btn1:
//                customViewGroup.startTranslteAnimaton();
//                Log.e(TAG,"btn 1 onTouch");
                break;
            case R.id.btn2:
//                customViewGroup.startTranslteAnimaton();
//                Log.e(TAG,"btn 2 onTouch");
                break;
            case R.id.btn3:
//                customViewGroup.startTranslteAnimaton();
//                Log.e(TAG,"btn 3 onTouch");
                break;

        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn1:
                Log.e(TAG,"btn 1 onClick");
                break;
            case R.id.btn2:
                Log.e(TAG,"btn 2 onClick");
                break;
            case R.id.btn3:
                Log.e(TAG,"btn 3 onClick");
                break;

        }
    }
}
