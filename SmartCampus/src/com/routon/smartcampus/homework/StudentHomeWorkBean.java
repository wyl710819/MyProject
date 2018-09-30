package com.routon.smartcampus.homework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class StudentHomeWorkBean {

	public int hId;
	public String description;
	public String className;
	public String assignmentTime;
	public ArrayList<String> fileUrls=new ArrayList<String>();
	
	public StudentHomeWorkBean(JSONObject obj) {
		if( obj == null ){
			return;
		}
		hId=obj.optInt("hId");
		description=obj.optString("description");
		className=obj.optString("className");
		assignmentTime=obj.optString("assignmentTime");
		
		JSONArray fileObj=obj.optJSONArray("fileUrl");
		for (int i = 0; i < fileObj.length(); i++) {
			JSONObject fileUrl = fileObj.optJSONObject(i);
			fileUrls.add(fileUrl.optString("url"));
		}
		
		
	}
}
