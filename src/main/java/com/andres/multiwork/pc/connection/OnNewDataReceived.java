package com.andres.multiwork.pc.connection;

public interface OnNewDataReceived {

    public void onNewDataReceived(byte[] data, ConnectionManager.CaptureType captureType, ConnectionManager.DeviceType deviceType);

}
