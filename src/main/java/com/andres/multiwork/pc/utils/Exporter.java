package com.andres.multiwork.pc.utils;

import com.protocolanalyzer.api.LogicBitSet;
import com.protocolanalyzer.api.TimePosition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Exporter {

    private boolean exportData = true;
    private boolean exportSamples = false;
    private String date = "";
    private String description = "";
    private String title = "";

    private final String dataSuffix = "-data.txt";
    private final String samplesSuffix = "-samples.txt";

    private String fileName = "exported_data";

    private final LogicBitSet samplesData;
    private final List<TimePosition> decodedData;

    /**
     * Export channels data to the specified file
     * @param samplesData list of bits containing the sampled data
     * @param decodedData list containing the decoded data
     */
    public Exporter(LogicBitSet samplesData, List<TimePosition> decodedData){
        this.samplesData = samplesData;
        this.decodedData = decodedData;
    }

    public void export(){
        exportData();
        exportSamples();
    }

    /**
     * Export samples taken from the logic analyzer to a txt file. First a header is created containing title, date
     *  and description. Then, two columns are created separated using two tabulators ("\t\t"). The left column contains
     *  the sample number starting form 0. The right column contains the character '1' or '0' according to the state
     *  of the channel at the given sample.
     */
    private void exportSamples(){
        if(isExportSamples() && samplesData != null){
            try {

                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName + samplesSuffix)));
                writeHeader(writer);

                // Write bit index and then bit value using char '1' or '0' separated using two tabulators '\t\t'
                for(int n = 0; n < samplesData.length(); n++){
                    char bit = (samplesData.get(n)) ? '1' : '0';
                    writer.write(n + "\t\t" + bit);
                    writer.write('\n');
                }

                writer.flush();
                writer.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    /**
     * Export data decoded from the logic analyzer to a txt file. First a header is created containing title, date
     *  and description. Then, two columns are created separated using three tabulators ("\t\t\t"). The left column contains
     *  the start and end time of the event. The right column contains the decoded event name.
     */
    private void exportData(){
        if(isExportData() && decodedData != null){
            try {

                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName + dataSuffix)));
                writeHeader(writer);

                for(TimePosition data : decodedData){
                    String event = data.getString();
                    double t1 = data.startTime();
                    double t2 = data.endTime();

                    writer.write(timeToLabel(t1) + " -> " + timeToLabel(t2) + "\t\t\t" + event + "\n");
                }

                writer.flush();
                writer.close();
            } catch (IOException e) { e.printStackTrace(); }

        }
    }

    /**
     * Writes a header in the given {@link java.io.BufferedWriter}. It contains a title, date
     *  and description.
     * @param writer {@link java.io.BufferedWriter} to write the header
     * @throws IOException
     */
    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write(title + " - " + date + "\n");
        writer.write(description + "\n");
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

    public boolean isExportData() {
        return exportData;
    }

    public void setExportData(boolean exportData) {
        this.exportData = exportData;
    }

    public boolean isExportSamples() {
        return exportSamples;
    }

    public void setExportSamples(boolean exportSamples) {
        this.exportSamples = exportSamples;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
