package com.gaoql.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.gaoql.R;


public class CircleButton extends View {
    public static final String TAG="GAOVG-CircleButton";
    private int mWidth;
    private int mHeight;
    private Paint circlePaint;//圆画笔
    private int radius=50;//圆的半径
    private int diameter;//圆的直径
    private Animation animation;
    public CircleButton(Context context) {
        super(context);
        /** 为了触发ACTION_MOVE ACTION_UP*/
        setClickable(true);
        init();
    }

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleButton);
        float radius =  typedArray.getDimension(R.styleable.CircleButton_radius,50);
        int background = typedArray.getResourceId(R.styleable.CircleButton_backgroundd,-1);

        this.setRadius((int)radius);
        /** 为了触发ACTION_MOVE ACTION_UP*/
        setClickable(true);
        init();
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasureWidthSize(widthMeasureSpec,MeasureSpec.getMode(widthMeasureSpec));
        int h = getMeasureHeightSize(heightMeasureSpec,MeasureSpec.getMode(heightMeasureSpec));

        setMeasuredDimension(w,h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight=h;
        diameter = Math.min(mWidth,mHeight);
        radius=diameter/2;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mWidth/2,radius,radius,circlePaint);
    }

    private void init(){
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.RED);
    }

    private int getMeasureWidthSize(int measureSpec,int mode){
        int measureSize = 0;
        switch (mode){
            case MeasureSpec.AT_MOST:
                measureSize=radius*2+getPaddingLeft()+getPaddingRight();
                break;
            case MeasureSpec.EXACTLY:
                measureSize=MeasureSpec.getSize(measureSpec);
                break;
            case MeasureSpec.UNSPECIFIED:
                measureSize=50;
                break;
            default:
                break;
        }
        return measureSize;
    }

    private int getMeasureHeightSize(int measureSpec,int mode){
        int measureSize = 0;
        switch (mode){
            case MeasureSpec.AT_MOST:
                measureSize=radius*2+getPaddingTop()+getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                measureSize=MeasureSpec.getSize(measureSpec);
                break;
            case MeasureSpec.UNSPECIFIED:
                measureSize=50;
                break;
            default:
                break;
        }
        return measureSize;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int a = event.getAction();
        switch (a){
            case MotionEvent.ACTION_DOWN:
//                Log.e(TAG,"dispatchTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG,"dispatchTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
//                Log.e(TAG,"dispatchTouchEvent ACTION_UP");
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int a = event.getAction();
        switch (a){
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG,"onTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG,"onTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
//                Log.e(TAG,"onTouchEvent ACTION_UP");
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
