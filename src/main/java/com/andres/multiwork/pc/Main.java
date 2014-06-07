package com.andres.multiwork.pc;

import com.andres.multiwork.pc.connection.ConnectionManager;
import com.andres.multiwork.pc.connection.OnNewDataReceived;
import com.andres.multiwork.pc.screens.LogicAnalyzerChartScreen;
import com.andres.multiwork.pc.screens.MultiWorkScreen;
import com.andres.multiwork.pc.screens.SettingsScreen;
import com.andres.multiwork.pc.screens.BuildProcedure;
import com.andres.multiwork.pc.screens.ScreenManager;
import com.protocolanalyzer.api.utils.Configuration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception{
        // Start screen maximized
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setResizable(false);

        GlobalValues.screenWidth = (int) bounds.getWidth();
        GlobalValues.screenHeight = (int) bounds.getHeight();

        // Configuration file
        GlobalValues.xmlSettings = new XMLConfiguration();
        GlobalValues.xmlSettings.setFile(new File("settings.xml"));

        if(!new File("settings.xml").exists()){
            GlobalValues.xmlSettings.save();
        }

        GlobalValues.xmlSettings.load();
        GlobalValues.xmlSettings.setAutoSave(true);

        // Channels settings
        GlobalValues.channelsSettings = new Configuration();

        // Language strings
        GlobalValues.resourceBundle = ResourceBundle.getBundle("language", new Locale("en"));

        // Finish app when exiting, otherwise UI disappear but the programs keeps running
        primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
            }
        });

        // Create connection manager and connect by bluetooth
        GlobalValues.connectionManager = new ConnectionManager();
        //GlobalValues.connectionManager.connectByBluetooth();

        // Screen Manager
        GlobalValues.screenManager = new ScreenManager();
        GlobalValues.screenManager.addScreen("SettingsScreen", new BuildProcedure() {
            @Override
            public MultiWorkScreen build() {
                Stage stage = new Stage();
                stage.setResizable(false);
                stage.setWidth(800);
                stage.setHeight(600);

                return new SettingsScreen(stage, GlobalValues.screenWidth, GlobalValues.screenHeight);
            }

            @Override
            public void show(Stage stage, final MultiWorkScreen multiWorkScreen, final Scene scene) {
                multiWorkScreen.show();
            }
        });
        GlobalValues.screenManager.addScreen("ChartScreen", new BuildProcedure() {
            @Override
            public MultiWorkScreen build() {
                return new LogicAnalyzerChartScreen(primaryStage, GlobalValues.screenWidth, GlobalValues.screenHeight);
            }

            @Override
            public void show(Stage stage, final MultiWorkScreen multiWorkScreen, final Scene scene) {
                multiWorkScreen.show();
            }
        });

        GlobalValues.screenManager.build("SettingsScreen");
        GlobalValues.screenManager.buildAndShowScreen("ChartScreen");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
