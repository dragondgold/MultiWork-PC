package com.andres.multiwork.pc.connection;

import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothListenThread implements Runnable {

	private StreamConnection mConnection;
    private BluetoothEvent bluetoothEvent;

	public BluetoothListenThread(StreamConnection connection, BluetoothEvent bluetoothEvent){
		mConnection = connection;
        this.bluetoothEvent = bluetoothEvent;
	}

	@Override
	public void run() {
		try {
            System.out.println("Connected!");
            InputStream inputStream = mConnection.openInputStream();
            OutputStream outputStream = mConnection.openOutputStream();

            // Prepare to receive data
            if(bluetoothEvent != null) bluetoothEvent.onBluetoothConnected(inputStream, outputStream);

            while(true){
                if(inputStream.available() > 0){
                    bluetoothEvent.onBluetoothDataReceived(inputStream, outputStream);
                }
            }
        } catch (Exception e) {
    		e.printStackTrace();
    	}
	}
}
