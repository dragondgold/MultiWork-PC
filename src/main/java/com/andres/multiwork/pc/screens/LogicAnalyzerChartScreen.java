package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.connection.OnNewDataReceived;
import com.andres.multiwork.pc.highstocks.AnnotationEvent;
import com.andres.multiwork.pc.highstocks.HighStockChart;
import com.andres.multiwork.pc.highstocks.SeriesLegendShiftClick;
import com.andres.multiwork.pc.utils.Decoder;
import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.MultiWorkScreen;
import com.protocolanalyzer.api.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LogicAnalyzerChartScreen extends MultiWorkScreen {

    private final HighStockChart mainChart;
    private final MenuBar menuBar;

    private final KeyCombination fullScreenCombination = new KeyCodeCombination(KeyCode.F11);
    private final KeyCombination analyzeCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.SHIFT_DOWN);

    private final Decoder decoder = Decoder.getDecoder().setSettings(GlobalValues.xmlSettings);

    /** Series click listener */
    private OnNewDataReceived onNewDataReceived;

    /** Series context menu on click */
    private final ContextMenu seriesContextMenu = new ContextMenu();
    private int currentSelectedSeries = -1;

    double y2 = 25, y1 = 0;
    final double yIncrement = 2;

    private final double initTime = 0;
    private final int annotationsPerCycle = 5;

    private Tooltip tooltip = new Tooltip();

    public LogicAnalyzerChartScreen(final Stage stage, final int width, final int height){
        super(stage);

        Pane pane = new Pane();
        menuBar = new MenuBar();
        pane.getChildren().addAll(menuBar);

        mainChart = new HighStockChart(pane);
        mainChart.setOnChartLoaded(() -> {
            mainChart.setTitle(GlobalValues.resourceBundle.getString("chartTitle"), "");
            mainChart.setXAxisLabel(GlobalValues.resourceBundle.getString("chartXAxis") + " [μS]");
            mainChart.setYAxisLabel(GlobalValues.resourceBundle.getString("chartYAxis"));
        });

        setScene(new Scene(pane, width, height));
        buildMenu();
        buildSeriesContextMenu();

        // Series event listener
        mainChart.setSeriesLegendShiftClick(new SeriesLegendShiftClick() {
            @Override
            public void onSeriesLegendShiftClick(int seriesNumber, double x, double y) {
                System.out.println("Clicked series " + seriesNumber);

                currentSelectedSeries = seriesNumber;
                seriesContextMenu.show(getStage(), x, y);
            }
        });
        /* //TODO: fix this
        mainChart.setAnnotationEvent(new AnnotationEvent() {
            @Override
            public void onAnnotationClicked(String title, double mouseX, double mouseY) {
                // Toggle hide and show tooltip when clicking on any annotation
                if(tooltip.isShowing()){
                    tooltip.hide();
                }else{
                    tooltip.show(getStage(), mouseX, mouseY);
                    tooltip.setText("Data: " + title);
                }
                System.out.println("Annotation clicked - " + title);
            }

            @Override
            public void onMouseEnter(String title, double mouseX, double mouseY) {
                System.out.println("Annotation mouse enter: " + title);
                if(!tooltip.isShowing()){
                    tooltip.show(getStage(), mouseX, mouseY);
                    tooltip.setText("Data: " + title);
                    return;
                }
                tooltip.setText("Data: " + title);
                tooltip.setX(mouseX);
                tooltip.setY(mouseY);
            }

            @Override
            public void onMouseOut(String title, double mouseX, double mouseY) {
                System.out.println("Annotation mouse out: " + title);
                tooltip.hide();
            }
        });*/

        // Load font and CSS file to apply it to the current scene
        Font.loadFont(LogicAnalyzerChartScreen.class.getResource("/UnicaOne-Regular.ttf").toExternalForm(), 12);
        getScene().getStylesheets().add(LogicAnalyzerChartScreen.class.getResource("/myStyle.css").toExternalForm());
    }

    /**
     * Build MenuBar menus
     */
    private void buildMenu(){
        menuBar.setPrefWidth(GlobalValues.screenWidth);

        // File Menu
        Menu menuFile = new Menu(GlobalValues.resourceBundle.getString("menuFile"));
        MenuItem menuItemSettings = new MenuItem(GlobalValues.resourceBundle.getString("menuSettings"));

        // Logic analyzer menu
        Menu menuAnalyzer = new Menu(GlobalValues.resourceBundle.getString("logicAnalyzer"));
        MenuItem menuItemAnalyzer = new MenuItem(GlobalValues.resourceBundle.getString("menuAnalyze"));
        menuItemAnalyzer.setAccelerator(analyzeCombination);

        // View menu
        Menu menuView = new Menu(GlobalValues.resourceBundle.getString("menuView"));
        MenuItem menuItemFullscreen = new MenuItem(GlobalValues.resourceBundle.getString("menuFullscreen"));
        menuItemFullscreen.setAccelerator(fullScreenCombination);

        // Add items to Menus
        menuFile.getItems().add(menuItemSettings);
        menuAnalyzer.getItems().add(menuItemAnalyzer);
        menuView.getItems().add(menuItemFullscreen);

        // Add Menu to MenuBar
        menuBar.getMenus().addAll(menuFile, menuAnalyzer, menuView);

        /** Events */
        menuItemSettings.setOnAction(actionEvent -> GlobalValues.screenManager.show("SettingsScreen"));

        menuItemAnalyzer.setOnAction(actionEvent -> {
            //GlobalValues.multiConnectionManager.getManager("Logic Analyzer").startCapture();

            // I2C Sample data
            LogicBitSet data, clk;
            data = LogicHelper.bitParser("100 11010010011100101 0 11010011110000111 0 11010011110000111 1 0011", 5, 40);
            clk =  LogicHelper.bitParser("110 01010101010101010 1 01010101010101010 1 01010101010101010 1 0111", 5, 40);

            byte[] buffer = Decoder.bitSetToBuffer(data, clk);
            decoder.setData(buffer);
            decoder.decodeAll();

            updateChart();
        });

        menuItemFullscreen.setOnAction(actionEvent -> getStage().setFullScreen(true));

        // Remove listener and exit mode when we close the window
        getStage().setOnHiding(event -> {
            GlobalValues.multiConnectionManager.removeDataReceivedListener(onNewDataReceived);
            GlobalValues.multiConnectionManager.exitMode();
        });

        // Add listener when showing window
        getStage().setOnShowing(event -> {
            GlobalValues.multiConnectionManager.addDataReceivedListener(onNewDataReceived);
            GlobalValues.multiConnectionManager.enterMode("Logic Analyzer");
        });

        // New data received!
        onNewDataReceived = (data, inputStream, outputStream, source) -> {
            System.out.println("Data received!");

            decoder.setData(data);
            decoder.decodeAll();
            updateChart();
        };
    }

    /**
     * Build context menu showed when clicking series
     */
    private void buildSeriesContextMenu(){
        MenuItem rawDataItem = new MenuItem(GlobalValues.resourceBundle.getString("rawData"));
        MenuItem exportItem = new MenuItem(GlobalValues.resourceBundle.getString("exportChannel"));
        MenuItem exportAllItem = new MenuItem(GlobalValues.resourceBundle.getString("exportAllChannels"));

        seriesContextMenu.getItems().addAll(rawDataItem, exportItem, exportAllItem);

        rawDataItem.setOnAction(event -> {
            ((RawDataScreen)GlobalValues.screenManager.getScreen("RawDataScreen")).setChannelToShow(currentSelectedSeries);
            GlobalValues.screenManager.show("RawDataScreen");
        });

        exportItem.setOnAction(event -> {
            ((ExportScreen)GlobalValues.screenManager.getScreen("ExportScreen")).setChannelToExport(currentSelectedSeries);
            GlobalValues.screenManager.show("ExportScreen");
        });

        exportAllItem.setOnAction(event -> {
            ((ExportScreen)GlobalValues.screenManager.getScreen("ExportScreen")).setChannelToExport(ExportScreen.EXPORT_ALL_CHANNELS);
            GlobalValues.screenManager.show("ExportScreen");
        });
    }

    /**
     * Updates chart with the new decoded data from {@link com.andres.multiwork.pc.utils.Decoder}
     */
    private void updateChart(){
        mainChart.showLoading("Processing data...");

        // Draw waveforms in the chart. We only put a point every time signal changes state. On this way we avoid
        //  adding to series large amount of data in the chart (slowing down rendering) without loosing visual quality of
        //  the signal.
        for(int channel = 0; channel < GlobalValues.channelsNumber; ++channel){
            final LogicBitSet bitsData = decoder.getRawData(channel);
            final int samplesNumber = decoder.getMaxSamplesNumber();
            double time = initTime;
            System.out.println("Channel " + channel + " contains " + bitsData.size());

            boolean bitState = false;
            for(int n = 0; n < samplesNumber; ++n){
                // Initial point
                if(n == 0){
                    bitState = bitsData.get(0);
                    mainChart.addLogicData(channel, time, bitsData.get(n), false, false);

                    // Found an state change, add this point
                }else if(bitsData.get(n) != bitState){
                    bitState = bitsData.get(n);

                    double tTime = time - 1.0d/decoder.getSampleFrequency();

                    // Previous state
                    mainChart.addLogicData(channel, tTime, bitsData.get(n-1), false, false);

                    // Current state
                    mainChart.addLogicData(channel, time, bitsData.get(n), false, false);

                    // Add seriesContextMenu the final point, doesn't care if the state didn't change
                }else if(n == (samplesNumber-1)){
                    mainChart.addLogicData(channel, time, bitsData.get(n), false, false);
                }

                // Increment time in uS
                time += (1.0d/decoder.getSampleFrequency())*1000000d;
            }
        }

        // Keep track of the current annotation where are in every channel
        final int[] currentAnnotation = new int[GlobalValues.channelsNumber];
        final boolean[] channelReady = new boolean[GlobalValues.channelsNumber];

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                for(int channel = 0; channel < GlobalValues.channelsNumber; ++channel) {
                    final List<TimePosition> dataList = decoder.getDecodedData(channel);

                    int n;
                    // Decoded data annotations
                    for (n = currentAnnotation[channel]; currentAnnotation[channel] < dataList.size() && (n-currentAnnotation[channel]) < annotationsPerCycle;
                            ++currentAnnotation[channel]) {
                        final TimePosition decodedData = decoder.getDecodedData(channel).get(currentAnnotation[channel]);

                        mainChart.addLogicAnnotation(decodedData.getString(), decodedData.startTime() * 1E6, decodedData.endTime() * 1E6, channel, false);
                    }
                    if(!(currentAnnotation[channel] < dataList.size())) channelReady[channel] = true;
                    else channelReady[channel] = false;

                    // All annotations added, redraw chart!
                    if(allChecked(channelReady)) {
                        stop();
                        mainChart.redraw();
                        // Give some time to chart to redraw and then set the extremes
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    mainChart.setXAxisExtremes(0, 300, false);
                                    mainChart.setYAxisExtremes(y1, y2, true);
                                    mainChart.hideLoading();
                                });
                            }
                        }, 50);
                    }
                }
            }
        }.start();
    }

    private boolean allChecked(boolean[] data){
        for(int n = 0; n < data.length; ++n){
            if(!data[n]) return false;
        }
        return true;
    }

    @Override
    public void show(){
        getStage().setScene(getScene());
        getStage().show();

        // Chart
        mainChart.getWebView().setLayoutY(menuBar.getHeight());
        mainChart.getWebView().setLayoutX(0);
        mainChart.getWebView().setPrefHeight(getScene().getHeight() - menuBar.getHeight());
        mainChart.getWebView().setPrefWidth(getScene().getWidth());

        // Move y axis limits when pressing up and down arrows
        getScene().setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.UP){
                if(y2 < 50) {
                    y2 += yIncrement;
                    y1 += yIncrement;
                }
            }
            if(event.getCode() == KeyCode.DOWN){
                if(y1 >= yIncrement) {
                    y2 -= yIncrement;
                    y1 -= yIncrement;
                }
            }
            mainChart.setYAxisExtremes(y1, y2, true);
            mainChart.getWebEngine().executeScript("getChart().redrawAnnotations()");
        });
    }
}
