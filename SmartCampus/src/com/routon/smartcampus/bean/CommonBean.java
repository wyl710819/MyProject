package com.routon.smartcampus.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommonBean<T> {
	public int code;
	public String msg;
	public int fullListSize;
	public int page;
	public int pageSize;
	public ArrayList<T> datas;
	
	public interface ParseDataCallback<T>{
		T parsedata(JSONObject jsonObject);
	}
	
	public ArrayList<T> parseDatas(JSONArray jsonArray,ParseDataCallback<T> callback) throws JSONException {
		if (jsonArray == null)
			return null;
		ArrayList<T> list = new ArrayList<T>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.opt(i);
			T bean = callback.parsedata(obj);
			list.add(bean);
		}	
		return list;
	}
	
	public void parseBean(String text,ParseDataCallback<T> callback){
		try {
			JSONObject jsonObject = new JSONObject(text);
			code = jsonObject.optInt("code");
			msg = jsonObject.optString("msg");
			fullListSize = jsonObject.optInt("fullListSize");
			page = jsonObject.optInt("page");
			pageSize = jsonObject.optInt("pageSize");
			datas = parseDatas(jsonObject.optJSONArray("datas"),callback);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
