package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gaoql.customview.MartixViewTest2;


public class MartixActivity extends AppCompatActivity {

    public static final String TAG="GAO";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_martixtest);
        MartixViewTest2 m = (MartixViewTest2)findViewById(R.id.re);
        int[] location = new int[2];
        m.getLocationInWindow(location);
        Log.i(TAG,location[0]+","+location[1]);
    }

}
