package com.routon.smartcampus.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClassExamBean {

	public int examId;
	public String examName;
	public String examType;
	public String examTime;
	public List<ClassAllGradeBean> classAllGrades = new ArrayList<ClassAllGradeBean>();
	public ArrayList<String> examAllCourses = new ArrayList<String>();
	
	public ClassExamBean(JSONObject jsonObject)
	{
		examId = jsonObject.optInt("examId");
		examName = jsonObject.optString("examName");
		examType = jsonObject.optString("examType");
		examTime = jsonObject.optString("examTime");
		JSONArray jsonArray = jsonObject.optJSONArray("classAllGrades");
		for(int i = 0;i<jsonArray.length();i++)
		{
			JSONObject jObject = jsonArray.optJSONObject(i);
			ClassAllGradeBean classAllGradeBean = new ClassAllGradeBean(jObject);
			classAllGrades.add(classAllGradeBean);
		}
		JSONArray array = jsonObject.optJSONArray("examAllCourses");
		for(int i=0;i<array.length();i++)
		{
			JSONObject object = array.optJSONObject(i);
			examAllCourses.add(object.optString("course"));
		}
	}
}
