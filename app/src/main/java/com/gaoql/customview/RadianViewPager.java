package com.gaoql.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.gaoql.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gql
 */

public class RadianViewPager extends ViewGroup {
    private String TAG = "RadianViewPager";
    private int mWidth;
    private int mHeight;
    private Drawable mBackground;
    private int mBackgroundColor;
    private Bitmap mBackgroundBitmap;
    private int mBackgroundResource;
    //底部圆弧高度
    private float mRadianTop = 0f;
    //排列子View所在圆弧高度,宽度,颜色,以及path
    private float mItemRadianTop = 0f;
    private float mItemRadianWidth = 0f;
    private int mItemRadianColor;
    private Path mItemRadianPath;
    private Path mRadianPath;

    private Path mLinePath;
    private int mDefaultColor = Color.WHITE;

    private Paint mCommonPaint;
    private Paint mTextPaint;
    private RectF rectF;
    private PathMeasure mPathMeasure;
    private PathMeasure mBottomPathMeasure;
    // 当前点的实际位置
    float[] pos = new float[2];
    // 当前点的tangent值,用于计算图片所需旋转的角度
    float[] tan = new float[2];
    private int mSettledItemCount = 0;
    private String[] settledItemText = {"我的计划", "极速之神"};
    private int mSettledCircleRadius;
    private int mSettledCircleColor;
    private Context mContext;
    private int currentIndex = 0;
    private int endIndex = 0;
    private float currentPrecent=0f;
    /**
     * 动画未开始状态
     */
    public static final int STATE_UNSTART = -1;
    /**
     * 动画开始状态
     */
    public static final int STATE_START = 0;
    /**
     * 动画进行状态
     */
    public static final int STATE_MOVING = 1;
    /**
     * 动画结束状态
     */
    public static final int STATE_STOP = 2;
    /**
     * 动画状态
     */
    private int translateState = STATE_UNSTART;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.i(TAG,"Handler开始 " + translateState );
                    if (translateState != STATE_START  &&translateState != STATE_MOVING  && currentIndex!=endIndex) {
                        Log.i(TAG,"startCircleMovingAnimatior");
                        startCircleMovingAnimatior();
                    }
                    break;
                default:
                    break;
            }

        }
    };
    public RadianViewPager(Context context) {
        this(context, null);
    }

    public RadianViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadianViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RadianViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadianViewPager);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.RadianViewPager_android_background:
                    mBackgroundResource = typedArray.getResourceId(attr, 0);
                    if (mBackgroundResource != 0) {
                        mBackground = mContext.getDrawable(mBackgroundResource);
                        if (mBackground instanceof ColorDrawable) {
                            mBackgroundColor = typedArray.getColor(attr, 0);
                        } else {
                            mBackgroundBitmap = drawableToBitmap(mBackground);
                        }
                    }
                    break;
                case R.styleable.RadianViewPager_radianTop:
                    mRadianTop = typedArray.getDimension(attr, 0f);
                    break;
                case R.styleable.RadianViewPager_itemRadianTop:
                    mItemRadianTop = typedArray.getDimension(attr, 0f);
                    break;
                case R.styleable.RadianViewPager_itemRadianWidth:
                    mItemRadianWidth = typedArray.getDimension(attr, 0f);
                    break;
                case R.styleable.RadianViewPager_itemRadianColor:
                    mItemRadianColor = typedArray.getColor(attr, mDefaultColor);
                    break;
                case R.styleable.RadianViewPager_settledCircleCount:
                    mSettledItemCount = typedArray.getInteger(attr, 0);
                    break;
                case R.styleable.RadianViewPager_settledCircleRadius:
                    mSettledCircleRadius = (int) typedArray.getDimension(attr, 0);
                    break;
                case R.styleable.RadianViewPager_settledCircleColor:
                    mSettledCircleColor = typedArray.getColor(attr, mDefaultColor);
                    break;
                default:
                    break;
            }

        }
        typedArray.recycle();
        setWillNotDraw(false);
        init();
    }

    private void init() {
        mCommonPaint = new Paint();
        mTextPaint = new Paint();
        rectF = new RectF();
        mItemRadianPath = new Path();
        mPathMeasure = new PathMeasure();
        mBottomPathMeasure = new PathMeasure();
        mRadianPath = new Path();
        mLinePath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
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
        mHeight = h;
        mWidth = w;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //todo:只支持一个子View 不是1个view将抛异常
        List<PointF> pointFs = getChildViewPosInRadian();

        for (int n = 0; n < getChildCount(); n++) {
            View v = getChildAt(n);
            PointF p = pointFs.get(n);
            float centerX = p.x;
            float centerY = p.y;
            int w = v.getMeasuredWidth();
            int h = v.getMeasuredHeight();
//            MarginLayoutParams mlp = (MarginLayoutParams)getLayoutParams();
            //这里就不计算margin了
            int left = (int) (centerX - w / 2f);
            int top = (int) (centerY - h / 2f);
            int right = (int) (centerX + w / 2f);
            int bottom = (int) (centerY + h / 2f);
            v.layout(left, top, right, bottom);
            Log.e(TAG, "CHILD onLayout: " + (n + 1) + ",l-" + left + ",t-" + top + ",r-" + right + ",b-" + bottom);

        }
    }

    // 绘制背景
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBackgroundColor == 0 && mBackground != null) {
            canvas.drawBitmap(mBackgroundBitmap, mWidth, mHeight, mCommonPaint);
        } else {
            canvas.drawColor(mBackgroundColor);
        }
        //绘制底部圆弧
        rectF.left = 0;
        rectF.top = mHeight - mRadianTop;
        rectF.right = mWidth;
        rectF.bottom = mHeight + mRadianTop;
        mCommonPaint.setColor(Color.WHITE);
        canvas.drawArc(rectF, 0, -180, true, mCommonPaint);
        //绘制item所做的圆弧
        rectF.left = 0;
        rectF.top = mHeight - mItemRadianTop;
        rectF.right = mWidth;
        rectF.bottom = mHeight + mRadianTop;
        mItemRadianPath.addArc(rectF, -180, 180);
        //画笔描边
        mCommonPaint.setStyle(Paint.Style.STROKE);
        mCommonPaint.setColor(Color.WHITE);
        mCommonPaint.setStrokeWidth(mItemRadianWidth);
