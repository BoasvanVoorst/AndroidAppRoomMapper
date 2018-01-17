package com.jabo.jabo.roommapper;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Server on 9-10-2017.
 */

public class TcpClient {

    public static String SERVER_IP = "192.168.101.11"; //server IP address
    public static final int SERVER_PORT = 11000;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    public boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
            if (mBufferOut != null && !mBufferOut.checkError()) {
                mBufferOut.flush();
                mBufferOut.println(message);
            }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        if (mBufferIn != null){
            try {
                mBufferIn.close();
            }
            catch (Exception e){
                Log.e("TCP","Bufferin: "+ e);
            }
        }
        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {
        mRun = true;
        Socket socket = null;
        String connected = "false";
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            try {
                socket = new Socket(serverAddr, SERVER_PORT);
            }
            catch (Exception e){
                Log.e("Socket","Failed "+e);
            }
            try {
                if (socket.isConnected()) {
                    Log.d("TCP", "Socket created");
                    connected = "true";
                } else {
                    Log.e("TCP", "Socket failed");
                    connected = "false";
                    return;
                }
            }
            catch (NullPointerException e){
                Log.e("TCP", "Socket failed");
                connected = "false";
                return;
            }

            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    try {
                        mServerMessage = mBufferIn.readLine();
                    }
                    catch (IOException e){
                    }
                    finally {
                    }
                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }
                }
            }
            catch (Exception e) {
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
                return;
            }

        } catch (Exception e) {
            return;
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public void Cancel(){
        mRun = false;
        try {
            if(mBufferIn != null) {
                mBufferIn.close();
            }
        }
        catch(Exception e){
            Log.e("TCP", "Buffer: ", e);
        }
    }
}
