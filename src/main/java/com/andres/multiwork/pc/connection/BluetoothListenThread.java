package com.andres.multiwork.pc.connection;

import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothListenThread implements Runnable {

	private StreamConnection mConnection;
    private BluetoothEvent bluetoothEvent;
    private boolean keepRunning = true;

	public BluetoothListenThread(StreamConnection connection, BluetoothEvent bluetoothEvent) {
        mConnection = connection;
        this.bluetoothEvent = bluetoothEvent;
        new Thread(this).start();
    }

    public void stopConnection(){
        keepRunning = false;
    }

	@Override
	public void run() {
		try {
            System.out.println("Connected!");
            InputStream inputStream = mConnection.openInputStream();
            OutputStream outputStream = mConnection.openOutputStream();

            // Prepare to receive data
            if(bluetoothEvent != null) bluetoothEvent.onBluetoothConnected(inputStream, outputStream);

            while(keepRunning){
                if(inputStream.available() > 0){
                    bluetoothEvent.onBluetoothDataReceived(inputStream, outputStream);
                }
            }
        } catch (Exception e) {
    		e.printStackTrace();
    	}
	}
}
