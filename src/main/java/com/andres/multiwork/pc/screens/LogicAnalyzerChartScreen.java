package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.charts.LogicAdvancedChart;
import com.andres.multiwork.pc.connection.OnNewDataReceived;
import com.andres.multiwork.pc.utils.Decoder;
import com.andres.multiwork.pc.GlobalValues;
import com.protocolanalyzer.api.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.InputStream;
import java.io.OutputStream;

public class LogicAnalyzerChartScreen extends MultiWorkScreen {

    private LogicAdvancedChart mainChart;
    private MenuBar menuBar;

    final KeyCombination zoomInCombination = new KeyCodeCombination(KeyCode.UP);
    final KeyCombination zoomOutCombination = new KeyCodeCombination(KeyCode.DOWN);
    final KeyCombination fullScreenCombination = new KeyCodeCombination(KeyCode.F11);
    final KeyCombination analyzeCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);

    private final double yChannel[] = {1, 8, 15, 22, 29, 36, 43, 50};
    private final double bitScale = 1.5;
    private Decoder decoder = new Decoder(GlobalValues.xmlSettings);

    private OnNewDataReceived onNewDataReceived;

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
    }

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
                data = LogicHelper.bitParser("100 11010010011100101 0 11010011110000111 0 11010011110000111 1 0011", 5, 2);
                clk =  LogicHelper.bitParser("110 01010101010101010 1 01010101010101010 1 01010101010101010 1 0111", 5, 2);

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
        getStage().setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                GlobalValues.connectionManager.removeDataReceivedListener(onNewDataReceived);
            }
        });
    }

    /**
     * Updates chart with the new decoded data from {@link com.andres.multiwork.pc.utils.Decoder}
     */
    private void updateChart(){
        final int samplesNumber = decoder.getMaxSamplesNumber();
        final double initTime = 0;
        double time = initTime;

        // Draw waveforms in the chart. We only put a point every time signal changes state. On this way we avoid
        //  adding a large amount of data in the chart slowing down rendering without loosing visual quality of
        //  the signal.
        for(int channel = 0; channel < GlobalValues.channelsNumber; ++channel){
            final LogicBitSet bitsData = decoder.getRawData(channel);

            boolean bitState = false;
            for(int n = 0; n < samplesNumber; ++n){
                // Initial point
                if(n == 0){
                    bitState = bitsData.get(0);

                    if(bitsData.get(n))mainChart.addData(channel, time, yChannel[channel]+bitScale);
                    else mainChart.addData(channel, time, yChannel[channel]);

                // Found an state change, add this point
                }else if(bitsData.get(n) != bitState){
                    bitState = bitsData.get(n);

                    double tTime = time - 1.0d/decoder.getSampleFrequency();

                    // Previous state
                    if(bitsData.get(n-1)) mainChart.addData(channel, tTime, yChannel[channel] + bitScale);
                    else mainChart.addData(channel, tTime, yChannel[channel]);

                    // Current state
                    if(bitsData.get(n)) mainChart.addData(channel, time, yChannel[channel] + bitScale);
                    else mainChart.addData(channel, time, yChannel[channel]);

                // Add a the final point, doesn't care if the state didn't change
                }else if(n == (samplesNumber-1)){
                    if(bitsData.get(n)) mainChart.addData(channel, time, yChannel[channel] + bitScale);
                    else mainChart.addData(channel, time, yChannel[channel]);
                }
                // Increment time
                time += 1.0d/decoder.getSampleFrequency();
            }

            // Decoded data annotations
            for (int n = 0; n < decoder.getDecodedData(channel).size(); ++n) {
                final TimePosition decodedData = decoder.getDecodedData(channel).get(n);
                mainChart.addAnnotation(decodedData.getString(), decodedData.startTime(), decodedData.endTime(), yChannel[channel] + 3*bitScale, 2);
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
