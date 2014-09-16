package com.andres.multiwork.pc.screens;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class ImportScreen extends MultiWorkScreen{

    private int channelToImport = 0;
    private ImportEvent importEvent;

    private Button filePickButton;
    private TextField fileName;
    private ChoiceBox<String> formatChoice;
    private Button importButton;
    private TextField channelNumberText;

    private String csvFormat;
    private Importer importer;

    public ImportScreen(Stage stage, final int width, final int height) {
        super(stage);
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(ExportScreen.class.getResource("/importScreen.fxml"));
            Pane pane = fxmlLoader.load();
            setScene(new Scene(pane, width, height));

            fileName = (TextField) fxmlLoader.getNamespace().get("fileNameText");
            filePickButton = (Button) fxmlLoader.getNamespace().get("filePickButton");
            formatChoice = (ChoiceBox<String>) fxmlLoader.getNamespace().get("csvTypeChoose");
            importButton = (Button) fxmlLoader.getNamespace().get("importButton");
            channelNumberText = (TextField) fxmlLoader.getNamespace().get("channelNumberText");

            formatChoice.getItems().addAll("MultiWork CSV Format");
            formatChoice.getSelectionModel().select(0);
            channelNumberText.setText("1");

            importButton.setOnAction(event -> {
                int channel = Integer.valueOf(channelNumberText.getText());
                if(channel < 1) channel = 1;
                else if(channel > GlobalValues.channelsNumber) channel = GlobalValues.channelsNumber;

                channelToImport = channel-1;
                channelNumberText.setText("" + channel);

                switch (csvFormat){
                    case "MultiWork CSV Format":
                        importer = new MultiWorkImporter(Decoder.getDecoder(), fileName.getText(), channelToImport);
                        importer.importData();

                        if(importEvent != null) importEvent.onImportFinished();
                        getStage().hide();
                        break;
                }
            });

            formatChoice.selectionModelProperty().addListener((observable, oldValue, newValue) ->
                    csvFormat = formatChoice.getValue());

            channelNumberText.setOnAction(event -> {
                int channel = Integer.valueOf(channelNumberText.getText());
                if(channel < 1) channel = 1;
                else if(channel > GlobalValues.channelsNumber) channel = GlobalValues.channelsNumber;

                channelToImport = channel-1;
                channelNumberText.setText("" + channel);
            });

            filePickButton.setOnAction(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("CSV File (*.csv)", "*.csv"));
                fileChooser.setTitle(GlobalValues.resourceBundle.getString("loadFile"));

                // Retrieve location and file name
                File file = fileChooser.showOpenDialog(getStage());
                fileName.setText(file.getPath());
            });

        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setOnImportFinished(ImportEvent importEvent){
        this.importEvent = importEvent;
    }

    @Override
    public void show() {
        getStage().setScene(getScene());
        getStage().show();

        csvFormat = formatChoice.getValue();
    }

}
