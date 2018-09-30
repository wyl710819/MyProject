package com.routon.inforelease.json;

import org.json.JSONException;
import org.json.JSONObject;



public class TerminalListSwtchBean {
	public int status;
	public int swtch;
	public String modifytime;
	
	public static TerminalListSwtchBean parseSwtchBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		TerminalListSwtchBean bean = new TerminalListSwtchBean();
		bean.status = jsonObject.optInt("status");
		bean.swtch = jsonObject.optInt("swtch");
		bean.modifytime = jsonObject.optString("modifytime");
		return bean;
	}
 };
