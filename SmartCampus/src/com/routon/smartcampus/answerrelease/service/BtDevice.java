package com.routon.smartcampus.answerrelease.service;

import java.io.Serializable;

import android.bluetooth.BluetoothDevice;

public class BtDevice implements Serializable{
	
	private BluetoothDevice device;
	private String name;
	private short rssi;
	private String status = "未连接";
	private String className;
	private int groupId;
	private boolean checked;
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public BtDevice(BluetoothDevice device, short rssi)
	{
		this.name = device.getName();
		this.device = device;
		this.rssi = rssi;
	}
	
	public BtDevice(BluetoothDevice device, String status)
	{
		this.device = device;
		this.status = status;
	}
	
	public BtDevice(String className, int groupId){
		this.className = className;
		this.groupId = groupId;
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public BluetoothDevice getDevice() {
		return device;
	}
	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}
	public short getRssi() {
		return rssi;
	}
	public void setRssi(short rssi) {
		this.rssi = rssi;
	}
	public String getStatus()
	{
		return status;
	}
	public void setStatus(String status)
	{
		this.status = status;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
}
