package com.jabo.jabo.roommapper;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.JoystickView;
import android.widget.JoystickView.OnJoystickMoveListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jabo.jabo.BT.BTConnectie;

import static android.R.drawable.button_onoff_indicator_off;

public class ControlPage extends AppCompatActivity {
    private int samples=1;
    private int power;
    private JoystickView joystick;
    static Context context;
    static Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_page);
        Context context = getApplicationContext();
        this.context =context;

        TextView samplesnumber = (TextView) findViewById(R.id.samples);
        if(true){
            samplesnumber.setText(String.format("%s", samples));
        }
        joystick = (JoystickView) findViewById(R.id.joystick);

        joystick.setOnJoystickMoveListener(new OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int input_power, int direction) {
                power= input_power;
                byte[] message = new byte[4];
                message[2] = (byte)power;
                message[3]= (byte)'\n';
                switch (direction) {
                    case JoystickView.FRONT:
                        message[1] = 1;
                        break;
                    case JoystickView.FRONT_RIGHT:
                        message[1] = 2;
                        break;
                    case JoystickView.RIGHT:
                        message[1] = 3;
                        break;
                    case JoystickView.RIGHT_BOTTOM:
                        message[1] = 4;
                        break;
                    case JoystickView.BOTTOM:
                        message[1] = 5;
                        break;
                    case JoystickView.BOTTOM_LEFT:
                        message[1] = 6;
                        break;
                    case JoystickView.LEFT:
                        message[1] = 7;
                        break;
                    case JoystickView.LEFT_FRONT:
                        message[1] = 8;
                        break;
                    default:
                        message[1] = 0;//halt
                }
                message[0] = (byte)(message.length+16);
                try {
                    if (BTConnectie.mConnectedThread.isAlive()) {
                        BTConnectie.mConnectedThread.write(message);
                    } else {
                        popup("cant send to device");
                        ImageView img = (ImageView) findViewById(R.id.Bluetooth);
                        img.setImageResource(button_onoff_indicator_off);
                    }
                }
                catch (Exception e){
                    popup("cant send to device");
                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);
    }

    public void add_sample(View v){
        if (samples < 30) {
            samples++;
            popup("sample added");
            TextView samplesnumber = (TextView) findViewById(R.id.samples);
            samplesnumber.setText(String.format("%s", samples));
        }
    }

    public void min_sample(View v){
        if(samples> 1) {
            popup("sample subtracted");
            samples--;
            TextView samplesnumber = (TextView) findViewById(R.id.samples);
            samplesnumber.setText(String.format("%s", samples));
        }
    }
    private boolean run = false;
    public void on_start(View v){
        start();
    }

    private void start(){
        byte[] message = new byte[3];
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

        if (run == false && equals == false){
            run = true;
            new ConnectTask().execute("");
            popup("started");
            samplesButtonadd.setEnabled(false);
            samplesButtonmin.setEnabled(false);
            RoomName.setEnabled(false);
            message[1] = (byte) samples;
            message[2]= (byte)'\n';
            message[0] = (byte)(message.length+32);
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
        else if (equals == false)
        {
            popup("stopped");
            if (ConnectTask.mTcpClient != null) {
                ConnectTask.mTcpClient.sendMessage("DISCONNECT<LOG>");
            }
            if (ConnectTask.mTcpClient != null) {
                ConnectTask.mTcpClient.sendMessage("<DISCONNECT>");
            }
            run = false;
            if(BTConnectie.mConnectedThread != null) {
                BTConnectie.mConnectedThread.write(message); // stop meting
            }
            ConnectTask.mTcpClient.Cancel();
            ConnectTask.mTcpClient.stopClient();
            samplesButtonadd.setEnabled(true);
            samplesButtonmin.setEnabled(true);
            RoomName.setEnabled(true);
        }
        else{
            ToggleButton StartButton = (ToggleButton) findViewById(R.id.StartButton);
            StartButton.setChecked(false);
        }
        //TODO continuously send direction, receive data points
    }

    public static void update(String Message) {
        if (Message == "No filename defined") {
            new ControlPage().start();
            popup("Server: No file name defined");
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        /*if(ConnectTask.mTcpClient.mRun == false) {
            ConnectTask.mTcpClient.stopClient();
        }*/
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
