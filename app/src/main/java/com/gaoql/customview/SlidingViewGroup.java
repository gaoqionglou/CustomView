package com.gaoql.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;



public class SlidingViewGroup extends ViewGroup {
    public SlidingViewGroup(Context context) {
        super(context);
    }

    public SlidingViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidingViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SlidingViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
        int width =0;
        int height = 0;
        if(getChildCount()==0){
            setMeasuredDimension(width,height);
        }
        if(widthMode==MeasureSpec.EXACTLY&&heightMode==MeasureSpec.EXACTLY){
            setMeasuredDimension(widthSize,heightSize);
        }else if(widthMode==MeasureSpec.AT_MOST){
            for(int i=0;i<getChildCount();i++){
                View v = getChildAt(i);
                int w = v.getMeasuredWidth();
                MarginLayoutParams mlp =(MarginLayoutParams) getLayoutParams();
                width = w+mlp.rightMargin+mlp.leftMargin;
            }
            height = heightSize;
        }else if(heightMode==MeasureSpec.AT_MOST){
            for(int j=0;j<getChildCount();j++){
                View v = getChildAt(j);
                int h = v.getMeasuredHeight();
                MarginLayoutParams mlp =(MarginLayoutParams) getLayoutParams();
                height = Math.max(height,h+mlp.topMargin+mlp.bottomMargin);
            }
            width=widthSize;
        }else {
            for(int m=0;m<getChildCount();m++){
                View v = getChildAt(m);
                int w = v.getMeasuredWidth();
                int h = v.getMeasuredHeight();
                MarginLayoutParams mlp =(MarginLayoutParams) getLayoutParams();
                width = w+mlp.rightMargin+mlp.leftMargin;
                height = Math.max(height,h+mlp.topMargin+mlp.bottomMargin);
            }
        }
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
            v.layout(left-w,t,w,h);
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
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
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
}
