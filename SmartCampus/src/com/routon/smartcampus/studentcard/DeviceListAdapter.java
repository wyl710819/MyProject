package com.routon.smartcampus.studentcard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

import com.routon.edurelease.R;


public class DeviceListAdapter extends BaseAdapter {
	

	private List<BleDeviceInfo> mDeviceList;
	private LayoutInflater mLayoutInflater;
	private int mPlatformVerison;
	private Context mContext;

//	private TouchObject mTmpobj;
	public DeviceListAdapter(Context context, List<BleDeviceInfo> deviceList){
		this.mDeviceList = deviceList;
		this.mLayoutInflater = LayoutInflater.from(context);
		this.mContext = context; 
	}
	
	public void setPlatformVersion(int version){
		mPlatformVerison = version;
	}

	@Override
	public int getCount() {
		return mDeviceList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDeviceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mDeviceList.get(position).hashCode();
	}
	
	public void updateData(List<BleDeviceInfo> deviceList){
		mDeviceList = deviceList;
	}

	private int  getBlueTScaleImageSrc(int rrsi)
	{
		 int [] imgId = {   //ImageView显示的图片数组
				   R.drawable.bluet_scale0, 
				   R.drawable.bluet_scale1,
				   R.drawable.bluet_scale2,
				   R.drawable.bluet_scale3,
				  };
		 int index = 3;
		 if (rrsi < 0 && rrsi > -100)
		 {
			 if (rrsi > -40)
			 {
				 index = 0;
			 }else if (rrsi > -50)
			 {
				 index = 1;
			 }else if (rrsi > -55)
			 {
				 index = 2;
			 }else if (rrsi > -60)
			 {
				 index = 3;
			 }
		 }
		 return imgId[index];
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		

		BleDeviceInfo bluetoothDevice = mDeviceList.get(position);
		Device device = null;
		Log.d("DeviceListAdapter","getView position:"+position);
		if(null == convertView){
			device = new Device();
			convertView = mLayoutInflater.inflate(R.layout.card_device_list_item, null);
			device.mDeviceNameTV = (TextView) convertView.findViewById(R.id.deviceName);
			device.mImageBlutScale = (ImageView)convertView.findViewById(R.id.image_blt_scale);
			device.mUpgradeIv = (ImageView)convertView.findViewById(R.id.image_upgrade);
			device.mUpgradeProgressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
			convertView.setTag(device);
		}else {
			device = (Device) convertView.getTag();
		}
		try {
			device.mDeviceNameTV.setText(bluetoothDevice.getBluetoothDevice().getName());
		} catch (Exception e) {
			device.mDeviceNameTV.setText("null");
		}
		
		if( mPlatformVerison > bluetoothDevice.getVersion() ){
			device.mUpgradeIv.setVisibility(View.VISIBLE);
		}else{
			device.mUpgradeIv.setVisibility(View.GONE);
		}
			
		int src = getBlueTScaleImageSrc(bluetoothDevice.getRssi());
		device.mImageBlutScale.setImageResource(src);

		int progress = bluetoothDevice.getProgress();
		if( progress < 0 ){
			device.mUpgradeProgressBar.setVisibility(View.GONE);
		}else{
			device.mUpgradeProgressBar.setVisibility(View.VISIBLE);
			device.mUpgradeProgressBar.setProgress(progress);
		}
		return convertView;
	}


	public class Device {
		
		public TextView mDeviceNameTV;  //设备名
		public ImageView mImageBlutScale;//蓝牙信号强度
		public ImageView mUpgradeIv;
		public ProgressBar mUpgradeProgressBar;
	}
}
