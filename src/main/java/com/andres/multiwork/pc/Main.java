package com.andres.multiwork.pc;

import com.andres.multiwork.pc.connection.MultiConnectionManager;
import com.andres.multiwork.pc.screens.*;
import com.andres.multiwork.pc.utils.MultiWorkScreen;
import com.andres.multiwork.pc.utils.BuildProcedure;
import com.andres.multiwork.pc.utils.ScreenManager;
import com.andres.multiwork.pc.utils.SideBar;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    private SideBar sideBar;

    @Override
    public void start(final Stage primaryStage) throws Exception{
        // Start screen maximized
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);

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

        // Language strings
        GlobalValues.resourceBundle = ResourceBundle.getBundle("language", new Locale("en"));

        // Create connection manager and connect by bluetooth
        GlobalValues.multiConnectionManager = new MultiConnectionManager();
        //GlobalValues.connectionManager.connectByBluetooth();

        // Generate main scene
        BorderPane borderPane = new BorderPane();
        sideBar = new SideBar(250, new Pane());
        borderPane.setLeft(sideBar);
        GlobalValues.screenManager = new ScreenManager( borderPane,
                                                        new Scene(borderPane, GlobalValues.screenWidth, GlobalValues.screenHeight));
        primaryStage.setScene(GlobalValues.screenManager.getMainScene());

        // Screen Manager
        GlobalValues.screenManager.addScreen("SettingsScreen", new BuildProcedure() {
            @Override
            public MultiWorkScreen build() {
                Stage stage = new Stage();
                stage.setResizable(false);
                stage.setWidth(800);
                stage.setHeight(600);

                return new SettingsScreen(stage, 800, 600);
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
        GlobalValues.screenManager.addScreen("RawDataScreen", new BuildProcedure() {
            @Override
            public MultiWorkScreen build() {
                Stage stage = new Stage();
                stage.setResizable(false);

                return new RawDataScreen(stage, 350, 510);
            }

            @Override
            public void show(Stage stage, MultiWorkScreen multiWorkScreen, Scene scene) {
                multiWorkScreen.show();
            }
        });
        GlobalValues.screenManager.addScreen("ExportScreen", new BuildProcedure() {
            @Override
            public MultiWorkScreen build() {
                Stage stage = new Stage();
                stage.setResizable(false);

                return new ExportScreen(stage, 350, 510);
            }

            @Override
            public void show(Stage stage, MultiWorkScreen multiWorkScreen, Scene scene) {
                multiWorkScreen.show();
            }
        });
        GlobalValues.screenManager.addScreen("ImportScreen", new BuildProcedure() {
            @Override
            public MultiWorkScreen build() {
                Stage stage = new Stage();
                stage.setResizable(false);

                return new ImportScreen(stage, 350, 510);
            }

            @Override
            public void show(Stage stage, MultiWorkScreen multiWorkScreen, Scene scene) {
                multiWorkScreen.show();
            }
        });

        GlobalValues.screenManager.build("ExportScreen");
        GlobalValues.screenManager.build("ImportScreen");
        GlobalValues.screenManager.build("SettingsScreen");
        GlobalValues.screenManager.buildAndShowScreen("ChartScreen");
        GlobalValues.screenManager.build("RawDataScreen");

        // Close JavaFX application. Otherwise the UI is hidden but the process is still running in background
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Exiting app!");
            GlobalValues.multiConnectionManager.exitMode();
            com.sun.javafx.application.PlatformImpl.exit();
        });
    }

    public static void main(String[] args) throws FileNotFoundException {
        if(args.length > 0) {
            // Log system output to file instead of console
            if (args[0].equals("log")) {
                System.setOut(new PrintStream(new FileOutputStream("log.txt", true)));
                System.setErr(new PrintStream(new FileOutputStream("log.txt", true)));
                System.out.println("------------------------------------------");
            }
        }

        launch(args);
    }
}
