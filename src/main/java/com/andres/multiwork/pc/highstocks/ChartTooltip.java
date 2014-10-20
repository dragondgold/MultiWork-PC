package com.andres.multiwork.pc.highstocks;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ChartTooltip extends StackPane {

    private Label label = new Label();

    public ChartTooltip() {
        super();
        init();
    }

    public ChartTooltip(Node... children) {
        super(children);
        init();
    }

    public void setText(String text){
        label.setText(text);
    }

    public void show(double x, double y){
        setOpacity(0.85);
        setLayoutX(x);
        setLayoutY(y);
    }

    public void hide(){
        setOpacity(0);
    }

    private void init(){
        getChildren().add(label);
        setAlignment(Pos.CENTER);

        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(13));
        setMouseTransparent(true);
        setStyle("-fx-border-color: black; -fx-border-width: 1.5; -fx-background-color: #242424; -fx-padding: 5px;" +
                 "-fx-border-radius: 10 10 10 10;\n" +
                 "-fx-background-radius: 10 10 10 10;");
    }

}
