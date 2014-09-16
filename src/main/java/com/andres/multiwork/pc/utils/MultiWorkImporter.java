package com.andres.multiwork.pc.utils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.protocolanalyzer.api.LogicBitSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MultiWorkImporter implements Importer {

    private final int channelNumber;
    private final Decoder decoder;
    private final String fileName;

    public MultiWorkImporter(Decoder decoder, String fileName, int channelNumber) {
        this.channelNumber = channelNumber;
        this.decoder = decoder;
        this.fileName = fileName;
    }

    @Override
    public void importData() {
        boolean sampleRateDefined = false;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            // CSVReader starts skipping the first 4 lines
            CSVReader csvReader = new CSVReader(bufferedReader, ',', CSVWriter.NO_QUOTE_CHARACTER);
            LogicBitSet logicBitSet = new LogicBitSet();

            List<String[]> data = csvReader.readAll();
            for(String[] line : data){
                switch (line[0]) {
                    case "Sample Rate":
                        decoder.setSampleFrequency(Long.valueOf(line[1]), channelNumber);
                        sampleRateDefined = true;
                        break;

                    // Just data for info
                    case "Sample Number":
                        continue;

                    // Read sample data
                    default:
                        try {
                            logicBitSet.set(Integer.valueOf(line[0]), line[1].equals("1"));
                        } catch (NumberFormatException ignored){}
                        break;
                }
            }

            if(!sampleRateDefined){
                System.err.println("Error - MultiWork CSV file corrupt. Sample rate was not defined");
                return;
            }

            decoder.setRawData(logicBitSet, channelNumber);
        } catch (IOException e) { e.printStackTrace(); }
    }
}