package com.routon.inforelease.json;

import org.json.JSONException;
import org.json.JSONObject;

public class SmsCodeBeanParser {

	public static SmsCodeBean parseSmsCodeBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseSmsCodeBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static SmsCodeBean parseSmsCodeBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		SmsCodeBean bean = new SmsCodeBean();
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		bean.obj = parseSmsCodeobjBean(jsonObject.optJSONObject("obj"));
		return bean;
	}

	public static SmsCodeObjBean parseSmsCodeobjBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		SmsCodeObjBean bean = new SmsCodeObjBean();
		bean.valid = jsonObject.optInt("valid");
	

		return bean;
	}
}
