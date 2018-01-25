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
    public static boolean run = false;
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
    protected void onResume(){
        super.onResume();
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sending = true;
                Log.d(TAG, "run: sendThread started");
                while(sending) {
                    try {
                        //Log.d(TAG, "run: direction send: "+direction[0]);
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
                Log.d(TAG, "run: sendThread stopped");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_page);
        direction[0] = 0;
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sending = true;
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
                Log.d(TAG, "run: sendThread stopped");
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
            RoomName.setEnabled(false);
            mBluetoothConnection.zero();
            final String _input = input;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (ConnectTask.mTcpClient != null) {
                        ConnectTask.mTcpClient.sendMessage("Start<LOG>");
                    }
                    if (ConnectTask.mTcpClient != null) {
                        ConnectTask.mTcpClient.sendMessage(_input+"<NAME>");
                    }
                }
            }).start();
        }
        else if (!equals) // stops measuring
        {
            popup("stopped");
            run = false;
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
            //Log.d(TAG, "updateSensor: sensor: "+result);
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
        run = false;
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
        finish();
    }

    @Override
    public void onPause(){
        super.onPause();
        mBluetoothConnection.cancel();
        sendThread = null;
    }

    public static void BTON(boolean state){ // updates the BTConnected image and if bluetooth connected it starts the send thread
        if(state) {
            BT.setImageResource(android.R.drawable.button_onoff_indicator_on);
            sendThread.start();
        }
        else BT.setImageResource(android.R.drawable.button_onoff_indicator_off);
    }
}
