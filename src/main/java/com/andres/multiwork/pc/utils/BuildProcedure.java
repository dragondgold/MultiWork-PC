package com.andres.multiwork.pc.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;

public interface BuildProcedure {

    public MultiWorkScreen build();

    public void show(final Stage stage, final MultiWorkScreen multiWorkScreen, final Scene scene);

}
