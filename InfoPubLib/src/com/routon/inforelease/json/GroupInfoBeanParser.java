package com.routon.inforelease.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.routon.inforelease.plan.create.GroupInfo;

public class GroupInfoBeanParser {

	public static GroupInfoBean parseGroupInfoBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseGroupInfoBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static GroupInfoBean parseGroupInfoBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		GroupInfoBean bean = new GroupInfoBean();
		bean.obj = parseGroupInfo(jsonObject.optJSONObject("obj"));
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		return bean;
	}

	public static GroupInfo parseGroupInfo(JSONObject jsonObject){	
		if (jsonObject == null)
			return null;
		GroupInfo info = new GroupInfo();
		info.setPid(jsonObject.optInt("pid"));
		info.setId(jsonObject.optInt("id"));
		info.setName(jsonObject.optString("name"));
		return info;
		
	}
}
