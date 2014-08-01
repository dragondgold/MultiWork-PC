package com.andres.multiwork.pc.highstocks;

public class AnnotationReference {

    private final double x1, width, y, height;
    private final String text;

    public AnnotationReference(double x1, double width, double y, double height, String text) {
        this.x1 = x1;
        this.width = width;
        this.y = y;
        this.height = height;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public double getX1() {
        return x1;
    }

    public double getX2(){
        return x1+width;
    }

    public double getWidth() {
        return width;
    }

    public double getY() {
        return y;
    }

    public double getHeight() {
        return height;
    }
}
