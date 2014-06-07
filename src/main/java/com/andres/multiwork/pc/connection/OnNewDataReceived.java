package com.andres.multiwork.pc.connection;

import java.io.InputStream;
import java.io.OutputStream;

public interface OnNewDataReceived {

    public void onNewDataReceived(byte[] data, InputStream inputStream, OutputStream outputStream, String source);

}
