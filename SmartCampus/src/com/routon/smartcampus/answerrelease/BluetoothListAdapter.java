package com.routon.smartcampus.answerrelease;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.answerrelease.service.BtDevice;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BluetoothListAdapter extends BaseAdapter{
	
	private Context context;
	private List<BtDevice> btDevices;
	private boolean connected;
	private int connectedDeviceIndex;
	
	public BluetoothListAdapter(Context context, List<BtDevice> btDevices, boolean connected, int connectedDeviceIndex)
	{
		this.context = context;
		this.btDevices = btDevices;
		this.connected = connected;
		this.connectedDeviceIndex = connectedDeviceIndex;
	}
	

	@Override
	public int getCount() {
		return btDevices.size();
	}

	@Override
	public Object getItem(int position) {
		return btDevices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		BluetoothHolder holder = null;
		if(convertView == null)
		{
			view = LayoutInflater.from(context).inflate(R.layout.item_answer_bluetooth, parent, false);
			convertView = view;
			holder = new BluetoothHolder(view);
			convertView.setTag(holder);
		}
		else {
			view = convertView;
			holder = (BluetoothHolder) view.getTag();
		}
		BtDevice btDevice = btDevices.get(position);
		if(btDevice == null)
		{
			return null;
		}
		holder.txtName.setText(btDevice.getName());
		holder.txtStatus.setText(btDevice.getStatus());
		holder.imgBtSignal.setImageResource(getSignalImageRes(btDevice.getRssi()));
		if(connected && position == connectedDeviceIndex)
		{
			holder.txtName.setTextColor(Color.parseColor("#3CB371"));
			holder.txtStatus.setTextColor(Color.parseColor("#3CB371"));
		}
		else {
			holder.txtName.setTextColor(Color.BLACK);
			holder.txtStatus.setTextColor(Color.BLACK);
		}
		return view;
	}
	
	class BluetoothHolder
	{
		TextView txtName;
		TextView txtStatus;
		ImageView imgBtIco;
		ImageView imgBtSignal;
		
		public BluetoothHolder(View view)
		{
			txtName = (TextView)view.findViewById(R.id.txt_answer_bluetooth_name);
			txtStatus = (TextView)view.findViewById(R.id.txt_answer_bluetooth_status);
			imgBtIco = (ImageView)view.findViewById(R.id.img_answer_bluetooth_ico);
			imgBtSignal = (ImageView)view.findViewById(R.id.img_answer_bluetooth_signal);
		}
	}
	
	private int getSignalImageRes(short rssid) {
		int resId = R.drawable.ic_wifi_signal_1;
		if (rssid > -70) {
			resId = R.drawable.ic_wifi_signal_4;
		} else if (rssid > -80) {
			resId = R.drawable.ic_wifi_signal_3;
		} else if (rssid > -90) {
			resId = R.drawable.ic_wifi_signal_2;
		} else {
			resId = R.drawable.ic_wifi_signal_1;
		}
		
		return resId;
	}

}
