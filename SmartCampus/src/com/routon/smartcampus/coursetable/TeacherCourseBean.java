package com.routon.smartcampus.coursetable;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;


public class TeacherCourseBean {
//	public String className;
//	public String courseTime;
//	public String courseName;
	
	
	
	public int week;
	public MyCourse myCourse;
	public ArrayList<MyCourse> courseList;
	public String day;
	public boolean weekEven;
	
	public TeacherCourseBean(JSONObject obj){
		if( obj == null ){
			return;
		}		
		week=obj.optInt("week");
		day = obj.optString("day");
		JSONArray jsonArray = obj.optJSONArray("courses");
		if( jsonArray != null ){
			courseList = new ArrayList<MyCourse>();
			for( int i = 0; i < jsonArray.length(); i++ ){
				MyCourse course = new MyCourse(jsonArray.optJSONObject(i));
				if( course != null ){
					courseList.add(course);
				}
			}
		}
	}
	public TeacherCourseBean(){
		
	}
	
	public static class MyCourse{
		public int lesson;
		public String course;
		public String teacherName;
		public String className;
		public int ampm;
		public String classTime;
		public String sid;
		public int absence;
		public String day;
		public MyCourse(JSONObject obj){
			if(obj==null){
				return;
			}
			lesson=obj.optInt("lesson");
			course=obj.optString("course");
			teacherName=obj.optString("teacherName");
			className = obj.optString("classname");
			ampm=obj.optInt("ampm");
			classTime=obj.optString("classTime");
			sid = obj.optString("sid");
			absence = obj.optInt("absence");
		}
	}
	
//	public TeacherCourseBean(String className,String courseTime,String courseName){
//		this.className=className;
//		this.courseTime=courseTime;
//		this.courseName=courseName;
//	}
	
//	public String toString(){
//		return "className:"+className+"courseTime:"+courseTime+"courseName:"+courseName;
//	}
}
