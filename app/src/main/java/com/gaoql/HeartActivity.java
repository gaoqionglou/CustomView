package com.gaoql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.gaoql.customview.BezierHeartView2;


public class HeartActivity extends AppCompatActivity implements View.OnClickListener{
    BezierHeartView2 bezierHeartView;
    Button click;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart);
        bezierHeartView=(BezierHeartView2)findViewById(R.id.bezierHeartView);
        click=(Button)findViewById(R.id.btn);
        click.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn){
            bezierHeartView.startTranslteAnimaton();
        }
    }
}
