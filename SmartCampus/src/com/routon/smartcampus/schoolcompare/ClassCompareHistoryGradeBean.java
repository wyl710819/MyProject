package com.routon.smartcampus.schoolcompare;

import org.json.JSONObject;

public class ClassCompareHistoryGradeBean {
	
	public String startTime;
	public String endTime;
	public int score;
	public int place;
	public int isFlag;
	
	public ClassCompareHistoryGradeBean(JSONObject jsonObject){
		startTime=jsonObject.optString("");
		endTime=jsonObject.optString("");
		score=jsonObject.optInt("");
		place=jsonObject.optInt("");
		isFlag=jsonObject.optInt("");
		
	}

	public ClassCompareHistoryGradeBean(String s,String e,int c,int p) {
		startTime=s;
		endTime=e;
		score=c;
		place=p;
	}

}
