package com.routon.inforelease.plan.create.velloyExpand;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

public class CookieImageRequest extends ImageRequest{
	private Map mHeaders = new HashMap();
	
	public CookieImageRequest(String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight,
            Config decodeConfig, Response.ErrorListener errorListener) {
		super(url, listener, maxWidth, maxHeight, decodeConfig, errorListener);
		// TODO Auto-generated constructor stub
	}
	
	public void setCookie(String cookie){
		mHeaders.put("Cookie", cookie);
	}
	
	@Override
	public Map getHeaders() throws AuthFailureError{
		return mHeaders;
	}
}

