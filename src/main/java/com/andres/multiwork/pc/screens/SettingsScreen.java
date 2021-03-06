package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.MultiWorkScreen;
import com.andres.multiwork.pc.utils.SettingsPane;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class SettingsScreen extends MultiWorkScreen {

    private static final int GENERAL_SETTINGS = 1;
    private static final int RIGOL_OSCILLOSCOPE = 2;
    private int currentChannel = 0;

    // Protocol
    private final ToggleGroup protocolToggleGroup = new ToggleGroup();
    private RadioButton radioI2C;
    private RadioButton radioUART;
    private RadioButton radioSPI;
    private RadioButton radioOneWire;
    private RadioButton radioClock;
    private RadioButton radioDisabled;
    private CheckBox checkSimpleTrigger;

    // Settings
    private ListView<String> listView;
    private MenuBar menuBar;

    private ChangeListener<Toggle> toggleChangeListener;
    private ChangeListener<Boolean> triggerChangeListener;

    private FXMLLoader fxmlLoader;
    private List<SettingsPane> settingsPaneList = new ArrayList<>();

    public SettingsScreen(final Stage stage, final int width, final int height) {
        super(stage);
        try {
            // Main screen
            fxmlLoader = new FXMLLoader(SettingsScreen.class.getResource("/LogicAnalyzerSettings/settingsScreen.fxml"));
            Pane mainScreen = fxmlLoader.load();

            // Pane corresponding to each protocol and general settings
            settingsPaneList.add(new I2CPane(GlobalValues.i2cProtocol));
            settingsPaneList.add(new UARTPane(GlobalValues.uartProtocol));
            settingsPaneList.add(new SPIPane(GlobalValues.spiProtocol));
            settingsPaneList.add(new GeneralPane(GENERAL_SETTINGS, fxmlLoader));
            settingsPaneList.add(new RigolScopePane(RIGOL_OSCILLOSCOPE, fxmlLoader));

            setScene(new Scene(mainScreen, width, height));
            stage.setTitle(GlobalValues.xmlSettings.getString("menuSettings"));

            buildReferences();
            initialize();
            listView.getSelectionModel().select(2);     // Select the first channel

            GlobalValues.xmlSettings.addConfigurationListener(configurationEvent -> {
                // There was a change in protocols
                if(configurationEvent.getPropertyName().contains("protocol")){
                    updateArrayListNames();
                }
            });

            // Notify panes that channel number changed!
            for (SettingsPane p : settingsPaneList){
                p.notifiyChannelChanged(currentChannel);
            }
            protocolToPane(GlobalValues.xmlSettings.getInt("protocol" + currentChannel, GlobalValues.uartProtocol));

            // Disable all panes on close
            stage.setOnCloseRequest(event -> {
                for(SettingsPane p : settingsPaneList) p.disable();
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Show the side pane corresponding to the current selected item
     */
    private void showPane(int pane){
        // Disable the side pane if the channel is disabled or protocol is clock
        if(pane == GlobalValues.channelDisabled || pane == GlobalValues.clockProtocol) {
            ((SplitPane) fxmlLoader.getNamespace().get("splitPane")).getItems().get(1).setDisable(true);
        }
        else {
            ((SplitPane) fxmlLoader.getNamespace().get("splitPane")).getItems().get(1).setDisable(false);
        }

        // Show and enable selected pane and disable all the others
        for(SettingsPane p : settingsPaneList) p.disable();
        for(SettingsPane p : settingsPaneList){
            if(p.getID() == pane){
                ((SplitPane)fxmlLoader.getNamespace().get("splitPane")).getItems().set(1, p.getPane());
                p.enable();
                break;
            }
        }
    }

    /**
     * Show pane according to protocol
     * @param protocol protocol to show
     */
    private void protocolToPane(int protocol){
        showPane(protocol);
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
        // We get this Nodes in a different way than other references. This is because all of this is inside
        //  SplitPane and lookup() doesn't work so this is the workaround!

        // Protocol
        radioI2C = (RadioButton)fxmlLoader.getNamespace().get("radioI2C");
        radioUART = (RadioButton)fxmlLoader.getNamespace().get("radioUART");
        radioSPI = (RadioButton)fxmlLoader.getNamespace().get("radioSPI");
        radioOneWire = (RadioButton)fxmlLoader.getNamespace().get("radioOneWire");
        radioClock = (RadioButton)fxmlLoader.getNamespace().get("radioClock");
        radioDisabled = (RadioButton)fxmlLoader.getNamespace().get("radioDisabled");

        // Trigger
        checkSimpleTrigger = (CheckBox)fxmlLoader.getNamespace().get("checkSimpleTrigger");

        listView = (ListView<String>)fxmlLoader.getNamespace().get("listView");
        menuBar = (MenuBar)fxmlLoader.getNamespace().get("menuBar");
    }

    /**
     * Init elements with corresponding texts and items
     */
    private void initialize() {
        updateArrayListNames();
        listView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            // If it's not a channel, then let's see what other pane it is
            if(!Pattern.compile("Channel [0-9]+").matcher(newValue).find()){
                if(newValue.equals(GlobalValues.resourceBundle.getString("general"))) showPane(GENERAL_SETTINGS);
                else if(newValue.equals(GlobalValues.resourceBundle.getString("rigolScope"))) showPane(RIGOL_OSCILLOSCOPE);
                return;
            }

            // Channels go from 0 to (GlobalValues.channelsNumber-1). Remove all non-numbers.
            String channel = newValue.substring(0, newValue.indexOf('-'));
            int index = Integer.valueOf(channel.replaceAll("[^0-9]", ""));
            currentChannel = index - 1;
            loadChannelSettings(currentChannel);

            // Notify panes that channel number changed!
            for (SettingsPane p : settingsPaneList){
                p.notifiyChannelChanged(currentChannel);
            }
            protocolToPane(GlobalValues.xmlSettings.getInt("protocol" + currentChannel, GlobalValues.uartProtocol));
        });

        // Protocol selection
        radioI2C.setToggleGroup(protocolToggleGroup);
        radioUART.setToggleGroup(protocolToggleGroup);
        radioSPI.setToggleGroup(protocolToggleGroup);
        radioOneWire.setToggleGroup(protocolToggleGroup);
        radioClock.setToggleGroup(protocolToggleGroup);
        radioDisabled.setToggleGroup(protocolToggleGroup);
        radioDisabled.setText(GlobalValues.resourceBundle.getString("channelDisabled"));

        // File Menu
        menuBar.getMenus().get(0).setText(GlobalValues.resourceBundle.getString("menuFile"));
        menuBar.getMenus().get(0).setOnAction(actionEvent -> getStage().close());

        // Show screen
        defineChangeListeners();
        loadChannelSettings(currentChannel);
    }

    private void updateArrayListNames(){
        // Channels list
        ObservableList<String> channelsList = FXCollections.observableArrayList();
        channelsList.add(GlobalValues.resourceBundle.getString("general"));
        channelsList.add(GlobalValues.resourceBundle.getString("rigolScope"));

        for(int n = 0; n < GlobalValues.channelsNumber; ++n){
            String s;
            switch (GlobalValues.xmlSettings.getInt("protocol" + n, GlobalValues.uartProtocol)){
                case GlobalValues.uartProtocol:
                    s = GlobalValues.resourceBundle.getString("channel") + " " + (n+1) + " - " + "UART";
                    break;

                case GlobalValues.i2cProtocol:
                    s = GlobalValues.resourceBundle.getString("channel") + " " + (n+1) + " - " + "I2C";
                    break;

                case GlobalValues.spiProtocol:
                    s = GlobalValues.resourceBundle.getString("channel") + " " + (n+1) + " - " + "SPI";
                    break;

                case GlobalValues.clockProtocol:
                    s = GlobalValues.resourceBundle.getString("channel") + " " + (n+1) + " - " + "Clock";
                    break;

                case GlobalValues.oneWireProtocol:
                    s = GlobalValues.resourceBundle.getString("channel") + " " + (n+1) + " - " + "1-Wire";
                    break;

                case GlobalValues.channelDisabled:
                    s = GlobalValues.resourceBundle.getString("channel") + " " + (n+1) + " - " + "NC";
                    break;

                default:
                    s = GlobalValues.resourceBundle.getString("channel") + " " + (n+1);
                    break;
            }
            channelsList.add(s);
        }
        listView.setItems(channelsList);
    }

    /**
     * Create change listeners from the preferences.
     */
    private void defineChangeListeners(){
        toggleChangeListener = (observableValue, oldValue, newValue) -> {
            String toggleString = ((RadioButton) newValue).getText();
            String disableString = GlobalValues.resourceBundle.getString("channelDisabled");

            if (toggleString.equals("I2C"))
                GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.i2cProtocol);
            else if (toggleString.equals("UART"))
                GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.uartProtocol);
            else if (toggleString.equals("Clock"))
                GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.clockProtocol);
            else if (toggleString.equals("SPI"))
                GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.spiProtocol);
            else if (toggleString.equals("1-Wire"))
                GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.oneWireProtocol);
            else if (toggleString.equals(disableString))
                GlobalValues.xmlSettings.setProperty("protocol" + currentChannel, GlobalValues.channelDisabled);

            protocolToPane(GlobalValues.xmlSettings.getInt("protocol" + currentChannel, GlobalValues.uartProtocol));
        };

        triggerChangeListener = (observableValue, oldValue, newValue) ->
                GlobalValues.xmlSettings.setProperty("simpleTrigger" + currentChannel, newValue);
    }

    /**
     * Add all change listeners to the preferences.
     */
    private void setupChangeListeners(){
        protocolToggleGroup.selectedToggleProperty().addListener(toggleChangeListener);
        checkSimpleTrigger.selectedProperty().addListener(triggerChangeListener);
    }

    /**
     * Remove all change listeners from the preferences.
     */
    private void removeChangeListeners(){
        protocolToggleGroup.selectedToggleProperty().removeListener(toggleChangeListener);
        checkSimpleTrigger.selectedProperty().removeListener(triggerChangeListener);
    }

    /**
     * Sets the preferences screen for the selected channel loading the saved settings. If no settings are available
     *  default values are loaded.
     * @param channelNumber channel number from 0 to {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}-1
     */
    private void loadChannelSettings(int channelNumber) {
        // Remove change listener while setting the preference values, otherwise they will be called while we set the values
        removeChangeListeners();

        XMLConfiguration settings = GlobalValues.xmlSettings;

        // Load settings
        int protocol = settings.getInt("protocol" + channelNumber, GlobalValues.uartProtocol);
        boolean simpleTrigger = settings.getBoolean("simpleTrigger" + channelNumber, false);

        radioI2C.setSelected(false);
        radioUART.setSelected(false);
        radioClock.setSelected(false);
        radioDisabled.setSelected(false);

        if(protocol == GlobalValues.i2cProtocol) radioI2C.setSelected(true);
        else if(protocol == GlobalValues.uartProtocol) radioUART.setSelected(true);
        else if(protocol == GlobalValues.clockProtocol) radioClock.setSelected(true);
        else if(protocol == GlobalValues.channelDisabled) radioDisabled.setSelected(true);

        checkSimpleTrigger.setSelected(simpleTrigger);

        // Restore change listeners
        setupChangeListeners();
    }

}
