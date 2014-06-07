package com.andres.multiwork.pc.charts;

import com.andres.multiwork.pc.GlobalValues;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.awt.*;
import java.util.ArrayList;

public class LogicChartView extends LineChart {

    private XYChart.Series<Double, Double>[] channelsSeries = new XYChart.Series[GlobalValues.channelsNumber];
    private ObservableList<Series<Double, Double>> seriesDataSet = FXCollections.observableArrayList();

    // Available time scales for zoom in x axis
    private final double timeScaleValues[] = {
            1E-9,		// 1nS
            5E-9,		// 5nS
            10E-9,		// 10nS
            50E-9,      // 50nS
            100E-9,		// 100nS
            1E-6,		// 1uS
            5E-6,       // 5uS
            10E-6,		// 10uS
            50E-6,      // 50uS
            100E-6,		// 100uS
            1E-3,		// 1mS
            5E-3,       // 5mS
            10E-3,		// 10mS
            50E-3,      // 50mS
            100E-3		// 100mS
    };

    private final int horizontalDivisions = 40;
    private int currentScaleIndex = 4;
    private int previousScaleIndex = currentScaleIndex;
    private double currentScale = timeScaleValues[currentScaleIndex];
    private boolean applyScale = true;
    private OnChartSliding onChartSliding;

    private ArrayList<BetterAnnotation> annotations = new ArrayList<>();
    private double prevX;
    private boolean showHoverValues = false;
    boolean paneAdded = false;
    private Pane annotationPane = new Pane();

    public LogicChartView(){
        super(new NumberAxis(), new NumberAxis());

        annotationPane.setMouseTransparent(true);

        for(int n = 0; n < GlobalValues.channelsNumber; ++n){
            channelsSeries[n] = new XYChart.Series<>();
            channelsSeries[n].setName(GlobalValues.resourceBundle.getString("channel") + " " + n);

            seriesDataSet.add(channelsSeries[n]);
        }
        setData(seriesDataSet);
        setAnimated(false);

        setOnMousePressed(mouseEvent -> prevX = mouseEvent.getX());

        setOnMouseDragged(mouseEvent -> {
            // Chart panning
            NumberAxis xAxis = ((NumberAxis)getXAxis());
            double axisWidth = xAxis.getUpperBound() - xAxis.getLowerBound();

            Node chartArea = lookup(".chart-plot-background");
            Bounds chartBounds = chartArea.localToScene(chartArea.getBoundsInLocal());
            double chartWidth = chartBounds.getWidth();

            double dragDistance = prevX - mouseEvent.getX();
            double chartSlideDistance = Math.abs((dragDistance * axisWidth) / chartWidth);

            // Mouse dragged to the left
            if(dragDistance > 0){
                xAxis.setUpperBound(xAxis.getUpperBound() + chartSlideDistance);
                xAxis.setLowerBound(xAxis.getLowerBound() + chartSlideDistance);
            }else {
                double lowerBound = xAxis.getLowerBound() - chartSlideDistance;
                if(lowerBound < 0) return;

                xAxis.setUpperBound(xAxis.getUpperBound() - chartSlideDistance);
                xAxis.setLowerBound(lowerBound);
            }

            if(onChartSliding != null)
                onChartSliding.onChartSliding(xAxis.getLowerBound(), xAxis.getUpperBound());

            prevX = mouseEvent.getX();
        });

        getXAxis().setAutoRanging(false);
        ((NumberAxis) getXAxis()).setLowerBound(0);
        ((NumberAxis) getXAxis()).setUpperBound(((NumberAxis) getXAxis()).getLowerBound() + toCoordinate(horizontalDivisions * currentScale));
    }

    public void setOnChartSliding(OnChartSliding onChartSliding){
        this.onChartSliding = onChartSliding;
    }

    public double getTimeScale(){
        return currentScale;
    }

    public void applyScale(boolean applyScale){
        this.applyScale = applyScale;
    }

