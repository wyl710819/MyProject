package com.routon.smartcampus.selectcourse;

import java.io.Serializable;

import org.json.JSONObject;

public class StudentSelectBean implements Serializable{
	String courseId;		//课程ID
	String courseName; 	//课程名称
	String entityId;		//记录ID
	String entityName;	//学生姓名
	String entityUserId;	//学生ID
	String subjectId;		//学科ID
	String subjectName;	//学科名称
	public StudentSelectBean (JSONObject object){
		courseId=object.optString("courseId");
		courseName=object.optString("courseName");
		entityId=object.optString("entityId");
		entityName=object.optString("entityName");
		entityUserId=object.optString("entityUserId");
		subjectId=object.optString("subjectId");
		subjectName = object.optString("subjectName");
	}
}
