package com.sciencefabulous.oble.observebluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

public class BleController extends Service {

    private static final String TAG = BleController.class.getSimpleName();
    private boolean isScanning = false;
    BluetoothLeScannerCompat scanner;
    private SerializedSubject<BluetoothDevice, BluetoothDevice> deviceScanSubject;
    // readWindows emits a single new Observable for each Read Window
    // Each of these emitted observables emits the individual reads
    // as they're read by BLE scanning
    public Observable<Observable<BluetoothDevice>> readWindows;

    public BleController() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "BleController Service Created");
        deviceScanSubject = PublishSubject.<BluetoothDevice>create().toSerialized();
        readWindows = deviceScanSubject.window(1, TimeUnit.MINUTES);
        startScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "BleController Service Destroyed");
        deviceScanSubject.onCompleted();
        stopScan();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new BleBinder();
    }

    public class BleBinder extends Binder {
        public BleController getService() {
            return BleController.this;
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "Scan result " + result.toString());
            if (isScanning) {
                deviceScanSubject.onNext(result.getDevice());
            }
        }
    };

    private void startScan() {
        Log.i(TAG, "Starting Scan Mode");
        isScanning = true;
        scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setPowerSave(5000,4000)
                .setUseHardwareBatchingIfSupported(false)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        scanner.startScan(filters, settings, scanCallback);
    }

    private void stopScan() {
        Log.i(TAG, "Stopping Scan Mode");
        isScanning = false;
        if (scanner != null) {
            scanner.stopScan(scanCallback);
        }
    }
}
