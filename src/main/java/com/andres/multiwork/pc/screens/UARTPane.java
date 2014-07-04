package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.SettingsPane;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.IOException;

public class UARTPane extends SettingsPane {

    private ChoiceBox<String> choiceParity;
    private TextField baudRateText;
    private CheckBox nineBitsModeCheck;
    private CheckBox twoStopBitsCheck;

    private ChangeListener<String> parityChangeListener;
    private ChangeListener<String> baudChangeListener;
    private ChangeListener<Boolean> nineBitsChangeListener;
    private ChangeListener<Boolean> twoStopBitsChangeListener;

    public UARTPane(int id) {
        super(id);

        try {
            setPane(FXMLLoader.load(SettingsScreen.class.getResource("/LogicAnalyzerSettings/PaneUART.fxml")));

            baudRateText = (TextField)getPane().lookup("#baudRateText");
            nineBitsModeCheck = (CheckBox)getPane().lookup("#nineBitsModeCheck");
            twoStopBitsCheck = (CheckBox)getPane().lookup("#twoStopBitsCheck");
            choiceParity = (ChoiceBox<String>)getPane().lookup("#choiceParity");

            // Parity selector
            ObservableList<String> parityList = FXCollections.observableArrayList();
            parityList.add(GlobalValues.resourceBundle.getString("parityEven"));
            parityList.add(GlobalValues.resourceBundle.getString("parityOdd"));
            parityList.add(GlobalValues.resourceBundle.getString("parityNone"));
            choiceParity.setItems(parityList);

            parityChangeListener = (observableValue, s, newValue) -> {
                String even = GlobalValues.resourceBundle.getString("parityEven");
                String odd = GlobalValues.resourceBundle.getString("parityOdd");
                String none = GlobalValues.resourceBundle.getString("parityNone");

                if (newValue.equals(even))
                    GlobalValues.xmlSettings.setProperty("parity" + getChannel(), GlobalValues.parityEven);
                else if (newValue.equals(odd))
                    GlobalValues.xmlSettings.setProperty("parity" + getChannel(), GlobalValues.parityOdd);
                else if (newValue.equals(none))
                    GlobalValues.xmlSettings.setProperty("parity" + getChannel(), GlobalValues.parityNone);

            };

            baudChangeListener = (observableValue, s, newValue) -> {
                GlobalValues.xmlSettings.setProperty("baudRate" + getChannel(), Math.abs(Integer.valueOf(newValue)));
            };

            nineBitsChangeListener = (observableValue, aBoolean, newValue) -> {
                GlobalValues.xmlSettings.setProperty("nineBitsMode" + getChannel(), newValue);
            };

            twoStopBitsChangeListener = (observableValue, aBoolean, newValue) -> {
                GlobalValues.xmlSettings.setProperty("twoStopBits" + getChannel(), newValue);
            };

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enable() {
        int parity = GlobalValues.xmlSettings.getInt("parity" + getChannel(), GlobalValues.parityNone);
        int baudRate = GlobalValues.xmlSettings.getInt("baudRate" + getChannel(), 9600);

        boolean nineBitsMode = GlobalValues.xmlSettings.getBoolean("nineBitsMode" + getChannel(), false);
        boolean twoStopBits = GlobalValues.xmlSettings.getBoolean("twoStopBits" + getChannel(), false);

        if(parity == GlobalValues.parityEven) choiceParity.setValue(GlobalValues.resourceBundle.getString("parityEven"));
        else if(parity == GlobalValues.parityOdd) choiceParity.setValue(GlobalValues.resourceBundle.getString("parityOdd"));
        else if(parity == GlobalValues.parityNone) choiceParity.setValue(GlobalValues.resourceBundle.getString("parityNone"));

        baudRateText.setText(Integer.toString(baudRate));
        nineBitsModeCheck.setSelected(nineBitsMode);
        twoStopBitsCheck.setSelected(twoStopBits);

        choiceParity.getSelectionModel().selectedItemProperty().addListener(parityChangeListener);
        baudRateText.textProperty().addListener(baudChangeListener);
        nineBitsModeCheck.selectedProperty().addListener(nineBitsChangeListener);
        twoStopBitsCheck.selectedProperty().addListener(twoStopBitsChangeListener);
    }

    @Override
    public void disable() {
        choiceParity.getSelectionModel().selectedItemProperty().removeListener(parityChangeListener);
        baudRateText.textProperty().removeListener(baudChangeListener);
        nineBitsModeCheck.selectedProperty().removeListener(nineBitsChangeListener);
        twoStopBitsCheck.selectedProperty().removeListener(twoStopBitsChangeListener);
    }
}
