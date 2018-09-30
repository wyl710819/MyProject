package com.routon.smartcampus.homework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FamilyHomeworkBean implements Serializable {

	private static final long serialVersionUID = -3280089283100519797L;
	public int hId;
	public int rate;
	public String remark;
	public String description;
	public String courseName;
	public String teacherName;
	public boolean isCheck;
	public ArrayList<String> homeworkImgUrls = new ArrayList<String>();
	public ArrayList<String> homeworkAudioUrls = new ArrayList<String>();
	public List<FeedbackHomeworkFileBean> homeworkFileList = new ArrayList<FeedbackHomeworkFileBean>();
	public List<FeedbackHomeworkFileBean> correctResList = new ArrayList<FeedbackHomeworkFileBean>();
	public List<FeedbackHomeworkFileBean> checkResList = new ArrayList<FeedbackHomeworkFileBean>();
	public String teacherImageUrl;
	public String rateStr="";
	public String parent_remark;
	public String checkTime="";

	public FamilyHomeworkBean(JSONObject obj) {
		if (obj == null) {
			return;
		}
		hId = obj.optInt("hId");
		rate = obj.optInt("rate");
		rateStr = obj.optString("rateStr");
		courseName = obj.optString("courseName");
		remark = obj.optString("remark");
		description = obj.optString("description");
		String checkStr = obj.optString("isCheck");
		isCheck=checkStr.equals("已检查");
		teacherName = obj.optString("teacherName");
		teacherImageUrl = obj.optString("teacherImageUrl");
		
		parent_remark=obj.optString("parent_remark");
		
		
		JSONArray jsonArray;
		JSONArray correctArray;
		JSONArray checkArray;
		try {
			jsonArray = obj.getJSONArray("fileUrl");
			if (jsonArray != null) {
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					FeedbackHomeworkFileBean bean=new FeedbackHomeworkFileBean(jsonObject);
					homeworkFileList.add(bean);
					
					
					if (jsonObject != null) {
						String imgUrl = (String) jsonObject.opt("url");
						int type = (int) jsonObject.opt("type");
						if (type==172) {
							homeworkAudioUrls.add(imgUrl);
						}else {
							homeworkImgUrls.add(imgUrl);
						}
						
					}
				}
			}
			
			correctArray = obj.getJSONArray("correctRes");
			if (correctArray != null) {
				for (int i = 0; i < correctArray.length(); i++) {
					JSONObject jsonObject = (JSONObject) correctArray.get(i);
					FeedbackHomeworkFileBean bean=new FeedbackHomeworkFileBean(jsonObject);
					correctResList.add(bean);
					
				}
			}
			
			checkArray = obj.getJSONArray("checkRes");
			if (checkArray != null) {
				for (int i = 0; i < checkArray.length(); i++) {
					JSONObject jsonObject = (JSONObject) checkArray.get(i);
					FeedbackHomeworkFileBean bean=new FeedbackHomeworkFileBean(jsonObject);
					checkResList.add(bean);
					
				}
			}
			
			
			
			
			
			
			
			
			
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public FamilyHomeworkBean() {
	}
}
