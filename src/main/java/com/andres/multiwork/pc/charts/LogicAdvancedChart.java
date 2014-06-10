package com.andres.multiwork.pc.charts;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Timer;
import java.util.TimerTask;

public class LogicAdvancedChart {

    private final double yChannel[] = {1, 8, 15, 22, 29, 36, 43, 50};
    private final double bitScale = 1.5;

    private LogicChartView mainChart = new LogicChartView();
    private LogicChartView smallChart = new LogicChartView();
    private Rectangle areaSelector = new Rectangle();

    private double width, height, layoutX, layoutY, prevX = -1;

    public LogicAdvancedChart(Pane pane){
        pane.getChildren().addAll(mainChart, smallChart, areaSelector);

        smallChart.setTitle("");
        smallChart.getXAxis().setAutoRanging(true);
        smallChart.getYAxis().setAutoRanging(true);
        smallChart.getXAxis().setOpacity(0);
        smallChart.setLegendVisible(false);
        smallChart.applyScale(false);

        // Symbols refers to the points drawn in the series
        smallChart.setCreateSymbols(false);
        updateLayout();

        mainChart.setShowHoverValues(true);
        smallChart.setShowHoverValues(false);

        // Scroll zooming
        pane.setOnScroll(scrollEvent -> {
            // Scrolling up
            if (scrollEvent.getDeltaY() > 0) {
                zoomIn();
            }
            // Scrolling down
            else {
                zoomOut();
            }
        });

        mainChart.setOnChartSliding((newUpperBound, newLowerBound) -> updateAreaSelector());

        pane.setOnMousePressed(mouseEvent -> {
            double x = mouseEvent.getX();
            double y = mouseEvent.getY();

            if(areaSelector.contains(x, y)){
                prevX = x;
            }else{
                prevX = -1;
            }
        });
        pane.setOnMouseDragged(mouseEvent -> {
            if(prevX < 0) return;

            // Drag the selector with the mouse pointer, we invert the drag distance because it's inverted
            // Moving to the left is positive and moving to the right is negative
            double dragDistance = -(prevX - mouseEvent.getX());
            double x = areaSelector.getX() + dragDistance;

            // Wrap so the rectangle doesn't go over chart limits
            Node smallChartArea = smallChart.lookup(".chart-plot-background");
            Bounds smallChartBounds = smallChartArea.localToScene(smallChartArea.getBoundsInLocal());

            // Shift of x coordinate due to the y axis on the left
            double xShift = smallChartBounds.getMinX();

            // Wrap
            if(x < xShift)
                x = xShift;
            else if((x + areaSelector.getWidth()) > (smallChartBounds.getMaxX()))
                x = smallChartBounds.getMaxX() - areaSelector.getWidth();

            areaSelector.setX(x);

            // Calculate start and end x coordinate of the main chart according to it's current scale
            Axis<Double> smallXAxis = smallChart.getXAxis();

            // Subtract xShift because the position is relative to the axis, not to the scene
            double startX = smallXAxis.getValueForDisplay(areaSelector.getX() - xShift) / mainChart.getTimeScale();
            double endX = smallXAxis.getValueForDisplay(areaSelector.getX() + areaSelector.getWidth() - xShift) / mainChart.getTimeScale();

            ((NumberAxis)mainChart.getXAxis()).setLowerBound(startX);
            ((NumberAxis)mainChart.getXAxis()).setUpperBound(endX);

            prevX = mouseEvent.getX();
        });
    }

    /**
     * Sets mouse events listener to series
     * @param seriesEvent {@link com.andres.multiwork.pc.charts.SeriesEvent}
     */
    public void setSeriesEventListener(SeriesEvent seriesEvent){
        mainChart.setSeriesEventListener(seriesEvent);
    }

    /**
     * Add data to the chart
     * @param channelNumber channel number where to add the data
     * @param time time in seconds
     * @param y y coordinate
     */
    public void addData(int channelNumber, double time, double y){
        mainChart.addData(channelNumber, time, y);
        smallChart.addData(channelNumber, time, y);
    }

    /**
     * Add logic data to the chart
     * @param channelNumber channel number where to add the data
     * @param time time in seconds
     * @param state true or false representing '1' or '0' state
     */
    public void addLogicData(int channelNumber, double time, boolean state){
        if(state) {
            mainChart.addData(channelNumber, time, yChannel[channelNumber] + bitScale);
            smallChart.addData(channelNumber, time, yChannel[channelNumber] + bitScale);
        }else{
            mainChart.addData(channelNumber, time, yChannel[channelNumber]);
            smallChart.addData(channelNumber, time, yChannel[channelNumber]);
        }
    }

    /**
     * Add an annotation in the chart with a rectangle surrounding the text
     * @param text annotation text
     * @param t1 x position of the top left corner
     * @param t2 x position of the bottom right corner
     * @param y y position of the top side
     * @param height height of the text which is drawn below the y position
     */
    public void addAnnotation(final String text, final double t1, final double t2, final double y, final double height){
        mainChart.addAnnotation(text, t1, t2, y, height);
    }

    /**
     * Add an annotation in the chart with a rectangle surrounding the text
     * @param text annotation text
     * @param t1 x position of the top left corner
     * @param t2 x position of the bottom right corner
     * @param channelNumber channel number where to add the annotation
     */
    public void addLogicAnnotation(final String text, final double t1, final double t2, int channelNumber){
        mainChart.addAnnotation(text, t1, t2, yChannel[channelNumber] + 3*bitScale, 2);
    }

