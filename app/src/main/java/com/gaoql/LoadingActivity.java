package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gaoql.customview.RotateLoadingDialog;

/**
 * Created by admin on 2017/2/19.
 */

public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout_rotateloading);
        setContentView(new RotateLoadingDialog(this));
    }
}
