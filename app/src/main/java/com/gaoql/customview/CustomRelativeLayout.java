package com.gaoql.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by admin on 2016/12/24.
 */

public class CustomRelativeLayout extends RelativeLayout {
    public static final String TAG="CustomRelativeLayout";
    private CustomViewGroup customViewGroup;
    private Paint p;
    public CustomRelativeLayout(Context context) {
        super(context);
        init();
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(){
        p = new Paint();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawLayer(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int childCount = getChildCount();
        if(childCount>1||childCount<=0){
            Log.e(TAG,"Stub!");
            return;
        }
        customViewGroup = (CustomViewGroup) getChildAt(0);
        super.onLayout(changed, l, t, r, b);
    }

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

    private void drawLayer(Canvas canvas){
        customViewGroup = (CustomViewGroup) getChildAt(0);
        //用于绘制icon...
        for(int i=0;i<customViewGroup.getChildCount();i++){
            CircleButton circleButton = (CircleButton)customViewGroup.getChildAt(i);
            int top =  circleButton.getTop();
            int bottom = circleButton.getBottom();
            int left = circleButton.getLeft();
            int right = circleButton.getRight();
            Drawable drawable = circleButton.getDrawable();
            Bitmap bitmap = drawableToBitmap(drawable);
            canvas.drawBitmap(bitmap,left+circleButton.getRadius()-bitmap.getWidth()/2,top+circleButton.getRadius()-bitmap.getHeight()/2,p);
        }

    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;

    }
}
