package com.routon.smartcampus.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.routon.inforelease.plan.create.GroupInfo;

public class ClassListBean {

	public ClassListBean() {
		// TODO Auto-generated constructor stub
	}
	
	public static void parseGroupList(ArrayList<GroupInfo> classGroups,JSONArray jsonArray) {
		if( classGroups == null ) return;

		try {
			int length = jsonArray.length();

			for (int i = 0; i < length; i++) {
				JSONObject parentJsonObj = jsonArray.getJSONObject(i);

				GroupInfo info = new GroupInfo();
				info.setId(parentJsonObj.getInt("id"));
				String showName = parentJsonObj.optString("showName");
         		if( showName != null && showName.isEmpty() == false ){
         			info.setName(showName);
         		}else{
         			info.setName(parentJsonObj.getString("name"));
         		}
				info.setPid(parentJsonObj.getInt("pid"));

				JSONArray childJsonArray = parentJsonObj.getJSONArray("children");
				if (childJsonArray == null || childJsonArray.length() == 0) {

					classGroups.add(info);
					continue;
				} else {
					parseGroupList(classGroups,childJsonArray);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
