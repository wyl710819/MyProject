package com.routon.inforelease.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TerminalGroupBeanParser {

	public static List<TerminalGroupBean> parseTerminalGroupBeanList(String text) {
		try {
			JSONArray jsonArray = new JSONArray(text);
			;
			return parseTerminalGroupBeanList(jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TerminalGroupBean parseTerminalGroupBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		TerminalGroupBean bean = new TerminalGroupBean();
		bean.id = jsonObject.optInt("id");
		bean.name = jsonObject.optString("name");
		bean.children = parseTerminalGroupBeanList(jsonObject.optJSONArray("children"));
		bean.pid = jsonObject.optInt("pid");
		return bean;
	}

	public static List<TerminalGroupBean> parseTerminalGroupBeanList(JSONArray jsonArray) throws JSONException {
		if (jsonArray == null)
			return null;
		List<TerminalGroupBean> list = new ArrayList<TerminalGroupBean>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
			if (obj == null) 
				continue;

			TerminalGroupBean bean = parseTerminalGroupBean(obj);
			list.add(bean);
		}
		return list;
	}
}
