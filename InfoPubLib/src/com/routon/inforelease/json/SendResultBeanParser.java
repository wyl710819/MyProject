package com.routon.inforelease.json;

import org.json.JSONException;
import org.json.JSONObject;

public class SendResultBeanParser {

	public static SendResultBean parseSendResultBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseSendResultBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static SendResultBean parseSendResultBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		SendResultBean bean = new SendResultBean();
		bean.obj = parseSendResultobjBean(jsonObject.optJSONObject("obj"));
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		return bean;
	}

	public static SendResultobjBean parseSendResultobjBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		SendResultobjBean bean = new SendResultobjBean();
		bean.exceptioncount = jsonObject.optString("exceptioncount");
		bean.sendId = jsonObject.optString("sendId");
		bean.cancelcount = jsonObject.optString("cancelcount");
		bean.sum = jsonObject.optInt("sum");
		bean.completecount = jsonObject.optString("completecount");
		return bean;
	}
}
