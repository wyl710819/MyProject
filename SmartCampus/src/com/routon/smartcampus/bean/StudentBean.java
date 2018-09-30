package com.routon.smartcampus.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.routon.inforelease.util.TimeUtils;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.homework.FeedbackHomeworkFileBean;

public class StudentBean implements  Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -548475039444430685L;
	public int sid;
	public String empName;
	public String birthday;
	public String school;
	public String grade;
	public String staffGroup;
	public long groupId;
//	public long groupIds;
	public String studentCode;
	public String staffCode;
	public long parentPhone;
	public String lastLoginTime;
	public int status;
	
	public String imageLastUpdateTime;
	
	//作业
	public int rate=-1;
	public int homeworkRate=0;
	public String rateStr;
	public boolean isStaffUserAgent = false;
	public String remark="";
	public boolean isCheck=false;
	public boolean isFeedback=false;
	public String checkTime="";
	public String parentRemark="";
	public List<FeedbackHomeworkFileBean> correctHomeworkPicList=new ArrayList<FeedbackHomeworkFileBean>();//批改作业图片数据
	public List<FeedbackHomeworkFileBean> correctHomeworkAudioList=new ArrayList<FeedbackHomeworkFileBean>();//批改作业录音数据
	public List<String> correctHomeworkMaterialIds=new ArrayList<String>();
	public List<String> checkHomeworkMaterialIds=new ArrayList<String>();//
	public List<FeedbackHomeworkFileBean> correctResList=new ArrayList<FeedbackHomeworkFileBean>();
	public List<FeedbackHomeworkFileBean> checkResList=new ArrayList<FeedbackHomeworkFileBean>();
	
	//考勤
	public int absenceCount=0;
	public int absenceTaxis=0;
	
	//图片保存路径
	public String imgSavePath;
	public String imgUrl;
	
	public int badgeCount;
	public int unExchangeCount;
	public int bonuspoints;//积分总数
	public int availableBonusPoints;//可用积分
	public int subclassPoint=0;//某种小红花积分
	//排行
	public int ranking;
	
	public int attd;//出勤数
	
	public String ctrlId;
	
	public String battery="1";
	
	//add by sj, for multi selector
	public boolean isSelect = false;
	public boolean isClick=false;
	public int currentIndex = -1;
	//end add
	
	public StudentBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		sid = obj.optInt("sid");
		empName = obj.optString("empName").trim();
		birthday = obj.optString("birthday");
		school = obj.optString("school");
		grade = obj.optString("grade");
		staffGroup = obj.optString("staffGroup");
		groupId = obj.optLong("groupIds");
//		groupIds=obj.optLong("groupIds");
		studentCode = obj.optString("studentCode");
		staffCode = obj.optString("staffCode");
		parentPhone = obj.optLong("parentPhone");
		status = obj.optInt("status");
		imageLastUpdateTime = obj.optString("imageLastUpdateTime");
		imgUrl = obj.optString("imgUrl");
		badgeCount = obj.optInt("badgeCount");
		unExchangeCount = obj.optInt("unExchangeCount");	
		
		//add by xiaolp 20171114 积分数目
		bonuspoints = obj.optInt("bonusPoints");
		//可用积分
		availableBonusPoints=obj.optInt("availableBonusPoints");
		//add by xiaolp 20171031　是否是代理人
		isStaffUserAgent = obj.optBoolean("isStaffUserAgent");
		absenceCount = obj.optInt("absenceCount");
		
		lastLoginTime = obj.optString("lastLoginTime");
		
		imgSavePath = FaceRecognizeMgr.getImageFilePath(String.valueOf(sid), 
				TimeUtils.getTime(imageLastUpdateTime, "yyyyMMddHHmmss"), grade, staffGroup);
		
		//增加数据
		attd = obj.optInt("attd");
		//学生卡mac
		ctrlId = obj.optString("ctrlId");
		//学生卡电量
		battery=obj.optString("battery");
		
	}
	
	public StudentBean(){
		
	}
	
	
}
