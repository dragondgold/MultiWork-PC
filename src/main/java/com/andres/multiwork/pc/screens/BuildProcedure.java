package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.screens.MultiWorkScreen;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface BuildProcedure {

    public MultiWorkScreen build();

    public void show(final Stage stage, final MultiWorkScreen multiWorkScreen, final Scene scene);

}
