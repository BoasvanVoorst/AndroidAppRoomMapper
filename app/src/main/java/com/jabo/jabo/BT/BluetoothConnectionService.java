package com.jabo.jabo.BT;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.jabo.jabo.roommapper.ConnectTask;
import com.jabo.jabo.roommapper.ControlPage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.android.gms.internal.zzagz.runOnUiThread;
import static com.jabo.jabo.roommapper.ControlPage.updateSensor;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ"; // debug tag

    private static final String appName = "JaBo";   // appname for connection

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // application uuid

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mInsecureAcceptThread;
    private ConnectedThread mConnectedThread;
    private ConnectThread mConnectThread;

    private BluetoothDevice mmDevice;

    private float cX = 0;
    private float cY = 0;

    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // gets the bluetooth adapter
        start();    // starts application
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = mmServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            // if the socket is created then do start the connected thread
            if(socket != null){
                connected(socket,mmDevice);
            }

            Log.i(TAG, "END mAcceptThread ");
        }

        // cancels the thread
        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE );
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                // updates the bluetooth image on ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ControlPage.BTON(true);
                    }
                });

                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }

                // updates the bluetooth image on ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ControlPage.BTON(false);
                    }
                });
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }

            connected(mmSocket,mmDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }



    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please Wait...",true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean mRun = true;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream
            byte[] newbuffer = new byte[1024];
            int bytes = 0; // bytes returned from read()
            int count = 0;
            int sensor = 0;
            int cDegree = 0;
            int X = 1;
            int Y = 0;
            float stapgrote = 10; // in cm
            float diagonalExtra = 4;
            Lock l = new ReentrantLock();

            // Keep listening to the InputStream until an exception occurs
            while (mRun) {
                l.lock();
                try{
                    // Read from the InputStream
                    try {
                        int bytesAvailable = mmInStream.available();
                        if (bytesAvailable >= 1) {
                            try {
                                bytes = mmInStream.read(buffer, 0, buffer.length);
                            } catch (IOException e) {
                                Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                                break;
                            }
                        }
                        if(bytes >= 100){
                            bytes = 0;
                        }
                        for (int i = 0; i < bytes; i++) {
                            newbuffer[count] = buffer[i];
                            count++;
                        }
                        if (count > 2) {
                            if (newbuffer[0] == -81) {//startmessage
                                //Log.d(TAG, "run: message received and started");
                                final int _sensor = sensor;

                                //region engine
                                if ((newbuffer[1] & 0b10000000) == 0b10000000) { // motor active
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ControlPage.EngineOn(true);
                                        }
                                    });
                                    //Log.d(TAG, "receiveBTMessage: engine on");
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ControlPage.EngineOn(false);
                                        }
                                    });
                                    //Log.d(TAG, "receiveBTMessage: engine off");
                                }
                                //endregion

                                //region sensors
                                switch (newbuffer[1] & 0b00011111) {// sensor number
                                    case 0:
                                        sensor = 0;
                                        //Log.d(TAG, "receiveBTMessage: sensor 0");
                                        break;
                                    case 1:
                                        sensor = 1;
                                        //Log.d(TAG, "receiveBTMessage: sensor 1");
                                        break;
                                    case 2:
                                        sensor = 2;
                                        //Log.d(TAG, "receiveBTMessage: sensor 2");
                                        break;
                                    case 3:
                                        sensor = 3;
                                        //Log.d(TAG, "receiveBTMessage: sensor 3");
                                        break;
                                    case 4:
                                        sensor = 4;
                                        //Log.d(TAG, "receiveBTMessage: sensor 4");
                                        break;
                                    case 5:
                                        sensor = 5;
                                        //Log.d(TAG, "receiveBTMessage: sensor 5");
                                        break;
                                }

                                //endregion

                                //region afbeelding update
                                if (sensor != 0) {
                                    switch (newbuffer[2] & 0b00000111) { // sensor zone // 40 - 20 groen // oranje // rood //
                                        case 1: //zone 1 (10 cm)
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updateSensor(Color.RED, _sensor);
                                                }
                                            });

                                            //Log.d(TAG, "receiveBTMessage: sensor " + sensor + " Red");
                                            break;
                                        case 2: //zone 2 (10 - 30 cm)
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updateSensor(Color.rgb(255, 153, 0), _sensor);
                                                }
                                            });
                                            //Log.d(TAG, "receiveBTMessage: sensor " + sensor + " Orange");
                                            break;
                                        case 3: //zone 3 (30 -50)
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updateSensor(Color.rgb(255, 255, 0), _sensor);
                                                }
                                            });
                                            //Log.d(TAG, "receiveBTMessage: sensor " + sensor + " Yellow");
                                            break;
                                        case 4: //zone 4 // 50<
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    updateSensor(Color.rgb(0, 255, 0), _sensor);
                                                }
                                            });
                                            //Log.d(TAG, "receiveBTMessage: sensor " + sensor + " Green");
                                            break;
                                        case 5: //zone 5 not used
                                            break;
                                    }
                                }
                                //endregion

                                //region send to server
                                Boolean EngineL = false, EngineR = false;

                                if ((newbuffer[1] & 0b01000000) == 0b01000000) {
                                    EngineL = true;
                                    //Log.d(TAG, "engine: EngineL on");
                                }
                                else;
                                    //Log.d(TAG, "engine: EngineL off");
                                if ((newbuffer[1] & 0b00100000) == 0b00100000) {
                                    EngineR = true;
                                    //Log.d(TAG, "engine: EngineR on");
                                }
                                else;// Log.d(TAG, "engine: EngineR off");

                                if (EngineL && EngineR) {// if both engines are on
                                    //forward or backward
                                    Log.d(TAG, "run: direction = " +ControlPage.direction[0]);
                                    switch (cDegree){
                                        case 0:
                                            if(ControlPage.direction[0] == 3 ){ // Vooruit
                                                // add to Y
                                                cY -= stapgrote;
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            } else if (ControlPage.direction[0] == 7){ // Achteruit
                                                // min from Y
                                                cY += stapgrote;
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            }
                                            break;
                                        case 45:
                                            if(ControlPage.direction[0] == 3 ){ // Vooruit
                                                // add to X and Y
                                                cX -= (stapgrote + diagonalExtra);
                                                cY -= (stapgrote + diagonalExtra);
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            } else if (ControlPage.direction[0] == 7){ // Achteruit
                                                // min from X and Y
                                                cX += (stapgrote + diagonalExtra);
                                                cY += (stapgrote + diagonalExtra);
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            }
                                            break;
                                        case 90:
                                            if(ControlPage.direction[0] == 3 ){ // Vooruit
                                                // add to X
                                                cX -= stapgrote;
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            } else if (ControlPage.direction[0] == 7){ // Achteruit
                                                // min from X
                                                cX += stapgrote;
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            }
                                            break;
                                        case 135:
                                            if(ControlPage.direction[0] == 3 ){ // Vooruit
                                                // add X and min from Y
                                                cX -= (stapgrote + diagonalExtra);
                                                cY += (stapgrote + diagonalExtra);
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            } else if (ControlPage.direction[0] == 7){ // Achteruit
                                                // min from X and add to Y
                                                cX += (stapgrote + diagonalExtra);
                                                cX -= (stapgrote + diagonalExtra);
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            }
                                            break;
                                        case 180:
                                            if(ControlPage.direction[0] == 3 ){ // Vooruit
                                                // min from Y
                                                cY += stapgrote;
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            } else if (ControlPage.direction[0] == 7){ // Achteruit
                                                // add to Y
                                                cY -= stapgrote;
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            }
                                            break;
                                        case 225:
                                            if(ControlPage.direction[0] == 3 ){ // Vooruit
                                                // min from Y and min from X
                                                cX += (stapgrote + diagonalExtra);
                                                cY += (stapgrote + diagonalExtra);
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            } else if (ControlPage.direction[0] == 7){ // Achteruit
                                                // add to Y and add to X
                                                cX -= (stapgrote + diagonalExtra);
                                                cY -= (stapgrote + diagonalExtra);
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            }
                                            break;
                                        case 270:
                                            if(ControlPage.direction[0] == 3 ){ // Vooruit
                                                // min from X
                                                cX += stapgrote;
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            } else if (ControlPage.direction[0] == 7){ // Achteruit
                                                // add to X
                                                cX -= stapgrote;
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            }
                                            break;
                                        case 315:
                                            if(ControlPage.direction[0] == 3 ){ // Vooruit
                                                // min from X and add to Y
                                                cX += (stapgrote + diagonalExtra);
                                                cY -= (stapgrote + diagonalExtra);
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            } else if (ControlPage.direction[0] == 7){ // Achteruit
                                                // add to X and min from Y
                                                cX -= (stapgrote + diagonalExtra);
                                                cY += (stapgrote + diagonalExtra);
                                                if (ConnectTask.mTcpClient != null && ControlPage.run) {
                                                    ConnectTask.mTcpClient.sendMessage(cX+","+cY+"<DP>");
                                                    //Log.d(TAG, "run: sendMessage to server: "+ cX+","+cY+"<DP>");
                                                }
                                            }
                                            break;
                                    }

                                } else if (EngineL) {// if engine L is on
                                    //rotation of -45 degrees
                                    cDegree += -45;
                                    if(cDegree == -45)cDegree = 315;
                                    //Log.d(TAG, "run: cDegree = "+cDegree);

                                } else if (EngineR) {// if engine R is on
                                    //rotation of 45 degrees
                                    cDegree += 45;
                                    if(cDegree == 360)cDegree = 0;
                                    //Log.d(TAG, "run: cDegree = "+cDegree);
                                } else {

                                }
                                //Log.d(TAG, "run: cDegree = "+cDegree);
                                //endregion

                                // end loop
                                for (int c = 0; c < newbuffer.length - 3; c++) {
                                    newbuffer[c] = newbuffer[c + 3];
                                }
                                count = count - 3;
                            } else {
                                Log.e(TAG, "run: start message missed");
                                for (int c = 0; c < newbuffer.length - 1; c++) {
                                    newbuffer[c] = newbuffer[c + 1];
                                }
                                count = count - 1;
                            }
                        }
                        bytes = 0;
                    } catch (IOException e) {

                    }
                }
                finally {
                        l.unlock();
                        //Log.d(TAG, "run: unlocked");
                    }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) throws IOException{
            mmOutStream.write(bytes);
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            mRun = false;
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.i(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) throws IOException{
        // Create temporary object
        ConnectedThread r;
        //perform the write
        if(mConnectedThread != null)
            mConnectedThread.write(out);
    }

    public synchronized void cancel(){
        //Log.d(TAG, "cancel: connectedthread");
        //if(mConnectedThread != null)
            mConnectedThread.cancel();
        if(mConnectThread != null)
            mConnectThread.cancel();
        if(mInsecureAcceptThread != null)
            mInsecureAcceptThread.cancel();
    }

    public void zero(){
        cX = 0;
        cY = 0;
    }
}
