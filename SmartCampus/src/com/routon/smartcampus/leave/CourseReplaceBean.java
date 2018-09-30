package com.routon.smartcampus.leave;

import java.io.Serializable;

import org.json.JSONObject;

public class CourseReplaceBean implements Serializable{

	private static final long serialVersionUID = -8483069755985158181L;
	
	public String courseTime; 
	public String className; 
	public String courseName; 
	public String replaceTeacherName="";
	public int subTeacherId;

	public long schoolId;
	public int lesson;
	public long groupId; 
	
    public CourseReplaceBean(String _courseTime,String _className,String _courseName,String _replaceTeacherName){
    	courseTime=_courseTime;
    	className=_className;
    	courseName=_courseName;
    	replaceTeacherName=_replaceTeacherName;
	}


	public CourseReplaceBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		
		courseName = obj.optString("lessonName");
		className = obj.optString("className");
		courseTime = obj.optString("lessonDate");
		lesson = obj.optInt("lesson");
		groupId = obj.optLong("groupId");
		schoolId = obj.optLong("schoolId");
	}

	public CourseReplaceBean(){
		
	}
	
}
