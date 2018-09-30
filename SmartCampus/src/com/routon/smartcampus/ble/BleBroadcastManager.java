package com.routon.smartcampus.ble;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.routon.smartcampus.ble.BleScanCallback;
import com.routon.smartcampus.ble.BleScanRuleConfig;
import com.routon.smartcampus.ble.BleScanState;
import com.routon.smartcampus.ble.BleScanner;
import com.routon.edurelease.R;
import com.routon.smartcampus.ble.BleLog;
import com.routon.smartcampus.ble.HexUtil;
import com.routon.smartcampus.ble.BleUtil;
import com.routon.smartcampus.ble.BluetoothUUID;
import com.routon.smartcampus.ble.ObserverManager;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Beacon Manager
 * 1. setMajor
 * 2. setMinor
 * 3. setTxPower
 * 4. startAdvertising
 * 5. stopAdvertising
 *
 * Created by charles on 17/7/3.
 */

public class BleBroadcastManager {
    public static final int DEFAULT_SCAN_TIME = 10000;
    public static final String TAG = "lock_mock_manager";
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothAdvertiser;
    private BleScanner bleScanner;
    private BleScanRuleConfig bleScanRuleConfig;
    private Activity mContext;

    private static class SingletonHolder {
        static final BleBroadcastManager instance = new BleBroadcastManager();
    }

    /**
     * Return the Acquisition singleton.
     */
    public synchronized static BleBroadcastManager getInstance() {
        return SingletonHolder.instance;
    }

    private BleBroadcastManager() {

    	bleScanner = BleScanner.getInstance();
        bleScanRuleConfig = new BleScanRuleConfig();
    }

    public boolean isBlueEnable() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
    /**
     * Get the BleScanner
     *
     * @return
     */
    public BleScanner getBleScanner() {
        return bleScanner;
    }
    
    
    /**
     * Cancel scan
     */
    public void cancelScan() {
    	if (bleScanner == null)
    	{
    		return;
    	}
        bleScanner.stopLeScan();
    }
    
    public BleScanState getScanSate() {
        return bleScanner.getScanState();
    }

    /**
     * scan device around
     *
     * @param callback
     */
    public void scan(BleScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleScanCallback can not be Null!");
        }

        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!");
            callback.onScanStarted(false);
            return;
        }

        UUID[] serviceUuids = bleScanRuleConfig.getServiceUuids();
        String[] deviceNames = bleScanRuleConfig.getDeviceNames();
        String deviceMac = bleScanRuleConfig.getDeviceMac();
        boolean fuzzy = bleScanRuleConfig.isFuzzy();
        long timeOut = bleScanRuleConfig.getScanTimeOut();

        bleScanner.scan(serviceUuids, deviceNames, deviceMac, fuzzy, timeOut, callback);
    }
    @SuppressLint("NewApi")
	public void setContext(Activity context) {
        mContext = context;

        //初始化BluetoothManager和BluetoothAdapter
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(BLUETOOTH_SERVICE);
        }

        if (mBluetoothManager != null && mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    @SuppressLint("NewApi")
	public void init(Activity activity, int requestCode) {

        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, "不支持ble", Toast.LENGTH_LONG).show();
//            activity.finish();
            return;
        }

        final BluetoothManager mBluetoothManager = (BluetoothManager) activity.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, "不支持ble", Toast.LENGTH_LONG).show();
