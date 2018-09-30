package com.routon.smartcampus.ble;

import com.routon.smartcampus.ble.BleDevice;





public interface Observable {

    void addObserver(Observer obj);

    void deleteObserver(Observer obj);

    void notifyObserver(BleDevice bleDevice);
    
    void notifyObserverNotifySuccess();

	void notifyObserverReceivColors(byte[][] colors);
}
