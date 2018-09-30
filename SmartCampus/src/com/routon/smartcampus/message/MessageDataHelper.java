package com.routon.smartcampus.message;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.util.TimeUtils;
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

public class MessageDataHelper {
	public static final String  INFO = "messageinfo";
	private static final String  UPDATE_TIME= "messageUpdateTime";
	public static MessageData createMessageDate(JSONObject obj){
		MessageData data = new MessageData();
		data.buzId = obj.optInt("buzId");
		data.type = obj.optInt("type");
		data.title = obj.optString("title");
		data.content = obj.optString("content");
		data.channel = obj.optString("channel");
		data.time = obj.optString("time");
		return data;
	}
	
	public static String getUpdateTimeStr(){
		if( SmartCampusApplication.mStudentDatas != null && SmartCampusApplication.mStudentDatas.size() > 0 ){
			return "teacher_"+SmartCampusApplication.mStudentDatas.get(0).parentPhone+"_"+UPDATE_TIME;	
		}else if( InfoReleaseApplication.authenobjData != null ){
			return "parent_"+InfoReleaseApplication.authenobjData.userId+"_"+UPDATE_TIME;
		}
		return UPDATE_TIME;
	}
	
	public static boolean hasNewMessage(ArrayList<MessageData> datas){
		if( datas == null ){
			return false;
		}
		for( int i = 0; i < datas.size(); i++ ){
			if( datas.get(i).isNew == 1 ){
				return true;
			}
		}
		return false;
	}
	
	public static void getPushMsgListData(final Activity activity,final DataResponse.Listener<ArrayList<MessageData>> listener,
			final DataResponse.ErrorListener errorListener,
			final DataResponse.SessionInvalidListener sessionInvalidListener){
		String channel = "";
		int type = 0;
		//家长版
		if( SmartCampusApplication.mStudentDatas != null && SmartCampusApplication.mStudentDatas.size() > 0 ){
			for( int i = 0; i < SmartCampusApplication.mStudentDatas.size();i++){
				channel += SmartCampusApplication.mStudentDatas.get(i).groupId;
				channel += ",";
			}
			if( channel.endsWith(",") == false ){
				channel += ",";
			}
			channel += SmartCampusApplication.mStudentDatas.get(0).parentPhone;
			
		}else{//老师版只需要获取基于分组的消息
			type = 2;
			channel += InfoReleaseApplication.authenobjData.userName;
		}
		String urlString = SmartCampusUrlUtils.getPushMsgUrl(channel,type);
		Log.d("MessageData","getPushMsgListData urlString:"+urlString);
		String updateTimeTag = MessageDataHelper.getUpdateTimeStr();
		final String updateTimeStr = activity.getSharedPreferences(MessageDataHelper.INFO, Context.MODE_PRIVATE).getString(updateTimeTag, "");
		final Calendar updateTime =  TimeUtils.getFormatCalendar(updateTimeStr, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
	                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  				

						@Override  
	                    public void onResponse(JSONObject response) {  
	                        Log.d("MessageData", "response="+response);                                   
							try {
								if(response.getInt("code") == 0){
									ArrayList<MessageData> list = new ArrayList<MessageData>();
									JSONArray jsonArray = response.optJSONArray("datas");
									if(jsonArray != null ){
										int length = jsonArray.length();									
							            for(int i = 0; i<length; i++){
							             	JSONObject obj = jsonArray.getJSONObject(i);
							             	if( obj != null ){
							             		MessageData data = MessageDataHelper.createMessageDate(obj);
							             		Calendar time =  null;
							             	    if( data.time != null && data.time.isEmpty() == false ){
							             	    	  time = TimeUtils.getFormatCalendar(data.time, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
							             	     }
							             	     Log.d("MessageData", "getPushMsgListData data.time="+data.time);   
							             	    Log.d("MessageData", "getPushMsgListData updateTimeStr="+updateTimeStr);   
							             		 if( updateTime != null  &&  time != null ){
							             	    	  if( time.after(updateTime) ){
							             	    		  data.isNew = 1;
							             	    	  }
							             		 }else{
							             			 data.isNew = 1;
							             		 }
							             		list.add(data);							   
							             	}
							            }						           
									}		
									 listener.onResponse(list);
		                        }else if(response.getInt("code") == -2){
		                        	InfoReleaseApplication.returnToLogin(activity);
		                        	if( sessionInvalidListener != null ){
	                        			sessionInvalidListener.onSessionInvalidResponse();
	                        		}		      
		                        }else{//失败            
		                        	Toast.makeText(activity, response.getString("msg"), Toast.LENGTH_LONG).show();	     
		                        	if( errorListener != null ){
		         						errorListener.onErrorResponse(null);
		                            }
		                        }
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								if( errorListener != null ){
	         						errorListener.onErrorResponse(null);
	                            }
							}
	                        
	                    }  
	                },   
	                new Response.ErrorListener() {  
	                    @Override  
	                    public void onErrorResponse(VolleyError arg0) {  
	                    	//先判断网络状况
							InfoReleaseApplication.showNetWorkFailed(activity);
		
							if( errorListener != null ){
								errorListener.onErrorResponse(arg0);
	                   	 	}
	                    }  
	                });  
	        
	        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
	        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 
	}
}
