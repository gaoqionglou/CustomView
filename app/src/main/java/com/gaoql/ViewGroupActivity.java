package com.gaoql;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.gaoql.customview.CircleButton;
import com.gaoql.customview.CustomPagerIndicator;
import com.gaoql.customview.SlidingViewPager;


public class ViewGroupActivity extends AppCompatActivity implements View.OnTouchListener,View.OnClickListener{
    public static final String TAG="GAOVG-ViewGroupActivity";
    private CircleButton btn1,btn2,btn3;
    private CustomPagerIndicator customPagerIndicator;
    private SlidingViewPager slidingViewPager;
    private ImageView iv1,iv2,iv3;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==0){
                customPagerIndicator.showTheFirst();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewgroup);
        customPagerIndicator =(CustomPagerIndicator)findViewById(R.id.customviewgroup);
        slidingViewPager = (SlidingViewPager)findViewById(R.id.slidingViewGroup);
        btn1= (CircleButton)findViewById(R.id.btn1);
        btn2= (CircleButton)findViewById(R.id.btn2);
        btn3= (CircleButton)findViewById(R.id.btn3);
        iv1= (ImageView)findViewById(R.id.iv1);
        iv2= (ImageView)findViewById(R.id.iv2);
        iv3= (ImageView)findViewById(R.id.iv3);
        slidingViewPager.setIndicator(customPagerIndicator);
        customPagerIndicator.setAttachView(slidingViewPager);
        btn1.setOnTouchListener(this);
        btn2.setOnTouchListener(this);
        btn3.setOnTouchListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        iv1.setOnTouchListener(this);
        iv2.setOnTouchListener(this);
        iv3.setOnTouchListener(this);
        iv1.setOnClickListener(this);
        iv2.setOnClickListener(this);
        iv3.setOnClickListener(this);
        handler.sendEmptyMessageDelayed(0,200);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        switch (id){
            case R.id.btn1:
//                customViewGroup.startTranslteAnimaton();
                Log.e(TAG,"btn 1 onTouch");
                break;
            case R.id.btn2:
//                customViewGroup.startTranslteAnimaton();
                Log.e(TAG,"btn 2 onTouch");
                break;
            case R.id.btn3:
//                customViewGroup.startTranslteAnimaton();
                Log.e(TAG,"btn 3 onTouch");
                break;
            case R.id.iv1:
                Log.e(TAG,"iv1 onTouch");
                break;
            case R.id.iv2:
                Log.e(TAG,"iv2 onTouch..");
                break;
            case R.id.iv3:
                Log.e(TAG,"iv3 onTouch");
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
            case R.id.iv1:
                Log.e(TAG,"iv1 onClick");
                break;
            case R.id.iv2:
                Log.e(TAG,"iv2 onClick");
                break;
            case R.id.iv3:
                Log.e(TAG,"iv3 onClick");
                break;
        }
    }
}
