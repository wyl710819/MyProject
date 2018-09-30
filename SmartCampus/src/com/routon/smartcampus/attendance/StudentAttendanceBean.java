package com.routon.smartcampus.attendance;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;


//学生考勤查询数据
public class StudentAttendanceBean {
	public ArrayList<AttendanceBean> studentlist;

	// 解析班级学期出勤数据
	public static StudentAttendanceBean parseStudentAttendanceBean(JSONObject jsonObject) {
		if (jsonObject == null)
			return null;
		StudentAttendanceBean bean = new StudentAttendanceBean();
		JSONArray jsonArray = jsonObject.optJSONArray("absence");
		if (jsonArray != null) {
			bean.studentlist = new ArrayList<AttendanceBean>();
			for (int i = 0; i < jsonArray.length(); i++) {
				AttendanceBean student = new AttendanceBean(jsonArray.optJSONObject(i));
				if (student != null) {
					bean.studentlist.add(student);
				}
			}
		}
		return bean;
	}

}