    /**
     * Add an annotation in the chart with a rectangle surrounding the text. The annotations is not shown until
     * renderAnnotations() is called
     * @param text annotation text
     * @param t1 x position of the top left corner
     * @param t2 x position of the bottom right corner
     * @param y y position of the top side
     * @param height height of the text which is drawn below the y position
     */
    public void addAnnotation(String text, double t1, double t2, double y, double height){
        if(applyScale){
            BetterAnnotation annotation = new BetterAnnotation(text, t1, y, t2-t1, height);
            annotation.hide();

            annotationPane.getChildren().add(annotation);
            annotations.add(annotation);
        }else {
            // TODO: add annotation when we are not applying scale
        }
    }

    public void addData(int channelNumber, double time, double y){
        if(applyScale) {
            Data data = new Data<Double, Double>(toCoordinate(time), y);
            if(showHoverValues) data.setNode(new HoverNode(time));
            channelsSeries[channelNumber].getData().add(data);
        }
        else {
            Data data = new Data<Double, Double>(time, y);
            if(showHoverValues) data.setNode(new HoverNode(time));
            channelsSeries[channelNumber].getData().add(data);
        }
    }

    public void zoomOut(){
        // Next time scale
        previousScaleIndex = currentScaleIndex;

        ++currentScaleIndex;
        if(currentScaleIndex >= timeScaleValues.length){
            currentScaleIndex = timeScaleValues.length-1;
            return;
        }
        currentScale = timeScaleValues[currentScaleIndex];
        rescale();
        getXAxis().setLabel(timeToLabel(currentScale));
    }

    public void zoomIn(){
        // Previous time scale
        previousScaleIndex = currentScaleIndex;

        --currentScaleIndex;
        if(currentScaleIndex < 0){
            currentScaleIndex = 0;
            return;
        }
        currentScale = timeScaleValues[currentScaleIndex];
        rescale();
        getXAxis().setLabel(timeToLabel(currentScale));
    }

    public boolean isShowHoverValues() {
        return showHoverValues;
    }

    public void setShowHoverValues(boolean showHoverValues) {
        this.showHoverValues = showHoverValues;
    }

    /** Render whatever we want when chart layout is about to be rendered */
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();

        if(!paneAdded) {
            ((Pane) getParent()).getChildren().add(annotationPane);
            paneAdded = true;
        }

        /** Annotations pane setup */
        // Set annotation pane position and add clipping to it so annotations doesn't go over chart plot area
        Node chartArea = lookup(".chart-plot-background");
        Bounds chartBounds = chartArea.localToScene(chartArea.getBoundsInLocal());
        Rectangle clippingArea = new Rectangle();

        annotationPane.setLayoutX(0);
        annotationPane.setLayoutY(0);
        annotationPane.setPrefWidth(getWidth());
        annotationPane.setPrefHeight(getScene().getHeight());
        //annotationPane.setStyle("-fx-border-color: red; -fx-border-width: 4;");

        clippingArea.setLayoutX(chartBounds.getMinX());
        clippingArea.setLayoutY(chartBounds.getMinY());
        clippingArea.setWidth(chartBounds.getWidth());
        clippingArea.setHeight(chartBounds.getHeight());

        // Clip the pane so we can't render outside chart plotting area
        annotationPane.setClip(clippingArea);

        // Axis time
        getXAxis().setLabel(timeToLabel(currentScale));

        // Line width
        for(int n = 0; n < GlobalValues.channelsNumber; ++n) {
            channelsSeries[n].nodeProperty().get().setStyle("-fx-stroke-width: 2px;");
        }

