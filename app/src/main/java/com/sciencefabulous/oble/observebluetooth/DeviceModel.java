package com.sciencefabulous.oble.observebluetooth;

import android.bluetooth.BluetoothDevice;
import android.widget.TextView;

import com.airbnb.epoxy.EpoxyModel;
import com.airbnb.epoxy.EpoxyModelWithHolder;

import java.math.BigInteger;

import butterknife.Bind;


public class DeviceModel extends EpoxyModelWithHolder<DeviceModel.DeviceHolder> {

    String deviceName;
    String deviceAddress;

    @Override
    protected int getDefaultLayout() {
        return R.layout.device_model_layout;
    }

    public DeviceModel(BluetoothDevice device) {
        deviceName = device.getName();
        deviceAddress = device.getAddress();
        String simpleAddress = deviceAddress.replace(":", "");
        long id = new BigInteger(simpleAddress, 16).longValue();
        this.id(id);
    }

    @Override
    public void bind(DeviceHolder holder) {
        holder.deviceName.setText(deviceName);
        holder.deviceAddress.setText(deviceAddress);
    }

    @Override
    protected DeviceHolder createNewHolder() {
        return new DeviceHolder();
    }

    static class DeviceHolder extends BaseEpoxyHolder {
        @Bind(R.id.deviceName) TextView deviceName;
        @Bind(R.id.deviceAddress) TextView deviceAddress;
    }
}
