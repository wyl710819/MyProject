package com.routon.inforelease.json;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieStringRequest;
import com.routon.inforelease.util.DataResponse;

public class GroupListData {
	private static final String TAG = "GroupListData";
	private static ArrayList<GroupInfo> mGroups = new ArrayList<GroupInfo>();
	private static ArrayList<GroupInfo> mClassGroups = new ArrayList<GroupInfo>();
	
	private static void parseGroupList(JSONArray jsonArray){
		if( jsonArray == null ){
			return;
		}
 		try {
             int length = jsonArray.length();
             
             for(int i = 0; i<length; i++){
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
     			
         		mGroups.add(info);
         		JSONArray childJsonArray = parentJsonObj.getJSONArray("children");
         		if(childJsonArray == null || childJsonArray.length() == 0){        			
         			mClassGroups.add(info);
         			continue;
         		}else{
             		parseGroupList(childJsonArray);
         		}
         	}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();		
 		}
 	}
	
	private static ArrayList<GroupInfo> mSchoolGroups = new ArrayList<GroupInfo>();
	private static ArrayList<GroupInfo> mSchoolClassGroups = new ArrayList<GroupInfo>();
	private static void parseSchoolGroupList(JSONArray jsonArray){
		if( jsonArray == null ){
			return;
		}
 		try {
             int length = jsonArray.length();
             
             for(int i = 0; i<length; i++){
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
     			
         		mSchoolGroups.add(info);
         		JSONArray childJsonArray = parentJsonObj.getJSONArray("children");
         		if(childJsonArray == null || childJsonArray.length() == 0){        			
         			mSchoolClassGroups.add(info);
         			continue;
         		}else{
         			parseSchoolGroupList(childJsonArray);
         		}
         	}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();		
 		}
 	}
	
	public static void resetStatic(){
		if( mSchoolGroups != null ){
			mSchoolGroups.clear();
		}
		if( mSchoolClassGroups != null ){
			mSchoolClassGroups.clear();
		}
		if( mGroups != null ){
			mGroups.clear();
		}
		if( mClassGroups != null ){
			mClassGroups.clear();
		}
	}
	
	public static void getSchoolGroupListData(final Activity activity,final String parent,final DataResponse.Listener<ArrayList<GroupInfo>> listener,
			final DataResponse.ErrorListener errorListener,
			final DataResponse.SessionInvalidListener sessionInvalidListener,final boolean getClass){
		String urlString = UrlUtils.getGroupListCmdUrl(parent);
		if( mSchoolGroups != null && mSchoolGroups.size() > 0 ){
			 if( listener != null ){
				 if( getClass == true ){
					 listener.onResponse(mSchoolClassGroups);
				 }else{
					 listener.onResponse(mSchoolGroups);
				 }
        	 }
			 
			 return;
		}
   
    	CookieStringRequest request = new CookieStringRequest(Request.Method.POST, urlString,
         		new Response.Listener<String>() {  
                     @Override  
                     public void onResponse(String response) {  
                         Log.d(TAG, "response="+response);  
                         
                         JSONObject obj = null;
                         try {
							obj = new JSONObject(response);
                         } catch (JSONException e) {
                         }
                         JSONArray jsonArray = null;
                         try {
							jsonArray = new JSONArray(response);
                         } catch (JSONException e) {
                        	 
                         }
                         if( obj != null ){
                        	 if( obj.optInt("code") == -2 ){
                        		InfoReleaseApplication.returnToLogin(activity);
                        		if( sessionInvalidListener != null ){
                        			sessionInvalidListener.onSessionInvalidResponse();
                        		}
                        	 }else{
                        		 if( errorListener != null ){
         							errorListener.onErrorResponse(null);
                            	 }
                        	 }
                         }else{
                        	 mSchoolGroups.clear();
                        	 mSchoolClassGroups.clear();
                        	 parseSchoolGroupList(jsonArray);  
                        	 if( listener != null ){
                        		 if( getClass == true ){
                        			 listener.onResponse(mSchoolClassGroups);
                        		 }else{
                        			 listener.onResponse(mSchoolGroups);
                        		 }
                        	 }
                         }
                     }
                 },   
                 new Response.ErrorListener() {  
                     @Override  
                     public void onErrorResponse(VolleyError arg0) {  
                     	Log.e(TAG, "sorry,Error arg0:"+arg0); 
                    	//先判断网络状况
						InfoReleaseApplication.showNetWorkFailed(activity);
	
						if( errorListener != null ){
							errorListener.onErrorResponse(arg0);
                   	 	}
                     }  
                 });  
    	 request.setCookie(HttpClientDownloader.getInstance().getCookie());
         InfoReleaseApplication.requestQueue.add(request); 
	}
	
