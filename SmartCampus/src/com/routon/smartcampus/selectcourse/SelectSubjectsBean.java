package com.routon.smartcampus.selectcourse;

import java.io.Serializable;

import org.json.JSONObject;

public class SelectSubjectsBean implements Serializable{
	String subjectId;
	String subjectName;
	int subjectType;
	int courseCount;
	public SelectSubjectsBean (JSONObject object){
		subjectId=object.optString("subjectId");
		subjectName=object.optString("subjectName");
		subjectType = object.optInt("subjectType");
		courseCount = object.optInt("courseCount");
	}
}
