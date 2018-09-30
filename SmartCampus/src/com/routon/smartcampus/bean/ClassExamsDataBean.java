package com.routon.smartcampus.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.widget.ImageView;

public class ClassExamsDataBean {
	
	public int total;
	public int groupId;
	public int sort;
	public String school;
	public String beginTime;
	public List<ClassExamBean> classExams = new ArrayList<ClassExamBean>();
	public String className;
	public String endTime;
	
	public ClassExamsDataBean(JSONObject jsonObject)
	{
		total = jsonObject.optInt("total");
		groupId = jsonObject.optInt("groupId");
		sort = jsonObject.optInt("sort");
		school = jsonObject.optString("school");
		beginTime = jsonObject.optString("beginTime");
		className = jsonObject.optString("className");
		endTime = jsonObject.optString("endTime");
		JSONArray jsonArray = jsonObject.optJSONArray("classExams");
		for(int i = 0;i<jsonArray.length();i++)
		{
			JSONObject jObject = jsonArray.optJSONObject(i);
			ClassExamBean classExamBean = new ClassExamBean(jObject);
			classExams.add(classExamBean);
		}
	}
}
