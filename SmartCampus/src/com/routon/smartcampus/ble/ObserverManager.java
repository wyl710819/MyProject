package com.routon.smartcampus.ble;




import java.util.ArrayList;
import java.util.List;

import com.routon.smartcampus.ble.BleDevice;


public class ObserverManager implements Observable {

    public static ObserverManager getInstance() {
        return ObserverManagerHolder.sObserverManager;
    }

    private static class ObserverManagerHolder {
        private static final ObserverManager sObserverManager = new ObserverManager();
    }

    private List<Observer> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer obj) {
        observers.add(obj);
    }

    @Override
    public void deleteObserver(Observer obj) {
        int i = observers.indexOf(obj);
        if (i >= 0) {
            observers.remove(obj);
        }
    }

    @Override
    public void notifyObserver(BleDevice bleDevice) {
        for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.disConnected(bleDevice);
        }
    }

	@Override
	public void notifyObserverNotifySuccess() {
		// TODO Auto-generated method stub
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onNotifySuccess();
        }
	}
	
	@Override
	public void notifyObserverReceivColors(byte [][] colors) {
		// TODO Auto-generated method stub
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onReceivColors(colors);
        }
	}
	
	
	public void notifyObserverReceivBrightness(byte brightness) {
		// TODO Auto-generated method stub
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onReceivBrightness(brightness);
        }
	}
	public void onPermissioned()
	{
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onPermissioned();
        }
	}
	
	public void onStartConnect()
	{
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onStartConnect();
        }
	}
	public void onConnectFail()
	{
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onConnectFail();
        }
	}
	public void onConnectSuccess()
	{
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onConnectSuccess();
        }
	}
	public void onDisConnected()
	{
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onDisConnected();
        }
	}

	public void onWriteOk(String taskname) {
		// TODO Auto-generated method stub
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onWriteOk(taskname);
        }
	}

	public void onWriteFail() {
		// TODO Auto-generated method stub
		for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onWriteFail();
        }
	}

}
