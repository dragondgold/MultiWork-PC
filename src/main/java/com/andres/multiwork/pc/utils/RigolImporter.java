package com.andres.multiwork.pc.utils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.protocolanalyzer.api.LogicBitSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class RigolImporter implements Importer{

    private final int channelNumber;
    private final Decoder decoder;
    private final String fileName;
    private final double lowThreshold;

    private long sampleRate = -1;

    /**
     * Import data using the format used in Rigol Oscilloscopes
     * @param decoder the decoder where to store the imported data
     * @param fileName from what file we have to import the data
     * @param channelNumber to what channel we should save the data
     * @param lowThreshold the minimum voltage of the signal to be considered a logic '1'
     */
    public RigolImporter(Decoder decoder, String fileName, int channelNumber, double lowThreshold) {
        this.channelNumber = channelNumber;
        this.decoder = decoder;
        this.fileName = fileName;
        this.lowThreshold = lowThreshold;
    }

    @Override
    public void importData() {
        try {
            int sampleCounter = 0;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            CSVReader csvReader = new CSVReader(bufferedReader, ',', CSVWriter.NO_QUOTE_CHARACTER);
            LogicBitSet logicBitSet = new LogicBitSet();

            List<String[]> data = csvReader.readAll();
            for(int n = 0; n < data.size(); ++n){
                String[] line = data.get(n);
                switch (line[0]) {
                    case "X":
                        System.out.println("Channel: " + line[1]);
                        break;

                    case "Second":
                        System.out.println("Units: " + line[0] + " - " + line[1]);
                        break;

                    // Read sample data
                    default:
                        // Sample rate was still not calculated
                        if(sampleRate < 0){
                            try {
                                // Get the time between two sample and there we have the sample time
                                double t1 = Double.valueOf(data.get(n)[0]);
                                double t2 = Double.valueOf(data.get(n + 1)[0]);
                                sampleRate = (long) (1d / (t2 - t1));
                                decoder.setSampleFrequency(sampleRate, channelNumber);
                            }catch (NumberFormatException e){
                                System.err.println("Error - Could not calculate sample rate");
                                return;
                            }
                        }
                        try {
                            double voltage = Double.valueOf(line[1]);
                            logicBitSet.set(sampleCounter++, voltage > lowThreshold);
                        } catch (NumberFormatException ignored){}
                        break;
                }
            }

            decoder.setRawData(logicBitSet, channelNumber);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
