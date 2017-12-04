package com.jabo.jabo.roommapper;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;


public class MappingPage extends Activity {
    static DrawView drawView;
    static int currentCoords = 0;
    static int[][] coords = new int[1000][2];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);
        if (ConnectTask.mTcpClient != null) {
            ConnectTask.mTcpClient.sendMessage("<RMAP>");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        drawView.update(coords);
    }

    public static void update(String Message){
        Message = Message.replace("message received","");
        String[] xy = Message.split(",");
        int x = Integer.parseInt(xy[0]);
        int y = Integer.parseInt(xy[1]);
        coords[currentCoords][0]=x;
        coords[currentCoords][1]=y;
        drawView.update(coords);
        currentCoords++;
    }
}
