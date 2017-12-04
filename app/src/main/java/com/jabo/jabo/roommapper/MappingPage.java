package com.jabo.jabo.roommapper;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;


public class MappingPage extends Activity {
    DrawView drawView;
    private int[][] coords = new int[][]{{0,0},{10,0},{20,0},{30,0},{40,0},{50,0},{60,0},{70,0},{80,0},{80,10},{80,20},{80,30},{80,40},{80,50},{80,60},{80,70},{80,80}};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);
        drawView.update(coords);
    }

    @Override
    protected void onResume(){
        super.onResume();
        drawView.update(coords);
    }

    public void update(String Message){

    }
}
