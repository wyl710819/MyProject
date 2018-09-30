package com.routon.smartcampus.schoolcompare;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.routon.inforelease.InfoReleaseApplication;

public class SubprojectBean implements Serializable {

	public String name;
	public int id;
	public int maxScore;
	public double score=-9999;
	public int weight;
	public int[] userIds=new int[0];
	public boolean isPermit = false;
	public boolean isGrade;
	
	public double initScore;
	public double operateStep;
	public double minScore;
	public double itemAvg = 0;
	
	public SubprojectBean(JSONObject jsonObject) {
		name=jsonObject.optString("name");
		id=jsonObject.optInt("id");
		maxScore=jsonObject.optInt("maxScore");
		weight=jsonObject.optInt("weight");
		initScore = jsonObject.optDouble("initScore");
		operateStep = jsonObject.optDouble("operateStep");
		minScore = jsonObject.optDouble("minScore");
		try {
			
			if(jsonObject.has("userIds")){
				JSONArray jsonArray=jsonObject.getJSONArray("userIds");
				if (jsonArray!=null) {
					userIds=new int[jsonArray.length()];
					for (int i = 0; i < jsonArray.length(); i++) {
						userIds[i]=(Integer) jsonArray.get(i);
					}
					
					
					int myUserId = InfoReleaseApplication.authenobjData.userId;
					for(int i=0; i<userIds.length; i++){
						if(myUserId == userIds[i]){
							isPermit = true;
							break;
						}
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if(!jsonObject.isNull("isPermit")){
			int permit = jsonObject.optInt("isPermit");
			if(permit == 1){
				isPermit = true;
			}else{
				isPermit = false;
			}		
		}
	
		itemAvg = jsonObject.optDouble("itemAvg");	
	}


	public SubprojectBean() {
	}

	public SubprojectBean(int _id, double _score) {
		id=_id;
		score=_score;
	}
	public SubprojectBean(SubprojectBean _bean) {
		name=_bean.name;
		id=_bean.id;
		maxScore=_bean.maxScore;
		weight=_bean.weight;
//		if (_bean.userIds!=null) {
			userIds=_bean.userIds;
//		}
		score=_bean.score;
		isGrade=_bean.isGrade;
		
		isPermit = _bean.isPermit;
		itemAvg = _bean.itemAvg;
	}


	public void setStrRightTv(String string) {
		// TODO Auto-generated method stub
		
	}
}
