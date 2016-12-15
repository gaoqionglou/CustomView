package com.gaoql.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by admin on 2016/11/1.
 */

public class BezierView extends View {
    public static final String TAG="GAO";
    private float centerX,centerY;
    private PointF start,end,control1,control2;
    private Paint mPaint ;
    private int pointNum;
    public BezierView(Context context) {
        super(context,null);
    }

    public BezierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        start = new PointF(0,0);
        end = new PointF(0,0);
        control1 = new PointF(0,0);
        control2 = new PointF(0,0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX=w/2;
        centerY=h/2;
        start.x=centerX+100;
        start.y=centerY;
        end.x=centerX-100;
        end.y=centerY+50;
        control2.x=w;
    }

    public void setPointNum(int pointNum){
this.pointNum=pointNum;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path path = new Path();
        //描点
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(20);
        canvas.drawPoint(start.x,start.y,mPaint);
        canvas.drawPoint(end.x,end.y,mPaint);
        canvas.drawPoint(control1.x,control1.y,mPaint);
        canvas.drawPoint(control2.x,control2.y,mPaint);
        //描线
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(5);
        canvas.drawLine(start.x,start.y,control1.x,control1.y,mPaint);
        canvas.drawLine(control1.x,control1.y,control2.x,control2.y,mPaint);
        canvas.drawLine(end.x,end.y,control2.x,control2.y,mPaint);
        //描贝塞尔曲线
        path.moveTo(start.x,start.y);
//        path.quadTo(control.x,control.y,end.x,end.y);
        path.cubicTo(control1.x,control1.y,control2.x,control2.y,end.x,end.y);
        mPaint.setColor(Color.RED);
        canvas.drawPath(path,mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(pointNum==1){
            control1.x=event.getX();
            control1.y=event.getY();
            invalidate();
        }else {
            control2.x = event.getX();
            control2.y = event.getY();
            invalidate();
        }
        Log.i(TAG,"event action "+event.getAction());
        return super.onTouchEvent(event);

    }
}
