package com.andres.multiwork.pc.utils;

import com.andres.multiwork.pc.GlobalValues;
import com.protocolanalyzer.api.*;
import com.protocolanalyzer.api.utils.Configuration;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Decoder {

    private final int id = 1;

    private I2CProtocol i2CProtocol;
    private UARTProtocol uartProtocol;
    private Clock clockProtocol;

    private Configuration channelsConfigurations = new Configuration();
    private XMLConfiguration generalSettings;

    private LogicBitSet[] channelsData = new LogicBitSet[GlobalValues.channelsNumber];
    private ArrayList<List<TimePosition>> decodedData = new ArrayList<List<TimePosition>>();

    /**
     * Creates a Decoder which takes care of decoding received data in byte[] format according to the
     * current channel settings in the {@link org.apache.commons.configuration.XMLConfiguration} object passed
     * @param settings {@link org.apache.commons.configuration.XMLConfiguration} containing channels settings
     */
    public Decoder(XMLConfiguration settings){
        generalSettings = settings;
        int sampleFreq = GlobalValues.xmlSettings.getInt("sampleRate", 4000000);

        i2CProtocol = new I2CProtocol(sampleFreq, channelsConfigurations, id);
        uartProtocol = new UARTProtocol(sampleFreq, channelsConfigurations, id);
        clockProtocol = new Clock(sampleFreq, channelsConfigurations, id);

        // Init channels data
        for(int n = 0; n < channelsData.length; ++n){
            channelsData[n] = new LogicBitSet();
        }
    }

    /**
     * Define the byte[] array containing the data from the logic analyzer's channels
     * @param data byte[] array containing the data from the logic analyzer's channels
     */
    public void setData(byte[] data){
        bufferToLogicBitSet(data, channelsData);
    }

    /**
     * Gets the decoded data from the selected channel
     * @param channelNumber channel number from which retrieve decoded data
     * @return {@link java.util.List} containing decoded data in every {@link com.protocolanalyzer.api.TimePosition} object
     */
    public List<TimePosition> getDecodedData(int channelNumber){
        return decodedData.get(channelNumber);
    }

    /**
     * Gets the decoded data from all the channels including the disabled ones
     * @return {@link java.util.ArrayList} containing {@link java.util.List} with {@link com.protocolanalyzer.api.TimePosition}
     * with the decoded data
     */
    public ArrayList<List<TimePosition>> getAllDecodedData(){
        return decodedData;
    }

    /**
     * Gets the raw data (bits from the logic analyzer) from the specified channel.
     * @param channelNumber channel number from which retrieve the data
     * @return {@link com.protocolanalyzer.api.LogicBitSet} containing the bits of the specified channel
     */
    public LogicBitSet getRawData(int channelNumber){
        return channelsData[channelNumber];
    }

    /**
     * Gets the raw data (bits from the logic analyzer) from all the channels.
     * @return array of {@link com.protocolanalyzer.api.LogicBitSet} containing the bits of all the channels
     */
    public LogicBitSet[] getAllRawData(){
        return channelsData;
    }

    /**
     * Gets the maximum number of samples of all the channels
     * @return maximum number of samples of all the channels
     */
    public int getMaxSamplesNumber(){
        int maxSamples = channelsData[0].length();
        for(LogicBitSet logicBitSet : channelsData){
            maxSamples = Math.max(maxSamples, logicBitSet.length());
        }

        return maxSamples;
    }

    /**
     * Gets the sample frequency of the last decoded data
     * @return sample frequency in Hertz of the last decoded data
     */
    public long getSampleFrequency(){
        return i2CProtocol.getSampleFrequency();
    }

    /**
     * Decode all the channels with the current settings saved in the {@link org.apache.commons.configuration.XMLConfiguration}
     * object passed in the constructor
     */
    public void decodeAll (){
        for(int n = 0; n < GlobalValues.channelsNumber; ++n){
            decode(n);
        }
    }

    /**
     * Decode the selected channel with the current settings saved in the {@link org.apache.commons.configuration.XMLConfiguration}
     * object passed in the constructor
     * @param channelNumber channel number to be decoded
     */
    public void decode (int channelNumber){
        int protocol = generalSettings.getInt("protocol" + channelNumber, GlobalValues.uartProtocol);
        int sampleRate = generalSettings.getInt("sampleRate", 4000000);

        switch (protocol){
            case GlobalValues.i2cProtocol:
                int clockNumber = generalSettings.getInt("clock" + channelNumber);

                // Set sample rate
                i2CProtocol.setSampleFrequency(sampleRate);
                clockProtocol.setSampleFrequency(sampleRate);

                // Set channels data
                clockProtocol.setChannelBitsData(channelsData[clockNumber]);
                i2CProtocol.setChannelBitsData(channelsData[channelNumber]);
                i2CProtocol.setClockSource(clockProtocol);

                // Decode and store the data
                i2CProtocol.decode(0);
                try {
                    if (decodedData.get(channelNumber) != null) {
                        decodedData.set(channelNumber, i2CProtocol.getDecodedData());
                    }
                } catch (IndexOutOfBoundsException e){
                    decodedData.add(channelNumber, i2CProtocol.getDecodedData());
                }
                break;

            case GlobalValues.uartProtocol:
                // Load channel's settings
                int parity = generalSettings.getInt("parity" + channelNumber, GlobalValues.parityNone);
                int baudRate = generalSettings.getInt("baudRate" + channelNumber, 9600);

                boolean nineBitsMode = generalSettings.getBoolean("nineBitsMode" + channelNumber, false);
                boolean twoStopBits = generalSettings.getBoolean("twoStopBits" + channelNumber, false);

                channelsConfigurations.setProperty("BaudRate" + id, baudRate);
                channelsConfigurations.setProperty("nineData" + id, nineBitsMode);
                channelsConfigurations.setProperty("dualStop" + id, twoStopBits);
                channelsConfigurations.setProperty("Parity" + id, parity);

                // Set channel data
                uartProtocol.setChannelBitsData(channelsData[channelNumber]);

                // Decode and store the data
                uartProtocol.decode(0);
                try {
                    if (decodedData.get(channelNumber) != null) {
                        decodedData.set(channelNumber, uartProtocol.getDecodedData());
                    }
                } catch (IndexOutOfBoundsException e){
                    decodedData.add(channelNumber, uartProtocol.getDecodedData());
                }
                break;

            default:
                break;
        }
    }

    /**
     * Transforms the data from the byte buffer to individual LogicBitSet for each channel.
     * @param data is a byte array which holds the data received from the logic analyzer device. Bit 0 of each byte
     *             represents the data in channel 0, bit 1 data in channel 1 and so on.
     */
    private void bufferToLogicBitSet (final byte[] data, LogicBitSet[] list) {

        // Clear BitSet
        for(int n=0; n < list.length; ++n) list[n].clear();

        for(int n=0; n < data.length; ++n){						// Go through received bytes
            for(int bit=0; bit < list.length; ++bit){			// Go through each "channel"
                if(LogicHelper.bitTest(data[n], bit)){			// Bit is '1'
                    list[bit].set(n);
                }
                else{											// Bit is '0'
                    list[bit].clear(n);
                }
            }
        }
    }

    public static byte[] bitSetToBuffer (final LogicBitSet... data){
        if (data.length == 0) return new byte[0];

        int maxLenght = data[0].length();
        for(LogicBitSet logicBitSet : data){
            maxLenght = Math.max(data.length, maxLenght);
        }

        byte[] buffer = new byte[maxLenght];

        for(int n = 0; n < data.length; ++n){
            for(int b = 0; b < data[n].length(); ++b){
                byte channelByte = LogicHelper.bitSet(buffer[b], data[n].get(b), n);
                buffer[b] = channelByte;
            }
        }

        return buffer;
    }
}
