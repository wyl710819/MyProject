package com.routon.smartcampus;

import java.util.ArrayList;
import java.util.List;

import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.flower.BadgeInfo;
import com.routon.smartcampus.user.UserInfoData;

public class SmartCampusApplication extends InfoReleaseApplication{
	public static ArrayList<StudentBean> mStudentDatas;
	
	public static int getSelIndex(UserInfoData data){
		String tearcherPortrait = data.getParentProtrait();
		int selIndex = 0;
		if( tearcherPortrait != null && tearcherPortrait.isEmpty() == false ){
			int index = 0;						
			for( StudentBean bean:mStudentDatas){
				if( String.valueOf(bean.sid).equals(tearcherPortrait) ){
					selIndex = index;
					return selIndex;
				}
				index++;
			}
		}
		return 0;
	}
	
	public static String getFamilyVersionParentPhone(){
		if(mStudentDatas != null && mStudentDatas.size() > 0){
			return ""+mStudentDatas.get(0).parentPhone;
		}else{
			return "";
		}
	}
	
//	public static ClassStudentData[] mAllStudentDataList;
	
	public static String ftpUrl="";
	public static String port="";
	public static String ftpUserName="appdata";
	public static String ftpUserpwd="#@&5jEbFm2h$x&U*";
//	public static ArrayList<BadgeInfo> badgeDataList = new ArrayList<BadgeInfo>();
//	private static ArrayList<BadgeInfo> mBadgeList=new ArrayList<BadgeInfo>();

	
	@Override
	public void onCreate(){
		super.onCreate();
		LogcatHelper.getInstance(this).start(); 
	}

//	public static ArrayList<Badge> flowersList=new ArrayList<Badge>();

	public static void resetStaticDatas(){
//		if( badgeDataList != null ){
//			badgeDataList.clear();
//		}
//		if( flowersList != null ){
//			flowersList.clear();
//		}
		mStudentDatas = null;
		InfoReleaseApplication.authenobjData = null;
	}
	

}
