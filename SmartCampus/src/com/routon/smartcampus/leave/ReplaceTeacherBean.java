package com.routon.smartcampus.leave;

import org.json.JSONObject;

public class ReplaceTeacherBean {
	
	public String teacherClass="";
	public String teacherName;
	public int teacherType;
	public int userid;
	
	public ReplaceTeacherBean(){
		
	}
	
	public ReplaceTeacherBean(String _teacherClass,String _teacherName,int _teacherType){
		teacherClass=_teacherClass;
		teacherName=_teacherName;
		teacherType=_teacherType;
	}
	public ReplaceTeacherBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		
//		teacherClass = obj.optString("teacherClass");
		teacherName = obj.optString("TeacherName");
		userid = obj.optInt("userid");
		teacherType = obj.optInt("status");
	}

}
