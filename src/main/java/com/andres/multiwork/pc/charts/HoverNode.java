package com.andres.multiwork.pc.charts;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

class HoverNode extends StackPane {

    public HoverNode(double value) {
        setPrefSize(10, 10);

        final Label label = createDataThresholdLabel(value);

        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                getChildren().clear();
                setCursor(Cursor.DEFAULT);
            }
        });
    }

    private Label createDataThresholdLabel(double value) {
        final Label label = new Label(timeToLabel(value));

        label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
        label.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");
        label.setTextFill(Color.FIREBRICK);

        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }

    /**
     * Converts time in seconds in the best way to show it (mS, uS or nS)
     * @param time time in seconds to convert
     * @return {@link java.lang.String} representation of the converted time with the corresponding unit
     */
    private String timeToLabel(double time){

        // Time > 1000uS, show it as mS
        if(time * 1E6 >= 1000){
            return String.format("%.2f", time*1E3) + " mS";
        }

        // Time > 1000nS show it as uS
        else if(time * 1E9 >= 1000){
            return String.format("%.2f", time*1E6) + " μS";
        }

        // Else, show it as nS
        else{
            return String.format("%.2f", time*1E9) + " nS";
        }
    }
}
