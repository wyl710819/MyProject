package com.routon.smartcampus.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClassCourseExamsDataBean {
	public int total;
	public int groupId;
	public int sort;
	public String school;
	public String beginTime;
	public List<ClassCourseExamBean> classExams = new ArrayList<ClassCourseExamBean>();
	public String className;
	public String endTime;
	public String courseName;
	
	public ClassCourseExamsDataBean(JSONObject jsonObject)
	{
		total = jsonObject.optInt("total");
		groupId = jsonObject.optInt("groupId");
		sort = jsonObject.optInt("sort");
		school = jsonObject.optString("school");
		beginTime = jsonObject.optString("beginTime");
		className = jsonObject.optString("className");
		endTime = jsonObject.optString("endTime");
		courseName = jsonObject.optString("courseName");
		JSONArray jsonArray = jsonObject.optJSONArray("classExams");
		for(int i = 0;i<jsonArray.length();i++)
		{
			JSONObject jObject = jsonArray.optJSONObject(i);
			ClassCourseExamBean classCourseExamBean = new ClassCourseExamBean(jObject);
			classExams.add(classCourseExamBean);
		}
	}

}
