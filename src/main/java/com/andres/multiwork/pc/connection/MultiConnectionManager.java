package com.andres.multiwork.pc.connection;

import com.andres.multiwork.pc.GlobalValues;
import jvisa.JVisa;
import jvisa.JVisaException;
import jvisa.JVisaReturnString;
import visatype.VisatypeLibrary;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class MultiConnectionManager {

    // Connections
    private BluetoothConnection bluetoothConnection;

    // Managers
    private ArrayList<ConnectionManager> managers = new ArrayList<>();
    // Events
    private ArrayList<OnNewDataReceived> listeners = new ArrayList<>();

    private final String bluetoothName = "linvor";
    private final String instrumentString = "USB0::0x1AB1::0x0588::DS1ET164267347::INSTR";

    /** Listener for incoming data from USB and Bluetooth */
    private OnNewDataReceived dataReceiverListener = this::notifyListeners;
    private ConnectionManager.CaptureType currentCaptureType = null;
    private ConnectionManager.DeviceType currentDeviceType = null;

    public MultiConnectionManager() {
        // Add every ConnectionManager available
        managers.add(new BTLogicAnalyzerManager(null, null, dataReceiverListener));
        managers.add(new RigolManager(dataReceiverListener));
    }

    /**
     * Start a capture with the specified {@link com.andres.multiwork.pc.connection.ConnectionManager.CaptureType}
     */
    public void startCapture(ConnectionManager.CaptureType captureType){
        if(currentDeviceType == null || captureType == null) return;
        currentCaptureType = captureType;

        // Call the startCapture() method of managers with the specified captureType and with the deviceType of
        //  the current connection
        managers.stream().filter(manager -> manager.getCaptureType() == currentCaptureType &&
                                            manager.getDeviceType() == currentDeviceType
                                ).forEach(ConnectionManager::startCapture);
    }

    public byte[] getData(ConnectionManager.CaptureType captureType){
        for(ConnectionManager manager : managers){
            if(manager.getDeviceType() == currentDeviceType && manager.getCaptureType() == currentCaptureType){
                return manager.getData();
            }
        }
        return new byte[0];
    }

    public ConnectionManager.CaptureType getCurrentCaptureType() {
        return currentCaptureType;
    }

    public ConnectionManager.DeviceType getCurrentDeviceType() {
        return currentDeviceType;
    }

    /**
     * Add a listener when new incoming data is available
     * @param onNewDataReceived {@link com.andres.multiwork.pc.connection.OnNewDataReceived} to be added
     */
    public void addDataReceivedListener(OnNewDataReceived onNewDataReceived){
        listeners.add(onNewDataReceived);
    }

    /**
     * Remove the specified listener
     * @param onNewDataReceived {@link com.andres.multiwork.pc.connection.OnNewDataReceived} to remove
     */
    public void removeDataReceivedListener(OnNewDataReceived onNewDataReceived){
        listeners.remove(onNewDataReceived);
    }

    public void closeConnection(){
        bluetoothConnection.closeConnection();
    }

    /** Notify to all registered listener of new incoming data */
    private void notifyListeners(byte[] data, ConnectionManager.CaptureType captureType, ConnectionManager.DeviceType deviceType){
        for(OnNewDataReceived listener : listeners){
            listener.onNewDataReceived(data, captureType, deviceType);
        }
    }

    /**
     * Connect using Bluetooth
     */
    public void connectByBluetooth(){
        bluetoothConnection = new BluetoothConnection(new BluetoothEvent() {
            @Override
            public void onBluetoothConnected(InputStream inputStream, OutputStream outputStream) {
                currentDeviceType = ConnectionManager.DeviceType.BLUETOOTH;
                for(ConnectionManager manager : managers){
                    if(manager.getDeviceType() == currentDeviceType){
                        manager.setStreams(outputStream, inputStream);
                    }
                }
            }

            @Override
            public void onBluetoothDataReceived(InputStream inputStream, OutputStream outputStream) {
                for(ConnectionManager manager : managers){
                    if(manager.getDeviceType() == currentDeviceType && manager.getCaptureType() == currentCaptureType){
                        manager.onNewDataReceived(inputStream, outputStream);
                    }
                }
            }
        });
        bluetoothConnection.startConnection(bluetoothName);
    }

    public void connectByUSB(){
        //TODO: USB connection
        currentDeviceType = ConnectionManager.DeviceType.USB;
    }

    public void connectWithOscilloscope() throws JVisaException{
        JVisa jVisa = new JVisa();
        long status = jVisa.openDefaultResourceManager();
        if(status != VisatypeLibrary.VI_SUCCESS){
            throw new JVisaException("Could not create session for resource manager.");
        }

        status = jVisa.openInstrument(instrumentString);
        if(status != VisatypeLibrary.VI_SUCCESS){
            throw new JVisaException("Could not open instrument session for " + instrumentString);
        }

        jVisa.write("*IDN?");
        JVisaReturnString r = new JVisaReturnString();
        jVisa.read(r);
        String[] scopeInfo = r.returnString.split(",");
        if(scopeInfo.length >= 4) {
            System.out.println("Company: " + scopeInfo[0]);
            System.out.println("Model: " + scopeInfo[1]);
            System.out.println("Serial Number: " + scopeInfo[2]);
            System.out.println("Software Version: " + scopeInfo[3]);
        }else {
            throw new JVisaException("Could not communicate with " + instrumentString);
        }

        currentDeviceType = ConnectionManager.DeviceType.RIGOL_SCOPE;
        managers.stream().filter(manager -> manager.getDeviceType() == currentDeviceType).forEach(manager -> manager.setStreams(jVisa, jVisa));
    }

    public long getCurrentSampleRate(){
        if(getCurrentDeviceType() != ConnectionManager.DeviceType.RIGOL_SCOPE)
            return GlobalValues.xmlSettings.getInt("sampleRate", 4000000);
        else {
            for(ConnectionManager manager : managers){
                if(manager.getDeviceType() == currentDeviceType){
                    return ((RigolManager)manager).getScope().getSampleFrequency();
                }
            }
        }
        return -1;
    }
}
