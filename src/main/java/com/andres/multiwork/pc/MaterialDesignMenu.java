package com.andres.multiwork.pc;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class MaterialDesignMenu extends Menu {

    private Pane stackPane = new Pane();
    private Label label = new Label();

    private Circle circleRipple;
    private Rectangle rippleClip = new Rectangle();
    private Duration rippleDuration =  Duration.millis(250);
    private double lastRippleHeight = 0;
    private double lastRippleWidth = 0;
    private Color rippleColor = new Color(1, 0, 0, 0.3);

    public MaterialDesignMenu() {
        init("");
    }

    public MaterialDesignMenu(String text) {
        init(text);
    }

    public MaterialDesignMenu(String text, Node graphic) {
        init(text);
    }

    private void init(String text){
        label.setText(text);
        createRippleEffect();

        stackPane.getChildren().addAll(circleRipple, label);
        setGraphic(stackPane);
    }

    private void createRippleEffect() {
        circleRipple = new Circle(0.1, rippleColor);
        circleRipple.setOpacity(0.0);
        // Optional box blur on ripple - smoother ripple effect
        //circleRipple.setEffect(new BoxBlur(3, 3, 2));
        // Fade effect bit longer to show edges on the end of animation
        final FadeTransition fadeTransition = new FadeTransition(rippleDuration, circleRipple);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        final Timeline scaleRippleTimeline = new Timeline();
        final SequentialTransition parallelTransition = new SequentialTransition();
        parallelTransition.getChildren().addAll(
                scaleRippleTimeline,
                fadeTransition
        );
        // When ripple transition is finished then reset circleRipple to starting point
        parallelTransition.setOnFinished(event -> {
            circleRipple.setOpacity(0.0);
            circleRipple.setRadius(0.1);
        });

        stackPane.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            parallelTransition.stop();
            // Manually fire finish event
            parallelTransition.getOnFinished().handle(null);
            circleRipple.setCenterX(event.getX());
            circleRipple.setCenterY(event.getY());

            // Recalculate ripple size if size of button from last time was changed
            if (stackPane.getWidth() != lastRippleWidth || stackPane.getHeight() != lastRippleHeight) {
                lastRippleWidth = stackPane.getWidth();
                lastRippleHeight = stackPane.getHeight();
                rippleClip.setWidth(lastRippleWidth);
                rippleClip.setHeight(lastRippleHeight);
                /*
                // try block because of possible null of Background, fills ...
                try {
                    rippleClip.setArcHeight(stackPane.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius());
                    rippleClip.setArcWidth(stackPane.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius());
                    circleRipple.setClip(rippleClip);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

                circleRipple.setClip(rippleClip);
                // Getting 45% of longest button's length, because we want edge of ripple effect always visible
                double circleRippleRadius = Math.max(stackPane.getHeight(), stackPane.getWidth()) * 0.45;
                final KeyValue keyValue = new KeyValue(circleRipple.radiusProperty(), circleRippleRadius, Interpolator.EASE_OUT);
                final KeyFrame keyFrame = new KeyFrame(rippleDuration, keyValue);
                scaleRippleTimeline.getKeyFrames().clear();
                scaleRippleTimeline.getKeyFrames().add(keyFrame);
            }
            parallelTransition.playFromStart();
        });
    }

    public void setRippleColor(Color color) {
        circleRipple.setFill(color);
    }
}
