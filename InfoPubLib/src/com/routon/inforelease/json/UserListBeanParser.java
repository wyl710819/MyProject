package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.util.Log;

public class UserListBeanParser {

	public static UserListBean parseUserListBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseUserListBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static UserListBean parseUserListBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		UserListBean bean = new UserListBean();
		bean.page = jsonObject.optInt("page");
		bean.pageSize = jsonObject.optInt("pageSize");
		bean.code = jsonObject.optInt("code");
		bean.fullListSize = jsonObject.optInt("fullListSize");
		bean.msg = jsonObject.optString("msg");

		bean.datas = parseUserListdatasBeanList(jsonObject.optJSONArray("datas"));
		return bean;
	}
	
	public static UserListdatasBean parseUserListdatasBeanList(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		UserListdatasBean bean = new UserListdatasBean();	
		bean.userid = jsonObject.optInt("userid");
		bean.username = jsonObject.optString("username");
		bean.realname = jsonObject.optString("realname");
		bean.phonenum = jsonObject.optString("phonenum");
		bean.email = jsonObject.optString("email");
		bean.address = jsonObject.optString("address");
		bean.createtime = jsonObject.optString("createtime");
		bean.state = jsonObject.optString("state");
		bean.pwdchangetime = jsonObject.optString("pwdchangetime");
		bean.loggingtime = jsonObject.optString("loggingtime"); 
		bean.loggingip = jsonObject.optString("loggingip");
		bean.groupids = jsonObject.optString("groupIds");
		bean.groupnames = jsonObject.optString("groupNames");
		return bean;
	}

	public static List<UserListdatasBean> parseUserListdatasBeanList(JSONArray jsonArray) {
		if (jsonArray == null)
			return null;
		List<UserListdatasBean> list = new ArrayList<UserListdatasBean>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
				if (obj == null) 
					continue;

				UserListdatasBean bean = parseUserListdatasBeanList(obj);
				if( bean.state != null && bean.state.equals("0")){//0是停用状态
					continue;
				}
				list.add(bean);
			}

		} catch (JSONException e) {

		}
		return list;

	}
}
