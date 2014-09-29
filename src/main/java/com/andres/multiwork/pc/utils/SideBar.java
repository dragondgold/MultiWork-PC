package com.andres.multiwork.pc.utils;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Animates a node on and off screen to the left. */
public class SideBar extends VBox {

    private final Animation hideSidebar;
    private final Animation showSidebar;

    private boolean isShowing = true;
    private final int minWidth = 1;

    /** creates a sidebar containing a vertical alignment of the given nodes */
    public SideBar(final double expandedWidth, Node... nodes) {
        VBox.setVgrow(this, Priority.ALWAYS);
        getStyleClass().add("sidebar");
        this.setPrefWidth(expandedWidth);
        this.setMinWidth(0);

        // create a bar to hide and show.
        setAlignment(Pos.CENTER);
        getChildren().addAll(nodes);

        // Create an animation to hide sidebar.
        hideSidebar = new Transition() {
            { setCycleDuration(Duration.millis(250)); }
            protected void interpolate(double frac) {
                final double curWidth = expandedWidth * (1.0 - frac);
                if(curWidth >= minWidth) setPrefWidth(curWidth);
                //setTranslateX(-expandedWidth + curWidth);
            }
        };
        hideSidebar.onFinishedProperty().set(actionEvent -> setPrefWidth(minWidth));

        // Create an animation to show sidebar.
        showSidebar = new Transition() {
            { setCycleDuration(Duration.millis(250)); }
            protected void interpolate(double frac) {
                final double curWidth = expandedWidth * frac;
                setPrefWidth(curWidth);
                //setTranslateX(-expandedWidth + curWidth);
            }
        };
        showSidebar.onFinishedProperty().set(actionEvent -> {

        });

        hide();

        // Hide on double click
        setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) hide();
        });

        // Show when mouse is on the left side of the window
        setOnMouseMoved(event -> {
            if(event.getScreenX() < 3){
                if(!isShowing) show();
            }
        });
    }

    public void show(){
        if (showSidebar.statusProperty().get() == Animation.Status.STOPPED && hideSidebar.statusProperty().get() == Animation.Status.STOPPED) {
            showSidebar.play();
            isShowing = true;
        }
    }

    public void hide(){
        if (showSidebar.statusProperty().get() == Animation.Status.STOPPED && hideSidebar.statusProperty().get() == Animation.Status.STOPPED) {
            hideSidebar.play();
            isShowing = false;
        }
    }

    /**
     * Toggle side-bar status
     */
    public void toggle(){
        if (isShowing) hide();
        else show();
    }
}