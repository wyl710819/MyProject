package com.routon.smartcampus.schoolcompare;

import org.json.JSONObject;

public class HistoryScoreBean {
	
	public String startTime;
	public String endTime;
	public double score;
	public int place;
	public int redflagImgFileId;
	public String redflagImgUrl;
	public int isIssued=0;
	
	public HistoryScoreBean(JSONObject jsonObject){
		startTime=jsonObject.optString("startTime");
		endTime=jsonObject.optString("finishTime");
		score=jsonObject.optDouble("scores");
		place=jsonObject.optInt("ranking");
		redflagImgFileId=jsonObject.optInt("redflagImgFileId");
		redflagImgUrl=jsonObject.optString("redflagImgUrl");
		isIssued=jsonObject.optInt("isIssued");
		
		
		if (startTime.contains(" ")) {
			startTime=startTime.substring(0, startTime.indexOf(" "));
		}
		
		if (endTime.contains(" ")) {
			endTime=endTime.substring(0, endTime.indexOf(" "));
		}
		
	}

	public HistoryScoreBean(String s,String e,int c,int p) {
		startTime=s;
		endTime=e;
		score=c;
		place=p;
	}

}
