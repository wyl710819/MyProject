package com.routon.smartcampus.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.flower.BadgeInfo;
import com.routon.smartcampus.flower.OftenBadgeBean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class BadgeSaveUtil {

	/**
	 * 获取常用小红花
	 * @param context
	 * @param userId 老师用户id
	 * @param id 一级徽章id
	 * @return id参数传0时返回所有常用徽章，传指定徽章id时返回指定徽章类型的常用徽章
	 */
	public static ArrayList<BadgeInfo> getAllOftenBadgeData(Context context,int id) {
		
		SharedPreferences prf = context.getSharedPreferences("badge_data", Context.MODE_PRIVATE);
		String remarkData = prf.getString(String.valueOf(InfoReleaseApplication.authenobjData.userId), null);
		Map<Integer, ArrayList<BadgeInfo>> map = new HashMap<>();
		try {
			
			if(remarkData!=null){
				Log.e("remarkData", remarkData);
				JSONObject jsonObject=new JSONObject(remarkData);
				JSONArray jsonArray= jsonObject.optJSONArray("data");
				
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jObject=(JSONObject) jsonArray.get(i);
					ArrayList<BadgeInfo> beans=new ArrayList<BadgeInfo>();
					
					int badgeId=(int) jObject.opt("badgeId");
					JSONArray badgeArray=jObject.optJSONArray("badgeData");
					for (int j = 0; j < badgeArray.length(); j++) {
						OftenBadgeBean bean=new OftenBadgeBean((JSONObject)badgeArray.get(j));
						beans.add(bean);
					}
					map.put(badgeId, beans);
				}
				
				
			}
			
			
			if (id==0) {
				ArrayList<BadgeInfo> beanList=new ArrayList<BadgeInfo>();
				 Set <Integer> set=map.keySet();
				
				for(Integer key:set){
		            List<BadgeInfo> value=map.get(key);
		            beanList.addAll(value);
		        }
				return beanList;
			}else {
				ArrayList<BadgeInfo> beanList=map.get(id);
				return beanList;
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 保存常用小红花
	 * @param context
	 * @param userId 老师用户id
	 * @param jsonStr 徽章json数据
	 * @param badgeId 一级徽章id
	 */
	@SuppressLint("NewApi")
	public static void saveRemarkArrays(Context context,JSONObject jsonStr,int badgeId) {
		
		//现获取原数据
		SharedPreferences prf = context.getSharedPreferences("badge_data", Context.MODE_PRIVATE);
		String remarkData = prf.getString(String.valueOf(InfoReleaseApplication.authenobjData.userId), null);
		
		Log.e("jsonStr", jsonStr.toString());
		try {
			if(remarkData!=null){
				JSONObject jsonObject=new JSONObject(remarkData);
				
				
				JSONArray jsonArray=jsonObject.optJSONArray("data");
				
				boolean idExist=false;
				int tag = 0;
				JSONObject beanObj = null;
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object=jsonArray.getJSONObject(i);
					if (object.optInt("badgeId")==badgeId) {
						beanObj=jsonArray.getJSONObject(i);
						JSONArray badgeArray=beanObj.optJSONArray("badgeData");
						badgeArray.put(jsonStr);
						beanObj.remove("badgeData");
						Log.e("badgeArray===", badgeArray.toString());
						beanObj.put("badgeData", badgeArray);
						idExist=true;
						tag=i;
					}
				}
				
				if (idExist) {
					jsonArray.remove(tag);
					jsonArray.put(beanObj);
					jsonObject.remove("data");
					jsonObject.put("data", jsonArray);
				}else {
					JSONArray beanArray = new JSONArray();
					beanArray.put(0, jsonStr);
					
					JSONObject badgeObject = new JSONObject();
					badgeObject.put("badgeId", badgeId);
					badgeObject.put("badgeData", beanArray);
					
					jsonArray.put(badgeObject);
					
					jsonObject.remove("data");
					jsonObject.put("data", jsonArray);
				}
				
				SharedPreferences.Editor editor = context.getSharedPreferences("badge_data", Context.MODE_PRIVATE).edit();
				editor.putString(String.valueOf(InfoReleaseApplication.authenobjData.userId), jsonObject.toString());
				editor.commit();
				
				
				
			}else {
				
				
				JSONArray beanArray = new JSONArray();
				beanArray.put(0, jsonStr);
				
				JSONObject badgeObject = new JSONObject();
				badgeObject.put("badgeId", badgeId);
				badgeObject.put("badgeData", beanArray);
				
				JSONArray dataArray = new JSONArray();
				dataArray.put(badgeObject);
				
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("userId", InfoReleaseApplication.authenobjData.userId);
				jsonObject.put("data", dataArray);
				
				SharedPreferences.Editor editor = context.getSharedPreferences("badge_data", Context.MODE_PRIVATE).edit();
				editor.putString(String.valueOf(InfoReleaseApplication.authenobjData.userId), jsonObject.toString());
				editor.commit();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressLint("NewApi")
	public static void delOftenBadge(Context context,int badgeId, String sort, String badgeIdStr){
		//现获取原数据
		SharedPreferences prf = context.getSharedPreferences("badge_data", Context.MODE_PRIVATE);
		String remarkData = prf.getString(String.valueOf(InfoReleaseApplication.authenobjData.userId), null);
		
		
		try {
		JSONObject jsonObject=new JSONObject(remarkData);
		JSONArray jsonArray=jsonObject.optJSONArray("data");
		
		JSONObject beanObj = null;
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject object=jsonArray.getJSONObject(i);
			Log.e("运行", object.optInt("badgeId")+"===="+badgeId);
			if (object.optInt("badgeId")==badgeId) {
				beanObj=jsonArray.getJSONObject(i);
				JSONArray badgeArray=beanObj.optJSONArray("badgeData");
				
				if (badgeArray.length()==1) {
					Log.e("运行", "2222");
					jsonArray.remove(i);
				}else {
					Log.e("运行", "3333");
					for (int j = 0; j < badgeArray.length(); j++) {
						if (jsonArray.getJSONObject(i).optString("sort").equals(sort)) {
							badgeArray.remove(j);
						}
						
					}
					
					beanObj.remove("badgeData");
					beanObj.put("badgeData", badgeArray);
					jsonArray.put(beanObj);
					
				}
				
			}
		}
			
			jsonObject.remove("data");
			jsonObject.put("data", jsonArray);
//		}
		
		SharedPreferences.Editor editor = context.getSharedPreferences("badge_data", Context.MODE_PRIVATE).edit();
		editor.putString(String.valueOf(InfoReleaseApplication.authenobjData.userId), jsonObject.toString());
		editor.commit();
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		//删除排序记录中的排序标记
		
		if (badgeIdStr.contains(",")) {
			
		}
	}
	
	
	/**
	 * @param badgeIdStr 
	 * @param badgeList
	 * @return   返回记录的排序集合
	 */
	public static ArrayList<BadgeInfo> badgeListSort(String badgeIdStr, ArrayList<BadgeInfo> badgeList){
		
		if (badgeIdStr.contains(",")) {
			String[] sortStrings = badgeIdStr.split(",");
			//排序
			ArrayList<BadgeInfo> sortList=new ArrayList<BadgeInfo>();
			if (sortStrings!=null) {
				for (int i = 0; i < sortStrings.length; i++) {
					for (int j = 0; j < badgeList.size(); j++) {
						OftenBadgeBean bean=(OftenBadgeBean) badgeList.get(j);
						/*if (sortStrings[i].equals(bean.sort)) {
							sortList.add(badgeList.get(j));
							
						}*/
					}
				}
			}
			
			return sortList;
		}else {
			return badgeList;
		}
	}
	
}
