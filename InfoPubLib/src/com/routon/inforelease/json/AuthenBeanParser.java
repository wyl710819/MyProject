package com.routon.inforelease.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthenBeanParser {

	public static AuthenBean parseAuthenBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parseAuthenBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static AuthenBean parseAuthenBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		AuthenBean bean = new AuthenBean();
		bean.code = jsonObject.optInt("code");
		bean.msg = jsonObject.optString("msg");
		bean.obj = parseAuthenobjBean(jsonObject.optJSONObject("obj"));
		return bean;
	}

	public static AuthenobjBean parseAuthenobjBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		AuthenobjBean bean = new AuthenobjBean();
		bean.phoneNum = jsonObject.optString("phoneNum");
		bean.email = jsonObject.optString("email");
		bean.address = jsonObject.optString("address");
		bean.userId = jsonObject.optInt("userId");
		bean.userName = jsonObject.optString("userName");
		bean.realName = jsonObject.optString("realName");
		bean.privilege = jsonObject.optInt("privilege");
		bean.portraitUrl = jsonObject.optString("portraitUrl");
		bean.ctrlId = jsonObject.optString("ctrlId");
		bean.groupIds = jsonObject.optString("groupIds");
		//审核权限
		bean.audit_classinfo_privilege = jsonObject.optInt("audit_classinfo_privilege");
		bean.audit_schoolnotice_privilege = jsonObject.optInt("audit_schoolnotice_privilege");
		//换课权限
		bean.timetable_privilege = jsonObject.optInt("timetable_privilege");
		int portrait = jsonObject.optInt("portrait");
		if( portrait > 0 ){
			bean.portrait = String.valueOf(portrait);
		}
		//是否能新建或修改其他用户信息权限
		bean.usermanage_privilege = jsonObject.optInt("usermanage_privilege");
		JSONArray nameList = jsonObject.optJSONArray("schoolNames");//获取JSONArray  
		if( nameList != null ){
			
	        int length = nameList.length();  
	        bean.schools = new String[length];
	        for(int i = 0; i < length; i++) {//遍历JSONArray  
	        	bean.schools[i]  = nameList.optString(i); 
	        }  
		}
		
		nameList = jsonObject.optJSONArray("schoolIds");//获取JSONArray  
		if( nameList != null ){
			
	        int length = nameList.length();  
	        bean.schoolIds = new String[length];
	        for(int i = 0; i < length; i++) {//遍历JSONArray  
	        	bean.schoolIds[i]  = nameList.optString(i); 
	        }  
		}
		
		JSONArray headTeacherClasses = jsonObject.optJSONArray("headTeacherClasses");//获取JSONArray  
		if( headTeacherClasses != null ){
			
	        int length = headTeacherClasses.length();  
	        bean.headTeacherClasses = new String[length];
	        for(int i = 0; i < length; i++) {//遍历JSONArray  
	        	bean.headTeacherClasses[i]  = headTeacherClasses.optString(i); 
	        }  
		}
		return bean;
	}
}
