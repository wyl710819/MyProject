package com.routon.inforelease.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResourceListBeanParser {

	public static ResourceListBean parseResourceListBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseResourceListBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ResourceListBean parseResourceListBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ResourceListBean bean = new ResourceListBean();
		bean.page = jsonObject.optString("page");
		bean.pageSize = jsonObject.optInt("pageSize");
		bean.code = jsonObject.optInt("code");
		bean.fullListSize = jsonObject.optInt("fullListSize");
		bean.msg = jsonObject.optString("msg");

		bean.datas = parseResourceListdatasBeanList(jsonObject.optJSONArray("datas"));
		return bean;
	}

	public static ResourceListdatasBean parseResourceListdatasBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ResourceListdatasBean bean = new ResourceListdatasBean();
		bean.content = jsonObject.optString("content");
		bean.resid = jsonObject.optInt("resid");
		bean.filetypeid = jsonObject.optInt("filetypeid");
		bean.createtime = jsonObject.optString("createtime");
		return bean;
	}

	public static List<ResourceListdatasBean> parseResourceListdatasBeanList(JSONArray jsonArray) throws JSONException {
		if (jsonArray == null)
			return null;
		List<ResourceListdatasBean> list = new ArrayList<ResourceListdatasBean>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
			if (obj == null) 
				continue;

			ResourceListdatasBean bean = parseResourceListdatasBean(obj);
			list.add(bean);
		}
		return list;
	}
}
