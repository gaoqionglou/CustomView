package com.gaoql.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by gql on 2016/10/20.
 */

public class PipeView extends View {

    private int[] mColors = {0xFFCCFF00, 0xFF6495ED, 0xFFE32636, 0xFF800000, 0xFF808000, 0xFFFF8C69, 0xFF808080,
            0xFFE6B800, 0xFF7CFC00};
    //起始角度
    private float mStartAngle = 0;
    //数据
    private ArrayList<PipeData> mPipeDatas;
    //宽 高
    private int mWidth,mHeight;
    //画笔
    private Paint mPaint = new Paint();

    public PipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setStyle(Paint.Style.FILL);//设置填充
        mPaint.setAntiAlias(true);//开启抗锯齿，边缘更圆滑
    }

    public PipeView(Context context) {
        this(context,null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth =w;
        mHeight=h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mPipeDatas==null){
            return;
        }
        //当前角度=起始角度-0
        float currentAngle = mStartAngle;
        //把画布的起始位置移动到中心
        canvas.translate(mWidth/2,mHeight/2);
        //确定圆的半径
        float r =(float)(Math.min(mWidth,mHeight)/2*0.8);
        RectF rectF = new RectF(-r,-r,r,r);
        for(PipeData data:mPipeDatas){
            float angle = data.getAngle();
            int color = data.getColor();
            mPaint.setColor(color);
            canvas.drawArc(rectF,currentAngle,angle,true,mPaint);
            currentAngle+=angle;
        }
    }
    //设置起始角度
    public void setStartAngle(float mStartAngle){
        this.mStartAngle = mStartAngle;
        invalidate();
    }
    //设置数据集合
    public void setPipeDatas(ArrayList<PipeData> mPipeDatas){
        this.mPipeDatas=mPipeDatas;
        initData();
        invalidate();
    }
    private void initData(){
        if(mPipeDatas.isEmpty()){
            return;
        }
        //先计算出数值总和，进而计算出百分比，最后得出扇形的角度
        float sum = 0;
        for (int i=0;i<mPipeDatas.size();i++){
            PipeData data = mPipeDatas.get(i);
            sum +=data.getValue();
            int j = i%mColors.length;
            data.setColor(mColors[j]);
        }
        for(int m=0;m<mPipeDatas.size();m++){
            PipeData data = mPipeDatas.get(m);
            float p = data.getValue()/sum;
            data.setPercentage(p);
            float angle=p*360;
            data.setAngle(angle);
        }

    }
}
