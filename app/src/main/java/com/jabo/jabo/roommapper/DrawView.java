package com.jabo.jabo.roommapper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View {
    Paint paint = new Paint();
    int[][] coords;

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
        for(int i =0;i<this.coords.length;i++) {
            try{
                canvas.drawLine(this.coords[i][0],this.coords[i][1],0,0,paint);
            }
            catch (Exception e){

            }
        }

    }

    public void update(int[][] coords){
        this.coords = coords;
        invalidate();
    }
}