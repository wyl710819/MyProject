package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


public class ClassInfoListBeanParser {

public static ClassInfoListBean parseClassInfoListBean(String text) {
	try {
		JSONObject jsonObject = new JSONObject(text);
		return parseClassInfoListBean(jsonObject);
	} catch (JSONException e) {
		e.printStackTrace();
	}
	return null;
}
public static ClassInfoListBean parseClassInfoListBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	ClassInfoListBean bean = new ClassInfoListBean();
	bean.datas = parseClassInfoListdatasBeanList(jsonObject.optJSONArray("datas"));
	bean.page = jsonObject.optInt("page");
	bean.pageSize = jsonObject.optInt("pageSize");
	bean.code = jsonObject.optInt("code");
	bean.fullListSize = jsonObject.optInt("fullListSize");
	bean.msg = jsonObject.optString("msg");
	return bean;
}
public static ClassInfoListdatasBean parseClassInfoListdatasBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	ClassInfoListdatasBean bean = new ClassInfoListdatasBean();
	bean.id = jsonObject.optInt("id");
	bean.files = parseClassInfoListfilesBeanList(jsonObject.optJSONArray("files"));
	bean.duration = jsonObject.optString("duration");
	bean.title = jsonObject.optString("title");
	bean.groupIds = jsonObject.optString("groupIds");
	bean.desc = jsonObject.optString("desc");
	bean.subtitle2 = jsonObject.optString("subtitle2");
	bean.status = jsonObject.optInt("status");
	bean.subtitle1 = jsonObject.optString("subtitle1");
	bean.groupNames = jsonObject.optString("groupNames");
	bean.type = jsonObject.optInt("type", 1);
	bean.priority = jsonObject.optInt("priority", 0);
	bean.startTime=jsonObject.optString("startTime");
	bean.endTime=jsonObject.optString("endTime");
	bean.editPkg=jsonObject.optInt("editPkg");
	bean.editPkgUrl=jsonObject.optString("editPkgUrl");
	bean.publishStatus = jsonObject.optInt("publishStatus", 0);
	bean.attitude = jsonObject.optString("attitude");
	return bean;
}
public static ClassInfoListfilesBean parseClassInfoListfilesBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	ClassInfoListfilesBean bean = new ClassInfoListfilesBean();
	bean.createtime = jsonObject.optString("createtime");
	bean.content = jsonObject.optString("content");
	bean.resid = jsonObject.optInt("resid");
	bean.filetypeid = jsonObject.optInt("filetypeid");
	//添加species
	bean.species=jsonObject.optInt("species");
	return bean;
}
public static List<ClassInfoListdatasBean> parseClassInfoListdatasBeanList(JSONArray jsonArray) throws JSONException {
	if (jsonArray == null)
		return null;
	List<ClassInfoListdatasBean> list = new ArrayList<ClassInfoListdatasBean>();
	for (int i = 0; i < jsonArray.length(); i++) {
		JSONObject obj = (JSONObject) jsonArray.opt(i);
		ClassInfoListdatasBean bean = parseClassInfoListdatasBean(obj);
		list.add(bean);
	}	
	return list;
}
public static List<ClassInfoListfilesBean> parseClassInfoListfilesBeanList(JSONArray jsonArray) throws JSONException {
	if (jsonArray == null)
		return null;
	List<ClassInfoListfilesBean> list = new ArrayList<ClassInfoListfilesBean>();
	for (int i = 0; i < jsonArray.length(); i++) {
		JSONObject obj = (JSONObject) jsonArray.opt(i);
		ClassInfoListfilesBean bean = parseClassInfoListfilesBean(obj);
		list.add(bean);
	}	
	return list;
}
 }
