package com.andres.multiwork.pc.utils;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.HashMap;

public class ScreenManager {

    private final HashMap<String, BuildProcedure> buildProcedureMap = new HashMap<>();
    private final HashMap<String, MultiWorkScreen> screenMap = new HashMap<>();

    private final BorderPane mainPane;
    private final Scene mainScene;

    /**
     * ScreenManager constructor
     * @param mainPane is the main pane where everything is displayed in the main screen. The left
     *                 side is used by a side bar and every screen displayed in the main stage should
     *                 put the content on the center on this {@link javafx.scene.layout.BorderPane}
     * @param mainScene is the main {@link javafx.scene.Scene} where the mainPane is displayed
     */
    public ScreenManager(BorderPane mainPane, Scene mainScene){
        this.mainPane = mainPane;
        this.mainScene = mainScene;
    }

    /**
     * Set the pane to be showed as the main pane in the window
     * @param mainPane pane to show
     */
    public void setMainPane(Pane mainPane){
        this.mainPane.setCenter(mainPane);
    }

    public Scene getMainScene() {
        return mainScene;
    }

    public void show(String tag){
        if(screenMap.containsKey(tag)){
            MultiWorkScreen screen = screenMap.get(tag);

            buildProcedureMap.get(tag).show(screen.getStage(), screen, screen.getScene());
        }
    }

    public MultiWorkScreen build(String tag){
        MultiWorkScreen screen = buildProcedureMap.get(tag).build();
        screenMap.put(tag, screen);

        return screen;
    }

    public MultiWorkScreen buildAndShowScreen(String tag){
        MultiWorkScreen multiWorkScreen = build(tag);
        show(tag);

        return multiWorkScreen;
    }

    public void addScreen(String tag, BuildProcedure buildProcedure){
        buildProcedureMap.put(tag, buildProcedure);
    }

    public MultiWorkScreen getScreen(String tag){
        return screenMap.get(tag);
    }

}
