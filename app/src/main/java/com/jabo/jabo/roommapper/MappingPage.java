package com.jabo.jabo.roommapper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MappingPage extends AppCompatActivity {
    static ImageView map;
    static Bitmap bitmap;
    static Canvas canvas;
    static int maxsamples = 500;
    static String[][] coords = new String[maxsamples][];
    static int number_of_samples = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping_page);
        map = (ImageView) findViewById(R.id.canvas);
        if (ConnectTask.mTcpClient != null) {
            ConnectTask.mTcpClient.sendMessage("<RMAP>");
        }
    }


    public static void on_update_map(String message){
        if(!message.equalsIgnoreCase("")) {
            Log.d("on_update_map",message);
            message = message.replace("received message", "");
            coords[number_of_samples] = message.split(",");
            draw_line(coords[number_of_samples][0], coords[number_of_samples][1], 0, 0);
            number_of_samples++;
        }
    }

    public static void draw_line(String x, String y,int prevX,int prevY){

        bitmap = Bitmap.createBitmap(map.getWidth(), map.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        canvas.drawColor(Color.LTGRAY);

        // Initialize a new Paint instance to draw the line
        Paint paint = new Paint();
        // Line color
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        // Line width in pixels
        paint.setStrokeWidth(4);
        paint.setAntiAlias(true);

        // Set a pixels value to offset the line from canvas edge
        int offset = 50;

        int _x = 0;
        int _y = 0;
        if(x != null && y != null){
            try{
                _x = Integer.parseInt(x);
                _y = Integer.parseInt(y);
            }
            catch (NumberFormatException e){
                Log.e("Draw line", "error",e);
            }
            canvas.drawLine(_y+offset,_x+offset,prevX+offset,prevY+offset,paint);
            map.setImageBitmap(bitmap);
        }
    }

}
