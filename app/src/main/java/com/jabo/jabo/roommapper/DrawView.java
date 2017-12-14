package com.jabo.jabo.roommapper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DrawView extends View {
    String TAG = "DrawView";
    Paint paint = new Paint();
    Canvas canvas;
    int[][] coords;
    int offsetx=0;
    int offsety=0;

    private void init() {
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {
        this.canvas = canvas;
        canvas.drawColor(Color.WHITE);
        for(int i =0;i<this.coords.length;i++) {
            try{
                canvas.drawLine((this.coords[i][2]+this.offsetx),(this.coords[i][3]+this.offsety),(this.coords[i][0]+this.offsetx),(this.coords[i][1]+this.offsety),paint);
            }
            catch (Exception e){

            }
        }
    }

    public void update(int[][] coords,int offsetx,int offsety){
        this.coords = coords;
        this.offsetx = offsetx;
        this.offsety = offsety;
        invalidate();
    }

    public void clear(){
        if(coords != null) {
            for (int i = 0; i < coords.length; i++) {
                coords[i][0] = 0;
                coords[i][1] = 0;
                coords[i][2] = 0;
                coords[i][3] = 0;
            }
        }
        if(canvas != null)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
    }
}