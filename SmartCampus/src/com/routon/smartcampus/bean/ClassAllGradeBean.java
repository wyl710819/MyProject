package com.routon.smartcampus.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


public class ClassAllGradeBean {
	public int sid;
	public String name;
	public int totalGrades;
	public List<StudentGradeBean> studentGrades = new ArrayList<StudentGradeBean>();
	public int rank;
	
	public ClassAllGradeBean()
	{
		
	}
	
	public ClassAllGradeBean(JSONObject jsonObject)
	{
		sid = jsonObject.optInt("sid");
		name = jsonObject.optString("name");
		totalGrades = jsonObject.optInt("totalGrades");
		studentGrades = studentGradesParser(jsonObject.optJSONArray("studentGrades"));
		rank = jsonObject.optInt("rank");
	}
	
	public static List<StudentGradeBean> studentGradesParser(JSONArray jsonArray)
	{
		List<StudentGradeBean> studentGrades = new ArrayList<StudentGradeBean>();
		for(int i = 0;i<jsonArray.length();i++)
		{
			JSONObject jsonObject = jsonArray.optJSONObject(i);
			StudentGradeBean studentGrade = new ClassAllGradeBean().new StudentGradeBean();
			studentGrade.course = jsonObject.optString("course");
			studentGrade.grades = jsonObject.optInt("grades");
			studentGrades.add(studentGrade);
		}
		return studentGrades;
	}
	
	
	public class StudentGradeBean
	{
		public String course;
		public int grades;
	}
}
