package com.sciencefabulous.oble.observebluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BleAct";
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    private DeviceAdapter mAdapter;
    private BleController mBleController;
    private boolean mBound = false;
    private Subscription mReadWindowSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        ButterKnife.bind(this);
        mAdapter = new DeviceAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        startBindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
        }
    }

    private void startBindService() {
        Log.i(TAG, "Binding to BleController Service");
        Intent bleController = new Intent(MainActivity.this, BleController.class);
        startService(bleController);
        bindService(bleController, mConnection, Context.BIND_ABOVE_CLIENT);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            Log.i(TAG, "BleController Service Connected");
            mBound = true;
            BleController.BleBinder bleBinder = (BleController.BleBinder) binder;
            mBleController = bleBinder.getService();
            mReadWindowSubscription = mBleController.readWindows.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Observable<BluetoothDevice>>() {
                        @Override
                        public void call(Observable<BluetoothDevice> window) {
                            Log.i(TAG, "Got window");
                            mAdapter.clearAll();
                            window.observeOn(AndroidSchedulers.mainThread())
                                    .filter(new Func1<BluetoothDevice, Boolean>() {
                                        @Override
                                        public Boolean call(BluetoothDevice bluetoothDevice) {
                                            return bluetoothDevice != null && bluetoothDevice.getName() != null;
                                        }
                                    }).distinct(new Func1<BluetoothDevice, String>() {
                                @Override
                                public String call(BluetoothDevice bluetoothDevice) {
                                    return bluetoothDevice.getAddress();
                                }
                            }).subscribe(
                                    new Action1<BluetoothDevice>() {
                                        @Override
                                        public void call(BluetoothDevice device) {
                                            mAdapter.addDevice(device);
                                        }
                                    });
                        }
                    });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "BleController Service Disconnected");
            mBound = false;
            if (mReadWindowSubscription != null) {
                mReadWindowSubscription.unsubscribe();
            }
        }
    };
}
