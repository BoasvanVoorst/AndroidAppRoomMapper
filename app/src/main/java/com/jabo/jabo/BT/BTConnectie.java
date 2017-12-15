package com.jabo.jabo.BT;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static com.jabo.jabo.roommapper.Menu.mBluetoothAdapter;

public class BTConnectie extends Thread {
    private BluetoothDevice mmDevice;
    private BluetoothSocket mmSocket;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard SerialPortService ID
    public static BTConnected mConnectedThread = null;
    private boolean run = false;

    public BTConnectie(BluetoothDevice device) throws Exception {
        BluetoothSocket tmp = null;
        mmDevice = device;
        tmp = device.createRfcommSocketToServiceRecord(uuid);
        mmSocket = tmp;
    }

    public void run(){
        run = true;
        mBluetoothAdapter.cancelDiscovery();
        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        try {
            mConnectedThread = new BTConnected(mmSocket);
            mConnectedThread.start();
        }
        catch (Exception e){
            Log.e("BTConnectie","RUN",e);
            mConnectedThread = null;
        }
        run = false;
    }

    public void cancel(){
        if(run) {
            if(mConnectedThread != null) {
                try {
                    mConnectedThread.cancel();
                    mmSocket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}