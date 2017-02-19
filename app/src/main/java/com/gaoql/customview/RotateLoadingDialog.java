package com.gaoql.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;

import com.gaoql.R;

/**
 * @author gql
 */

public class RotateLoadingDialog extends View {
    private Paint mPaint;
    private Path mPath;
    private PathMeasure mPathMeasure ;
    private int mWidth,mHeight;
    private int radius;
    private Matrix mMatrix;
    private Bitmap mBitmap;
    float currentValue=0f;
    float[] pos = new float[2];
    float[] tan=new float[2];
    public RotateLoadingDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);init();
    }

    public RotateLoadingDialog(Context context, AttributeSet attrs) {
        super(context, attrs);init();
    }

    public RotateLoadingDialog(Context context) {
        super(context);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(50);
        mPath = new Path();
        mPathMeasure = new PathMeasure();
        mMatrix = new Matrix();
        BitmapFactory.Options options = new BitmapFactory.Options();
        mBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.arrow,options);
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
        mWidth =w;
        mHeight=h;
        radius = Math.min(mWidth,mHeight)/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth/2,mHeight/2);
        mPath.addCircle(0,0,radius, Path.Direction.CW);

        mPathMeasure.setPath(mPath,false);
        currentValue+=0.005;
        if(currentValue>=1){
            currentValue =0;
        }
        mPathMeasure.getPosTan(mPathMeasure.getLength()*currentValue,pos,tan);
        mMatrix.reset();
        float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI); // 计算图片旋转角度
        mMatrix.postRotate(degrees,mBitmap.getWidth()/2,mHeight/2);
        mMatrix.postTranslate(pos[0]-mBitmap.getWidth()/2,pos[1]-mBitmap.getHeight()/2);
        canvas.drawPath(mPath,mPaint);
        canvas.drawBitmap(mBitmap,mMatrix,mPaint);
        invalidate();
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
