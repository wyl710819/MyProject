package com.routon.inforelease;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.routon.common.BaseFragment;
import com.routon.inforelease.groupmanager.GroupActivity;
import com.routon.inforelease.usercontrol.UserListActivity;
import com.routon.inforelease.widget.SettingItem;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 设置界面
 * @author xiaolp
 *
 */
public class SettingFragment extends BaseFragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setting, container, false);
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
//		this.hideProgressDialog();
	}
	
//	private void clearcache(){
//		DataCleanManager.cleanInternalCache(SettingFragment.this.getActivity());
//		DataCleanManager.cleanDatabases(SettingFragment.this.getActivity());
//		DataCleanManager.cleanCustomCache(PictureAddActivity.getExternalStorageDirectory());
//	}
	
	private int[] mItemNames = {
			R.string.userinfo,
			R.string.userlistmanager,
			R.string.groupmanager,
			R.string.menu_bluetooth_text,
			R.string.about,
	};

	
	private int[] mItemIcons = {
			R.drawable.ic_setting_user,
			R.drawable.ic_setting_usermanager,
			R.drawable.ic_setting_groupmanager,
			R.drawable.ic_setting_bluetooth,
			R.drawable.ic_setting_about,
	};



	public void setNotShowBluetooth(){
		mItemNames[3] = 0;
	}
	
	public void setNotShowGroupManager(){
		mItemNames[2] = 0;
	}
	
	public void setNotShowUserManager(){
		mItemNames[1] = 0;
	}
	
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
		if( 0 == InfoReleaseApplication.authenobjData.usermanage_privilege ){
			mItemNames[1] = 0;
		}
		for( int i = 0; i < mItemNames.length; i++ ){
			SettingItem item = new SettingItem(this.getContext());
			if( mItemNames[i] == 0 ) continue;
			item.setName(mItemNames[i]);
			item.setMoreClicked(true);
			item.setInfoImg(mItemIcons[i]);
			item.setTag(mItemNames[i]);
			item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Integer tag = (Integer) v.getTag();
					if( tag == R.string.userinfo ){
						Intent intent = new Intent();		
						intent.setClass(SettingFragment.this.getActivity(), UserAdminActivity.class);
						startActivity(intent);					
					}else if( tag == R.string.userlistmanager ){
						Intent intent = new Intent();				
						intent.setClass(SettingFragment.this.getActivity(), UserListActivity.class);
						startActivity(intent);
					}else if( tag == R.string.groupmanager ){
						Intent intent = new Intent();				
						intent.setClass(SettingFragment.this.getActivity(), GroupActivity.class);
						startActivity(intent);
					}else if( tag == R.string.menu_bluetooth_text ){
						Intent intent = new Intent();
						intent.putExtra("isLib", true);
						intent.setComponent(new ComponentName(getActivity().getPackageName(),"com.routon.remotecontrol.MainActivity"));
						startActivity(intent);
					}else if( tag == R.string.about ){
						showAboutDialog();
					}
					
				}
			});		
			contentLayout.addView(item);
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
