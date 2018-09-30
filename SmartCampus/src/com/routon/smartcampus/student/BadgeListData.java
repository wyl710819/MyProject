package com.routon.smartcampus.student;

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
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.edurelease.R;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

public class BadgeListData {
	private static final String TAG = "BadgeListData";
	public static void getBadgesListData(final Activity activity,final DataResponse.Listener<ArrayList<Badge>> listener,
			final DataResponse.ErrorListener errorListener,
			final DataResponse.SessionInvalidListener sessionInvalidListener){
		String urlString = SmartCampusUrlUtils.getBadgeListUrl();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
	                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  				

						@Override  
	                    public void onResponse(JSONObject response) {  
	                        Log.d(TAG, "response="+response);                                   
							try {
								if(response.getInt("code") == 0){
//									flowerTypes = new ArrayList<BadgeType>();
									ArrayList<Badge> flowersList = new ArrayList<Badge>();
									
									JSONArray array = response.getJSONArray("datas");
									int len = array.length();
									for(int i = 0; i< len; i++){
										JSONObject obj = (JSONObject) array.get(i);
										Badge flower = new Badge(obj);
										flowersList.add(flower);																	
									}
									if( flowersList.size() == 0 ){
										Toast.makeText(activity, R.string.get_badge_data_empty, Toast.LENGTH_LONG).show();
									}
									if( listener != null ){
		                        		listener.onResponse(flowersList);
		                        	}
									
//									if (flowersList.size()>0) {
//										badgeTypeNameList = new ArrayList<String>();
//										badgeTypeNameList.add("全部");
//										badgeTypeIdList = new ArrayList<Integer>();
//										badgeTypeIdList.add(0);
//										flowerTypes = BadgeType.filterBadgeTypesFromBadges(flowersList);
//										for(BadgeType badgeType: flowerTypes){
//											badgeTypeIdList.add(badgeType.id);
//											badgeTypeNameList.add(badgeType.name);
//										}
//										
//										badgeTypeListView.setAdapter(new ClassSelListViewAdapter(getContext(), badgeTypeNameList));
//										badgeText.setText("全部");
//									}
//									
//									getStudentListData(classGroups.get(0).getId(),0,0);
									
		                        }else if(response.getInt("code") == -2){
		                        	InfoReleaseApplication.returnToLogin(activity);
		                        	if( sessionInvalidListener != null ){
	                        			sessionInvalidListener.onSessionInvalidResponse();
	                        		}
		                        }else{//失败
		                        	Log.e(TAG, response.getString("msg"));  
		                        	Toast.makeText(activity, response.getString("msg"), Toast.LENGTH_LONG).show();
		                        	if( errorListener != null ){
		    							errorListener.onErrorResponse(null);
		                       	 	}  	    						
		                        }

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                        
	                    }  
	                },   
	                new Response.ErrorListener() {  
	                    @Override  
	                    public void onErrorResponse(VolleyError arg0) {  
	                    	Log.e(TAG, "sorry,Error"); 
	                    	//先判断网络状况
							if( true == InfoReleaseApplication.showNetWorkFailed(activity) ){
								Toast.makeText(activity, R.string.get_badge_data_failed, Toast.LENGTH_LONG).show();
							}
							if( errorListener != null ){
    							errorListener.onErrorResponse(arg0);
                       	 	}

	                    }  
	                });  
	        
	        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
	        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 
	}
}
