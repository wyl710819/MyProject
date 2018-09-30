package com.routon.smartcampus.attendance;

import java.util.ArrayList;

import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.user.UserInfoData;

public class AttendanceApplication extends InfoReleaseApplication{
	public static ArrayList<AttendanceBean> mStudentDatas;
	
	public static int getSelIndex(UserInfoData data){
		String tearcherPortrait = data.getParentProtrait();
		int selIndex = 0;
		if( tearcherPortrait != null && tearcherPortrait.isEmpty() == false ){
			int index = 0;						
			for( AttendanceBean bean:mStudentDatas){
				if( String.valueOf(bean.sid).equals(tearcherPortrait) ){
					selIndex = index;
					return selIndex;
				}
				index++;
			}
		}
		return 0;
	}
	
	public static ClassStudentData[] mAllStudentDataList;
//	public static int mSelIndex = 0;

}
