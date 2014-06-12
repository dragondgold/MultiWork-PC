package com.andres.multiwork.pc.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class MultiWorkScreen {

    private Stage stage;
    private Scene scene;

    public MultiWorkScreen(Stage stage){
        this.stage = stage;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    abstract public void show();

}
