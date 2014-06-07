package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.IOException;

public class SettingsScreen extends MultiWorkScreen {

    private static final int GENERAL_SETTINGS = 0;
    private static final int CHANNELS_SETTINGS = 1;

    private int currentChannel = 0;

    // Protocol
    private final ToggleGroup protocolToggleGroup = new ToggleGroup();
    private RadioButton radioI2C;
    private RadioButton radioUART;
    private RadioButton radioClock;
    private RadioButton radioDisabled;
    private CheckBox checkSimpleTrigger;

    // I2C
    private ChoiceBox<String> choiceClockChannel;
    private ChoiceBox<String> choiceParity;

    // UART
    private TextField baudRateText;
    private CheckBox nineBitsModeCheck;
    private CheckBox twoStopBitsCheck;

    // General
    private ChoiceBox<Integer> sampleRateChoice;
    private CheckBox simpleTriggerGeneral;

    // Settings
    private ListView<String> listView;
    private MenuBar menuBar;

    private ChangeListener<Toggle> toggleChangeListener;
    private ChangeListener<Boolean> triggerChangeListener;
    private ChangeListener<String> clockChannelListener;
    private ChangeListener<String> parityChangeListener;
    private ChangeListener<String> baudChangeListener;
    private ChangeListener<Boolean> nineBitsChangeListener;
    private ChangeListener<Boolean> twoStopBitsChangeListener;
    private ChangeListener<Integer> sampleRateChangeListener;
    private ChangeListener<Boolean> simpleTriggerGeneralListener;

    private FXMLLoader fxmlLoader;
    private AnchorPane channelsSettingsPane;
    private AnchorPane generalSettingsPane;
    private Pane mainScreen;

    public SettingsScreen(final Stage stage, final int width, final int height) {
        super(stage);
        try {
            // Main screen
            fxmlLoader = new FXMLLoader(SettingsScreen.class.getResource("/settingsScreen.fxml"));
            mainScreen = fxmlLoader.load();

            // Pane corresponding to general settings
            generalSettingsPane = FXMLLoader.load(SettingsScreen.class.getResource("/generalSettings.fxml"));

            // Pane corresponding to channels settings
            channelsSettingsPane = FXMLLoader.load(SettingsScreen.class.getResource("/channelsSettings.fxml"));

            showPane(CHANNELS_SETTINGS);
            setScene(new Scene(mainScreen, width, height));

            buildReferences();
            initialize();
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Show the side pane corresponding to the current selected item
     */
    private void showPane(int pane){
        if(pane == CHANNELS_SETTINGS){
            ((SplitPane)fxmlLoader.getNamespace().get("splitPane")).getItems().set(1, channelsSettingsPane);
        }else if(pane == GENERAL_SETTINGS) {
            ((SplitPane)fxmlLoader.getNamespace().get("splitPane")).getItems().set(1, generalSettingsPane);
        }
    }

    @Override
    public void show() {
        getStage().setScene(getScene());
        getStage().show();
    }

    /**
     * Lookup in FXML for every defined item
     */
    private void buildReferences(){
        // Protocol
        radioI2C = (RadioButton)channelsSettingsPane.lookup("#radioI2C");
        radioUART = (RadioButton)channelsSettingsPane.lookup("#radioUART");
        radioClock = (RadioButton)channelsSettingsPane.lookup("#radioClock");
        radioDisabled = (RadioButton)channelsSettingsPane.lookup("#radioDisabled");

        // Trigger
        checkSimpleTrigger = (CheckBox)channelsSettingsPane.lookup("#checkSimpleTrigger");

        // I2C
        choiceClockChannel = (ChoiceBox<String>)channelsSettingsPane.lookup("#choiceClockChannel");
        choiceParity = (ChoiceBox<String>)channelsSettingsPane.lookup("#choiceParity");

        // UART
        baudRateText = (TextField)channelsSettingsPane.lookup("#baudRateText");
        nineBitsModeCheck = (CheckBox)channelsSettingsPane.lookup("#nineBitsModeCheck");
        twoStopBitsCheck = (CheckBox)channelsSettingsPane.lookup("#twoStopBitsCheck");

        // General
        sampleRateChoice = (ChoiceBox<Integer>)generalSettingsPane.lookup("#sampleRateChoice");
        simpleTriggerGeneral = (CheckBox) generalSettingsPane.lookup("#simpleTriggerGeneral");

        // We get listView and menuBar in a different way than the other references. This is because all of this is inside
        //  SplitPane and lookup() doesn't work so this is the workaround!
        // ListView
        listView = (ListView<String>)fxmlLoader.getNamespace().get("listView");
        // MenuBar
        menuBar = (MenuBar)fxmlLoader.getNamespace().get("menuBar");
    }

    /**
     * Init elements with corresponding texts and items
     */
    private void initialize() {
        // Channels list
        ObservableList<String> channelsList = FXCollections.observableArrayList();
        channelsList.add(GlobalValues.resourceBundle.getString("general"));
        for(int n = 1; n <= GlobalValues.channelsNumber; ++n){
            channelsList.add(GlobalValues.resourceBundle.getString("channel") + " " + n);
        }

        listView.setItems(channelsList);
        listView.getSelectionModel().select(1);
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                // Show again the channel settings if we come from general settings
                if(oldValue.contains(GlobalValues.resourceBundle.getString("general"))){
                    showPane(CHANNELS_SETTINGS);
                }
                else if(newValue.contains(GlobalValues.resourceBundle.getString("general"))){
                    showPane(GENERAL_SETTINGS);
                    return;
                }

                int index = Character.getNumericValue(newValue.charAt(newValue.length() - 1));
                // Channels go from 0 to (GlobalValues.channelsNumber-1)
                currentChannel = index - 1;
                loadChannelSettings(currentChannel);
            }
        });

