package com.routon.smartcampus.utils;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

public class FileRequest extends Request<String> {

    private final Response.Listener<String> mListener;
    
    public String mSavePath = null;
    private Map mHeaders = new HashMap();

    
    public FileRequest(String url, String savePath,Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        
        mSavePath =  savePath;
        mListener = listener;
    }
    
	public void setCookie(String cookie){
		mHeaders.put("Cookie", cookie);
	}
	
	@Override
	public Map getHeaders() throws AuthFailureError{
		return mHeaders;
	}

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
       
        try {
            if (response.data == null) {
                return Response.error(new ParseError(response));
            } else {
            	FileUtil.createFileWithByte(response.data, mSavePath);
                return Response.success(mSavePath, HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (OutOfMemoryError e) {
            VolleyLog.e("Caught OOM for %d byte image, url=%s", response.data.length, getUrl());
            return Response.error(new ParseError(e));
        }
    }


    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

}