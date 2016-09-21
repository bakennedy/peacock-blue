package com.sciencefabulous.oble.observebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = "BleAct";
    @Bind(R.id.recycler_view) RecyclerView recyclerView;

    private DeviceAdapter adapter;

    public SerializedSubject<BluetoothDevice, BluetoothDevice> publishSubject;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        ButterKnife.bind(this);
        publishSubject = PublishSubject.<BluetoothDevice>create().toSerialized();
        adapter = new DeviceAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        publishSubject.window(1, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                new Action1<rx.Observable<BluetoothDevice>>() {
                    @Override
                    public void call(rx.Observable<BluetoothDevice> window) {
                        Log.i(TAG, "Got window");
                        adapter.clearAll();
                        window.observeOn(AndroidSchedulers.mainThread()).filter(new Func1<BluetoothDevice, Boolean>() {
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
                                    adapter.addDevice(device);
                                }
                        });
                    }
                }
        );
    }

    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        publishSubject.onNext(bluetoothDevice);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        bluetoothAdapter.startLeScan(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothAdapter.stopLeScan(this);
    }
}
