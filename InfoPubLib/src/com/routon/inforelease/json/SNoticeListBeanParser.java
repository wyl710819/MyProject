package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


public class SNoticeListBeanParser {

public static SNoticeListBean parseSNoticeListBean(String text) {
	try {
		JSONObject jsonObject = new JSONObject(text);
		return parseSNoticeListBean(jsonObject);
	} catch (JSONException e) {
		e.printStackTrace();
	}
	return null;
}
public static SNoticeListBean parseSNoticeListBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	SNoticeListBean bean = new SNoticeListBean();
	bean.code = jsonObject.optInt("code");
	bean.msg = jsonObject.optString("msg");
	bean.rows = parseSNoticeListrowsBeanList(jsonObject.optJSONArray("rows"));
	return bean;
}
public static SNoticeListrowsBean parseSNoticeListrowsBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	SNoticeListrowsBean bean = new SNoticeListrowsBean();
	bean.id = jsonObject.optInt("id");
	bean.createTime = jsonObject.optString("createTime");
	bean.fontColor = jsonObject.optString("fontColor");
	bean.status = jsonObject.optInt("status");
	bean.modifyUser = jsonObject.optInt("modifyUser");
	bean.font = jsonObject.optString("font");
	bean.sorting = jsonObject.optInt("sorting");
	bean.startTime = jsonObject.optString("startTime");
	bean.endTime = jsonObject.optString("endTime");
	bean.notice = jsonObject.optString("notice");
	bean.fontSize = jsonObject.optInt("fontSize");
	bean.modifyTime = jsonObject.optString("modifyTime");
	bean.groups = jsonObject.optString("groups");
	bean.bgcolor = jsonObject.optString("bgcolor");
	bean.attitude = jsonObject.optString("attitude");
	
	return bean;
}
public static List<SNoticeListrowsBean> parseSNoticeListrowsBeanList(JSONArray jsonArray) throws JSONException {
	if (jsonArray == null)
		return null;
	List<SNoticeListrowsBean> list = new ArrayList<SNoticeListrowsBean>();
	for (int i = 0; i < jsonArray.length(); i++) {
		JSONObject obj = (JSONObject) jsonArray.opt(i);
		SNoticeListrowsBean bean = parseSNoticeListrowsBean(obj);
		list.add(bean);
	}	
	return list;
}
 }
