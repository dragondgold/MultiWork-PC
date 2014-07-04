package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.SettingsPane;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;

import java.io.IOException;

public class SPIPane extends SettingsPane{

    private ChoiceBox<String> choiceClockChannel;
    private ChoiceBox<String> choiceSPIMode;
    private CheckBox cpolCheck;
    private CheckBox cphaCheck;

    private ChangeListener<String> choiceClockListener;
    private ChangeListener<String> choiceSPIModeListener;
    private ChangeListener<Boolean> cpolCheckListener;
    private ChangeListener<Boolean> cphaCheckListener;

    public SPIPane(int id) {
        super(id);

        try {
            setPane(FXMLLoader.load(SettingsScreen.class.getResource("/LogicAnalyzerSettings/PaneSPI.fxml")));

            choiceClockChannel = (ChoiceBox<String>) getPane().lookup("#choiceClockChannel");
            choiceSPIMode = (ChoiceBox<String>) getPane().lookup("#choiceSPIMode");

            cpolCheck = (CheckBox) getPane().lookup("#CPOLCheck");
            cphaCheck = (CheckBox) getPane().lookup("#CPHACheck");

            choiceClockListener = (observable, oldValue, newValue) -> {
                String disableString = GlobalValues.resourceBundle.getString("channelDisabled");

                // Check if clock channel was disabled
                if (newValue.equals(disableString))
                    GlobalValues.xmlSettings.setProperty("clockSPI" + getChannel(), GlobalValues.channelDisabled);

               // Otherwise check which is the new channel
                else {
                    int index = Character.getNumericValue(newValue.charAt(newValue.length() - 1)) - 1;
                    if (index > 0 && index < GlobalValues.channelsNumber)
                        GlobalValues.xmlSettings.setProperty("clockSPI" + getChannel(), index);
                    else {
                        System.out.println("Corrupted settings, setting SPI clock channel to disabled");
                        GlobalValues.xmlSettings.setProperty("clockSPI" + getChannel(), GlobalValues.channelDisabled);
                    }
                }
            };

            // SPI mode only changes CPOL and CPHA states so SPI mode is not saved is calculated based
            //  on CPOL and CPHA states
            choiceSPIModeListener = (observable, oldValue, newValue) -> {
                // Set CPOL and CPHA based on SPI mode
                switch (Integer.valueOf(newValue)){
                    case 0:
                        cpolCheck.setSelected(false);
                        cphaCheck.setSelected(false);
                        break;
                    case 1:
                        cpolCheck.setSelected(false);
                        cphaCheck.setSelected(true);
                        break;
                    case 2:
                        cpolCheck.setSelected(true);
                        cphaCheck.setSelected(false);
                        break;
                    case 3:
                        cpolCheck.setSelected(true);
                        cphaCheck.setSelected(true);
                        break;
                }
            };

            cpolCheckListener = (observable, oldValue, newValue) -> {
                GlobalValues.xmlSettings.setProperty("cpolSPI", newValue.booleanValue());
                configureSPIMode();
            };

            cphaCheckListener = (observable, oldValue, newValue) -> {
                GlobalValues.xmlSettings.setProperty("cphaSPI", newValue.booleanValue());
                configureSPIMode();
            };

        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Set SPI mode based on CPOL and CPHA state
     */
    private void configureSPIMode(){
        if(!cphaCheck.isSelected() && !cpolCheck.isSelected()){
            choiceSPIMode.getSelectionModel().select(0);

        }else if(cphaCheck.isSelected() && !cpolCheck.isSelected()){
            choiceSPIMode.getSelectionModel().select(1);

        }else if(!cphaCheck.isSelected() && cpolCheck.isSelected()){
            choiceSPIMode.getSelectionModel().select(2);

        }else if(cphaCheck.isSelected() && cpolCheck.isSelected()){
            choiceSPIMode.getSelectionModel().select(3);
        }
    }

    /**
     * Get the {@link java.lang.String} referencing the given channel number in the given {@link javafx.scene.control.ChoiceBox}
     * @param channelNumber channel number we are looking for
     * @param choiceBox {@link javafx.scene.control.ChoiceBox} where to search the channel number reference
     * @return {@link java.lang.String} contained in the {@link javafx.scene.control.ChoiceBox} representing the given channel number
     */
    private String getChannelString(int channelNumber, ChoiceBox<String> choiceBox){
        for(String str : choiceBox.getItems()){
            // Get channel number
            if(Character.getNumericValue(str.charAt(str.length()-1))-1 == channelNumber){
                return str;
            }
        }
        return null;
    }

    @Override
    public void enable() {
        // Use disable() first just in case enabled() is called again without calling disable() first so when we are
        //  updating the settings all the listener will start acting because they are enabled.
        disable();

        // Generate clock channels according to current channelNumber. If we are on Channel 1, it can't be a clock channel so
        //  we generate all others channels but channel 1.
        ObservableList<String> clockChannelsList = FXCollections.observableArrayList();
        clockChannelsList.add(GlobalValues.resourceBundle.getString("channelDisabled"));
        for(int n = 1; n <= GlobalValues.channelsNumber; ++n){
            if(n != (getChannel()+1)) clockChannelsList.add(GlobalValues.resourceBundle.getString("channel") + " " + n);
        }
        choiceClockChannel.setItems(clockChannelsList);

        // Set the clock channel number stored in the settings
        int clockChannel = GlobalValues.xmlSettings.getInt("clockSPI" + getChannel(), GlobalValues.channelDisabled);
        if(clockChannel == GlobalValues.channelDisabled) choiceClockChannel.setValue(GlobalValues.resourceBundle.getString("channelDisabled"));
        else choiceClockChannel.setValue(getChannelString(clockChannel, choiceClockChannel));

        // CPOL and CPHA checks
        cpolCheck.setSelected(GlobalValues.xmlSettings.getBoolean("cpolSPI", false));
        cphaCheck.setSelected(GlobalValues.xmlSettings.getBoolean("cphaSPI", false));

        // SPI Modes
        ObservableList<String> spiModesList = FXCollections.observableArrayList();
        spiModesList.addAll("0", "1", "2", "3");
        choiceSPIMode.setItems(spiModesList);

        // Set SPI mode based on CPOL and CPHA checks
        configureSPIMode();

        choiceClockChannel.getSelectionModel().selectedItemProperty().addListener(choiceClockListener);
        choiceSPIMode.getSelectionModel().selectedItemProperty().addListener(choiceSPIModeListener);
        cpolCheck.selectedProperty().addListener(cpolCheckListener);
        cphaCheck.selectedProperty().addListener(cphaCheckListener);
    }

    @Override
    public void disable() {
        choiceClockChannel.getSelectionModel().selectedItemProperty().removeListener(choiceClockListener);
        choiceSPIMode.getSelectionModel().selectedItemProperty().removeListener(choiceSPIModeListener);
        cpolCheck.selectedProperty().removeListener(cpolCheckListener);
        cphaCheck.selectedProperty().removeListener(cphaCheckListener);
    }
}
