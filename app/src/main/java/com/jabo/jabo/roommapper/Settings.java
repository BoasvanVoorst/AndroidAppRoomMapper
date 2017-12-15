package com.jabo.jabo.roommapper;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import static com.jabo.jabo.roommapper.Menu.mBluetoothAdapter;


public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void save (View v){

    }
    private ArrayList<BluetoothDevice> mDeviceList;
    private String devices[] = {"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};
    public void connect_device(View v){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

        list.addAll(pairedDevices);
        mDeviceList	= list;
        try {
            for (int i = 0; i < mDeviceList.size(); i++) {
                Log.d("devicenames",mDeviceList.get(i).getName());
                if(mDeviceList.get(i).getName() != null) {
                    devices[i] = mDeviceList.get(i).getName();
                }
                else{
                    Log.e("devicename","No device name found");
                }
            }
        }
        catch (NullPointerException e){
            Log.e("connect","get device name",e);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a device");
        builder.setItems(devices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                BluetoothDevice device = mDeviceList.get(which);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    //unpairDevice(device);
                } else {
                    showToast("Pairing...");

                    pairDevice(device);
                }
            }
        });
        builder.show();
    }

    public void wake_screen(View v){

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
