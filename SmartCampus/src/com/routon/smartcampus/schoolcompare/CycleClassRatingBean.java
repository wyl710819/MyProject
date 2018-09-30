package com.routon.smartcampus.schoolcompare;

import org.json.JSONObject;

public class CycleClassRatingBean {

	public int groupId;
	public String groupName="";
	public int rank;
	public double totalScore;
	
	public CycleClassRatingBean(JSONObject object){
		groupId=object.optInt("groupId");
		groupName=object.optString("groupName");
		rank=object.optInt("rank");
		totalScore=object.optDouble("totalScore");
		
    }
}
