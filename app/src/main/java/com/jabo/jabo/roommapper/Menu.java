package com.jabo.jabo.roommapper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jabo.jabo.BT.BTConnected;
import com.jabo.jabo.BT.BTConnectie;

import java.util.Set;

import static android.R.drawable.button_onoff_indicator_off;
import static android.R.drawable.button_onoff_indicator_on;

public class Menu extends AppCompatActivity {
    private static final String TAG = "Menu";
    public static BTConnectie BT;
    public static BluetoothAdapter mBluetoothAdapter = null;
    boolean devicefound = false;
    public static Handler mmHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        final Activity var = this;
        mmHandler=null;
        mmHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputmessage){
                switch (inputmessage.what){
                    case BTConnected.ENGINE_ON:
                        Log.d(TAG, "handleMessage: Engine on");
                        break;
                    case BTConnected.ENGINE_OFF:
                        Log.d(TAG, "handleMessage: Engine off");
                        break;
                    case BTConnected.UPDATE_SENSOR:
                        Log.d(TAG, "handleMessage: Update sensor"+inputmessage.arg1);
                        int color = inputmessage.arg1;
                        int sensor = inputmessage.arg2;
                        break;
                    case BTConnected.CONNECTION_FAILED:
                        Toast.makeText(var, "BT connection failed",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        new ConnectTask().execute("");
        // configfile
        // wifi verbinding
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            ImageView img = (ImageView) findViewById(R.id.imageView2);
            img.setImageResource(button_onoff_indicator_on);
        }
        else{
            ImageView img = (ImageView) findViewById(R.id.imageView2);
            img.setImageResource(button_onoff_indicator_off);
        }
        // bt verbinding
        int REQUEST_ENABLE_BT = 1;
        String btdeviceName = "JaBo";
        //String btdeviceHardwareAddress = "F6:D0:05:15:59:26";
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        String devices[] = new String[] {};
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            int i =0;
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceName.equalsIgnoreCase(btdeviceName) /*&& deviceHardwareAddress.equalsIgnoreCase(btdeviceHardwareAddress)*/){
                    devicefound = true;
                    try {
                        BT = new BTConnectie(device,mmHandler);
                        ImageView img = (ImageView) findViewById(R.id.imageView);
                        img.setImageResource(button_onoff_indicator_on);
                        BT.start();
                    }
                    catch (Exception e){
                        ImageView img = (ImageView) findViewById(R.id.imageView);
                        img.setImageResource(button_onoff_indicator_off);
                    }
                    break;
                }
                else{
                    i++;
                    devicefound = false;
                }
            }
        }
        if (devicefound == false){
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            ImageView img = (ImageView) findViewById(R.id.imageView);
            img.setImageResource(button_onoff_indicator_off);
        }
        else{
            ImageView img = (ImageView) findViewById(R.id.imageView);
            img.setImageResource(button_onoff_indicator_on);
        }
        Button settingspage = (Button) findViewById(R.id.SettingsButton);
        settingspage.setEnabled(false); //TODO settingspage deactivated
    }

    @Override
    public void onResume(){
        super.onResume();
        int REQUEST_ENABLE_BT = 1;
        String btdeviceName = "JaBo";
        //String btdeviceHardwareAddress = "F6:D0:05:15:59:26";
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        String devices[] = new String[] {};
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            int i =0;
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceName.equalsIgnoreCase(btdeviceName) /*&& deviceHardwareAddress.equalsIgnoreCase(btdeviceHardwareAddress)*/){
                    devicefound = true;
                    try {
                        BT = new BTConnectie(device,mmHandler);
                        ImageView img = (ImageView) findViewById(R.id.imageView);
                        img.setImageResource(button_onoff_indicator_on);
                        BT.start();
                    }
                    catch (Exception e){
                        ImageView img = (ImageView) findViewById(R.id.imageView);
                        img.setImageResource(button_onoff_indicator_off);
                    }
                    break;
                }
                else{
                    i++;
                    devicefound = false;
                }
            }
        }
        if (devicefound == false){
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            ImageView img = (ImageView) findViewById(R.id.imageView);
            img.setImageResource(button_onoff_indicator_off);
        }
        else{
            ImageView img = (ImageView) findViewById(R.id.imageView);
            img.setImageResource(button_onoff_indicator_on);
        }
    }

    public void SettingsPage(View view){
        Intent intent = new Intent(this,Settings.class);
        startActivity(intent);
    }

    public void MapPage(View view){
        Intent intent = new Intent(this,MappingPage.class);
        startActivity(intent);
    }

    public void ControlPage(View view){
        Intent intent = new Intent(this,ControlPage.class);
        startActivity(intent);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!devicefound) {
            unregisterReceiver(mReceiver);
        }
        if(BT != null) {
            try {
                BT.cancel();
            }
            catch (Exception e){
                Log.e("BT","cancel",e);
            }
        }
    }

}
