package com.andres.multiwork.pc.connection;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.IOException;
import java.util.Vector;

public class BluetoothConnection implements Runnable {

    private BluetoothEvent bluetoothEvent;
    private Thread btThread;
    private final Vector<RemoteDevice> devicesDiscovered = new Vector<>();
    private RemoteDevice selectedDevice;
    private String btName;
    private boolean keepRunning = true;

	public BluetoothConnection(BluetoothEvent bluetoothEvent) {
        this.bluetoothEvent = bluetoothEvent;
        btThread = new Thread(this);
    }

    /**
     * Connects with the specified bluetooth device
     * @param name name of the bluetooth device
     */
    public void startConnection(String name){
        btName = name;
        scanForDevices();
    }

    private void scanForDevices(){
        devicesDiscovered.clear();

        DiscoveryListener listener = new DiscoveryListener() {
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                System.out.println("Device MAC: " + btDevice.getBluetoothAddress() + " found");
                devicesDiscovered.addElement(btDevice);

                try {
                    System.out.println("     name " + btDevice.getFriendlyName(false));

                    // Found the bluetooth device, start connecting to it
                    if(btDevice.getFriendlyName(false).equals(btName)){
                        selectedDevice = btDevice;
                        selectedDevice = devicesDiscovered.get(0);
                        btThread.start();
                    }
                } catch (IOException cantGetDeviceName) {}
            }

            public void inquiryCompleted(int discType) {
                System.out.println("Device Inquiry completed!");
                System.out.println(devicesDiscovered.size() +  " device(s) found");
            }

            public void serviceSearchCompleted(int transID, int respCode) {}

            public void servicesDiscovered(int transID, ServiceRecord[] serviceRecord) {}
        };

        try {
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
            if (started) {
                System.out.println("Waiting for device inquiry to complete...");
            }
        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        keepRunning = false;
    }

	@Override
	public void run() {
		connectToDevice();
	}

	/** Wait for connection from devices */
	private void connectToDevice() {
		// Retrieve the local Bluetooth device object
		LocalDevice local;
		StreamConnectionNotifier notifier;
		StreamConnection connection;

		// Setup the server to listen for connection
		try {
			local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);

            // Connect to device in port 3
            String url = "btspp://" + selectedDevice.getBluetoothAddress() + ":3";
            notifier = (StreamConnectionNotifier)Connector.open(url);

        } catch (BluetoothStateException e) {
        	System.out.println("Bluetooth is not turned on!");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// Wait for connection
        BluetoothListenThread processThread = null;
		while(keepRunning) {
			try {
				System.out.println("Waiting for connection...");
	            connection = notifier.acceptAndOpen();

                processThread = new BluetoothListenThread(connection, bluetoothEvent);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
        if(processThread != null) processThread.stopConnection();
	}
}
