package com.gaoql.customview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;


public class SlidingViewPager extends LinearLayout implements GestureDetector.OnGestureListener{
    public static final String TAG="BuffSlidingViewPager";
    private GestureDetector mGestureDetector ;
    private Scroller mScroller;
    private Matrix mMatrix;
    private Camera mCamera;
    private Context mContext;
    private VelocityTracker velocityTracker;//速率跟踪器
    private ViewConfiguration configuration;//获取系统配置的最小滑动距离和速率
    private float mDownX,mDownY;
    private int offsetX,offsetY;
    private int currentPageIndex=0;
    private int minScrollDistance = 0;
    private int minFlingVelocity = 0;
    private float dx,dy;//x方向和y方向的移动的距离，带正负号
    private CustomPagerIndicator indicator;
    private int mWidth,mHeight;
    private boolean isCanSliding = false;//是否需要滑动
    private boolean isDraging = false;
    private int childViewWidth;
    private State state =State.None;
    public SlidingViewPager(Context context) {
        this(context,null);
    }

    public SlidingViewPager(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public SlidingViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context,attrs,defStyleAttr,0);
    }

    public SlidingViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setClickable(true);
        init(context);
    }
    /** 初始化手势监听器等等*/
    private void init(Context context){
        this.mContext = context;
        mCamera = new Camera();
        mMatrix = new Matrix();
        mGestureDetector = new GestureDetector(context,this);
        mGestureDetector.setIsLongpressEnabled(false);
        mScroller = new Scroller(context);
        configuration = ViewConfiguration.get(context);
        minScrollDistance =  configuration.getScaledTouchSlop();
        minFlingVelocity = configuration.getScaledMinimumFlingVelocity();

    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        this.currentPageIndex = currentPageIndex;
    }

    /**
     * 获取指示器
     * @return
     */
    public CustomPagerIndicator getIndicator() {
        return indicator;
    }

    /**
     * 为ViewPager指定一个指示器
     * @param indicator
     */
    public void setIndicator(CustomPagerIndicator indicator) {
        this.indicator = indicator;
        for(int i=0;i<indicator.getChildCount();i++){
            CircleButton childView = (CircleButton)indicator.getChildAt(i);
            childView.setAttachView(this);
        }
    }

    /** 1 测试和布局*/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        scrollTo(currentPageIndex*mWidth, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout( changed, l,  t, r,  b);
    }

    /** 2 绘制*/
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
//        super.dispatchDraw(canvas);
        for (int i = 0; i < getChildCount(); i++) {
            drawChildWith3D(canvas, i, getDrawingTime());
        }
    }
    private void drawChildWith3D(Canvas canvas, int i,long drawingTime){
        int curScreenX = mWidth * i;
        //屏幕中不显示的部分不进行绘制
        if (getScrollX() + mWidth < curScreenX) {
            return;
        }
        if (curScreenX < getScrollX() - mWidth) {
            return;
        }
        float centerX = (getScrollX() > curScreenX) ? curScreenX + mHeight : curScreenX;
        float centerY = mHeight / 2;
        float degree = -90f * (getScrollX() - curScreenX) / mWidth;
        if (degree > 90 || degree < -90) {
            return;
        }
        canvas.save();

        mCamera.save();
        mCamera.rotateY(degree);
        mCamera.getMatrix(mMatrix);
        float scale = mContext.getResources().getDisplayMetrics().density;
        float[] mValues = new float[9];
        mMatrix.getValues(mValues);			    //获取数值
        mValues[6] = mValues[6]/scale;			//数值修正
        mValues[7] = mValues[7]/scale;			//数值修正
        mMatrix.setValues(mValues);
        mCamera.restore();

        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);
        canvas.concat(mMatrix);
        drawChild(canvas, getChildAt(i), drawingTime);
        canvas.restore();
    }
    /** 3 事件分发*/
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        View childView = getChildAt(0);
        childViewWidth = childView.getRight()-childView.getLeft();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isCanSliding = true;
                offsetX=getScrollX();
                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                if (!isCanSliding) {
                    isCanSliding = isCanSliding(ev);
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isCanSliding(MotionEvent ev){
        float currentX = ev.getX();
        float currentY = ev.getY();
        if(Math.abs(currentX-mDownX) > Math.abs(currentY-mDownY) && Math.abs(currentX-mDownX)>minScrollDistance){
            //X方向的距离大于y的滑动距离 && x方向的滑动距离大于系统最短滑动距离，认为是水平滑动
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(getIndicator()!=null&&getIndicator().getTranslateState()==CustomPagerIndicator.STATE_MOVING){
            //发现正在滑动的,调用父类的方法分发掉该事件
            return true;
        }
        return  isCanSliding;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker(event);
        float x = event.getX();
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                dx = x - mDownX;
                mDownX = x;
                if (currentPageIndex == 0 && dx > 0 || currentPageIndex == getChildCount() - 1 && dx < 0) {
                    break;
                }
                scrollBy((int) -dx, 0);
                break;
            case MotionEvent.ACTION_UP:
                if(isCanSliding) {
                    int scrollX = getScrollX();
                    int delta = scrollX - offsetX;
                    //计算出一秒移动1000像素的速率 1000 表示每秒多少像素（pix/second),1代表每微秒多少像素（pix/millisecond)
                    velocityTracker.computeCurrentVelocity(1000, configuration.getScaledMaximumFlingVelocity());
                    float velocityX = velocityTracker.getXVelocity();
                    float velocityY = velocityTracker.getYVelocity();
                    if (Math.abs(delta) < childViewWidth / 3) {
                        // 小于三分之一，弹回去
                        Log.e(TAG, "onTouchEvent ACTION_UP back 1  ");
                        state = State.None;
                        requestUpdateState(state,delta);
                    } else if (Math.abs(velocityX) <= configuration.getScaledMinimumFlingVelocity() && Math.abs(velocityY) <= configuration.getScaledMinimumFlingVelocity()) {
                        //当速度小于系统速度，但过了三分一的距离，此时应该滑动一页
                        Log.e(TAG, "onTouchEvent ACTION_UP back 2  ");
                        if (delta > 0) { //左滑趋势
                            Log.e(TAG,"onTouchEvent page index 2-1 -- "+currentPageIndex);
                            if (currentPageIndex >=0 ) {
                                Log.e(TAG, "onTouchEvent ACTION_UP back 2-1  ");
                                state = State.ToNext;
                            }
                        } else {//右滑趋势
                            Log.e(TAG,"onTouchEvent page index 2-2 -- "+currentPageIndex);
                            if (currentPageIndex < getChildCount()) {
                                Log.e(TAG, "onTouchEvent ACTION_UP back 2-2  ");
                                state = State.ToPre;
                            }
                        }
                        Log.e(TAG,"requestUpdateState 1 "+state);
                        requestUpdateState(state,delta);
                        Log.e(TAG,"requestUpdateState 1 addChildViewCenterPointToIndicator "+currentPageIndex);
                        addChildViewCenterPointToIndicator(currentPageIndex);
                        Log.i(TAG,"startIndicatorCircleMoving 1");
                        startIndicatorCircleMoving();
                    }

                }
                realseVelocityTracker();
                break;
            default:
                break;

        }
        return mGestureDetector.onTouchEvent(event) ;
    }

    /**
     * 获取速度跟踪器
     */
    private void obtainVelocityTracker(MotionEvent event){
        if(velocityTracker==null){
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    /**
     * 释放速度跟踪器
     */
    private void realseVelocityTracker(){
        if(velocityTracker!=null){
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
//        Log.e(TAG,"onSingleTapUp");
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.e(TAG,"onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.e(TAG,"onFling velocityX-"+velocityX+",velocityY-"+velocityY);
        View childView = getChildAt(0);
        int width = childView.getRight()-childView.getLeft();
        float distanceX = e2.getX()-e1.getX();
        int delta = getScrollX() - offsetX;
        if(Math.abs(delta)<width/3){
            //弹回
            Log.e(TAG,"onFling donothing");
            state = State.None;
            Log.e(TAG,"requestUpdateState 2 "+state);
            requestUpdateState(state,delta);
            return true;
        }
        if(Math.abs(distanceX)>=minScrollDistance) {
            if(distanceX == 0){
                Log.e(TAG,"onFling donothing distanceX =0");
                return true;
            }
            if (distanceX < 0) {
                if (currentPageIndex < getChildCount() - 1) {
                    state = State.ToNext;
                    Log.e(TAG,"requestUpdateState 3 "+state);
                    requestUpdateState(state,delta);
                }
            }
            if (distanceX > 0) {
                if (currentPageIndex > 0) {
                    state = State.ToPre;
                    Log.e(TAG,"requestUpdateState 4 "+state);
                    requestUpdateState(state,delta);
                }
            }
            Log.e(TAG,"onFling addChildViewCenterPointToIndicator "+currentPageIndex);
            addChildViewCenterPointToIndicator(currentPageIndex);
            Log.i(TAG,"startIndicatorCircleMoving 2");
            startIndicatorCircleMoving();
        }

        return true;
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    /**
     * 缓慢滑动
     * @param dx
     * @param dy
     */
    public void smoothScrollBy(int dx, int dy) {
        int scrollX = getScrollX();
        int deltaX = dx - scrollX;
        //设置mScroller的滚动偏移量,从当前的偏移位置移动到deltaX的位置
        mScroller.startScroll(scrollX, 0, deltaX, 0,1000);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    /**
     * 以手指滑动速度去滑动,
     * @param dx
     * @param dy
     */
    public void scrollByVelocity(int dx, int dy, float velocityX, float velocityY) {
        int scrollX = getScrollX();
        int deltaX = dx - scrollX;
        float time = Math.abs(deltaX)*1000f/Math.abs(velocityX);
        //设置mScroller的滚动偏移量,从当前的偏移位置移动到deltaX的位置
        mScroller.startScroll(scrollX, 0, deltaX, 0,(int)time);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }


    private void requestUpdateState(State state,int delta){
        Log.e(TAG,"page index -"+currentPageIndex);
        switch (state){
            case None:
                stayInCurrentPage();
                Log.e(TAG,"stayInCurrentPage - "+currentPageIndex);
                break;
            case ToNext:
                moveToNextPage(delta);
                Log.e(TAG,"moveToNextPage - "+currentPageIndex);
                break;
            case ToPre:
                moveToPrePage(delta);
                Log.e(TAG,"moveToPrePage - "+currentPageIndex);
                break;
            default:
                break;
        }
        invalidate();
    }



    /**
     * 下一页，向左
     */
    private void moveToNextPage(int delta){
        int dx = childViewWidth - delta;
        int scrollX =  getScrollX();
        currentPageIndex++;
        mScroller.startScroll(scrollX, 0, dx, 0, 1000);
    }

    /**
     * 上一页，向右
     */
    private void moveToPrePage(int delta){
        int dx = childViewWidth+delta;
        int scrollX =  getScrollX();
        currentPageIndex--;
        mScroller.startScroll(scrollX, 0, -dx, 0, 1000);
    }

    private void stayInCurrentPage(){
        int scrollX = getScrollX();
        int detla = currentPageIndex*childViewWidth - scrollX ;
        mScroller.startScroll(scrollX,0,detla,0);
    }

    private void addChildViewCenterPointToIndicator(int currentPageIndex){
        if(getIndicator()==null){
            return;
        }
        indicator.addChildViewCenterPointToQueue(currentPageIndex);
    }
    private void startIndicatorCircleMoving(){
        if(getIndicator()==null){
            return;
        }
        indicator.startCircleMoving();
    }

    /**
     * 从当前页面移动到指定的页面
     * @param currentPageIndex
     * @param targetPageIndex
     */
    public void moveTo(int currentPageIndex,int targetPageIndex){
        View childView = getChildAt(0);
        int width = childView.getRight()-childView.getLeft();
        int scrollX =  getScrollX();
        int diff = targetPageIndex - currentPageIndex;
        this.currentPageIndex = targetPageIndex;
        mScroller.startScroll(scrollX, 0,diff *width, 0, 1000);
        invalidate();
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    private int getChildViewIndex(float x,float y){
        int index = -1;
        for(int i =0;i<getChildCount();i++){
            View childView  =getChildAt(i);
            int top = childView.getTop();
            int bottom = childView.getBottom();
            int left = childView.getLeft();
            int right = childView.getRight();
            boolean inWidth = x>=left&&x<=right;
            boolean inHeight = y>=top&&y<=bottom;
            boolean isInChildView = inWidth&&inHeight;
            if(isInChildView) {
                index = i;
                break;
            }
        }
        return index;
    }
    public  enum State {
        ToPre,ToNext,None;
    }
}
