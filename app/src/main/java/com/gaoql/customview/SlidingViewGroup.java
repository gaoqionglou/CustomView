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
            Log.e(TAG,"CHILD onLayout: "+(n+1)+",l-"+(left-w)+",t-"+t+",r-"+left+",b-"+h);

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
        switch (action){
            case MotionEvent.ACTION_DOWN:
                offsetX=getScrollX();
                lastX = x;
                break;
            case MotionEvent.ACTION_UP:
                //拿x的速度
                velocityTracker.computeCurrentVelocity(1000);//计算1000ms运动了多少个像素 TODO:这个时间怎么给怎么传？
                float velocityX = velocityTracker.getXVelocity();
                //TODO:要做手势滑动的距离和这个速率的相关处理。
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = x-lastX;
                lastX = x;
                scrollBy((int)-dx,0);/** */
                break;
            case MotionEvent.ACTION_CANCEL://TODO:事件被上层拦截时触发。那么用来注销VelocityTracker？
//                scrollTo(getScrollX(),0);
                break;
            default:
                break;
        }
        realseVelocityTracker();
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
        Log.e(TAG,"e1-"+e1.getX()+","+e1.getY());
        Log.e(TAG,"e2-"+e2.getX()+","+e2.getY());
        Log.e(TAG,"distanceX-"+distanceX+",distanceY"+distanceY);
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

    //调用此方法滚动到目标位置
    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    //调用此方法设置滚动的相对偏移
    public void smoothScrollBy(int dx, int dy) {

        //设置mScroller的滚动偏移量
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        Log.e(TAG,"startScroll "+mScroller.getFinalX()+","+mScroller.getFinalY()+","+dx+","+dy);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }
}
