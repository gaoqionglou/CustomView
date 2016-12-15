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

/**
 * Created by admin on 2016/11/2.
 */

public class BezierHeartView extends View {
    public static final String TAG="Bezier";
    private static final float C = 0.551915024494f;     // 一个常量，用来计算绘制圆形贝塞尔曲线控制点的位置
    private float centerX,centerY;
    private PointF[] data = new PointF[4];//4个点
    private PointF[] ctrl = new PointF[8];//8个控制点
    private Paint paint ;

    private float r = 100;//圆的半径
    private float diff = r*C;        // 圆形的控制点与数据点的差值

    private float duration = 1000;//变化的总时长
    private float current = 0;
    private float count = 100;
    private float piece = duration/count;
    private Path path ;
    private float mInterpolatedTime=0;
    private float mWidth;
    private float mHeight;
    TranslteAnimaton move = new TranslteAnimaton();
    public BezierHeartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public BezierHeartView(Context context) {
        this(context,null);
    }

    private void initData(){
        paint = new Paint();
        paint.setColor(Color.BLACK);
//        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(60);

        path =new Path();

        for(int i=0;i<data.length;i++){
            data[i] = new PointF(0,0);
        }
        for(int i=0;i<ctrl.length;i++){
            ctrl[i] = new PointF(0,0);
        }
        data[0].x=-r;
        data[0].y=0;
        data[1].x=0;
        data[1].y=-r;
        data[2].x=r;
        data[2].y=0;
        data[3].x=0;
        data[3].y=r;

        ctrl[0].x=data[0].x;
        ctrl[0].y=data[0].y-diff;

        ctrl[1].x=data[1].x-diff;
        ctrl[1].y=data[1].y;
        ctrl[2].x=data[1].x+diff;
        ctrl[2].y=data[1].y;

        ctrl[3].x=data[2].x;
        ctrl[3].y=data[2].y-diff;
        ctrl[4].y=data[2].y+diff;
        ctrl[4].x=data[2].x;

        ctrl[5].y=data[3].y;
        ctrl[5].x=data[3].x+diff;
        ctrl[6].y=data[3].y;
        ctrl[6].x=data[3].x-diff;

        ctrl[7].x=data[0].x;
        ctrl[7].y=data[0].y+diff;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.i(TAG,"on Layout:"+changed+","+left+","+top+","+right+","+bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i(TAG,"onSizeChanged : w-"+w+",h-"+h+",oldw-"+oldw+",oldh-"+oldh);
/*        centerX=w/2;
        centerY=h/2;*/
        centerX=r;
        centerY=r;
        mWidth=w;
        mHeight=h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.reset();
//        centerX=r+mInterpolatedTime*50;
        float resultX=0f;
        float time=0.5f;
        float k1=(mWidth-r)/time;
        float b1=r;
        float k2=(r-mWidth)/time;
        float b2=2*mWidth-r;
        if(mInterpolatedTime<=1){
//            resultX=k1*mInterpolatedTime+b1;
            resultX = mInterpolatedTime*mWidth;
            Log.i(TAG,"<= resultX--"+resultX);
        }/*else {
            resultX=k2*mInterpolatedTime+b2;
            Log.i(TAG,"> resultX--"+resultX);
        }*/
        if(mInterpolatedTime<=0.3){
            data[1].x+=mInterpolatedTime*mWidth*0.5;
            data[2].x+=mInterpolatedTime*mWidth;
            data[3].x+=mInterpolatedTime*mWidth*0.5;
        }

        centerX=resultX;
//        if()
//        Log.i(TAG,"坐标:"+data[0].x+","+data[0].y);
        canvas.translate(centerX,centerY);
        drawPoint(canvas);

        drawCubicBezier(canvas);

/*        current += piece;
        if (current < duration){

            data[1].y += 120/count;
            ctrl[5].y -= 80/count;
            ctrl[6].y -= 80/count;

            ctrl[4].x -= 20/count;
            ctrl[7].x += 20/count;

            postInvalidateDelayed((long) piece);
        }*/

    }

    private void drawPoint(Canvas canvas){
        //绘制控制点
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        for(int i=0;i<ctrl.length;i++){
            canvas.drawPoint(ctrl[i].x,ctrl[i].y,paint);
        }
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(5);
        canvas.drawLine(data[0].x,data[0].y,ctrl[0].x,ctrl[0].y,paint);
        canvas.drawLine(data[0].x,data[0].y,ctrl[7].x,ctrl[7].y,paint);

        canvas.drawLine(data[1].x,data[1].y,ctrl[1].x,ctrl[1].y,paint);
        canvas.drawLine(data[1].x,data[1].y,ctrl[2].x,ctrl[2].y,paint);

        canvas.drawLine(data[2].x,data[2].y,ctrl[3].x,ctrl[3].y,paint);
        canvas.drawLine(data[2].x,data[2].y,ctrl[4].x,ctrl[4].y,paint);

        canvas.drawLine(data[3].x,data[3].y,ctrl[5].x,ctrl[5].y,paint);
        canvas.drawLine(data[3].x,data[3].y,ctrl[6].x,ctrl[6].y,paint);
    }

    private void drawCubicBezier(Canvas canvas){
        path.moveTo(data[0].x,data[0].y);

        path.cubicTo(ctrl[0].x,ctrl[0].y,ctrl[1].x,ctrl[1].y,data[1].x,data[1].y);
        path.cubicTo(ctrl[2].x,ctrl[2].y,ctrl[3].x,ctrl[3].y,data[2].x,data[2].y);
        path.cubicTo(ctrl[4].x,ctrl[4].y,ctrl[5].x,ctrl[5].y,data[3].x,data[3].y);
        path.cubicTo(ctrl[6].x,ctrl[6].y,ctrl[7].x,ctrl[7].y,data[0].x,data[0].y);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path,paint);

    }

    private class TranslteAnimaton extends Animation{
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            Log.i(TAG,"interpolatedTime : "+interpolatedTime);
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
            move.setDuration(1000);
            move.setInterpolator(new AccelerateDecelerateInterpolator());
//            move.setRepeatCount(Animation.RESTART);
            //move.setRepeatMode(Animation.REVERSE);
            startAnimation(move);
    }

    public void stopTranslteAnimaton(){
        clearAnimation();
    }
}
