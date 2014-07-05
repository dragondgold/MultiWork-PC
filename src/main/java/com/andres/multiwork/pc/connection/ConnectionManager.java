package com.andres.multiwork.pc.connection;

import java.io.InputStream;
import java.io.OutputStream;

public interface ConnectionManager {

    /**
     * Starts a capture from the hardware, example: logic analyzer, frecuencimeter, etc.
     */
    public void startCapture();

    /**
     * Used when new data arrives from whatever protocol we choose
     */
    public void onNewDataReceived(InputStream input, OutputStream output);

    /**
     * Indicates what mode we are entering to the hardware
     */
    public void enterMode();

    /**
     * Indicates we are leaving the current mode
     */
    public void exitMode();

    public String getID();
}
