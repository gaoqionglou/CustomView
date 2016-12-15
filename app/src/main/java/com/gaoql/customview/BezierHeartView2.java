package com.gaoql.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.Arrays;
import java.util.List;


public class BezierHeartView2 extends View {
    public static final String TAG="BezierHeartView2";
    private static final float C = 0.551915024494f;     // 一个常量，用来计算绘制圆形贝塞尔曲线控制点的位置
    private float radius=100;
    private float diff = radius*C;        // 圆形的控制点与数据点的差值
    private float firstCenterX,firstCenterY;
    private Paint linePaint;
    private Paint paint;
    private Path path ;
    private float width,height;
    private PointF[] dataPoint = new PointF[4];
    private PointF[] firstDataPoint = new PointF[4];
    private PointF[] ctrlPoint = new PointF[8];
    private TranslteAnimaton move = new TranslteAnimaton();
    private float mInterpolatedTime=0f;
    private float k ;

    float finalX_3;
    float kMiddle_3;
    float bMiddle_3;
    float d1_3;
    float k0_3;
    float b0_3;
    float k2_3;
    float b2_3;

    float finalX_4;
    float kMiddle_4;
    float bMiddle_4;
    float d1_4;
    float k0_4;
    float b0_4;
    float k2_4;
    float b2_4;
    public BezierHeartView2(Context context) {
        this(context,null);
    }

