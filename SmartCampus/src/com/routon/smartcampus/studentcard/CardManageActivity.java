package com.routon.smartcampus.studentcard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.utils.Md5Util;
import com.routon.widgets.Toast;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.studentcard.BleUpgradeTool.BleUpgradeCallBack;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.StudentHelper;

public class CardManageActivity extends CustomTitleActivity{
	private final static String TAG = "CardManageActivity";
	private CircleProgressView mStepCircleView = null;
	private TextView mUpgradeTextView = null;
	private Button mUpgradeBtn = null;
	private Button mStopBtn = null;
	private View.OnClickListener mClickListener = null;
//	private boolean mScanFlag = false;
	private BleUpgradeTool mBleUpgradeTool = null;
	private ViewGroup mStepViewGroup = null;
	
	private int mSelIndex = 0;
	
	private String mMacAddress = null;
	private int mSId = -1;
	private String mName = null;
	private String mLocalImagePath = null;
	private String mImageUrl = null;
	private String mClass = null;
	
//	private StudentBean mStudentBean = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,   
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   
		setContentView(R.layout.activity_card_manager);
		initTitleBar("校园卡管理");
		this.setTitleBackBtnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CardManageActivity.this.finish();
			}
		});
//		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		
		
//		this.setTitleNextBtnClickListener("",mClickListener);
//		mScanFlag = true;
//		
		mBleUpgradeTool = new BleUpgradeTool(this,new BleUpgradeCallBack(){

			@Override
			public void getDownloadUrlComplete() {
				// TODO Auto-generated method stub
				//获取到下载地址后，开始扫描		
//				initListView();
//				mAdapter.setPlatformVersion(mBleUpgradeTool.getPlatformVersion());
				Toast.makeText(CardManageActivity.this, "请靠近手机摇动校园卡", Toast.LENGTH_SHORT).show();
				startScan();
			}

			@Override
			public void afterInitBlueTooth() {
				// TODO Auto-generated method stub
				//开启蓝牙后
				mBleUpgradeTool.getVersionTxtUrl();
			}

			@Override
			public void upgradeSuccess() {
				// TODO Auto-generated method stub
				if( mBleUpgradeTool.getDevices().size() > 0 && mSelIndex >= 0  ){
					mBleUpgradeTool.getDevices().get(mSelIndex).updateVersion(mBleUpgradeTool.getPlatformVersion());
					mBleUpgradeTool.getDevices().get(mSelIndex).updateProgress(-1);
				}
				hideUpgradeProgressDialog();
				setUpgradeEnable(false);
//				mAdapter.notifyDataSetChanged();
				showAlertDialog("升级成功","校园卡固件版本已经升级到最新版本");
				updateVersionText();
			}

			@Override
			public void upgradeFailed() {
				Log.d(TAG,"upgradeFailed");
				// TODO Auto-generated method stub
				if( mBleUpgradeTool.getDevices().size() > 0 && mSelIndex >= 0 ){
					mBleUpgradeTool.getDevices().get(mSelIndex).updateVersion(mBleUpgradeTool.getPlatformVersion());
					mBleUpgradeTool.getDevices().get(mSelIndex).updateProgress(-1);
				}
				hideUpgradeProgressDialog();
				showAlertDialog("升级失败","校园卡固件升级失败，请点击右上角刷新按钮重新扫描，扫描过程中摇动校园卡");
			}

			@Override
			public void upgradeProgress(int progress) {
				// TODO Auto-generated method stub
				Log.d(TAG,"upgradeProgress mSelIndex:"+mSelIndex);
				if( mSelIndex >= 0 ){
					
					mBleUpgradeTool.getDevices().get(mSelIndex).updateProgress(progress);
					hideProgressDialog();
					showUpgradeProgressDialog(progress);
				}
//				mAdapter.notifyDataSetChanged();			
			}

			@Override
			public void connectFailed() {
				Log.d(TAG,"connectFailed");
				// TODO Auto-generated method stub
//				mAdapter.notifyDataSetChanged();
				showAlertDialog("连接失败","设备连接失败，请点击右上角刷新按钮重新扫描，扫描过程中摇动校园卡");
			}

			@Override
			public void bluetoothDisconnect() {
				// TODO Auto-generated method stub
				Log.d(TAG,"bluetoothDisconnect");
				hideUpgradeProgressDialog();
				mSelIndex = 0;
				mBleUpgradeTool.initBlueTooth();
//				mAdapter.notifyDataSetChanged();
			}

			@Override
			public void findBleMac() {
				// TODO Auto-generated method stub
				//找到对应mac的ble设备
				//搜索到设备后，停止转圈
				if( mBleUpgradeTool.getDevices()!= null && mBleUpgradeTool.getDevices().size() > 0 ){
					hideProgressDialog();
					mStopBtn.setVisibility(View.INVISIBLE);
					cancelScanTask();
					BleDeviceInfo device = mBleUpgradeTool.getDevices().get(0);
					setTitleNextBtnClickListener("刷新",mClickListener);
					if( device.getVersion() < mBleUpgradeTool.getPlatformVersion() ){
						setUpgradeEnable(true);
						mBleUpgradeTool.showDownloadUpgradeDialog(0);
					}else{
						setUpgradeEnable(false);
					}
					updateVersionText();				
				}
			}		
			

			@Override
			public void needToUpgrade() {
				// TODO Auto-generated method stub
				//在范围内待升级的设备
				hideProgressDialog();
				hideAlertDialog();
				mStopBtn.setVisibility(View.INVISIBLE);
				setTitleNextBtnClickListener("刷新",mClickListener);
				cancelScanTask();
				setUpgradeEnable(true);
				mBleUpgradeTool.showNeedRepairUpgradeDialog();
				updateVersionText();
			}

			@Override
			public void updateStep() {
				// TODO Auto-generated method stub
				if( mSId == -1 ){
					return;
				}
				BleDeviceInfo device = mBleUpgradeTool.getDevices().get(0);
				mStepCircleView.setStep(device.getStep(),10000);
				uploadStepCount(device.getStep());
			}			
		});
		
