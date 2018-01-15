package com.jabo.jabo.roommapper;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import static android.R.drawable.button_onoff_indicator_off;
import static android.R.drawable.button_onoff_indicator_on;

public class Menu extends AppCompatActivity {
    private static final String TAG = "Menu";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        new ConnectTask().execute("");

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
