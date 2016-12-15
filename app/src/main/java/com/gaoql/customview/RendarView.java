package com.gaoql.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2016/10/21.
 */

public class RendarView extends View {
    public static final String TAG="GAO";
    private float ANGLE_60= (float) Math.PI/6;
    private float r;//六边形的半径
    private float n=5;//一条半径划分成几段，用n表示
    private float mWidth;
    private float mHeight;
    private Paint linePaint = new Paint();//线条画笔
    private Paint txtPaint = new Paint();//文本画笔
    private Paint contentPaint = new Paint();//雷达区内容画笔
    private Map<Integer,ArrayList<Point>> listMap = new HashMap<Integer, ArrayList<Point>>();
    private String[] txt = new String []{"a","b","c","d","e","f"};
    public RendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RendarView(Context context) {
        this(context,null);
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        r = (float)(Math.min(mWidth,mHeight)/2*0.9);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //移动至中心
        canvas.translate(mWidth/2,mHeight/2);
        drawLine(canvas);
        drawContent(canvas);
    }
    //绘制边界线
    private void drawLine(Canvas canvas){
        initPoint(r);
        linePaint.setAlpha(255);
        Path path = new Path();
        for (int i=0;i<6;i++){
            ArrayList<Point> ps =listMap.get(i);
            path.moveTo(ps.get(0).getX(),ps.get(0).getY());
            for(int j=1;j<ps.size();j++) {
                Point p = ps.get(j);
                path.lineTo(p.getX(),p.getY());
            }
            path.close();//形成闭环
        }
        //描半径
        ArrayList<Point> ps1 =listMap.get(0);
        path.moveTo(0,0);
        for (int i=0;i<6;i++){
            path.lineTo(ps1.get(i).getX(),ps1.get(i).getY());
            txtPaint.setTextSize(50);
            canvas.drawText(txt[i],ps1.get(i).getX(),ps1.get(i).getY(),txtPaint);
            path.moveTo(0,0);
        }
        linePaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path,linePaint);

    }
    private void drawContent(Canvas canvas){
        //依次确定每个点
        contentPaint.setColor(Color.BLUE);
        contentPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        Path contentPath = new Path();
        int a= (int)(Math.random()*6);
        contentPath.moveTo(listMap.get(a).get(0).getX(),listMap.get(a).get(0).getY());
        for (int i =1;i<6;i++){
            int r= (int)(Math.random()*5);
            Point p = listMap.get(r).get(i);
            canvas.drawCircle(p.getX(),p.getY(),10,contentPaint);
            contentPath.lineTo(p.getX(),p.getY());
        }
        contentPath.close();
        contentPaint.setAlpha(127);
        //绘制填充区域
        canvas.drawPath(contentPath,contentPaint);

    }
    //初始化描点
    private void initPoint(float r){
        float s = r/n;
        for(int j=0;j<6;j++) {
            float dr = r-s*j;
            ArrayList<Point> ps = new ArrayList<Point>();
            Point p1 = new Point(-dr, 0);
            Point p2 = new Point(-dr/2,(float)(dr*Math.sin(ANGLE_60)));
            Point p3 = new Point(dr/2,(float)(dr*Math.sin(ANGLE_60)));
            Point p4 = new Point(dr,0);
            Point p5 = new Point(dr/2,(float)(-dr*Math.sin(ANGLE_60)));
            Point p6 = new Point(-dr/2,(float)(-dr*Math.sin(ANGLE_60)));
            ps.add(p1);
            ps.add(p2);
            ps.add(p3);
            ps.add(p4);
            ps.add(p5);
            ps.add(p6);
            listMap.put(j,ps);
        }
        Log.i(TAG,listMap.toString());
    }


    //点对象，存储x,y坐标。用于绘图
    class Point {
        private float x;
        private float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
