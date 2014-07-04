package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.SettingsPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;

import java.io.IOException;

public class GeneralPane extends SettingsPane{

    private ChoiceBox<Integer> sampleRateChoice;
    private CheckBox simpleTriggerGeneral;

    private ChangeListener<Integer> sampleRateChangeListener;
    private ChangeListener<Boolean> simpleTriggerGeneralListener;

    public GeneralPane(int id) {
        super(id);

        try {
            setPane(FXMLLoader.load(SettingsScreen.class.getResource("/LogicAnalyzerSettings/PaneGeneral.fxml")));

            sampleRateChoice = (ChoiceBox<Integer>)getPane().lookup("#sampleRateChoice");
            simpleTriggerGeneral = (CheckBox) getPane().lookup("#simpleTriggerGeneral");

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

            sampleRateChangeListener = (ChangeListener<Integer>) (observableValue, integer, newValue) -> {
                GlobalValues.xmlSettings.setProperty("sampleRate", newValue);
            };

            simpleTriggerGeneralListener = (ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                GlobalValues.xmlSettings.setProperty("simpleTriggerGeneral", newValue);
            };

        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void enable() {
        int sampleRate = GlobalValues.xmlSettings.getInt("sampleRate", 4000000);
        boolean simpleTrigger = GlobalValues.xmlSettings.getBoolean("simpleTriggerGeneral", false);

        sampleRateChoice.setValue(sampleRate);
        simpleTriggerGeneral.setSelected(simpleTrigger);

        sampleRateChoice.getSelectionModel().selectedItemProperty().addListener(sampleRateChangeListener);
        simpleTriggerGeneral.selectedProperty().addListener(simpleTriggerGeneralListener);
    }

    @Override
    public void disable() {
        sampleRateChoice.getSelectionModel().selectedItemProperty().removeListener(sampleRateChangeListener);
        simpleTriggerGeneral.selectedProperty().removeListener(simpleTriggerGeneralListener);
    }
}