    /**
     * Zoom in to the next time scale available in chart
     */
    public void zoomIn(){
        mainChart.zoomIn();
        updateAreaSelector();
    }

    /**
     * Zoom out to the next time scale available in chart
     */
    public void zoomOut(){
        mainChart.zoomOut();
        updateAreaSelector();
    }

    /**
     * Get the main chart
     * @return main chart
     */
    public LogicChartView getMainChart(){
        return mainChart;
    }

    /**
     * Get the small chart View which is displayed below the main chart. It is used to give a global View of all the
     * data in the chart
     * @return small chart
     */
    public LogicChartView getSmallChart(){
        return smallChart;
    }

    /**
     * Set chart height
     * @param height chart height
     */
    public void setHeight(double height){
        this.height = height;
        updateLayout();
    }

    /**
     * Get chart height
     * @return chart height
     */
    public double getHeight(){
        return height;
    }

    /**
     * Set chart width
     * @param width chart width
     */
    public void setWidth(double width){
        this.width = width;
        updateLayout();
    }

    /**
     * Get chart width
     * @return chart width
     */
    public double getWidth(){
        return width;
    }

    /**
     * Set the top left Y coordinate of the chart
     * @param y top left Y coordinate of the chart
     */
    public void setY(double y){
        layoutY = y;
        updateLayout();
    }

    /**
     * Get the top left Y coordinate of the chart
     * @return top left Y coordinate of the chart
     */
    public double getY(){
        return layoutY;
    }

    /**
     * Set the top left X coordinate of the chart
     * @param x top left X coordinate of the chart
     */
    public void setX(double x){
        layoutX = x;
        updateLayout();
    }

    /**
     * Get the top left X coordinate of the chart
     * @return top left X coordinate of the chart
     */
    public double getX(){
        return layoutX;
    }

    /**
     * Update the area selector and annotations on the chart. This should be called every time we change annotations or
     * data in the chart!
     */
    public void updateChart(){
        updateAreaSelector();
        mainChart.renderAnnotations();
    }

    /**
     * Update charts layout. Here we take care of assigning all the Scene space between the two chart. The small
     * chart takes 30% of the specified height by the user.
     */
    private void updateLayout(){
        double titleHeight = smallChart.lookup(".chart-title").getBoundsInParent().getHeight();
        double xAxisHeight = smallChart.lookup(".axis").getBoundsInParent().getHeight();
        double legendHeight = smallChart.lookup(".chart-legend").getBoundsInParent().getHeight();

        // Calculate the main chart height taking into account small chart height
        double mainChartHeight = getHeight() - getHeight()*0.3;

        mainChart.setLayoutX(getX());
        mainChart.setLayoutY(getY());
        smallChart.setLayoutX(getX());
        smallChart.setLayoutY(getY() + mainChartHeight - titleHeight);

        mainChart.setPrefHeight(mainChartHeight);
        smallChart.setPrefHeight(getHeight()*0.3 + xAxisHeight + legendHeight);

        mainChart.setPrefWidth(getWidth());
        smallChart.setPrefWidth(getWidth());

        /**
         * Why we use Platform.runLater() if I can directly do this? JavaFX doesn't immediately render whatever we modify.
         * Every change we is scheduled and rendered in the next "pulse" so methods like getDisplayPosition() or modified
         *  bounds are not updated until the next "pulse". With Platform.runLater() we schedule the code so it is executed
         *  after the current pulsed is rendered, so, on this way in the next pulse when this code is executed we have the
         *  updated values according to the current render.
         */
        Platform.runLater(() -> {

        });
    }

    /**
     * Update area selector based in the current chart bounds. The area selector is the rectangle we move on the small
     * chart to view a specific part of all the data in the chart.
     */
    private void updateAreaSelector(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    Axis<Double> xAxis = smallChart.getXAxis();
                    Node smallChartArea = smallChart.lookup(".chart-plot-background");
                    Bounds smallChartBounds = smallChartArea.localToScene(smallChartArea.getBoundsInLocal());

                    // Shift of x coordinate due to the y axis on the left
                    double xShift = smallChartBounds.getMinX();

                    // Get the max and min showed points in the big chart without scale being applied
                    double originalLowerX = ((NumberAxis)mainChart.getXAxis()).getLowerBound() * mainChart.getTimeScale();
                    double originalUpperX = ((NumberAxis)mainChart.getXAxis()).getUpperBound() * mainChart.getTimeScale();

                    // Calculate the display coordinates in the small chart
                    double startX = xAxis.getDisplayPosition(originalLowerX) + xShift;
                    double endX = xAxis.getDisplayPosition(originalUpperX) + xShift;

                    // Draw the rectangle
                    areaSelector.setX(startX);
                    areaSelector.setY(smallChartBounds.getMinY());

                    // Wrap so it doesn't go further than the small chart width
                    if(endX > smallChartBounds.getMaxX())
                        endX = smallChartBounds.getMaxX();

                    areaSelector.setWidth(endX - startX);
                    areaSelector.setHeight(smallChartBounds.getHeight());
                    areaSelector.setFill(Color.rgb(51, 102, 255, 0.2));
                });
            }
        }, 10);
    }

}
