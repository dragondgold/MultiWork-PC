package com.andres.multiwork.pc.charts;

import com.andres.multiwork.pc.utils.Utils;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TextBox {

    private Rectangle box = new Rectangle();
    private Text text = new Text();
    private double xPadding = 5, yPadding = 0;

    private double x = 0, y = 0, width = 100, height = 50;

    public TextBox(Pane pane) {
        this("", pane);
    }

    public TextBox(ObservableList<Node> nodes) {
        this("", nodes);
    }

    public TextBox(String s, Pane pane) {
        text.setText(s);
        init(pane.getChildren());
    }

    public TextBox(String s, ObservableList<Node> nodes) {
        text.setText(s);
        init(nodes);
    }

    private void init(ObservableList<Node> nodes){
        box.setFill(Color.TRANSPARENT);
        box.setStroke(Color.BLACK);
        box.setStrokeWidth(2);

        text.setStroke(Color.BLACK);
        text.setTextOrigin(VPos.CENTER);

        nodes.addAll(text, box);
        adjustBox();
    }

    public Rectangle getBox(){
        return box;
    }

    public String getText(){
        return text.getText();
    }

    public void show(){
        box.setOpacity(1);
        text.setOpacity(1);
    }

    public void hide(){
        box.setOpacity(0);
        text.setOpacity(0);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
        adjustBox();
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
        adjustBox();
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        adjustBox();
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        adjustBox();
    }

    public void setText(String text){
        this.text.setText(text);
        adjustBox();
    }

    public double getXPadding() {
        return xPadding;
    }

    public void setxPadding(double xPadding) {
        this.xPadding = xPadding;
        adjustBox();
    }

    public double getYPadding() {
        return yPadding;
    }

    public void setyPadding(double yPadding) {
        this.yPadding = yPadding;
        adjustBox();
    }

    private void adjustBox(){
        calculateFont();

        box.setX(x);
        box.setY(y);

        text.setLayoutX(x + getXPadding());
        text.setLayoutY(y + height/2);

        box.setWidth(width);
        box.setHeight(height);
    }

    private void calculateFont(){
        double desiredWidth = width - 2*getXPadding();
        double desiredHeight = height - 2*getYPadding();

        double sampleWidth = Utils.computeTextWidth(new Font("System Regular", 20), text.getText(), 0);
        double fontSize = (desiredWidth*20) / sampleWidth;

        double yScale = desiredHeight / Utils.computeTextHeight(new Font("System Regular", fontSize), text.getText(), 0);

        text.setFont(new Font("System Regular", fontSize));
        text.setScaleY(yScale);
    }
}
