package com.routon.smartcampus;

import java.lang.ref.WeakReference;
import java.util.List;

import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.smartcampus.user.UserInfoData;
import com.routon.smartcampus.view.SlidingMenu;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Window;
import com.routon.widgets.Toast;

import com.routon.edurelease.R;
import com.routon.edurelease.receiver.NotificationHelper;
import com.tencent.android.tpush.XGCustomPushNotificationBuilder;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

public class MainActivity extends FragmentActivity {// implements
													// OnClickListener {
	private static String TAG = "MainActivity";
	private static String TPushTAG = "XGPush";

	private Fragment functionFragment = null;
	private Fragment settingFragment = null;

	private static final String function_fragment_tag = "function";
	private static final String setting_fragment_tag = "setting";

	private SlidingMenu mMenu = null;
	
	private MsgReceiver updateListViewReceiver;
	
	private static NotificationHelper helper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_campus_main);
		
		if( SmartCampusApplication.mFamilyVersion == false ){
			GroupListData.getGroupListData(this, null, null, null,true,true);
		}

		mMenu = (SlidingMenu) findViewById(R.id.id_menu);

		functionFragment = null;
		settingFragment = null;

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		functionFragment = new FunctionListFragment();
		((FunctionListFragment) functionFragment).setSlidingMenu(mMenu);
		transaction.add(R.id.fl_content, functionFragment, function_fragment_tag);

		settingFragment = new MySettingFragment();
		((MySettingFragment) settingFragment).setTitlebarBackground(this.getResources().getDrawable(
				R.drawable.student_title_bg));
		transaction.add(R.id.fl_setting_content, settingFragment, setting_fragment_tag);

		transaction.commit();
		
		initXGPushConfig();
	}
	
	@Override
	public void onBackPressed() {

		if (mMenu.isMenuOpen() == true) {
			// 处理
			mMenu.toggle();
		} else {
			// 处理
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager fm = getSupportFragmentManager();
		List<Fragment> frags = fm.getFragments();
		for (Fragment f : frags) {
			if (f != null) {
				f.onActivityResult(requestCode, resultCode, data);
			}
		}
	}
	
	
	private Message mMsg = null;
	void initXGPushConfig(){	
		helper = new NotificationHelper(getApplicationContext());
		XGPushConfig.enableDebug(this, true);
		updateListViewReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.routon.edurelease.activity.UPDATE_LISTVIEW");
		registerReceiver(updateListViewReceiver, intentFilter);
		// 1.获取设备Token
		Handler handler = new HandlerExtension(MainActivity.this);
		mMsg = handler.obtainMessage();
		
		UserInfoData userInfoData = new UserInfoData(this);
		String name = userInfoData.getTeacherName();
		if(SmartCampusApplication.mFamilyVersion){
			name = SmartCampusApplication.getFamilyVersionParentPhone();		
			String oldParentPhone = helper.getBindParentPhone();
			if(oldParentPhone != null && name.equals(oldParentPhone)==false){
				XGPushManager.unregisterPush(getApplicationContext());
			}
			helper.setBindParentPhone(name);
		}else{//老师版
			//如果家长帐号存在，绑定家长帐号，否则绑定老师帐号
			String oldParentPhone = helper.getBindParentPhone();
			if(oldParentPhone != null){
				name = oldParentPhone;
			}
		}
		XGPushManager.registerPush(getApplicationContext(), name,
				new XGIOperateCallback() {
					@Override
					public void onSuccess(Object data, int flag) {
						Log.w(TPushTAG,
								"+++ register push sucess. token:" + data);
						mMsg.obj = "+++ register push sucess. token:" + data;
						mMsg.sendToTarget();
					}

					@Override
					public void onFail(Object data, int errCode, String msg) {
						Log.w(TPushTAG,
								"+++ register push fail. token:" + data
										+ ", errCode:" + errCode + ",msg:"
										+ msg);

						mMsg.obj = "+++ register push fail. token:" + data
								+ ", errCode:" + errCode + ",msg:" + msg;
						mMsg.sendToTarget();
					}
				});
		XGCustomPushNotificationBuilder build = new XGCustomPushNotificationBuilder();
	    build.setSound(
	            RingtoneManager.getActualDefaultRingtoneUri(
	                    getBaseContext(), RingtoneManager.TYPE_NOTIFICATION)) // 设置声音
	                    // setSound(
	                    // Uri.parse("android.resource://" + getPackageName()
	                    // + "/" + R.raw.wind)) 设定Raw下指定声音文件
	                    .setDefaults(Notification.DEFAULT_VIBRATE) // 振动
	                    .setFlags(Notification.FLAG_AUTO_CANCEL); // 是否可清除
	    // 设置自定义通知layout,通知背景等可以在layout里设置
	    build.setLayoutId(R.layout.layout_notification);
	    // 设置自定义通知内容id
	    build.setLayoutTextId(R.id.ssid);
	    // 设置自定义通知标题id
	    build.setLayoutTitleId(R.id.title);
	    // 设置自定义通知图片id
	    build.setLayoutIconId(R.id.icon);
	    // 设置自定义通知图片资源
	    build.setLayoutIconDrawableId(R.drawable.ic_launcher);
//	    // 设置状态栏的通知小图标
//	    build.setIcon(R.drawable.ic_launcher);
	    //设置时间id
	    build.setLayoutTimeId(R.id.time);
	    	    
//	    Intent intent = new Intent(MainActivity.this,MainActivity.class);  
//        PendingIntent pi = PendingIntent.getActivities(MainActivity.this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT); 
	    
//        Intent appIntent=null;
//        appIntent = new Intent(MainActivity.this,MainActivity.class);
//        appIntent.setAction(Intent.ACTION_MAIN);
//        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//关键的一步，设置启动模式
//        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0,appIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//        
//        build.setContentIntent(contentIntent);
//	    // 若不设定以上自定义layout，又想简单指定通知栏图片资源
//	    build.setNotificationLargeIcon(R.drawable.tenda_icon);
	    XGPushManager.setDefaultNotificationBuilder(this, build);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		Log.d(TPushTAG, "onResumeXGPushClickedResult:" + click);
		if (click != null) { // 判断是否来自信鸽的打开方式
//			Toast.makeText(this, "通知被点击:" + click.toString(),
//					Toast.LENGTH_SHORT).show();
			Log.i(TAG, "----------" + click.toString());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(updateListViewReceiver);
		super.onDestroy();
	}
	
	private static class HandlerExtension extends Handler {
		WeakReference<MainActivity> mActivity;

		HandlerExtension(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MainActivity theActivity = mActivity.get();
			if (theActivity == null) {
				theActivity = new MainActivity();
			}
			if (msg != null) {
				Log.w(TPushTAG, msg.obj.toString());
				Log.d(TPushTAG, XGPushConfig.getToken(theActivity));

				if(SmartCampusApplication.mFamilyVersion){
					
					String groups = "";
					for(int i=0; i<SmartCampusApplication.mStudentDatas.size(); i++){
						if(i!=0){
							groups += ",";
						}	
						//家长版绑定分组前加“p_”,老师版绑定分组加"t_"
						groups += "p_"+String.valueOf(SmartCampusApplication.mStudentDatas.get(i).groupId);
					}
					
					helper.bindGroups(groups);
				}
				
				if(SmartCampusApplication.mFamilyVersion == false){
					if(InfoReleaseApplication.authenobjData.audit_classinfo_privilege == 1 ||
							InfoReleaseApplication.authenobjData.audit_schoolnotice_privilege == 1 ){
						UserInfoData userInfoData = new UserInfoData(theActivity);
						String name = userInfoData.getTeacherName();
						
						String oldName = helper.getBindTeacherName();
						if(oldName != null && name.equals(oldName)==false){
							XGPushManager.deleteTag(theActivity, oldName);
						}						
						XGPushManager.setTag(theActivity, name);
						helper.setBindTeacherName(name);
					}
				}
//				XGPushManager.setTag(theActivity, "abc");
			}
		}
	}

	public class MsgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
//			allRecorders = notificationService.getCount();
//			getNotificationswithouthint(id);
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
			@NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
		case 200:
			if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if(settingFragment != null){
					((MySettingFragment)settingFragment).startCameraActivity();
				}
			} else {
				Toast.makeText(MainActivity.this, "授权失败，无法开启扫描二维码功能!", Toast.LENGTH_LONG).show();
			}
			break;

		default:
			break;
		}
	}
}
