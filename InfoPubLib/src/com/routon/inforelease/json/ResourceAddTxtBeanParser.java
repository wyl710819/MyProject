package com.routon.inforelease.json;

import org.json.JSONException;
import org.json.JSONObject;

public class ResourceAddTxtBeanParser {

	public static ResourceAddTxtBean parseResourceAddTxtBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseResourceAddTxtBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ResourceAddTxtBean parseResourceAddTxtBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ResourceAddTxtBean bean = new ResourceAddTxtBean();
		bean.id = jsonObject.optInt("id");
		bean.exception = jsonObject.optString("exception");
		bean.obj = parseResourceAddTxtobjBean(jsonObject.optJSONObject("obj"));
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		return bean;
	}

	public static ResourceAddTxtobjBean parseResourceAddTxtobjBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ResourceAddTxtobjBean bean = new ResourceAddTxtobjBean();
		bean.fileId = jsonObject.optInt("fileId");
		bean.result = jsonObject.optString("result");
		bean.resName = jsonObject.optString("resName");
		bean.msg = jsonObject.optString("msg");
		return bean;
	}
}
