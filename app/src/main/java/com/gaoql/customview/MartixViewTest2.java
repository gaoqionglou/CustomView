package com.gaoql.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.gaoql.R;

/**
 * Created by Administrator on 2016/10/26.
 */

public class MartixViewTest2 extends View {
    private float mWidth;
    private float mHeight;
    public static final String TAG="REYOU";
    Matrix mMatrix;
    Bitmap mBitmap;
    public MartixViewTest2(Context context){
        this(context,null);
        initBitmapAndMatrix();
    }

    public MartixViewTest2(Context context, AttributeSet attrs) {
        super(context, attrs);
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
                R.drawable.image);

        mMatrix = new Matrix();
        dosomethig();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap,mMatrix,null);
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public void setMatrix(Matrix mMatrix) {
        this.mMatrix = mMatrix;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    private void dosomethig(){
        int h = mBitmap.getHeight();
        int w =mBitmap.getWidth();
        Log.i(TAG, "bitmap_H--"+h+",bitmap_W--"+w);
//        mMatrix.postTranslate(0,100);
//        float[] dst = new float[9];
//        mMatrix.getValues(dst);
//        for(int i = 0; i < 3; ++i)
//        {
//            String temp = new String();
//            for(int j = 0; j < 3; ++j)
//            {
//                temp += dst[3 * i + j ] + "\t";
//            }
//            Log.i(TAG, temp);
//        }

         // 2. 旋转(围绕图像的中心点)
//        mMatrix.setRotate(45f, w/2, h/2);

/*          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
        mMatrix.preTranslate(w * 0.5f, 0f);

          // 下面的代码是为了查看matrix中的元素
          float[] matrixValues = new float[9];
        mMatrix.getValues(matrixValues);
          for(int i = 0; i < 3; ++i)
          {
              String temp = new String();
              for(int j = 0; j < 3; ++j)
              {
                  temp += matrixValues[3 * i + j ] + "\t";
              }
              Log.i(TAG, temp);
          }*/


/*
         // 3. 旋转(围绕坐标原点) + 平移(效果同2)
        mMatrix.setRotate(45f);
        mMatrix.preTranslate(-1f * w / 2f, -1f * h / 2f);
        mMatrix.postTranslate((float)w / 2f, (float)h / 2f);

          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
        mMatrix.postTranslate((float)w * 1.5f, 0f);


          // 下面的代码是为了查看matrix中的元素
          float[] matrixValues = new float[9];
        mMatrix.getValues(matrixValues);
          for(int i = 0; i < 3; ++i)
          {
              String temp = new String();
              for(int j = 0; j < 3; ++j)
              {
                  temp += matrixValues[3 * i + j ] + "\t";
              }
              Log.i(TAG, temp);
          }
*/

//          // 4. 缩放
//          matrix.setScale(2f, 2f);
//          // 下面的代码是为了查看matrix中的元素
//          float[] matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }
//
//          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
//          matrix.postTranslate(view.getImageBitmap().getWidth(), view.getImageBitmap().getHeight());
//          view.setImageMatrix(matrix);
//
//          // 下面的代码是为了查看matrix中的元素
//          matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }


/*//          // 5. 错切 - 水平
          mMatrix.setSkew(0.5f, 0f);
          // 下面的代码是为了查看matrix中的元素
          float[] matrixValues = new float[9];
        mMatrix.getValues(matrixValues);
          for(int i = 0; i < 3; ++i)
          {
              String temp = new String();
              for(int j = 0; j < 3; ++j)
              {
                  temp += matrixValues[3 * i + j ] + "\t";
              }
              Log.i(TAG, temp);
          }*/

//          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
//          matrix.postTranslate(view.getImageBitmap().getWidth(), 0f);
//          view.setImageMatrix(matrix);
//
//          // 下面的代码是为了查看matrix中的元素
//          matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }

//          // 6. 错切 - 垂直
//          matrix.setSkew(0f, 0.5f);
//          // 下面的代码是为了查看matrix中的元素
//          float[] matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }
//
//          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
//          matrix.postTranslate(0f, view.getImageBitmap().getHeight());
//          view.setImageMatrix(matrix);
//
//          // 下面的代码是为了查看matrix中的元素
//          matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }

//          7. 错切 - 水平 + 垂直
//          matrix.setSkew(0.5f, 0.5f);
//          // 下面的代码是为了查看matrix中的元素
//          float[] matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }
//
//          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
//          matrix.postTranslate(0f, view.getImageBitmap().getHeight());
//          view.setImageMatrix(matrix);
//
//          // 下面的代码是为了查看matrix中的元素
//          matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }

          // 8. 对称 (水平对称)
          float matrix_values[] = {1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f};
        mMatrix.setValues(matrix_values);
          // 下面的代码是为了查看matrix中的元素
          float[] matrixValues = new float[9];
        mMatrix.getValues(matrixValues);
          for(int i = 0; i < 3; ++i)
          {
              String temp = new String();
              for(int j = 0; j < 3; ++j)
              {
                  temp += matrixValues[3 * i + j ] + "\t";
              }
              Log.i(TAG, temp);
          }

          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
        mMatrix.postTranslate(100f, h+100);
          // 下面的代码是为了查看matrix中的元素
          matrixValues = new float[9];
          mMatrix.getValues(matrixValues);
          for(int i = 0; i < 3; ++i)
          {
              String temp = new String();
              for(int j = 0; j < 3; ++j)
              {
                  temp += matrixValues[3 * i + j ] + "\t";
              }
              Log.i(TAG, temp);
          }

//          // 9. 对称 - 垂直
//          float matrix_values[] = {-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
//          matrix.setValues(matrix_values);
//          // 下面的代码是为了查看matrix中的元素
//          float[] matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }
//
//          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
//          matrix.postTranslate(view.getImageBitmap().getWidth() * 2f, 0f);
//          view.setImageMatrix(matrix);
//
//          // 下面的代码是为了查看matrix中的元素
//          matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }


//          // 10. 对称(对称轴为直线y = x)
//          float matrix_values[] = {0f, -1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f};
//          matrix.setValues(matrix_values);
//          // 下面的代码是为了查看matrix中的元素
//          float[] matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }
//
//          // 做下面的平移变换，纯粹是为了让变换后的图像和原图像不重叠
//          matrix.postTranslate(view.getImageBitmap().getHeight() + view.getImageBitmap().getWidth(),
//                  view.getImageBitmap().getHeight() + view.getImageBitmap().getWidth());
//          view.setImageMatrix(matrix);
//
//          // 下面的代码是为了查看matrix中的元素
//          matrixValues = new float[9];
//          matrix.getValues(matrixValues);
//          for(int i = 0; i < 3; ++i)
//          {
//              String temp = new String();
//              for(int j = 0; j < 3; ++j)
//              {
//                  temp += matrixValues[3 * i + j ] + "\t";
//              }
//              Log.e("TestTransformMatrixActivity", temp);
//          }
        print();
    }

    private void print(){
        int[] location = new int[2];
        this.getLocationInWindow(location);
        Log.i(TAG,location[0]+","+location[1]);

    }
}
