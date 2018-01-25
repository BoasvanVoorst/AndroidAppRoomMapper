package com.jabo.jabo.roommapper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.EditText;


public class MappingPage extends AppCompatActivity {
    final static String TAG = "MappingPage";
    static DrawView drawView;
    static int currentCoords = 0;
    static float[][] coords = new float[10000][4];
    private static float offsetx;
    private static float offsety;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        drawView.clear();
        setContentView(drawView);
        drawView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                offsetx = drawView.getWidth()/2;
                offsety = drawView.getHeight()/2;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose room name");
        EditText input = new EditText(this);
        input.setHint("roomname");
        final EditText _input = input;
        builder.setView(input);
        builder.setPositiveButton("next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String name =_input.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (ConnectTask.mTcpClient != null) {
                            ConnectTask.mTcpClient.sendMessage(name+"<NAME>");
                        }
                        if (ConnectTask.mTcpClient != null) {
                            ConnectTask.mTcpClient.sendMessage("<RMAP>");
                        }
                    }
                }).start();

            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                MappingPage.this.finish();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        drawView.update(coords,Math.round(offsetx),Math.round(offsety));
    }

    static float prev_x = 0;
    static float prev_y = 0;
    public static void update(String Message){
        Message = Message.replace("File already Exists","");
        Message = Message.replace("message received","");
        Message = Message.replace("received message","");
        String[] xy = Message.split(",");
        if(xy.length>1){
            if(!xy[0].equalsIgnoreCase("") && !xy[1].equalsIgnoreCase("")) {
                float x = Float.parseFloat(xy[0]);
                float y = Float.parseFloat(xy[1]);
                coords[currentCoords][0] = x;
                coords[currentCoords][1] = y;
                coords[currentCoords][2] = prev_x;
                coords[currentCoords][3] = prev_y;
                prev_x = x;
                prev_y = y;
            }
        }
        drawView.update(coords,Math.round(offsetx),Math.round(offsety));
        currentCoords++;
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        drawView = null;
        for (int i = 0; i < coords.length; i++) {
            coords[i][0] = 0;
            coords[i][1] = 0;
            coords[i][2] = 0;
            coords[i][3] = 0;
        }
    }

    private float mPreviousX;
    private float mPreviousY;
    @Override
    public boolean onTouchEvent(MotionEvent event){

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                offsetx += dx;
                offsety += dy;
                update("");

                // your code
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
