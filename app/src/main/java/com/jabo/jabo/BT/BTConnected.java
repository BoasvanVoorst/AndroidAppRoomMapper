package com.jabo.jabo.BT;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.jabo.jabo.roommapper.ConnectTask;
import com.jabo.jabo.roommapper.ControlPage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class BTConnected extends Thread{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public void start(){
        super.start();
        run();
    }

    public BTConnected(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int begin = 0;
        int bytes = 0;
        while (true) {
            try {
                bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                if(buffer[0] == 0x33) {
                    int X = buffer[2]|(buffer[1]<<8);
                    int Y = buffer[4]|(buffer[3]<<8);//3300010001 =x,1 en y,1
                    Log.d("x,y",X+","+Y);
                    if (ConnectTask.mTcpClient != null) {
                        ConnectTask.mTcpClient.sendMessage(X +","+Y+"<DP>");
                    }
                }

                for(int i = 0;i < buffer.length; i++){
                    buffer[i] = 0;
                }
                bytes = 0;
            } catch (IOException e) {
                Log.e("BTConnected","no connection",e);
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("BTC","write",e);
            ControlPage.popup("Cant write to device");
        }
    }

    public void cancel() {
        if (mmSocket != null) {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}

