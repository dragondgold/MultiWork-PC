package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.charts.LogicAdvancedChart;
import com.andres.multiwork.pc.connection.OnNewDataReceived;
import com.andres.multiwork.pc.utils.Decoder;
import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.MultiWorkScreen;
import com.protocolanalyzer.api.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.Glow;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.io.OutputStream;

public class LogicAnalyzerChartScreen extends MultiWorkScreen {

    private final LogicAdvancedChart mainChart;
    private final MenuBar menuBar;

    private final KeyCombination zoomInCombination = new KeyCodeCombination(KeyCode.UP);
    private final KeyCombination zoomOutCombination = new KeyCodeCombination(KeyCode.DOWN);
    private final KeyCombination fullScreenCombination = new KeyCodeCombination(KeyCode.F11);
    private final KeyCombination analyzeCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);

    private final Decoder decoder = Decoder.getDecoder().setSettings(GlobalValues.xmlSettings);

    /** Series click listener */
    private OnNewDataReceived onNewDataReceived;

    /** Series context menu on click */
    private final ContextMenu seriesContextMenu = new ContextMenu();
    private int currentSelectedSeries = -1;

    public LogicAnalyzerChartScreen(final Stage stage, final int width, final int height){
        super(stage);

        Pane pane = new Pane();
        menuBar = new MenuBar();
        pane.getChildren().addAll(menuBar);

        mainChart = new LogicAdvancedChart(pane);
        mainChart.getMainChart().setTitle(GlobalValues.resourceBundle.getString("chartTitle"));
        mainChart.getMainChart().getXAxis().setLabel(GlobalValues.resourceBundle.getString("chartXAxis"));
        mainChart.getMainChart().getYAxis().setLabel(GlobalValues.resourceBundle.getString("chartYAxis"));
        mainChart.getSmallChart().getYAxis().setLabel(GlobalValues.resourceBundle.getString("chartYAxis"));

        setScene(new Scene(pane, width, height));
        buildMenu();
        buildSeriesContextMenu();

        // Series event listener
        mainChart.setSeriesEventListener((series, mouseEvent) -> {
            if("MOUSE_ENTERED".equals(mouseEvent.getEventType().getName())) {
                series.getNode().setEffect(new Glow(2.5));
            }
            if("MOUSE_EXITED".equals(mouseEvent.getEventType().getName())) {
                series.getNode().setEffect(null);
            }
            if("MOUSE_CLICKED".equals(mouseEvent.getEventType().getName())) {
                if(mouseEvent.getButton() == MouseButton.SECONDARY){
                    // TODO: action based on click
                    System.out.println("Clicked series: " + series.getName());

                    currentSelectedSeries = Character.getNumericValue(series.getName().charAt(series.getName().length()-1));
                    seriesContextMenu.show(getStage(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            }
        });
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
        menuItemSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                GlobalValues.screenManager.show("SettingsScreen");
            }
        });

        menuItemAnalyzer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //GlobalValues.connectionManager.getLogicAnalyzerManager().startCapture();

                LogicBitSet data, clk;
                data = LogicHelper.bitParser("100 11010010011100101 0 11010011110000111 0 11010011110000111 1 0011", 5, 3);
                clk =  LogicHelper.bitParser("110 01010101010101010 1 01010101010101010 1 01010101010101010 1 0111", 5, 3);

                byte[] buffer = Decoder.bitSetToBuffer(data, clk);
                decoder.setData(buffer);
                decoder.decodeAll();

                updateChart();
            }
        });

        menuItemFullscreen.setOnAction(actionEvent -> getStage().setFullScreen(true));

        getScene().setOnKeyReleased(keyEvent -> {
            if(zoomInCombination.match(keyEvent)){
                mainChart.zoomIn();
            }else if(zoomOutCombination.match(keyEvent)){
                mainChart.zoomOut();
            }
        });

        onNewDataReceived = new OnNewDataReceived() {
            @Override
            public void onNewDataReceived(byte[] data, InputStream inputStream, OutputStream outputStream, String source) {
                System.out.println("Data received!");

                decoder.setData(data);
                decoder.decodeAll();
                updateChart();
            }
        };
        GlobalValues.connectionManager.addDataReceivedListener(onNewDataReceived);

        // Remove listener when we close the window
        getStage().setOnHiding(event -> {
            GlobalValues.connectionManager.removeDataReceivedListener(onNewDataReceived);
        });
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
            GlobalValues.screenManager.show("ExportScreen");
        });
    }

    /**
     * Updates chart with the new decoded data from {@link com.andres.multiwork.pc.utils.Decoder}
     */
    private void updateChart(){
        final int samplesNumber = decoder.getMaxSamplesNumber();
        final double initTime = 0;
        double time = initTime;

        // Draw waveforms in the chart. We only put seriesContextMenu point every time signal changes state. On this way we avoid
        //  adding seriesContextMenu large amount of data in the chart slowing down rendering without loosing visual quality of
        //  the signal.
        for(int channel = 0; channel < GlobalValues.channelsNumber; ++channel){
            final LogicBitSet bitsData = decoder.getRawData(channel);

            boolean bitState = false;
            for(int n = 0; n < samplesNumber; ++n){
                // Initial point
                if(n == 0){
                    bitState = bitsData.get(0);

                    mainChart.addLogicData(channel, time, bitsData.get(n));

                // Found an state change, add this point
                }else if(bitsData.get(n) != bitState){
                    bitState = bitsData.get(n);

                    double tTime = time - 1.0d/decoder.getSampleFrequency();

                    // Previous state
                    mainChart.addLogicData(channel, tTime, bitsData.get(n-1));

                    // Current state
                    mainChart.addLogicData(channel, time, bitsData.get(n));

                // Add seriesContextMenu the final point, doesn't care if the state didn't change
                }else if(n == (samplesNumber-1)){
                    mainChart.addLogicData(channel, time, bitsData.get(n));
                }
                // Increment time
                time += 1.0d/decoder.getSampleFrequency();
            }

            // Decoded data annotations
            for (int n = 0; n < decoder.getDecodedData(channel).size(); ++n) {
                final TimePosition decodedData = decoder.getDecodedData(channel).get(n);
                mainChart.addLogicAnnotation(decodedData.getString(), decodedData.startTime(), decodedData.endTime(), channel);
            }

            if(channel < GlobalValues.channelsNumber-1) time = initTime;
        }
        mainChart.updateChart();
    }

    @Override
    public void show(){
        getStage().setScene(getScene());
        getStage().show();

        // Chart
        mainChart.setY(menuBar.getHeight());
        mainChart.setHeight(getScene().getHeight() - menuBar.getHeight());
        mainChart.setWidth(getScene().getWidth());
    }
}
