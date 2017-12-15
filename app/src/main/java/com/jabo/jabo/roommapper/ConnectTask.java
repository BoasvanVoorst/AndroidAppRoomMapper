package com.jabo.jabo.roommapper;

import android.os.AsyncTask;

public class ConnectTask extends AsyncTask<String, String, TcpClient> {
    public static TcpClient mTcpClient;
    public static String[] Coords;
    @Override
    protected TcpClient doInBackground(String... message) {

        mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceived(String message) {
                publishProgress(message);
            }
        });

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