package com.routon.inforelease.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResourceUploadBeanParser {

	public static ResourceUploadBean parseResourceUploadBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseResourceUploadBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ResourceUploadBean parseResourceUploadBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ResourceUploadBean bean = new ResourceUploadBean();
		bean.obj = parseResourceUploadobjBeanList(jsonObject.optJSONArray("obj"));
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		return bean;
	}

	public static ResourceUploadobjBean parseResourceUploadobjBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ResourceUploadobjBean bean = new ResourceUploadobjBean();
		bean.fileId = jsonObject.optInt("fileId");
		bean.status = jsonObject.optInt("status");
		bean.uploadMsg = jsonObject.optString("uploadMsg");
		bean.originalFileName = jsonObject.optString("originalFileName");
		bean.formFileKey = jsonObject.optString("formFileKey");
		bean.ftpFileName = jsonObject.optString("ftpFileName");
		return bean;
	}

	public static List<ResourceUploadobjBean> parseResourceUploadobjBeanList(JSONArray jsonArray) throws JSONException {
		if (jsonArray == null)
			return null;
		List<ResourceUploadobjBean> list = new ArrayList<ResourceUploadobjBean>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
			if (obj == null) 
				continue;

			ResourceUploadobjBean bean = parseResourceUploadobjBean(obj);
			list.add(bean);
		}
		return list;
	}
}
