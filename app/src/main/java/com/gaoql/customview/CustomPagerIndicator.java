package com.gaoql.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.gaoql.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author gql
 */

public class CustomPagerIndicator extends/* ViewGroup*/LinearLayout {
    public static final String TAG = "GAOVG-CustomViewGroup";
    /**
     * 一个常量，用来计算绘制圆形贝塞尔曲线控制点的位置
     */
    private static final float C = 0.551915024494f;
    private float radius = 140f;
    /**
     * 圆形的控制点与数据点的差值
     */
    private float diff = radius * C;
    /**
     * 触碰圆的第一次初始化的中心坐标
     */
    private float firstCenterX, firstCenterY;
    private Paint linePaint;
    private Paint paint;
    private Paint ripplePaint;//描边波浪，环涟漪画笔
    private Path path;
    private int mWidth = 0;
    private int mHeight=0;

    /**
     * 圆的4个点
     */
    private PointF[] dataPoint = new PointF[4];
    /**
     * 触碰圆的第一次初始化的4个点
     */
    private PointF[] firstDataPoint = new PointF[4];
    /**
     * 贝塞尔曲线的8个控制点，和dataPoint有关
     */
    private PointF[] ctrlPoint = new PointF[8];
    /**
     * 简单的平移动画类
     */
    private TranslateAnimation move = new TranslateAnimation();
    private RippleScaleAnimation rippleScaleAnimation = new RippleScaleAnimation();
    private float rippleWidth = 20f;
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
    private int rippleState = STATE_UNSTART;
    /**
     * 动画播放时间 范围在[0,1]
     */
    private float mInterpolatedTime = 0f;//0-1的播放时间
    private float rippleInterpolatedTime = 0f;//0-1的播放时间
    private SlidingViewPager attachView;
    /**
     * 圆的平移过程x,y的斜率
     */
    private float kx, ky;
    /**
     * 触摸坐标
     */
    private float ex, ey;
    /**
     * 保存坐标点的队列
     */
    private LimitQueue<PointF> pointLimitQueue = new LimitQueue<>(100);
    /**
     * handler定时执行动画->重绘
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    // 移除所有的msg.what为0等消息，保证只有一个循环消息队列再跑
                    handler.removeMessages(0);
                    if (translateState != STATE_START && pointLimitQueue.size() > 1) {
                        startTranslateAnimation();
                    }
                    // 再次发出msg，循环更新
                    handler.sendEmptyMessageDelayed(0, 1000);
                    break;

                case 1:
                    // 直接移除，定时器停止
                    handler.removeMessages(0);
                    break;
                case 3:
                    //开始描边波浪动画
                    startRippleScaleAnimation();
                    break;

                default:
                    break;
            }

        }
    };

    public CustomPagerIndicator(Context context) {
        this(context, null);
    }

    public CustomPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public int getTranslateState() {
        return translateState;
    }

    public int getRippleState() {
        return rippleState;
    }

    public SlidingViewPager getAttachView() {
        return attachView;
    }

    public void setAttachView(SlidingViewPager attachView) {
        this.attachView = attachView;
    }

    /**
     * 动画是否进行中
     * @return
     */
    public boolean isTranslateOrRippleInProgress(){
        return getTranslateState() == STATE_MOVING || getRippleState() == STATE_MOVING;
    }


    /**
     * 测量子View 得出父View的大小
     * onMeasure() 在这个函数中，ViewGroup会接受childView的请求的大小，
     * 然后通过childView的 measure(newWidthMeasureSpec, heightMeasureSpec)函数存储到childView中，
     * 以便childView的getMeasuredWidth() andgetMeasuredHeight() 的值可以被后续工作得到
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
         */
       /* int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);*/
        /**
         * 算出所有的childView的宽和高,
         */
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        /**
         * 下面处理ViewGroup 长宽是wrapcontent
         */
        /*int counts = getChildCount();
        measure(widthMode, heightMode, widthSize, heightSize, counts);*/

    }


    private void measure(int widthMode, int heightMode, int widthSize, int heightSize, int childCounts) {
        if (childCounts == 0) {
            setMeasuredDimension(0, 0);
            return;
        }
        /**
         * EXACTLY：表示设置了精确的值，一般当childView设置其宽、高为精确值、match_parent时，ViewGroup会将其设置为EXACTLY；
         AT_MOST：表示子布局被限制在一个最大值内，一般当childView设置其宽、高为wrap_content时，ViewGroup会将其设置为AT_MOST；
         UNSPECIFIED：表示子布局想要多大就多大，一般出现在AadapterView的item的heightMode中、ScrollView的childView的heightMode中；此种模式比较少见。
         */
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            //MATCH_PARENT 设置为上层容器推荐的长 宽
//           Log.e(TAG,"ViewGroup,w-"+widthSize+",h-"+heightSize);
            mWidth = widthSize;
            mHeight= heightSize;
            setMeasuredDimension(widthSize, heightSize);
            return;
        }
        int width = 0;
        int height = 0;
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            for (int i = 0; i < childCounts; i++) {
                View childView = getChildAt(i);
                int childWith = childView.getMeasuredWidth();
                int childHeight = childView.getMeasuredHeight();
                MarginLayoutParams clp = (MarginLayoutParams) childView.getLayoutParams();
                width += childWith + clp.leftMargin + clp.rightMargin;
                height = Math.max(height, childHeight + clp.topMargin + clp.bottomMargin);
            }
        } else if (widthMode == MeasureSpec.AT_MOST) {
            for (int i = 0; i < childCounts; i++) {
                View childView = getChildAt(i);
                int childWith = childView.getMeasuredWidth();
                MarginLayoutParams clp = (MarginLayoutParams) childView.getLayoutParams();
                width += childWith + clp.leftMargin + clp.rightMargin;
            }
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            for (int i = 0; i < childCounts; i++) {
                View childView = getChildAt(i);
                int childHeight = childView.getMeasuredHeight();
                MarginLayoutParams clp = (MarginLayoutParams) childView.getLayoutParams();
                height = Math.max(height, childHeight + clp.topMargin + clp.bottomMargin);
            }
            width = widthSize;
        }