//            activity.finish();
            return;
        }

        mBluetoothAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothAdvertiser == null) {
//            Toast.makeText(activity, "the device not support peripheral", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "the device not support peripheral");
//            activity.finish();
            return;
        }

        //打开蓝牙的套路
        if ((mBluetoothAdapter == null) || (!mBluetoothAdapter.isEnabled())) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, requestCode);
        }
    }

    private short mMajor = 10001;
    private short mMinor = 12345;
    private int mTxPower = 59;

    public void setMajor(int major) {
        mMajor = (short) major;
    }

    public void setMinor(int minor) {
        mMinor = (short) minor;
    }

    public void setTxPower(int txPower) {
        mTxPower = txPower;
    }

    @SuppressLint("NewApi")
	public void startAdvertising() {
        //获取BluetoothLeAdvertiser，BLE发送BLE广播用的一个API
        if (mBluetoothAdvertiser == null) {
            mBluetoothAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        }
        if (mBluetoothAdvertiser != null) {
            try {
                //创建BLE beacon Advertising并且广播
            	
            	AdvertiseData data = BleUtil.createIBeaconAdvertiseData(BluetoothUUID.bleServerUUID,
                        mMajor, mMinor, (byte) -0x3b);
            	
            	 AdvertiseSettings mSettings=new AdvertiseSettings.Builder().setConnectable(true).setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setTimeout(0).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).build();

                 AdvertiseData.Builder advertiseDataBuilder=new AdvertiseData.Builder().setIncludeDeviceName(false);
                
                 
                 int size_1=data.getManufacturerSpecificData().size();
                 for (int i=0;i<size_1;i++){
                     int key=data.getManufacturerSpecificData().keyAt(i);
                     byte[] value=data.getManufacturerSpecificData().get(key);
                     advertiseDataBuilder.addManufacturerData(key,value);
                     BleLog.e(" Manufacturer id:"+key+" data:"+HexUtil.formatHexString(value));
                 }
//                 int size_2=data.getServiceUuids().size();
//                 for (int i=0;i<size_2;i++){
//                     ParcelUuid uuid=data.getServiceUuids().get(i);
//                     advertiseDataBuilder.addServiceUuid(uuid);
//                     BleLog.e(" service uuid:"+uuid.getUuid().toString());
//                 }
//
//                 Set<ParcelUuid> keySet= data.getServiceData().keySet();
//                 for (ParcelUuid key_uuid:keySet){
//                	 BleLog.e(" service data uuid:"+key_uuid.getUuid().toString()+" data:"+ HexUtil.formatHexString(data.getServiceData().get(key_uuid)));
//                     advertiseDataBuilder.addServiceData(key_uuid,data.getServiceData().get(key_uuid));
//                 }

                 AdvertiseData mAdvertiseData=advertiseDataBuilder.build();

//                 mAdvertiseCallback=new AdvertiseCallback() {
//                     @Override
//                     public void onStartSuccess(AdvertiseSettings settingsInEffect) {
//                         if(okbleAdvertiseCallback!=null){
//                             okbleAdvertiseCallback.onStartSuccess();
//                         }
//                     }
//                     @Override
//                     public void onStartFailure(int errorCode) {
//                         if(okbleAdvertiseCallback!=null){
//                             okbleAdvertiseCallback.onStartFailure(errorCode,OKBLEAdvertiseFailedDescUtils.getDesc(errorCode));
//                         }
//                     }
//                 };
                 
                 
                mBluetoothAdvertiser.startAdvertising(mSettings,
                         mAdvertiseData,
                         mAdvCallback);
            } catch (Exception e) {
                Log.v(TAG, "Fail to setup BleService");
            }
        }
    }

    @SuppressLint("NewApi")
	public AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        //设置广播的模式,应该是跟功耗相关
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        builder.setConnectable(connectable);
        builder.setTimeout(timeoutMillis);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings mAdvertiseSettings = builder.build();
        if (mAdvertiseSettings == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseSettings;
    }

    //发送广播的回调，onStartSuccess/onStartFailure很明显的两个Callback
    @SuppressLint("NewApi")
	private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        public void onStartSuccess(android.bluetooth.le.AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
//            Toast.makeText(mContext, "Advertise Start Success", Toast.LENGTH_SHORT).show();
            if (settingsInEffect != null) {
                Log.d(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode() + " timeout=" + settingsInEffect.getTimeout());
            } else {
                Log.d(TAG, "onStartSuccess, settingInEffect is null");
            }
        }

        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d(TAG, "onStartFailure errorCode=" + errorCode);

            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
//                Toast.makeText(mContext, "advertise_failed_data_too_large", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
//                Toast.makeText(mContext, "advertise_failed_too_many_advertises", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising because no advertising instance is available.");

            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
//                Toast.makeText(mContext, "advertise_failed_already_started", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising as the advertising is already started");

            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
//                Toast.makeText(mContext, "advertise_failed_internal_error", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Operation failed due to an internal error");

            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
//                Toast.makeText(mContext, "advertise_failed_feature_unsupported", Toast.LENGTH_LONG).show();
                Log.e(TAG, "This feature is not supported on this platform");

            }
        }
    };

    @SuppressLint("NewApi")
	public void stopAdvertising() {
        //关闭BluetoothLeAdvertiser，BluetoothAdapter，BluetoothGattServer
        if (mBluetoothAdvertiser != null) {
            mBluetoothAdvertiser.stopAdvertising(mAdvCallback);
            mBluetoothAdvertiser = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter = null;
        }
    }

	public BluetoothAdapter getBluetoothAdapter() {
		// TODO Auto-generated method stub
		return mBluetoothAdapter;
	}

	public void initScanRule(BleScanRuleConfig scanRuleConfig) {
		// TODO Auto-generated method stub
		this.bleScanRuleConfig = scanRuleConfig;
	}

	
	public void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
//            Toast.makeText(mContext, mContext.getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            bluetoothAdapter.enable();
//            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<String>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(mContext, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(mContext, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }
 private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        	mContext.finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            mContext.startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                	BleLog.e("onPermissionGranted ...");
                	ObserverManager.getInstance().onPermissioned();
                }
                break;
        }
    }
    public boolean onBlePermissionOpenResult(int requestCode, int resultCode,
			Intent data) {
		// TODO Auto-generated method stub
    	
    	BleLog.e("onBlePermissionOpenResult "+requestCode +"result "+resultCode);
		if (REQUEST_CODE_OPEN_GPS == requestCode)
		{
			if (!checkGPSIsOpen())
			{
				 Toast.makeText(mContext, mContext.getString(R.string.gpsNotifyMsg), Toast.LENGTH_LONG).show();		            
			}else
			{
				ObserverManager.getInstance().onPermissioned();
			}
			return true;
		}
		return false;
	}
	
}
