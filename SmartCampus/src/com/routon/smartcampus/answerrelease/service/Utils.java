package com.routon.smartcampus.answerrelease.service;

import android.util.Log;

public class Utils {
	private static final String TAG = "Utils";
	public static final String serviceUuid = "0-F-E-E-0";
	public static final String characteristicUUID = "0-E-E-E-0";

	public static final int BLUETOOTH_DISCOVERABLE = 0;
	public static final int READ_FINISH = 1;
	public static final int DATA_ERROR = 2;
	public static final int GETID = 4;
	public static final int CURSOCKET = 5;
	public static final int BT_SET_NAME = 6;
	public static final int NET_SET_SUCCESS = 100;
	public static final int NET_SET_FAILED = 101;
	public static final int ROUTON_GENERAL_ACK = 200;
	public static final int BONDED_S1701_FOUND = 300;
	public static final int LOST_BONDED_S1701 = 400;
	public static final int CONNECT_STATE_CHANGED = 500;
	public static final int ATTENCE_FINISHED = 600;
	public static final String ERROR = "data is error!!!";
	public static int SOCKET_COUNT = 100;

	public static final String BLUETOOTH_DISCOVERABLE_TIME = "120";

	public static String getBinaryString(String config) {
		String indexString = "0123456789abcdef";
		String configString = config.substring(config.length() - 1, config.length());
		int indexOfConfigNum = indexString.indexOf(configString);
		String ConfigBinary = Integer.toBinaryString(indexOfConfigNum);
		if (ConfigBinary.length() < 4) {
			for (int i = ConfigBinary.length(); i < 4; i++) {
				ConfigBinary = "0" + ConfigBinary;
			}
		}
		return ConfigBinary;
	}

	public static int[] getBinaryArray(String binaryString) {
		int[] tmp = new int[4];
		for (int i = 0; i < binaryString.length(); i++) {
			String tmpString = String.valueOf(binaryString.charAt(i));
			tmp[i] = Integer.parseInt(tmpString);
		}
		return tmp;
	}

	public static String arrayToString(int[] array) {
		String getIndexString = "0123456789abcdef";
		int total = 0;
		System.out.println();
		for (int i = 0; i < array.length; i++) {
			total = total + (int) (array[i] * Math.pow(2, array.length - i - 1));
		}
		Log.d(TAG, "in arrayToString cecConfig is:" + total);
		String cecConfig = "cec" + getIndexString.charAt(total);
		Log.d(TAG, "in arrayToString cecConfig is:" + cecConfig);
		return cecConfig;
	}

	public static byte[] toByteArray(int iSource, int iArrayLen) {
		byte[] bLocalArr = new byte[iArrayLen];
		for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
			bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
		}
		return bLocalArr;
	}
}
