package com.andres.multiwork.pc.utils;

import com.andres.multiwork.pc.GlobalValues;
import com.protocolanalyzer.api.*;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Decoder {

    private I2CProtocol i2CProtocol;
    private UARTProtocol uartProtocol;
    private SPIProtocol spiProtocol;
    private Clock clockProtocol;

    private XMLConfiguration generalSettings;

    private LogicBitSet[] channelsData = new LogicBitSet[GlobalValues.channelsNumber];
    private ArrayList<List<TimePosition>> decodedData = new ArrayList<>();

    private long sampleFrequency[] = new long[GlobalValues.channelsNumber];

    private static Decoder decoderInstance;

    /**
     * Gets an instance of decoder. Used when Singleton is required. The default {@link org.apache.commons.configuration.XMLConfiguration}
     * used is {@link com.andres.multiwork.pc.GlobalValues#xmlSettings} but this can be changed using
     * {@link com.andres.multiwork.pc.utils.Decoder#setSettings(org.apache.commons.configuration.XMLConfiguration)}
     * @return {@link com.andres.multiwork.pc.utils.Decoder} instance
     */
    public synchronized static Decoder getDecoder(){
        if(decoderInstance == null){
            decoderInstance = new Decoder(GlobalValues.xmlSettings);
            return decoderInstance;
        }
        return decoderInstance;
    }

    public Decoder setSettings(XMLConfiguration settings){
        generalSettings = settings;
        return this;
    }

    /**
     * Creates a Decoder which takes care of decoding received data in byte[] format according to the
     * current channel settings in the {@link org.apache.commons.configuration.XMLConfiguration} object passed.
     * If Singleton is intended use {@link Decoder#getDecoder()}
     * @param settings {@link org.apache.commons.configuration.XMLConfiguration} containing channels settings
     */
    public Decoder(XMLConfiguration settings){
        decoderInstance = this;

        generalSettings = settings;
        int sampleFreq = generalSettings.getInt("sampleRate", 4000000);

        i2CProtocol = new I2CProtocol(sampleFreq);
        uartProtocol = new UARTProtocol(sampleFreq);
        clockProtocol = new Clock(sampleFreq);
        spiProtocol = new SPIProtocol(sampleFreq);

        // Init channels data
        for(int n = 0; n < channelsData.length; ++n){
            channelsData[n] = new LogicBitSet();

            // Just add empty items to the list so we can replace them later
            decodedData.add(new ArrayList<>());
        }

        for(int n = 0; n < sampleFrequency.length; ++n) sampleFrequency[n] = sampleFreq;
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
     * @param channelNumber channel number from which retrieve decoded data from 0 to {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}-1
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
     * @param channelNumber channel number from which retrieve the data from 0 to {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}-1
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
     * Get the sample frequency of the specified channel
     * @param channelNumber channel number from which retrieve frequency, 0 to {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}-1
     * @return sample frequency
     */
    public long getSampleFrequency(int channelNumber){
        return sampleFrequency[channelNumber];
    }

    /**
     * Set the sample frequency of the specified channel
     * @param sampleFrequency sample frequency for the channel
     * @param channelNumber channel number to se frequency, 0 to {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}-1
     */
    public void setSampleFrequency(long sampleFrequency, int channelNumber){
        this.sampleFrequency[channelNumber] = sampleFrequency;
    }

    /**
     * Set the sample frequency for all the channels
     * @param sampleFrequency sample frequency for the channels
     */
    public void setSampleFrequency(long sampleFrequency){
        for(int n = 0; n < this.sampleFrequency.length; ++n) this.sampleFrequency[n] = sampleFrequency;
    }

    /**
     * Decode all the channels with the current settings saved in the {@link org.apache.commons.configuration.XMLConfiguration}
     * object passed in the constructor
     */
    public void decodeAll (){
        long t1 = System.nanoTime();
        for(int n = 0; n < GlobalValues.channelsNumber; ++n){
            decode(n);
        }
        long t2 = System.nanoTime();
        System.out.println(GlobalValues.channelsNumber + " channels decoded in " + (t2-t1)/1000000 + " mS");
    }

    /**
     * Decode the selected channel with the current settings saved in the {@link org.apache.commons.configuration.XMLConfiguration}
     * object passed in the constructor
     * @param channelNumber channel number to be decoded from 0 to {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}-1
     */
    public void decode (final int channelNumber){
        // Clear previous decoded data
        i2CProtocol.getDecodedData().clear();
        uartProtocol.getDecodedData().clear();
        spiProtocol.getDecodedData().clear();

        int protocol = generalSettings.getInt("protocol" + channelNumber, GlobalValues.uartProtocol);
        int sampleRate = (int)sampleFrequency[channelNumber];

        switch (protocol){
            case GlobalValues.i2cProtocol:
                int clockNumber = generalSettings.getInt("clock" + channelNumber, GlobalValues.channelDisabled);
                if(clockNumber == GlobalValues.channelDisabled) return;

                // Set sample rate
                i2CProtocol.setSampleFrequency(sampleRate);
                clockProtocol.setSampleFrequency(sampleRate);

                // Set channels data
                clockProtocol.setChannelBitsData(channelsData[clockNumber]);
                i2CProtocol.setChannelBitsData(channelsData[channelNumber]);
                i2CProtocol.setClockSource(clockProtocol);

                // Decode and store the data. Create a copy of the decoded data List because if decode() method is called
                //  again for another channel we will clear all the decoded data in the channels to decode the new one
                //  and the data in the list will be deleted as well (reference)
                i2CProtocol.decode(0);
                decodedData.set(channelNumber, new ArrayList<>(i2CProtocol.getDecodedData()));
                break;

            case GlobalValues.uartProtocol:
                // Load channel's settings
                int parity = generalSettings.getInt("parity" + channelNumber, GlobalValues.parityNone);
                int baudRate = generalSettings.getInt("baudRate" + channelNumber, 9600);

                boolean nineBitsMode = generalSettings.getBoolean("nineBitsMode" + channelNumber, false);
                boolean twoStopBits = generalSettings.getBoolean("twoStopBits" + channelNumber, false);

                uartProtocol.setBaudRate(baudRate);
                uartProtocol.set9BitsMode(nineBitsMode);
                uartProtocol.setTwoStopBits(twoStopBits);
                uartProtocol.setParity(UARTProtocol.Parity.values()[parity]);

                // Set channel data
                uartProtocol.setChannelBitsData(channelsData[channelNumber]);

                // Decode and store the data
                uartProtocol.decode(0);
                decodedData.set(channelNumber, new ArrayList<>(uartProtocol.getDecodedData()));
                break;

            case GlobalValues.spiProtocol:
                int clockChannel = GlobalValues.xmlSettings.getInt("clockSPI" + channelNumber, GlobalValues.channelDisabled);
                boolean cpol = GlobalValues.xmlSettings.getBoolean("cpolSPI", false);
                boolean cpha = GlobalValues.xmlSettings.getBoolean("cphaSPI", false);

                // We need a clock channel
                if(clockChannel == GlobalValues.channelDisabled) return;

                spiProtocol.setCPOL(cpol);
                spiProtocol.setCPHA(cpha);
                spiProtocol.setClockSource(clockProtocol);

                spiProtocol.setChannelBitsData(channelsData[channelNumber]);
                spiProtocol.setClockSource(clockProtocol);
                clockProtocol.setChannelBitsData(channelsData[clockChannel]);

                // Decode and store the data
                spiProtocol.decode(0);
                decodedData.set(channelNumber, new ArrayList<>(spiProtocol.getDecodedData()));
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
        for (LogicBitSet aList : list) aList.clear();

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
            maxLenght = Math.max(logicBitSet.length(), maxLenght);
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
