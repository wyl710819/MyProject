package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


public class ResourceRescatBeanParser {

public static List<ResourceRescatBean> parseResourceRescatBeanList(String text) {
	try {
		JSONArray jsonArray = new JSONArray(text);;
		return parseResourceRescatBeanList(jsonArray);
	} catch (JSONException e) {
		e.printStackTrace();
	}
	return null;
}
public static ResourceRescatBean parseResourceRescatBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	ResourceRescatBean bean = new ResourceRescatBean();
	bean.icon = jsonObject.optString("icon");
	bean.isParent = jsonObject.optBoolean("isParent");
	bean.url = jsonObject.optString("url");
	bean.id = jsonObject.optInt("id");
	bean.open = jsonObject.optBoolean("open");
	bean.parentId = jsonObject.optInt("parentId");
	bean.queryChildNum = jsonObject.optInt("queryChildNum");
	bean.nodes = parseResourceRescatnodesBeanList(jsonObject.optJSONArray("nodes"));
	bean.name = jsonObject.optString("name");
	bean.target = jsonObject.optString("target");
	bean.checked = jsonObject.optBoolean("checked");
	bean.childNum = jsonObject.optInt("childNum");
	bean.isShow = jsonObject.optInt("isShow");
	return bean;
}
public static ResourceRescatnodesBean parseResourceRescatnodesBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	ResourceRescatnodesBean bean = new ResourceRescatnodesBean();
	bean.icon = jsonObject.optString("icon");
	bean.isParent = jsonObject.optBoolean("isParent");
	bean.url = jsonObject.optString("url");
	bean.id = jsonObject.optInt("id");
	bean.open = jsonObject.optBoolean("open");
	bean.parentId = jsonObject.optInt("parentId");
	bean.queryChildNum = jsonObject.optInt("queryChildNum");
	bean.nodes = parseResourceRescatnodesBeanList(jsonObject.optJSONArray("nodes"));
	bean.name = jsonObject.optString("name");
	bean.target = jsonObject.optString("target");
	bean.checked = jsonObject.optBoolean("checked");
	bean.childNum = jsonObject.optInt("childNum");
	bean.isShow = jsonObject.optInt("isShow");
	return bean;
}

public static List<ResourceRescatBean> parseResourceRescatBeanList(JSONArray jsonArray) throws JSONException {
	if (jsonArray == null)
		return null;
	List<ResourceRescatBean> list = new ArrayList<ResourceRescatBean>();
	for (int i = 0; i < jsonArray.length(); i++) {
		JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
		if (obj == null) 
			continue;

		ResourceRescatBean bean = parseResourceRescatBean(obj);
		list.add(bean);
	}	
	return list;
}
public static List<ResourceRescatnodesBean> parseResourceRescatnodesBeanList(JSONArray jsonArray) throws JSONException {
	if (jsonArray == null)
		return null;
	List<ResourceRescatnodesBean> list = new ArrayList<ResourceRescatnodesBean>();
	for (int i = 0; i < jsonArray.length(); i++) {
		JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
		if (obj == null) 
			continue;

		ResourceRescatnodesBean bean = parseResourceRescatnodesBean(obj);
		list.add(bean);
	}	
	return list;
}
 }
