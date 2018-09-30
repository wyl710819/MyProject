package com.routon.smartcampus.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClassCourseExamBean {
	
	public int examId;
	public String examName;
	public String examType;
	public String examTime;
	public List<ClassCourseGradeBean> classCourseGrades = new ArrayList<ClassCourseGradeBean>();
	
	public ClassCourseExamBean(JSONObject jsonObject)
	{
		examId = jsonObject.optInt("examId");
		examName = jsonObject.optString("examName");
		examType = jsonObject.optString("examType");
		examTime = jsonObject.optString("examTime");
		JSONArray jsonArray = jsonObject.optJSONArray("classCourseGrades");
		for(int i = 0;i<jsonArray.length();i++)
		{
			JSONObject jObject = jsonArray.optJSONObject(i);
			ClassCourseGradeBean classCourseGradeBean = new ClassCourseGradeBean(jObject);
			classCourseGrades.add(classCourseGradeBean);
		}
	}

}
