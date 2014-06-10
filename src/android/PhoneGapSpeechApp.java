package com.nuance.speechkit.phonegap;

import android.os.Bundle;
import android.util.Log;


import org.apache.cordova.*;

public class PhoneGapSpeechApp extends DroidGap {
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PhoneGapSpeechApp", "onCreate: Entered method.");
        super.loadUrl("file:///android_asset/www/index.html");
    }
}