    public BezierHeartView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width=w;
        height=h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
         k = (width - 2*radius);
        float resultX = firstCenterX+k*mInterpolatedTime;
        dataPoint = initDataPoint(resultX,firstCenterY);
        initCtrlPoint(dataPoint);
        List<PointF> ps = Arrays.asList(dataPoint);
        List<PointF> cs = Arrays.asList(ctrlPoint);
//        drawPoint(canvas);
        drawCubicBezier(canvas);
    }

    private void initData(){
        /** 初始化中心点 */
        initCenterPoint(radius,radius);
        dataPoint = initDataPoint(firstCenterX,firstCenterY);
        initCtrlPoint(dataPoint);

        linePaint = new Paint();
        paint = new Paint();
        path = new Path();

    }

    private void initCenterPoint(float x,float y){
        firstCenterY = y;
        firstCenterX = x;
    }

    private PointF[] initDataPoint(float centerX,float centerY){
        for(int i=0;i<dataPoint.length;i++){
            if(dataPoint[i]==null) {
                dataPoint[i] = new PointF(0, 0);
                firstDataPoint[i] = new PointF(0, 0);
            }
        }
        float bendingDistance = width/4f;
        float s1 = 0.3f;
        float s2 = 0.6f;
        float s3 = 0.9f;

        if(mInterpolatedTime<=0){
            dataPoint[0].x=centerX-radius;
            dataPoint[0].y=centerY;

            dataPoint[1].x=centerX;
            dataPoint[1].y=centerY-radius;
            dataPoint[2].x=centerX+radius;
            dataPoint[2].y=centerY;

            dataPoint[3].x=centerX;
            dataPoint[3].y=centerY+radius;

            firstDataPoint[0].x=centerX-radius;
            firstDataPoint[0].y=centerY;

            firstDataPoint[1].x=centerX;
            firstDataPoint[1].y=centerY-radius;
            firstDataPoint[2].x=centerX+radius;
            firstDataPoint[2].y=centerY;

            firstDataPoint[3].x=centerX;
            firstDataPoint[3].y=centerY+radius;
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"1-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"1-dataPoint "+ds.toString()  );
        }else if(mInterpolatedTime>0&&mInterpolatedTime<=s1){
            dataPoint[0].x= firstDataPoint[0].x;
            dataPoint[0].y = firstDataPoint[0].y;
            dataPoint[1].x = firstDataPoint[1].x;
            dataPoint[1].y = firstDataPoint[1].y;
            dataPoint[2].x = firstDataPoint[2].x+bendingDistance/s1*mInterpolatedTime;
            dataPoint[2].y = firstDataPoint[2].y;
            dataPoint[3].x = firstDataPoint[3].x;
            dataPoint[3].y = firstDataPoint[3].y;
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"2-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"2-dataPoint "+ds.toString()  );
        }else if(mInterpolatedTime>s1&&mInterpolatedTime<=s2){
             finalX_3= firstCenterX+k*s2;
             kMiddle_3 = (finalX_3-firstDataPoint[1].x)/(s2-s1);
             bMiddle_3 = firstDataPoint[1].x-(finalX_3-firstDataPoint[1].x)*s1/(s2-s1);
             d1_3 = firstDataPoint[2].x+bendingDistance;
             k0_3 = (finalX_3-radius-bendingDistance)/(s2-s1);
             b0_3 = firstDataPoint[0].x-(finalX_3-radius-bendingDistance)*s1/(s2-s1);
             k2_3 = (finalX_3+radius- d1_3)/(s2-s1);
             b2_3 = d1_3 - s1*(finalX_3+radius-d1_3)/(s2-s1);

            dataPoint[0].x= k0_3*mInterpolatedTime+b0_3;
            dataPoint[0].y = centerY;
            dataPoint[1].x = kMiddle_3*mInterpolatedTime+bMiddle_3;
            dataPoint[1].y = centerY-radius;
            dataPoint[2].x = k2_3*mInterpolatedTime+b2_3;
            dataPoint[2].y = centerY;
            dataPoint[3].x = kMiddle_3*mInterpolatedTime+bMiddle_3;
            dataPoint[3].y = centerY+radius;
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"3-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
            Log.i(TAG,"3-dataPoint "+ds.toString()  );
        }else if(mInterpolatedTime>s2&&mInterpolatedTime<=s3){
            finalX_4 = firstCenterX+k*s3;
            kMiddle_4 = (finalX_4-kMiddle_3*s2-bMiddle_3)/(s3-s2);
            bMiddle_4 = kMiddle_3*s2+bMiddle_3-(finalX_4-kMiddle_3*s2-bMiddle_3)*s2/(s3-s2);
            k0_4 = (finalX_4-radius-k0_3*s2-b0_3)/(s3-s2);
            b0_4 = k0_3*s2+b0_3-(finalX_4-radius-k0_3*s2-b0_3)*s2/(s3-s2);
            k2_4 = (finalX_4+radius- k2_3*s2-b2_3)/(s3-s2);
            b2_4 = k2_3*s2+b2_3 - s2*(finalX_4+radius- k2_3*s2-b2_3)/(s3-s2);


            dataPoint[0].x = k0_4*mInterpolatedTime+b0_4;
            dataPoint[0].y = centerY;
            dataPoint[1].x = kMiddle_4*mInterpolatedTime+bMiddle_4;
            dataPoint[1].y = centerY-radius;
            dataPoint[2].x = k2_4*mInterpolatedTime+b2_4;
            dataPoint[2].y = centerY;
            dataPoint[3].x = kMiddle_4*mInterpolatedTime+bMiddle_4;
            dataPoint[3].y = centerY+radius;
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"4-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"4-dataPoint "+ds.toString()  );
        }else{
            dataPoint[0].x=centerX-radius;
            dataPoint[0].y=centerY;

            dataPoint[1].x=centerX;
            dataPoint[1].y=centerY-radius;
            dataPoint[2].x=centerX+radius;
            dataPoint[2].y=centerY;

            dataPoint[3].x=centerX;
            dataPoint[3].y=centerY+radius;
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"5-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"5-dataPoint "+ds.toString()  );
        }
        return dataPoint;
    }

    private PointF[] initCtrlPoint(PointF[] dataPoint){
        for(int i=0;i<ctrlPoint.length;i++){
            ctrlPoint[i] = new PointF(0,0);
        }
        ctrlPoint[0].x=dataPoint[0].x;
        ctrlPoint[0].y=dataPoint[0].y-diff;

        ctrlPoint[1].x=dataPoint[1].x-diff;
        ctrlPoint[1].y=dataPoint[1].y;
        ctrlPoint[2].x=dataPoint[1].x+diff;
        ctrlPoint[2].y=dataPoint[1].y;

        ctrlPoint[3].x=dataPoint[2].x;
        ctrlPoint[3].y=dataPoint[2].y-diff;
        ctrlPoint[4].y=dataPoint[2].y+diff;
        ctrlPoint[4].x=dataPoint[2].x;

        ctrlPoint[5].y=dataPoint[3].y;
        ctrlPoint[5].x=dataPoint[3].x+diff;
        ctrlPoint[6].y=dataPoint[3].y;
        ctrlPoint[6].x=dataPoint[3].x-diff;

        ctrlPoint[7].x=dataPoint[0].x;
        ctrlPoint[7].y=dataPoint[0].y+diff;
        return ctrlPoint;

    }

    private void drawPoint(Canvas canvas){
        //绘制控制点
        linePaint.setColor(Color.BLACK);
        linePaint.setTextSize(60);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(20);
        for(int i=0;i<ctrlPoint.length;i++){
            canvas.drawPoint(ctrlPoint[i].x,ctrlPoint[i].y,linePaint);
        }
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(5);

        canvas.drawLine(dataPoint[0].x,dataPoint[0].y,ctrlPoint[0].x,ctrlPoint[0].y,linePaint);
        canvas.drawLine(dataPoint[0].x,dataPoint[0].y,ctrlPoint[7].x,ctrlPoint[7].y,linePaint);

        canvas.drawLine(dataPoint[1].x,dataPoint[1].y,ctrlPoint[1].x,ctrlPoint[1].y,linePaint);
        canvas.drawLine(dataPoint[1].x,dataPoint[1].y,ctrlPoint[2].x,ctrlPoint[2].y,linePaint);

        canvas.drawLine(dataPoint[2].x,dataPoint[2].y,ctrlPoint[3].x,ctrlPoint[3].y,linePaint);
        canvas.drawLine(dataPoint[2].x,dataPoint[2].y,ctrlPoint[4].x,ctrlPoint[4].y,linePaint);

        canvas.drawLine(dataPoint[3].x,dataPoint[3].y,ctrlPoint[5].x,ctrlPoint[5].y,linePaint);
        canvas.drawLine(dataPoint[3].x,dataPoint[3].y,ctrlPoint[6].x,ctrlPoint[6].y,linePaint);
    }

    private void drawCubicBezier(Canvas canvas){
        /** 清除Path中的内容
         reset不保留内部数据结构，但会保留FillType.
         rewind会保留内部的数据结构，但不保留FillType */
        path.rewind();
        path.moveTo(dataPoint[0].x,dataPoint[0].y);

        path.cubicTo(ctrlPoint[0].x,ctrlPoint[0].y,ctrlPoint[1].x,ctrlPoint[1].y,dataPoint[1].x,dataPoint[1].y);
        path.cubicTo(ctrlPoint[2].x,ctrlPoint[2].y,ctrlPoint[3].x,ctrlPoint[3].y,dataPoint[2].x,dataPoint[2].y);
        path.cubicTo(ctrlPoint[4].x,ctrlPoint[4].y,ctrlPoint[5].x,ctrlPoint[5].y,dataPoint[3].x,dataPoint[3].y);
        path.cubicTo(ctrlPoint[6].x,ctrlPoint[6].y,ctrlPoint[7].x,ctrlPoint[7].y,dataPoint[0].x,dataPoint[0].y);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path,paint);

    }
    private class TranslteAnimaton extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
//            Log.i(TAG,"interpolatedTime : "+interpolatedTime);
            mInterpolatedTime=interpolatedTime;
            invalidate();
        }
    }

    /**
     *
     */
    public void  startTranslteAnimaton(){
        path.reset();
        mInterpolatedTime = 0;
        move.setDuration(3000);
        move.setInterpolator(new AccelerateDecelerateInterpolator());
        startAnimation(move);
    }

    public void setRadius(float radius){
        this.radius=radius;
    }
    public float getRadius(){
        return this.radius;
    }
}
