package com.routon.smartcampus.homework;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class CorrectStudentBean implements Serializable {

	private static final long serialVersionUID = -7561254166695749831L;
	public int rate=-1;
	public String remark;
	public long studentId;
	public String name;
	public String rateStr;
	public boolean isCheck;
	public String checkTime="";
	public String parentRemark="";
	public boolean isFeedback;
	public List<String> correctHomeworkMaterialIds=new ArrayList<String>();
	public List<String> checkHomeworkMaterialIds=new ArrayList<String>();
	public List<FeedbackHomeworkFileBean> correctResList=new ArrayList<FeedbackHomeworkFileBean>();
	public List<FeedbackHomeworkFileBean> checkResList=new ArrayList<FeedbackHomeworkFileBean>();

	public CorrectStudentBean(JSONObject obj) {
		if (obj == null) {
			return;
		}
		
		rate = obj.optInt("rate");
		studentId = obj.optLong("studentId");
		remark = obj.optString("remark");
		name = obj.optString("name");
		String checkStr = obj.optString("isCheck");
		isCheck=checkStr.equals("已检查");
		rateStr = obj.optString("rateStr");
		checkTime = obj.optString("parent_check_time");
		parentRemark = obj.optString("parent_remark");
		
		
		//
		JSONArray correctArray = obj.optJSONArray("correctRes");
		for (int i = 0; i < correctArray.length(); i++) {
			
			JSONObject correctResObj = (JSONObject) correctArray.optJSONObject(i);
			FeedbackHomeworkFileBean bean = new FeedbackHomeworkFileBean(correctResObj);
			correctResList.add(bean);
			correctHomeworkMaterialIds.add(bean.fileTypeId);
		}
		
		JSONArray checktArray = obj.optJSONArray("checkRes");
		if (checktArray.length() > 0) {
			isFeedback=true;
		}
		for (int i = 0; i < checktArray.length(); i++) {
			
			JSONObject checktResObj = (JSONObject) checktArray.optJSONObject(i);
			FeedbackHomeworkFileBean bean = new FeedbackHomeworkFileBean(checktResObj);
			checkResList.add(bean);
			checkHomeworkMaterialIds.add(bean.fileId);
		}


	}

	public CorrectStudentBean() {
	}
}
