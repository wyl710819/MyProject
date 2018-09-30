package com.routon.smartcampus.homework;

import java.io.Serializable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeworkBean implements Serializable{
	public String description;
	public String courseName;
	public long teacherId;
	public int hId;
	public String className;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public long getTeacherId() {
		return teacherId;
	}
	public void setTeacherId(long teacherId) {
		this.teacherId = teacherId;
	}
	public int gethId() {
		return hId;
	}
	public void sethId(int hId) {
		this.hId = hId;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	

}
