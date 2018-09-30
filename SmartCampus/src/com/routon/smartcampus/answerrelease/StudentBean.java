package com.routon.smartcampus.answerrelease;

import java.io.Serializable;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.routon.inforelease.util.TimeUtils;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.homework.QueryGradeHomeworkBean;

public class StudentBean implements  Parcelable{
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
	//答题
	public String name;
//	public String imgUrl;
	public String sids;
//	public String imgSavePath;
	public String result=null;
	public String mac;
	public String tim="0";
	public String ctrlId;
	public String answerTime="";
	public int answerStatus;
	//作业
	public int rate=-1;
	public int homeworkRate=0;
	public boolean isStaffUserAgent = false;
	public String remark="";
	public boolean isCheck=false;
	
	//考勤
	public int absenceCount=0;
	public int absenceTaxis=0;
	public int attenceType=0;//默认0缺勤
	
	//图片保存路径
	public String imgSavePath;
	public String imgUrl;
	
	public int badgeCount;
	public int unExchangeCount;
	public int bonuspoints;//积分总数
	//排行
	public int ranking;
	
	public int attd;//出勤数
	
	public StudentBean(JSONObject obj){
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
		isStaffUserAgent = obj.optBoolean("isStaffUserAgent");
		absenceCount = obj.optInt("absenceCount");
		mac=obj.optString("ctrlId");
		lastLoginTime = obj.optString("lastLoginTime");
		
		imgSavePath = FaceRecognizeMgr.getImageFilePath(String.valueOf(sid), 
				TimeUtils.getTime(imageLastUpdateTime, "yyyyMMddHHmmss"), grade, staffGroup);
		
		//增加数据
		attd = obj.optInt("attd");
	}
	
	public String getTim() {
		return tim;
	}



	public void setTim(String tim) {
		this.tim = tim;
	}
	//答题
	public StudentBean(String _sids,String _name,String _imgUrl,String _imgSavePath, String _mac){
		name=_name;
		imgUrl=_imgUrl;
		sids=_sids;
		imgSavePath=_imgSavePath;
		mac=_mac;
	}
	
	public StudentBean(){
		
	}

	StudentBean(Parcel par){
		this.empName=par.readString();
		this.imgSavePath=par.readString();
		this.mac=par.readString();
		this.sid=par.readInt();
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
		dest.writeString(mac);
		dest.writeInt(sid);
	}
	
	public static final Creator<StudentBean> CREATOR = new Creator<StudentBean>() {  
		 @Override  
	        public StudentBean createFromParcel(Parcel par) {
        	
	        	return new StudentBean(par);      
	            
	        }  
	  
	        @Override  
	        public StudentBean[] newArray(int size) {  
	            return new StudentBean[size];  
	        }  
	};
	
	
}
