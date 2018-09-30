package com.routon.smartcampus.selectcourse;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class SelectCourseBean implements Serializable{
	public String entityId;
	public String entityName;								//名称
	public int selectType; //1,	//选课类型：1选学科2分层3学考选考4选老师5选课程
	public String selectStartDate; //选课开始时间
	public String selectEndDate;  //”:”"2018-05-11 17:00:00.0"”,			//选课结束时间
	public int selectCount;  //”:1,								//已参与学生数
	public int typeCount;  //”:1,								//可选学科分类数
	public String selectDesc; //”:””								//选课说明
	public int selectStatus;
	
	public SelectCourseBean (JSONObject object){
		entityId=object.optString("entityId");
		entityName=object.optString("entityName");
		selectType = object.optInt("selectType");
		selectStartDate=object.optString("selectStartDate");
		selectEndDate=object.optString("selectEndDate");
		selectCount=object.optInt("selectCount");
		typeCount=object.optInt("typeCount");
		selectDesc=object.optString("selectDesc");
		selectStatus = object.optInt("selectStatus");
	}
public SelectCourseBean() {
		
	}
}
