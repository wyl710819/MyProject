package com.routon.inforelease.json;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import android.util.Log;

public class TerminalListBeanParser {

	public static TerminalListBean parseTerminalListBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseTerminalListBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static TerminalListBean parseTerminalListBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		TerminalListBean bean = new TerminalListBean();
		bean.page = jsonObject.optInt("page");
		bean.pageSize = jsonObject.optInt("pageSize");
		bean.code = jsonObject.optInt("code");
		bean.fullListSize = jsonObject.optInt("fullListSize");
		bean.msg = jsonObject.optString("msg");

		bean.datas = parseTerminalListdatasBeanList(jsonObject.optJSONArray("datas"));
		return bean;
	}
	
	public static TerminalListdatasBean parseTerminalListdatasBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		TerminalListdatasBean bean = new TerminalListdatasBean();	
		bean.terplace2 = jsonObject.optString("terplace2");
		bean.terip = jsonObject.optString("terip");
		bean.bsgroup = jsonObject.optString("bsgroup");
		bean.holidayonofftime = jsonObject.optString("holidayonofftime");
		bean.lastcomutime = jsonObject.optString("lastcomutime");
		bean.onofftime = jsonObject.optString("onofftime");
		bean.terminalid = jsonObject.optString("terminalid");
		bean.type = jsonObject.optInt("type");
		bean.typename = jsonObject.optString("typename");
		bean.ipaddress = jsonObject.optString("ipaddress"); 
		bean.firstcommtime = jsonObject.optString("firstcommtime");
		bean.createtime = jsonObject.optString("createtime");
		bean.logintime = jsonObject.optString("logintime");
		bean.olstate = jsonObject.optString("olstate"); 
		bean.disksize = jsonObject.optString("disksize");
		bean.softver = jsonObject.optString("softver");
		bean.groupid = jsonObject.optInt("groupid");
		bean.archiveid = jsonObject.optInt("archiveid");
		bean.termodealias = jsonObject.optString("termodealias");
		bean.txtTerminalState = jsonObject.optInt("txtTerminalState");
		bean.areastr = jsonObject.optString("areastr");
		bean.organization = jsonObject.optString("organization");
		bean.btmac = jsonObject.optString("btmac");
		
		//installplace = areastr + organization + installplace
		bean.installplace = jsonObject.optString("installplace");
		
		JSONArray jsonArray = jsonObject.optJSONArray("swtchs");
		if (jsonArray != null)
		{
			bean.mswtchs = new  ArrayList<TerminalListSwtchBean>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
				if (obj == null) 
					continue;
				
					TerminalListSwtchBean swtch = TerminalListSwtchBean.parseSwtchBean(obj);
					bean.mswtchs.add(swtch);
			}
		}
//		if( bean.organization != null ){
//			bean.installplace = bean.organization + bean.installplace;
//		}
//		if( bean.areastr != null ){
//			bean.installplace = bean.areastr + bean.installplace;
//		}
		return bean;
	}

	public static List<TerminalListdatasBean> parseTerminalListdatasBeanList(JSONArray jsonArray) {
		if (jsonArray == null)
			return null;
		List<TerminalListdatasBean> list = new ArrayList<TerminalListdatasBean>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
				if (obj == null) 
					continue;

				TerminalListdatasBean bean = parseTerminalListdatasBean(obj);
				list.add(bean);
			}

		} catch (JSONException e) {

		}
		return list;

	}
}
