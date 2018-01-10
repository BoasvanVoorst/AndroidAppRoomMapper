package com.jabo.jabo.BT;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.jabo.jabo.roommapper.ConnectTask;
import com.jabo.jabo.roommapper.ControlPage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class BTConnected extends Thread{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private static Handler mmHandler;
    private final String TAG = "BTConnected";

    public static final int ENGINE_ON = 1;
    public static final int ENGINE_OFF = 0;
    public static final int UPDATE_SENSOR = 2;
    public static final int CONNECTION_FAILED = 3;
    public static final int CONNECTED = 4;


    public void start(){
        super.start();
        run();
    }

    public BTConnected(BluetoothSocket socket, Handler handler) {
        mmHandler = handler;
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

        int begin = 0;
        int bytes = 0;
        while (true) {
            try {
                if(mmInStream.available()> 0){
                    byte[] buffer = new byte[mmInStream.available()];
                    bytes = mmInStream.read(buffer);
                    if(bytes > 5) {
                        if ((buffer[0] & 0b10000000) == 0b10000000) {
                            Log.d(TAG, "run: motor on");
                            //new ControlPage().EngineOn(true);
                            mmHandler.obtainMessage(ENGINE_ON).sendToTarget();
                        } else {
                            Log.d(TAG, "run: motor off");
                            mmHandler.obtainMessage(ENGINE_OFF).sendToTarget();
                            //new ControlPage().EngineOn(false);
                        }
                        if ((buffer[0] & 0b00000111) != 0) {
                            if (buffer[1] > 80) {
                                mmHandler.obtainMessage(UPDATE_SENSOR, Color.GRAY, (buffer[0] & 0b00000111)).sendToTarget();
                                //new ControlPage().updateSensor(Color.GRAY,1);
                            }
                            if (buffer[1] > 40) {
                                mmHandler.obtainMessage(UPDATE_SENSOR, Color.GREEN, (buffer[0] & 0b00000111)).sendToTarget();
                                //new ControlPage().updateSensor(Color.GREEN,1);
                            }
                            if (buffer[1] > 20) {
                                mmHandler.obtainMessage(UPDATE_SENSOR, Color.YELLOW, (buffer[0] & 0b00000111)).sendToTarget();
                                //new ControlPage().updateSensor(Color.YELLOW,1);
                            }
                            if (buffer[1] > 10) {
                                mmHandler.obtainMessage(UPDATE_SENSOR, Color.RED, (buffer[0] & 0b00000111)).sendToTarget();
                                //new ControlPage().updateSensor(Color.RED,1);
                            }
                        }

                        int X = buffer[4] | (buffer[3] << 8);
                        Log.d(TAG, "run: X" + X);
                        int Direction = buffer[5];
                        Log.d(TAG, "run: Direction " + Direction);

                        if (ConnectTask.mTcpClient != null) {
                            ConnectTask.mTcpClient.sendMessage(X + "," + Direction + "<DP>");
                        }
                        bytes = 0;
                        for (int c = 0; c < buffer.length; c++) {
                            buffer[c] = 0;
                        }
                    }
                }

            } catch (IOException e) {
                Log.e("BTConnected","no connection",e);
                mmHandler.obtainMessage(CONNECTION_FAILED).sendToTarget();
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

