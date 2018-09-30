package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


public class ResourceResTagBeanParser {

public static ResourceResTagBean parseResourceResTagBean(String text) {
	try {
		JSONObject jsonObject = new JSONObject(text);
		return parseResourceResTagBean(jsonObject);
	} catch (JSONException e) {
		e.printStackTrace();
	}
	return null;
}
public static ResourceResTagBean parseResourceResTagBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	ResourceResTagBean bean = new ResourceResTagBean();
	bean.datas = parseResourceResTagdatasBeanList(jsonObject.optJSONArray("datas"));
	bean.info = parseResourceResTaginfoBean(jsonObject.optJSONObject("info"));
	return bean;
}
public static ResourceResTagdatasBean parseResourceResTagdatasBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	ResourceResTagdatasBean bean = new ResourceResTagdatasBean();
	bean.id = jsonObject.optInt("id");
	bean.labelname = jsonObject.optString("labelname");
	return bean;
}
public static ResourceResTaginfoBean parseResourceResTaginfoBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	ResourceResTaginfoBean bean = new ResourceResTaginfoBean();
	bean.pageSize = jsonObject.optInt("pageSize");
	bean.code = jsonObject.optInt("code");
	bean.fullListSize = jsonObject.optInt("fullListSize");
	bean.msg = jsonObject.optString("msg");
	return bean;
}
public static List<ResourceResTagdatasBean> parseResourceResTagdatasBeanList(JSONArray jsonArray) throws JSONException {
	if (jsonArray == null)
		return null;
	List<ResourceResTagdatasBean> list = new ArrayList<ResourceResTagdatasBean>();
	for (int i = 0; i < jsonArray.length(); i++) {
		JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
		if (obj == null) 
			continue;

		ResourceResTagdatasBean bean = parseResourceResTagdatasBean(obj);
		list.add(bean);
	}	
	return list;
}
 }
