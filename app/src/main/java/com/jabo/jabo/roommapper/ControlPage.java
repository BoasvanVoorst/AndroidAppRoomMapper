package com.jabo.jabo.roommapper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.PowerManager;

import com.jabo.jabo.BT.BluetoothConnectionService;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.R.drawable.button_onoff_indicator_off;
import static android.R.drawable.button_onoff_indicator_on;

public class ControlPage extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private final String TAG = "ControlPage";

    private static int message_distance = 10;

    private final static int X = 0;
    private final static int Y = 1;
    private static int[][] coords = new int[2][];
    private static int coordnumber = 0;

    protected PowerManager.WakeLock mWakeLock;

    private static Context context;
    private static Toast toast;
    private static byte[] direction = new byte[1];
    private static boolean run = false;
    private SharedPreferences systemPreferences;

    private byte[] receivedMessage = new byte[10];

    static ImageButton ForwardButton;
    static ImageButton BackwardButton;
    static ImageButton LeftButton;
    static ImageButton RightButton;
    static ImageView BT;
    private static int degree=0;
    static ImageView engine;
    static ImageView sensor1;
    static ImageView sensor2;
    static ImageView sensor3;
    static ImageView sensor4;
    static ImageView sensor5;
    static ImageView Sensors[] = {sensor1,sensor2,sensor3,sensor4,sensor5};

    //TODO reconnect crashes app

    //region bt init
    BluetoothConnectionService mBluetoothConnection;

    BluetoothAdapter mBluetoothAdapter;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    //endregion

    static boolean sending = true;

    Thread sendThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_page);
        final Activity var = this;
        Context context = getApplicationContext();
        this.context =context;
        engine = (ImageView) findViewById(R.id.engineImage);
        sensor1 = (ImageView)findViewById(R.id.sensor1);
        sensor2 = (ImageView)findViewById(R.id.sensor2);
        sensor3 = (ImageView)findViewById(R.id.sensor3);
        sensor4 = (ImageView)findViewById(R.id.sensor4);
        sensor5 = (ImageView)findViewById(R.id.sensor5);
        BT = (ImageView) findViewById(R.id.Bluetooth);
        systemPreferences = this.getSharedPreferences("com.jabo.jabo.roommapper_preferences",MODE_PRIVATE);
        //region Wakelock

        Log.d(TAG, "onCreate: Powermanager");
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wakescreen");

        if(systemPreferences.getBoolean("screen_awake",true)) {
            this.mWakeLock.acquire();
        }
        //endregion

        //region buttons init
        ForwardButton = (ImageButton) findViewById(R.id.ForwardButton);
        BackwardButton = (ImageButton) findViewById(R.id.BackwardButton);
        LeftButton = (ImageButton) findViewById(R.id.LeftButton);
        RightButton = (ImageButton) findViewById(R.id.RightButton);
        //endregion

        //region forwardbutton
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
                    sending = true;
                    sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: sendThread started");
                            while(sending) {
                                try {
                                    mBluetoothConnection.write(direction);
                                    try {
                                        sendThread.sleep(50);
                                    }
                                    catch(InterruptedException e){

                                    }
                                    //Log.d(TAG, "run: sending" + direction);
                                }
                                catch (IOException e){
                                    //Log.e(TAG, "run: sendThread: ", e );
                                }
                            }
                        }
                    });
                    sendThread.start();
                    //mBluetoothConnection.write(direction);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    BackwardButton.setEnabled(true);
                    sending = false;
                    sendThread=null;
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
                    try {
                        mBluetoothConnection.write(direction);
                    }catch (ConnectException e){
                        popup("turn on device please");
                    } catch (Exception e) {
                        popup("Cant send to device");
                        connectDevice();
                    }
                }
                return false;
            }
        });

        //endregion

        //region backwardbutton
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
                    sending = true;
                    sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: sendThread started");
                            while(sending) {
                                try {
                                    mBluetoothConnection.write(direction);
                                    try {
                                        sendThread.sleep(50);
                                    }
                                    catch(InterruptedException e){

                                    }
                                    //Log.d(TAG, "run: sending" + direction);
                                }
                                catch (IOException e){
                                    //Log.e(TAG, "run: sendThread: ", e );
                                }
                            }
                        }
                    });
                    sendThread.start();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    ForwardButton.setEnabled(true);
                    sending = false;
                    sendThread=null;
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
                    try {
                        mBluetoothConnection.write(direction);
                    } catch (ConnectException e){
                        popup("turn on device please");
                    } catch (Exception e) {
                        popup("Cant send to device");
                        connectDevice();
                    }
                }
                return false;
            }
        });
        //endregion

        //region Rightbutton
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
                    sending = true;
                    sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: sendThread started");
                            while(sending) {
                                try {
                                    mBluetoothConnection.write(direction);
                                    try {
                                        sendThread.sleep(50);
                                    }
                                    catch(InterruptedException e){

                                    }
                                    //Log.d(TAG, "run: sending" + direction);
                                }
                                catch (IOException e){
                                    //Log.e(TAG, "run: sendThread: ", e );
                                }
                            }
                        }
                    });
                    sendThread.start();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    LeftButton.setEnabled(true);
                    sending = false;
                    sendThread = null;
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
                    try {
                        mBluetoothConnection.write(direction);
                    } catch (ConnectException e){
                        popup("turn on device please");
                    } catch (Exception e) {
                        popup("Cant send to device");
                        connectDevice();
                    }
                }
                return false;
            }
        });
        //endregion

        //region leftbutton
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
                    sending = true;
                    sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: sendThread started");
                            while(sending) {
                                try {
                                    mBluetoothConnection.write(direction);
                                    try {
                                        sendThread.sleep(50);
                                    }
                                    catch(InterruptedException e){

                                    }
                                    //Log.d(TAG, "run: sending" + direction);
                                }
                                catch (IOException e){
                                    //Log.e(TAG, "run: sendThread: ", e );
                                }
                            }
                        }
                    });
                    sendThread.start();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    RightButton.setEnabled(true);
                    sending = false;
                    sendThread = null;
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
                    try {
                        mBluetoothConnection.write(direction);
                    } catch (ConnectException e){
                        popup("turn on device please");
                    } catch (Exception e) {
                        popup("Cant send to device");
                        connectDevice();
                    }
                }
                return false;
            }
        });
        //endregion

        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        enableDisableBT();

        connectDevice();
    }

    public void connectDevice(){
        if(mBluetoothConnection != null) mBluetoothConnection.cancel();
        String btdeviceName = systemPreferences.getString("Device_Name","JaBo");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            int i =0;
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if(deviceName.equalsIgnoreCase(btdeviceName)){
                    try {
                        Log.d("Device","Connected");
                        mBTDevice = device;
                        startConnection();
                    }
                    catch (Exception e){

                    }
                    break;
                }
                else{
                    i++;
                    Log.d(deviceName,btdeviceName);
                    Log.d("Device","not found");
                }
            }
        }
    }

    public void on_start(View v){
        start();
    }

    private void start(){
        byte[] message = new byte[1];
        message[0]=(byte)0b10000000;
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
            //new ConnectTask().execute("");
            popup("started");
            samplesButtonadd.setEnabled(false);
            samplesButtonmin.setEnabled(false);
            RoomName.setEnabled(false);
            try {
                mBluetoothConnection.write(message);
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
            run = false;
            try {
                mBluetoothConnection.write(message);
            } catch (ConnectException e){
                popup("turn on device please");
            } catch (Exception e) {
                popup("Cant send to device");
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

    public static void EngineOn(boolean state){
        if(state){
            engine.setImageResource(android.R.drawable.presence_online);
        }
        else{
            engine.setImageResource(android.R.drawable.presence_busy);
        }
    }

    public static void updateSensor(int color, int wich_sensor){
        Sensors[wich_sensor-1].setBackgroundColor(color);
    }

    //region broadcastreceiver1
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };
    //endregion

    //region Broadcastreceiver4
    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };
    //endregion


    //create method for starting connection
    //***remember the conncetion will fail and app will crash if you haven't paired first
    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection = new BluetoothConnectionService(ControlPage.this);
        mBluetoothConnection.startClient(device,uuid);
    }



    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        else{
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(ControlPage.this);
        }
    }

    @Override
    protected void onDestroy(){
        if(systemPreferences.getBoolean("screen_awake",true)) super.onDestroy();
        Log.d(TAG, "onDestroy: called.");
        this.mWakeLock.release();
        mBluetoothConnection.cancel();
        try {
            unregisterReceiver(mBroadcastReceiver1);
        }
        catch(IllegalArgumentException e){
            Log.e(TAG, "onDestroy: BroadcastReceiver1",e );
        }
        try {
            unregisterReceiver(mBroadcastReceiver4);
        }
        catch(IllegalArgumentException e){
            Log.e(TAG, "onDestroy: BroadcastReceiver4",e );
        }
    }

    public static void receiveBTMessage(byte[] message){
        int sensor = 0;
        boolean Ldrive;
        boolean Rdrive;
        if((message[0]&0b10101111) == 0b10101111){

            if((message[1]&0b10000000) == 0b10000000){ // motor active
                EngineOn(true);
            }
            else{
                EngineOn(false);
            }

            if((message[1]&0b01000000)==0b01000000){
                Ldrive = true;
            }
            else{
                Ldrive = false;
            }

            if((message[1]&0b00100000)==0b00100000){
                Rdrive = true;
            }
            else{
                Rdrive = false;
            }

            switch (message[1]&0b00011111){// sensor number
                case 0b00000000:
                    sensor = 0;
                    break;
                case 0b00000001:
                    sensor = 1;
                    break;
                case 0b00000010:
                    sensor = 2;
                    break;
                case 0b00000100:
                    sensor = 3;
                    break;
                case 0b00001000:
                    sensor = 4;
                    break;
                case 0b00010000:
                    sensor = 5;
                    break;
            }

            switch (message[2]&0b00000111){ // sensor zone // 40 - 20 groen // oranje // rood //
                case 0b00000001: //zone 1 (10 cm)
                    updateSensor(R.color.RED,sensor);
                    break;
                case 0b00000010: //zone 2 (10 - 30 cm)
                    updateSensor(R.color.ORANGE,sensor);
                    break;
                case 0b00000011: //zone 3 (30 -50)
                    updateSensor(R.color.YELLOW,sensor);
                    break;
                case 0b00000100: //zone 4 // 50<
                    updateSensor(R.color.GREEN,sensor);
                    break;
                case 0b00000101: //zone 5 not used
                    break;
            }

            //TODO richting blokeren waar rood wordt gedetecteerd

            switch (message[2]>>4){ // direction
                case 0: // halt
                    //TODO
                    break;
                case 1: // links
                    switch (degree){        // check the current direction and change it with a new direction
                        case 0:
                            degree = 270;
                            break;
                        case 45:
                            degree = 315;
                            break;
                        case 90:
                            degree = 0;
                            break;
                        case 135:
                            degree = 45;
                            break;
                        case 180:
                            degree = 90;
                            break;
                        case 225:
                            degree = 135;
                            break;
                        case 270:
                            degree = 180;
                            break;
                        case 315:
                            degree = 225;
                            break;
                    }
                    break;
                case 2: // vooruit links
                    //TODO
                    break;
                case 3: // vooruit
                    if(Rdrive && Ldrive){ //TODO
                        switch (degree){
                            case 0:
                                // add to y
                                coords[Y][coordnumber] += message_distance;
                                coordnumber++;
                                break;
                            case 45:
                                // add to x and y //TODO
                                break;
                            case 90:
                                // add to x
                                coords[X][coordnumber] += message_distance;
                                coordnumber++;
                                break;
                            case 135:
                                // min to y and add to x //TODO
                                break;
                            case 180:
                                coords[Y][coordnumber] += -message_distance;
                                coordnumber++;
                                // min to y
                                break;
                            case 225:
                                // min to x and y //TODO
                                break;
                            case 270:
                                // min to x
                                coords[X][coordnumber] += -message_distance;
                                coordnumber++;
                                break;
                            case 315:
                                // min to x and add to y //TODO
                                break;
                        }
                    }
                    break;
                case 4: // vooruit rechts
                    //TODO
                    break;
                case 5: // rechts
                    switch (degree){
                        case 0:
                            degree = 90;
                            break;
                        case 45:
                            degree = 135;
                            break;
                        case 90:
                            degree = 180;
                            break;
                        case 135:
                            degree = 225;
                            break;
                        case 180:
                            degree = 270;
                            break;
                        case 225:
                            degree = 315;
                            break;
                        case 270:
                            degree = 0;
                            break;
                        case 315:
                            degree = 45;
                            break;
                    }
                    break;
                case 6: // achteruit rechts
                    //TODO
                    break;
                case 7: // achteruit
                    if(Rdrive && Ldrive){ //TODO
                        switch (degree){
                            case 0:
                                // min to y
                                coords[Y][coordnumber] += -message_distance;
                                coordnumber++;
                                break;
                            case 45:
                                // min to x and y //TODO
                                break;
                            case 90:
                                // min to x
                                coords[X][coordnumber] += -message_distance;
                                coordnumber++;
                                break;
                            case 135:
                                // add to y and min to x //TODO
                                break;
                            case 180:
                                // add to y
                                coords[Y][coordnumber] += message_distance;
                                coordnumber++;
                                break;
                            case 225:
                                // add to x and y //TODO
                                break;
                            case 270:
                                coords[X][coordnumber] += -message_distance;
                                coordnumber++;
                                break;
                            case 315:
                                // add to x and min to y //TODO
                                break;
                        }
                    }
                    break;
                case 8: // achteruit links
                        //TODO
                    break;
            }
            if(run){
                if (ConnectTask.mTcpClient != null) {
                    ConnectTask.mTcpClient.sendMessage(coords[X][coordnumber]+","+coords[Y][coordnumber]+"<DP>");
                }
            }
        }
    }
    public static void BTON(boolean state){
        if(state) BT.setImageResource(android.R.drawable.button_onoff_indicator_on);
        else BT.setImageResource(android.R.drawable.button_onoff_indicator_off);
    }
}
