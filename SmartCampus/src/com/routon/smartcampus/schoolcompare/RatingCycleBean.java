package com.routon.smartcampus.schoolcompare;


import org.json.JSONObject;

public class RatingCycleBean {

	public int id;
	public String startTime;
	public String finishTime;
	
	public RatingCycleBean(JSONObject object){
			id=object.optInt("id");
			startTime=object.optString("startTime");
			finishTime=object.optString("finishTime");
			
			if (startTime.contains(" ")) {
				startTime=startTime.substring(0, startTime.indexOf(" "));
			}
			
			if (finishTime.contains(" ")) {
				finishTime=finishTime.substring(0, finishTime.indexOf(" "));
			}
	}
}
