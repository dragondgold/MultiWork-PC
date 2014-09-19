package com.andres.multiwork.pc.highstocks;

import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class HighStockChart {

    private WebView webView;
    private WebEngine webEngine;

    private SeriesLegendShiftClick seriesLegendShiftClick;
    private ChartLoaded chartLoaded;
    private AnnotationEvent annotationEvent;

    private double yChannel[] = {1, 8, 15, 22, 29, 36, 43, 50};
    private double bitScale = 2;

    private int defaultAnnotationFontSize = 10;
    private String defaultAnnotationTextColor = "white";

    // Store position parameter of the added annotations. We need them to redraw the annotations in the chart
    //  every time zoom is changed because annotation width and height is on pixels so we need to update it to
    //  the new scale.
    private ArrayList<AnnotationReference> annotationsList = new ArrayList<>();

    public HighStockChart(Pane pane){
        webView = new WebView();
        webEngine = webView.getEngine();

        try {
            webEngine.load(getClass().getResource("/HighStock/chart.htm").toURI().toURL().toString());
            pane.getChildren().add(webView);

            webView.setContextMenuEnabled(false);

            // Reload on size changed
            webView.heightProperty().addListener((observable, oldValue, newValue) -> webEngine.reload());

            // Listen for alert() events to communicate between Java and Javascript side
            webEngine.setOnAlert(event -> {
                final String data = event.getData();

                // Shift click event on series legend
                if(data.contains("Series Legend Shift Click")){
                    Double[] parsedData = numericStringParse(data);

                    if(seriesLegendShiftClick != null)
                        seriesLegendShiftClick.onSeriesLegendShiftClick(parsedData[0].intValue(), parsedData[1], parsedData[2]);

                }else if(data.contains("Chart Loaded Event")) {
                    if (chartLoaded != null) chartLoaded.onChartLoaded();

                }else if(data.contains("Chart Redraw Event")){
                    updateAnnotations();

                // TODO:
                }else if(data.contains("Annotation Click")){
                    String[] result = data.split(",");
                    // If annotation text is set to "" then the second string at position [1]
                    //  doesn't exists
                    String text;
                    if(result.length <= 1) text = "";
                    else text = result[1];

                    Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

                    if(annotationEvent != null) annotationEvent.onAnnotationClicked(text, mouseLocation.getX(), mouseLocation.getY());

                // TODO:
                }else if(data.contains("Annotation Enter")){
                    String[] result = data.split(",");
                    // If annotation text is set to "" then the second string at position [1]
                    //  doesn't exists
                    String text;
                    if(result.length <= 1) text = "";
                    else text = result[1];

                    Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

                    if(annotationEvent != null) annotationEvent.onMouseEnter(text, mouseLocation.getX(), mouseLocation.getY());

                // TODO:
                }else if(data.contains("Annotation Out")){
                    String[] result = data.split(",");
                    // If annotation text is set to "" then the second string at position [1]
                    //  doesn't exists
                    String text;
                    if(result.length <= 1) text = "";
                    else text = result[1];

                    Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

                    if(annotationEvent != null) annotationEvent.onMouseOut(text, mouseLocation.getX(), mouseLocation.getY());

                }else {
                    System.out.println(data);
                }
            });

        } catch (Exception e) { e.printStackTrace(); }
    }

    public void setOnChartLoaded(ChartLoaded chartLoaded) {
        this.chartLoaded = chartLoaded;
    }

    public void setSeriesLegendShiftClick(SeriesLegendShiftClick seriesLegendShiftClick) {
        this.seriesLegendShiftClick = seriesLegendShiftClick;
    }

    public void setAnnotationEvent(AnnotationEvent annotationEvent) {
        this.annotationEvent = annotationEvent;
    }

    /**
     * Dim the chart's plot area and show a loading label text. A custom text can be given as a parameter for loading.
     * @param str {@link String} to be showed, if null default "Loading..." is showed.
     */
    public void showLoading(String str){
        if(str == null) webEngine.executeScript("getChart().showLoading()");
        else webEngine.executeScript("getChart().showLoading(\"" + str + "\")");
    }

    /**
     * Hide the loading screen showed using {@link HighStockChart#showLoading(String)}
     */
    public void hideLoading(){
        webEngine.executeScript("getChart().hideLoading()");
    }

    public void setBitScale(double bitScale) {
        this.bitScale = bitScale;
    }

    public void setyChannel(double[] yChannel) {
        this.yChannel = yChannel;
    }

    public void setXAxisExtremes(double lowerBound, double upperBound, boolean forceRedraw){
        webEngine.executeScript("getChart().xAxis[0].setExtremes(" + lowerBound + "," + upperBound + "," + forceRedraw + ")");
    }

    public void setYAxisExtremes(double lowerBound, double upperBound, boolean forceRedraw){
        webEngine.executeScript("getChart().yAxis[0].setExtremes(" + lowerBound + "," + upperBound + "," + forceRedraw + ")");
    }

    public void setXAxisLabel(String str){
        webEngine.executeScript("getChart().xAxis[0].update({title:{ text: '" + str + "'}})");
    }

    public void setYAxisLabel(String str){
        webEngine.executeScript("getChart().yAxis[0].update({title:{ text: '" + str + "'}})");
    }

    public void removeAllAnnotations(){
        webEngine.executeScript("removeAllAnnotations()");
        annotationsList.clear();
    }
    
    public void clearAllSeries(){
        webEngine.executeScript("clearAllSeries()");
    }

    /**
     * Add logic data to the chart
     * @param channelNumber channel number where to add the data from 0 to {@link HighStockChart#yChannel} size-1
     * @param time time in seconds
     * @param state true or false representing '1' or '0' state
     * @param redraw whether to redraw the chart after adding the data
     * @param shift whether to shift the data (discards first point and add this point to the end)
     */
    public void addLogicData(int channelNumber, double time, boolean state, boolean redraw, boolean shift){
        if(state) {
            addData(time, yChannel[channelNumber] + bitScale, channelNumber, redraw, shift);
        }else{
            addData(time, yChannel[channelNumber], channelNumber, redraw, shift);
        }
    }

    /**
     * Add data to the chart
     * @param x x coordinate
     * @param y y coordinate
     * @param index index of the series where to add the data
     * @param redraw whether to redraw the chart after adding the data
     * @param shift whether to shift the data (discards first point and add this point to the end)
     */
    public void addData(double x, double y, int index, boolean redraw, boolean shift){
        webEngine.executeScript("addData(" + x + "," + y + "," + index + "," + redraw + "," + shift + ")");
    }

    public int getDefaultAnnotationFontSize() {
        return defaultAnnotationFontSize;
    }

    public void setDefaultAnnotationFontSize(int defaultAnnotationFontSize) {
        this.defaultAnnotationFontSize = defaultAnnotationFontSize;
    }

    /**
     * Add an annotation in the chart with a rectangle surrounding the text
     * @param text annotation text
     * @param x x position of the top left corner
     * @param y y position of the top left corner
     * @param width annotation width
     * @param height annotation height
     * @param fontSize annotation font size
     * @param dragX whether to allow dragging on x axis or not
     * @param dragY whether to allow dragging on y axis or not
     */
    public void addRectangleAnnotation(final String text, final double x, final double y, final double width, final double height,
                                       int fontSize, boolean dragX, boolean dragY, String textColor){

        double displayWidth = Math.abs(xAxisToPixel(width) - xAxisToPixel(0));
        double displayHeight = Math.abs(yAxisToPixel(height) - yAxisToPixel(0));

        webEngine.executeScript("addAnnotation({\n" +
                                            "title: {\n" +
                                                "text: '" + text + "',\n" +
                                                "x: 0,\n" +
                                                "y: -5,\n" +
                                                "style: {\n" +
                                                    "fontSize: " + fontSize + ",\n" +
                                                    "color: '" + textColor + "',\n" +
                                                "}\n" +
                                            "},\n" +
                                            "anchorX: \"left\",\n" +
                                            "anchorY: \"top\",\n" +
                                            "allowDragY: " + dragY + ",\n" +
                                            "allowDragX: " + dragX + ",\n" +
                                            "xValue: " + x + ",\n" +
                                            "yValue: " + y + ",\n" +
                                            "shape: {\n" +
                                                "units: 'pixels',\n" +
                                                "type: 'rect',\n" +
                                                "params: {\n" +
                                                    "fill: '#5E5E5E',\n" +
                                                    "x: 0,\n" +
                                                    "y: 0,\n" +
                                                    "width: " + displayWidth + ",\n" +
                                                    "height: " + displayHeight + ",\n" +
                                                "}\n" +
                                            "}\n" +
                                        "})");

        AnnotationReference annotationReference = new AnnotationReference(x, width, y, height, text);
        annotationsList.add(annotationReference);
    }

    /**
     * Add an annotation in the chart with a rectangle surrounding the text using default annotation font size
     * @param text annotation text
     * @param x x position of the top left corner
     * @param y y position of the top left corner
     * @param width annotation width
     * @param height annotation height
     */
    public void addRectangleAnnotation(final String text, final double x, final double y, final double width, final double height,
                                       boolean dragX, boolean dragY){
        addRectangleAnnotation(text, x, y, width, height, defaultAnnotationFontSize, dragX, dragY, defaultAnnotationTextColor);
    }

    /**
     * Add a logic annotation
     * @param text annotation text
     * @param x1 annotation start x coordinate
     * @param x2 annotation end x coordinate
     * @param channelNumber channel number where to add the data from 0 to {@link HighStockChart#yChannel} size-1
     * @param forceCalculation if true annotation size will be calculated automatically in this moment. If false the size will be calculated
     *                         the next time {@link HighStockChart#updateAnnotations()} is called or zoom in the chart is changed.
     */
    public void addLogicAnnotation(String text, double x1, double x2, int channelNumber, boolean forceCalculation){
        int fontSize = defaultAnnotationFontSize;

        if(forceCalculation) {
            fontSize = calculateFontSize(text, bitScale);
            text = calculateString(x1, x2, text, fontSize);
        }

        addRectangleAnnotation(text, x1, yChannel[channelNumber]+2.5*bitScale,
                                x2-x1, bitScale, fontSize, false, false, defaultAnnotationTextColor);
    }

    private int calculateFontSize(String text, double height){
        double desiredHeight = Math.abs(yAxisToPixel(height) - yAxisToPixel(0));

        // Calculate font size to fit the desired height
        double sampleHeight = computeFontSize(text, 20)[1];
        int fontSize = (int) Math.floor((desiredHeight * 20) / sampleHeight);
        fontSize = (int) Math.floor(fontSize - (fontSize * 0.1));

        return fontSize;
    }

    private String calculateString(double x1, double x2, String text, int fontSize){
        // Don't display the string if the Sting width is greater than the annotation width
        double desiredWidth = Math.abs(xAxisToPixel(x2) - xAxisToPixel(x1));
        double wordWidth = computeFontSize(text, fontSize)[0];

        if(wordWidth > desiredWidth) return "";
        else return text;
    }

    /**
     * Adds a HighStock flag
     * @param title flag title's
     * @param x flag x position
     * @param seriesIndex series where to add the flag
     */
    public void addFlag(String title, double x, int seriesIndex){
        seriesIndex = 8 + seriesIndex;
        webEngine.executeScript("addFlag('" + title + "'," + x + "," + seriesIndex + ")");
    }

    public ArrayList<Double> getXData(int seriesIndex){
        ArrayList<Double> list = new ArrayList<>();

        JSObject jsObject = (JSObject) webEngine.executeScript("getXData(" + seriesIndex + ")");
        try{
            for(int n = 0; true; ++n){
                Double value = (Double) jsObject.getSlot(n);
                list.add(value);
            }
        }catch (ClassCastException e){
            return list;
        }
    }

    private Double[] numericStringParse(String str){
        int startIndex = str.indexOf("->") + 2;
        ArrayList<Double> list = new ArrayList<>();

        while(startIndex < str.length()){
            int endIndex = str.indexOf(',', startIndex);
            if(endIndex == -1) endIndex = str.length();

            String substring = str.substring(startIndex, endIndex);

            list.add(Double.parseDouble(substring));

            startIndex = endIndex + 1;
        }
        return list.toArray(new Double[list.size()]);
    }

    /**
     * Forces the update of all the annotations in the chart with the current chart zoom and size. This is usually called after
     *  calling {@link #addLogicAnnotation(String, double, double, int, boolean)} with forceCalculation to false
     */
    public void updateAnnotations(){
        for(int n = 0; n < annotationsList.size(); ++n){
            final AnnotationReference annotation = annotationsList.get(n);
            String color = defaultAnnotationTextColor;

            int fontSize = calculateFontSize(annotation.getText(), bitScale);
            // Text doesn't fit on the annotation width, make it transparent
            if("".equals(calculateString(annotation.getX1(), annotation.getX2(), annotation.getText(), fontSize))){
                color = "transparent";
            }

            double displayWidth = Math.abs(xAxisToPixel(annotation.getWidth()) - xAxisToPixel(0));
            double displayHeight = Math.abs(yAxisToPixel(annotation.getHeight()) - yAxisToPixel(0));

            //double textCenter = computeFontSize(annotation.getText(), fontSize)[0] / 2;
            //double x1 = xAxisToPixel(annotation.getX1());
            //double textPosition = textCenter + x1;
            double textPosition = 0;

            webEngine.executeScript("updateAnnotation(" + displayWidth + "," + displayHeight + ",'" +
                    annotation.getText() + "'," + fontSize + "," + n + ",'" + color + "'," + textPosition + ")");
        }
    }

    /**
     * Compute text size for "Unica One, sans-serif" font
     * @param text text to measure
     * @param fontSize font size
     * @return array containing [0] text width and [1] text height
     */
    private double[] computeFontSize(String text, int fontSize){
        String jsResult = webEngine.executeScript("getTextDimension('" + text + "'," + fontSize + ")").toString();
        String[] dimensions = jsResult.split(",");

        return new double[] {Double.parseDouble(dimensions[0]), Double.parseDouble(dimensions[1])};
    }

    public double xAxisToPixel(double x){
        String result = webEngine.executeScript("getChart().xAxis[0].toPixels(" + x + ")").toString();
        return Double.parseDouble(result);
    }

    public double yAxisToPixel(double y){
        String result = webEngine.executeScript("getChart().yAxis[0].toPixels(" + y + ")").toString();
        return Double.parseDouble(result);
    }

    /**
     * Get the extreme values of the x axis.
     * @return array containing [0] minimum and [1] maximum x axis value
     */
    public double[] getXExtremes(){
        String jResult = webEngine.executeScript("getXExtremes()").toString();
        String[] extremes = jResult.split(",");

        return new double[] {Double.parseDouble(extremes[0]), Double.parseDouble(extremes[1])};
    }

    /**
     * Get the extreme values of the y axis.
     * @return array containing [0] minimum and [1] maximum y axis value
     */
    public double[] getYExtremes(){
        String jResult = webEngine.executeScript("getYExtremes()").toString();
        String[] extremes = jResult.split(",");

        return new double[] {Double.parseDouble(extremes[0]), Double.parseDouble(extremes[1])};
    }

    public void setTitle(String title, String subtitle){
        webEngine.executeScript("setTitle(\"" + title + "\",\"" + subtitle + "\")");
    }

    public void redraw(){
        webEngine.executeScript("redrawChart()");
    }

    public WebView getWebView() {
        return webView;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }
}
