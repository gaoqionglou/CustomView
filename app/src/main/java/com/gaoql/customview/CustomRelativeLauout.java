package com.gaoql.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.gaoql.R;

/**
 * @author gql
 */

public class CustomRelativeLauout extends RelativeLayout {
    private int mWidth;
    private int mHeight;
    private int mCircleWidth;
    private int mCircleMargin;
    private int mCircleColor;
    private Drawable mCircleDrawable;
    private Bitmap mCircleBitmap;
    private Paint mCriclePaint;
    private Paint mImagePaint;
    private RectF rectF;
    private Path mPath;
    private Path mImagePath;
    private PathMeasure mPathMeasure;
    private Matrix mMatrix;
    private float[] pos = new float[2];
    private float[] pos_moving = new float[2];
    private float[] tan = new float[2];
    private float startAngle=0f;
    private float currentSweepAngle=90f;
    private float currentPrecent;
    public CustomRelativeLauout(Context context) {
        super(context);
    }

    public CustomRelativeLauout(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAttr(context,attrs);
        init();
    }

    public CustomRelativeLauout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr,defStyleAttr);
        getAttr(context,attrs);
        init();
    }

    public CustomRelativeLauout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getAttr(context,attrs);
        init();
    }

    private void getAttr(Context context,AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomRelativeLauout);
        for(int i=0;i<typedArray.getIndexCount();i++){
            int attr = typedArray.getIndex(i);
            switch (attr){
                case R.styleable.CustomRelativeLauout_circleWidth:
                    mCircleWidth = (int)typedArray.getDimension(attr,0);
                    break;
                case R.styleable.CustomRelativeLauout_circleMargin:
                    mCircleMargin = (int)typedArray.getDimension(attr, 0);
                    break;
                case R.styleable.CustomRelativeLauout_circleColor:
                    mCircleColor = typedArray.getColor(attr, 0);
                    break;
                case R.styleable.CustomRelativeLauout_circleDrawable:
                    mCircleDrawable = typedArray.getDrawable(attr);
                    mCircleBitmap = drawableToBitmap(mCircleDrawable);
                default:
                    break;
            }
        }
        typedArray.recycle();
    }

    private void init(){
        mCriclePaint = new Paint();
        mImagePaint = new Paint();
        rectF = new RectF();
        mPath = new Path();
        mImagePath = new Path();
        mPathMeasure = new PathMeasure();
        mMatrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth =w;
        mHeight=h;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        View v = getChildAt(0);
        int radius = (int)Math.sqrt(Math.pow(v.getWidth(),2)+Math.pow(v.getHeight(),2))/2+mCircleMargin;
        mCriclePaint.setStyle(Paint.Style.STROKE);
        mCriclePaint.setColor(mCircleColor);
        mCriclePaint.setStrokeWidth(mCircleWidth);
        canvas.drawCircle(mWidth / 2, mHeight / 2, radius, mCriclePaint);
        //旋转90°纠正起点,原因是mImagePath.addCircle,起始位置不是在（0，-r）而是在(r,0)
//        canvas.rotate(-90);
        mImagePath.reset();
        mImagePath.addCircle(mWidth / 2, mHeight / 2, radius, Path.Direction.CW);
        mPathMeasure.setPath(mImagePath, false);
        mPathMeasure.getPosTan(mPathMeasure.getLength()*currentPrecent  , pos_moving, tan);
        Log.i("CRL",pos_moving[0]+","+pos_moving[1]);
        mMatrix.reset();
        float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);//计算图片的旋转角度，再矩阵变化
        mMatrix.postRotate(degrees, mCircleBitmap.getWidth() / 2, mCircleBitmap.getHeight() / 2);
        mMatrix.postTranslate(pos_moving[0] - mCircleBitmap.getWidth() / 2, pos_moving[1] - mCircleBitmap.getHeight() / 2);
        canvas.drawBitmap(mCircleBitmap, mMatrix, mImagePaint);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void rotate(){
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                    currentPrecent = (float) animation.getAnimatedValue();
                    invalidate();
            }
        });
        valueAnimator.setDuration(5000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }
}
