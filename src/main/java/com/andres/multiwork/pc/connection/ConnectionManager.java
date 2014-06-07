package com.andres.multiwork.pc.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ConnectionManager {

    // Connections
    private BluetoothConnection bluetoothConnection;

    // Managers
    private LogicAnalyzerManager logicAnalyzerManager;

    // Event
    private ArrayList<OnNewDataReceived> listeners = new ArrayList<>();

    public void addDataReceivedListener(OnNewDataReceived onNewDataReceived){
        listeners.add(onNewDataReceived);
    }

    public void removeDataReceivedListener(OnNewDataReceived onNewDataReceived){
        listeners.remove(onNewDataReceived);
    }

    public void connectByBluetooth(){
        bluetoothConnection = new BluetoothConnection(new BluetoothEvent() {
            @Override
            public void onBluetoothConnected(InputStream inputStream, OutputStream outputStream) {
                logicAnalyzerManager = new LogicAnalyzerManager(inputStream, outputStream, logicAnalyzerReceiver);
            }

            @Override
            public void onBluetoothDataReceived(InputStream inputStream, OutputStream outputStream) {
                logicAnalyzerManager.onNewDataReceived(inputStream, outputStream);
            }
        });
        bluetoothConnection.startConnection("linvor");
    }

    private OnNewDataReceived logicAnalyzerReceiver = new OnNewDataReceived() {
        @Override
        public void onNewDataReceived(byte[] data, InputStream inputStream, OutputStream outputStream, String source) {
            notifyListeners(data, inputStream, outputStream, source);
        }
    };

    public void notifyListeners(byte[] data, InputStream inputStream, OutputStream outputStream, String source){
        for(OnNewDataReceived listener : listeners){
            listener.onNewDataReceived(data, inputStream, outputStream, source);
        }
    }

    public LogicAnalyzerManager getLogicAnalyzerManager(){
        return logicAnalyzerManager;
    }

    public void connectByUSB(){
        //TODO: USB connection
    }

}
