package com.jabo.jabo.roommapper;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import java.util.ArrayList;
import java.util.UUID;

public class ControlPage extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private final String TAG = "ControlPage";

    protected PowerManager.WakeLock mWakeLock;

    private static Context context;
    private static Toast toast;
    private static byte[] direction = new byte[1024];
    private boolean run = false;

    ImageButton ForwardButton;
    ImageButton BackwardButton;
    ImageButton LeftButton;
    ImageButton RightButton;

    //region bt init
    BluetoothConnectionService mBluetoothConnection;

    BluetoothAdapter mBluetoothAdapter;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_page);
        final Activity var = this;
        Context context = getApplicationContext();
        this.context =context;

        //region Wakelock
        Log.d(TAG, "onCreate: Powermanager");
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"wakescreen");
        this.mWakeLock.acquire();
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
        //endregion

        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

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
        Log.d(TAG, "onDestroy: called.");
        this.mWakeLock.release();
        if(mBroadcastReceiver1 != null)
            unregisterReceiver(mBroadcastReceiver1);
        if(mBroadcastReceiver2 != null)
            unregisterReceiver(mBroadcastReceiver2);
        if(mBroadcastReceiver3 != null)
            unregisterReceiver(mBroadcastReceiver3);
        if(mBroadcastReceiver4 != null)
            unregisterReceiver(mBroadcastReceiver4);
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

    public void EngineOn(boolean state){
        ImageView engine = (ImageView) findViewById(R.id.engineImage);
        if(state){
            engine.setImageResource(android.R.drawable.presence_online);
        }
        else{
            engine.setImageResource(android.R.drawable.presence_busy);
        }
    }

    public void updateSensor(int color, int wich_sensor){
        ImageView sensor1 = (ImageView)findViewById(R.id.sensor1);
        ImageView sensor2 = (ImageView)findViewById(R.id.sensor2);
        ImageView sensor3 = (ImageView)findViewById(R.id.sensor3);
        ImageView sensor4 = (ImageView)findViewById(R.id.sensor4);
        ImageView sensor5 = (ImageView)findViewById(R.id.sensor5);
        ImageView Sensors[] = {sensor1,sensor2,sensor3,sensor4,sensor5};
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

    //region broadcastreceiver2
    /**
     * Broadcast Receiver for changes made to bluetooth states such as:
     * 1) Discoverability mode on/off or expire.
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };
    //endregion

    //region Broadcastreceiver3
    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
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
    //***remember the conncction will fail and app will crash if you haven't paired first
    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

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
        /*if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }*/
    }

    //region make discoverable (niet nodig)
    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);

    }
    //endregion


    public void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
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

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(ControlPage.this);
        }
    }
}
