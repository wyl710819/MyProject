package com.routon.smartcampus;


import java.util.ArrayList;
import java.util.Calendar;

import com.android.volley.VolleyError;
import com.routon.common.BaseFragment;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.groupmanager.GroupActivity;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.usercontrol.UserListActivity;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.widget.SettingItem;
import com.routon.smartcampus.createcode.ClassListActivity;
import com.routon.smartcampus.createcode.CreateQRImageActivity;
import com.routon.smartcampus.message.MessageActivity;
import com.routon.smartcampus.message.MessageData;
import com.routon.smartcampus.message.MessageDataHelper;
import com.routon.smartcampus.user.ChildListActivity;
import com.routon.smartcampus.user.ParentUserAdminActivity;
import com.routon.smartcampus.user.ScreenGroupChangeActivity;
import com.routon.smartcampus.user.ScreenGroupSelectActivity;
import com.routon.smartcampus.user.TeacherUserAdminActivity;
import com.routon.smartcampus.user.UserHelper;
import com.routon.smartcampus.user.UserInfoData;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.edurelease.R;

/**
 * 设置界面
 * @author xiaolp
 *
 */
public class MySettingFragment extends BaseFragment{
	
	private final String TAG = "MySettingFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setting, container, false);
	}
	
	public void onStart(){
		super.onStart();
		getMessageData();
	}
	
	public String getVersionName(Context context)
	{
		String versionName = null;
		try
		{
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionName = context.getPackageManager().getPackageInfo(this.getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return versionName;
	}
	
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}	
	
	private ArrayList<Integer> mItemNames = new ArrayList<Integer>();	
	private ArrayList<Integer> mItemIcons = new ArrayList<Integer>();
	
	private void showAboutDialog(){
	
		Context context = this.getContext();
		LinearLayout linearlayout = new LinearLayout(context);
		int padding = (int) context.getResources().getDimension(R.dimen.abount_dialog_padding);
		linearlayout.setPadding(padding, padding, padding, padding);
		ImageView image = new ImageView(context);
		image.setImageResource(R.drawable.ic_company);
		image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		linearlayout.addView(image);
		
		TextView textview = new TextView(context);
		int textPadding = (int) context.getResources().getDimension(R.dimen.abount_dialog_text_padding);
		textview.setPadding(textPadding, 0, 0, 0);
		String ver = getVersionName(this.getContext());	
		
		String text = context.getString(R.string.app_name) +"\nV"+ver+"\n"+ context.getString(R.string.abount_text_2)
				+"\n@"+Calendar.getInstance().get(Calendar.YEAR)+" Jinglun,Inc.";
		textview.setText(text);
		textview.setTextColor(Color.BLACK);
		textview.setGravity(Gravity.CENTER);
		textview.setTextSize(18);
		linearlayout.addView(textview);
		
		new AlertDialog.Builder(context)
	 	.setIcon(android.R.drawable.ic_dialog_info)
	 	.setView(linearlayout)
	 	.setPositiveButton("确定", null)
	 	.show();
	}
	
	private ArrayList<MessageData> mMessageData = null;
	
	//平台接口未调试通过，暂时屏蔽消息中心功能
	public void getMessageData(){
		mMessageData = null;
		MessageDataHelper.getPushMsgListData(getOwnActivity(), new DataResponse.Listener<ArrayList<MessageData>>() {

			@Override
			public void onResponse(ArrayList<MessageData> response) {
				// TODO Auto-generated method stub
				if( getView() == null){
					return;
				}
				mMessageData = new ArrayList<MessageData>();
				if( response != null ){
					mMessageData.addAll(response);
				}
				boolean hasNewMessage = MessageDataHelper.hasNewMessage(mMessageData);
				setMessageItem(hasNewMessage);
			}
		}, null, null);
//		MessageData data = new MessageData();
//		data.title = "小红花消息";
//		data.isNew = 1;
//		data.content = "收到一朵回答正确小红花";
//		data.time = "2018-08-09 01:02:03";
//		
//		MessageData data1 = new MessageData();
//		data1.title = "小红花消息";
//		data1.content = "收到一朵回答正确小红花,发现自己学习上的不足，克服困难，改进学习方法，再接再厉，继续加油";
//		data1.time = "2018-07-09 01:02:03";
//				
//		mMessageData = new ArrayList<MessageData>();
//		mMessageData.add(data);
//		mMessageData.add(data1);
//		boolean hasNewMessage = MessageDataHelper.hasNewMessage(mMessageData);
//		setMessageItem(hasNewMessage);
	}
	
	private void setMessageItem(boolean hasNewMessage){
		if( getView() == null ){
			return;
		}
		LinearLayout contentLayout = (LinearLayout) (getView().findViewById(R.id.layout_content));
		SettingItem item = (SettingItem) (contentLayout.getChildAt(1));
		item.showNewMark(hasNewMessage);		
	}

	@Override  
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		this.initTitleBar(R.string.setting);
		
		if (isSmartCampus) {
			RelativeLayout titlebarLayout=(RelativeLayout) getView().findViewById(R.id.titlebar);
			titlebarLayout.setBackground(mDrawable);
		}
		LinearLayout contentLayout = (LinearLayout) (getView().findViewById(R.id.layout_content));
		
		if( mContentBg > 0 ){
			contentLayout.setBackgroundResource(mContentBg);
		}
		mItemNames.add(R.string.userinfo);
		mItemIcons.add(R.drawable.ic_setting_user);
		mItemNames.add(R.string.menu_message_text);
		mItemIcons.add(R.drawable.menu_message);	
		if( SmartCampusApplication.mFamilyVersion == true ){

		}else{
			if( InfoReleaseApplication.authenobjData != null && 0 != InfoReleaseApplication.authenobjData.usermanage_privilege ){
				mItemNames.add(R.string.userlistmanager);
				mItemIcons.add(R.drawable.ic_setting_usermanager);
			}
			//关闭分组管理功能
//			mItemNames.add(R.string.groupmanager);
//			mItemIcons.add(R.drawable.ic_setting_groupmanager);
			mItemNames.add(R.string.menu_parentapp);
			mItemIcons.add(R.drawable.menu_parentapp);
			mItemNames.add(R.string.menu_remotecontrol);
			mItemIcons.add(R.drawable.ic_setting_bluetooth);
			mItemNames.add(R.string.menu_clearcache);
			mItemIcons.add(R.drawable.ic_setting_clearcache);
			mItemNames.add(R.string.menu_change_group);
			mItemIcons.add(R.drawable.menu_change_group);
			
		}
		
		mItemNames.add(R.string.menu_exit_login);
		mItemIcons.add(R.drawable.menu_exit_login);
		
		mItemNames.add(R.string.about);
		mItemIcons.add(R.drawable.ic_setting_about);
		
			
		for( int i = 0; i < mItemNames.size(); i++ ){
			SettingItem item = new SettingItem(this.getContext());
			item.setName(mItemNames.get(i));
			item.setMoreClicked(true);
			item.setInfoImg(mItemIcons.get(i));
			item.setTag(mItemNames.get(i));
			item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Integer tag = (Integer) v.getTag();
					if( tag == R.string.userinfo ){
						if( SmartCampusApplication.mFamilyVersion == false ){
							Intent intent = new Intent();		
							intent.setClass(MySettingFragment.this.getActivity(), TeacherUserAdminActivity.class);
							startActivity(intent);		
						}else{
							Intent intent = new Intent();		
							intent.setClass(MySettingFragment.this.getActivity(), ParentUserAdminActivity.class);
							startActivity(intent);		
						}
					}else if( tag == R.string.userlistmanager ){
						Intent intent = new Intent();				
						intent.setClass(MySettingFragment.this.getActivity(), UserListActivity.class);
						startActivity(intent);
					}else if( tag == R.string.groupmanager ){
						Intent intent = new Intent();				
						intent.setClass(MySettingFragment.this.getActivity(), GroupActivity.class);
						startActivity(intent);
					}else if( tag == R.string.menu_bluetooth_text ){
						Intent intent = new Intent();
						intent.putExtra("isLib", true);
						intent.setComponent(new ComponentName(getActivity().getPackageName(),"com.routon.remotecontrol.MainActivity"));
						startActivity(intent);
					}else if( tag == R.string.about ){
						showAboutDialog();
					}else if( tag == R.string.menu_qrcode ){
						getClassListData();
					}else if( tag == R.string.menu_exit_login ){
						showQuitDialog();
					}else if( tag == R.string.menu_child_manage ){					
						Intent intent = new Intent(getActivity(), ChildListActivity.class);
						startActivity(intent);
					}else if( tag == R.string.menu_clearcache ){
						new AlertDialog.Builder(getOwnActivity()).setMessage("清理缓存后，部分图片需要重新从网络获取，是否继续？")
						.setPositiveButton("清理", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						}).setNegativeButton("取消",null).show();
					}else if( tag == R.string.menu_remotecontrol ){
						Intent intent = new Intent();
						intent.putExtra("isLib", true);
						intent.setComponent(new ComponentName(getActivity().getPackageName(),"com.routon.remotecontrol.MainActivity"));
						startActivity(intent);
					}else if(tag == R.string.menu_change_group){
						startCamera();
					}else if( tag == R.string.change_rule ){//角色选择
						UserHelper.showChangeRuleDialog(getOwnActivity());
					}else if( tag == R.string.menu_message_text){//消息中心						
						Intent intent = new Intent(getActivity(), MessageActivity.class);
						intent.putExtra(MessageActivity.MESSAGE_DATA_TAG, mMessageData);
						startActivity(intent);
						setMessageItem(false);
					}else if( tag == R.string.menu_parentapp ){//家长版APP页面
						Intent intent = new Intent(getActivity(), ParentAppActivity.class);
						startActivity(intent);
					}
					
				}
			});		
			contentLayout.addView(item);
		}
	}
	
	private void showQuitDialog(){		
		new AlertDialog.Builder(getOwnActivity()).setMessage("退出到登录界面，是否继续？")
		.setPositiveButton("退出登录", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				UserInfoData data = new UserInfoData(getOwnActivity());
				data.setAutoCheck(false);
				InfoReleaseApplication.returnToLogin(getOwnActivity(), false);
			}
		}).setNegativeButton("取消", null).show();
	}
	
	private void getClassListData() {  	 
    	showProgressDialog();
    	GroupListData.getClassListData(getOwnActivity(), new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub		 
				 
                if( classGroups.size() == 1 ){
                	//获取当前班级
                	Intent intent = new Intent();
					intent.setClass(getOwnActivity(), CreateQRImageActivity.class);
					intent.putExtra(MyBundleName.CLASS_NAME,classGroups.get(0).getName());
					intent.putExtra(MyBundleName.CLASS_ID,classGroups.get(0).getId());
					
					startActivity(intent);	
                }else if( classGroups.size() > 1 ){
                	Intent intent = new Intent();
					intent.setClass(getOwnActivity(), ClassListActivity.class);
					int[] classIds = new int[classGroups.size()];
					String[] classNames = new String[classGroups.size()];
					for(int i = 0;i < classGroups.size(); i++){
						classIds[i] = classGroups.get(i).getId();
						classNames[i] = classGroups.get(i).getName();
					}
					intent.putExtra(MyBundleName.CLASS_IDS, classIds);
					intent.putExtra(MyBundleName.CLASS_NAMES, classNames);
					
					startActivity(intent);	
                }else{
                	Toast.makeText(getOwnActivity(), "班级列表为空", Toast.LENGTH_SHORT).show();
                }
                hideProgressDialog(); 
			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
		});  
	}
	
	public void startCamera() {
		if (Build.VERSION.SDK_INT >= 23) {
			int hasPermission = ActivityCompat.checkSelfPermission(getContext(),
					Manifest.permission.CAMERA);
			if (hasPermission == PackageManager.PERMISSION_GRANTED) {
				startCameraActivity();
				return;
			}

			ActivityCompat.requestPermissions(getActivity(),
					new String[] { android.Manifest.permission.CAMERA },200);
		} else {
			startCameraActivity();
		}
	}

	
	public void startCameraActivity(){
		Intent intent = new Intent(getContext(), ScreenGroupChangeActivity.class);
		startActivityForResult(intent, 0);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:
			if(resultCode == Activity.RESULT_OK){
				String tid = data.getStringExtra(ScreenGroupChangeActivity.INTENT_TID_DATA);
				if(!TextUtils.isEmpty(tid)){
					Log.d(TAG, "tid:"+tid);
					Intent intent = new Intent(getContext(), ScreenGroupSelectActivity.class);
					intent.putExtra("tid", tid);
					startActivity(intent);
				}
			}
			break;

		default:
			break;
		}
	}

	private boolean isSmartCampus=false;


	private Drawable mDrawable;
	private int mContentBg = 0;
	
	public void setTitlebarBackground(Drawable drawable){
		this.mDrawable=drawable;
		isSmartCampus = true;
	}
	
	public void setContentBackground(int res){
		mContentBg = res;
	}
}
