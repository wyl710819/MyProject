package com.routon.inforelease.offline;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.routon.ad.element.StringUtils;
import com.routon.ad.pkg.HttpPostTask;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.R;
import com.routon.inforelease.TerminalListHelper;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.inforelease.plan.PlanListFragment;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.remotecontrol.BluetoothChatService;
import com.routon.remotecontrol.BluetoothSendRecv;

public class AdOfflineReleaseActivity extends CustomTitleActivity {
	private static final String TAG = "AdOfflineRelease";

	private LinearLayout mStatusView;

	private String mHostApName = "";
	private String mHostApPwd = "";
	private String mServerIp = "192.168.43.1";
	private int mServerPort = 8080;

	private BluetoothChatService mChatService;

	private WifiAdmin mWifiAdmin;

	private static final int STEP_BLUETOOTH_CONNECT = 1;
	private static final int STEP_REQ_TERMINAL_INFO = 2;
	// private static final int STEP_REQ_NET_HOSTAP = 3;
	private static final int STEP_CONNECT_AP = 3;
	private static final int STEP_REQ_OFFLINE_RELEASE = 4;
	private static final int STEP_UPLOAD_PKG = 5;
	private int mStep;

	private String mUploadFilePath;
	private int mUploadFileType;
	private PlanListrowsBean mBean = null;
	
	private String mBlueMac;
	private String mReplaceValue = "true";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		termId = this.getIntent().getStringExtra("termId");
		mBlueMac = this.getIntent().getStringExtra("blueMac");
		startBy = this.getIntent().getStringExtra("start_by");
		mUploadFilePath = this.getIntent().getStringExtra("path");
		mUploadFileType = this.getIntent().getIntExtra("type", 0);
		planName = this.getIntent().getStringExtra("plan_name");
		planId = this.getIntent().getStringExtra("plan_id");
		mBean = (PlanListrowsBean)(this.getIntent().getSerializableExtra("data"));

		setContentView(R.layout.layout_offline_release);
		initView();

		mChatService = BluetoothChatService.getInstance();
		mChatService.init(this, mHandler);

		WifiAdmin wifiAdmin = new WifiAdmin(this);
		mWifiAdmin = wifiAdmin;

		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(mWifiStateReceiver, filter);
		
