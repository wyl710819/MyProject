package com.routon.smartcampus.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

//班级学期出勤数据
public class ClassAttendanceBean {
	public int total = 0;
	public ArrayList<StudentBean> staffs;

	
	//解析班级学期出勤数据
	public static ClassAttendanceBean parseClassAttendanceBean(JSONObject jsonObject) {
		if( jsonObject == null ) return null;
		ClassAttendanceBean bean = new ClassAttendanceBean();
		bean.total = jsonObject.optInt("total");
		JSONArray jsonArray = jsonObject.optJSONArray("staffs");
		if( jsonArray != null ){
			bean.staffs = new ArrayList<StudentBean>();
			for( int i = 0; i < jsonArray.length(); i++ ){
				StudentBean studentBean = new StudentBean(jsonArray.optJSONObject(i));
				if( studentBean != null ){
					bean.staffs.add(studentBean);
				}
			}
		}
		return bean;
	}
}
