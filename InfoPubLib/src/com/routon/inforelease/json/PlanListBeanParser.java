package com.routon.inforelease.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlanListBeanParser {

	public static PlanListBean parsePlanListBean(String text) {
		try {
			JSONObject jsonObject = new JSONObject(text);
			return parsePlanListBean(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PlanListBean parsePlanListBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		PlanListBean bean = new PlanListBean();
		bean.total = jsonObject.optInt("total");
		bean.rows = parsePlanListrowsBeanList(jsonObject.optJSONArray("rows"));
		return bean;
	}
	
	public static JSONObject getJSONObject(PlanListrowsBean bean){
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.putOpt("name", bean.name);
			jsonObject.putOpt("id", bean.contractId);//计划id，新增时为-1
			jsonObject.putOpt("groups", bean.groups);
			jsonObject.putOpt("editPkgUrl", bean.editPkgUrl);
			jsonObject.putOpt("fileId", bean.fileId);
			JSONArray adList = new JSONArray();
			if( bean.materialList != null ){
				for( int i = 0; i < bean.materialList.size(); i++ ){
					JSONObject materialObj = PlanMaterialBeanParser.getJSONObject(bean.materialList.get(i));
					adList.put(materialObj);
				}
			}
			
			if( bean.subTitleList != null ){
				for( int i = 0; i < bean.subTitleList.size(); i++ ){
					JSONObject materialObj = PlanMaterialBeanParser.getJSONObject(bean.subTitleList.get(i));
					adList.put(materialObj);
				}
			}
			
			jsonObject.putOpt("adList", adList);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonObject;
	} 

	public static PlanListrowsBean parsePlanListrowsBean(JSONObject jsonObject) throws JSONException {
		if (jsonObject == null)
			return null;
		PlanListrowsBean bean = new PlanListrowsBean();
		bean.name=jsonObject.optString("contractName");
		bean.status = jsonObject.optString("status");
		bean.beginTime = jsonObject.optString("beginTime");
		bean.endTime = jsonObject.optString("endTime");
		bean.contractName = jsonObject.optString("contractName");
		int publishStatus = jsonObject.optInt("publishStatus");
		if( publishStatus == 1 ){//1表示已发布
			bean.published = true;
		}
		
		bean.contractId = jsonObject.optInt("contractId");
		bean.groups = jsonObject.optString("groups");
		bean.editPkgUrl = jsonObject.optString("editPkgUrl");
		bean.fileId = jsonObject.optInt("fileId");
		
		JSONArray adList = jsonObject.optJSONArray("adList");
		bean.subTitleList = new ArrayList<PlanMaterialrowsBean>();
		bean.materialList = new ArrayList<PlanMaterialrowsBean>();
		if( adList != null ){
			for( int i = 0; i < adList.length(); i++ ){
				JSONObject adObject = adList.optJSONObject(i);
				if( adObject != null ){
					PlanMaterialrowsBean planMaterialrowsBean = PlanMaterialBeanParser.parsePlanMaterialrowsBean(adObject);
					if( planMaterialrowsBean.type == 1 ){//文字
						bean.subTitleList.add(planMaterialrowsBean);
					}else if( planMaterialrowsBean.type == 3 ){//图片
						bean.materialList.add(planMaterialrowsBean);
					}				
				}
			}
		}
		return bean;
	}

	public static List<PlanListrowsBean> parsePlanListrowsBeanList(JSONArray jsonArray) throws JSONException {
		if (jsonArray == null)
			return null;
		List<PlanListrowsBean> list = new ArrayList<PlanListrowsBean>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = (JSONObject) jsonArray.optJSONObject(i);
			if (obj == null) 
				continue;
			PlanListrowsBean bean = parsePlanListrowsBean(obj);
			list.add(bean);
		}
		return list;
	}
}
