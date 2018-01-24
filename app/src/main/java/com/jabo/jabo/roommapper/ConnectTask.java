package com.jabo.jabo.roommapper;

import android.os.AsyncTask;
import android.util.Log;

public class ConnectTask extends AsyncTask<String, String, TcpClient> {
    public static TcpClient mTcpClient;
    public static String[] Coords;
    private String TAG = "ConnectTask";
    @Override
    protected TcpClient doInBackground(String... message) {

        mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceived(String message) {
                publishProgress(message);
            }
        });
        Log.d(TAG, "doInBackground: mTcpClient started");
        mTcpClient.run();

        return null;
    }

    public static void sendmessage(String Message){
        if (mTcpClient != null) {
            mTcpClient.sendMessage(Message);
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        MappingPage page = new MappingPage();
        page.update(values[0]);
    }
}