//        Log.e(TAG,"ViewGroup,w-"+width+",h-"+height);
        mWidth = width;
        mHeight= height;
        setMeasuredDimension(width, height);
    }

    /**
     * onLayout() 在这个函数中，ViewGroup会拿到childView的getMeasuredWidth() andgetMeasuredHeight()，用来布局所有的childView
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed,l,t,r,b);
        //将子View横向排列
        /*int w = l;//坐标
        float d =  mWidth/getChildCount();
        float diff = 0;
        for (int i = 1; i <=getChildCount(); i++) {
            CircleButton childView = (CircleButton) getChildAt(i-1);
            radius = childView.getRadius();
            int childWith = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();
            MarginLayoutParams clp = (MarginLayoutParams) childView.getLayoutParams();
            Log.e(TAG,"-DIFF-"+diff);
            float left = d*i-d/2-radius;
            float right = left+2*radius;
            w += childWith + clp.leftMargin + clp.rightMargin;
            Log.e(TAG,"left-"+left+",top-"+t+",right-"+right+",height-"+childHeight);
            childView.layout((int)left*//*w - childWith*//*, t, *//*w*//*(int)right, childHeight);

        }*/

    }

    /**
     * 初始化
     */
    private void initData() {
        linePaint = new Paint();
        paint = new Paint();
        path = new Path();
        ripplePaint = new Paint();
        setClickable(true);
    }

    /**
     * 初始化起始位置的中心点
     */
    private void initCenterPoint(float x, float y) {
        firstCenterY = y;
        firstCenterX = x;
    }

    /**
     * TODO: 因为X轴涉及到形变，Y要随X轴的形变而变化，难，不做了
     * 描绘圆的4个点.由动画行进时间决定
     *
     * @param kx              开始位置到结束位置 平移过程中圆形的中心点x坐标的一次函数的斜率
     * @param ky              开始位置到束位置 平移过程中圆形的中心点y坐标的一次函数的斜率
     * @param centerX         中心点x
     * @param centerY         中心点y
     * @param bendingDistance 弹性圆的拉伸长度
     * @return
     */
    private PointF[] initDataPoint(float kx, float ky, float centerX, float centerY, float bendingDistance) {
        for (int i = 0; i < dataPoint.length; i++) {
            if (dataPoint[i] == null) {
                dataPoint[i] = new PointF(0, 0);
                firstDataPoint[i] = new PointF(0, 0);
            }
        }
        boolean isRight = bendingDistance > 0;
//        float bendingDistance = 200/* TODO:拉伸距离 width/4f */;
        /** 这三个数值代表4个平移弹性圆的分界点
         *   以右侧运动为栗子
         *  (0,s1] 右半圆拉伸
         *  (s1,s2] 左半圆拉伸，右半圆逐渐恢复
         *  (s2,s3] 左半圆恢复
         *  (s3,1] 左右半圆一起到达重点，此处应有回弹没做
         * */
        float s1 = 0.3f;
        float s2 = 0.6f;
        float s3 = 0.8f;
        float s4 = 0.9f;

        if (mInterpolatedTime <= 0) {
            dataPoint[0].x = centerX - radius;
            dataPoint[0].y = centerY;

            dataPoint[1].x = centerX;
            dataPoint[1].y = centerY - radius;
            dataPoint[2].x = centerX + radius;
            dataPoint[2].y = centerY;

            dataPoint[3].x = centerX;
            dataPoint[3].y = centerY + radius;

            firstDataPoint[0].x = centerX - radius;
            firstDataPoint[0].y = centerY;

            firstDataPoint[1].x = centerX;
            firstDataPoint[1].y = centerY - radius;
            firstDataPoint[2].x = centerX + radius;
            firstDataPoint[2].y = centerY;

            firstDataPoint[3].x = centerX;
            firstDataPoint[3].y = centerY + radius;
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"1-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"1-dataPoint "+ds.toString()  );
        } else if (mInterpolatedTime > 0 && mInterpolatedTime <= s1) {
            /** 0 - s1 阶段 水平的2个dataPoint，向平移的方向拉伸bendingDistanced的距离，用时为[0,s1]*/
            if (isRight) {
                dataPoint[0].x = firstDataPoint[0].x;
                dataPoint[0].y = firstDataPoint[0].y;
                dataPoint[1].x = firstDataPoint[1].x;
                dataPoint[1].y = firstDataPoint[1].y;
                dataPoint[2].x = firstDataPoint[2].x + bendingDistance / s1 * mInterpolatedTime;
                dataPoint[2].y = firstDataPoint[2].y;
                dataPoint[3].x = firstDataPoint[3].x;
                dataPoint[3].y = firstDataPoint[3].y;
            } else {
                dataPoint[0].x = firstDataPoint[0].x + bendingDistance / s1 * mInterpolatedTime;
                dataPoint[0].y = firstDataPoint[0].y;
                dataPoint[1].x = firstDataPoint[1].x;
                dataPoint[1].y = firstDataPoint[1].y;
                dataPoint[2].x = firstDataPoint[2].x;
                dataPoint[2].y = firstDataPoint[2].y;
                dataPoint[3].x = firstDataPoint[3].x;
                dataPoint[3].y = firstDataPoint[3].y;
            }
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"2-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"2-dataPoint "+ds.toString()  );
        } else if (mInterpolatedTime > s1 && mInterpolatedTime <= s2) {


            if (isRight) {
                /** 向右为例， finalCenterX 是终点s2的中心点坐标，注意:中心点的x行走是规律的是(从头到尾都是 y=kx+b )
                 *  1. 计算垂直方向上的2个点的一次函数表达式，起点是[0,s1]阶段的结束点就是firstDataPoint[1].x，终点就是finalCenterX了..草稿纸计算得如下
                 *  2. 计算水平方向上的2个点的一次函数表达式，d0是左的点，起点是d0_s1,结束点是finalCenterX-radius-bendingDistance,为什么减掉bendingDistance，因为要营造左点回来较慢被拉伸的效果
                 *     d2是右点，起点是d2_s1,结束点是 finalCenterX+radius 用时s2-s1..草稿纸计算得如下
                 * */
                float finalCenterX = firstCenterX + kx * s2;
                float kMiddle_s2 = (finalCenterX - firstDataPoint[1].x) / (s2 - s1);
                float bMiddle_s2 = firstDataPoint[1].x - (finalCenterX - firstDataPoint[1].x) * s1 / (s2 - s1);
                float d2_s1 = firstDataPoint[2].x + bendingDistance;
                float d0_s1 = firstDataPoint[0].x;
                float k0_s2 = (finalCenterX - radius -bendingDistance - d0_s1) / (s2 - s1);
                float b0_s2 = firstDataPoint[0].x - (finalCenterX - radius -bendingDistance- d0_s1) * s1 / (s2 - s1);
                float k2_s2 = (finalCenterX + radius - d2_s1) / (s2 - s1);
                float b2_s2 = d2_s1 - s1 * (finalCenterX + radius - d2_s1) / (s2 - s1);

//                float finalY_3 = firstCenterY+ky*s2;
//                float kMiddleY_3= (finalY_3-firstDataPoint[0].y)/(s2-s1);
//                float bMiddleY_3= firstDataPoint[0].y-(finalY_3-firstDataPoint[0].y)*s1/(s2-s1);
//                float k1_Y_3= (finalY_3-radius - firstDataPoint[1].y)/(s2-s1);
//                float b1_Y_3= firstDataPoint[1].y-(finalY_3-radius - firstDataPoint[1].y)*s1/(s2-s1);
//                float k3_Y_3=(finalY_3+radius - firstDataPoint[2].y)/(s2-s1);
//                float b3_Y_3= firstDataPoint[2].y-(finalY_3+radius - firstDataPoint[2].y)*s1/(s2-s1);

                dataPoint[0].x = k0_s2 * mInterpolatedTime + b0_s2;
                dataPoint[0].y = centerY;
                dataPoint[1].x = kMiddle_s2 * mInterpolatedTime + bMiddle_s2;
                dataPoint[1].y = centerY - radius;
                dataPoint[2].x = k2_s2 * mInterpolatedTime + b2_s2;
                dataPoint[2].y = centerY;
                dataPoint[3].x = kMiddle_s2 * mInterpolatedTime + bMiddle_s2;
                dataPoint[3].y = centerY + radius;
            } else {
                float finalCenterX = firstCenterX + kx * s2;
                float kMiddle = (finalCenterX - firstDataPoint[1].x) / (s2 - s1);
                float bMiddle = firstDataPoint[1].x - (finalCenterX - firstDataPoint[1].x) * s1 / (s2 - s1);
                float d0_s1 = firstDataPoint[0].x + bendingDistance;
                float d2_s1 = firstDataPoint[2].x /*+ bendingDistance*/;
                float k0_s2 = (finalCenterX - radius - d0_s1) / (s2 - s1);
                float b0_s2 = d0_s1 - (finalCenterX - radius - d0_s1) * s1 / (s2 - s1);
                float k2_s2 = (finalCenterX + radius- bendingDistance- d2_s1) / (s2 - s1);
                float b2_s2 = firstDataPoint[2].x - s1 * (finalCenterX + radius- bendingDistance - d2_s1) / (s2 - s1);

//                float finalY_3 = firstCenterY+ky*s2;
//                float kMiddleY_3= (finalY_3-firstDataPoint[0].y)/(s2-s1);
//                float bMiddleY_3= firstDataPoint[0].y-(finalY_3-firstDataPoint[0].y)*s1/(s2-s1);
//                float k1_Y_3= (finalY_3-radius - firstDataPoint[1].y)/(s2-s1);
//                float b1_Y_3= firstDataPoint[1].y-(finalY_3-radius - firstDataPoint[1].y)*s1/(s2-s1);
//                float k3_Y_3=(finalY_3+radius - firstDataPoint[2].y)/(s2-s1);
//                float b3_Y_3= firstDataPoint[2].y-(finalY_3+radius - firstDataPoint[2].y)*s1/(s2-s1);

                dataPoint[0].x = k0_s2 * mInterpolatedTime + b0_s2;
                dataPoint[0].y = centerY;
                dataPoint[1].x = kMiddle * mInterpolatedTime + bMiddle;
                dataPoint[1].y = centerY - radius;
                dataPoint[2].x = k2_s2 * mInterpolatedTime + b2_s2;
                dataPoint[2].y = centerY;
                dataPoint[3].x = kMiddle * mInterpolatedTime + bMiddle;
                dataPoint[3].y = centerY + radius;
            }
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"3-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"3-dataPoint "+ds.toString()  );
        } else if (mInterpolatedTime > s2 && mInterpolatedTime <= s3) {

            if (isRight) {
                /** 向右为例，便于理解，这里把上面的代码copy了一份，要通过上面[s1，s2]来计算s2时间的结束点位置
                 *
                 * */
                float finalCenterX_s2 = firstCenterX + kx * s2;
                float kMiddle_s2 = (finalCenterX_s2 - firstDataPoint[1].x) / (s2 - s1);
                float bMiddle_s2 = firstDataPoint[1].x - (finalCenterX_s2 - firstDataPoint[1].x) * s1 / (s2 - s1);
                float d2_s2 = firstDataPoint[2].x + bendingDistance;
                float d0_s2 = firstDataPoint[0].x + bendingDistance;
                float k0_s2 = (finalCenterX_s2 - radius - d0_s2) / (s2 - s1);
                float b0_s2 = firstDataPoint[0].x - (finalCenterX_s2 - radius - d0_s2) * s1 / (s2 - s1);
                float k2_s2 = (finalCenterX_s2 + radius - d2_s2) / (s2 - s1);
                float b2_s2 = d2_s2 - s1 * (finalCenterX_s2 + radius - d2_s2) / (s2 - s1);

                float finalCenterX_s3 = firstCenterX + kx * s3;
                float kMiddle_s3 = (finalCenterX_s3 - (kMiddle_s2 * s2 + bMiddle_s2)) / (s3 - s2);
                float bMiddle_s3 = (kMiddle_s2 * s2 + bMiddle_s2) - (finalCenterX_s3 - (kMiddle_s2 * s2 + bMiddle_s2)) * s2 / (s3 - s2);
                float k0_s3 = (finalCenterX_s3 - radius - (k0_s2 * s2 + b0_s2)) / (s3 - s2);
                float b0_s3 = (k0_s2 * s2 + b0_s2) - (finalCenterX_s3 - radius - (k0_s2 * s2 + b0_s2)) * s2 / (s3 - s2);
                float k2_s3 = (finalCenterX_s3 + radius - (k2_s2 * s2 + b2_s2)) / (s3 - s2);
                float b2_s3 = k2_s2 * s2 + b2_s2 - s2 * (finalCenterX_s3 + radius - (k2_s2 * s2 + b2_s2)) / (s3 - s2);

//                float finalY_3 = firstCenterY+ky*s2;
//                float kMiddleY_3= (finalY_3-firstDataPoint[0].y)/(s2-s1);
//                float bMiddleY_3= firstDataPoint[0].y-(finalY_3-firstDataPoint[0].y)*s1/(s2-s1);
//                float k1_Y_3= (finalY_3-radius - firstDataPoint[1].y)/(s2-s1);
//                float b1_Y_3= firstDataPoint[1].y-(finalY_3-radius - firstDataPoint[1].y)*s1/(s2-s1);
//                float k3_Y_3=(finalY_3+radius - firstDataPoint[2].y)/(s2-s1);
//                float b3_Y_3= firstDataPoint[2].y-(finalY_3+radius - firstDataPoint[2].y)*s1/(s2-s1);
//
//                float finalY_4 = firstCenterY+ky*s3;
//                float kMiddleY_4= (finalY_4-kMiddleY_3*s2-bMiddleY_3)/(s2-s1);
//                float bMiddleY_4=kMiddleY_3*s2+bMiddleY_3-(finalY_4-kMiddleY_3*s2-bMiddleY_3)*s1/(s2-s1);
//                float k1_Y_4= (finalY_4-radius - k1_Y_3*s2-b1_Y_3)/(s2-s1);
//                float b1_Y_4= k1_Y_3*s2+b1_Y_3-(finalY_4-radius - k1_Y_3*s2-b1_Y_3)*s1/(s2-s1);
//                float k3_Y_4=(finalY_4+radius - k3_Y_3*mInterpolatedTime-b3_Y_3)/(s2-s1);
//                float b3_Y_4= k3_Y_3*mInterpolatedTime+b3_Y_3 - (finalY_4+radius - k3_Y_3*mInterpolatedTime-b3_Y_3)*s1/(s2-s1);

                dataPoint[0].x = k0_s3 * mInterpolatedTime + b0_s3;
                dataPoint[0].y = centerY;
                dataPoint[1].x = kMiddle_s3 * mInterpolatedTime + bMiddle_s3;
                dataPoint[1].y = centerY - radius;
                dataPoint[2].x = k2_s3 * mInterpolatedTime + b2_s3;
                dataPoint[2].y = centerY;
                dataPoint[3].x = kMiddle_s3 * mInterpolatedTime + bMiddle_s3;
                dataPoint[3].y = centerY + radius;
            } else {
                float finalCenterX_s3 = firstCenterX + kx * s2;
                float kMiddle_s3 = (finalCenterX_s3 - firstDataPoint[1].x) / (s2 - s1);
                float bMiddle_s3 = firstDataPoint[1].x - (finalCenterX_s3 - firstDataPoint[1].x) * s1 / (s2 - s1);
                float d0_s3 = firstDataPoint[0].x + bendingDistance;
                float d2_s3 = firstDataPoint[2].x + bendingDistance;
                float k0_s3 = (finalCenterX_s3 - radius - d0_s3) / (s2 - s1);
                float b0_s3 = d0_s3 - (finalCenterX_s3 - radius - d0_s3) * s1 / (s2 - s1);
                float k2_s3 = (finalCenterX_s3 + radius - d2_s3) / (s2 - s1);
                float b2_s3 = firstDataPoint[2].x - s1 * (finalCenterX_s3 + radius - d2_s3) / (s2 - s1);

                float finalCenterX_s4 = firstCenterX + kx * s3;
                float kMiddle_s4 = (finalCenterX_s4 - (kMiddle_s3 * s2 + bMiddle_s3)) / (s3 - s2);
                float bMiddle_s4 = kMiddle_s3 * s2 + bMiddle_s3 - (finalCenterX_s4 - (kMiddle_s3 * s2 + bMiddle_s3)) * s2 / (s3 - s2);
                float k0_s4 = (finalCenterX_s4 - radius - (k0_s3 * s2 + b0_s3)) / (s3 - s2);
                float b0_s4 = (k0_s3 * s2 + b0_s3) - (finalCenterX_s4 - radius - (k0_s3 * s2 + b0_s3)) * s2 / (s3 - s2);
                float k2_s4 = (finalCenterX_s4 + radius - (k2_s3 * s2 + b2_s3)) / (s3 - s2);
                float b2_s4 = (k2_s3 * s2 + b2_s3) - s2 * (finalCenterX_s4 + radius - (k2_s3 * s2 + b2_s3)) / (s3 - s2);

//                float finalY_3 = firstCenterY+ky*s2;
//                float kMiddleY_3= (finalY_3-firstDataPoint[0].y)/(s2-s1);
//                float bMiddleY_3= firstDataPoint[0].y-(finalY_3-firstDataPoint[0].y)*s1/(s2-s1);
//                float k1_Y_3= (finalY_3-radius - firstDataPoint[1].y)/(s2-s1);
//                float b1_Y_3= firstDataPoint[1].y-(finalY_3-radius - firstDataPoint[1].y)*s1/(s2-s1);
//                float k3_Y_3=(finalY_3+radius - firstDataPoint[2].y)/(s2-s1);
//                float b3_Y_3= firstDataPoint[2].y-(finalY_3+radius - firstDataPoint[2].y)*s1/(s2-s1);
//
//                float finalY_4 = firstCenterY+ky*s3;
//                float kMiddleY_4= (finalY_4-kMiddleY_3*s2-bMiddleY_3)/(s2-s1);
//                float bMiddleY_4=kMiddleY_3*s2+bMiddleY_3-(finalY_4-kMiddleY_3*s2-bMiddleY_3)*s1/(s2-s1);
//                float k1_Y_4= (finalY_4-radius - k1_Y_3*s2-b1_Y_3)/(s2-s1);
//                float b1_Y_4= k1_Y_3*s2+b1_Y_3-(finalY_4-radius - k1_Y_3*s2-b1_Y_3)*s1/(s2-s1);
//                float k3_Y_4=(finalY_4+radius - k3_Y_3*mInterpolatedTime-b3_Y_3)/(s2-s1);
//                float b3_Y_4= k3_Y_3*mInterpolatedTime+b3_Y_3 - (finalY_4+radius - k3_Y_3*mInterpolatedTime-b3_Y_3)*s1/(s2-s1);

                dataPoint[0].x = k0_s4 * mInterpolatedTime + b0_s4;
                dataPoint[0].y = centerY;
                dataPoint[1].x = kMiddle_s4 * mInterpolatedTime + bMiddle_s4;
                dataPoint[1].y = centerY - radius;
                dataPoint[2].x = k2_s4 * mInterpolatedTime + b2_s4;
                dataPoint[2].y = centerY;
                dataPoint[3].x = kMiddle_s4 * mInterpolatedTime + bMiddle_s4;
                dataPoint[3].y = centerY + radius;
            }
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"4-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"4-dataPoint "+ds.toString()  );
        } else {
            if(isRight) {
                if(mInterpolatedTime>s3&&mInterpolatedTime<=s4) {
                    float d =  radius;
                    float k = d*0.25f/(s3-s4);
                    float b = d*0.25f-k*s3;
                    dataPoint[0].x = centerX-(d*3f/4f + k*mInterpolatedTime+b);
                    dataPoint[0].y = centerY;

                    dataPoint[1].x = centerX;
                    dataPoint[1].y = centerY - radius;
                    dataPoint[2].x = centerX + radius;
                    dataPoint[2].y = centerY;

                    dataPoint[3].x = centerX;
                    dataPoint[3].y = centerY + radius;
                }else if(mInterpolatedTime>s4&&mInterpolatedTime<=1){
                    float d = radius;
                    float k = -d*0.25f/(s4-1);
                    float b = -k*s4;
                    dataPoint[0].x = centerX-(d*3f/4f + k*mInterpolatedTime+b);
                    dataPoint[0].y = centerY;

                    dataPoint[1].x = centerX;
                    dataPoint[1].y = centerY - radius;
                    dataPoint[2].x = centerX + radius;
                    dataPoint[2].y = centerY;

                    dataPoint[3].x = centerX;
                    dataPoint[3].y = centerY + radius;
                }
            }else {
                if(mInterpolatedTime>s3&&mInterpolatedTime<=s4) {
                    float d =  radius;
                    float k = d*0.25f/(s3-s4);
                    float b = d*0.25f-k*s3;
                    dataPoint[0].x = centerX - radius;
                    dataPoint[0].y = centerY;

                    dataPoint[1].x = centerX;
                    dataPoint[1].y = centerY - radius;
                    dataPoint[2].x = centerX+d*3f/4f +k*mInterpolatedTime+b ;
                    dataPoint[2].y = centerY;

                    dataPoint[3].x = centerX;
                    dataPoint[3].y = centerY + radius;
                }else if(mInterpolatedTime>s4&&mInterpolatedTime<=1){
                    float d = radius;
                    float k = -d*0.25f/(s4-1);
                    float b = -k*s4;
                    dataPoint[0].x = centerX - radius;
                    dataPoint[0].y = centerY;

                    dataPoint[1].x = centerX;
                    dataPoint[1].y = centerY - radius;
                    dataPoint[2].x = centerX+d*3f/4f +k*mInterpolatedTime+b;
                    dataPoint[2].y = centerY;

                    dataPoint[3].x = centerX;
                    dataPoint[3].y = centerY + radius;
                }
            }
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"5-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
//            Log.i(TAG,"5-dataPoint "+ds.toString()  );
        }
        return dataPoint;
    }

    /**
     * 初始化控制点
     *
     * @param dataPoint 圆的4个数据点
     * @return
     */
    private PointF[] initCtrlPoint(PointF[] dataPoint) {
        for (int i = 0; i < ctrlPoint.length; i++) {
            ctrlPoint[i] = new PointF(0, 0);
        }
        diff = radius * C;
        ctrlPoint[0].x = dataPoint[0].x;
        ctrlPoint[0].y = dataPoint[0].y - diff;

        ctrlPoint[1].x = dataPoint[1].x - diff;
        ctrlPoint[1].y = dataPoint[1].y;
        ctrlPoint[2].x = dataPoint[1].x + diff;
        ctrlPoint[2].y = dataPoint[1].y;

        ctrlPoint[3].x = dataPoint[2].x;
        ctrlPoint[3].y = dataPoint[2].y - diff;
        ctrlPoint[4].y = dataPoint[2].y + diff;
        ctrlPoint[4].x = dataPoint[2].x;

        ctrlPoint[5].y = dataPoint[3].y;
        ctrlPoint[5].x = dataPoint[3].x + diff;
        ctrlPoint[6].y = dataPoint[3].y;
        ctrlPoint[6].x = dataPoint[3].x - diff;

        ctrlPoint[7].x = dataPoint[0].x;
        ctrlPoint[7].y = dataPoint[0].y + diff;
        return ctrlPoint;

    }

    /**
     * 绘制贝塞尔曲线
     *
     * @param canvas
     */
    private void drawCubicBezier(Canvas canvas) {
        /** 清除Path中的内容
         reset不保留内部数据结构，但会保留FillType.
         rewind会保留内部的数据结构，但不保留FillType */
        path.reset();
        path.moveTo(dataPoint[0].x, dataPoint[0].y);

        path.cubicTo(ctrlPoint[0].x, ctrlPoint[0].y, ctrlPoint[1].x, ctrlPoint[1].y, dataPoint[1].x, dataPoint[1].y);
        path.cubicTo(ctrlPoint[2].x, ctrlPoint[2].y, ctrlPoint[3].x, ctrlPoint[3].y, dataPoint[2].x, dataPoint[2].y);
        path.cubicTo(ctrlPoint[4].x, ctrlPoint[4].y, ctrlPoint[5].x, ctrlPoint[5].y, dataPoint[3].x, dataPoint[3].y);
        path.cubicTo(ctrlPoint[6].x, ctrlPoint[6].y, ctrlPoint[7].x, ctrlPoint[7].y, dataPoint[0].x, dataPoint[0].y);
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);

    }

    /**
     * 重绘的过程不会回调这个方法-，-
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     * 重绘的回调
     *
     * @param canvas
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
//        Log.i(TAG,"dispatchDraw");
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        if (pointLimitQueue.size() == 0) {
//            Log.i(TAG,"dispatchDraw 0");
            return;
        } else if (pointLimitQueue.size() == 1) {
            /** 队列中只有一个点 */
            Log.i(TAG, "dispatchDraw 1");
            PointF p = pointLimitQueue.getFirst();
            initCenterPoint(p.x, p.y);
            dataPoint = initDataPoint(0, 0, p.x, p.y, 0);
            initCtrlPoint(dataPoint);
            drawCubicBezier(canvas);
            /** 此时队列中只有一个点，则描绘边界*/
            ripple(p, canvas);
        } else if (pointLimitQueue.size() == 2) {
            /** 队列有2个点则重置点的数据，并执行动画重绘界面*/
            Log.i(TAG, " dispatchDraw 2  pointLimitQueue-" + pointLimitQueue.queue.toString()+",mInterpolatedTime-"+mInterpolatedTime);
            /**麻烦事情 开始吧*/
            PointF pFirst = pointLimitQueue.get(0);
            PointF pLast = pointLimitQueue.get(1);
            float xFirst = pFirst.x;
            float yFirst = pFirst.y;
            float xLast = pLast.x;
            float yLast = pLast.y;
            float xWidth = xLast - xFirst;
            float yWidth = yLast - yFirst;
            float bendingDistance = xWidth / 4f;
            kx = xWidth;
            ky = yWidth;
            float resultX = firstCenterX + kx * mInterpolatedTime;
            float resultY = firstCenterY + ky * mInterpolatedTime;
            dataPoint = initDataPoint(kx, ky, resultX, resultY, bendingDistance);
            initCtrlPoint(dataPoint);
            drawCubicBezier(canvas);

        }
    }

    /**
     * 画笔设置为描边，指定描边的宽度和透明度(随时间而变化)
     *
     * @param p
     * @param canvas
     */
    private void ripple(PointF p, Canvas canvas) {
//        画笔设置为描边，指定描边的宽度和透明度(随时间而变化)
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setColor(Color.WHITE);
        ripplePaint.setStrokeWidth(rippleWidth * rippleInterpolatedTime);
        ripplePaint.setAlpha(255 - (int) (255 * rippleInterpolatedTime));
        float rw = rippleWidth * rippleInterpolatedTime;
        CircleButton circleButton = (CircleButton) isInChildView(p.x, p.y);
        canvas.drawCircle(p.x, p.y, circleButton.getRadius() + rw / 2, ripplePaint);

    }

    @Override
    public void invalidate() {
        super.invalidate();
        handler.sendEmptyMessage(1);
    }

    /**
     * 事件分发
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(TAG,"dispatchTouchEvent");
        int a = ev.getAction();
        switch (a) {
            case MotionEvent.ACTION_DOWN:
                ex = ev.getX();
                ey = ev.getY();
                PointF p = new PointF();
                Log.i(TAG, "p-" + ex + "," + ey);
                View v = isInChildView(ex, ey);
                if (v != null&&!isTranslateOrRippleInProgress()) {
                    /** 当且仅当 触摸点DOWN在子View内 以及动画结束的时候进入*/
                    /** 当触摸点DOWN在子View内，触发一个描边波浪效果 */
                    startRipple();
                    int top = v.getTop();
                    int bottom = v.getBottom();
                    int left = v.getLeft();
                    int right = v.getRight();
                    p.x = left + (right - left) / 2f;
                    p.y = top + (bottom - top) / 2f;
                    pointLimitQueue.offer(p);
                    Log.i(TAG, "ps-" + pointLimitQueue.queue.toString());
                }
                if (pointLimitQueue.size() == 1) {
                    //如果队列中只有一个点，那么直接调用重绘
                    invalidate();
                } else if (pointLimitQueue.size() > 1) {
                    //一个点以上，则开始圆形平移的动画
                    startCircleMoving();
                }
                Log.e(TAG, "dispatchTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG,"dispatchTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
//                Log.e(TAG,"dispatchTouchEvent ACTION_UP");
                break;
        }