	public static void getGroupListData(final Activity activity,final DataResponse.Listener<ArrayList<GroupInfo>> listener,
			final DataResponse.ErrorListener errorListener,
			final DataResponse.SessionInvalidListener sessionInvalidListener){
		getGroupListData(activity,listener,errorListener,sessionInvalidListener,false,false);
	}
	
	public static void getClassListData(final Activity activity,final DataResponse.Listener<ArrayList<GroupInfo>> listener,
			final DataResponse.ErrorListener errorListener,
			final DataResponse.SessionInvalidListener sessionInvalidListener){
		getGroupListData(activity,listener,errorListener,sessionInvalidListener,false,true);
	}

	//getClass是否获取叶子节点数据
	public static void getGroupListData(final Activity activity,final DataResponse.Listener<ArrayList<GroupInfo>> listener,
			final DataResponse.ErrorListener errorListener,
			final DataResponse.SessionInvalidListener sessionInvalidListener,boolean refresh,final boolean getClass){
		
		if( refresh == true ){
			mGroups.clear();
			mClassGroups.clear();
		}
		
		String urlString = UrlUtils.getGroupListUrl();	 
		if( mGroups != null && mGroups.size() > 0 ){
			 if( listener != null ){
				 if( getClass == true ){
					 listener.onResponse(mClassGroups);
				 }else{
					 listener.onResponse(mGroups);
				 }
        	 }
			 
			 return;
		}
   
    	 CookieStringRequest request = new CookieStringRequest(Request.Method.POST, urlString,
         		new Response.Listener<String>() {  
                     @Override  
                     public void onResponse(String response) {  
                         Log.d(TAG, "response="+response);  
                         if( activity == null || activity.isDestroyed() == true ){
                        	 return;
                         }
                         JSONObject obj = null;
                         try {
							obj = new JSONObject(response);
                         } catch (JSONException e) {
                         }
                         JSONArray jsonArray = null;
                         try {
							jsonArray = new JSONArray(response);
                         } catch (JSONException e) {
                        	 
                         }
                         if( obj != null ){
                        	 if( obj.optInt("code") == -2 ){
                        		InfoReleaseApplication.returnToLogin(activity);
                        		if( sessionInvalidListener != null ){
                        			sessionInvalidListener.onSessionInvalidResponse();
                        		}
                        	 }else{
                        		 if( errorListener != null ){
         							errorListener.onErrorResponse(null);
                            	 }
                        	 }
                         }else{
                        	 mGroups.clear();
                        	 mClassGroups.clear();
                        	 parseGroupList(jsonArray);  
                        	 if( listener != null ){
                        		 if( getClass == true ){
                        			 listener.onResponse(mClassGroups);
                        		 }else{
                        			 listener.onResponse(mGroups);
                        		 }
                        	 }
                         }
                     }
                 },   
                 new Response.ErrorListener() {  
                     @Override  
                     public void onErrorResponse(VolleyError arg0) {  
                     	Log.e(TAG, "sorry,Error arg0:"+arg0); 
                     	if( activity == null || activity.isDestroyed() == true ){
                       	 return;
                        }
                    	//先判断网络状况
						InfoReleaseApplication.showNetWorkFailed(activity);
	
						if( errorListener != null ){
							errorListener.onErrorResponse(arg0);
                   	 	}
                     }  
                 });  
    	 request.setCookie(HttpClientDownloader.getInstance().getCookie());
         InfoReleaseApplication.requestQueue.add(request); 
	}
}
