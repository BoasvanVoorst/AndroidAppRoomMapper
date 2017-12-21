package com.jabo.jabo.roommapper;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.JoystickView;
import android.widget.JoystickView.OnJoystickMoveListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.PowerManager;

import com.jabo.jabo.BT.BTConnectie;

import static android.R.drawable.button_onoff_indicator_off;

public class ControlPage extends AppCompatActivity {
    protected PowerManager.WakeLock mWakeLock;

    String TAG = "ControlPage";
    static Context context;
    static Toast toast;
    static byte[] direction = new byte[1];

    ImageButton ForwardButton;
    ImageButton BackwardButton;
    ImageButton LeftButton;
    ImageButton RightButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_page);

        Log.d(TAG, "onCreate: Powermanager");
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"wakescreen");
        this.mWakeLock.acquire();

        Context context = getApplicationContext();
        this.context =context;

        ForwardButton = (ImageButton) findViewById(R.id.ForwardButton);
        BackwardButton = (ImageButton) findViewById(R.id.BackwardButton);
        LeftButton = (ImageButton) findViewById(R.id.LeftButton);
        RightButton = (ImageButton) findViewById(R.id.RightButton);

        findViewById(R.id.ForwardButton).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    BackwardButton.setEnabled(false);
                    if(RightButton.isPressed()) {
                        Log.d(TAG, "onTouch: Right_front");
                        direction[0] = 4;
                    }
                    else if (LeftButton.isPressed()){
                        Log.d(TAG, "onTouch: Left Front");
                        direction[0] = 2;
                    }
                    else{
                        Log.d(TAG, "onTouch: forward");
                        direction[0] = 3;
                    }
                    if(BTConnectie.mConnectedThread != null) {
                        BTConnectie.mConnectedThread.write(direction);
                    }
                    else{
                        popup("cant send to bt device");
                        Log.d(TAG, "onTouch: error");
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BackwardButton.setEnabled(true);
                    if(RightButton.isPressed()) {
                        Log.d(TAG, "onTouch: Right");
                        direction[0] = 5;
                    }
                    else if (LeftButton.isPressed()){
                        Log.d(TAG, "onTouch: Left");
                        direction[0] = 1;
                    }
                    else{
                        Log.d(TAG, "onTouch: halt");
                        direction[0] = 0;
                    }
                    if(BTConnectie.mConnectedThread != null) {
                        BTConnectie.mConnectedThread.write(direction);
                    }
                    else{
                        popup("cant send to bt device");
                        Log.d(TAG, "onTouch: error");
                    }
                }
                return false;
            }
        });
        
        findViewById(R.id.BackwardButton).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    ForwardButton.setEnabled(false);
                    if(RightButton.isPressed()) {
                        Log.d(TAG, "onTouch: Right_Back");
                        direction[0] = 6;
                    }
                    else if (LeftButton.isPressed()){
                        Log.d(TAG, "onTouch: Left Back");
                        direction[0] = 8;
                    }
                    else{
                        Log.d(TAG, "onTouch: backward");
                        direction[0] = 7;
                    }
                    if(BTConnectie.mConnectedThread != null) {
                        BTConnectie.mConnectedThread.write(direction);
                    }
                    else{
                        popup("cant send to bt device");
                        Log.d(TAG, "onTouch: error");
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    ForwardButton.setEnabled(true);
                    if(RightButton.isPressed()) {
                        Log.d(TAG, "onTouch: right");
                        direction[0] = 5;
                    }
                    else if (LeftButton.isPressed()){
                        Log.d(TAG, "onTouch: left");
                        direction[0] = 1;
                    }
                    else{
                        Log.d(TAG, "onTouch: halt");
                        direction[0] = 0;
                    }
                    if(BTConnectie.mConnectedThread != null) {
                        BTConnectie.mConnectedThread.write(direction);
                    }
                    else{
                        popup("cant send to bt device");
                        Log.d(TAG, "onTouch: error");
                    }
                }
                return false;
            }
        });
        
        findViewById(R.id.RightButton).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    LeftButton.setEnabled(false);
                    if(ForwardButton.isPressed()){
                        direction[0] = 4;
                        Log.d(TAG, "onTouch: Front_right");
                    }
                    else if(BackwardButton.isPressed()){
                        direction[0] = 6;
                        Log.d(TAG, "onTouch: Backwards_right");
                    }
                    else{
                        Log.d(TAG, "onTouch: right");
                        direction[0] = 5;
                    }
                    if(BTConnectie.mConnectedThread != null) {
                        BTConnectie.mConnectedThread.write(direction);
                    }
                    else{
                        popup("cant send to bt device");
                        Log.d(TAG, "onTouch: error");
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    LeftButton.setEnabled(true);
                    if(ForwardButton.isPressed()){
                        direction[0] = 3;
                        Log.d(TAG, "onTouch: forward");
                    }
                    else if(BackwardButton.isPressed()){
                        direction[0] = 7;
                        Log.d(TAG, "onTouch: Backwards");
                    }
                    else{
                        Log.d(TAG, "onTouch: halt");
                        direction[0] = 0;
                    }
                    if(BTConnectie.mConnectedThread != null) {
                        BTConnectie.mConnectedThread.write(direction);
                    }
                    else{
                        popup("cant send to bt device");
                        Log.d(TAG, "onTouch: error");
                    }
                }
                return false;
            }
        });
        
        findViewById(R.id.LeftButton).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    RightButton.setEnabled(false);
                    if(ForwardButton.isPressed()){
                        direction[0] = 2;
                        Log.d(TAG, "onTouch: Left_forward");
                    }
                    else if(BackwardButton.isPressed()){
                        direction[0] = 8;
                        Log.d(TAG, "onTouch: left_backwards");
                    }
                    else{
                        Log.d(TAG, "onTouch: left");
                        direction[0] = 1;
                    }
                    if(BTConnectie.mConnectedThread != null) {
                        BTConnectie.mConnectedThread.write(direction);
                    }
                    else{
                        popup("cant send to bt device");
                        Log.d(TAG, "onTouch: error");
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    RightButton.setEnabled(true);
                    if(ForwardButton.isPressed()){
                        direction[0] = 3;
                        Log.d(TAG, "onTouch: forward");
                    }
                    else if(BackwardButton.isPressed()){
                        direction[0] = 7;
                        Log.d(TAG, "onTouch: Backwards");
                    }
                    else{
                        Log.d(TAG, "onTouch: halt");
                        direction[0] = 0;
                    }
                    if(BTConnectie.mConnectedThread != null) {
                        BTConnectie.mConnectedThread.write(direction);
                    }
                    else{
                        popup("cant send to bt device");
                        Log.d(TAG, "onTouch: error");
                    }
                }
                return false;
            }
        });

    }

    private boolean run = false;
    public void on_start(View v){
        start();
    }

    private void start(){
        byte[] message = new byte[1];
        message[0]=(byte)0xAA;
        Button samplesButtonadd = (Button) findViewById(R.id.addSample);
        Button samplesButtonmin = (Button) findViewById(R.id.minsample);
        TextView RoomName = (TextView) findViewById(R.id.RoomName);
        String input = RoomName.getText().toString();
        boolean equals = false;
        if (input.equalsIgnoreCase("Room Name")){
            equals = true;
            popup("please enter a new room name");
        }
        else if (input.equalsIgnoreCase("")){
            equals = true;
            popup("please enter a room name");
        }

        if (run == false && equals == false &&BTConnectie.mConnectedThread != null){
            run = true;
            //new ConnectTask().execute("");
            popup("started");
            samplesButtonadd.setEnabled(false);
            samplesButtonmin.setEnabled(false);
            RoomName.setEnabled(false);
            try {
                if(BTConnectie.mConnectedThread != null) {
                    BTConnectie.mConnectedThread.write(message); //start meting
                }
            }
            catch (Exception e){
                Log.e("ControlPage","Write",e);
            }
            if (ConnectTask.mTcpClient != null) {
                ConnectTask.mTcpClient.sendMessage("Start<LOG>");
            }
            if (ConnectTask.mTcpClient != null) {
                ConnectTask.mTcpClient.sendMessage(input+"<NAME>");
            }
        }
        else if (equals == false&&BTConnectie.mConnectedThread != null)
        {
            popup("stopped");
            run = false;
            if(BTConnectie.mConnectedThread != null) {
                BTConnectie.mConnectedThread.write(message); // stop meting
            }
            samplesButtonadd.setEnabled(true);
            samplesButtonmin.setEnabled(true);
            RoomName.setEnabled(true);
        }
        else{
            ToggleButton StartButton = (ToggleButton) findViewById(R.id.StartButton);
            StartButton.setChecked(false);
            popup("cant send message to device");
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.mWakeLock.release();
    }

    public static void popup(String message){
        int duration = Toast.LENGTH_SHORT;
        if(toast != null) {
            toast.cancel();
            toast = Toast.makeText(context,message,duration);
            toast.show();
        }
        else {
            toast = Toast.makeText(context,message,duration);
            toast.show();
        }
    }
}
