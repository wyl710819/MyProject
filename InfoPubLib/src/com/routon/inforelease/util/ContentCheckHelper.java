package com.routon.inforelease.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.widgets.Toast;

public class ContentCheckHelper {
	public static void showContentListDialog(final Context context,ArrayList<String> contentlist,DialogInterface.OnClickListener listener){
	    final String[] items = new String[contentlist.size()];
	    contentlist.toArray(items);
	    AlertDialog.Builder listDialog = 
	        new AlertDialog.Builder(context);
	    listDialog.setTitle("请选择审核意见");
	    listDialog.setItems(items, listener);
	    listDialog.show();
	}
	
	public static void getContentCheckList(final Context context,final DataResponse.Listener<ArrayList<String>> listener,
			final DataResponse.ErrorListener errorListener){	
		String urlString = UrlUtils.getContentCheckListUrl();
		final ArrayList<String> contents = new ArrayList<String>();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
	                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
	                    @Override  
	                    public void onResponse(JSONObject response) {  
	                        Log.d("ContentCheckHelper", "response="+response);  
	                        int code = response.optInt("code");	     
	                        if( code == 0 ){
	                        	JSONArray datas = response.optJSONArray("datas");
	                        	if( datas != null ){
	                        		for( int i = 0; i < datas.length(); i++ ){
	                        			JSONObject itemData = datas.optJSONObject(i);
	                        			if(itemData != null ){
	                        				String content = itemData.optString("content");
	                        				contents.add(content);
	                        			}
	                        		}
	                        		listener.onResponse(contents);
	                        	}
	                        }else{
	                        	Toast.makeText(context, response.optString("msg"), Toast.LENGTH_LONG).show();
	                        	errorListener.onErrorResponse(null);
	                        }
	                    }  
	                },   
	                new Response.ErrorListener() {  
	                    @Override  
	                    public void onErrorResponse(VolleyError arg0) {  
	                    	Log.e("ContentCheckHelper", "sorry,Error"); 
	                    	
	                    	if(InfoReleaseApplication.showNetWorkFailed(context) == true){
	                    		Toast.makeText(context, "获取数据失败!", Toast.LENGTH_LONG).show();
	                    	}
	                    	errorListener.onErrorResponse(null);
	                    }  
	                });  
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
	    InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
}
