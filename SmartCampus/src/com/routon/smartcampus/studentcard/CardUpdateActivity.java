package com.routon.smartcampus.studentcard;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import com.routon.widgets.Toast;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;

public class CardUpdateActivity extends CustomTitleActivity{
	private final static String TAG = "DeviceSelectActivity";
	private ListView mListView = null;

	private DeviceListAdapter mAdapter = null;
	private View.OnClickListener mClickListener = null;
	private boolean mScanFlag = false;
	private BleUpgradeTool mBleUpgradeTool = null;
	
	private int mSelIndex = 0;
	
	private int mShowDialogTimes = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,   
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   
		setContentView(R.layout.activity_card_deviceselect);
		initTitleBar("校园卡管理");
		this.setTitleBackBtnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CardUpdateActivity.this.finish();
			}
		});
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		
		
		this.setTitleNextBtnClickListener("",mClickListener);
		mScanFlag = true;
		
//		mBleUpgradeTool = new BleUpgradeTool(this,new BleUpgradeCallBack(){
//
//			@Override
//			public void getDownloadUrlComplete() {
//				// TODO Auto-generated method stub
//				//获取到下载地址后，开始扫描		
//				initListView();
//				mAdapter.setPlatformVersion(mBleUpgradeTool.getPlatformVersion());
//				startScan();
//			}
//
//			@Override
//			public void afterInitBlueTooth() {
//				// TODO Auto-generated method stub
//				//开启蓝牙后
//				mBleUpgradeTool.getVersionTxtUrl();
//			}
//
//			@Override
//			public void scanDevice() {
//				// TODO Auto-generated method stub
//				mAdapter.notifyDataSetChanged();
//				if( mBleUpgradeTool.getStatus() == BleUpgradeTool.STATUS_SCAN ){
//					//搜索到设备后，停止转圈
//					if( mBleUpgradeTool.getDevices()!= null && mBleUpgradeTool.getDevices().size() > 0 ){
//						mProgressDialog.hide();
//						cancelScanTask();
//					}
//				}
//			}
//
//			@Override
//			public void upgradeSuccess() {
//				// TODO Auto-generated method stub
//				if( mBleUpgradeTool.getDevices().size() > 0 && mSelIndex >= 0  ){
//					mBleUpgradeTool.getDevices().get(mSelIndex).updateVersion(mBleUpgradeTool.getPlatformVersion());
//					mBleUpgradeTool.getDevices().get(mSelIndex).updateProgress(-1);
//				}
//				mAdapter.notifyDataSetChanged();
//				showUpgradeSuccessDialog();
//			}
//
//			@Override
//			public void upgradeFailed() {
//				// TODO Auto-generated method stub
//				if( mBleUpgradeTool.getDevices().size() > 0 && mSelIndex >= 0 ){
//					mBleUpgradeTool.getDevices().get(mSelIndex).updateVersion(mBleUpgradeTool.getPlatformVersion());
//					mBleUpgradeTool.getDevices().get(mSelIndex).updateProgress(-1);
//				}
//				mAdapter.notifyDataSetChanged();
//				showUpgradeFailedDialog();
//			}
//
//			@Override
//			public void upgradeProgress(int progress) {
//				// TODO Auto-generated method stub
//				mBleUpgradeTool.getDevices().get(mSelIndex).updateProgress(progress);
//				mAdapter.notifyDataSetChanged();			
//			}
//
//			@Override
//			public void connectFailed() {
//				// TODO Auto-generated method stub
//				mAdapter.notifyDataSetChanged();
//				Toast.makeText(CardUpdateActivity.this, "设备连接失败，请重新扫描设备或者摇动要升级的设备", Toast.LENGTH_SHORT).show();
//			}
//
//			@Override
//			public void bluetoothDisconnect() {
//				// TODO Auto-generated method stub
//				mSelIndex = -1;
//				mBleUpgradeTool.initBlueTooth();
//				mAdapter.notifyDataSetChanged();
//			}
//			
//		});
//		mBleUpgradeTool.initBlueTooth();
//		
//		TextView tipLabel = (TextView) this.findViewById(R.id.tipLabel);
//		tipLabel.setText("扫描过程中请靠近手机摇动校园卡");
	}
	
	void showUpgradeSuccessDialog(){
		if( mShowDialogTimes == 0 ){
			return;
		}
		mShowDialogTimes--;
		// 构造对话框
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("升级成功");
		builder.setMessage("校园卡固件版本已经升级到最新版本");	
		builder.setPositiveButton("确定", null);
		builder.setCancelable(true);
		Dialog dialog = builder.create();
		dialog.show();
	}
	
	void showUpgradeFailedDialog(){
		if( mShowDialogTimes == 0 ){
			return;
		}
		mShowDialogTimes--;
		// 构造对话框
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("升级失败");
		builder.setMessage("校园卡固件升级失败，请重新扫描后选择升级，扫描过程中摇动校园卡");	
		builder.setPositiveButton("确定", null);
		builder.setCancelable(true);
		Dialog dialog = builder.create();
		dialog.show();
	}
	
	void initListView(){
		mListView = (ListView) findViewById(R.id.deviceListView);
		mAdapter = new DeviceListAdapter(this,mBleUpgradeTool.getDevices());
		mClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( mScanFlag == true ){
//					stopScan();
				}else{
					startScan();
				}
			}
		};
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if( mBleUpgradeTool.getPlatformVersion() > mBleUpgradeTool.getDevices().get(position).getVersion() ){
					mShowDialogTimes = 1;
					mSelIndex = position;
					stopScan();
					mBleUpgradeTool.showDownloadUpgradeDialog(position);
				}
			}
		});
	}
	
	private final Timer mTimer = new Timer(); 
	TimerTask mScanTask = null;
	private final int SCAN_TIMEOUT = 60000; //60s
	
	void startScanTask(){
		 Log.d(TAG,"startScanTask");
		 if( mScanTask == null ){
			 mScanTask = new TimerTask() { 
				    @Override 
				    public void run() { 
				        // TODO Auto-generated method stub 				    	
			    	   Log.d(TAG,"scan timeout");
			    	   CardUpdateActivity.this.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(CardUpdateActivity.this, "没有扫描到校园卡，请重新扫描，扫描过程中靠近手机摇动校园卡", Toast.LENGTH_SHORT).show();
							stopScan();
						}
			    		   
			    	   });
				    } 
				}; 
		 	}
		 mTimer.schedule(mScanTask, SCAN_TIMEOUT);
		
	 }
	
	 void cancelScanTask(){
		 Log.d(TAG,"cancelScanTask");
		 if( mScanTask != null ){
			 mScanTask.cancel();
			 mScanTask = null;
		 }
	 }
	
	private ProgressDialog mProgressDialog = null;
	void startScan(){
		startScanTask();
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("正在扫描校园卡\n请靠近手机摇动校园卡..."); 
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
		mBleUpgradeTool.startScan();
		mAdapter.notifyDataSetChanged();
		this.setTitleNextBtnClickListener("",mClickListener);
		mScanFlag = true;
	}
	void stopScan(){
		mProgressDialog.hide();
		cancelScanTask();
		mBleUpgradeTool.stopScan();
		this.setTitleNextBtnClickListener("扫描",mClickListener);
		mScanFlag = false;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
//		unregisterReceiver(mReceiver);
		super.onPause();
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		if( requestCode == 0 ){
			if( resultCode == Activity.RESULT_OK ){
				this.finish();
			}
		}
	}

	
	protected void onDestroy(){
		super.onDestroy();
		mBleUpgradeTool.exit();	
	}
}
