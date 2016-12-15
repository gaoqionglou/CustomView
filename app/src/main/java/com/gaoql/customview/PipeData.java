package com.gaoql.customview;

/**
 * Created by admin on 2016/10/20.
 */

public class PipeData {
    //用户关心的数据
    private String name; //名字
    private float value; //数值
    private float percentage;//百分比
    //非用户关系的数据
    private int color ; //颜色
    private float angle;//扇形的角度

    public PipeData(String name, float value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        return "PipeData{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", percentage=" + percentage +
                ", color=" + color +
                ", angle=" + angle +
                '}';
    }
}
