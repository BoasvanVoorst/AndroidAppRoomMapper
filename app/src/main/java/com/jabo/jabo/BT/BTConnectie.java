package com.jabo.jabo.BT;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static com.jabo.jabo.roommapper.Menu.mBluetoothAdapter;

public class BTConnectie extends Thread {
    private final String TAG = "BTConnectie";
    private BluetoothDevice mmDevice;
    private BluetoothSocket mmSocket;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard SerialPortService ID
    public static BTConnected mConnectedThread = null;
    private boolean run = false;
    private static Handler mmHandler;

    public BTConnectie(BluetoothDevice device,Handler handler) throws Exception {
        BluetoothSocket tmp = null;
        mmDevice = device;
        tmp = device.createRfcommSocketToServiceRecord(uuid);
        mmSocket = tmp;
        mmHandler = handler;
    }

    public void run(){
        run = true;
        mBluetoothAdapter.cancelDiscovery();
        do {
            if(mmSocket != null) {
                try {
                    mmSocket.connect();
                } catch (IOException connectException) {
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        Log.e(TAG, "run: ", closeException);
                    }
                    mmHandler.obtainMessage(BTConnected.CONNECTION_FAILED).sendToTarget();
                    return;
                }
                try {
                    mConnectedThread = new BTConnected(mmSocket, mmHandler);
                    mConnectedThread.start();
                    mmHandler.obtainMessage(BTConnected.CONNECTED).sendToTarget();
                } catch (Exception e) {
                    Log.e(TAG, "run: ", e);
                    mConnectedThread = null;
                }
                run = false;
            }
            else{
                BluetoothSocket tmp = null;
                try {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
                }
                catch (IOException e){

                }
                mmSocket = tmp;
            }
        }while(mmSocket == null);
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