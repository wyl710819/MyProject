package com.routon.smartcampus.attendance;


import org.json.JSONObject;

public class AttendanceRecordBean {

	public String day;
	public int lesson;
	public String course;
	
	
	AttendanceRecordBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		day=obj.optString("day");
		lesson=obj.optInt("lesson");
		course=obj.optString("course");
		
	}
	
	public AttendanceRecordBean(){
		
	}
}
