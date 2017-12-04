package com.jabo.jabo.roommapper;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import com.jabo.jabo.roommapper.ControlPage;
/**
 * Created by Server on 9-10-2017.
 */
// run new ConnectTask().execute("");
/*
    to send a message to the server:
    if (mTcpClient != null) {
    mTcpClient.sendMessage("testing");
}
     */
/*
    close connection to the server:
    if (mTcpClient != null) {
    mTcpClient.stopClient();
}
     */
public class ConnectTask extends AsyncTask<String, String, TcpClient> {
    public static TcpClient mTcpClient;
    public static String[] Coords;
    @Override
    protected TcpClient doInBackground(String... message) {

        //we create a TCPClient object
        mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceived(String message) {
                //this method calls the onProgressUpdate
                //Log.d("MessageReceived","1");
                publishProgress(message);
            }
        });

        mTcpClient.run();

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        //Log.d("test", "response " + values[0]);
        //for(int i = 0; i<1;i++);
        //ControlPage.update(values[0]);
        //MappingPage mappingpage = new MappingPage();
        //mappingpage.on_update_map(values[0]);
    }
}