/*        if(isTranslateOrRippleInProgress()){
            Log.e(TAG,"动画进行中");
            return true;
        }*/
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 事件处理
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(TAG, "onTouchEvent");// TODO: 2017/2/6 当 onInterceptTouchEvent返回true，这里怎么没有被触发？
        int a = event.getAction();
        switch (a) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "onTouchEvent ACTION_DOWN");
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

    /**
     * 事件拦截
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e(TAG,"onInterceptTouchEvent");
        if(isTranslateOrRippleInProgress()
                &&pointLimitQueue.getLast().equals(getChildViewCenterPointToQueue(getAttachView().getCurrentPageIndex()))){
            /** 当且仅当 动画进行中以及当前滑动的终点页面的下标所对应的PointF是点队列中的最后一个时候，不需要传递事件给子View,*/
                    return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 开始描边波浪动画
     */
    public void startRipple() {
        handler.sendEmptyMessage(3);
    }

    /**
     * 开始弹性圆形移动动画
     */
    public void startCircleMoving() {
        handler.sendEmptyMessage(0);
    }

    /**
     * 点击位置是否在某个子View内,如果在，返回这个View，不在返回null
     *
     * @param x
     * @param y
     * @return
     */
    private View isInChildView(float x, float y) {
        View v = null;
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            int top = childView.getTop();
            int bottom = childView.getBottom();
            int left = childView.getLeft();
            int right = childView.getRight();
            boolean inWidth = x >= left && x <= right;
            boolean inHeight = y >= top && y <= bottom;
            boolean isInChildView = inWidth && inHeight;
            if (isInChildView) {
                v = childView;
                break;
            }
        }
        return v;
    }

    /**
     * 获取子View的中心点坐标,并加入点队列
     * @return
     */
    public PointF addChildViewCenterPointToQueue(View childView) {
        int top = childView.getTop();
        int bottom = childView.getBottom();
        int left = childView.getLeft();
        int right = childView.getRight();
        PointF p = new PointF();
        p.x = left + (right - left) / 2f;
        p.y = top + (bottom - top) / 2f;
        pointLimitQueue.offer(p);
        return p;
    }

    /**
     * 获取子View的中心点坐标,并加入点队列
     *
     * @param index 子View的在父容器中的计数下标
     * @return
     */
    public PointF addChildViewCenterPointToQueue(int index) {
//        Log.i(TAG,"ps 1-"+pointLimitQueue.queue.toString());
        View childView = getChildAt(index);
        int top = childView.getTop();
        int bottom = childView.getBottom();
        int left = childView.getLeft();
        int right = childView.getRight();
        PointF p = new PointF();
        p.x = left + (right - left) / 2f;
        p.y = top + (bottom - top) / 2f;
        pointLimitQueue.offer(p);
//        Log.i(TAG,"ps 2-"+pointLimitQueue.queue.toString());
        return p;
    }

    public PointF getChildViewCenterPointToQueue(int index) {
//        Log.i(TAG,"ps 1-"+pointLimitQueue.queue.toString());
        View childView = getChildAt(index);
        int top = childView.getTop();
        int bottom = childView.getBottom();
        int left = childView.getLeft();
        int right = childView.getRight();
        PointF p = new PointF();
        p.x = left + (right - left) / 2f;
        p.y = top + (bottom - top) / 2f;
//        Log.i(TAG,"ps 2-"+pointLimitQueue.queue.toString());
        return p;
    }

    /**
     *
     */
    public void showTheFirst() {
        addChildViewCenterPointToQueue(0);
        invalidate();
    }

