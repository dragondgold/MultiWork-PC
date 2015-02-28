package com.andres.multiwork.pc.utils;

import com.protocolanalyzer.api.LogicHelper;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Convert an String to the corresponding voltage. Valid strings are for example 20mv, 50v, 60uv, etc.
     * The conversion is case insensitive.
     * @param string string to convert
     * @return voltage in volts converted, it's equal to Double.MAX_VALUE if an error ocurred.
     */
    public static double stringToVoltage(String string){
        double unit;
        double voltage;
        // Regex to match any valid string, 20mv, 50v, 60uv, 20.5mv, etc. It's case insensitive
        if(string.matches("(?i)(\\d+(\\.\\d{1,2})?)(mv|uv|v)\\b(?<=\\w)")){
            // See if the string contains mv case insensitive. The contains() method is case sensitive
            if(Pattern.compile(Pattern.quote("mv"), Pattern.CASE_INSENSITIVE).matcher(string).find()) unit = 1E-3;
            else if(Pattern.compile(Pattern.quote("uv"), Pattern.CASE_INSENSITIVE).matcher(string).find()) unit = 1E-6;
            else if(Pattern.compile(Pattern.quote("v"), Pattern.CASE_INSENSITIVE).matcher(string).find()) unit = 1E-0;
            else return Double.MAX_VALUE;

            voltage = Double.parseDouble(string.split("(mv|uv|v)\\b(?<=\\w)")[0]);

            return voltage*unit;
        }
        return Double.MAX_VALUE;
    }

    public static String voltageToString(double voltage){
        DecimalFormat df = new DecimalFormat("#.00");
        if(voltage < 1000E-6){
            return df.format(voltage*1E6) + "uv";
        } else if(voltage < 1000E-3){
            return df.format(voltage*1E3) + "mv";
        } else return df.format(voltage) + "v";
    }

    /**
     * Converts bytes received from Rigol oscilloscope to actual voltage values.
     * Taken from http://www.cibomahto.com/2010/04/controlling-a-rigol-oscilloscope-using-linux-and-python/
     * @param buffer buffer containing the bytes from the oscilloscope
     * @param voltageOffset voltage offset in volts
     * @param voltageScale voltage scale in volts
     * @return double[] array containing the voltage values
     */
    public static double[] rigolBytesToVoltage(byte[] buffer, double voltageOffset, double voltageScale){
        double[] waveform = new double[buffer.length];

        for(int n = 0; n < buffer.length; ++n){
            // Invert the data
            waveform[n] = (double)buffer[n] * -1 + 255;
            // Now, we know from experimentation that the scope display range is actually
            // 30-229. So shift by 130 - the voltage offset in counts, then scale to
            // get the actual voltage.
            waveform[n] = (waveform[n] - 130.0 - voltageOffset/voltageScale*25) / (25*voltageScale);
        }

        return waveform;
    }

    /**
     * Converts bytes received from Rigol oscilloscope to logic levels '1' and '0'. For example, if we pass
     *   channelNumber as 0 so the bit 0 in each byte will represent the logic state at that point.
     * @param buffer buffer containing the bytes from the oscilloscope
     * @param voltageOffset voltage offset in volts
     * @param voltageScale voltage scale in volts
     * @param lowerThreshold lower voltage threshold
     * @param upperThreshold higher voltage threshold
     * @param channelNumber channel number which defines which bit in the byte is modified. Goes from 0 to
     *                      {@link com.andres.multiwork.pc.GlobalValues#channelsNumber}-1
     */
    public static byte[] rigolBytesToBits(byte[] buffer, double voltageOffset, double voltageScale, double lowerThreshold,
                                          double upperThreshold, int channelNumber){
        byte[] result = new byte[buffer.length];
        double voltage;
        for(int n = 0; n < buffer.length; ++n){
            // Invert the data
            voltage = ((125 - buffer[n]) * 5 * voltageScale / 128) - voltageOffset;

            result[n] = 0;
            result[n] = LogicHelper.bitSet(result[n], voltage > lowerThreshold && voltage < upperThreshold, channelNumber);
        }
        return result;
    }

    /**
     * Combine multiple byte[] buffers into a single one. The max number of buffer is 8.
     * All buffer must have the same length and only have a single bit set at most representing
     *  the state of the channel, that is, each buffer represents a single channel, otherwise the
     *  buffer combination won't be as expected.
     * @param buffers byte[] buffers to combine
     * @throws java.lang.IllegalArgumentException if there are more than 8 buffers
     * @return combined byte[] buffer
     */
    public static byte[] combineByteBuffers(byte[] ... buffers){
        if(buffers.length > 8)
            throw new IllegalArgumentException("There can not be more than 8 buffers. There are " + buffers.length + " currently.");

        byte[] result = new byte[buffers[0].length];
        for(int byteIndex = 0; byteIndex < buffers[0].length; ++byteIndex) {
            for (byte[] buffer : buffers) {
                result[byteIndex] |= buffer[byteIndex];
            }
        }
        return result;
    }
}
