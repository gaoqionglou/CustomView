package com.gaoql.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;


public class SlidingViewGroup extends ViewGroup implements GestureDetector.OnGestureListener{
    public static final String TAG="SlidingViewGroup";
    private GestureDetector gestureDetector ;
    private Scroller mScroller;
    private VelocityTracker velocityTracker;//速率跟踪器
    private ViewConfiguration configuration;
    private int i =1;
    private float lastX=0;
    private int offsetX,offsetY;
    private int currentPageIndex=0;
    private int minScrollDistance = 0;
    private int minFlingVelocity = 0;
    private int minScrollVer = 0;
    private float direction = 1f;
    private float dx;
    private boolean isOnFling = false;
    private boolean isFirst = true;
    public SlidingViewGroup(Context context) {
        this(context,null);
    }

    public SlidingViewGroup(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public SlidingViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context,attrs,defStyleAttr,0);
    }

    public SlidingViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setClickable(true);
        init(context);
    }
    /** 初始化手势监听器*/
    private void init(Context context){
        gestureDetector = new GestureDetector(context,this);
        gestureDetector.setIsLongpressEnabled(false);
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

    /** 1 测试和布局*/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        measureChildren(widthMeasureSpec,heightMeasureSpec);
        measure(widthSize,heightSize,widthMode,heightMode);

    }

    /**
     * 简单总结下，一般的写法
     * 如果w,h都是match_parent,那么直接取上层容器的推荐值
     * 如果w是match_parent,h是wrap_content,那么w取推荐值，h取一个最大值
     * 如果w是wrap-content,h是math_parent,那么h取推荐值，w取累加值
     * @param widthSize
     * @param heightSize
     * @param widthMode
     * @param heightMode
     */
    private void measure(int widthSize,int heightSize,int widthMode,int heightMode){
        /**
         * EXACTLY：表示设置了精确的值，一般当childView设置其宽、高为精确值、match_parent时，ViewGroup会将其设置为EXACTLY；
         AT_MOST：表示子布局被限制在一个最大值内，一般当childView设置其宽、高为wrap_content时，ViewGroup会将其设置为AT_MOST；
         UNSPECIFIED：表示子布局想要多大就多大，一般出现在AadapterView的item的heightMode中、ScrollView的childView的heightMode中；此种模式比较少见。
         */
        int width =0;
        int height = 0;
        if(getChildCount()==0){
            setMeasuredDimension(width,height);
        }
        int a = MeasureSpec.UNSPECIFIED;
        if(widthMode==MeasureSpec.EXACTLY&&heightMode==MeasureSpec.EXACTLY){
            //MATCH_PARENT 设置为上层容器推荐的长 宽
//           Log.e(TAG,"ViewGroup,w-"+widthSize+",h-"+heightSize);
            setMeasuredDimension(widthSize,heightSize);
            return;
        }
        if(widthMode==MeasureSpec.AT_MOST&&heightMode==MeasureSpec.AT_MOST){
            for(int i=0;i<getChildCount();i++){
                View childView = getChildAt(i);
                int childWith = childView.getMeasuredWidth();
                int childHeight = childView.getMeasuredHeight();
                MarginLayoutParams clp= (MarginLayoutParams)childView.getLayoutParams();
                width+=childWith+clp.leftMargin+clp.rightMargin;
                height=Math.max(height,childHeight+clp.topMargin+clp.bottomMargin);
            }
        }else if(widthMode==MeasureSpec.AT_MOST){
            for(int i=0;i<getChildCount();i++){
                View childView = getChildAt(i);
                int childWith = childView.getMeasuredWidth();
                MarginLayoutParams clp= (MarginLayoutParams)childView.getLayoutParams();
                width+=childWith+clp.leftMargin+clp.rightMargin;
            }
            height = heightSize;
        }else if(heightMode==MeasureSpec.AT_MOST){
            for(int i=0;i<getChildCount();i++){
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = l;
        for(int n=0;n<getChildCount();n++){
            View v = getChildAt(n);
            int w = v.getMeasuredWidth();
            int h = v.getMeasuredHeight();
            MarginLayoutParams mlp = (MarginLayoutParams)getLayoutParams();
            left+=w+mlp.rightMargin+mlp.leftMargin;
            v.layout(left-w,t,left,h);
//            Log.e(TAG,"CHILD onLayout: "+(n+1)+",l-"+(left-w)+",t-"+t+",r-"+left+",b-"+h);

        }
    }

    /** 2 绘制*/
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    /** 3 事件分发*/
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //暂时不分发事件，自己处理
        return !super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker(event);
        int action = event.getAction();
        float x = event.getRawX();
        float ex = event.getX();
        float ey = event.getY();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG,"onTouchEvent ACTION_DOWN");
                Log.e(TAG,"onTouchEvent ACTION_DOWN getScrollX - "+getScrollX());
                offsetX=getScrollX();
                lastX = x;
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG,"onTouchEvent ACTION_UP getScrollX - "+getScrollX());
                View childView = getChildAt(0);
                int width = childView.getRight()-childView.getLeft();
                int scrollX =  getScrollX();
                int delta = scrollX - offsetX;
                //计算出一秒移动1000像素的速率 1000 表示每秒多少像素（pix/second),1代表每微秒多少像素（pix/millisecond)
                velocityTracker.computeCurrentVelocity(1000, configuration.getScaledMaximumFlingVelocity());
                float velocityX = velocityTracker.getXVelocity();
                float velocityY = velocityTracker.getYVelocity();
                //TODO:暂时用子View的三分之一，后续改成屏幕的三分一
                if(Math.abs(delta)<width/3){
                    /** 小于三分之一，弹回去 */
                    Log.e(TAG,"onTouchEvent ACTION_UP back 1  ");
                    mScroller.startScroll(scrollX, 0, -delta, 0,1000);
                    invalidate();
                }else if(Math.abs(velocityX)<=configuration.getScaledMinimumFlingVelocity()&&Math.abs(velocityY)<=configuration.getScaledMinimumFlingVelocity()){
                    //当速度小于系统速度，但过了三分一的距离，此时应该滑动一页
                    Log.e(TAG,"onTouchEvent ACTION_UP back 2  ");
                    if(isFirst){
                        Log.e(TAG,"onTouchEvent ACTION_UP back 2-0  ");
                        isFirst=false;
                        currentPageIndex++;
                        int dx = width - delta;
                        mScroller.startScroll(scrollX, 0, dx, 0, 1000);
                        invalidate();
                        break;
                    }

                    if(Math.signum(delta)>0){ //左滑趋势
                        if(currentPageIndex >0) {
                            currentPageIndex++;
                            isFirst=false;
                            Log.e(TAG, "onTouchEvent ACTION_UP back 2-1  ");
                            int dx = width - delta;
                            mScroller.startScroll(scrollX, 0, dx, 0, 1000);
                            invalidate();
                        }
                    }else{//右滑趋势
                        if(currentPageIndex < getChildCount()) {
                            isFirst=false;
                            currentPageIndex--;
                            Log.e(TAG, "onTouchEvent ACTION_UP back 2-2  ");
                            int dx = width + delta;
                            mScroller.startScroll(scrollX, 0, -dx, 0, 1000);
                            invalidate();
                        }
                    }
//                    mScroller.startScroll(scrollX, 0, -delta, 0,1000);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                dx = x-lastX;
                Log.e(TAG,"onTouchEvent ACTION_MOVE dx - "+dx+",x - "+x+",lastX - "+lastX+",currentPageIndex--"+currentPageIndex);
                lastX = x;

                if(currentPageIndex==0&&dx>0 || currentPageIndex==getChildCount()-1&&dx<0){
                    break;
                }
                scrollBy((int)-dx,0);/** */
                break;
            case MotionEvent.ACTION_CANCEL://TODO:事件被上层拦截时触发。那么用来注销VelocityTracker？
                realseVelocityTracker();
//                scrollTo(getScrollX(),0);
                break;
            default:
                break;
        }
        return gestureDetector.onTouchEvent(event);
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

    private void realseVelocityTracker(){
        if(velocityTracker!=null){
            velocityTracker.recycle();
        }
    }
    /** 4 布局  */
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
     * 具体地说，典型的触屏事件及其listener执行的流程见下：

     1). 单击事件的执行流程：
     有两种情况，一种是时间很短，一种时间稍长。
     时间很短：onDown ----> onSingleTapUp ----> onSingleTapConfirmed
     时间稍长：onDown ----> onShowPress   ----> onSingleTapUp ----> onSingleTapConfirmed

     2). 长按事件
     onDown ----> onShowPress ----> onLongPress
     3.抛(fling)：手指触动屏幕后，稍微滑动后立即松开:
     onDown ----> onScroll ----> onScroll ----> onScroll ----> ………  ----> onFling
     4.拖动(drag)
     onDown ----> onScroll ----> onScroll ----> onFiling
     TODO:--注意：有的时候会触发onFiling，但是有的时候不会触发，这是因为人的动作不标准所致。
     GestureDetector的源码里面，当if ((Math.abs(velocityY) > mMinimumFlingVelocity)
     || (Math.abs(velocityX) > mMinimumFlingVelocity)){
     handled = mListener.onFling(mCurrentDownEvent, ev, velocityX, velocityY);
     }
     速度太慢导致无法触发onFling
     */

    @Override
    public boolean onDown(MotionEvent e) {
        Log.e(TAG,"onDown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.e(TAG,"onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.e(TAG,"onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//       Log.e(TAG,"e1-"+e1.getX()+","+e1.getY());
//        Log.e(TAG,"e2-"+e2.getX()+","+e2.getY());
//        Log.e(TAG,"distanceX-"+distanceX+",distanceY"+distanceY);
/*        int a =  getChildAt(0).getWidth();
        if(distanceX>0) {
            smoothScrollBy(200, 0);
        }else if(distanceX<0){
            smoothScrollBy(-200, 0);
        }*/
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
            Log.e(TAG,"onFling donothing");
            return true;
        }
        if(Math.abs(distanceX)>=minScrollDistance) {
            Log.e(TAG,"before--"+currentPageIndex);
            if (distanceX < 0) {
                if (currentPageIndex < getChildCount() - 1) {
                    isFirst=false;
//                    smoothScrollBy((currentPageIndex + 1) * width, 0);
                    scrollByVelocity((currentPageIndex + 1) * width, 0,velocityX,velocityY);
                    currentPageIndex++;
                }
            }
            if (distanceX > 0) {
                if (currentPageIndex > 0) {
                    isFirst=false;
//                    smoothScrollBy((currentPageIndex - 1) * width, 0);
                    scrollByVelocity((currentPageIndex - 1) * width, 0,velocityX,velocityY);
                    currentPageIndex--;
                }
            }
            Log.e(TAG,"after--"+currentPageIndex);
        }

        return true;
    }

    @Override
    public void computeScroll() {
//        super.computeScroll();
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

    /**
     * 下一页，向左
     */
    private void moveToNextPage(){
        View childView = getChildAt(0);
        int width = childView.getRight()-childView.getLeft();
        int scrollX =  getScrollX();
        currentPageIndex++;
        mScroller.startScroll(scrollX, 0, width, 0, 1000);
        invalidate();
    }

    /**
     * 上一页，向右
     */
    private void moveToPrePage(){
        View childView = getChildAt(0);
        int width = childView.getRight()-childView.getLeft();
        int scrollX =  getScrollX();
        currentPageIndex--;
        mScroller.startScroll(scrollX, 0, -width, 0, 1000);
        invalidate();
    }

    public void moveTo(int currentPageIndex,int targetPageIndex){
        View childView = getChildAt(0);
        int width = childView.getRight()-childView.getLeft();
        int scrollX =  getScrollX();
        int diff = targetPageIndex - currentPageIndex;
        this.currentPageIndex = targetPageIndex;
         direction =  Math.signum(diff);
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
}
