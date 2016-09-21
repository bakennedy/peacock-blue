package com.sciencefabulous.oble.observebluetooth;

import android.bluetooth.BluetoothDevice;

import com.airbnb.epoxy.EpoxyAdapter;

import java.util.Collection;

/**
 * Created by bakennedy on 9/21/16.
 */
public class DeviceAdapter extends EpoxyAdapter {

    public DeviceAdapter() {
        enableDiffing();
    }

    public void addDevices(Collection<BluetoothDevice> devices) {
        for (BluetoothDevice device : devices) {
            DeviceModel model = new DeviceModel(device);
            if (! models.contains(model)) {
                addModel(model);
            }
            notifyModelsChanged();
        }
    }

    public void addDevice(BluetoothDevice device) {
        DeviceModel model = new DeviceModel(device);
        if (! models.contains(model)) {
            addModel(model);
            notifyModelsChanged();
        }
    }

    public void clearAll() {
        this.models.clear();
        notifyModelsChanged();
    }
}
