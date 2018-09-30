package com.routon.smartcampus.homework;

import java.io.Serializable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class NewAddHomeWorkBean implements Serializable{
	public String description;
	public String courseName;
	public Long teacherId;
	public static  String[] imgList;
	public NewAddHomeWorkBean(JSONObject obj) {
		if( obj == null ){
			return;
		}
		
		description=obj.optString("description");
		courseName=obj.optString("courseName");
		teacherId=obj.optLong("teacherId");

		
	}
	NewAddHomeWorkBean(){
		
	}
}
