package com.gaoql;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.gaoql.customview.CustomRelativeLauout;
import com.gaoql.customview.RadianViewPager;

public class MagicActivity extends AppCompatActivity implements RadianViewPager.OnPagerChangeListener{
    private ImageView iv1,iv2,iv3,iv4;
    private RadianViewPager rvp;
    private CustomRelativeLauout crl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magic);
        crl = (CustomRelativeLauout) findViewById(R.id.crl);
        rvp = (RadianViewPager)findViewById(R.id.vp);
        iv1=(ImageView)findViewById(R.id.iv1);
        iv2=(ImageView)findViewById(R.id.iv2);
        iv3=(ImageView)findViewById(R.id.iv3);
        iv4=(ImageView)findViewById(R.id.iv4);
        rvp.setOnPagerChangeListener(this);
    }

    @Override
    public void pageChanged(int from, int to,float endx,float endy) {
        startAnimation(iv1);
        startAnimation(iv2);
        startAnimation(iv3);
        startAnimation(iv4);
        crl.rotate();
    }

    public void startAnimation(View v){
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f);
        ObjectAnimator.ofPropertyValuesHolder(v, pvhX, pvhY,pvhZ).setDuration(1000).start();
    }
}
