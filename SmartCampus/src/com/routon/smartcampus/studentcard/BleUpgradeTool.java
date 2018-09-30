package com.routon.smartcampus.studentcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

public class BleUpgradeTool {
	private final static String TAG = "BleUpgradeTool";
	private CustomTitleActivity mActivity = null;
	
	//学生卡的升级地址
	private String mDownloadUrl = null;
	private int mPlatformVersion = 0;
	private String mSavePath = null;
	private String mSaveTmpPath = null;
	private String mUpdateUrl = SmartCampusUrlUtils.getStudenCardUpdateUrl();
	
	public static final int STATUS_NONE = 0;
	public static final int STATUS_INIT = 1;
	public static final int STATUS_SCAN = 2;
	public static final int STATUS_STOP = 3;
	public static final int STATUS_CONNECT = 4;
	private int mStatus;
	
	public interface BleUpgradeCallBack{
		void getDownloadUrlComplete();
		void afterInitBlueTooth();
		void findBleMac();
		void updateStep();
		void needToUpgrade();
		void upgradeSuccess();
		void upgradeFailed();
		void connectFailed();
		void upgradeProgress(int progress);
		void bluetoothDisconnect();
	};
	private BleUpgradeCallBack mBleUpgradeCB;
	
	public BleUpgradeTool(CustomTitleActivity activity,BleUpgradeCallBack cb){
		mActivity = activity;
		mBleUpgradeCB = cb;
		
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		mActivity.registerReceiver(mReceiver, filter);
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	public int getPlatformVersion(){
		return mPlatformVersion;
	}
	
	private boolean mBleSupported = true;
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBtAdapter;
	private boolean mBtAdapterEnabled;
	
	private List<BleDeviceInfo> mDeviceInfoList;
	private boolean mScanning = false;
	public void initBlueTooth(){
		if (!mActivity.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(mActivity, "ble not supported", Toast.LENGTH_LONG).show();
			mBleSupported = false;
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to BluetoothAdapter through BluetoothManager.
		mBluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
		mBtAdapter = mBluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBtAdapter == null) {
			Toast.makeText(mActivity, "bt not supported", Toast.LENGTH_LONG).show();
			mBleSupported = false;
		}

		mBtAdapterEnabled = mBtAdapter.isEnabled();
		Log.d(TAG,"initBlueTooth mBtAdapterEnabled:"+mBtAdapterEnabled);
		if (mBtAdapterEnabled) {
			mBleUpgradeCB.afterInitBlueTooth();
		} else {//开启蓝牙
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			if (bluetoothAdapter != null) {		
				bluetoothAdapter.enable();
			}
			// Broadcast receiver
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mBtAdapterEnabled = mBtAdapter.isEnabled();
					if( mBtAdapterEnabled == false ){//延迟后如果蓝牙还未开启，启动定时器
						startBlueToothEnableTask();
					}else{
						mBleUpgradeCB.afterInitBlueTooth();
					}		
				}
			}, 1000);		
		}
		
		if( mDeviceInfoList == null ){
			mDeviceInfoList = new ArrayList<BleDeviceInfo>();
		}else{
			mDeviceInfoList.clear();
		}
	}
	
	public List<BleDeviceInfo> getDevices(){
		return mDeviceInfoList;
	}
	
	
	 private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         public void onReceive (Context context, Intent intent) {
             String action = intent.getAction();
             if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            	 int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                 if( state == BluetoothAdapter.STATE_OFF){
                	 // Bluetooth is disconnected, do handling here              	 
                	 Toast.makeText(mActivity, "蓝牙连接断开", Toast.LENGTH_SHORT).show();
                	 mBtAdapterEnabled = mBtAdapter.isEnabled();
                	 mBleUpgradeCB.bluetoothDisconnect();
                 }else if( state == BluetoothAdapter.STATE_ON){
                	 if( mBlueToothEnableTask != null ){
                		 mBtAdapterEnabled = mBtAdapter.isEnabled();
						 if( mBtAdapterEnabled == true ){
							mBleUpgradeCB.afterInitBlueTooth();
						 }
                		 cancelBlueToothEnableTask();
                	 }         	 
                	 Toast.makeText(mActivity, "蓝牙连接开启", Toast.LENGTH_SHORT).show();
                 }
             }
         }

     };
	
	
	private BleDeviceInfo mSelDevice = null;
	private BluetoothGatt mBluetoothGatt = null;
	private String mBluetoothDeviceAddress;
	private volatile boolean mBusy = false; // Write/read pending response
	
	private void discoverServices() {	
		if( mBluetoothGatt == null ){
			return;
		}
		if (mBluetoothGatt.discoverServices()) {
			Log.d(TAG,"Service discovery started");
		} else {
			upgradeFailed();
			setError("Service discovery start failed");
		}
	}
	
	private void findChangeBToACharacteristic(){
		if (mBluetoothGatt == null) {
			Log.d(TAG,"getSupportedGattServices: mBluetoothGatt == null, return null");
			return;
		}
		//查找oad服务
		List<BluetoothGattService> servicelist = mBluetoothGatt.getServices();
		BluetoothGattService service = null;
		
		for (BluetoothGattService gattService : servicelist) {
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				//找到changeBtoA服务
				if (gattCharacteristic.getUuid().toString().equals(SampleGattAttributes.TST_VCOM)) {
					mCharChangeBtoA = gattCharacteristic;
					return;
				}
			}
		}
	}
	
	private void findOadCharacteristics() {
		try {
			if (mBluetoothGatt == null) {
				Log.d(TAG,"getSupportedGattServices: mBluetoothGatt == null, return null");
				return;
			}
			//查找oad服务
			List<BluetoothGattService> servicelist = mBluetoothGatt.getServices();
			BluetoothGattService service = null;
			if( servicelist != null ){		
				Log.d(TAG,"servicelist.size() = " + servicelist.size());
				for (int i = 0; i < servicelist.size(); i++) {
					BluetoothGattService srv = servicelist.get(i);
					Log.d(TAG,i + ": dd-- BluetoothGattService srv.uuid = " + srv.getUuid());
	
					if (srv.getUuid().equals(GattInfo.OAD_SERVICE_UUID)) {
						Log.d(TAG,"checkOad(): find Oad Service ,srv=" + srv);
						service = srv;
						break;
					}		
				}
			}

			if (service != null) {//找到oad service
				//如果在扫描，停止扫描
				scanLeDevice(false);
				// Characteristics list
				List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
				// mCharListCc = mConnControlService.getCharacteristics();
				Log.d(TAG,"FwUpdateActivity() constructor mCharListOad:"+characteristics.size());

				if (characteristics.size() >= 2) {
					mCharIdentify = characteristics.get(0);
					mCharBlock = characteristics.get(1);
					mCharBlock
							.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
					Log.d(TAG,"checkOad(): find Oad Service ,mCharIdentify=" + mCharIdentify.getUuid().toString()
							+",mCharBlock:"+mCharBlock.getUuid().toString());
					return;
				}
				
			}else{
				setError("Failed to find oad service");
			}
		} catch (Exception e) {
			e.printStackTrace();
			setError("Failed to read services");
		}
	}
	
	public boolean changeBtoA() {
		if (mCharChangeBtoA != null) {
			Log.i("changeBtoA", "changeBtoA");
			byte[] sendmsg = new byte[2];
			sendmsg[0] = 'u';
			sendmsg[1] = 'p';
			mCharChangeBtoA.setValue(sendmsg);
			mBluetoothGatt
					.writeCharacteristic(mCharChangeBtoA);
			return true;
		}
		return false;	
	}
	
	private void resetCharacteristics(){
		mCharChangeBtoA = null;
		mCharIdentify = null;
		mCharBlock = null;
	}
	
	/**
	 * GATT client callbacks
	 */
	private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			Log.d(TAG,"mGattCallbacks: onConnectionStateChange newState:"+newState);
			if (mBluetoothGatt == null) {
				// Log.e(TAG, "mBluetoothGatt not created!");
				return;
			}

			BluetoothDevice device = gatt.getDevice();
			String address = device.getAddress();
			//不是当前选择和连接的device，则断开和连接状态不响应
			if( mSelDevice != null && address.equals(mSelDevice.getBluetoothDevice().getAddress()) == false ){
				return;
			}
			// Log.d(TAG, "onConnectionStateChange (" + address + ") " +
			// newState +
			// " status: " + status);

			try {
				switch (newState) {
				case BluetoothProfile.STATE_CONNECTED:
					mBusy = false;	
					if (status == BluetoothGatt.GATT_SUCCESS) {//设备连接上以后，搜索服务
						resetCharacteristics();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						discoverServices();
						cancelConnectTask();
					}else{
						setError("Connect failed. Status: " + status);
					}
					break;
				case BluetoothProfile.STATE_DISCONNECTED:
					mBusy = false;
					break;
				default:
					// Log.e(TAG, "New state not processed: " + newState);
					break;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.d(TAG,"mGattCallbacks: onServicesDiscovered");
			mBusy = false;
			BluetoothDevice device = gatt.getDevice();
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.d(TAG,"Service discovery complete device:"+device.getName()+",address:"+device.getAddress());
				if( isImageAState(device) == true ){//imageA查找oad服务
					findOadCharacteristics();
					//after find oad Characteristics
					//load oad file
					if( mCharIdentify != null && mCharBlock != null ){
						loadFile(mSavePath);
						//start program
						startProgramming();
					}else{
						upgradeFailed();
					}
				}else{//imageB查找切换服务
					findChangeBToACharacteristic();
					if( mCharChangeBtoA != null ){
						changeBtoA();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//搜索imageA，这里设定一个超时，如果10s后imageA未搜索到，升级失败
						startScanTask();
						scanLeDevice(true);
					}else{
						upgradeFailed();
					}
				}
				// enableDataCollection(true);
				// getFirmwareRevison();
			} else {
				upgradeFailed();
//				Toast.makeText(mActivity,
//						"Service discovery failed", Toast.LENGTH_LONG)
//						.show();
				return;
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			Log.d(TAG,"mGattCallbacks: onCharacteristicChanged");
			mBusy = false;
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
//			Log.d(TAG,"mGattCallbacks: onCharacteristicRead");
			mBusy = false;
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
//			Log.d(TAG,"mGattCallbacks: onCharacteristicWrite"+"mProgInfo.iBlocks = "
//					+ mProgInfo.iBlocks);
			mBusy = false;
//			Log.d(TAG,"mGattCallbacks: onCharacteristicWrite:"+Conversion.BytetohexString(characteristic.getValue(), false));
			if (status != BluetoothGatt.GATT_SUCCESS) {
				Toast.makeText(mActivity, "GATT error: status=" + status,
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
//			Log.d(TAG,"mGattCallbacks: onDescriptorRead");
			mBusy = false;
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
//			Log.d(TAG,"mGattCallbacks: onDescriptorWrite");
			// Log.i(TAG, "onDescriptorWrite: " +
			// descriptor.getUuid().toString());
			mBusy = false;
		}
	};
	
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (!checkGatt())
			return;
		mBusy = true;
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	public boolean writeCharacteristic(
			BluetoothGattCharacteristic characteristic, byte b) {
		if (!checkGatt())
			return false;

		byte[] val = new byte[1];
		val[0] = b;
		characteristic.setValue(val);

		mBusy = true;
    	
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}
	
	private boolean checkGatt() {
		if (mBtAdapter == null) {
			// Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		if (mBluetoothGatt == null) {
			// Log.w(TAG, "BluetoothGatt not initialized");
			return false;
		}

		if (mBusy) {
			// Log.w(TAG, "LeService busy");
			return false;
		}
		return true;

	}


	public boolean writeCharacteristic(
			BluetoothGattCharacteristic characteristic, boolean b) {
		if (!checkGatt())
			return false;

		byte[] val = new byte[1];

		val[0] = (byte) (b ? 1 : 0);
		characteristic.setValue(val);
		
		mBusy = true;
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}

	public boolean writeCharacteristic(
			BluetoothGattCharacteristic characteristic) {
		if (!checkGatt())
			return false;

		mBusy = true;
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}
	
	public void disconnect(String address) {
		Log.d(TAG,"disconnect: address:"+address);
		if (mBtAdapter == null) {
			// Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
			return;
		}
		BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
		int connectionState = mBluetoothManager.getConnectionState(device,
				BluetoothProfile.GATT);

		if (mBluetoothGatt != null) {
			if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
				mBluetoothGatt.disconnect();
			} else {
				 Log.w(TAG, "Attempt to disconnect in state: " +
				 connectionState);
			}
		}
	}
	
	public boolean connect(final String address) {
		Log.d(TAG,"connect address:"+address);
		if (mBtAdapter == null || address == null) {
			// Log.w(TAG,
			// "BluetoothAdapter not initialized or unspecified address.");
			return false;
		}
		final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
		int connectionState = mBluetoothManager.getConnectionState(device,
				BluetoothProfile.GATT);

		if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {

			// Previously connected device. Try to reconnect.
			if (mBluetoothDeviceAddress != null
					&& address.equals(mBluetoothDeviceAddress)
					&& mBluetoothGatt != null) {
				// Log.d(TAG, "Re-use GATT connection");
				if (mBluetoothGatt.connect()) {
					return true;
				} else {
					 Log.w(TAG, "GATT re-connect failed.");
					return false;
				}
			}

			if (device == null) {
				 Log.w(TAG, "Device not found.  Unable to connect.");
				return false;
			}
			// We want to directly connect to the device, so we are setting the
			// autoConnect parameter to false.
			// Log.d(TAG, "Create a new GATT connection.");
			mBluetoothGatt = device.connectGatt(mActivity, false, mGattCallbacks);
			mBluetoothDeviceAddress = address;
		} else {
			 Log.w(TAG, "Attempt to connect in state: " + connectionState);
			return false;
		}
		return true;
	}

	
	void onConnect() {
		Log.d(TAG,"onConnect(): mNumDevs = " + mDeviceInfoList.size()+",mSelDevice:"+mSelDevice);
		if( mSelDevice == null ){
			upgradeFailed();
			return;
		}
		this.scanLeDevice(false);
		mStatus = STATUS_CONNECT;

		int connState = mBluetoothManager.getConnectionState(
				mSelDevice.getBluetoothDevice(), BluetoothGatt.GATT);

		Log.d(TAG,"onConnect(): connState = " + connState);
		boolean ok = false;
		switch (connState) {
			case BluetoothGatt.STATE_CONNECTED:
				Log.d(TAG,"onConnect(): case BluetoothGatt.STATE_CONNECTED:");
				disconnect(mSelDevice.getBluetoothDevice().getAddress());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ok = connect(mSelDevice.getBluetoothDevice().getAddress());
				if (!ok) {
					setError("Connect failed");
					upgradeFailed();
				}else{
					startConnectTask();
				}
				break;
			case BluetoothGatt.STATE_DISCONNECTED:
				Log.d(TAG,"onConnect(): case BluetoothGatt.STATE_DISCONNECTED:");
				ok = connect(mSelDevice.getBluetoothDevice()
						.getAddress());
				if (!ok) {
					setError("Connect failed");
					upgradeFailed();
				}else{
					startConnectTask();
				}
				break;
			default:
				Log.d(TAG,"onConnect(): default:");
				setError("Device busy (connecting/disconnecting)");
				break;
		}

	}
	
	public void startScan() {
		Log.d(TAG,"startScan: ");
		// Start device discovery
		if (mBleSupported) {
			mDeviceInfoList.clear();
			mStatus = STATUS_SCAN;
			if( mScanning == false ){
				scanLeDevice(true);
	
				if (!mScanning) {
					setError("Device discovery start failed");
				}
			}
		} else {
			setError("BLE not supported on this device");
		}
	}
	
	public void exit(){
		Log.d(TAG,"exit");
		quit();
		mActivity.unregisterReceiver(mReceiver);
		mActivity = null;
	}
	
	public void quit(){
		scanLeDevice(false);		
		if (mBluetoothGatt != null && mBtAdapterEnabled == true ) {
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		cancelScanTask();
		cancelConnectTask();
	}
	
	public void stopScan() {
		mStatus = STATUS_STOP;
		scanLeDevice(false);
	}
	
	private final static String ImageAStr = "ImgA";
	private String mDeviceMarkName = null;
//	private String mDeviceMarkNeedRepairName = "Routon-S1705";
//	private String mDeviceMarkImageAName = "Routon-S1705-ImgA";
	
	public void setDeviceMarkName(String markName){
		mDeviceMarkName = markName;
	}
	
	boolean checkDeviceFilter(String deviceName,boolean isImageA) {
		// pr("checkDeviceFilter: ");
		if (deviceName == null)
			return false;
		//imageB可升级
		//S1705-22D6-F73A
		if( (mDeviceMarkName != null && deviceName.contains(mDeviceMarkName) && deviceName.contains(ImageAStr) == isImageA)
				|| (mDeviceMarkName == null && deviceName.contains(ImageAStr) == isImageA) ){
			String[] arrays = deviceName.split("-");
			if( arrays.length >= 3 ){
				//过滤mac
				if( mMacFilter != null ){
					String[] arrays1 = mMacFilter.split(":");
					if( arrays1.length >= 4 ){
						if( (arrays[arrays.length-2]+arrays[arrays.length-1]).equals(arrays1[arrays1.length-4]+arrays1[arrays1.length-3]+arrays1[arrays1.length-2]+arrays1[arrays1.length-1])){
							return true;
						}else{
							return false;
						}
					}
				}else{
					return true;
				}
			}
			
			return false;
		}	
		return false;
	}
	
	private BleDeviceInfo deviceInfoExists(String address) {
		// pr("deviceInfoExists: ");
		if( mDeviceInfoList == null ) return null;
		for (int i = 0; i < mDeviceInfoList.size(); i++) {
			if (mDeviceInfoList.get(i).getBluetoothDevice().getAddress()
					.equals(address)) {
				return mDeviceInfoList.get(i);
			}
		}
		return null;
	}
	
	
	private String mMacFilter = null;
	public void setMacFilter(String mac){
		mMacFilter = mac;
	}
	
//	private boolean isNeedToUpdateDevice(String deviceName) {
//		if (deviceName == null)
//			return false;
//		if( deviceName.contains(mDeviceMarkNeedRepairName)){
//			return true;
//		}
//		return false;
//	}
	private static final int RASSI_MIN = -80;
	private byte mDeviceIden = 0x70;//s1705
	
	public void setDeviceIden(byte iden){
		mDeviceIden = iden;
	}
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {
			if( scanRecord.length <= 3 ){
				return;
			}
			if( scanRecord[3] != mDeviceIden ){
				return;
			}
			if( mStatus == STATUS_SCAN ){//扫描状态
				 
				if (checkDeviceFilter(device.getName(),false)) {//检测imageB
//					 Log.d(TAG, "onLeScan STATUS_SCAN imageB device.getName: " + device.getName()+",rssi:"+rssi
//							 +",scanRecord:"+Conversion.BytetohexString(scanRecord, false)
//							 +",scanRecord[4]:"+scanRecord[4]);
					 if( scanRecord[4] != 0x10 ){
						 return;
					 }
					 BleDeviceInfo deviceinfo = deviceInfoExists(device.getAddress());
//					 if( deviceinfo != null ){
//						 if( rssi < -100 || rssi == 127 ){//设备超出范围，删除设备
//							 mDeviceInfoList.remove(deviceinfo);
//							 return;
//						 }
//					 }else{
//						 if( rssi < -60 || rssi == 127 ){//设备超出范围，删除设备
//							 return;
//						 }
//					 }
					 //绑定了mac地址，信号值范围可以加大
					 if( rssi < RASSI_MIN || rssi == 127 ){//设备超出范围
						 return;
					 }
					 int version = 0;
					 int step = 0;
					 if( scanRecord.length >= 12 ){
						 version = Conversion.buildUint32( scanRecord[10],scanRecord[11]);
					 	 step = Conversion.buildUint32( scanRecord[8],scanRecord[9])+((scanRecord[7] << 16) & 0xff0000);
					 }
					 Log.d(TAG, "onLeScan version:"+version);
					 if( deviceinfo == null && mDeviceInfoList.size() >= 1){
							return;
						}
					 if( deviceinfo == null ){
						 deviceinfo = new BleDeviceInfo(device, rssi);
						 mDeviceInfoList.add(deviceinfo);
						 deviceinfo.updateVersion(version);
						 if( mActivity != null ){
							 mActivity.runOnUiThread(new Runnable(){
		
								@Override
								public void run() {
									// TODO Auto-generated method stub
									 mBleUpgradeCB.findBleMac();
								}				
							 });
						 }
					 }else{
						 deviceinfo.updateVersion(version);
						 deviceinfo.updateRssi(rssi);
					 }
					 deviceinfo.updateStep(step);
					 
					 if( mActivity != null ){
						 mActivity.runOnUiThread(new Runnable(){
	
							@Override
							public void run() {
								// TODO Auto-generated method stub
								 mBleUpgradeCB.updateStep();
							}				
						 });
					 }			 
					 
				}else if( checkDeviceFilter(device.getName(),true) ){//检测到imageA,不再兼容老版本
					if( rssi == 127 || rssi < RASSI_MIN ){//设备超出范围，删除设备
						return;
					}
					Log.d(TAG, "onLeScan STATUS_SCAN device.getName: " + device.getName()+",rssi:"+rssi);
					BleDeviceInfo deviceinfo = deviceInfoExists(device.getAddress());
					if( deviceinfo == null && mDeviceInfoList != null && mDeviceInfoList.size() >= 1){
//						Log.d(TAG, "onLeScan STATUS_SCAN return");
						return;
					}
					if( deviceinfo == null ){
						deviceinfo = new BleDeviceInfo(device, rssi);
						mDeviceInfoList.add(deviceinfo);
						if( mActivity != null ){
							 mActivity.runOnUiThread(new Runnable(){
		
								@Override
								public void run() {
									// TODO Auto-generated method stub
									mBleUpgradeCB.needToUpgrade();
								}				
							 });
						 }	
					}else{
						deviceinfo.updateRssi(rssi);
					}				 				
				}
			}else if( mStatus == STATUS_CONNECT ){//连接状态下，一般搜索imageA		
				Log.d(TAG, "onLeScan device.getName: " + device.getName()+",rssi:"+rssi);
				 if( checkDeviceFilter(device.getName(),true)){//查找到imageA
					 Log.d(TAG, "onLeScan STATUS_CONNECT name:"+device.getName()+",rssi:"+rssi);
					 //android手机近距离imageA信号一般稍大，如果范围值定义太小，不容易发布
					 if( rssi == 127 || rssi < RASSI_MIN ){//设备超出范围，删除设备
						return;
					 }	
					 
					 cancelScanTask();
					 mSelDevice = new BleDeviceInfo(device, rssi);
					 //连接imageA
					 onConnect();
				 }
				 
			 }
		}
	};
	
	private final int SCAN_TIMEOUT = 20000; // Seconds
	private final int CONNECT_TIMEOUT = 30000;
	TimerTask mScanTask = null;
	TimerTask mBlueToothEnableTask = null;
	
	void startBlueToothEnableTask(){
		 if( mBlueToothEnableTask == null ){
			 mBlueToothEnableTask = new TimerTask() { 
				    @Override 
				    public void run() { 
				        // TODO Auto-generated method stub 
			    	   Log.d(TAG,"mScanImageATask timeout");
			    	   if( mActivity != null ){
				    	   mActivity.runOnUiThread(new Runnable(){
	
							@Override
							public void run() {
								// TODO Auto-generated method stub
								cancelBlueToothEnableTask();
								mBtAdapterEnabled = mBtAdapter.isEnabled();
								if( mBtAdapterEnabled == true ){
									mBleUpgradeCB.afterInitBlueTooth();
								}
							}
				    		   
				    	   });
			    	   }
				    } 
				}; 
				mTimer.schedule(mBlueToothEnableTask, 100000);
		 	}	
	 }
	
	 void cancelBlueToothEnableTask(){
		 if( mBlueToothEnableTask != null ){
			 mBlueToothEnableTask.cancel();
			 mBlueToothEnableTask = null;
		 }
	 }
	
	 void startScanTask(){
		 Log.d(TAG,"startScanTask");
		 if( mScanTask == null ){
			 mScanTask = new TimerTask() { 
				    @Override 
				    public void run() { 
				        // TODO Auto-generated method stub 				    	
			    	   Log.d(TAG,"mScanImageATask timeout");
			    	   cancelScanTask();
			    	   scanLeDevice(false);
			    	   if( mActivity != null ){
				    	   mActivity.runOnUiThread(new Runnable(){
	
							@Override
							public void run() {
								// TODO Auto-generated method stub
								 upgradeFailed();
							}
				    		   
				    	   });
			    	   }
				    } 
				}; 
				mTimer.schedule(mScanTask, SCAN_TIMEOUT);
		 	}
	 }
	
	 void cancelScanTask(){
		 Log.d(TAG,"cancelScanTask");
		 if( mScanTask != null ){
			 mScanTask.cancel();
			 mScanTask = null;
		 }
	 }
	
	
	//when connect,start,connected cancel
	TimerTask mConnectTask = null;
	
	 void startConnectTask(){
		 if( mConnectTask == null ){
			 mConnectTask = new TimerTask() { 
				    @Override 
				    public void run() { 
				        // TODO Auto-generated method stub 
			    	   Log.d(TAG,"mConnectTask timeout");
			    	   if( mActivity == null ){
			    		   return;
			    	   }
			    	   mActivity.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							cancelConnectTask();
							// TODO Auto-generated method stub
							if(mSelDevice != null && mSelDevice.getBluetoothDevice() != null ){
								disconnect(mSelDevice.getBluetoothDevice().getAddress());
							}
							mActivity.hideProgressDialog();
							mBleUpgradeCB.connectFailed();
						}
			    		   
			    	   });
				    } 
				}; 
				 mTimer.schedule(mConnectTask, CONNECT_TIMEOUT);
		 	}
		
		
	 }
	
	 void cancelConnectTask(){
		 if( mConnectTask != null ){
			 mConnectTask.cancel();
			 mConnectTask = null;
		 }
	 }
	
	private final Timer mTimer = new Timer(); 
	
	private boolean scanLeDevice(boolean enable) {
		Log.d(TAG,"scanLeDevice: "+enable+",mScanning:"+mScanning);
		if (enable) {
			mScanning = mBtAdapter.startLeScan(mLeScanCallback);
		} else {
			if (mScanning) {
				mScanning = false;
			}
			mBtAdapter.stopLeScan(mLeScanCallback);
		}
		return mScanning;
	}
	
	void setError(String txt) {
		Log.e(TAG,txt);
		Toast.makeText(mActivity, "点击刷新按钮后重试", Toast.LENGTH_SHORT).show();
//		Toast.makeText(mActivity, "Turning BT adapter off and on again may fix Android BLE stack problems", Toast.LENGTH_SHORT).show();
	}
	
	private void hideProgressInMainUI(){
		if( mActivity == null ){
			return;
		}
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {	
				if ( mActivity == null)
					return;
				mActivity.hideProgressDialog();	
			}
		});
	}
	
	public void setUpdateUrl(String url){
		if( url == null ){
			return;
		}
		mUpdateUrl = url;
	}
	
	public boolean getVersionTxtUrl() {
		if (mActivity == null)
			return false;
		if (InfoReleaseApplication.showNetWorkFailed(mActivity) == false) {// 网络连接失败
			return false;
		}
		Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				if ( mActivity == null)
					return;
				int code = response.optInt("code");
				if( code == 0 ){//成功
					JSONObject obj = response.optJSONObject("obj");
					if( obj != null ){
						String txtUrl = obj.optString("url");
						if( txtUrl.isEmpty() == false ){
							getCardVersionText(txtUrl);
						}else{
							Toast.makeText(mActivity, "校园卡升级下载地址获取失败", Toast.LENGTH_SHORT).show();
							hideProgressInMainUI();
						}
					}
				}else{
					Toast.makeText(mActivity, "校园卡升级下载地址获取失败", Toast.LENGTH_SHORT).show();
					hideProgressInMainUI();
				}
			}
		};
		Log.d(TAG,"update url:"+mUpdateUrl);
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(mUpdateUrl, null,
				listener, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						hideProgressInMainUI();
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

		return true;
	}
	
	public boolean getCardVersionText(String url){
		Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d(TAG,"response:"+response);
				if ( mActivity == null)
					return;
				JSONObject obj;
				try {		
					obj = new JSONObject(response);
					mDownloadUrl = obj.optString("url");
					//取到下载地址为空，直接返回
					if( mDownloadUrl == null ){
						Toast.makeText(mActivity, "校园卡升级下载地址为空", Toast.LENGTH_SHORT).show();
						hideProgressInMainUI();
						return;
					}
					String version = obj.optString("version");
					if( mDownloadUrl == null ){
						Toast.makeText(mActivity, "版本号获取失败", Toast.LENGTH_SHORT).show();
						hideProgressInMainUI();
						return;
					}
					mPlatformVersion = Integer.parseInt(version);
					String[] arrays = mDownloadUrl.split("/");
					mSavePath = mActivity.getExternalCacheDir()+"/"+arrays[arrays.length-1];
					mSaveTmpPath = mActivity.getExternalCacheDir()+"/"+"tmp.dd";
					
					hideProgressInMainUI();
					Log.d(TAG,"mDownloadUrl:"+mDownloadUrl+",mSavePath:"+mSavePath);
					if( mBleUpgradeCB != null ){
						mStatus = STATUS_INIT;
						mBleUpgradeCB.getDownloadUrlComplete();
					}				
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		StringRequest request = new StringRequest(url,
				listener, new Response.ErrorListener() {
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(mActivity, "校园卡升级下载地址获取失败", Toast.LENGTH_SHORT).show();
						hideProgressInMainUI();
					}
				});
		InfoReleaseApplication.requestQueue.add(request);
		return true;
	}
	
	
	private void getStudentCardupgradeFile(){
		if( mActivity == null ){
			return;
		}
		mActivity.showProgressDialog(false);
		new DownloadAsyncTask().execute(mDownloadUrl,mSavePath,mSaveTmpPath);
	}
	
	private void hideProgressDialog(){
		if( mActivity != null ){
			mActivity.hideProgressDialog();
		}
	}
	
	
	boolean isImageAState(BluetoothDevice device){
		if( device == null || device.getName() == null ){
			return false;
		}
		if(device.getName().contains(ImageAStr)){
			return true;
		}
		return false;
	}
	
	//开始升级
	private void startUpgrade(){
		if( mActivity == null ){
			return;
		}
		mActivity.showProgressDialog(false);
		onConnect();
//		boolean isImageA = isImageAState(mSelDevice);
//		if(  isImageA == true ){//imageA 连接
//			
//		}else{//imageB 连接
//			
//		}
	}
	
	public void showNeedRepairUpgradeDialog(){
		final File file = new File(mSavePath);
		if( file.exists() == true ){
			showDownloadUpgradeDialog(0,"升级","搜索到需要修复的校园卡固件，升级需要几分钟，升级过程中最好不要中断，是否升级？");
		}else{
			showDownloadUpgradeDialog(0,"下载升级","搜索到需要修复的校园卡固件，升级需要几分钟，升级过程中最好不要中断，是否下载并升级？");
		}
	}
	
	private AlertDialog mDownloadUpgradeDialog = null;
	
	public boolean isDownloadUpgradeDialogShow(){
		if( mDownloadUpgradeDialog == null ){
			return false;
		}
		if( mDownloadUpgradeDialog.isShowing() == true ){
			return true;
		}
		return false;
	}
	
	private void showDownloadUpgradeDialog(final int deviceIndex,String title,String msg){
		if( mDownloadUpgradeDialog != null ){
			return;
		}
		if( mActivity == null ){
			return;
		}
		// 构造对话框
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(title);
		builder.setMessage(msg);
		final File file = new File(mSavePath);
		Log.d(TAG,"showDownloadUpgradeDialog mSavePath:"+mSavePath+" exist:"+file.exists());
		
		Dialog.OnClickListener positiveListener = new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {//确认
					dialog.dismiss();
					if( mDeviceInfoList.size() <= deviceIndex ){
						mBleUpgradeCB.upgradeFailed();
						return;
					}
					mSelDevice = mDeviceInfoList.get(deviceIndex);
					if( file.exists() == false ){//文件不存在，下载
						getStudentCardupgradeFile();
					}else{//文件存在，直接升级
						startUpgrade();
					}
				}
		};
			Dialog.OnClickListener negativeListener = new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//稍后下载
					dialog.dismiss();
					
				}
			};
	
			builder.setPositiveButton("确定", positiveListener);
			builder.setNegativeButton("取消", negativeListener);
		
			builder.setCancelable(false);
			mDownloadUpgradeDialog = builder.create();
			mDownloadUpgradeDialog.setOnDismissListener(new OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					mDownloadUpgradeDialog = null;
				}
				
			});
			mDownloadUpgradeDialog.show();
	}
	
	public void showDownloadUpgradeDialog(final int deviceIndex){
		final File file = new File(mSavePath);
		if( file.exists() == true ){
			showDownloadUpgradeDialog(deviceIndex,"升级","校园卡固件版本有更新，升级需要几分钟，升级过程中最好不要中断，是否升级？");
		}else{
			showDownloadUpgradeDialog(deviceIndex,"下载升级","校园卡固件版本有更新，升级需要几分钟，升级过程中最好不要中断，是否下载并升级？");
		}
	}
	
	 private class DownloadAsyncTask extends AsyncTask<String, Integer, Integer>{
	        public DownloadAsyncTask() {
	        }
	        
	        @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	        }

	        @Override
	        protected Integer doInBackground(String... params) {

	            //完成下载任务
	            String s = params[0];//这是从execute方法中传过来的参数, 即下载的地址
	            try {
	                URL url = new URL(s);
	                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	                //开始下载
	                byte[] bytes = new byte[1024];
	                int len = -1;
	                InputStream in = conn.getInputStream();
	                FileOutputStream out = new FileOutputStream(params[2]);
	                while( (len = in.read(bytes)) != -1 ){
	                    out.write(bytes, 0, len);
	                    out.flush();
	                }
	                out.close();
	                in.close();     
	                File oldFile = new File(params[2]);
	                File newFile = new File(params[1]);
	                
	                //执行重命名
	                boolean ret = oldFile.renameTo(newFile);
	                Log.d(TAG,"rename params[2]:"+params[2]+" to params[1]:"+params[1]+" result:"+ret);

	                startUpgrade();
	                
	            } catch (MalformedURLException e) {
	                e.printStackTrace();
	                hideProgressDialog();
	            } catch (IOException e) {
	                e.printStackTrace();
	                hideProgressDialog();
	            }
	            return 0;
	        }

	        @Override
	        protected void onProgressUpdate(Integer... values) {
	            super.onProgressUpdate(values);

	        }

	        @Override
	        protected void onPostExecute(Integer integer) {
	            super.onPostExecute(integer);
	        }
	 }
	 
	 private BluetoothGattCharacteristic mCharChangeBtoA = null;
	 private BluetoothGattCharacteristic mCharBlock = null;
	 private BluetoothGattCharacteristic mCharIdentify = null;
	 private ProgInfo mProgInfo = new ProgInfo();
	 private final byte[] mOadBuffer = new byte[OAD_BUFFER_SIZE];
	 private final byte[] mFileBuffer = new byte[FILE_BUFFER_SIZE];
	 private boolean mProgramming = false;
	 // Programming parameters
	 private static final short OAD_CONN_INTERVAL = 12; // 15 milliseconds
	 private static final short OAD_SUPERVISION_TIMEOUT = 50; // 500 milliseconds
	 private static final int GATT_WRITE_TIMEOUT = 12000; // 300; // Milliseconds
	 private static final int FILE_BUFFER_SIZE = 0x50000;
	 private static final int OAD_BLOCK_SIZE = 16;
	 private static final int HAL_FLASH_WORD_SIZE = 4;
	 private static final int OAD_BUFFER_SIZE = 2 + OAD_BLOCK_SIZE;
	 private static final int OAD_IMG_HDR_SIZE = 8;
	 private static final long TIMER_INTERVAL = 1000;
	 private static final int SEND_INTERVAL = 20; // Milliseconds (make sure this
													// is longer than the
													// connection interval)
	 private static final int BLOCKS_PER_CONNECTION = 1; // May sent up to four
														// blocks per connection
	 
	 private int mFileValidLength = 0;
	 private int mOadFileVersion = 0;
	
	 private boolean loadFile(String filepath) {
		boolean fSuccess = false;

		// Load binary file
		try {	
			File f = new File(filepath);
			Log.d(TAG,"filepath:"+filepath);
			InputStream stream = new FileInputStream(f);
			stream.read(mFileBuffer, 0, mFileBuffer.length);
			stream.close();
			mOadFileVersion = Conversion
					.buildUint16(mFileBuffer[5], mFileBuffer[4]);
			mFileValidLength = Conversion
						.buildUint32(mFileBuffer[7], mFileBuffer[6]);
		} catch (IOException e) {
			// Handle exceptions here
			Log.d(TAG,"File open failed: " + filepath + "\n");
			return false;
		}
		return fSuccess;
	}
	 
	 private void startProgramming() {
		
		mProgramming = true;

		// Prepare image notification
		byte[] buf = new byte[OAD_IMG_HDR_SIZE + 2 + 2];
		buf[0] = Conversion.loUint16((short) mOadFileVersion);
		buf[1] = Conversion.hiUint16((short) mOadFileVersion);
		
		buf[2] = Conversion.loUint16((short) mFileValidLength);
		buf[3] = Conversion.hiUint16((short) mFileValidLength);
		System.arraycopy(mFileBuffer, 8, buf, 4, 4);

		// Send image notification
		mCharIdentify.setValue(buf);
		writeCharacteristic(mCharIdentify);
		Log.d(TAG,"Programming started buf:"+Conversion.BytetohexString(buf, false));

		// Initialize stats
		mProgInfo.reset();

			// Start the programming thread
		new Thread(new OadTask()).start();
	}
	 
	 private void stopProgramming() {
		Log.d(TAG,"stopProgramming() start");
		
		if ( mProgramming == true ) {
			mProgramming = false;
			Log.d(TAG,"stopProgramming: mProgInfo.iBlocks = " + mProgInfo.iBlocks
						+ ", nBlocks = " + mProgInfo.nBlocks);
		}
		
		if (mProgInfo.iBlocks == mProgInfo.nBlocks) {
			upgradeSuccess();
		} else {
			upgradeFailed();
		}
	}
	 
	 private void upgradeFailed(){
		 Log.d(TAG,"upgradeFailed");
		 if( mActivity == null ){
			 return;
		 }
		 mActivity.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				 quit();
				 mActivity.hideProgressDialog();
				 Toast.makeText(mActivity, "升级失败", Toast.LENGTH_SHORT).show();
				 mBleUpgradeCB.upgradeFailed();
			}
			 
		 });
		 
	 }
	 
	 private void upgradeSuccess(){
		 Log.d(TAG,"upgradeSuccess");
		 if( mActivity == null ){
			 return;
		 }
		 mActivity.runOnUiThread(new Runnable(){

				@Override
				public void run() {
						quit();
						Toast.makeText(mActivity, "升级成功", Toast.LENGTH_SHORT).show();
						mActivity.hideProgressDialog();
						mBleUpgradeCB.upgradeSuccess();
				}
		 });
	 }
	 
	 public boolean waitIdle(int timeout) {
			timeout /= 10;
			while (--timeout > 0) {
				if (mBusy)
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				else{
					try {//调整写数据成功后的间隔时间，加上10ms的延迟，否则写卡很有可能失败
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}

			return timeout > 0;
		}
	 
	 private void programBlock() {
		 if( mActivity == null ){
			 return;
		 }
		if (!mProgramming)
			return;

		if (mProgInfo.iBlocks < mProgInfo.nBlocks) {
			mProgramming = true;
			String msg = new String();

			// Prepare block
			mOadBuffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
			mOadBuffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
			System.arraycopy(mFileBuffer, mProgInfo.iBytes, mOadBuffer, 2,
					OAD_BLOCK_SIZE);
			if( mCharBlock == null ){
				return;
			}
			// Send block
			mCharBlock.setValue(mOadBuffer);
//			Log.i(TAG, "programBlock(): mProgInfo.iBlocks = "
//					+ mProgInfo.iBlocks + ", mProgInfo.nBlocks="
//					+ mProgInfo.nBlocks+",mOadBuffer:"+Conversion.BytetohexString(mOadBuffer, false));
			if ((mProgInfo.iBlocks % 100) == 0) {	
				mActivity.runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mBleUpgradeCB.upgradeProgress((mProgInfo.iBlocks * 100)
								/ mProgInfo.nBlocks);
					}
					
				});
				
			}
			
			boolean success = writeCharacteristic(mCharBlock);
//			Log.i(TAG, "programBlock(): success = "+success+",mProgInfo.iBlocks:"+mProgInfo.iBlocks);
			if (success) {
				// Update stats
				mProgInfo.iBlocks++;
				mProgInfo.iBytes += OAD_BLOCK_SIZE;
				if (!waitIdle(GATT_WRITE_TIMEOUT)) {
					mProgramming = false;
					success = false;
					msg = "GATT write timeout\n";
					Log.d(TAG,msg+",mProgInfo.iBlocks:"+mProgInfo.iBlocks);
				}
			} else {
				mProgramming = false;
				msg = "GATT writeCharacteristic failed\n";
			}
			if (!success) {
				final String log = msg;
				Log.d(TAG,msg);
//				mActivity.runOnUiThread(new Runnable() {
//					public void run() {
//							mLog.append(log);
//					}
//				});
			}
		} else {
			mProgramming = false;
		}

		if (!mProgramming) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
//						displayStats();
					stopProgramming();
				}
			});
		}
	}
	 
	 private class OadTask implements Runnable {
			@Override
			public void run() {
				while (mProgramming) {
					try {
						Thread.sleep(SEND_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (int i = 0; i < BLOCKS_PER_CONNECTION & mProgramming; i++) {
						programBlock();
					}
					if ((mProgInfo.iBlocks % 100) == 0) {
						// Display statistics each 100th block
						mActivity.runOnUiThread(new Runnable() {
							public void run() {
								
							}
						});
					}
				}
			}
		}

		private class ProgInfo {
			int iBytes = 0; // Number of bytes programmed
			short iBlocks = 0; // Number of blocks programmed
			short nBlocks = 0; // Total number of blocks

			void reset() {
				iBytes = 0;
				iBlocks = 0;
				nBlocks = (short) (mFileValidLength / (OAD_BLOCK_SIZE / HAL_FLASH_WORD_SIZE));
				Log.i(TAG, "nBlocks = "
						+ nBlocks+",iBytes:"+iBytes);
			}
		}

}
