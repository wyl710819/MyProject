package com.routon.smartcampus.utils;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class XMLRequest extends Request<XmlPullParser> {

	private final Listener<XmlPullParser> mListener;
	private Map mHeaders = new HashMap();

	public XMLRequest(int method, String url, Listener<XmlPullParser> listener,
			ErrorListener errorListener) {
		super(method, url, errorListener);
		mListener = listener;
	}

	public XMLRequest(String url, Listener<XmlPullParser> listener, ErrorListener errorListener) {
		this(Method.GET, url, listener, errorListener);
	}
	
	public void setCookie(String cookie){
		mHeaders.put("Cookie", cookie);
	}
	
	@Override
	public Map getHeaders() throws AuthFailureError{
		return mHeaders;
	}

	@Override
	protected Response<XmlPullParser> parseNetworkResponse(NetworkResponse response) {
		try {
			String xmlString = new String(response.data);
//			Log.d("xmlrequest","parseNetworkResponse:"+xmlString);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xmlPullParser = factory.newPullParser();
			xmlPullParser.setInput(new StringReader(xmlString));
			return Response.success(xmlPullParser, HttpHeaderParser.parseCacheHeaders(response));
		} catch (XmlPullParserException e) {
			return Response.error(new ParseError(e));
		}
	}

	@Override
	protected void deliverResponse(XmlPullParser response) {
		mListener.onResponse(response);
	}

}
