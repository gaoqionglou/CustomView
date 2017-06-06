package com.gaoql.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import com.gaoql.R;

/**
 * 圆形控件
 * @author gql
 */
public class CircleRadianButton extends View {
    public static final String TAG="CircleRadianButton";
    private Context mContext ;
    private int mWidth;
    private int mHeight;

    private Drawable mBackground;
    private int mBackgroundResource;
    private int mBackgroundColor;
    private Bitmap mBackgroundBitmap;

    private int radius;//半径
    private int diameter;//直径

    private Paint mCommonPaint;
    private Path mPath;
    public CircleRadianButton(Context context) {
        this(context,null);
    }

    public CircleRadianButton(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CircleRadianButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.CircleRadianButton);
        for(int i=0;i<typedArray.getIndexCount();i++){
            int attr = typedArray.getIndex(i);
            switch (attr){
                case R.styleable.CircleRadianButton_radius:
                    radius = (int)typedArray.getDimension(attr,0);
                    break;
                case R.styleable.CircleRadianButton_crbackground:
                    mBackgroundResource = typedArray.getResourceId(attr, 0);
                    if(mBackgroundResource!=0){
                        mBackground = mContext.getDrawable(mBackgroundResource);
                        if(mBackground instanceof ColorDrawable){
                            mBackgroundColor = typedArray.getColor(attr, 0);
                        }else {
                            mBackgroundBitmap = drawableToBitmap(mBackground);
                        }
                    }
                    break;
                default:
                    break;
            }

        }
        typedArray.recycle();
        /** 为了触发ACTION_MOVE ACTION_UP*/
        setClickable(true);
        init();
    }

    private void init(){
        mCommonPaint = new Paint();mPath = new Path();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = measureWidth(widthMeasureSpec);
        mHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));

}

    private int measureWidth(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.AT_MOST:
                int width = size + getPaddingLeft() + getPaddingRight();
                result = Math.min(width, size);
                break;
            case MeasureSpec.UNSPECIFIED:
                result = size + getPaddingLeft() + getPaddingRight();
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            default:
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.AT_MOST:
                int height = size + getPaddingTop() + getPaddingBottom();
                result = Math.min(height, size);
                break;
            case MeasureSpec.UNSPECIFIED:
                result = size + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            default:
                break;
        }
        return result;
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

//        if(mBackgroundColor ==0&&mBackground!=null){
//            canvas.drawBitmap(mBackgroundBitmap,mWidth,mHeight,mCommonPaint);
//        }else {
//            canvas.drawColor(mBackgroundColor);
//        }
        // 添加一个圆形区域
        mPath.addCircle(mWidth/2,mHeight/2 ,radius, Path.Direction.CCW);
        // 也可以通过设置 path 来显示自定义区域
        canvas.clipPath(mPath);

        if(mBackgroundColor ==0&&mBackground!=null){
            Matrix mShaderMatrix = new Matrix();
            int mBitmapWidth =  mBackgroundBitmap.getWidth();
            int mBitmapHeight =  mBackgroundBitmap.getHeight();
            float scale = 0f ;
            float dx=0f,dy=0f;
            if (mBitmapWidth * mHeight > mWidth * mBitmapHeight) {
                //y轴缩放 x轴平移 使得图片的y轴方向的边的尺寸缩放到图片显示区域（mDrawableRect）一样）
                scale = mHeight/ (float) mBitmapHeight;
                dx = (mWidth- mBitmapWidth * scale) * 0.5f;
            } else {
                //x轴缩放 y轴平移 使得图片的x轴方向的边的尺寸缩放到图片显示区域（mDrawableRect）一样）
                scale = mWidth / (float) mBitmapWidth;
                dy = (mHeight - mBitmapHeight * scale) * 0.5f;
            }
            // shaeder的变换矩阵，我们这里主要用于放大或者缩小。
            mShaderMatrix.setScale(scale, scale);
            // 平移
            mShaderMatrix.postTranslate((int) (dx + 0.5f) , (int) (dy + 0.5f) );
            canvas.drawBitmap(mBackgroundBitmap,mShaderMatrix,mCommonPaint);
        }else {
            canvas.drawColor(mBackgroundColor);
        }
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
        return super.onTouchEvent(event);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
