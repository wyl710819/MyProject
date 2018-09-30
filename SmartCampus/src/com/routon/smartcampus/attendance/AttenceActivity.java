package com.routon.smartcampus.attendance;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.answerrelease.service.BluetoothService;
import com.routon.smartcampus.answerrelease.service.Broadcast;
import com.routon.smartcampus.answerrelease.service.BtDevice;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.widgets.Toast;

public class AttenceActivity extends BaseFragmentActivity{
	
	private AttenceStartFragment attenceStartFragment;
	private AttenceSearchFragment attenceSearchFragment;
	private AttenceBottomBar attenceBottomBar;
	
	private BluetoothAdapter bluetoothAdapter;
	private volatile boolean stopConnected;
	private Timer timer;
	private FragmentManager fm;
	
	private static final String TAG = "AttenceActivity";
	
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "服务启动");
		}
	};

	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_attence);
		initData();
		initView();
	}

	public void initData(){
		stopConnected = false;
		Log.d(TAG, "onCreate----stopConnected:" + stopConnected);
		fm = getSupportFragmentManager();
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// 实例化获取图片全路径
		FaceRecognizeMgr.getInstance(this);
		// 启动服务
		Intent intent = new Intent(this, BluetoothService.class);
		bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}
	
	public void initView(){
		setMoveBackEnable(true);
		showAttenceStartFragment();
		attenceBottomBar = (AttenceBottomBar)findViewById(R.id.bottom_bar_attence);
		attenceBottomBar.setType(AttenceBottomBar.TYPE_START);
		attenceBottomBar.setAttenceBottomClickListener(new AttenceBottomClickListener() {
			
			@Override
			public void onStartClick(View view) {
				//attenceBottomBar.setType(AttenceBottomBar.TYPE_START);
				//showAttenceStartFragment();
			}
			
			@Override
			public void onSearchClick(View view) {
				//attenceBottomBar.setType(AttenceBottomBar.TYPE_SEARCH);
				//showAttenceSearchFragment();
				startSearchActivity();
			}
			
			@Override
			public void onExitClick(View view) {
				AttenceActivity.this.finish();
			}
		});
	}
	
	// 切换到后台30s后发送结束答题指令并关闭蓝牙连接
	@Override
	public void onStop() {
		super.onStop();
		if (bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}
		stopConnected = true;
		Log.d(TAG, "onStop----stopConnected:" + stopConnected);
		startTimer();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		stopConnected = false;
		Log.d(TAG, "onRestart----stopConnected:" + stopConnected);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		if(timer != null){
			timer.cancel();
		}
	}
	
	public void startTimer(){
		timer =new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (stopConnected) {
					Intent disIntent = new Intent(Broadcast.ACTION_NOTIFY_SERVICE_DISCONNECT);
					sendBroadcast(disIntent);
				}
			}
		}, 30000);
	};
	
	public void showAttenceStartFragment(){
		attenceStartFragment = (AttenceStartFragment) fm.findFragmentByTag("AttenceStartFragment");
		FragmentTransaction transaction = fm.beginTransaction();
		if(attenceStartFragment == null){
			attenceStartFragment = new AttenceStartFragment();
			transaction.add(R.id.fl_attence_main, attenceStartFragment, "AttenceStartFragment");
			transaction.show(attenceStartFragment);
		}else {
			transaction.show(attenceStartFragment);
		}
		transaction.commit();
	}
	
	public void showAttenceSearchFragment(){
		attenceSearchFragment = (AttenceSearchFragment)fm.findFragmentByTag("AttenceSearchFragment");
		attenceStartFragment = (AttenceStartFragment) fm.findFragmentByTag("AttenceStartFragment");
		FragmentTransaction transaction = fm.beginTransaction();
		if(attenceStartFragment != null){
			transaction.hide(attenceStartFragment);
		}
		attenceSearchFragment = new AttenceSearchFragment();
		if(attenceStartFragment != null){
			BtDevice btDevice = attenceStartFragment.getSelectBtDevice();
			Bundle bundle = new Bundle();
			bundle.putInt("classGroupId", btDevice.getGroupId());
			bundle.putInt("sid", attenceStartFragment.teacherId);
			bundle.putString("className", btDevice.getClassName());
			attenceSearchFragment.setArguments(bundle);
		}
		transaction.add(R.id.fl_attence_main, attenceSearchFragment, "AttenceSearchFragment");
		transaction.show(attenceSearchFragment);
		transaction.commit();
	}
	
	public void startSearchActivity(){
		BtDevice btDevice = attenceStartFragment.getSelectBtDevice();
		if(btDevice == null){
			Toast.makeText(this, "还未获取班级信息", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(this , SearchClassAttenceActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("classGroupId", btDevice.getGroupId());
		bundle.putInt("sid", attenceStartFragment.teacherId);
		bundle.putString("className", btDevice.getClassName());
		intent.putExtras(bundle);
		startActivity(intent);
	}
}
