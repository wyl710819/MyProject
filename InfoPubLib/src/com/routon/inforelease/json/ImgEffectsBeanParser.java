package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class ImgEffectsBeanParser {

	public static ImgEffectsBean parseImgEffectsBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseImgEffectsBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ImgEffectsBean parseImgEffectsBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ImgEffectsBean bean = new ImgEffectsBean();
		bean.imgEffects = parseImgEffectsimgEffectsBean(jsonObject.optJSONObject("imgEffects"));
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		return bean;
	}

	public static ImgEffectsimgEffectsBean parseImgEffectsimgEffectsBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ImgEffectsimgEffectsBean bean = new ImgEffectsimgEffectsBean();
		bean.items = parseImgEffectsitemsBeanList(jsonObject.optJSONArray("items"));
		bean.defaultKey = jsonObject.optInt("defaultKey");
		return bean;
	}

	public static ImgEffectsitemsBean parseImgEffectsitemsBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		ImgEffectsitemsBean bean = new ImgEffectsitemsBean();
		bean.value = jsonObject.optString("value");
		bean.key = jsonObject.optString("key");
		return bean;
	}

	public static List<ImgEffectsitemsBean> parseImgEffectsitemsBeanList(JSONArray jsonArray) throws JSONException {
		if (jsonArray == null)
			return null;
		List<ImgEffectsitemsBean> list = new ArrayList<ImgEffectsitemsBean>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.get(i);
			ImgEffectsitemsBean bean = parseImgEffectsitemsBean(obj);
			if (bean != null)
				list.add(bean);
		}
		return list;
	}
}
