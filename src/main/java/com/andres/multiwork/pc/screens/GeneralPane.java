package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.SettingsPane;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Pane;

import java.io.IOException;

@SuppressWarnings("unchecked")
public class GeneralPane extends SettingsPane{

    private ChoiceBox<Integer> sampleRateChoice;
    private CheckBox simpleTriggerGeneral;
    private CheckBox debugModeCheck;

    private ChangeListener<Integer> sampleRateChangeListener;
    private ChangeListener<Boolean> simpleTriggerGeneralListener;
    private ChangeListener<Boolean> debugModeListener;

    private FXMLLoader fxmlLoader;

    public GeneralPane(int id, FXMLLoader fxmlLoader) {
        super(id);
        this.fxmlLoader = fxmlLoader;

        try {
            setPane(FXMLLoader.load(SettingsScreen.class.getResource("/LogicAnalyzerSettings/PaneGeneral.fxml")));

            sampleRateChoice = (ChoiceBox<Integer>)getPane().lookup("#sampleRateChoice");
            simpleTriggerGeneral = (CheckBox) getPane().lookup("#simpleTriggerGeneral");
            debugModeCheck = (CheckBox) getPane().lookup("#debugModeCheck");

            // Sample Rate
            ObservableList<Integer> sampleRateList = FXCollections.observableArrayList();
            sampleRateList.add(40000000);
            sampleRateList.add(20000000);
            sampleRateList.add(10000000);
            sampleRateList.add(4000000);
            sampleRateList.add(400000);
            sampleRateList.add(2000);
            sampleRateList.add(10);
            sampleRateChoice.setItems(sampleRateList);

            sampleRateChangeListener = (observableValue, integer, newValue) ->
                GlobalValues.xmlSettings.setProperty("sampleRate", newValue);

            simpleTriggerGeneralListener = (observable, oldValue, newValue) ->
                GlobalValues.xmlSettings.setProperty("simpleTriggerGeneral", newValue);

            debugModeListener = (observable, oldValue, newValue) ->
                GlobalValues.xmlSettings.setProperty("debugMode", newValue);

        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void enable() {
        int sampleRate = GlobalValues.xmlSettings.getInt("sampleRate", 4000000);
        boolean simpleTrigger = GlobalValues.xmlSettings.getBoolean("simpleTriggerGeneral", false);
        boolean debugMode = GlobalValues.xmlSettings.getBoolean("debugMode", false);

        sampleRateChoice.setValue(sampleRate);
        simpleTriggerGeneral.setSelected(simpleTrigger);
        debugModeCheck.setSelected(debugMode);

        sampleRateChoice.getSelectionModel().selectedItemProperty().addListener(sampleRateChangeListener);
        simpleTriggerGeneral.selectedProperty().addListener(simpleTriggerGeneralListener);
        debugModeCheck.selectedProperty().addListener(debugModeListener);

        ((Pane)fxmlLoader.getNamespace().get("generalPane")).setDisable(true);
    }

    @Override
    public void disable() {
        sampleRateChoice.getSelectionModel().selectedItemProperty().removeListener(sampleRateChangeListener);
        simpleTriggerGeneral.selectedProperty().removeListener(simpleTriggerGeneralListener);
        debugModeCheck.selectedProperty().removeListener(debugModeListener);

        ((Pane)fxmlLoader.getNamespace().get("generalPane")).setDisable(false);
    }
}
