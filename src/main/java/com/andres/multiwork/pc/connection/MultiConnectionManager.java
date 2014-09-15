package com.andres.multiwork.pc.connection;

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
    private String currentMode = "";

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

    /** Listener for incoming data from USB and Bluetooth */
    private OnNewDataReceived dataReceiverListener = (data, inputStream, outputStream, source) ->
            notifyListeners(data, inputStream, outputStream, source);

    /** Notify to all registered listener of new incoming data */
    private void notifyListeners(byte[] data, InputStream inputStream, OutputStream outputStream, String source){
        for(OnNewDataReceived listener : listeners){
            listener.onNewDataReceived(data, inputStream, outputStream, source);
        }
    }

    /**
     * Enter to the specified mode (Logic Analyzer, Frecuencimeter, etc)
     * @param id mode
     */
    public void enterMode(String id){
        for(ConnectionManager manager : managers) {
            if(manager.getID().equalsIgnoreCase(id)){
                manager.enterMode();
                currentMode = id;
            }
        }
    }

    /**
     * Exit from the last specified mode (Logic Analyzer, Frecuencimeter, etc)
     */
    public void exitMode(){
        for(ConnectionManager manager : managers) {
            if(manager.getID().equalsIgnoreCase(currentMode)){
                manager.exitMode();
                currentMode = "";
            }
        }
    }

    /**
     * Get {@link ConnectionManager} with the specified ID
     * @param id ID of the manager
     * @return {@link ConnectionManager} with specified ID
     */
    public ConnectionManager getManager(String id){
        for(ConnectionManager manager : managers) {
            if(manager.getID().equalsIgnoreCase(id)) return manager;
        }
        return null;
    }

    /**
     * Connect using Bluetooth
     */
    public void connectByBluetooth(){
        bluetoothConnection = new BluetoothConnection(new BluetoothEvent() {
            @Override
            public void onBluetoothConnected(InputStream inputStream, OutputStream outputStream) {
                managers.add(new LogicAnalyzerManager(inputStream, outputStream, dataReceiverListener));
            }

            @Override
            public void onBluetoothDataReceived(InputStream inputStream, OutputStream outputStream) {
                for(ConnectionManager manager : managers) {
                    if(manager.getID().equalsIgnoreCase(currentMode)) manager.onNewDataReceived(inputStream, outputStream);
                }
            }
        });
        bluetoothConnection.startConnection(bluetoothName);
    }

    public void connectByUSB(){
        //TODO: USB connection
    }

}
