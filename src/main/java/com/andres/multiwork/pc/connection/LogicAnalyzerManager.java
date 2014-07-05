package com.andres.multiwork.pc.connection;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.CRC16;
import com.protocolanalyzer.api.LogicHelper;
import com.protocolanalyzer.api.utils.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LogicAnalyzerManager implements ConnectionManager {

    private static final byte F40MHz =  'A';
    private static final byte F20MHz =  'S';
    private static final byte F10MHz =  'D';
    private static final byte F4MHz =   'F';
    private static final byte F400KHz = 'G';
    private static final byte F2KHz =   'H';
    private static final byte F10Hz =   'J';

    private static final byte START_BYTE = 'S';
    private static final byte RETRY_BYTE = 'R';

    private static final byte LOGIC_ANALYZER_MODE = 'L';
    private static final byte EXIT = 0;

    // Time in mS to abort data waiting. Timeout is timeOutLimit*30mS
    private static final int timeOutLimit = 67;

    private boolean receivedModeResponse = false;
    private byte[] dataBuffer;
    private OnNewDataReceived onDataReceived;

    private InputStream inputStream;
    private OutputStream outputStream;

    public LogicAnalyzerManager(InputStream inputStream, OutputStream outputStream, OnNewDataReceived onNewDataReceived) {
        onDataReceived = onNewDataReceived;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void startCapture() {
        try {
            outputStream.write(1);
            int sampleRate = GlobalValues.xmlSettings.getInt("sampleRate", 4000000);

            // Send sample frequency
            if(sampleRate == 40000000)      outputStream.write(F40MHz);
            else if(sampleRate == 20000000) outputStream.write(F20MHz);
            else if(sampleRate == 10000000) outputStream.write(F10MHz);
            else if(sampleRate == 4000000)  outputStream.write(F4MHz);
            else if(sampleRate == 400000)   outputStream.write(F400KHz);
            else if(sampleRate == 2000)     outputStream.write(F2KHz);
            else if(sampleRate == 10)       outputStream.write(F10Hz);

            // Trigger usage
            outputStream.write(GlobalValues.xmlSettings.getBoolean("simpleTriggerGeneral", false) ? 'S' : 'N');

            // Mask
            byte triggerMask = buildTriggerMask();
            outputStream.write(triggerMask);

            System.out.println("Parameters sent, now wait for response!");
            System.out.println("triggerMask: " + Integer.toBinaryString(triggerMask));
            System.out.println("simpleTriggerGeneral: " + GlobalValues.xmlSettings.getBoolean("simpleTriggerGeneral", false));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewDataReceived(InputStream input, OutputStream output) {
        handleReceivedData();
    }

    @Override
    public void enterMode() {
        try {
            outputStream.write(LOGIC_ANALYZER_MODE);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void exitMode() {
        try {
            outputStream.write(EXIT);
            receivedModeResponse = false;
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public String getID() {
        return "Logic Analyzer";
    }

    public byte[] getDataBuffer(){
        return dataBuffer;
    }

    private void handleReceivedData(){
        // If we haven't yet received the response of entering in logic analyzer mode we test it now
        if(!receivedModeResponse){
            try {
                while(inputStream.available() > 0){
                    if(inputStream.read() == LOGIC_ANALYZER_MODE){
                        receivedModeResponse = false;
                        break;
                    }
                }
                if(receivedModeResponse)
                    System.out.println("Logic Analyzer Mode ERROR");
            } catch (IOException e) { e.printStackTrace(); }
        }
        // Otherwise read the incoming data
        else {
            try {
                int[] data = new int[3];
                boolean retry = false;

                while(inputStream.available() > 0 || retry){
                    // The first bytes are Start and Mode bytes
                    if(inputStream.read() == START_BYTE && inputStream.read() == LOGIC_ANALYZER_MODE){
                        System.out.println("Receiving data...");
                        boolean keepGoing = true;
                        ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(10);

                        while(keepGoing){
                            int timeOutCounter = 0;
                            // Wait for data and count time until timeout
                            while(!(inputStream.available() > 0)){
                                // Timeout!
                                if(timeOutCounter >= timeOutLimit){
                                    // TODO: notify user
                                    System.out.println("Data waiting timeout");
                                    return;
                                }

                                try { Thread.sleep(30); }
                                catch (InterruptedException e) { e.printStackTrace(); }

                                ++timeOutCounter;
                            }
                            System.out.println("Finished waiting data");

                            // Read data bytes now until we receive two 0xFF bytes which indicate end of transmission
                            for(int n = 0; n < data.length; ++n){
                                data[n] = inputStream.read();
                                if(n == 1 && data[0] == 0xFF && data[1] == 0xFF){
                                    keepGoing = false;
                                    System.out.println("Finished receiving data");
                                    break;
                                }
                            }
                            if(keepGoing){
                                byteArrayBuffer.append(data[0]);
                                byteArrayBuffer.append(data[1]);
                                byteArrayBuffer.append(data[2]);
                            }
                        }

                        // Read CRC16 checksum bytes
                        int CRC16L = inputStream.read();
                        int CRC16H = inputStream.read();
                        int CRC16Checksum = LogicHelper.byteToInt((byte) CRC16L, (byte) CRC16H);

                        System.out.println("Received data length: " + byteArrayBuffer.length());

                        // Decode compressed data with Run Length algorithm
                        dataBuffer = LogicHelper.runLengthDecode(byteArrayBuffer);
                        System.out.println("Received data full length: " + dataBuffer.length);

                        // Check if checksum matches
                        int calculatedCRC16 = CRC16.calculateCRC(dataBuffer);
                        if(calculatedCRC16 == CRC16Checksum){
                            System.out.println("CRC16 matches: " + calculatedCRC16);
                            retry = false;
                        }
                        // Checksum doesn't match, retry data transfer
                        else{
                            System.out.println("CRC16 does NOT match. Received: " + calculatedCRC16
                                    + " - Calculated: " + calculatedCRC16);
                            outputStream.write(RETRY_BYTE);
                            retry = true;
                        }

                        if(!retry){
                            onDataReceived.onNewDataReceived(dataBuffer, inputStream, outputStream, "LogicAnalyzer");
                            return;
                        }
                    }
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private byte buildTriggerMask(){
        byte mask = 0;
        for(int n = 0; n < GlobalValues.channelsNumber; ++n){
            mask = LogicHelper.bitSet(mask, GlobalValues.xmlSettings.getBoolean("simpleTrigger" + n), n);
        }
        return mask;
    }
}
