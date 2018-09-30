package com.routon.smartcampus.homework;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class QueryClassHomeworkBean {


	public String classId;
	public String className;
	public String gradeId;
	public int hId=0;
	public String description;
	public List<String> imgClassList=new ArrayList<String>();
	public ArrayList<HomeworkResourse> resourseList=new ArrayList<HomeworkResourse>();
	/**
	 * 只显示一条作业数据
	 * 
	 * */
	QueryClassHomeworkBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		classId=obj.optString("classId");
		className=obj.optString("className");
		JSONArray fileObj=obj.optJSONArray("homeworkList");
		if(fileObj!=null){
			JSONObject homeObj = fileObj.optJSONObject(0);
			description=homeObj.optString("description");
			hId=homeObj.optInt("hId");
			
			JSONArray imgFile=homeObj.optJSONArray("fileUrl");
			if(imgClassList.size()>0){
				imgClassList.clear();
			}
			
			for (int i = 0; i < imgFile.length(); i++) {
				if(imgFile!=null){
					JSONObject fileUrl = imgFile.optJSONObject(i);
					HomeworkResourse resourse=new HomeworkResourse(imgFile.optJSONObject(i));
					if(resourse!=null){
						resourseList.add(resourse);
					}
					imgClassList.add(fileUrl.optString("url"));
				}
				//Log.d("XXX",imgClassList.size()+"tupian");
				
			}
			
		}
		
	}
	QueryClassHomeworkBean(){
		
	}
	public static class HomeworkResourse{
		public int type=0;
		public String fileIdparams;
		public String fileUrl;
		public HomeworkResourse(JSONObject obj){
			if(obj==null){
				return;
			}
			type=obj.optInt("type");
			fileIdparams=obj.optString("fileIdparams");
			fileUrl=obj.optString("url");
		}
	}
}
