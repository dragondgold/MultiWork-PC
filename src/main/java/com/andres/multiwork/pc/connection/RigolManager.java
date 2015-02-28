package com.andres.multiwork.pc.connection;

import com.andres.multiwork.pc.GlobalValues;
import com.andres.multiwork.pc.utils.Utils;
import com.andres.rigol.RigolOscilloscope;
import jvisa.JVisa;

public class RigolManager implements ConnectionManager<JVisa, JVisa> {

    private final OnNewDataReceived onNewDataReceived;
    private RigolOscilloscope scope;
    private byte[] sampledData = new byte[0];
    private JVisa jVisa;

    public RigolManager(OnNewDataReceived onNewDataReceived) {
        this.onNewDataReceived = onNewDataReceived;
    }

    @Override
    public void startCapture() {
        if(scope != null){
            scope.stopSampling();
            scope.setPointMode(RigolOscilloscope.PointMode.RAW);
            sampledData = scope.getChannelData(1);
            scope.releaseScope();

            double lowerThreshold = GlobalValues.xmlSettings.getDouble("rigolFromVoltage", 0);
            double upperThreshold = GlobalValues.xmlSettings.getDouble("rigolToVoltage", 0);
            byte ch1[] = null, ch2[] = null;

            if(GlobalValues.xmlSettings.getBoolean("rigolCH1Enable", false)) {
                double voltageOffset = scope.getVoltageOffset(1);
                double voltageScale = scope.getVoltageScale(1);

                ch1 = Utils.rigolBytesToBits(sampledData, voltageOffset, voltageScale, lowerThreshold, upperThreshold, 0);
                int logic1 = 0, logic0 = 0;
                for(byte b : ch1){
                    if(b == 0) ++logic0;
                    else ++logic1;
                }
                System.out.println("CH1 -> Logic 0: " + logic0 + " - Logic 1: " + logic1);
            }

            if(GlobalValues.xmlSettings.getBoolean("rigolCH2Enable", false)) {
                double voltageOffset = scope.getVoltageOffset(2);
                double voltageScale = scope.getVoltageScale(2);

                ch2 = Utils.rigolBytesToBits(sampledData, voltageOffset, voltageScale, lowerThreshold, upperThreshold, 1);
                int logic1 = 0, logic0 = 0;
                for(byte b : ch2){
                    if(b == 0) ++logic0;
                    else ++logic1;
                }
                System.out.println("CH2 -> Logic 0: " + logic0 + " - Logic 1: " + logic1);
            }

            if(ch1 != null && ch2 != null){
                byte result[] = Utils.combineByteBuffers(ch1, ch2);
                onNewDataReceived.onNewDataReceived(result, getCaptureType(), getDeviceType());
            }
            else if(ch1 != null) onNewDataReceived.onNewDataReceived(ch1, getCaptureType(), getDeviceType());
            else onNewDataReceived.onNewDataReceived(ch2, getCaptureType(), getDeviceType());
        }
    }

    public RigolOscilloscope getScope(){
        return scope;
    }

    @Override
    public void onNewDataReceived(JVisa input, JVisa output) {

    }

    @Override
    public byte[] getData() {
        return sampledData;
    }

    @Override
    public void setStreams(JVisa outputStream, JVisa inputStream) {
        this.jVisa = outputStream;
        scope = new RigolOscilloscope(outputStream);
        scope.releaseScope();
    }

    @Override
    public JVisa getInputStream() {
        return jVisa;
    }

    @Override
    public JVisa getOutputStream() {
        return jVisa;
    }

    @Override
    public CaptureType getCaptureType() {
        return CaptureType.LOGIC_ANALYZER;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.RIGOL_SCOPE;
    }
}