        // Protocol selection
        radioI2C.setToggleGroup(protocolToggleGroup);
        radioUART.setToggleGroup(protocolToggleGroup);
        radioClock.setToggleGroup(protocolToggleGroup);
        radioDisabled.setToggleGroup(protocolToggleGroup);
        radioDisabled.setText(GlobalValues.resourceBundle.getString("channelDisabled"));

        // Parity selector
        ObservableList<String> parityList = FXCollections.observableArrayList();
        parityList.add(GlobalValues.resourceBundle.getString("parityEven"));
        parityList.add(GlobalValues.resourceBundle.getString("parityOdd"));
        parityList.add(GlobalValues.resourceBundle.getString("parityNone"));
        choiceParity.setItems(parityList);

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

        // File Menu
        menuBar.getMenus().get(0).setText(GlobalValues.resourceBundle.getString("menuFile"));
        menuBar.getMenus().get(0).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                getStage().close();
            }
        });

        // Show screen
        defineChangeListeners();
        loadChannelSettings(currentChannel);
    }

    /**
     * Create change listeners from the preferences.
     */
    private void defineChangeListeners(){
        toggleChangeListener = new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observableValue, Toggle oldValue, Toggle newValue) {
                String toggleString = ((RadioButton) newValue).getText();
                String disableString = GlobalValues.resourceBundle.getString("channelDisabled");

                if (toggleString.equals("I2C"))
                    GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.i2cProtocol);
                else if (toggleString.equals("UART"))
                    GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.uartProtocol);
                else if (toggleString.equals("Clock"))
                    GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.clockProtocol);
                else if (toggleString.equals(disableString))
                    GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.channelDisabled);

                disableSelectedPreferences(GlobalValues.xmlSettings.getInt("protocol" + currentChannel, GlobalValues.uartProtocol));
            }
        };

        triggerChangeListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                GlobalValues.xmlSettings.setProperty("simpleTrigger" + currentChannel, newValue);
                //disableSelectedPreferences(GlobalValues.xmlSettings.getInt("protocol" + currentChannel, GlobalValues.uartProtocol));
            }
        };

        clockChannelListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String newValue) {
                String disableString = GlobalValues.resourceBundle.getString("channelDisabled");

                if (newValue.equals(disableString))
                    GlobalValues.xmlSettings.setProperty("clock" + currentChannel, GlobalValues.channelDisabled);
                else {
                    int index = Character.getNumericValue(newValue.charAt(newValue.length() - 1)) - 1;
                    if (index > 0 && index < GlobalValues.channelsNumber)
                        GlobalValues.xmlSettings.setProperty("clock" + currentChannel, index);
                    else {
                        System.out.println("Corrupted settings, setting clock channel to disabled");
                        GlobalValues.xmlSettings.setProperty("clock" + currentChannel, GlobalValues.channelDisabled);
                    }
                }
                //disableSelectedPreferences(GlobalValues.xmlSettings.getInt("protocol" + currentChannel, GlobalValues.uartProtocol));
            }
        };

        parityChangeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String newValue) {
                String even = GlobalValues.resourceBundle.getString("parityEven");
                String odd = GlobalValues.resourceBundle.getString("parityOdd");
                String none = GlobalValues.resourceBundle.getString("parityNone");

                if (newValue.equals(even))
                    GlobalValues.xmlSettings.setProperty("parity" + currentChannel, GlobalValues.parityEven);
                else if (newValue.equals(odd))
                    GlobalValues.xmlSettings.setProperty("parity" + currentChannel, GlobalValues.parityOdd);
                else if (newValue.equals(none))
                    GlobalValues.xmlSettings.setProperty("parity" + currentChannel, GlobalValues.parityNone);

            }
        };

        baudChangeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String newValue) {
                GlobalValues.xmlSettings.setProperty("baudRate" + currentChannel, Math.abs(Integer.valueOf(newValue)));
            }
        };

        nineBitsChangeListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean newValue) {
                GlobalValues.xmlSettings.setProperty("nineBitsMode" + currentChannel, newValue);
            }
        };

        twoStopBitsChangeListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean newValue) {
                GlobalValues.xmlSettings.setProperty("twoStopBits" + currentChannel, newValue);
            }
        };

        sampleRateChangeListener = new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer newValue) {
                GlobalValues.xmlSettings.setProperty("sampleRate", newValue);
            }
        };

        simpleTriggerGeneralListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                GlobalValues.xmlSettings.setProperty("simpleTriggerGeneral", newValue);
            }
        };

    }

    /**
     * Add all change listeners to the preferences.
     */
    private void setupChangeListeners(){
        protocolToggleGroup.selectedToggleProperty().addListener(toggleChangeListener);
        checkSimpleTrigger.selectedProperty().addListener(triggerChangeListener);
        choiceClockChannel.getSelectionModel().selectedItemProperty().addListener(clockChannelListener);
        choiceParity.getSelectionModel().selectedItemProperty().addListener(parityChangeListener);
        baudRateText.textProperty().addListener(baudChangeListener);
        nineBitsModeCheck.selectedProperty().addListener(nineBitsChangeListener);
        twoStopBitsCheck.selectedProperty().addListener(twoStopBitsChangeListener);
        sampleRateChoice.getSelectionModel().selectedItemProperty().addListener(sampleRateChangeListener);
        simpleTriggerGeneral.selectedProperty().addListener(simpleTriggerGeneralListener);
    }

    /**
     * Remove all change listeners from the preferences.
     */
    private void removeChangeListeners(){
        protocolToggleGroup.selectedToggleProperty().removeListener(toggleChangeListener);
        checkSimpleTrigger.selectedProperty().removeListener(triggerChangeListener);
        choiceClockChannel.getSelectionModel().selectedItemProperty().removeListener(clockChannelListener);
        choiceParity.getSelectionModel().selectedItemProperty().removeListener(parityChangeListener);
        baudRateText.textProperty().removeListener(baudChangeListener);
        nineBitsModeCheck.selectedProperty().removeListener(nineBitsChangeListener);
        twoStopBitsCheck.selectedProperty().removeListener(twoStopBitsChangeListener);
        sampleRateChoice.getSelectionModel().selectedItemProperty().removeListener(sampleRateChangeListener);
        simpleTriggerGeneral.selectedProperty().removeListener(simpleTriggerGeneralListener);
    }

    /**
     * Disable preferences that doesn't correspond to the current selected protocol. For example, disable UART
     *  setting while in I2C protocol.
     * @param protocol current protocol as constant defined in {@link com.andres.multiwork.pc.GlobalValues}
     */
    private void disableSelectedPreferences(int protocol){
        if(protocol == GlobalValues.i2cProtocol){
            checkSimpleTrigger.setDisable(false);
            choiceClockChannel.setDisable(false);
            choiceParity.setDisable(true);
            baudRateText.setDisable(true);
            nineBitsModeCheck.setDisable(true);
            twoStopBitsCheck.setDisable(true);
            sampleRateChoice.setDisable(false);
        }
        else if(protocol == GlobalValues.uartProtocol){
            checkSimpleTrigger.setDisable(false);
            choiceClockChannel.setDisable(true);
            choiceParity.setDisable(false);
            baudRateText.setDisable(false);
            nineBitsModeCheck.setDisable(false);
            twoStopBitsCheck.setDisable(false);
            sampleRateChoice.setDisable(false);
        }else if(protocol == GlobalValues.clockProtocol){
            checkSimpleTrigger.setDisable(false);
            choiceClockChannel.setDisable(true);
            choiceParity.setDisable(true);
            baudRateText.setDisable(true);
            nineBitsModeCheck.setDisable(true);
            twoStopBitsCheck.setDisable(true);
            sampleRateChoice.setDisable(false);
        }else if(protocol == GlobalValues.channelDisabled){
            checkSimpleTrigger.setDisable(true);
            choiceClockChannel.setDisable(true);
            choiceParity.setDisable(true);
            baudRateText.setDisable(true);
            nineBitsModeCheck.setDisable(true);
            twoStopBitsCheck.setDisable(true);
            sampleRateChoice.setDisable(true);
        }
    }

    /**
     * Sets the preferences screen for the selected channel loading the saved settings. If no settings are available
     *  default values are loaded.
     * @param channelNumber channel number from 0 to (GlobalValues.channelsNumber-1)
     */
    private void loadChannelSettings(int channelNumber) {
        // Remove change listener while setting the preference values, otherwise they will be called while we set the values
        removeChangeListeners();

        XMLConfiguration settings = GlobalValues.xmlSettings;

        // Generate clock channels according to current channelNumber. If we are on Channel 1, it can't be a clock channel so
        //  we generate all others channels but channel 1.
        ObservableList<String> clockChannelsList = FXCollections.observableArrayList();
        for(int n = 1; n <= GlobalValues.channelsNumber; ++n){
            if(n != channelNumber) clockChannelsList.add(GlobalValues.resourceBundle.getString("channel") + " " + n);
        }
        choiceClockChannel.setItems(clockChannelsList);

        // Load settings
        int clockChannel = settings.getInt("clock" + channelNumber, 8);
        int protocol = settings.getInt("protocol" + channelNumber, GlobalValues.uartProtocol);
        int sampleRate = GlobalValues.xmlSettings.getInt("sampleRate", 4000000);

        boolean simpleTrigger = settings.getBoolean("simpleTrigger" + channelNumber, false);
        int parity = settings.getInt("parity" + channelNumber, GlobalValues.parityNone);
        int baudRate = settings.getInt("baudRate" + channelNumber, 9600);

        boolean nineBitsMode = settings.getBoolean("nineBitsMode" + channelNumber, false);
        boolean twoStopBits = settings.getBoolean("twoStopBits" + channelNumber, false);

        /**
         * Update the settings according to the preferences loaded. Preferences like protocols are stored with an Integer constant value
         *  stored in 'GlobalValues' class.
         */
        if(clockChannel == GlobalValues.channelDisabled) choiceClockChannel.setValue(GlobalValues.resourceBundle.getString("channelDisabled"));
        else{
            for(String str : choiceClockChannel.getItems()){
                // Get channel number
                if(Character.getNumericValue(str.charAt(str.length()-1))-1 == clockChannel){
                    choiceClockChannel.setValue(str);
                }
            }
        }

        if(parity == GlobalValues.parityEven) choiceParity.setValue(GlobalValues.resourceBundle.getString("parityEven"));
        else if(parity == GlobalValues.parityOdd) choiceParity.setValue(GlobalValues.resourceBundle.getString("parityOdd"));
        else if(parity == GlobalValues.parityNone) choiceParity.setValue(GlobalValues.resourceBundle.getString("parityNone"));

        baudRateText.setText("" + baudRate);
        nineBitsModeCheck.setSelected(nineBitsMode);
        twoStopBitsCheck.setSelected(twoStopBits);

        radioI2C.setSelected(false);
        radioUART.setSelected(false);
        radioClock.setSelected(false);
        radioDisabled.setSelected(false);

        if(protocol == GlobalValues.i2cProtocol) radioI2C.setSelected(true);
        else if(protocol == GlobalValues.uartProtocol) radioUART.setSelected(true);
        else if(protocol == GlobalValues.clockProtocol) radioClock.setSelected(true);
        else if(protocol == GlobalValues.channelDisabled) radioDisabled.setSelected(true);

        checkSimpleTrigger.setSelected(simpleTrigger);
        sampleRateChoice.setValue(sampleRate);

        disableSelectedPreferences(protocol);
        // Restore change listeners
        setupChangeListeners();
    }

}
