package com.sciencefabulous.oble.observebluetooth;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by bakennedy on 9/21/16.
 */
public class OBApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }
}
