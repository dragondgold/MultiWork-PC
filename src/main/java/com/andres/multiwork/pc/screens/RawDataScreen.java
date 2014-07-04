package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.DecodedTableItem;
import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.Decoder;
import com.andres.multiwork.pc.utils.MultiWorkScreen;
import com.protocolanalyzer.api.TimePosition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class RawDataScreen extends MultiWorkScreen {

    private final Decoder decoder = Decoder.getDecoder().setSettings(GlobalValues.xmlSettings);
    private final ObservableList<DecodedTableItem> decodedTableItems = FXCollections.observableArrayList();

    private TableColumn t1Column;
    private TableColumn t2Column;
    private TableColumn eventColumn;

    public RawDataScreen(final Stage stage, final int width, final int height){
        super(stage);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RawDataScreen.class.getResource("/rawDataScreen.fxml"));
            Pane mainScreen = fxmlLoader.load();
            setScene(new Scene(mainScreen, width, height));

            t1Column = (TableColumn)fxmlLoader.getNamespace().get("t1Column");
            t1Column.setCellValueFactory(new PropertyValueFactory<DecodedTableItem, String>("startTime"));
            t1Column.setSortable(false);

            t2Column = (TableColumn)fxmlLoader.getNamespace().get("t2Column");
            t2Column.setCellValueFactory(new PropertyValueFactory<DecodedTableItem, String>("endTime"));
            t2Column.setSortable(false);

            eventColumn = (TableColumn)fxmlLoader.getNamespace().get("eventColumn");
            eventColumn.setCellValueFactory(new PropertyValueFactory<DecodedTableItem, String>("dataString"));

            ((TableView)fxmlLoader.getNamespace().get("rawDataTable")).setItems(decodedTableItems);

        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Set channel to show
     * @param channelNumber channel number from 0 to {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}-1
     */
    public void setChannelToShow(int channelNumber){
        int channelToShow = channelNumber;
        getStage().setTitle(GlobalValues.resourceBundle.getString("channel") + " " + (channelToShow+1));

        decodedTableItems.clear();
        for (TimePosition data : decoder.getDecodedData(channelToShow)){
            DecodedTableItem item = new DecodedTableItem(data.getString(), timeToLabel(data.startTime()), timeToLabel(data.endTime()));
            decodedTableItems.add(item);
        }
    }

    /**
     * Converts time in seconds in the best way to show it (mS, uS or nS)
     * @param time time in seconds to convert
     * @return {@link java.lang.String} representation of the converted time with the corresponding unit
     */
    private String timeToLabel(double time){

        // Time > 1000uS, show it as mS
        if(time * 1E6 >= 1000){
            return String.format("%.2f", time*1E3) + " mS";
        }

        // Time > 1000nS show it as uS
        else if(time * 1E9 >= 1000){
            return String.format("%.2f", time*1E6) + " μS";
        }

        // Else, show it as nS
        else{
            return String.format("%.2f", time*1E9) + " nS";
        }
    }

    @Override
    public void show() {
        getStage().setScene(getScene());
        getStage().show();
    }
}