/*    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }*/


    /**
     * 这里使用LinkList(实现了Queue接口，有队列的性质)，
     * 这是一个可指定长度的Queue(LinkedList)
     *
     * @param <E>
     */
    class LimitQueue<E> {

        private int limit; // 队列长度

        public LinkedList<E> queue = new LinkedList<E>();

        public LimitQueue(int limit) {
            this.limit = limit;
        }

        /**
         * 入列：当队列大小已满时，把队头的元素poll掉
         * 由于存在调用了showFirst()事先offer第一个点进去，因此需要把这个点去掉
         */
        public void offer(E e) {
            if (queue.size() >= limit) {
                queue.poll();
            }
            if (queue.size() == 1) {
                if (queue.getFirst().equals(e)) {
                    return;
                }
            }
            queue.offer(e);
        }

        public void poll() {
            queue.poll();
        }

        public E get(int position) {
            return queue.get(position);
        }

        public E getLast() {
            return queue.getLast();
        }

        public E getFirst() {
            return queue.getFirst();
        }

        public int getLimit() {
            return limit;
        }

        public int size() {
            return queue.size();
        }

    }

    /**
     * 平移动画类
     */
    private class TranslateAnimation extends Animation {
        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            /** mInterpolatedTime [0,1],可以看成是动画执行时间，全程为1，从0开始*/
//            Log.e(TAG,"interpolatedTime "+interpolatedTime);
            mInterpolatedTime = interpolatedTime;
            if (mInterpolatedTime > 0) {
                translateState = STATE_START;
            }
            if (mInterpolatedTime == 1) {
                translateState = STATE_STOP;
            }
            if (mInterpolatedTime < 1) {
                translateState = STATE_MOVING;
            }
            if (mInterpolatedTime != 1) {
                //如果执行事件不是1的话，持续执行重绘
                invalidate();
            }
            if (mInterpolatedTime == 1 && pointLimitQueue.size() >= 2) {
                //执行完成之后，并且队列中有2个，出队一个，表明一次平移执行完成之后，队列中只能存在一个点
                pointLimitQueue.poll();
            }
        }

    }

    /**
     * 开始弹性平移动画
     */
    public void startTranslateAnimation() {
        mInterpolatedTime = 0;
        move.setDuration(1000);
        move.setAnimationListener(new TranslateAnimatonListener());
        move.setInterpolator(new AccelerateDecelerateInterpolator());
        startAnimation(move);
    }

    /**
     * 描边波浪动画
     */
    class RippleScaleAnimation extends Animation {
        public RippleScaleAnimation() {

        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
//            Log.e("RippleScaleAnimation","interpolatedTime "+interpolatedTime+","+hasEnded());
            rippleInterpolatedTime = interpolatedTime;
            if (rippleInterpolatedTime > 0) {
                rippleState = STATE_START;
            }
            if (rippleInterpolatedTime == 1) {
                rippleState = STATE_STOP;
            }
            if (rippleInterpolatedTime < 1) {
                rippleState = STATE_MOVING;
            }
            invalidate();
        }
    }

    /**
     * 开始描边波浪动画
     */
    private void startRippleScaleAnimation() {
        rippleInterpolatedTime = 0;
        rippleScaleAnimation.setDuration(500);
        rippleScaleAnimation.setAnimationListener(new RippleAnimatonListener());
        rippleScaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        startAnimation(rippleScaleAnimation);
    }

    /**
     * 平移动画的监听器
     * 在结束的时候，停止平移的定时器，并且启动描边波浪的动画
     */
    class TranslateAnimatonListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Log.e("TranslateListener", "onAnimationEnd " + translateState);
            handler.sendEmptyMessage(1);//结束定时器
            handler.sendEmptyMessage(3);//开启描边波浪效果
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * 描边动画的监听器
     */
    class RippleAnimatonListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
//            rippleState = STATE_START;
            Log.e("RippleAnimatonListener", "onAnimationStart " + rippleState);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
//            rippleState = STATE_STOP;
            Log.e("RippleAnimatonListener", "onAnimationEnd " + rippleState);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }


}
