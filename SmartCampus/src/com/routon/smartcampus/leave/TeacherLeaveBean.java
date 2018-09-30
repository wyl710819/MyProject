package com.routon.smartcampus.leave;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.routon.inforelease.util.TimeUtils;

public class TeacherLeaveBean implements Serializable{

	private static final long serialVersionUID = -8165938717875610830L;
	
	
	public String startTime;
	public String endTime;
	public List<String> agentTeachers;
	public int id;
	public String reason;
	public int catalog;
	public String teacherName;
	public String teacherId;
	public int status;
	private SimpleDateFormat mShowSdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm);
	public TeacherLeaveBean(){}
	
	public TeacherLeaveBean(String _startTime,String _endTime,List<String> _agentTeachers){
		startTime=_startTime;
		endTime=_endTime;
		agentTeachers=_agentTeachers;
	}
	
	public TeacherLeaveBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		
		startTime = obj.optString("startTime");
		Calendar startTimeC=TimeUtils.getFormatCalendar(startTime,TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		if (startTimeC != null) {
			startTime=mShowSdf.format(startTimeC.getTime());
		}
				
		endTime = obj.optString("endTime");
		Calendar endTimeC=TimeUtils.getFormatCalendar(endTime,TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		if (endTimeC != null) {
			endTime=mShowSdf.format(endTimeC.getTime());
		}
		id = obj.optInt("id");
		reason=obj.optString("reason");
		catalog=obj.optInt("catalog");
		teacherName=obj.optString("teachername");
		teacherId=obj.optString("teacherId");
		status=obj.optInt("status");
		agentTeachers=new ArrayList<String>();
		try {
		JSONArray jsonObjects = obj.optJSONArray("lessons");
		 for (int i = 0; i < jsonObjects.length(); i++) {
			
				JSONObject jsonObject = (JSONObject) jsonObjects.get(i);
				String subTeacherName=jsonObject.optString("subTeacherName");
				agentTeachers.add(subTeacherName);

		 }
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
