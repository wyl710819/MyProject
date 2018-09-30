package com.routon.smartcampus.ble;

import android.bluetooth.BluetoothGatt;


import com.routon.smartcampus.ble.BleDevice;


public interface Observer {

    void disConnected(BleDevice bleDevice);
    void onNotifySuccess();
    void onReceivColors(byte [][] colors);
    void onReceivBrightness(byte brightness);
    void onStartConnect();
    void onConnectFail();
    void onConnectSuccess();
    void onDisConnected();
	void onPermissioned();
	void onWriteOk(String name);
	void onWriteFail();
}
