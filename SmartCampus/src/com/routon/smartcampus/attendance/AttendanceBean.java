package com.routon.smartcampus.attendance;

import java.io.Serializable;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.routon.inforelease.util.TimeUtils;
import com.routon.smartcampus.answerrelease.StudentBean;
import com.routon.smartcampus.face.FaceRecognizeMgr;

public class AttendanceBean implements  Parcelable{
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
	public String studentCode;
	public String staffCode;
	public long parentPhone;
	public String lastLoginTime;
	public int status;
	
	public String imageLastUpdateTime;
	
	
	//考勤
	public int absenceCount=0;
	public int absenceTaxis=0;
	public String mac;
	public int attenceType=0;
	//图片保存路径
	public String imgSavePath;
	public String imgUrl;
	
	public int badgeCount;
	public int unExchangeCount;
	public int bonuspoints;//积分总数
	//排行
	public int ranking;
	
	public int attd;//出勤数
	
	public String ctrlId;
	
	public AttendanceBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		sid = obj.optInt("sid");
		empName = obj.optString("empName");
		birthday = obj.optString("birthday");
		school = obj.optString("school");
		grade = obj.optString("grade");
		staffGroup = obj.optString("staffGroup");
		groupId = obj.optLong("groupId");
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
		//add by xiaolp 20171031　是否是代理人
		absenceCount = obj.optInt("absenceCount");
		
		lastLoginTime = obj.optString("lastLoginTime");
		
		imgSavePath = FaceRecognizeMgr.getImageFilePath(String.valueOf(sid), 
				TimeUtils.getTime(imageLastUpdateTime, "yyyyMMddHHmmss"), grade, staffGroup);
		
		//增加数据
		attd = obj.optInt("attd");
		mac=obj.optString("ctrlId");
		ctrlId = obj.optString("ctrlId");
	}
	
	public AttendanceBean(){
		
	}

	AttendanceBean(Parcel par){
		this.empName=par.readString();
		this.imgSavePath=par.readString();
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(empName);
		dest.writeString(imgSavePath);
	}
	public static final Creator<AttendanceBean> CREATOR = new Creator<AttendanceBean>() {  
		 @Override  
	        public AttendanceBean createFromParcel(Parcel par) {
       	
	        	return new AttendanceBean(par);      
	            
	        }  
	  
	        @Override  
	        public AttendanceBean[] newArray(int size) {  
	            return new AttendanceBean[size];  
	        }  
	};
	
}
