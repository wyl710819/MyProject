package com.routon.inforelease.widget;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.widgets.Toast;

public class LoginHelper {
	public static void getVertifyNum(final Context context,String phone,final CountdownButton button){
		String url = UrlUtils.getVertifyUrl(phone);
		button.setEnabled(false);
		Log.d("getVertifyNum","url:"+url);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						button.setEnabled(true);
						int code = response.optInt("code",-1);
						Log.d("","response:"+response);
						if ( code == 0) {	
							int length = response.optInt("valid",120);
							button.setLength(length*1000);
							button.start();
						} else {
							Toast.makeText(context, response.optString("msg"), Toast.LENGTH_LONG).show();
							Toast.makeText(context, "获取验证码失败", Toast.LENGTH_LONG).show(50);
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {			
						//先判断网络状况
						button.setEnabled(true);
						InfoReleaseApplication.showNetWorkFailed(context);
						Toast.makeText(context, "获取验证码失败", Toast.LENGTH_LONG).show(50);
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
}
