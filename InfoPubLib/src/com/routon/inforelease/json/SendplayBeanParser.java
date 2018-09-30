package com.routon.inforelease.json;

import org.json.JSONException;
import org.json.JSONObject;

public class SendplayBeanParser {

	public static SendplayBean parseSendplayBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseSendplayBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static SendplayBean parseSendplayBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		SendplayBean bean = new SendplayBean();
		bean.obj = parseSendplayobjBean(jsonObject.optJSONObject("obj"));
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		return bean;
	}

	public static SendplayobjBean parseSendplayobjBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		SendplayobjBean bean = new SendplayobjBean();
		bean.sendId = jsonObject.optString("sendId");
		return bean;
	}
}