        // Every time the chart is redrawn we have to update annotations positions
        renderAnnotations();
    }

    /**
     * Set new chart bounds after zooming in/out. We zoom in around mouse position
     */
    private void rescale(){
        double originalMidValue;
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

        // If mouse pointer is inside chart zoom around mouse, otherwise zoom around the current displayed mid value
        if(getBoundsInParent().contains(mouseLocation.getX(), mouseLocation.getY())){
            originalMidValue = ((NumberAxis)getXAxis()).getValueForDisplay(mouseLocation.getX()).doubleValue() * timeScaleValues[previousScaleIndex];
        }
        else{
            double midValue = (((NumberAxis)getXAxis()).getUpperBound() + ((NumberAxis)getXAxis()).getLowerBound())/2;
            originalMidValue = midValue * timeScaleValues[previousScaleIndex];
        }

        // Replace all the x coordinates with the corresponding new ones according to the new time scale
        for(XYChart.Series<Double, Double> series : channelsSeries){
            ObservableList<Data<Double, Double>> dataList = series.getData();

            for(int n = 0; n < dataList.size(); ++n) {
                Data<Double, Double> data = dataList.get(n);

                double currentValue = data.getXValue();
                double originalValue = currentValue * timeScaleValues[previousScaleIndex];
                double newValue = toCoordinate(originalValue);

                data.setXValue(newValue);
            }
        }

        // Calculate the new bounds taking into account we zoom in the mid value of the chart.
        double lowerBound = toCoordinate(originalMidValue) - toCoordinate(horizontalDivisions * (currentScale/2));
        if(lowerBound < 0) lowerBound = 0;
        ((NumberAxis) getXAxis()).setLowerBound(lowerBound);

        // Upper bound
        ((NumberAxis) getXAxis()).setUpperBound(toCoordinate(horizontalDivisions * (currentScale/2)) + toCoordinate(originalMidValue));

        // Calculate the new tick unit
        double lowerX = ((NumberAxis) getXAxis()).getLowerBound();
        double upperX = ((NumberAxis) getXAxis()).getUpperBound();
        double newWidth = Math.abs(upperX - lowerX);

        ((NumberAxis) getXAxis()).setTickUnit(newWidth / horizontalDivisions);
    }

    /**
     * Render all the added annotations with the current scale
     */
    public void renderAnnotations(){
        /**
         * We have to ensure the new chart scale is rendered in order for getDisplayPosition() to return
         * correct values. Sometimes Platform.runLater() makes the code run in the next pulse but in this
         * case didn't work so I add a larger delay of 10mS
         */
        Axis<Double> xAxis = getXAxis();
        Axis<Double> yAxis = getYAxis();

        Node chartArea = lookup(".chart-plot-background");
        Bounds chartBounds = chartArea.localToScene(chartArea.getBoundsInLocal());

        double xShift = chartBounds.getMinX();
        double yShift = chartBounds.getMinY();

        for(BetterAnnotation annotation : annotations) {
            final double t1 = annotation.getX1();
            final double t2 = annotation.getX2();
            final double y = annotation.getY();
            final double height = annotation.getAnnotationHeight();

            annotation.setLayoutX(xAxis.getDisplayPosition(toCoordinate(t1)) + xShift);
            annotation.setAnnotationWidth(xAxis.getDisplayPosition(toCoordinate(t2)) - xAxis.getDisplayPosition(toCoordinate(t1)));

            annotation.setLayoutY(yAxis.getDisplayPosition(y) + yShift);
            annotation.setAnnotationHeight(yAxis.getDisplayPosition(y) - yAxis.getDisplayPosition(y + height));
            annotation.show();
        }
    }

    /**
     * Converts time in seconds in the best way to show it (mS, uS or nS)
     * @param time time in seconds to convert
     * @return {@link java.lang.String} representation of the converted time with the corresponding unit
     */
    private String timeToLabel(double time){

        // Time > 1000uS, show it as mS
        if(time * 1E6 >= 1000){
            return "x" + String.format("%.2f", time*1E3) + " mS";
        }

        // Time > 1000nS show it as uS
        else if(time * 1E9 >= 1000){
            return "x" + String.format("%.2f", time*1E6) + " μS";
        }

        // Else, show it as nS
        else{
            return "x" + String.format("%.2f", time*1E9) + " nS";
        }
    }

    /**
     * Converts time in seconds to the corresponding x coordinate in the chart according to the current
     * time scale
     * @param time time in seconds to convert
     * @return x coordinate in the chart according to the current time scale
     */
    private double toCoordinate (double time){
        return (time/currentScale);
    }

}
