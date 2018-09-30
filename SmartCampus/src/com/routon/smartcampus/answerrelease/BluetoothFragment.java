package com.routon.smartcampus.answerrelease;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.routon.edurelease.R;
import com.routon.smartcampus.answerrelease.service.BluetoothService;
import com.routon.smartcampus.answerrelease.service.Broadcast;
import com.routon.smartcampus.answerrelease.service.BtDevice;
import com.routon.smartcampus.answerrelease.service.ClsUtils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BluetoothFragment extends Fragment {
	private List<BtDevice> btDevices;
	private BluetoothListAdapter adapter;
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothReceiver receiver;
	private IntentFilter filter;
	private ListView mListBluetooth;
	private String savedS1701Mac;
	private String savedS1701Name;
	private String autoConnectS1701Name;

	private final String TAG = "BluetoothFragment";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiver = new BluetoothReceiver();
		filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(Broadcast.BT_START_DISCOVERY);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(Broadcast.ACTION_BT_CONNECT_STATE_CHANGED);
		filter.addAction(Broadcast.ACTION_AUTO_CONNECT);
		getActivity().registerReceiver(receiver, filter);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (receiver != null) {
			getActivity().unregisterReceiver(receiver);
		}
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_answer_bluetooth, container, false);
		init(view);
		return view;
	}

	public void init(View view) {
		savedS1701Mac = getActivity().getSharedPreferences("save_mac", Context.MODE_PRIVATE).getString("S1701_mac", "");
		savedS1701Name = getActivity().getSharedPreferences("save_mac", Context.MODE_PRIVATE).getString("S1701_name",
				"");
		mListBluetooth = (ListView) view.findViewById(R.id.list_answer_bluetooth);
		btDevices = new ArrayList<BtDevice>();
		adapter = new BluetoothListAdapter(getActivity(), btDevices, false, 0);
		mListBluetooth.setAdapter(adapter);
		mListBluetooth.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (bluetoothAdapter.isDiscovering()) {
					bluetoothAdapter.cancelDiscovery();
				}
				BluetoothDevice device = btDevices.get(position).getDevice();
				int bondstate = device.getBondState();
				String mac = device.getAddress();
				String status = btDevices.get(position).getStatus();
				String name = btDevices.get(position).getName();
				Log.d(TAG, "the select name:" + name + " mac:" + mac + " status:" + status + " bondstate:" + bondstate);
				Intent intent = new Intent(Broadcast.ACTION_NOTIFY_SERVICE_CONNNECT);
				intent.putExtra(Broadcast.EXTRA_S1701_MAC, mac);
				intent.putExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS, status);
				intent.putExtra(Broadcast.EXTRA_S1701_BONDSTATE, bondstate);
				getActivity().sendBroadcast(intent);
			}
		});
	}

	private class BluetoothReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, action);
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				int bondState = device.getBondState();
				short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
				if (device.getName() == null
						|| (!device.getName().contains("S1701") && !device.getName().contains("s1701"))) {
					return;
				}
				BtDevice btDevice = new BtDevice(device, rssi);
				for (int i = 0; i < btDevices.size(); i++) {
					BtDevice hasDevice = btDevices.get(i);
					if (hasDevice.getDevice().getAddress().equals(device.getAddress())) {
						Log.d(TAG, "already add");
						return;
					}
				}
				Log.d(TAG, "savedS1701Mac:" + savedS1701Mac + " savedS1701Name:" + savedS1701Name);
				Log.d(TAG, "device name:" + device.getName() + "   device mac:" + device.getAddress());
				if (device.getAddress().equals(savedS1701Mac) && BluetoothService.flag) {
					btDevice.setStatus("已连接");
					btDevices.add(btDevice);
					List<BtDevice> orderedBtDevices = generateOrderedDevices(btDevices);
					for (int i = 0; i < orderedBtDevices.size(); i++) {
						BtDevice hasBtDevice = orderedBtDevices.get(i);
						if (hasBtDevice.getDevice().getAddress().equals(savedS1701Mac)) {
							btDevices = orderedBtDevices;
							adapter = new BluetoothListAdapter(getActivity(), orderedBtDevices, true, i);
							mListBluetooth.setAdapter(adapter);
							autoConnect(orderedBtDevices);
							return;
						}
					}
				} else {
					btDevices.add(btDevice);
					List<BtDevice> orderedBtDevices = generateOrderedDevices(btDevices);
					for (int i = 0; i < orderedBtDevices.size(); i++) {
						BtDevice hasBtDevice = orderedBtDevices.get(i);
						if (hasBtDevice.getDevice().getAddress().equals(savedS1701Mac) && BluetoothService.flag) {
							btDevices = orderedBtDevices;
							adapter = new BluetoothListAdapter(getActivity(), orderedBtDevices, true, i);
							mListBluetooth.setAdapter(adapter);
							autoConnect(orderedBtDevices);
							return;
						}
					}
					adapter = new BluetoothListAdapter(getActivity(), orderedBtDevices, false, 0);
					mListBluetooth.setAdapter(adapter);
					btDevices = orderedBtDevices;
					autoConnect(orderedBtDevices);
				}
			} else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				Log.d(TAG, "搜索结束");
				AnswerMainActivity activity = (AnswerMainActivity) getActivity();
				activity.cancleBtDiscoveryAnim();
			} else if (action.equals(Broadcast.BT_START_DISCOVERY)) {
				savedS1701Mac = getActivity().getSharedPreferences("save_mac", Context.MODE_PRIVATE).getString(
						"S1701_mac", "");
				savedS1701Name = getActivity().getSharedPreferences("save_mac", Context.MODE_PRIVATE).getString(
						"S1701_name", "");
				btDevices.clear();
				autoConnectS1701Name = null;
				adapter = new BluetoothListAdapter(getActivity(), btDevices, false, 0);
				mListBluetooth.setAdapter(adapter);
			} else if (action.equals(Broadcast.ACTION_BT_CONNECT_STATE_CHANGED)) {
				String btdeviceAddress = intent.getStringExtra("btdevice_address");
				String status = intent.getStringExtra("btdevice_status");
				Log.d(TAG, "address:" + btdeviceAddress + "   status:" + status);
				for (int i = 0; i < btDevices.size(); i++) {
					BtDevice btDevice = btDevices.get(i);
					if (btDevice.getDevice().getAddress().equals(btdeviceAddress)) {
						btDevice.setStatus(status);
						if (status.equals("已连接")) {
							adapter = new BluetoothListAdapter(getActivity(), btDevices, true, i);
						} else {
							adapter = new BluetoothListAdapter(getActivity(), btDevices, false, i);
						}
						mListBluetooth.setAdapter(adapter);
					}
				}
			} else if (action.equals(Broadcast.ACTION_AUTO_CONNECT)) {
				autoConnectS1701Name = "S1701_" + intent.getStringExtra(Broadcast.EXTRA_AUTO_CONNECT_S1701);
				Log.d(TAG, "autoConnectS1701Name:" + autoConnectS1701Name);
				autoConnect(btDevices);
			}
		}

	}

	/**
	 * 更新蓝牙状态
	 * 
	 * @param device
	 *            蓝牙设备
	 * @param status
	 *            蓝牙连接以及配对状态
	 */
	public void setBtStatus(BluetoothDevice device, String status) {
		for (int i = 0; i < btDevices.size(); i++) {
			BtDevice btDevice = btDevices.get(i);
			if (btDevice.getDevice().getAddress().equals(device.getAddress())) {
				btDevice.setStatus(status);
				adapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * 
	 * @param btDevices
	 * @return 按照配对过的S1701排在前面的规则，排序后生成的S1701列表
	 */
	public List<BtDevice> generateOrderedDevices(List<BtDevice> btDevices) {
		List<BtDevice> orderedBtDevices = new ArrayList<BtDevice>();
		for (int i = 0; i < btDevices.size(); i++) {
			BtDevice btDevice = btDevices.get(i);
			if (btDevice.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
				orderedBtDevices.add(btDevice);
			}
		}
		for (int i = 0; i < btDevices.size(); i++) {
			BtDevice btDevice = btDevices.get(i);
			if (btDevice.getDevice().getBondState() != BluetoothDevice.BOND_BONDED) {
				orderedBtDevices.add(btDevice);
			}
		}
		return orderedBtDevices;
	}

	/**
	 * 根据autoConnectS1701Name在蓝牙搜索列表中自动查找连接
	 * 
	 * @param btDevices
	 * @param autoConnectS1701Name
	 */
	public void autoConnect(List<BtDevice> btDevices) {
		if (TextUtils.isEmpty(autoConnectS1701Name)) {
			Log.d(TAG, "autoConnectS1701Name is empty");
			return;
		}
		for (int i = 0; i < btDevices.size(); i++) {
			BtDevice btDevice = btDevices.get(i);
			if (btDevice.getName().equals(autoConnectS1701Name)) {
				Log.d(TAG, "开始自动连接");
				if (bluetoothAdapter.isDiscovering()) {
					bluetoothAdapter.cancelDiscovery();
				}
				BluetoothDevice device = btDevice.getDevice();
				int bondstate = device.getBondState();
				String mac = device.getAddress();
				String status = btDevice.getStatus();
				String name = btDevice.getName();
				Log.d(TAG, "the autoconnect name:" + name + " mac:" + mac + " status:" + status + " bondstate:"
						+ bondstate);
				Intent intent = new Intent(Broadcast.ACTION_NOTIFY_SERVICE_CONNNECT);
				intent.putExtra(Broadcast.EXTRA_S1701_MAC, mac);
				intent.putExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS, status);
				intent.putExtra(Broadcast.EXTRA_S1701_BONDSTATE, bondstate);
				getActivity().sendBroadcast(intent);
			}
		}
	}
}
