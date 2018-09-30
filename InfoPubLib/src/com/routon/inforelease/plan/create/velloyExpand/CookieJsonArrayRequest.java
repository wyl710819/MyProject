package com.routon.inforelease.plan.create.velloyExpand;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.routon.inforelease.HttpClientDownloader;

public class CookieJsonArrayRequest extends JsonArrayRequest {
	private Map mHeaders = new HashMap();

	public CookieJsonArrayRequest(String url, Listener<JSONArray> listener, ErrorListener errorListener) {
		super(url, listener, errorListener);
		// TODO Auto-generated constructor stub
		setRetryPolicy(new DefaultRetryPolicy(
				HttpClientDownloader.TIMEOUT, 1 , 1
		));
//		setRetryPolicy(new RetryPolicy() {
//
//			@Override
//			public void retry(VolleyError error) throws VolleyError {
//				// Toast.makeText(PictureAddActivity.this, "网络连接失败!",
//				// Toast.LENGTH_LONG).show();
//			}
//
//			@Override
//			public int getCurrentTimeout() {
//				// TODO Auto-generated method stub
//				return 20 * 1000;
//			}
//
//			@Override
//			public int getCurrentRetryCount() {
//				// TODO Auto-generated method stub
//				return 2;
//			}
//		});
	}

	public void setCookie(String cookie) {
		mHeaders.put("Cookie", cookie);
	}

	@Override
	public Map getHeaders() throws AuthFailureError {
		return mHeaders;
	}

}
