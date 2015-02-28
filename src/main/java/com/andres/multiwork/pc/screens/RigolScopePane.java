package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.SettingsPane;
import com.andres.multiwork.pc.utils.Utils;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class RigolScopePane extends SettingsPane {

    private FXMLLoader fxmlLoader;

    private CheckBox channel1Check, channel2Check;
    private TextField fromVoltageText, toVoltageText;

    private ChangeListener<Boolean> channel1CheckListener, channel2CheckListener;
    private ChangeListener<Boolean> fromVoltageTextListener, toVoltageTextListener;

    public RigolScopePane(int id, FXMLLoader fxmlLoader) {
        super(id);
        this.fxmlLoader = fxmlLoader;

        try {
            setPane(FXMLLoader.load(getClass().getResource("/LogicAnalyzerSettings/PaneRigol.fxml")));

            channel1Check = (CheckBox) getPane().lookup("#channel1Check");
            channel2Check = (CheckBox) getPane().lookup("#channel2Check");

            fromVoltageText = (TextField) getPane().lookup("#fromVoltageText");
            toVoltageText = (TextField) getPane().lookup("#toVoltageText");

            channel1CheckListener = (observable, oldValue, newValue) -> GlobalValues.xmlSettings.setProperty("rigolCH1Enable", newValue.booleanValue());
            channel2CheckListener = (observable, oldValue, newValue) -> GlobalValues.xmlSettings.setProperty("rigolCH2Enable", newValue.booleanValue());

            fromVoltageTextListener = (observable, oldValue, newValue) -> {
                // Focus lost
                if(!newValue) {
                    double v = Utils.stringToVoltage(fromVoltageText.getText());
                    if (v == Double.MAX_VALUE) fromVoltageText.setText("0");
                    else GlobalValues.xmlSettings.setProperty("rigolFromVoltage", v);
                }
            };

            toVoltageTextListener = (observable, oldValue, newValue) -> {
                // Focus lost
                if(!newValue) {
                    double v = Utils.stringToVoltage(toVoltageText.getText());
                    if (v == Double.MAX_VALUE) toVoltageText.setText("0");
                    else GlobalValues.xmlSettings.setProperty("rigolToVoltage", v);
                }
            };

        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void enable() {
        channel1Check.setSelected(GlobalValues.xmlSettings.getBoolean("rigolCH1Enable"));
        channel2Check.setSelected(GlobalValues.xmlSettings.getBoolean("rigolCH2Enable"));
        fromVoltageText.setText(Utils.voltageToString(GlobalValues.xmlSettings.getDouble("rigolFromVoltage")));
        toVoltageText.setText(Utils.voltageToString(GlobalValues.xmlSettings.getDouble("rigolToVoltage")));

        channel1Check.selectedProperty().addListener(channel1CheckListener);
        channel2Check.selectedProperty().addListener(channel2CheckListener);
        fromVoltageText.focusedProperty().addListener(fromVoltageTextListener);
        toVoltageText.focusedProperty().addListener(toVoltageTextListener);

        ((Pane)fxmlLoader.getNamespace().get("generalPane")).setDisable(true);
    }

    @Override
    public void disable() {
        channel1Check.selectedProperty().removeListener(channel1CheckListener);
        channel2Check.selectedProperty().removeListener(channel2CheckListener);
        fromVoltageText.focusedProperty().addListener(fromVoltageTextListener);
        toVoltageText.focusedProperty().addListener(toVoltageTextListener);

        ((Pane)fxmlLoader.getNamespace().get("generalPane")).setDisable(false);
    }
}
