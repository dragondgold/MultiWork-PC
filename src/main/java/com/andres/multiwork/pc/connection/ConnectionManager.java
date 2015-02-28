package com.andres.multiwork.pc.connection;

public interface ConnectionManager<I, O> {

    public enum CaptureType {
        LOGIC_ANALYZER, FRECUENCIMETER
    }

    public enum DeviceType {
        USB, BLUETOOTH, RIGOL_SCOPE, ANOTHER_SCOPE
    }

    /**
     * Starts a capture from the hardware, example: logic analyzer, frecuencimeter, etc.
     */
    public void startCapture();

    /**
     * Used when new data arrives from whatever protocol we choose
     */
    public void onNewDataReceived(I input, O output);

    public byte[] getData();

    public void setStreams(O outputStream, I inputStream);

    public I getInputStream();

    public O getOutputStream();

    public CaptureType getCaptureType();

    public DeviceType getDeviceType();
}