//		mStudentBean = (StudentBean) this.getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
		mSId = this.getIntent().getIntExtra(MyBundleName.STUDENT_CARD_ID, -1);
		mMacAddress = this.getIntent().getStringExtra(MyBundleName.STUDENT_CARD_MAC_ADDRESS);
		mName = this.getIntent().getStringExtra(MyBundleName.STUDENT_CARD_NAME);
		mClass = this.getIntent().getStringExtra(MyBundleName.STUDENT_CARD_CLASS);
		mImageUrl = this.getIntent().getStringExtra(MyBundleName.STUDENT_CARD_IMAGE_URL);
		this.mLocalImagePath = this.getIntent().getStringExtra(MyBundleName.STUDENT_CARD_LOCAL_IMAGE_PATH);
		mBleUpgradeTool.setMacFilter(mMacAddress);
		initView();
//		TextView tipLabel = (TextView) this.findViewById(R.id.tipLabel);
//		tipLabel.setText("扫描过程中请靠近手机摇动校园卡");
		
		mBleUpgradeTool.initBlueTooth();
	}
	
	void resetVersionText(){
		TextView versionTv = (TextView) this.findViewById(R.id.version_tv);
		versionTv.setText("");
	}
	
	void updateVersionText(){
		if( mBleUpgradeTool == null ){
			return;
		}
		BleDeviceInfo device = mBleUpgradeTool.getDevices().get(0);
		if( device != null ){
			TextView versionTv = (TextView) this.findViewById(R.id.version_tv);
			int platformVersion = mBleUpgradeTool.getPlatformVersion();
	       // <!-- android:text="当前版本:5\n可升级版本:6" --> 
			if( platformVersion > device.getVersion() ){
				versionTv.setText("当前版本:"+device.getVersion()+"\n可升级版本:"+platformVersion);
			}else{
				versionTv.setText("当前版本:"+device.getVersion());
			}
		}
	}
	
	int lastStep = -1;
	
	void uploadStepCount(int count){
		if( lastStep == count ){
			return;
		}
		 // 获得当前时间  
        Date date = new Date();  
        // 获得SimpleDateFormat类  
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd"); 
        String time = sf.format(date);
        String KEY = "&8K*p%W3t4QpkHCf";
        String md5Str = (Md5Util.getMd5(mSId+time+ KEY + count));
		String urlString = SmartCampusUrlUtils.getAddStepNumber(mSId,time, count, md5Str);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                    	Log.d(TAG,"response:"+response);
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
        lastStep = count;
		
	}
	
	private ProgressDialog mUpgradeProgressDialog = null;
	
	void hideUpgradeProgressDialog(){
		if( mUpgradeProgressDialog != null ){
			mUpgradeProgressDialog.dismiss();
		}
	}
	
	void showUpgradeProgressDialog(int progress){
		if( mUpgradeProgressDialog == null ){		        
	        mUpgradeProgressDialog = new ProgressDialog(this);
	        mUpgradeProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
	        mUpgradeProgressDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
	        mUpgradeProgressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
	        mUpgradeProgressDialog.setMax(100);
	        mUpgradeProgressDialog.show();
			mUpgradeProgressDialog.setOnDismissListener(new OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					mUpgradeProgressDialog = null;
				}
				
			});
		}
		mUpgradeProgressDialog.setProgress(progress);
	}
	
	void setUpgradeEnable(boolean enable){
		if( enable == true ){
			mUpgradeTextView.setText(R.string.student_card_version_can_upgrade);
			mUpgradeBtn.setVisibility(View.VISIBLE);
		}else{
			mUpgradeTextView.setText(R.string.student_card_version_cannot_upgrade);
			mUpgradeBtn.setVisibility(View.INVISIBLE);
		}
	}
	
	void initView(){	
		TextView nameTv = (TextView)this.findViewById(R.id.student_name_tv);
		nameTv.setText(mName);
		TextView classTv = (TextView)this.findViewById(R.id.student_class_tv);
		classTv.setText(mClass);//(mStudentBean.grade+mStudentBean.staffGroup);	
		ImageView studentIv = (ImageView)this.findViewById(R.id.student_profile_iv);
		StudentHelper.loadStudentImage(mLocalImagePath, mImageUrl, this, studentIv);
		mStepCircleView = (CircleProgressView) this.findViewById(R.id.student_step_cv);
		
		mStepViewGroup = (ViewGroup)this.findViewById(R.id.student_step_rl);
		mClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startScan();
			}
		};
		
		if( mSId == -1 ){
			mStepViewGroup.setVisibility(View.GONE);
		}
		
		mUpgradeTextView = (TextView) this.findViewById(R.id.student_upgrade_tv);
		mUpgradeTextView.setText("扫描过程中靠近手机摇动校园卡");
		mUpgradeBtn = (Button) this.findViewById(R.id.student_upgrade_btn);
		mStopBtn = (Button) this.findViewById(R.id.stop_btn);
		mUpgradeBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				stopScan();
				hideAlertDialog();
				mBleUpgradeTool.showDownloadUpgradeDialog(0);
			}
		});
		mUpgradeBtn.setVisibility(View.INVISIBLE);
		
		mStopBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				stopScan();
				mUpgradeTextView.setText("没有搜索到"+CardManageActivity.this.mName+"的校园卡");
			}
		});
		mUpgradeBtn.setVisibility(View.INVISIBLE);
	}
	
	private Dialog mAlertDialog = null;
	
	void hideAlertDialog(){
		if( mAlertDialog != null ){
			mAlertDialog.dismiss();
		}
	}
	
	void showAlertDialog(String title,String message){
		//如果需要升级界面弹出，则不显示alert对话框
		if( mBleUpgradeTool.isDownloadUpgradeDialogShow() == true ){
			return;
		}
		if( mAlertDialog != null ){
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);	
		builder.setPositiveButton("确定", null);
		builder.setCancelable(true);
		mAlertDialog = builder.create();
		mAlertDialog.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				mAlertDialog = null;
			}
			
		});
		mAlertDialog.show();
	}
	
	void showUpgradeFailedDialog(){
		showAlertDialog("升级失败","校园卡固件升级失败，请点击右上角刷新按钮重新扫描，扫描过程中摇动校园卡");
	}
	
	void initListView(){
//		mListView = (ListView) findViewById(R.id.deviceListView);
//		mAdapter = new DeviceListAdapter(this,mBleUpgradeTool.getDevices());
		mClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if( mScanFlag == true ){
//					stopScan();
//				}else{
					startScan();
//				}
			}
		};
