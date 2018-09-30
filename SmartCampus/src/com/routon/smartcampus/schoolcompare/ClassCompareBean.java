package com.routon.smartcampus.schoolcompare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClassCompareBean implements Serializable{

	
	public String groupName;
	public int groupId;
	public String ratingId;
	public double compareScore=0;
	public double compareTaxis;
	public String parentGroupName;
	public String parent;
	public boolean isHeadTeacher;
	public String ratingDate;
	public ArrayList<SubprojectBean> subprojectBeanList;
	public List<ClassCompareBean.SubprojectScoreBean> subprojectScoreBeanList;
	
	
	public ClassCompareBean(JSONObject jsonObject) {
		try {
			groupId=jsonObject.optInt("groupId");
			groupName=jsonObject.optString("groupName");
			parentGroupName=jsonObject.optString("parentGroupName");
			parent=jsonObject.optString("parent");
		JSONArray jsonArray=jsonObject.optJSONArray("items");
		if (jsonArray!=null) {
			subprojectScoreBeanList=new ArrayList<ClassCompareBean.SubprojectScoreBean>();
			for (int i = 0; i < jsonArray.length(); i++) {
				SubprojectScoreBean bean = new SubprojectScoreBean(jsonArray.getJSONObject(i));
				subprojectScoreBeanList.add(bean);
			}
		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public ClassCompareBean(){
		
	}
	
	public class SubprojectScoreBean implements Serializable{
		public int subprojectId;
		public double subprojectScore;
		public SubprojectScoreBean(JSONObject jsonObject) {
			subprojectId=jsonObject.optInt("id");
			subprojectScore=jsonObject.optDouble("score");
		}
		
	}


//	public void setSubprojectScore() {//设置子选项分数
//		if (subprojectScoreBeanList!=null && subprojectScoreBeanList.size()>0 && subprojectBeanList!=null && subprojectBeanList.size()>0) {
//				for (int i = 0; i < subprojectScoreBeanList.size(); i++) {
//					for (int j = 0; j < subprojectBeanList.size(); j++) {
//						if (subprojectScoreBeanList.get(i).subprojectId==subprojectBeanList.get(j).id) {
//							if (subprojectBeanList.get(j).score!=0) {
//								subprojectBeanList.get(j).isGrade=true;
//							}
//							subprojectBeanList.get(j).score=subprojectScoreBeanList.get(i).subprojectScore;
//							compareScore+=subprojectScoreBeanList.get(i).subprojectScore;
//						}
//					}
//				}
//		}else {
//			subprojectBeanList=new ArrayList<SubprojectBean>();
//		}
//	}
	
}
