package com.gaoql.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;

import com.gaoql.R;


/**
 * Created by Administrator on 2016/10/26.
 */

public class MartixViewTest extends View {
    private float mWidth;
    private float mHeight;
    Matrix mPolyMatrix;
    Bitmap mBitmap;
    public MartixViewTest(Context context){
        super(context);
        initBitmapAndMatrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth=w;
        mHeight=h;
    }
    private void initBitmapAndMatrix() {
        mBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.image2);

        mPolyMatrix = new Matrix();


        float[] src = {0, 0,                                    // 左上
                mBitmap.getWidth(), 0,                          // 右上
                mBitmap.getWidth(), mBitmap.getHeight(),        // 右下
                0, mBitmap.getHeight()};                        // 左下

        float[] dst = {0, 0,                                    // 左上
                mBitmap.getWidth(), 400,                        // 右上
                mBitmap.getWidth(), mBitmap.getHeight() - 200,  // 右下
                0, mBitmap.getHeight()};                        // 左下

        // 核心要点
//        mPolyMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1); // src.length >> 1 为位移运算 相当于处以2
        mPolyMatrix.postScale(0.26f, 0.26f);
        mPolyMatrix.postTranslate(0,200);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap,mPolyMatrix,null);
    }

}
