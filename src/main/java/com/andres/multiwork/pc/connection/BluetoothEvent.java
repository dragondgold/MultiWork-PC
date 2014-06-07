package com.andres.multiwork.pc.connection;

import java.io.InputStream;
import java.io.OutputStream;

public interface BluetoothEvent {

    public void onBluetoothConnected(InputStream inputStream, OutputStream outputStream);

    public void onBluetoothDataReceived(InputStream inputStream, OutputStream outputStream);

}
