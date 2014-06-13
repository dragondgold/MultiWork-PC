package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.Decoder;
import com.andres.multiwork.pc.utils.Exporter;
import com.andres.multiwork.pc.utils.MultiWorkScreen;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class ExportScreen extends MultiWorkScreen {

    private CheckBox exportDataCheck;
    private CheckBox exportSamplesCheck;
    private DatePicker datePicker;
    private TextArea description;
    private TextField title;
    private TextField fileName;
    private Button filePickButton;

    private int channelToExport = -1;

    public ExportScreen(final Stage stage, final int width, final int height) {
        super(stage);
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(ExportScreen.class.getResource("/exportScreen.fxml"));
            Pane pane = fxmlLoader.load();
            setScene(new Scene(pane, width, height));

            // Export button
            ((Button)fxmlLoader.getNamespace().get("exportButton")).setOnAction(event -> {
                if(!fileName.getText().isEmpty()) {
                    Exporter exporter = new Exporter(Decoder.getDecoder().getRawData(channelToExport), Decoder.getDecoder().getDecodedData(channelToExport));
                    exporter.setTitle(title.getText());
                    exporter.setDate(datePicker.getEditor().getText());
                    exporter.setDescription(description.getText());
                    exporter.setFileName(fileName.getText());
                    exporter.setExportData(exportDataCheck.isSelected());
                    exporter.setExportSamples(exportSamplesCheck.isSelected());

                    exporter.export();
                    getStage().hide();
                }
            });

            exportDataCheck = (CheckBox) fxmlLoader.getNamespace().get("dataExportCheck");
            exportSamplesCheck = (CheckBox) fxmlLoader.getNamespace().get("sampleExportCheck");
            datePicker = (DatePicker) fxmlLoader.getNamespace().get("datePicker");
            description = (TextArea) fxmlLoader.getNamespace().get("descriptionText");
            title = (TextField) fxmlLoader.getNamespace().get("titleText");
            fileName = (TextField) fxmlLoader.getNamespace().get("fileNameText");
            filePickButton = (Button) fxmlLoader.getNamespace().get("filePickButton");

            // Pick file store location
            filePickButton.setOnAction(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle(GlobalValues.resourceBundle.getString("saveFile"));

                // Retrieve location and file name
                File file = fileChooser.showSaveDialog(getStage());
                fileName.setText(file.getPath());
            });

        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setChannelToExport(int channelNumber){
        channelToExport = channelNumber - 1;
        getStage().setTitle(GlobalValues.resourceBundle.getString("channel") + " " + (channelToExport+1));
    }

    @Override
    public void show() {
        getStage().setScene(getScene());
        getStage().show();
    }
}
