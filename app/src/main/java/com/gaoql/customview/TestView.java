package com.gaoql.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.gaoql.R;

/**
 * Created by admin on 2016/12/20.
 */

public class TestView extends View {
    private int mWidth;
    private int mHeight;
    private Paint circlePaint;//圆画笔
    private int radius=50;//圆的半径
    private int diameter;//圆的直径
    private int backgroundd;
    Bitmap b ;
    /**
     * 绘图的Paint
     */
    private Paint mBitmapPaint;
    /**
     * 3x3 矩阵，主要用于缩小放大
     */
    private Matrix mMatrix = new Matrix();
    /**
     * 渲染图像，使用图像为绘制图形着色
     */
    private BitmapShader mBitmapShader;
    public TestView(Context context) {
        super(context);
        /** 为了触发ACTION_MOVE ACTION_UP*/
        setClickable(true);
        init();
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleButton);
        float radius =  typedArray.getDimension(R.styleable.CircleButton_radius,50);
        int background = typedArray.getResourceId(R.styleable.CircleButton_backgroundd,-1);
        String one = typedArray.getString(R.styleable.CircleButton_radius);
        String two = typedArray.getString(R.styleable.CircleButton_backgroundd);
        Log.i("test", "one:" + one);
        Log.i("test", "two:" + two);
        //typedArray.getResourceId(R.styleable.CircleButton_background,R.color.colorAccent);

        this.setBackgroundd(background);
        this.setRadius((int)radius);
        /** 为了触发ACTION_MOVE ACTION_UP*/
        setClickable(true);
        init();
        typedArray.recycle();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getBackgroundd() {
        return backgroundd;
    }

    public void setBackgroundd(int backgroundd) {
        this.backgroundd = backgroundd;
    }

    private void init(){
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.RED);
         b = BitmapFactory.decodeResource(getResources(),getBackgroundd());
        mBitmapPaint= new Paint();
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
        float bw =  b.getWidth();
        float bh =  b.getHeight();
        float rw = mWidth/bw;
        float rh = mHeight/bh;
//        canvas.drawBitmap(b,matrix,new Paint());
        mBitmapShader = new BitmapShader(b, Shader.TileMode.CLAMP.CLAMP, Shader.TileMode.CLAMP.CLAMP);
        // 拿到bitmap宽或高的小值
        int bSize = Math.min(b.getWidth(), b.getHeight());
//        float scale = mWidth * 1.0f / bSize;
        float scale = Math.max(getWidth() * 1.0f / b.getWidth(), getHeight()
                * 1.0f / b.getHeight());
        // shader的变换矩阵，我们这里主要用于放大或者缩小
        mMatrix.setScale(scale, scale);
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(mMatrix);
        // 设置shader
        mBitmapPaint.setShader(mBitmapShader);
//        canvas.drawCircle(mWidth/2,radius,radius,mBitmapPaint);

        canvas.drawRoundRect(new RectF(0,0,mWidth,mHeight), 30, 30,
                mBitmapPaint);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasureWidthSize(widthMeasureSpec,MeasureSpec.getMode(widthMeasureSpec));
        int h = getMeasureHeightSize(heightMeasureSpec,MeasureSpec.getMode(heightMeasureSpec));

        setMeasuredDimension(w,h);
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
}
