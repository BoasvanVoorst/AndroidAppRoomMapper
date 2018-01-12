package com.jabo.jabo.roommapper;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Server on 12/01/2018.
 */

public class Settings extends PreferenceActivity{

    @Override
    public void onCreate(Bundle BundleSavedInstance){
        super.onCreate(BundleSavedInstance);
        addPreferencesFromResource(R.xml.settings);
    }
}
