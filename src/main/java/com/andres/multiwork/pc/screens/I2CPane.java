package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.SettingsPane;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;

import java.io.IOException;

@SuppressWarnings("unchecked")
public class I2CPane extends SettingsPane {

    private ChoiceBox<String> choiceClockChannel;
    private ChangeListener<String> clockChannelListener;

    public I2CPane(int id) {
        super(id);

        try {
            setPane(FXMLLoader.load(getClass().getResource("/LogicAnalyzerSettings/PaneI2C.fxml")));

            // Clock selector
            choiceClockChannel = (ChoiceBox<String>)getPane().lookup("#choiceClockChannel");

            clockChannelListener = (observableValue, s, newValue) -> {
                String disableString = GlobalValues.resourceBundle.getString("channelDisabled");

                // Check if clock channel was disabled
                if (newValue.equals(disableString))
                    GlobalValues.xmlSettings.setProperty("clock" + getChannel(), GlobalValues.channelDisabled);

                // Otherwise check which is the new channel
                else {
                    int index = Character.getNumericValue(newValue.charAt(newValue.length() - 1)) - 1;
                    if (index > 0 && index < GlobalValues.channelsNumber)
                        GlobalValues.xmlSettings.setProperty("clock" + getChannel(), index);
                    else {
                        System.out.println("Corrupted settings, setting clock channel to disabled");
                        GlobalValues.xmlSettings.setProperty("clock" + getChannel(), GlobalValues.channelDisabled);
                    }
                }
            };
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void enable() {
        // Use disable() first just in case enabled() is called again without calling disable() first so when we are
        //  updating the settings all the listener will start acting because they are enabled.
        disable();

        // Update the settings according to the preferences loaded. Preferences like protocols are stored with an integer constant value
        //  stored in 'GlobalValues' class.
        int clockChannel = GlobalValues.xmlSettings.getInt("clock" + getChannel(), GlobalValues.channelDisabled);

        // Generate clock channels according to current channelNumber. If we are on Channel 1, it can't be a clock channel so
        //  we generate all others channels but channel 1.
        ObservableList<String> clockChannelsList = FXCollections.observableArrayList();

        clockChannelsList.add(GlobalValues.resourceBundle.getString("channelDisabled"));
        for(int n = 1; n <= GlobalValues.channelsNumber; ++n){
            if(n != (getChannel()+1)) clockChannelsList.add(GlobalValues.resourceBundle.getString("channel") + " " + n);
        }
        choiceClockChannel.setItems(clockChannelsList);

        // Set the channel number stored in the settings
        if(clockChannel == GlobalValues.channelDisabled) choiceClockChannel.setValue(GlobalValues.resourceBundle.getString("channelDisabled"));
        else{
            for(String str : choiceClockChannel.getItems()){
                // Get channel number
                if(Character.getNumericValue(str.charAt(str.length()-1))-1 == clockChannel){
                    choiceClockChannel.setValue(str);
                }
            }
        }

        choiceClockChannel.getSelectionModel().selectedItemProperty().addListener(clockChannelListener);
    }

    @Override
    public void disable() {
        choiceClockChannel.getSelectionModel().selectedItemProperty().removeListener(clockChannelListener);
    }



}