		showReplaceDialog();
	}
	
	private void startProcess() {
		if (mBlueMac != null && mBlueMac.length() > 0 && termId != null) {
			addStatus("正在建立蓝牙连接");
			mStep = STEP_BLUETOOTH_CONNECT;
			mChatService.connectBlueMACDevice(mBlueMac);
			mHandler.sendEmptyMessageDelayed(MSG_DEVICE_CONNECT_TIMEOUT, 10000);
		} else {
			reqTerminalInfo();
		}

		showUploadProg();		
	}
	
	private void showReplaceDialog() {
		AlertDialog dlg = new AlertDialog.Builder(this).setTitle("离线发布").setMessage("是否覆盖已有内容？")
				.setNegativeButton("覆 盖", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mReplaceValue = "true";
						startProcess();
					}
				}).setPositiveButton("不覆盖", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mReplaceValue = "false";
						startProcess();
					}
				}).create();
		dlg.show();
	}

	private void initView() {
		mStatusView = (LinearLayout) findViewById(R.id.status_view);
		mStatusView.setVisibility(View.GONE);

		// title bar
		this.initTitleBar("离线文件发布");
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mWifiStateReceiver);

		// BluetoothChatService.getInstance().stop();

		super.onDestroy();
	}

	private boolean mActivated = true;

	@Override
	protected void onPause() {
//		mActivated = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
//		mActivated = true;
		super.onResume();
	}

	private void addStatus(String text) {
		TextView view = new TextView(this);
		view.setText(text);
		mStatusView.addView(view, 0);
	}

	private void reqTerminalInfo() {
		if (mInterrupt)
			return;
		mStep = STEP_REQ_TERMINAL_INFO;
		addStatus("正在查询终端信息");
		Log.v(TAG, "send terminal info req");
		Bundle bdl = new Bundle();
		bdl.putString(BluetoothSendRecv.KEY_ACTION, BluetoothSendRecv.terminal_info_req);
		// bdl.putString(BluetoothSendRecv.KEY_DATA, "");
		mChatService.cfg_net_req(bdl);
		mHandler.sendEmptyMessageDelayed(MSG_REQ_TERMINAL_INFO_TIMEOUT, 10000);

		// Message msg = Message.obtain(mHandler,
		// BluetoothChatService.MESSAGE_READ);
		// Bundle d = new Bundle();
		// d.putString(BluetoothSendRecv.KEY_ACTION,
		// BluetoothSendRecv.terminal_info_req_ack);
		// d.putString(BluetoothSendRecv.KEY_DATA,
		// "ap_name:CI-24T-ROUTON;ap_key:Jldz600355;ap_ip:192.168.43.1");
		// msg.setData(d);
		// mHandler.sendMessageDelayed(msg, 5000);
	}

	private void cfgTerminalNet2HostAp() {
		if (mInterrupt)
			return;
		// mStep = STEP_REQ_NET_HOSTAP;
		// addStatus("请求终端切换为HostAP模式");
		// Log.v(TAG, "send config terminal network to host ap mode req");
		// Bundle bdl = new Bundle();
		// String data = "type=hostap&savenet=true&timeout=120";
		// bdl.putString(BluetoothSendRecv.KEY_ACTION,
		// BluetoothSendRecv.cfg_net_req_req);
		// bdl.putString(BluetoothSendRecv.KEY_DATA, data);
		// mChatService.cfg_net_req(bdl);
		// mHandler.sendEmptyMessageDelayed(MSG_REQ_NET_HOSTAP_TIMEOUT, 30000);
	}

	private void cfgTerminalPrepareOfflineRelease() {
		if (mInterrupt)
			return;
		mStep = STEP_REQ_OFFLINE_RELEASE;
		Log.v(TAG, "send release offline prog req");
		addStatus("向终端请求离线文件传输");
		Bundle bdl = new Bundle();
		bdl.putString(BluetoothSendRecv.KEY_ACTION, BluetoothSendRecv.release_offline_prog_req);
		bdl.putString(BluetoothSendRecv.KEY_DATA, "");
		mChatService.cfg_net_req(bdl);
		mHandler.sendEmptyMessageDelayed(MSG_REQ_RELEASE_OFFLINE_TIMEOUT, 10000);
	}

	private void restoreTerminalNet() {
		// addStatus("请求终端恢复网络模式");
		// Log.v(TAG, "send restore terminal network req");
		// Bundle bdl = new Bundle();
		// String data = "type=restorenet";
		// bdl.putString(BluetoothSendRecv.KEY_ACTION,
		// BluetoothSendRecv.cfg_net_req_req);
		// bdl.putString(BluetoothSendRecv.KEY_DATA, data);
		// mChatService.cfg_net_req(bdl);
	}

	private void startUpload() {
		if (mInterrupt)
			return;
		mStep = STEP_UPLOAD_PKG;
		addStatus("开始向终端传送离线文件");
		String url = "http://" + mServerIp + ":" + mServerPort + "/upload?type=" + mUploadFileType + "&replace=" + mReplaceValue;
		Log.v(TAG, "post offline pkg url: " + url);
		new Thread(new HttpPostTask(url, null, "application/octet-stream", mUploadFilePath,
				new HttpPostTask.OnHttpPostTaskListener() {

					@Override
					public void onTaskFinished(HttpPostTask task, int code) {
						Message msg = Message.obtain(mHandler, MSG_UPLOAD_OFFLINE_PKG, code, 0);
						mHandler.sendMessage(msg);
						Log.v(TAG, "post offline pkg finished ");
						mHandler.sendEmptyMessageDelayed(MSG_BLUETOOTH_RESULT_TIMEOUT, 5000);
						
					}
				})).start();
		// showUploadProg();
	}

	private ProgressDialog mUploadProgDlg;

	private void showUploadProg() {
		if (mUploadProgDlg == null) {
			mUploadProgDlg = new ProgressDialog(this);
			mUploadProgDlg.setTitle("离线发布");
			mUploadProgDlg.setMessage("正在发布离线文件");
			mUploadProgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mUploadProgDlg.setCancelable(false);
			// mUploadProgDlg.setButton(ProgressDialog.BUTTON_POSITIVE, "确 定",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// finish();
			// }
			// });
		}

		mUploadProgDlg.show();
	}

	private boolean mInterrupt = false;

	private void showMsg(String msg) {
		if (mInterrupt)
			return;
		mInterrupt = true;

		// 仅AP连接步骤后发生的错误需要恢复手机wifi状态
		if (mStep >= STEP_CONNECT_AP) {
			restoreWifiConnect();
		}

		if (!mActivated)
			return;
		addStatus(msg);
		if (mUploadProgDlg != null) {
			mUploadProgDlg.dismiss();
		}

		AlertDialog dlg = new AlertDialog.Builder(this).setTitle("离线发布").setMessage(msg)
				.setNegativeButton("确 定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create();
		dlg.show();
	}

	private static final int MSG_UPLOAD_OFFLINE_PKG = 100;
	private static final int MSG_DEVICE_CONNECT_TIMEOUT = 101;
	private static final int MSG_REQ_TERMINAL_INFO_TIMEOUT = 102;
	private static final int MSG_REQ_RELEASE_OFFLINE_TIMEOUT = 103;
	private static final int MSG_CONNECT_AP_TIMEOUT = 104;
	private static final int MSG_REQ_NET_HOSTAP_TIMEOUT = 105;
	private static final int MSG_BLUETOOTH_RESULT_TIMEOUT = 106;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothChatService.MESSAGE_READ:
				onReadChatMessage(msg.getData());
				break;

			case BluetoothChatService.MESSAGE_DEVICE_CONNECT_OK:
				addStatus("蓝牙连接成功");
				mHandler.removeMessages(MSG_DEVICE_CONNECT_TIMEOUT);
				if (mStep == STEP_BLUETOOTH_CONNECT) {
					reqTerminalInfo();
				}
				break;

			case BluetoothChatService.MESSAGE_DEVICE_CONNECT_FAIL:
				addStatus("蓝牙连接失败");
				showMsg("蓝牙连接失败");
				mHandler.removeMessages(MSG_DEVICE_CONNECT_TIMEOUT);
				break;

			case BluetoothChatService.MESSAGE_DEVICE_CONNECT_LOST:
				addStatus("蓝牙连接丢失");
				mHandler.removeMessages(MSG_DEVICE_CONNECT_TIMEOUT);
				break;

			case MSG_UPLOAD_OFFLINE_PKG:
				restoreTerminalNet();
				if (msg.arg1 == 0) {
					addStatus("离线文件传送完成");
					Log.d("adoffline","mBean:"+mBean);
					if( mBean != null ){
						if( mBean.terminalIDs.trim().isEmpty() == true ){
							mBean.terminalIDs = mTerminalId;
						}else{
							mBean.terminalIDs = mBean.terminalIDs + ","+mTerminalId;
						}
						Log.d("adoffline","mBean.terminalIDs:"+mBean.terminalIDs+",mTerminalId:"+mTerminalId);
						OfflinePackageMgr offlinePkgMgr = new OfflinePackageMgr(AdOfflineReleaseActivity.this);
						offlinePkgMgr.savePlanInfo(mBean);
						//通知刷新计划列表
						Intent intent = new Intent(PlanListFragment.ACTION_PLAN_LIST_CHANGED);
						AdOfflineReleaseActivity.this.sendBroadcast(intent);
					}
					// Toast.makeText(AdOfflineReleaseActivity.this, "发布离线计划成功",
					// Toast.LENGTH_SHORT).show();
				} else {
					showMsg("离线文件传送失败");
					// Toast.makeText(AdOfflineReleaseActivity.this, "发布离线计划失败",
					// Toast.LENGTH_SHORT).show();
				}
				break;

			case MSG_DEVICE_CONNECT_TIMEOUT:
				showMsg("蓝牙连接到终端超时");
				break;

			case MSG_REQ_TERMINAL_INFO_TIMEOUT:
				showMsg("获取终端信息超时");
				break;

			case MSG_REQ_NET_HOSTAP_TIMEOUT:
				showMsg("切换终端网络超时");
				break;

			case MSG_REQ_RELEASE_OFFLINE_TIMEOUT:
				restoreTerminalNet();
				showMsg("发布离线文件超时");
				break;

			case MSG_CONNECT_AP_TIMEOUT:
				restoreTerminalNet();
				showMsg("连接终端AP超时");
				break;
			case MSG_BLUETOOTH_RESULT_TIMEOUT:
				showMsg("等待结果超时");
				break;
			}

		}
	};

	protected void onReadChatMessage(Bundle bundle) {
		String action = bundle.getString(BluetoothSendRecv.KEY_ACTION);
		String data = bundle.getString(BluetoothSendRecv.KEY_DATA);
		Log.v(TAG, "onReadChatMessage action: " + action + " data: " + data);

		if (action.equals(BluetoothSendRecv.cfg_net_ack)) {
			Map<String, String> map = parseData2Map(data, "=");
			String result = map.get("result");
			Log.v(TAG, "result: " + result);
			// if (mStep == STEP_REQ_NET_HOSTAP) {
			// mHandler.removeMessages(MSG_REQ_NET_HOSTAP_TIMEOUT);
			// if ("success".equals(result)) {
			// mHostApName = map.get("hostap_ap");
			// mHostApPwd = map.get("hostap_pw");
			// mServerIp = map.get("hostap_ip");
			// Log.v(TAG, "host ap name: " + mHostApName + " pwd: " + mHostApPwd
			// + " ip: " + mServerIp);
			// addStatus("");
			// changeWifiConnectHostAp();
			// } else {
			// showMsg("切换终端网络状态失败");
			// }
			// } else {
			// Log.v(TAG, "current status is not req net hostap");
			// }
		} else if (action.equals(BluetoothSendRecv.terminal_info_req_ack)) {
			mHandler.removeMessages(MSG_REQ_TERMINAL_INFO_TIMEOUT);
			Map<String, String> map = parseData2Map(data, "=");
			mTerminalId = map.get("termid");
			mHostApName = map.get("ap_name");
			mHostApPwd = map.get("ap_key");
			mServerIp = map.get("ap_ip");
			Log.v(TAG, "host ap name: " + mHostApName + " pwd: " + mHostApPwd + " ip: " + mServerIp
					+",mTerminalId:"+mTerminalId+",termId:"+termId);
			addStatus("查询到终端热点名为: " + mHostApName);
			if (startBy!=null&&startBy.equals("deviceSelect")) {
				checkAuthorizationWthBtMac(null,mTerminalId);
				
//				if (mTerminalId != null && termId!=null&&termId.equals(mTerminalId)) {
//					
//					if (mHostApName == null || mHostApName.isEmpty()) {
//						showMsg("未查询到终端热点状态，请确认终端网络是否为热点模式");
//						if (mStep == STEP_REQ_TERMINAL_INFO) {
//							// cfgTerminalNet2HostAp();
//						}
//					} else {
//						if (mStep == STEP_REQ_TERMINAL_INFO) {
//							changeWifiConnectHostAp();
//						}
//					}
//					
//				}else{
//					mUploadProgDlg.dismiss();
//					showMsg("您无权管理这台终端");
//				}
			}else {
				if (mHostApName == null || mHostApName.isEmpty() || mHostApPwd == null || mHostApPwd.isEmpty()) {
					showMsg("未查询到终端热点状态，请确认终端网络是否为热点模式");
					if (mStep == STEP_REQ_TERMINAL_INFO) {
						// cfgTerminalNet2HostAp();
					}
				} else {
					if (mStep == STEP_REQ_TERMINAL_INFO) {
						if (mHostApPwd.length() > 8) {
							showMsg("终端网络配置不支持离线发布");
							return;
						}
						changeWifiConnectHostAp();
					}
				}
			}
			
			
		} else if (action.equals(BluetoothSendRecv.hostap_info_ack)) {
			mHandler.removeMessages(MSG_REQ_RELEASE_OFFLINE_TIMEOUT);
			Map<String, String> map = parseData2Map(data, "=");
			String port = map.get("port");
			Log.v(TAG, "host ap serv port: " + port);
			mServerPort = StringUtils.toInteger(port, 8080);
			addStatus("终端接受已离线文件传输请求");

			if (mStep == STEP_REQ_OFFLINE_RELEASE) {
				startUpload();
			}
			
		} else if (action.equals(BluetoothSendRecv.release_offline_prog_ack)) {
			Map<String, String> map = parseData2Map(data, "=");
			String result = map.get("result");
			Log.v(TAG, "result: " + result);
			mHandler.removeMessages(MSG_BLUETOOTH_RESULT_TIMEOUT);
			if ("success".equals(result)) {
				if (planName!=null) {
					PublishStateUtils.writeData(this,getDir("isPublishOffPlan.txt", Context.MODE_PRIVATE).getPath(),planName);
					
				}else {
					PublishStateUtils.writeData(this,getDir("isPublishPlan.txt", Context.MODE_PRIVATE).getPath(),planId);
				}
				
				Intent intent = new Intent(PlanListFragment.ACTION_PLAN_OFF_CHANGED);
				this.sendBroadcast(intent);
				showMsg("离线计划发布成功");
				
			} else {
				showMsg("离线计划发布失败");
			}
		}
	}
	
	private void checkAuthorizationWthBtMac(final String btMac, final String termId) {
		Log.d(TAG,"checkAuthorizationWthBtMac btMac:"+btMac+",termId:"+termId);
    	TerminalListHelper.getTerminalAuth(this,btMac,termId,false,new TerminalListHelper.Response() {
			
			@Override
			public void onSuccess(String text, TerminalListdatasBean data) {
				// TODO Auto-generated method stub
				if (mHostApName == null || mHostApName.isEmpty() || mHostApPwd == null || mHostApPwd.isEmpty()) {
					showMsg("未查询到终端热点状态，请确认终端网络是否为热点模式");
					if (mStep == STEP_REQ_TERMINAL_INFO) {
						// cfgTerminalNet2HostAp();
					}
				} else {
					if (mStep == STEP_REQ_TERMINAL_INFO) {
						if (mHostApPwd.length() > 8) {  // 密码长度大于8,表示是4G+hostap模式，不能离线发布
							showMsg("终端网络配置不支持离线发布");
							return;
						}
						changeWifiConnectHostAp();
					}
				}
			}
			
			@Override
			public void onFailed(String text) {
				// TODO Auto-generated method stub
				mUploadProgDlg.dismiss();
				showMsg(text);
			}
			
			@Override
			public void onError(String text) {
				// TODO Auto-generated method stub
				mUploadProgDlg.dismiss();
				showMsg(text);
			}
		});
	}

	private int mLastNetworkId = -1;
	
	private WifiConfiguration mHostWifiConfig = null;

	private void changeWifiConnectHostAp() {
		addStatus("正在连接到终端wifi热点");
		mStep = STEP_CONNECT_AP;
		mHandler.sendEmptyMessageDelayed(MSG_CONNECT_AP_TIMEOUT, 60000);
		Log.v(TAG, "changeWifiConnectHostAp");

		mLastNetworkId = -1;
		if (mWifiAdmin.isWifiOpened()) {
			mLastNetworkId = mWifiAdmin.getNetworkId();

			mHostWifiConfig = mWifiAdmin.CreateWifiInfo(mHostApName, mHostApPwd, 3);
			mWifiAdmin.addNetwork(mHostWifiConfig);
		} else {
			addStatus("正在打开手机wifi");
			mWifiAdmin.openWifi();
		}
	}

	private void restoreWifiConnect() {
		if( mHostWifiConfig != null ){
			mWifiAdmin.removeWifiConfig(mHostWifiConfig);
			mHostWifiConfig = null;
		}
		if (mLastNetworkId >= 0) {
			addStatus("恢复wifi连接");
			mWifiAdmin.restoreNetwork(mLastNetworkId);
		} else {
			mWifiAdmin.closeWifi();
		}
	}

	private Map<String, String> parseData2Map(String data, String expr) {
		Map<String, String> map = new HashMap<String, String>();
		if (data == null || data.isEmpty())
			return map;
		String[] split = data.split("&");
		if (split == null)
			return map;
		for (String s : split) {
			String[] split1 = s.split(expr);
			if (split1 == null || split1.length != 2)
				continue;
			map.put(split1[0], split1[1]);
		}
		return map;
	}

	private WifiReceiver mWifiStateReceiver = new WifiReceiver();

	private String mTerminalId;

	private String termId;

	private String startBy;

	private String planName;

	private String planId;

	class WifiReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
				// signal strength changed
			} else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {// wifi连接上与否
				Log.v(TAG, "网络状态改变");
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
					Log.v(TAG, "wifi网络连接断开");
				} else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {

					WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();

					// 获取当前wifi名称
					Log.v(TAG, "连接到网络:" + wifiInfo.getSSID() + " length: " + wifiInfo.getSSID().length());
					Log.v(TAG, "期望连接到网络:" + mHostApName + " length: " + mHostApName.length());
					addStatus("手机wifi已连接到：" + wifiInfo.getSSID());
					if (mHostApName != null && mHostApName.length() > 0) {
						if (wifiInfo.getSSID().trim().contains(mHostApName.trim())) {
							Log.v(TAG, "connect hoast ap success");
							addStatus("手机wifi已连接到终端热点");
							mHandler.removeMessages(MSG_CONNECT_AP_TIMEOUT);
							if (mStep == STEP_CONNECT_AP) {
								cfgTerminalPrepareOfflineRelease();
							}
						}
					}
				}
			} else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {// wifi打开与否
				int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);

				if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
					Log.v(TAG, "系统关闭wifi");
				} else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
					Log.v(TAG, "系统开启wifi");
					if (mStep == STEP_CONNECT_AP) {
						addStatus("手机wifi已打开");
						mHostWifiConfig = mWifiAdmin.CreateWifiInfo(mHostApName, mHostApPwd, 3);
						mWifiAdmin.addNetwork(mHostWifiConfig);
					}
				}
			}
		}
	}
}