//		mListView.setAdapter(mAdapter);
//		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				// TODO Auto-generated method stub
//				if( mBleUpgradeTool.getPlatformVersion() > mBleUpgradeTool.getDevices().get(position).getVersion() ){
//					mShowDialogTimes = 1;
//					mSelIndex = position;
//					stopScan();
//					mBleUpgradeTool.showDownloadUpgradeDialog(position);
//				}
//			}
//		});
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
			    	   CardManageActivity.this.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mUpgradeBtn.setVisibility(View.INVISIBLE);
							mUpgradeTextView.setText("没有搜索到"+CardManageActivity.this.mName+"的校园卡");
							Toast.makeText(CardManageActivity.this, "没有扫描到校园卡，请重新扫描，扫描过程中靠近手机摇动校园卡", Toast.LENGTH_SHORT).show();
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
	
//	private ProgressDialog mProgressDialog = null;
	void startScan(){
		startScanTask();
//		if( mProgressDialog != null ){
//			return;
//		}
		resetVersionText();
		mUpgradeBtn.setVisibility(View.INVISIBLE);
		mUpgradeTextView.setText("正在扫描校园卡\n请靠近手机摇动校园卡...");
		mStopBtn.setVisibility(View.VISIBLE);
//		mProgressDialog = new ProgressDialog(this);
//		mProgressDialog.setMessage("正在扫描校园卡，请靠近手机摇动校园卡..."); 
//		mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//			
//			@Override
//			public void onDismiss(DialogInterface arg0) {
//				// TODO Auto-generated method stub
//				mProgressDialog = null;
//			}
//		});
//		mProgressDialog.setCancelable(false);
//		mProgressDialog.show();
		mBleUpgradeTool.startScan();
//		mAdapter.notifyDataSetChanged();
		this.setTitleNextBtnClickListener("",mClickListener);
//		mScanFlag = true;
	}
	void stopScan(){
		hideProgressDialog();
		mStopBtn.setVisibility(View.INVISIBLE);
		cancelScanTask();
		mBleUpgradeTool.stopScan();
		this.setTitleNextBtnClickListener("刷新",mClickListener);
//		mScanFlag = false;
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
