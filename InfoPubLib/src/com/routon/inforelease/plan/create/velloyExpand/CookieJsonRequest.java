package com.routon.inforelease.plan.create.velloyExpand;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;

import com.routon.widgets.Toast;

public class CookieJsonRequest extends JsonRequest<JSONObject>{
	
	private static final String TAG = "CookieJsonRequest";
	
	private Map mHeaders = new HashMap();
	//private Map<String, String> params = null;
	private List<NameValuePair> params;
	private static final String PROTOCOL_CHARSET = HTTP.UTF_8;
	
	public CookieJsonRequest(int method, String url, List<NameValuePair> params,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, "", listener, errorListener);
		this.params = params;
		
		setRetryPolicy(new DefaultRetryPolicy(
				HttpClientDownloader.TIMEOUT, 1 , 1
        		));
//		setRetryPolicy(new RetryPolicy() {
//			
//			@Override
//			public void retry(VolleyError error) throws VolleyError {
////				Toast.makeText(PictureAddActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
//			}
//			
//			@Override
//			public int getCurrentTimeout() {
//				// TODO Auto-generated method stub
//				return 20*1000;
//			}
//			
//			@Override
//			public int getCurrentRetryCount() {
//				// TODO Auto-generated method stub
//				return 2;
//			}
//		});
	}
	
	public void setCookie(String cookie){
		mHeaders.put("Cookie", cookie);
	}
	
	@Override
	public Map getHeaders() throws AuthFailureError{
		return mHeaders;
	}
	
	@Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		try {
			JSONObject jsonObject = new JSONObject(new String(response.data, getParamsEncoding()));
			return Response.success(jsonObject, HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (Exception je) {
			return Response.error(new ParseError(je));
		}
	}
	
	@Override
	public String getBodyContentType() {
		return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
	}

	/**
     * Returns the raw POST or PUT body to be sent.
     */
	@Override
    public byte[] getBody() {
//        if (params != null && params.size() > 0) {
//            return encodeParameters(params, PROTOCOL_CHARSET);
//        }
		if (params != null && params.size() > 0) {
			UrlEncodedFormEntity entity;
			try {
				entity = new UrlEncodedFormEntity(params, PROTOCOL_CHARSET);
				ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
				entity.writeTo(b);
				return b.toByteArray();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
        return null;
    }
    
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
//            if (encodedParams.length() > 0)
//            	encodedParams.delete(encodedParams.length() - 1, encodedParams.length());
//            Log.i(TAG, "---------------encodeParameters------------"+encodedParams.toString());
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }
}
