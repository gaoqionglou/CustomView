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
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author gql
 */

public class CustomViewGroup extends ViewGroup {
    public static final String TAG="GAOVG-CustomViewGroup";
    private static final float C = 0.551915024494f;     // 一个常量，用来计算绘制圆形贝塞尔曲线控制点的位置
    private float radius=20;
    private float diff = radius*C;        // 圆形的控制点与数据点的差值
    private float firstCenterX,firstCenterY;
    private Paint linePaint;
    private Paint paint;
    private Path path ;
    private PointF[] dataPoint = new PointF[4];
    private PointF[] firstDataPoint = new PointF[4];
    private PointF[] ctrlPoint = new PointF[8];

    public static final int STATE_START = 0;
    public static final int STATE_MOVING = 1;
    public static final int STATE_STOP = 2;
    private float kx,ky ;


    float ex;
    float ey;
    LimitQueue<PointF> pointLimitQueue = new LimitQueue<>(100);
    private float mInterpolatedTime=0f;//0-1的播放时间
    private TranslteAnimaton move = new TranslteAnimaton();
    private int state = -1;
private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 0:
                // 移除所有的msg.what为0等消息，保证只有一个循环消息队列再跑
                handler.removeMessages(0);
                if(state !=STATE_START&&pointLimitQueue.size()>1) startTranslteAnimaton();
                // 再次发出msg，循环更新
                handler.sendEmptyMessageDelayed(0, 1000);
                break;

            case 1:
                // 直接移除，定时器停止
                handler.removeMessages(0);
                break;

