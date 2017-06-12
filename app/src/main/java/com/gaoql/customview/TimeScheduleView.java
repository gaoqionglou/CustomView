package com.gaoql.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.gaoql.R;

/**
 * @author  gql
 * @since 2017/5/15.
 */

public class TimeScheduleView extends View {
    private String TAG = "TimeScheduleView";
    private int mBackgroudColor;//整个view的背景色
    private int innerCircleColor;//内圆的背景色
    private int outterCircleWidth;//外圆的长度
    private String mText;//文字
    private int mTextSize;//文字大小
    private int mTextColor;//文字颜色
    private int mProgressCircleColor;//进度条外圆颜色
    private int mProgressCircleWitdh;//进度条外圆长度
    private int outterCircleColor;//外圆的颜色
    private Paint mTextPaint;//文字画笔
    private Paint mOutterCriclePaint;//外圆画笔
    private Paint mInnerCriclePaint;//内圆画笔
    private Paint mProgressCirclePaint;//进度条画笔
    private Paint mRipplePaint;//描边波浪，环涟漪画笔
    private float mRippleWidth ;
    private int mRippleColor ;
    private Paint mImagePaint;
    private Drawable mProgressDrawable;//进度条图片
    private Bitmap mProgressBitmap;//进度条图片
    private int color = Color.BLACK;//默认颜色
    private Path mPath;
    private Path mImagePath;
    private PathMeasure mPathMeasure;
    private int mWidth, mHeight;
    private int innerCircleRadius;
    private int outterCircleRadius;
    private RectF rectF;
    private int startAngle = -90;
    private float currentSweepAngle = 0f;
    private float max = 100f;
    private float current = 0;
    // 当前点的实际位置
    float[] pos = new float[2];
    // 当前点的tangent值,用于计算图片所需旋转的角度
    float[] tan = new float[2];
    private Matrix mMatrix;
    private float precent;
    private float currentPrecent;
    private float rippleInterpolatedTime = 0f;
    //开一个handler去跑涟漪动画
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    startRippleAnimatior();
                    break;
                default:
                    break;
            }

        }
    };
    public TimeScheduleView(Context context) {
        super(context);
    }

    public TimeScheduleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeScheduleView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public TimeScheduleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeScheduleView);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.TimeScheduleView_outterCirlceRadius:
                    outterCircleRadius = (int) typedArray.getDimension(attr, 0f);
                    break;
                case R.styleable.TimeScheduleView_innerCirlceRadius:
                    innerCircleRadius = (int) typedArray.getDimension(attr, 0f);
                    break;
                case R.styleable.TimeScheduleView_mbackgroundColor:
                    mBackgroudColor = typedArray.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.TimeScheduleView_innerCircleColor:
                    innerCircleColor = typedArray.getColor(attr, color);
                    break;
                case R.styleable.TimeScheduleView_outterCirlceWidth:
                    outterCircleWidth = (int) typedArray.getDimension(attr, 0f);
                    break;
                case R.styleable.TimeScheduleView_text:
                    mText = typedArray.getString(attr);
                    break;
                case R.styleable.TimeScheduleView_textSize:
                    mTextSize = typedArray.getDimensionPixelSize(attr, 10);
                    break;
                case R.styleable.TimeScheduleView_textColor:
                    mTextColor = typedArray.getColor(attr, color);
                    break;
                case R.styleable.TimeScheduleView_progressCircleColor:
                    mProgressCircleColor = typedArray.getColor(attr, color);
                    break;
                case R.styleable.TimeScheduleView_progressCircleWidth:
                    mProgressCircleWitdh = typedArray.getDimensionPixelSize(attr, 10);
                    break;
                case R.styleable.TimeScheduleView_outterCircleColor:
                    outterCircleColor = typedArray.getColor(attr, color);
                    break;
                case R.styleable.TimeScheduleView_rippleWidth:
                    mRippleWidth = typedArray.getDimension(attr, 20f);
                    break;
                case R.styleable.TimeScheduleView_rippleColor:
                    mRippleColor = typedArray.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.TimeScheduleView_progressDrawable:
                    mProgressDrawable = typedArray.getDrawable(attr);
                    mProgressBitmap = drawableToBitmap(mProgressDrawable);
                    break;
                default:
                    break;
            }

        }
        typedArray.recycle();
        intit();
    }

    private void intit() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        mOutterCriclePaint = new Paint();
        mOutterCriclePaint.setStyle(Paint.Style.STROKE);
        mOutterCriclePaint.setColor(outterCircleColor);
        mOutterCriclePaint.setStrokeWidth(outterCircleWidth);

        mInnerCriclePaint = new Paint();
        mInnerCriclePaint.setColor(innerCircleColor);

        mProgressCirclePaint = new Paint();
        mProgressCirclePaint.setStyle(Paint.Style.STROKE);
        mProgressCirclePaint.setColor(mProgressCircleColor);
        mProgressCirclePaint.setStrokeWidth(mProgressCircleWitdh);

        mRipplePaint = new Paint();
        mRipplePaint.setStyle(Paint.Style.STROKE);
        mRipplePaint.setColor(mRippleColor);
        mRipplePaint.setStrokeWidth(mRippleWidth);

        mImagePaint = new Paint();
        rectF = new RectF();
        mPath = new Path();
        mMatrix = new Matrix();
        mPathMeasure = new PathMeasure();
        mImagePath = new Path();
    }

    public int getBackgroudColor() {
        return mBackgroudColor;
    }

    public void setBackgroudColor(int mBackgroudColor) {
        this.mBackgroudColor = mBackgroudColor;
    }

    public int getOutterCircleWidth() {
        return outterCircleWidth;
    }

    public void setOutterCircleWidth(int outterCircleWidth) {
        this.outterCircleWidth = outterCircleWidth;
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public int getmTextSize() {
        return mTextSize;
    }

    public void setTextSize(int mTextSize) {
        this.mTextSize = mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    public int getProgressCircleColor() {
        return mProgressCircleColor;
    }

    public void setProgressCircleColor(int mProgressCircleColor) {
        this.mProgressCircleColor = mProgressCircleColor;
    }

    public int getProgressCircleWitdh() {
        return mProgressCircleWitdh;
    }

    public void setProgressCircleWitdh(int mProgressCircleWitdh) {
        this.mProgressCircleWitdh = mProgressCircleWitdh;
    }

    public int getOutterCircleColor() {
        return outterCircleColor;
    }

    public void setOutterCircleColor(int outterCircleColor) {
        this.outterCircleColor = outterCircleColor;
    }

    public Paint getTextPaint() {
        return mTextPaint;
    }

    public int getInnerCircleRadius() {
        return innerCircleRadius;
    }

    public void setInnerCircleRadius(int innerCircleRadius) {
        this.innerCircleRadius = innerCircleRadius;
    }

    public int getOutterCircleRadius() {
        return outterCircleRadius;
    }

    public void setOutterCircleRadius(int outterCircleRadius) {
        this.outterCircleRadius = outterCircleRadius;
    }

    public void setProgressImage(int id) {
        mProgressBitmap = BitmapFactory.decodeResource(getResources(), id);
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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPath.reset();
        canvas.translate(mWidth / 2, mHeight / 2);
        canvas.drawCircle(0, 0, outterCircleRadius, mOutterCriclePaint);
        canvas.drawCircle(0, 0, innerCircleRadius, mInnerCriclePaint);
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        float textWidth = mTextPaint.measureText(mText);
        float textHeight = (Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent));
        canvas.drawText(mText, 0 - textWidth / 2, 0 + textHeight / 2, mTextPaint);
        rectF.left = -outterCircleRadius;
        rectF.top = -outterCircleRadius;
        rectF.right = outterCircleRadius;
        rectF.bottom = outterCircleRadius;
//        canvas.drawRect(rectF,new Paint());
//        currentSweepAngle = countSweepAngle();
        Log.i(TAG, "currentSweepAngle :" + currentSweepAngle);
        mPath.addArc(rectF, startAngle, currentSweepAngle);
        canvas.drawPath(mPath, mProgressCirclePaint);
        //旋转90°纠正起点,原因是mImagePath.addCircle,起始位置不是在（0，-r）而是在(r,0)
        canvas.rotate(-90);
        mImagePath.reset();
        mImagePath.addCircle(0, 0, outterCircleRadius, Path.Direction.CW);
        mPathMeasure.setPath(mImagePath, false);
        mPathMeasure.getPosTan(mPathMeasure.getLength() * currentPrecent , pos, tan);
        mMatrix.reset();
        float degrees = (float) (Math.atan2(tan[1], tan[0]) * 180.0 / Math.PI);//计算图片的旋转角度，再矩阵变化
        mMatrix.postRotate(degrees, mProgressBitmap.getWidth() / 2, mProgressBitmap.getHeight() / 2);
        mMatrix.postTranslate(pos[0] - mProgressBitmap.getWidth() / 2, pos[1] - mProgressBitmap.getHeight() / 2);
        canvas.drawBitmap(mProgressBitmap, mMatrix, mImagePaint);
        mRipplePaint.setStrokeWidth(mRippleWidth * rippleInterpolatedTime);
        mRipplePaint.setAlpha(255 - (int) (255 * rippleInterpolatedTime));
        float rw = mRippleWidth * rippleInterpolatedTime;
        canvas.drawCircle(0, 0, innerCircleRadius + rw / 2, mRipplePaint);
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    public float countSweepAngle() {
        precent = current / max;
        if (precent >= 1) precent = 1f;
        Log.i(TAG, "Angle :" + (360 * precent));
        return 360 * precent;
    }

    /**
     * 开始进度动画
     */
    public void startProgressAnimaton() {
        float endAngle = 0;
        if (currentSweepAngle == countSweepAngle()) {
            return;
        } else {
            endAngle = countSweepAngle();
            Log.i(TAG, "endAngle :" + endAngle);
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(currentSweepAngle, endAngle);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentSweepAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }

    /**
     * 开始进度动画
     */
    public void startFlyingAnimaton() {
        if (currentSweepAngle == precent) {
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(currentPrecent, precent);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPrecent = (float) animation.getAnimatedValue();
                if(currentPrecent==1){
                    handler.sendEmptyMessage(0);
                }
                invalidate();
            }
        });
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }
    private void startRippleAnimatior(){
        ValueAnimator rippleAnimator = ValueAnimator.ofFloat(0f,1f);
        rippleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rippleInterpolatedTime = (float)animation.getAnimatedValue();
//                Log.e(TAG,"startRippleAnimatior interpolatedTime "+mInterpolatedTime);
                invalidate();

            }
        });
        rippleAnimator.setDuration(1000);
        rippleAnimator.setInterpolator(new LinearInterpolator());
        rippleAnimator.start();
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
