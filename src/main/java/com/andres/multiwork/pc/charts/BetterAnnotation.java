package com.andres.multiwork.pc.charts;

import com.andres.multiwork.pc.utils.Utils;
import javafx.event.EventHandler;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class BetterAnnotation extends StackPane {

    private final double x1, x2, y, height;
    private final double xPadding = 5, yPadding = 2;
    private final Text text;
    private final String stringText;
    private final String wordSeparator = "\t\t\t\t\t\t\t";

    public BetterAnnotation(String string, double x, double y, double width, double height){
        stringText = string;
        this.x1 = x;
        this.x2 = x + width;
        this.y = y;
        this.height = height;

        text = new Text(string);
        text.setTextAlignment(TextAlignment.CENTER);

        setStyle("-fx-border-color: red; -fx-border-width: 1.5; -fx-border-radius: 5 5 5 5;");
        text.setFill(Color.FIREBRICK);

        setMinSize(width, height);
        setMaxSize(width, height);
        setLayoutX(x);
        setLayoutY(y);

        calculateFont(width, height);
        getChildren().setAll(text);
    }

    private void calculateFont(double desiredWidth, double desiredHeight){
        desiredWidth = desiredWidth - (2*xPadding);
        desiredHeight = desiredHeight - (2*yPadding);

        // Calculate font size to fit the height
        double sampleHeight = Utils.computeTextHeight(Font.font("Arial", FontWeight.EXTRA_BOLD, 20), stringText, 0);
        double fontSize = (desiredHeight * 20) / sampleHeight;

        double wordWidth = Utils.computeTextWidth(Font.font("Arial", FontWeight.EXTRA_BOLD, fontSize), stringText + wordSeparator, 0);

        //TODO: text.getText() always return an empty String and I don't know why! I shouldn't need a separate variable
        final String string = stringText + wordSeparator;
        int wordNumber = (int) Math.floor(desiredWidth/wordWidth);

        // Append the text calculated times to fit the width, we add at least one
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(string);
        for(int n = 0; n < (wordNumber-1); n++){
            stringBuffer.append(string);
        }

        text.setText(stringBuffer.toString().trim());
        text.setFont(new Font("Arial", fontSize));
    }

    public void setAnnotationHeight(double height){
        setMinHeight(height);
        setMaxHeight(height);
        calculateFont(getMinWidth(), height);
    }

    public void setAnnotationWidth(double width){
        setMinWidth(width);
        setMaxWidth(width);
        calculateFont(width, getMinHeight());
    }

    public String getText(){
        return stringText;
    }

    public double getX1() {
        return x1;
    }

    public double getX2() {
        return x2;
    }

    public double getY() {
        return y;
    }

    public double getAnnotationHeight(){
        return height;
    }

    public void show(){
        setOpacity(1);
    }

    public void hide(){
        setOpacity(0);
    }

    public boolean isShowing(){
        return getOpacity() == 1;
    }
}
