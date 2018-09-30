package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


public class FindAdPeriodsBeanParser {

public static FindAdPeriodsBean parseFindAdPeriodsBean(String text) {
	try {
		JSONObject jsonObject = new JSONObject(text);
		return parseFindAdPeriodsBean(jsonObject);
	} catch (JSONException e) {
		e.printStackTrace();
	}
	return null;
}
public static FindAdPeriodsBean parseFindAdPeriodsBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	FindAdPeriodsBean bean = new FindAdPeriodsBean();
	bean.periods = parseFindAdPeriodsperiodsBeanList(jsonObject.getJSONArray("periods"));
	bean.code = jsonObject.optInt("code");
	bean.msg = jsonObject.optString("msg");
	return bean;
}
public static FindAdPeriodsperiodsBean parseFindAdPeriodsperiodsBean(JSONObject jsonObject) throws JSONException {
	if (jsonObject == null)
		return null;
	FindAdPeriodsperiodsBean bean = new FindAdPeriodsperiodsBean();
	bean.loops = jsonObject.optInt("loops");
	bean.periodId = jsonObject.optInt("periodId");
	bean.max = jsonObject.optInt("max");
	bean.adId = jsonObject.optInt("adId");
	bean.beginTime = jsonObject.optString("beginTime");
	bean.endTime = jsonObject.optString("endTime");
	return bean;
}

public static JSONObject getJSONObject(FindAdPeriodsperiodsBean bean){
	JSONObject obj = new JSONObject();
	try {
		obj.putOpt("loops", bean.loops);
		obj.putOpt("periodId", bean.periodId);
		obj.putOpt("max", bean.max);
		obj.putOpt("adId", bean.adId);
		obj.putOpt("beginTime", bean.beginTime);
		obj.putOpt("endTime", bean.endTime);
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return obj;
}

public static ArrayList<FindAdPeriodsperiodsBean> parseFindAdPeriodsperiodsBeanList(JSONArray jsonArray) throws JSONException {
	if (jsonArray == null)
		return null;
	ArrayList<FindAdPeriodsperiodsBean> list = new ArrayList<FindAdPeriodsperiodsBean>();
	for (int i = 0; i < jsonArray.length(); i++) {
		JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
		FindAdPeriodsperiodsBean bean = parseFindAdPeriodsperiodsBean(obj);
		list.add(bean);
	}	
	return list;
}
 }
