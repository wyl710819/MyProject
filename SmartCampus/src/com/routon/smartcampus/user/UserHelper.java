package com.routon.smartcampus.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.Log;

import com.routon.inforelease.InfoReleaseApplication;

public class UserHelper {
	public static void changeRule(final Activity activity){
		new AlertDialog.Builder(activity).setMessage("角色修改，跳转到登录界面")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						InfoReleaseApplication.returnToLogin(activity, false);
					}
				}).setCancelable(false).show();
	}
	
	public static void showChangeRuleDialog(final Activity activity){
		String[] items = new String[] { "老师", "家长"};  
		UserInfoData data = new UserInfoData(activity);
		int role = data.getUserRole();
		final boolean[] checkItems = new  boolean[]{false,false};
		final boolean[] updateCheckItems = new boolean[]{false,false};
		if( role == UserInfoData.UNKOWN_USER_ROLE ){
			checkItems[0] = false;
			checkItems[1] = false;
		}else if( role == UserInfoData.TEACHER_USER_ROLE ){
			checkItems[0] = true;
			checkItems[1] = false;
		}else if( role == UserInfoData.PARNT_USER_ROLE ){
			checkItems[0] = false;
			checkItems[1] = true;
		}else{
			checkItems[0] = true;
			checkItems[1] = true;
		}
		updateCheckItems[0] = checkItems[0];
		updateCheckItems[1] = checkItems[1];
		AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("角色选择")
                .setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						Log.d("MySetting","onClick checkItems[0]:"+checkItems[0]+",updateCheckItems[0]:"+updateCheckItems[0]);
						if( checkItems[0] != updateCheckItems[0] || checkItems[1] != updateCheckItems[1] ){
							//修改保存在内存中的角色信息
							UserInfoData data = new UserInfoData(activity);
							data.setUserRole(updateCheckItems[0],updateCheckItems[1]);
							changeRule(activity);
						}
					}
				})
                .setMultiChoiceItems(items, updateCheckItems, new OnMultiChoiceClickListener() {  
  
                    @Override  
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {  
                    }  
                }).create();  
        dialog.show();  
	}
	
}
