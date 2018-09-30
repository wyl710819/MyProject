package com.routon.smartcampus.bean;

import org.json.JSONObject;

import android.R.integer;

public class ClassCourseGradeBean {

	public int sid;
	public String name;
	public int grades;
	public int rank;
	
	public ClassCourseGradeBean(JSONObject jsonObject)
	{
		sid = jsonObject.optInt("sid");
		name = jsonObject.optString("name");
		grades = jsonObject.optInt("grades");
		rank = jsonObject.optInt("rank");
	}
}
