package com.andres.multiwork.pc.connection;

import java.io.InputStream;
import java.io.OutputStream;

public interface Manager {

    /**
     * Starts a capture from the hardware, example: logic analyzer, frecuencimeter, etc.
     */
    public void startCapture();

    /**
     * Used when new data arrives from whatever protocol we choose
     * @param inputStream
     * @param outputStream
     */
    public void onNewDataReceived(InputStream inputStream, OutputStream outputStream);

    /**
     * Indicates what mode we are entering to the hardware
     */
    public void enterMode();

    /**
     * Indicates we are leaving the current mode
     */
    public void exitMode();
}
