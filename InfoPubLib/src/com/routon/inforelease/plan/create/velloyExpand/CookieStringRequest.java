package com.routon.inforelease.plan.create.velloyExpand;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import com.android.volley.toolbox.StringRequest;


public class CookieStringRequest extends StringRequest{
	
//	private static final String TAG = "CookieStringRequest";
	
	private Map mHeaders = new HashMap();
	
	public CookieStringRequest(int method, String url,
			Listener<String> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);
		
		setRetryPolicy(new DefaultRetryPolicy(
        		8000, 1 , 1
        		));
	}
	
	public void setCookie(String cookie){
		mHeaders.put("Cookie", cookie);
	}
	
	@Override
	public Map getHeaders() throws AuthFailureError{
		return mHeaders;
	}
	
	@Override
	public String getBodyContentType() {
		return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
	}
}
