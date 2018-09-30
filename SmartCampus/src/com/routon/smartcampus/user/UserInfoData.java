package com.routon.smartcampus.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.routon.inforelease.InfoReleaseApplication;

public class UserInfoData {
	private SharedPreferences mSharedPrefrences = null;
	public UserInfoData(Context context){
		mSharedPrefrences = context.getSharedPreferences(InfoReleaseApplication.USERINFO, Context.MODE_PRIVATE);
	}
	public String getTeacherName(){
		return  mSharedPrefrences.getString(InfoReleaseApplication.TAG_NAME,null);
	}
	
	public void setTeacherName(String name){
		mSharedPrefrences.edit().putString(InfoReleaseApplication.TAG_NAME,name).commit();
	}
	
	public void saveMainListOrder(String mainlistorder){
		mSharedPrefrences.edit().putString("mainlistorder",mainlistorder).commit();
	}
	
	public String getMainListOrder(){
		return  mSharedPrefrences.getString("mainlistorder",null);
	}
	
	public String getMainListHideMenus(){
		return  mSharedPrefrences.getString("mainlist_hidemenus",null);
	}
	
	public void saveMainListHideMenus(String hidemenus){
		mSharedPrefrences.edit().putString("mainlist_hidemenus",hidemenus).commit();
	}
	
	public static final int UNKOWN_USER_ROLE = 0;
	public static final int TEACHER_USER_ROLE = 1;
	public static final int PARNT_USER_ROLE = 2;
	public static final int ALL_USER_ROLE = 3;
	
	public void resetUserRole(){
	}
	
	//主界面设置角色后，老师数据和家长数据可能都为空，此时返回登录界面，按照mUserRole显示登录界面，显示完成后mUserRole立即复位为-1
	private static int mUserRole = -1;
	
	public int getUserRole(){
		if( mUserRole != -1 ){
			int role = mUserRole;
			mUserRole = -1;
			return role;
		}
		String parentphone = getParentPhone();
		String teacherName = getTeacherName();
		//没有老师的帐号记录，也没有家长的帐号记录
		if( (parentphone == null || parentphone.isEmpty() == true) &&
				( teacherName == null || teacherName.isEmpty() == true )){
			return UNKOWN_USER_ROLE;
		}
		//没有家长的帐号记录，有老师的帐号记录
		if( parentphone == null || parentphone.isEmpty() == true){
			return TEACHER_USER_ROLE;
		}
		//有家长的帐号记录，没有老师的帐号记录
		if( teacherName == null || teacherName.isEmpty() == true ){
			return PARNT_USER_ROLE;
		}
		return ALL_USER_ROLE;
	}
	
//	public void setUserRole(int rule){
//		 mSharedPrefrences.edit().putInt("UserRole",rule).commit();
//	}
	
	public void setUserRole(boolean teacherEnable,boolean parentEnable){
		if( teacherEnable == false ){//清空老师的历史记录
			setTeacherName(null);
			setTeacherPwd(null);
			setTeacherPortrait(null);
		}
		if( parentEnable == false ){//清空家长的历史记录
			this.setParentPhone(null);
			this.setParentVerifyNum(null);
			this.setParentPortrait(null);
		}
		if( teacherEnable == false && parentEnable == false ){
			mUserRole  = UNKOWN_USER_ROLE;
		}else if( teacherEnable == false && parentEnable == true ){
			mUserRole = PARNT_USER_ROLE;
		}else if( teacherEnable == true && parentEnable == false ){
			mUserRole = TEACHER_USER_ROLE;
		}else if( teacherEnable == true && parentEnable == true ){
			mUserRole = ALL_USER_ROLE;
		}
	}
	
	
	public String getParentPhone(){
		return  mSharedPrefrences.getString("ParentPhone",null);
	}
	
	public void setParentPhone(String parentPhone){
		 mSharedPrefrences.edit().putString("ParentPhone",parentPhone).commit();
	}
	
	public String getParentVerifyNum(){
		return mSharedPrefrences.getString("ParentVerifyNum",null);
	}
	
	public void setParentVerifyNum(String parentVerifyNum){
		 mSharedPrefrences.edit().putString("ParentVerifyNum",parentVerifyNum).commit();
	}
	
	public String getParentProtrait(){
		return mSharedPrefrences.getString("ParentProtrait",null);
	}
	
	public void setParentPortrait(String parentPortrait){
		 mSharedPrefrences.edit().putString("ParentProtrait",parentPortrait).commit();
	}
	
	public boolean getParentFirstLogin(){
		return mSharedPrefrences.getBoolean("ParentFirstLogin_"+getParentPhone(), true);
	}
	
	public void setParentFirstLogin(boolean flag){
		mSharedPrefrences.edit().putBoolean("ParentFirstLogin_"+getParentPhone(), flag).commit();
	}
	
	public String getTeacherPwd(){
		return mSharedPrefrences.getString(InfoReleaseApplication.TAG_PWD,null);
	}
	
	public void setTeacherPwd(String pwd){
		mSharedPrefrences.edit().putString(InfoReleaseApplication.TAG_PWD,pwd).commit();
	}
	
	public String getTeacherPortrait(){
		return mSharedPrefrences.getString(InfoReleaseApplication.TAG_PORTRAIT,null);
	}
	
	public void setTeacherPortrait(String portrait){
		mSharedPrefrences.edit().putString(InfoReleaseApplication.TAG_PORTRAIT,portrait).commit();
	}
	
	
	public boolean getIsCheck(){
		return true;
//		return mSharedPrefrences.getBoolean(InfoReleaseApplication.TAG_ISCHECK, true);
	}
	
	public void setIsCheck(boolean ischeck){
		mSharedPrefrences.edit().putBoolean(InfoReleaseApplication.TAG_ISCHECK,ischeck).commit();
	}
	
	public boolean getAutoCheck(){
		return false;
//		return mSharedPrefrences.getBoolean(InfoReleaseApplication.TAG_AUTOCHECK, false);
	}
	
	public void setAutoCheck(boolean autocheck){
		mSharedPrefrences.edit().putBoolean(InfoReleaseApplication.TAG_AUTOCHECK,autocheck).commit();
	}
	
	public boolean getIsParentRule(){
		return mSharedPrefrences.getBoolean(InfoReleaseApplication.TAG_IS_PARENT, false);
	}
	
	public void setIsParentRule(boolean isParentRule){
		 mSharedPrefrences.edit().putBoolean(InfoReleaseApplication.TAG_IS_PARENT, isParentRule).commit();
	}
}
