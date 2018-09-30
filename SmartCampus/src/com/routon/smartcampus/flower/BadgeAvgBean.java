package com.routon.smartcampus.flower;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BadgeAvgBean {

	public double avg_badgeCounts=0;//班级小红花平均数
	public int badgeId;//小红花id
	public List<Integer> studentBadgeWeeklyCounts=new ArrayList<Integer>();//学生小红花周数据
	public int studentBadgeCount=0;
	public String badgeName="";
	
	public BadgeAvgBean(JSONObject obj, String type) {
		if( obj == null ){
			return;
		}
		try {
		if (type.equals("is_avg")) {
			avg_badgeCounts = obj.optDouble("avg_badgeCounts");
			badgeId = obj.optInt("badgeId");
		}else if (type.equals("is_student")) {
			JSONArray jsonArray = obj.optJSONArray("badgeCounts");
			studentBadgeWeeklyCounts=new ArrayList<Integer>();
			if(jsonArray!=null && jsonArray.length()>0){
				for (int i = 0; i < jsonArray.length(); i++) {
					
				      studentBadgeWeeklyCounts.add((Integer) jsonArray.get(i));
				      studentBadgeCount+=(Integer) jsonArray.get(i);
				}
			}
			
			badgeId = obj.optInt("badgeId");
		}
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public BadgeAvgBean(){
		
	}
	
	public String toString(){
		return null;
		
	}
}