//        mCommonPaint.setColor(mItemRadianColor);
        canvas.drawPath(mItemRadianPath, mCommonPaint);
        List<PointF> pointFs = getChildViewPosInRadian();
        mItemRadianPath.reset();
        rectF.left = 0;
        rectF.top = mHeight - mItemRadianTop;
        rectF.right = mWidth;
        rectF.bottom = mHeight + mRadianTop;
        mItemRadianPath.addArc(rectF, -180, 180);
        canvas.drawPath(mItemRadianPath, mCommonPaint);
        for (int n = 0; n < mSettledItemCount; n++) {
            PointF p = pointFs.get(n);
            mCommonPaint.setStyle(Paint.Style.FILL);
            mCommonPaint.setColor(mSettledCircleColor);
            canvas.drawCircle(p.x, p.y, mSettledCircleRadius, mCommonPaint);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(currentPrecent>0){
            Log.i(TAG,"123");
        }
        Log.i(TAG,"currentPrecent "+currentPrecent );
        Log.i(TAG,"currentIndex "+currentIndex );
        CircleRadianButton circleRadianButton = (CircleRadianButton)getChildAt(0);
        ArrayList<PointF> pointFs = new ArrayList<>();
        rectF.left = 0;
        rectF.top = mHeight - mItemRadianTop;
        rectF.right = mWidth;
        rectF.bottom = mHeight + mRadianTop;
        mItemRadianPath.reset();
        mItemRadianPath.addArc(rectF, -180, 180);
        mBottomPathMeasure.setPath(mItemRadianPath, false);
        float sum = mBottomPathMeasure.getLength();
        float length = sum / mSettledItemCount * 1f;
        float currentLenght =  length / 2f + length * currentIndex;
        if(currentPrecent==0f) currentPrecent = currentLenght/sum;
        mBottomPathMeasure.getPosTan(sum*currentPrecent, pos, tan);
        PointF pointF1 = new PointF();
        pointF1.x =pos[0];
        pointF1.y =pos[1];

        rectF.left = 0;
        rectF.top = mHeight - mRadianTop;
        rectF.right = mWidth;
        rectF.bottom = mHeight + mRadianTop;
        mRadianPath.addArc(rectF,-180, 180);
        mBottomPathMeasure.setPath(mRadianPath, false);
        float radinLength = mBottomPathMeasure.getLength();
        mBottomPathMeasure.getPosTan(radinLength*currentPrecent, pos, tan);
        PointF pointF4 = new PointF();
        pointF4.x = pos[0];
        pointF4.y=pos[1];
        mBottomPathMeasure.getPosTan(radinLength*currentPrecent -50, pos, tan);
        PointF pointF2 = new PointF();
        pointF2.x = pos[0];
        pointF2.y=pos[1];
        mBottomPathMeasure.getPosTan(radinLength*currentPrecent +50, pos, tan);
        PointF pointF3 = new PointF();
        pointF3.x = pos[0];
        pointF3.y=pos[1];
        mLinePath.reset();
        mLinePath.moveTo(pointF1.x,pointF1.y);
        mLinePath.lineTo(pointF4.x,pointF4.y);
//        canvas.drawCircle(pointF1.x,pointF1.y,10,mCommonPaint);
//        canvas.drawCircle(pointF4.x,pointF4.y,10,mCommonPaint);
        mBottomPathMeasure.setPath(mLinePath, false);
        CircleRadianButton c = (CircleRadianButton) getChildAt(0);
        mBottomPathMeasure.getPosTan(c.getRadius()+10,pos,tan);
        PointF pointF5 =new PointF();
        pointF5.x = pos[0];
        pointF5.y = pos[1];
        mLinePath.reset();
        mLinePath.moveTo(pointF5.x,pointF5.y);
        mLinePath.lineTo(pointF2.x,pointF2.y);
        mLinePath.lineTo(pointF3.x,pointF3.y);
        mCommonPaint.setStyle(Paint.Style.FILL);
        mCommonPaint.setColor(Color.WHITE);
        canvas.drawPath(mLinePath,mCommonPaint);}

    /**
     * 分发
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(TAG, "dispatchTouchEvent");
        List<PointF> pointFs = getChildViewPosInRadian();
        int a = ev.getAction();
        switch (a) {
            case MotionEvent.ACTION_DOWN:
                float ex = ev.getX();
                float ey = ev.getY();
                int index = isInRange(ex, ey, pointFs, mSettledCircleRadius);
                Log.i(TAG, "将要去点中的第" + (index + 1) + "个");
                if (index != -1) {
                    endIndex = index;
                    startCircleMoving();
                }
                Log.e(TAG, "dispatchTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "dispatchTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "dispatchTouchEvent ACTION_UP");
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 拦截
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 消化
     *
     * @param event
     * @return
     */
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

    private List<PointF> getChildViewPosInRadian() {
        ArrayList<PointF> pointFs = new ArrayList<>();
        rectF.left = 0;
        rectF.top = mHeight - mItemRadianTop;
        rectF.right = mWidth;
        rectF.bottom = mHeight + mRadianTop;
        mItemRadianPath.reset();
        mItemRadianPath.addArc(rectF, -180, 180);
        mPathMeasure.setPath(mItemRadianPath, false);
        float sum = mPathMeasure.getLength();
        float length = sum / mSettledItemCount * 1f;
        for (int i = 0; i < mSettledItemCount; i++) {
            mPathMeasure.getPosTan(length / 2f + length * i, pos, tan);
            PointF pointF = new PointF();
            pointF.x = pos[0];
            pointF.y = pos[1];
            pointFs.add(pointF);
        }
        return pointFs;
    }

    /**
     * 检查是否在所在区域中，并返回所在index
     *
     * @param targetX
     * @param targetY
     * @param centerPoints
     * @param mSettledCircleRadius
     * @return
     */
    private int isInRange(float targetX, float targetY, List<PointF> centerPoints, int mSettledCircleRadius) {
        int index = -1;
        for (PointF pointF : centerPoints) {
            if (isInRange(targetX, targetY, pointF, mSettledCircleRadius)) {
                return centerPoints.indexOf(pointF);
            }
        }
        return index;
    }

    /**
     * 检查是否在所在区域中
     *
     * @param targetX
     * @param targetY
     * @param centerPoint
     * @param mSettledCircleRadius
     * @return
     */
    private boolean isInRange(float targetX, float targetY, PointF centerPoint, int mSettledCircleRadius) {
        float centerX = centerPoint.x;
        float centerY = centerPoint.y;
        int left = (int) (centerX - mSettledCircleRadius);
        int top = (int) (centerY - mSettledCircleRadius);
        int right = (int) (centerX + mSettledCircleRadius);
        int bottom = (int) (centerY + mSettledCircleRadius);
        boolean inWidth = targetX >= left && targetX <= right;
        boolean inHeight = targetY >= top && targetY <= bottom;
        return inWidth && inHeight;
    }

    private void startCircleMoving(){
        handler.sendEmptyMessage(0);
    }

    private void startCircleMovingAnimatior(){
        rectF.left = 0;
        rectF.top = mHeight - mItemRadianTop;
        rectF.right = mWidth;
        rectF.bottom = mHeight + mRadianTop;
        mItemRadianPath.reset();
        mItemRadianPath.addArc(rectF, -180, 180);
        mPathMeasure.setPath(mItemRadianPath, false);
        final float sum = mPathMeasure.getLength();
        float length = sum / mSettledItemCount * 1f;
        float currentLength =  length / 2f + length * currentIndex*1f;
        float endLength =  length / 2f + length * endIndex*1f;
        float precent  =  currentLength / sum;
        final float endPrecent =  endLength / sum;
        final View child =  getChildAt(0);

        ValueAnimator translateAnimator = ValueAnimator.ofFloat(precent,endPrecent);
        translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPrecent = (float)animation.getAnimatedValue();
                float currentPos[] = new float[2];
                float currentTan[] = new float[2];
                mPathMeasure.getPosTan(sum*currentPrecent, currentPos, currentTan);
                Log.e(TAG,"startTranslateAnimatior precent "+currentPrecent);
                if(currentPrecent!=endPrecent){
                    translateState  = STATE_MOVING;
                    CircleRadianButton circleRadianButton = (CircleRadianButton)child;
                    child.layout((int)currentPos[0]-circleRadianButton.getRadius(),
                            (int)currentPos[1]-circleRadianButton.getRadius(),
                            (int)currentPos[0]+circleRadianButton.getRadius(),
                            (int)currentPos[1]+circleRadianButton.getRadius());
                }else{
                    currentIndex = endIndex;
                    translateState =STATE_STOP;
                }
                invalidate();
            }
        });
        translateAnimator.setDuration(1000);
        translateAnimator.setInterpolator(new LinearInterpolator());
        translateAnimator.start();

    }

}
