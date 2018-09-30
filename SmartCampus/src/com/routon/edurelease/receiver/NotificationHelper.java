package com.routon.edurelease.receiver;

import com.tencent.android.tpush.XGPushManager;

import android.content.Context;
import android.content.SharedPreferences;

public class NotificationHelper {
	private SharedPreferences mSharedPrefrences = null;
	
	private static String BIND_TEACHER_NAME = "bind_teacher_name";
	private static String BIND_PARENT_PHONE = "bind_parent_phone";
	private static String BIND_PARENT_GROUPS = "bind_parent_gruops";
	
	private Context mContext;
	public NotificationHelper(Context context){
		mContext = context;
		mSharedPrefrences = context.getSharedPreferences("XGPush_bind_info", Context.MODE_PRIVATE);
	}
	public String getBindTeacherName(){
		return  mSharedPrefrences.getString(BIND_TEACHER_NAME,null);
	}
	
	public void setBindTeacherName(String name){
		mSharedPrefrences.edit().putString(BIND_TEACHER_NAME,name).commit();
	}
	
	public String getBindParentPhone(){
		return  mSharedPrefrences.getString(BIND_PARENT_PHONE,null);
	}
	
	public void setBindParentPhone(String phone){
		mSharedPrefrences.edit().putString(BIND_PARENT_PHONE,phone).commit();
	}
	
	public String getBindParentGroups(){
		return  mSharedPrefrences.getString(BIND_PARENT_GROUPS,null);
	}
	
	public void setBindParentGroups(String groups){
		mSharedPrefrences.edit().putString(BIND_PARENT_GROUPS,groups).commit();
	}
	
	public void bindGroups(String groups){
		
		String oldGroups = getBindParentGroups();
		if(oldGroups != null && !groups.equals(oldGroups)){
			String[] groupArray = oldGroups.split(",");
			for(int i=0; i<groupArray.length; i++){
				XGPushManager.deleteTag(mContext, groupArray[i]);
			}
		}
		String[] groupArray = groups.split(",");
		for(int i=0; i<groupArray.length; i++){
			XGPushManager.setTag(mContext, groupArray[i]);
		}
		
		setBindParentGroups(groups);
	}
}
