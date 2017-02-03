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
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.drawable.VectorDrawable;
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
    private Paint borderPaint;//白边画笔
    private Paint circlePaint;//圆画笔
    private Paint bitmapPaint;//位图画笔
    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private Matrix mMatrix;
    private int radius=50;//圆的半径
    private int diameter;//圆的直径
    private int mBackgroudColor=0;
    private int mBackgroud=0;
    private Animation animation;
    private Drawable mDrawable;
    private View attachView=null;
    public CircleButton(Context context) {
        this(context,null);
    }

    public CircleButton(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CircleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        required >API 15
//        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomTitleView, defStyle, R.style.DefualtCircleButton);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.CircleButton);
        for(int i=0;i<typedArray.getIndexCount();i++){
            int attr = typedArray.getIndex(i);
            switch (attr){
                case R.styleable.CircleButton_radius:
                    radius = (int)typedArray.getDimension(attr,radius);
                    setRadius(radius);
                    break;
                case R.styleable.CircleButton_backgroundColor:
                    mBackgroudColor = typedArray.getColor(attr,Color.WHITE);
                    break;
                case R.styleable.CircleButton_backgroundDrawable:
                    mDrawable = typedArray.getDrawable(attr);
                    mBitmap =drawableToBitmap(mDrawable);
//                    mBitmap = drawab;//BitmapFactory.decodeResource(getResources(),mBackgroud);
                    mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                    setBitmap(mBitmap);
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

    public View getAttachView() {
        return attachView;
    }

    public void setAttachView(View attachView) {
        this.attachView = attachView;
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
        /** 边界 */
        canvas.drawCircle(mWidth/2,getRadius(),getRadius(),borderPaint);
/*        if (mBackgroud == 0) {
            mBackgroudColor = mBackgroudColor == 0 ? Color.WHITE : mBackgroudColor;// TODO:构造函数里面默认给了白色，为什么是0啊喂！
            circlePaint.setColor(mBackgroudColor);
            canvas.drawCircle(mWidth / 2, getRadius(), getRadius(), circlePaint);
        } else {
            Path p = new Path();
            p.addCircle(mWidth / 2,getRadius(), getRadius(), Path.Direction.CW);
            canvas.clipPath(p);
            if(mBackgroudColor!=0){
                circlePaint.setColor(Color.GRAY);
//                canvas.drawColor(mBackgroudColor);
            }
//            float scaleX = mWidth / mBitmap.getWidth();
//            float scaleY = mHeight / mBitmap.getHeight();
            //取最小值出来，如果用上面的做法去缩放会显示不全

            int size = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
            float scale = mWidth * 1.0f / size;
            mMatrix.postScale(scale, scale);
            mBitmapShader.setLocalMatrix(mMatrix);
            bitmapPaint.setShader(mBitmapShader);
            canvas.drawCircle(mWidth / 2, getRadius(), getRadius(), bitmapPaint);
        }*/

    }

    private void init(){
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.WHITE);
        bitmapPaint = new Paint();
        borderPaint=new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(1);
        mMatrix = new Matrix();
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
                SlidingViewGroup slidingViewGroup = (SlidingViewGroup)getAttachView();
                int pageIndex = slidingViewGroup.getCurrentPageIndex();
                CustomViewGroup parent = (CustomViewGroup)getParent();
                int childIndex =  parent.indexOfChild(this);
                Log.e(TAG,"onTouchEvent ACTION_UP "+pageIndex+" moveTo "+childIndex);
                slidingViewGroup.moveTo(pageIndex,childIndex);
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

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public void setMatrix(Matrix mMatrix) {
        this.mMatrix = mMatrix;
    }

    public void setDrawable(Drawable mDrawable) {
        this.mDrawable = mDrawable;
    }

    public Drawable getDrawable() {
        return mDrawable;
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
}