            default:
                break;
        }

    }
};
    public CustomViewGroup(Context context) {
        this(context,null);
    }

    public CustomViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public CustomViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     *  测量子View 得出父View的大小
     *  onMeasure() 在这个函数中，ViewGroup会接受childView的请求的大小，
     *  然后通过childView的 measure(newWidthMeasureSpec, heightMeasureSpec)函数存储到childView中，
     *  以便childView的getMeasuredWidth() andgetMeasuredHeight() 的值可以被后续工作得到
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        /**
         * 算出所有的childView的宽和高,
         */
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        /**
         * 下面处理ViewGroup 长宽是wrapcontent
         */
        int width = 0;
        int height = 0;
        MarginLayoutParams layoutParams = null;
        int counts = getChildCount();
        measure(widthMode,heightMode,widthSize,heightSize,counts);

    }


    private void measure(int widthMode,int heightMode,int widthSize,int heightSize,int childCounts){
       if(childCounts==0){
           setMeasuredDimension(0,0);
           return;
       }
        /**
         * EXACTLY：表示设置了精确的值，一般当childView设置其宽、高为精确值、match_parent时，ViewGroup会将其设置为EXACTLY；
         AT_MOST：表示子布局被限制在一个最大值内，一般当childView设置其宽、高为wrap_content时，ViewGroup会将其设置为AT_MOST；
         UNSPECIFIED：表示子布局想要多大就多大，一般出现在AadapterView的item的heightMode中、ScrollView的childView的heightMode中；此种模式比较少见。
         */
       if(widthMode==MeasureSpec.EXACTLY&&heightMode==MeasureSpec.EXACTLY){
           //MATCH_PARENT 设置为上层容器推荐的长 宽
//           Log.e(TAG,"ViewGroup,w-"+widthSize+",h-"+heightSize);
           setMeasuredDimension(widthSize,heightSize);
           return;
       }
       int width =0;
       int height = 0;
       if(widthMode==MeasureSpec.AT_MOST&&heightMode==MeasureSpec.AT_MOST){
           for(int i=0;i<childCounts;i++){
               View childView = getChildAt(i);
               int childWith = childView.getMeasuredWidth();
               int childHeight = childView.getMeasuredHeight();
               MarginLayoutParams clp= (MarginLayoutParams)childView.getLayoutParams();
               width+=childWith+clp.leftMargin+clp.rightMargin;
               height=Math.max(height,childHeight+clp.topMargin+clp.bottomMargin);
           }
       }else if(widthMode==MeasureSpec.AT_MOST){
           for(int i=0;i<childCounts;i++){
               View childView = getChildAt(i);
               int childWith = childView.getMeasuredWidth();
               MarginLayoutParams clp= (MarginLayoutParams)childView.getLayoutParams();
               width+=childWith+clp.leftMargin+clp.rightMargin;
           }
           height = heightSize;
       }else if(heightMode==MeasureSpec.AT_MOST){
           for(int i=0;i<childCounts;i++){
               View childView = getChildAt(i);
               int childHeight = childView.getMeasuredHeight();
               MarginLayoutParams clp= (MarginLayoutParams)childView.getLayoutParams();
               height=Math.max(height,childHeight+clp.topMargin+clp.bottomMargin);
           }
           width = widthSize;
       }
//        Log.e(TAG,"ViewGroup,w-"+width+",h-"+height);
       setMeasuredDimension(width,height);
    }

    /**
     * onLayout() 在这个函数中，ViewGroup会拿到childView的getMeasuredWidth() andgetMeasuredHeight()，用来布局所有的childView
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //将子View横向排列
        int w=l;//坐标
        int h=t;//
        for(int i=0;i<getChildCount();i++){
            View childView = getChildAt(i);
            int childWith = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();
            MarginLayoutParams clp = (MarginLayoutParams)childView.getLayoutParams();
            w+=childWith+clp.leftMargin+clp.rightMargin;
//            Log.e(TAG,"CHILD onLayout: "+(i+1)+",l-"+(l+w-childWith)+",t-"+t+",r-"+w+",b-"+b);
            childView.layout(l+w-childWith,t,w,childHeight);
        }

    }

    private void initData(){
/*      应该不用这里做
        dataPoint = initDataPoint(firstCenterX,firstCenterY);
        initCtrlPoint(dataPoint);*/

        linePaint = new Paint();
        paint = new Paint();
        path = new Path();
    }
    private void initCenterPoint(float x,float y){
        firstCenterY = y;
        firstCenterX = x;
    }

    private PointF[] initDataPoint(float d ,float centerX,float centerY,float bendingDistance){
        for(int i=0;i<dataPoint.length;i++){
            if(dataPoint[i]==null) {
                dataPoint[i] = new PointF(0, 0);
                firstDataPoint[i] = new PointF(0, 0);
            }
        }
        boolean isRight = bendingDistance>0;
//        float bendingDistance = 200/* TODO:拉伸距离 width/4f */;
        float s1 = 0.3f;
        float s2 = 0.6f;
        float s3 = 0.9f;

        if(mInterpolatedTime<=0){
            dataPoint[0].x=centerX-radius;
            dataPoint[0].y=centerY;

            dataPoint[1].x=centerX;
            dataPoint[1].y=centerY-radius;
            dataPoint[2].x=centerX+radius;
            dataPoint[2].y=centerY;

            dataPoint[3].x=centerX;
            dataPoint[3].y=centerY+radius;

            firstDataPoint[0].x=centerX-radius;
            firstDataPoint[0].y=centerY;

            firstDataPoint[1].x=centerX;
            firstDataPoint[1].y=centerY-radius;
            firstDataPoint[2].x=centerX+radius;
            firstDataPoint[2].y=centerY;

            firstDataPoint[3].x=centerX;
            firstDataPoint[3].y=centerY+radius;
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"1-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
            Log.i(TAG,"1-dataPoint "+ds.toString()  );
        }else if(mInterpolatedTime>0&&mInterpolatedTime<=s1){
            if(isRight) {
                dataPoint[0].x = firstDataPoint[0].x;
                dataPoint[0].y = firstDataPoint[0].y;
                dataPoint[1].x = firstDataPoint[1].x;
                dataPoint[1].y = firstDataPoint[1].y;
                dataPoint[2].x = firstDataPoint[2].x + bendingDistance / s1 * mInterpolatedTime;
                dataPoint[2].y = firstDataPoint[2].y;
                dataPoint[3].x = firstDataPoint[3].x;
                dataPoint[3].y = firstDataPoint[3].y;
            }else {
                dataPoint[0].x = firstDataPoint[0].x+  bendingDistance / s1 * mInterpolatedTime;
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
            Log.i(TAG,"2-dataPoint "+ds.toString()  );
        }else if(mInterpolatedTime>s1&&mInterpolatedTime<=s2){



            if(isRight){
                float finalX_3= firstCenterX+d*s2;
                float kMiddle_3 = (finalX_3-firstDataPoint[1].x)/(s2-s1);
                float bMiddle_3 = firstDataPoint[1].x-(finalX_3-firstDataPoint[1].x)*s1/(s2-s1);
                float d2_3 = firstDataPoint[2].x+bendingDistance;
                float d0_3 = firstDataPoint[0].x+bendingDistance;
                float k0_3 = (finalX_3-radius-d0_3)/(s2-s1);
                float b0_3 = firstDataPoint[0].x-(finalX_3-radius-d0_3)*s1/(s2-s1);
                float k2_3 = (finalX_3+radius- d2_3)/(s2-s1);
                float b2_3 = d2_3 - s1*(finalX_3+radius-d2_3)/(s2-s1);
                dataPoint[0].x= k0_3*mInterpolatedTime+b0_3;
                dataPoint[0].y = centerY;
                dataPoint[1].x = kMiddle_3*mInterpolatedTime+bMiddle_3;
                dataPoint[1].y = centerY-radius;
                dataPoint[2].x = k2_3*mInterpolatedTime+b2_3;
                dataPoint[2].y = centerY;
                dataPoint[3].x = kMiddle_3*mInterpolatedTime+bMiddle_3;
                dataPoint[3].y = centerY+radius;
            }else{
                float finalX_3= firstCenterX+d*s2;
                float kMiddle_3 = (finalX_3-firstDataPoint[1].x)/(s2-s1);
                float bMiddle_3 = firstDataPoint[1].x-(finalX_3-firstDataPoint[1].x)*s1/(s2-s1);
                float d0_3 = firstDataPoint[0].x+bendingDistance;
                float d2_3 = firstDataPoint[2].x+bendingDistance;
                float k0_3 = (finalX_3-radius-d0_3)/(s2-s1);
                float b0_3 = d0_3-(finalX_3-radius-d0_3)*s1/(s2-s1);
                float k2_3 = (finalX_3+radius-d2_3)/(s2-s1);
                float b2_3 = firstDataPoint[2].x - s1*(finalX_3+radius-d2_3)/(s2-s1);
                dataPoint[0].x= k0_3*mInterpolatedTime+b0_3;
                dataPoint[0].y = centerY;
                dataPoint[1].x = kMiddle_3*mInterpolatedTime+bMiddle_3;
                dataPoint[1].y = centerY-radius;
                dataPoint[2].x = k2_3*mInterpolatedTime+b2_3;
                dataPoint[2].y = centerY;
                dataPoint[3].x = kMiddle_3*mInterpolatedTime+bMiddle_3;
                dataPoint[3].y = centerY+radius;
            }
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"3-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
            Log.i(TAG,"3-dataPoint "+ds.toString()  );
        }else if(mInterpolatedTime>s2&&mInterpolatedTime<=s3){

            if(isRight) {
                float finalX_3= firstCenterX+d*s2;
                float kMiddle_3 = (finalX_3-firstDataPoint[1].x)/(s2-s1);
                float bMiddle_3 = firstDataPoint[1].x-(finalX_3-firstDataPoint[1].x)*s1/(s2-s1);
                float d2_3 = firstDataPoint[2].x+bendingDistance;
                float d0_3 = firstDataPoint[0].x+bendingDistance;
                float k0_3 = (finalX_3-radius-d0_3)/(s2-s1);
                float b0_3 = firstDataPoint[0].x-(finalX_3-radius-d0_3)*s1/(s2-s1);
                float k2_3 = (finalX_3+radius- d2_3)/(s2-s1);
                float b2_3 = d2_3 - s1*(finalX_3+radius-d2_3)/(s2-s1);

                float finalX_4 = firstCenterX+d*s3;
                float kMiddle_4 = (finalX_4-kMiddle_3*s2-bMiddle_3)/(s3-s2);
                float bMiddle_4 = kMiddle_3*s2+bMiddle_3-(finalX_4-kMiddle_3*s2-bMiddle_3)*s2/(s3-s2);
                float k0_4 = (finalX_4-radius-k0_3*s2-b0_3)/(s3-s2);
                float b0_4 = k0_3*s2+b0_3-(finalX_4-radius-k0_3*s2-b0_3)*s2/(s3-s2);
                float k2_4 = (finalX_4+radius- k2_3*s2-b2_3)/(s3-s2);
                float b2_4 = k2_3*s2+b2_3 - s2*(finalX_4+radius- k2_3*s2-b2_3)/(s3-s2);

                dataPoint[0].x = k0_4 * mInterpolatedTime + b0_4;
                dataPoint[0].y = centerY;
                dataPoint[1].x = kMiddle_4 * mInterpolatedTime + bMiddle_4;
                dataPoint[1].y = centerY - radius;
                dataPoint[2].x = k2_4 * mInterpolatedTime + b2_4;
                dataPoint[2].y = centerY;
                dataPoint[3].x = kMiddle_4 * mInterpolatedTime + bMiddle_4;
                dataPoint[3].y = centerY + radius;
            }else {
                float finalX_3= firstCenterX+d*s2;
                float kMiddle_3 = (finalX_3-firstDataPoint[1].x)/(s2-s1);
                float bMiddle_3 = firstDataPoint[1].x-(finalX_3-firstDataPoint[1].x)*s1/(s2-s1);
                float d0_3 = firstDataPoint[0].x+bendingDistance;
                float d2_3 = firstDataPoint[2].x+bendingDistance;
                float k0_3 = (finalX_3-radius-d0_3)/(s2-s1);
                float b0_3 = d0_3-(finalX_3-radius-d0_3)*s1/(s2-s1);
                float k2_3 = (finalX_3+radius-d2_3)/(s2-s1);
                float b2_3 = firstDataPoint[2].x - s1*(finalX_3+radius-d2_3)/(s2-s1);

                float finalX_4 = firstCenterX+d*s3;
                float kMiddle_4 = (finalX_4-kMiddle_3*s2-bMiddle_3)/(s3-s2);
                float bMiddle_4 = kMiddle_3*s2+bMiddle_3-(finalX_4-kMiddle_3*s2-bMiddle_3)*s2/(s3-s2);
                float k0_4 = (finalX_4-radius-k0_3*s2-b0_3)/(s3-s2);
                float b0_4 = k0_3*s2+b0_3-(finalX_4-radius-k0_3*s2-b0_3)*s2/(s3-s2);
                float k2_4 = (finalX_4+radius- k2_3*s2-b2_3)/(s3-s2);
                float b2_4 = k2_3*s2+b2_3 - s2*(finalX_4+radius- k2_3*s2-b2_3)/(s3-s2);

                dataPoint[0].x = k0_4 * mInterpolatedTime + b0_4;
                dataPoint[0].y = centerY;
                dataPoint[1].x = kMiddle_4 * mInterpolatedTime + bMiddle_4;
                dataPoint[1].y = centerY - radius;
                dataPoint[2].x = k2_4 * mInterpolatedTime + b2_4;
                dataPoint[2].y = centerY;
                dataPoint[3].x = kMiddle_4 * mInterpolatedTime + bMiddle_4;
                dataPoint[3].y = centerY + radius;
            }
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"4-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
            Log.i(TAG,"4-dataPoint "+ds.toString()  );
        }else{
            dataPoint[0].x=centerX-radius;
            dataPoint[0].y=centerY;

            dataPoint[1].x=centerX;
            dataPoint[1].y=centerY-radius;
            dataPoint[2].x=centerX+radius;
            dataPoint[2].y=centerY;

            dataPoint[3].x=centerX;
            dataPoint[3].y=centerY+radius;
            List<PointF> ps = Arrays.asList(firstDataPoint);
//            Log.i(TAG,"5-firstDataPoint "+ps.toString()  );
            List<PointF> ds = Arrays.asList(dataPoint);
            Log.i(TAG,"5-dataPoint "+ds.toString()  );
        }
        return dataPoint;
    }

    private PointF[] initCtrlPoint(PointF[] dataPoint){
        for(int i=0;i<ctrlPoint.length;i++){
            ctrlPoint[i] = new PointF(0,0);
        }
        ctrlPoint[0].x=dataPoint[0].x;
        ctrlPoint[0].y=dataPoint[0].y-diff;

        ctrlPoint[1].x=dataPoint[1].x-diff;
        ctrlPoint[1].y=dataPoint[1].y;
        ctrlPoint[2].x=dataPoint[1].x+diff;
        ctrlPoint[2].y=dataPoint[1].y;

        ctrlPoint[3].x=dataPoint[2].x;
        ctrlPoint[3].y=dataPoint[2].y-diff;
        ctrlPoint[4].y=dataPoint[2].y+diff;
        ctrlPoint[4].x=dataPoint[2].x;

        ctrlPoint[5].y=dataPoint[3].y;
        ctrlPoint[5].x=dataPoint[3].x+diff;
        ctrlPoint[6].y=dataPoint[3].y;
        ctrlPoint[6].x=dataPoint[3].x-diff;

        ctrlPoint[7].x=dataPoint[0].x;
        ctrlPoint[7].y=dataPoint[0].y+diff;
        return ctrlPoint;

    }


    private void drawCubicBezier(Canvas canvas){
        /** 清除Path中的内容
         reset不保留内部数据结构，但会保留FillType.
         rewind会保留内部的数据结构，但不保留FillType */
        path.reset();
        path.moveTo(dataPoint[0].x,dataPoint[0].y);

        path.cubicTo(ctrlPoint[0].x,ctrlPoint[0].y,ctrlPoint[1].x,ctrlPoint[1].y,dataPoint[1].x,dataPoint[1].y);
        path.cubicTo(ctrlPoint[2].x,ctrlPoint[2].y,ctrlPoint[3].x,ctrlPoint[3].y,dataPoint[2].x,dataPoint[2].y);
        path.cubicTo(ctrlPoint[4].x,ctrlPoint[4].y,ctrlPoint[5].x,ctrlPoint[5].y,dataPoint[3].x,dataPoint[3].y);
        path.cubicTo(ctrlPoint[6].x,ctrlPoint[6].y,ctrlPoint[7].x,ctrlPoint[7].y,dataPoint[0].x,dataPoint[0].y);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path,paint);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
//        Log.i(TAG,"dispatchDraw");
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        if(pointLimitQueue.size()==0){
            Log.i(TAG,"dispatchDraw 0");
            return;
        }else if(pointLimitQueue.size()==1){
            Log.i(TAG,"dispatchDraw 1");
            PointF p =pointLimitQueue.getFirst();
            initCenterPoint(p.x,p.y);
            dataPoint =  initDataPoint(0,p.x,p.y,0);
            initCtrlPoint(dataPoint);
            drawCubicBezier(canvas);
//            canvas.drawCircle(p.x,p.y,10,paint);
        }else if(pointLimitQueue.size()==2){
            Log.i(TAG," pointLimitQueue " +pointLimitQueue.queue.toString());
            /**麻烦事情 开始吧*/
            PointF pFirst = pointLimitQueue.get(0);
            PointF pLast = pointLimitQueue.get(1);
            float xFirst = pFirst.x;
            float yFirst = pFirst.y;
            float xLast = pLast.x;
            float yLast = pLast.y;
            float xWidth = xLast-xFirst;//求距离，使用绝对值
            float yWidth = yLast-yFirst;
            float bendingDistance = xWidth/4f;
            kx = xWidth;
            float d = kx;
            ky = yWidth;
            float resultX = firstCenterX+kx*mInterpolatedTime;
            float resultY = firstCenterX+kx*mInterpolatedTime;
            dataPoint = initDataPoint(d,resultX,firstCenterY,bendingDistance);
            initCtrlPoint(dataPoint);
            List<PointF> ps = Arrays.asList(dataPoint);
            List<PointF> cs = Arrays.asList(ctrlPoint);
//        drawPoint(canvas);
            drawCubicBezier(canvas);

        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        handler.sendEmptyMessage(1);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int a = ev.getAction();
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

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int a = event.getAction();
        switch (a){
            case MotionEvent.ACTION_DOWN:
//                Log.e(TAG,"onTouchEvent ACTION_DOWN");
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
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int a = ev.getAction();
        switch (a){
            case MotionEvent.ACTION_DOWN:
                 ex = ev.getX();
                 ey = ev.getY();
                PointF p = new PointF();
                p.x=ex;p.y=ey;
                Log.i(TAG,"p-"+ex+","+ey);
                if(isInChildView(ex,ey)){
                    pointLimitQueue.offer(p);
                    Log.i(TAG,"ps-"+pointLimitQueue.queue.toString());
                }
                if(pointLimitQueue.size()==1){
                    invalidate();
                }else
                handler.sendEmptyMessage(0);
//                Log.e(TAG,"onInterceptTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG,"onInterceptTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
//                Log.e(TAG,"onInterceptTouchEvent ACTION_UP");
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 点击位置是否在某个子View内
     * @param x
     * @param y
     * @return
     */
    private boolean isInChildView(float x,float y){
        boolean isInChildView = false;
            for(int i =0;i<getChildCount();i++){
            View v  =getChildAt(i);
            int top = v.getTop();
            int bottom = v.getBottom();
            int left = v.getLeft();
            int right = v.getRight();
            boolean inWidth = x>=left&&x<=right;
            boolean inHeight = y>=top&&y<=bottom;
            isInChildView = inWidth&&inHeight;
                if(isInChildView) break;
        }
        return isInChildView;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
    }


    /**
     * 这里使用LinkList(实现了Queue接口，有队列的性质)，
     * 这是一个可指定长度的Queue(LinkedList)
     * @param <E>
     */
    class LimitQueue<E>{

        private int limit; // 队列长度

        public LinkedList<E> queue = new LinkedList<E>();

        public LimitQueue(int limit){
            this.limit = limit;
        }

        /**
         * 入列：当队列大小已满时，把队头的元素poll掉
         */
        public void offer(E e){
            if(queue.size() >= limit){
                queue.poll();
            }
            queue.offer(e);
        }

        public void poll(){
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

    private class TranslteAnimaton extends Animation {
        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mInterpolatedTime=interpolatedTime;
            if(mInterpolatedTime!=1)invalidate();
            if(mInterpolatedTime==1&&pointLimitQueue.size()>=2){
                pointLimitQueue.poll();
            }
        }

    }

    public void  startTranslteAnimaton(){
        mInterpolatedTime = 0;
        move.setDuration(1000);
        move.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.e(TAG,"onAnimationStart");
                state = STATE_START;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.e(TAG,"onAnimationEnd");
                state = STATE_STOP;
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                Log.e(TAG,"onAnimationRepeat");
            }
        });
        move.setInterpolator(new AccelerateDecelerateInterpolator());
        startAnimation(move);
    }
}
