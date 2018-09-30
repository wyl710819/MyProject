package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class PlanMaterialBeanParser {

	public static PlanMaterialBean parsePlanMaterialBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parsePlanMaterialBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PlanMaterialBean parsePlanMaterialBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		PlanMaterialBean bean = new PlanMaterialBean();
		bean.total = jsonObject.optInt("total");
		bean.rows = parsePlanMaterialrowsBeanList(jsonObject.optJSONArray("rows"));
		return bean;
	}
	
	public static JSONObject getJSONObject(PlanMaterialrowsBean bean){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.putOpt("fileID", bean.fileID);
			jsonObject.putOpt("thumbnail", bean.thumbnail);
			jsonObject.putOpt("adId", bean.adId);
			jsonObject.putOpt("type", bean.type);
			jsonObject.putOpt("name", bean.name);
			jsonObject.putOpt("modify", bean.modify);
			JSONArray params = new JSONArray();
			if( bean.params != null ){
				for( int i = 0; i < bean.params.size(); i++ ){
					JSONObject materialObj = getJSONObject(bean.params.get(i));
					params.put(materialObj);
				}
			}
			jsonObject.putOpt("params",params);
			
			JSONArray periods = new JSONArray();
			if( bean.periods != null ){
				for( int i = 0; i < bean.periods.size(); i++ ){
					JSONObject periodObj = FindAdPeriodsBeanParser.getJSONObject(bean.periods.get(i));
					periods.put(periodObj);
				}
			}
			jsonObject.putOpt("periods",periods);
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}

	public static PlanMaterialrowsBean parsePlanMaterialrowsBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		PlanMaterialrowsBean bean = new PlanMaterialrowsBean();
		bean.fileID = jsonObject.optInt("fileID");
		bean.thumbnail = jsonObject.optString("thumbnail");
		bean.adId = jsonObject.optInt("adId");
		bean.type = jsonObject.optInt("type");
		bean.name = jsonObject.optString("name");
		bean.modify = jsonObject.optLong("modify");
		bean.params = parsePlanMaterialparamsBeanList(jsonObject.optJSONArray("params"));
		
		bean.periods = FindAdPeriodsBeanParser.parseFindAdPeriodsperiodsBeanList(jsonObject.optJSONArray("periods"));
		
		return bean;
	}

	public static PlanMaterialparamsBean parsePlanMaterialparamsBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		PlanMaterialparamsBean bean = new PlanMaterialparamsBean();
		bean.adParamId = jsonObject.optInt("adParamId");
		bean.adParamValue = jsonObject.optString("adParamValue");
		return bean;
	}
	
	public static JSONObject getJSONObject(PlanMaterialparamsBean bean){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.putOpt("adParamId", bean.adParamId);
			jsonObject.putOpt("adParamValue", bean.adParamValue);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}

	public static List<PlanMaterialrowsBean> parsePlanMaterialrowsBeanList(JSONArray jsonArray) throws JSONException {
		if (jsonArray == null)
			return null;
		List<PlanMaterialrowsBean> list = new ArrayList<PlanMaterialrowsBean>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.get(i);
			PlanMaterialrowsBean bean = parsePlanMaterialrowsBean(obj);
			if (bean != null)
				list.add(bean);
		}
		return list;
	}

	public static List<PlanMaterialparamsBean> parsePlanMaterialparamsBeanList(JSONArray jsonArray) throws JSONException {
		if (jsonArray == null)
			return null;
		List<PlanMaterialparamsBean> list = new ArrayList<PlanMaterialparamsBean>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.get(i);
			PlanMaterialparamsBean bean = parsePlanMaterialparamsBean(obj);
			if (bean != null)
				list.add(bean);
		}
		return list;
	}
}
