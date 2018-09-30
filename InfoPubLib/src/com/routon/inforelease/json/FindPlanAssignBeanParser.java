package com.routon.inforelease.json;

import org.json.JSONException;
import org.json.JSONObject;

public class FindPlanAssignBeanParser {

	public static FindPlanAssignBean parseFindPlanAssignBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseFindPlanAssignBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static FindPlanAssignBean parseFindPlanAssignBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		FindPlanAssignBean bean = new FindPlanAssignBean();
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		bean.groups = jsonObject.optString("groups");

		return bean;
	}
}
