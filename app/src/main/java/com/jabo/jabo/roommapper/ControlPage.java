package com.jabo.jabo.roommapper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.PowerManager;
import com.jabo.jabo.BT.BluetoothConnectionService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ControlPage extends AppCompatActivity/* implements AdapterView.OnItemClickListener*/{
    private final String TAG = "ControlPage";

    protected PowerManager.WakeLock mWakeLock;

    private static Thread sendThread;
    private static Context context;
    private static Toast toast;
    public static byte[] direction = new byte[1];
    private static boolean run = false;
    private static boolean sending = true;
    private SharedPreferences systemPreferences;

    static ImageButton ForwardButton;
    static ImageButton BackwardButton;
    static ImageButton LeftButton;
    static ImageButton RightButton;
    static ImageView BT;
    static ImageView engine;
    static ImageView sensor1;
    static ImageView sensor2;
    static ImageView sensor3;
    static ImageView sensor4;
    static ImageView sensor5;
    static ImageView Sensors[] = new ImageView[5];

    //region bt init
    private BluetoothConnectionService mBluetoothConnection;
    private BluetoothAdapter mBluetoothAdapter;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothDevice mBTDevice;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_page);
        direction[0] = 0;
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sendThread started");
                while(sending) {
                    try {
                        Log.d(TAG, "run: direction send: "+direction[0]);
                        mBluetoothConnection.write(direction);
                        try {
                            sendThread.sleep(25);
                        }
                        catch(InterruptedException e){
                        }
                    }
                    catch (IOException e){
                    }
                }
            }
        });

        Context context = getApplicationContext();
        this.context = context;
        engine = (ImageView) findViewById(R.id.engineImage);
        Sensors[0] = sensor1 = (ImageView)findViewById(R.id.sensor1);
        Sensors[1] = sensor2 = (ImageView)findViewById(R.id.sensor2);
        Sensors[2] = sensor3 = (ImageView)findViewById(R.id.sensor3);
        Sensors[3] = sensor4 = (ImageView)findViewById(R.id.sensor4);
        Sensors[4] = sensor5 = (ImageView)findViewById(R.id.sensor5);
        BT = (ImageView) findViewById(R.id.Bluetooth);
        systemPreferences = this.getSharedPreferences("com.jabo.jabo.roommapper_preferences",MODE_PRIVATE);
        //region Wakelock

        Log.d(TAG, "onCreate: Powermanager");
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakescreen");

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
                }
                return false;
            }
        });
        //endregion

        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver2, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        enableDisableBT();

        connectDevice();
    }

    public void connectDevice(){ // checks for bluetooth device
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

    private void start(){           // runs when start button is pressed
        Button samplesButtonadd = (Button) findViewById(R.id.addSample);
        Button samplesButtonmin = (Button) findViewById(R.id.minsample);
        TextView RoomName = (TextView) findViewById(R.id.RoomName);
        String input = RoomName.getText().toString();
        boolean equals = false;

        if (input.equalsIgnoreCase("Room Name")){ // checks if roomname is not empty and does not have the standard name
            equals = true;
            popup("please enter a new room name");
        }
        else if (input.equalsIgnoreCase("")){
            equals = true;
            popup("please enter a room name");
        }

        if (!run && !equals){   // if measuring is not started and if it has a correct file name than start measuring and log this to the server.
            run = true;
            popup("started");
            samplesButtonadd.setEnabled(false);
            samplesButtonmin.setEnabled(false);
            RoomName.setEnabled(false);
            if (ConnectTask.mTcpClient != null) {
                ConnectTask.mTcpClient.sendMessage("Start<LOG>");
            }
            if (ConnectTask.mTcpClient != null) {
                ConnectTask.mTcpClient.sendMessage(input+"<NAME>");
            }
        }
        else if (!equals) // stops measuring
        {
            popup("stopped");
            run = false;
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

    public static void popup(String message){       // simple popup message
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

    public static void EngineOn(boolean state){     //updates engine image from thread
        if(state){
            engine.setImageResource(android.R.drawable.presence_online);
        }
        else{
            engine.setImageResource(android.R.drawable.presence_busy);
        }
    }

    public static void updateSensor(int color, int wich_sensor){// updates sensor images from thread
        int result;
        String TAG = "updateSensor";
        if (wich_sensor != 0){
            result = wich_sensor -1;
            Log.d(TAG, "updateSensor: sensor: "+result);
            Sensors[result].setColorFilter(color);
        }
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

    //region Broadcastreceiver2
    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver2
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

    public void startBTConnection(BluetoothDevice device, UUID uuid){           // starts Bluetooth connection
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection = new BluetoothConnectionService(ControlPage.this);
        mBluetoothConnection.startClient(device,uuid);
    }

    public void enableDisableBT(){      // enables Bluetooth
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){     // if bluetooth not is enabled request to enable Bluetooth
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
    protected void onDestroy(){     // if application gets closed destroys al threads and unregisters broadcast receivers
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        if(systemPreferences.getBoolean("screen_awake",true)) this.mWakeLock.release();
        mBluetoothConnection.cancel();
        sending = false;
        sendThread = null;
        try {
            unregisterReceiver(mBroadcastReceiver1);
        }
        catch(IllegalArgumentException e){
            Log.e(TAG, "onDestroy: BroadcastReceiver1",e );
        }
        try {
            unregisterReceiver(mBroadcastReceiver2);
        }
        catch(IllegalArgumentException e){
            Log.e(TAG, "onDestroy: BroadcastReceiver2",e );
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        mBluetoothConnection.cancel();
        sendThread = null;
    }

    private static int count = 0;
    private static byte[] buffer = new byte[6];
    public static void receiveBTMessage(byte message){
        String TAG = "receiveBTMessage";
        int sensor = 0;
        boolean Ldrive;
        boolean Rdrive;

        /*if((message[0]&0xAF) == 0xAF){
            Log.d(TAG, "receiveBTMessage: init message received");

            if((message[1]&0b10000000) == 0b10000000){ // motor active
                EngineOn(true);
                Log.d(TAG, "receiveBTMessage: engine on");
            }
            else{
                EngineOn(false);
                Log.d(TAG, "receiveBTMessage: engine off");
            }

            if((message[1]&0b01000000)==0b01000000){
                Ldrive = true;
                Log.d(TAG, "receiveBTMessage: Ldrive on");
            }
            else{
                Ldrive = false;
                Log.d(TAG, "receiveBTMessage: Ldrive off");
            }

            if((message[1]&0b00100000)==0b00100000){
                Rdrive = true;
                Log.d(TAG, "receiveBTMessage: Rdrive on");
            }
            else{
                Rdrive = false;
                Log.d(TAG, "receiveBTMessage: Rdrive off");
            }

            switch (message[1]&0b00011111){// sensor number
                case 0:
                    sensor = 0;
                    Log.d(TAG, "receiveBTMessage: sensor 0");
                    break;
                case 1:
                    sensor = 1;
                    Log.d(TAG, "receiveBTMessage: sensor 1");
                    break;
                case 2:
                    sensor = 2;
                    Log.d(TAG, "receiveBTMessage: sensor 2");
                    break;
                case 3:
                    sensor = 3;
                    Log.d(TAG, "receiveBTMessage: sensor 3");
                    break;
                case 4:
                    sensor = 4;
                    Log.d(TAG, "receiveBTMessage: sensor 4");
                    break;
                case 5:
                    sensor = 5;
                    Log.d(TAG, "receiveBTMessage: sensor 5");
                    break;
            }

            switch (message[2]&0b00000111){ // sensor zone // 40 - 20 groen // oranje // rood //
                case 1: //zone 1 (10 cm)
                    updateSensor(R.color.RED,sensor);
                    Log.d(TAG, "receiveBTMessage: sensor "+ sensor + " Red");
                    break;
                case 2: //zone 2 (10 - 30 cm)
                    updateSensor(R.color.ORANGE,sensor);
                    Log.d(TAG, "receiveBTMessage: sensor "+ sensor + " Orange");
                    break;
                case 3: //zone 3 (30 -50)
                    updateSensor(R.color.YELLOW,sensor);
                    Log.d(TAG, "receiveBTMessage: sensor "+ sensor + " Yellow");
                    break;
                case 4: //zone 4 // 50<
                    updateSensor(R.color.GREEN,sensor);
                    Log.d(TAG, "receiveBTMessage: sensor "+ sensor + " Green");
                    break;
                case 5: //zone 5 not used
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
        /*Log.d(TAG, "receiveBTMessage: empty received message");
        for(int i = 0; i<message.length;i++){
            message[i]=0;
        }*/
    }

    public static void BTON(boolean state){ // updates the BTConnected image and if bluetooth connected it starts the send thread
        if(state) {
            BT.setImageResource(android.R.drawable.button_onoff_indicator_on);
            sendThread.start();
        }
        else BT.setImageResource(android.R.drawable.button_onoff_indicator_off);
    }
}
