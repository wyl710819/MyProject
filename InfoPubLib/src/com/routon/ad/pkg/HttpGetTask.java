package com.routon.ad.pkg;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.util.Log;

public abstract class HttpGetTask implements Runnable {
	private static final String TAG = "HttpGetTask";
	
	public static final int ERR_NONE = 0;
	public static final int ERR_PARAM = 1;
	public static final int ERR_TIMEOUT = 2;
	public static final int ERR_TRANSFER = 3;
	public static final int ERR_OPEN = 4;
		
	protected String mUrl;
	
	private Object mContext;
	
	private int mConnectTimeOut = 5000; // default 5s
	
	private Map<String, String> mRequestHeaders;
	
	private boolean mIsRunning;
	
	private int mContentLength;
	
	private int mRecvLength;
	
	private String mSession;

	public HttpGetTask(String url) {
		mUrl = url;		
	}
	
	public HttpGetTask(String url, Object context) {
		mUrl = url;
		mContext = context;
	}
	
	public void setContext(Object context) {
		mContext = context;
	}
	
	public Object getContext() {
		return mContext;
	}
	
	public String getSession() {
		return mSession;
	}
	
	public void setConnectTimeOut(int ms) {
		if (ms > 0) {
			mConnectTimeOut = ms;
		}
	}
	
	public boolean isRunning() {
		return mIsRunning;
	}
	
	public void addRequestHeader(String key, String value) {
		if (key == null || value == null)
			return;
		
		if (mRequestHeaders == null)
			mRequestHeaders = new HashMap<String, String>();
		
		mRequestHeaders.put(key, value);
	}
	
	public int getProgress() {
		if (mContentLength > 0) {
			return mRecvLength * 100 / mContentLength;
		}
		
		return 0;
	}

	@Override
	public void run() {
		mIsRunning = true;
		URL url;
		
		Log.v(TAG, "run");
		notifyTaskStarted();

		try {
			Log.v(TAG, "begin request url: " + mUrl);
			url = new URL(mUrl);

		} catch (MalformedURLException e) {
			e.printStackTrace();
			notifyTaskFinished(ERR_PARAM);
			return;
		}

		HttpURLConnection urlConn = null;
		try {
			urlConn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			notifyTaskFinished(ERR_OPEN);
			return;
		}
		
		urlConn.setConnectTimeout(mConnectTimeOut);
		if (mRequestHeaders != null) {
			Set<Entry<String, String>> set = mRequestHeaders.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			while (it.hasNext()) {
				Entry<String, String> item = it.next();
				urlConn.setRequestProperty(item.getKey(), item.getValue());
			}
		}
		
		byte[] buffer = new byte[8192];
		int read_size = 0;
		int code = ERR_NONE;
		try {
			urlConn.connect();
			
			int responseCode = urlConn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				if (urlConn.getHeaderField("Set-Cookie") != null) {
					mSession = urlConn.getHeaderField("Set-Cookie");
				}
				
				InputStream is = null;
								
				mContentLength = urlConn.getContentLength();
				onBeginTransfer();
				
				try {
					is = urlConn.getInputStream();

					while (true) {
						read_size = is.read(buffer);
						if (read_size < 0)
							break;

						if (read_size > 0) {
							mRecvLength += read_size;
							onDataTransfer(buffer, read_size);	
						}
						
//						try {
//							Thread.sleep(100);
//						} catch (Exception e) {
//							
//						}
					}
					code = ERR_NONE;
					onEndTransfer(code);
				} catch (IOException e) {
					code = ERR_TRANSFER;
					onEndTransfer(code);
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (Exception e) {
							
						}
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			code = ERR_TRANSFER;
		}
		
		Log.v(TAG, "finish url: " + mUrl + " code: " + code);
		notifyTaskFinished(code);
	}
	
	protected abstract void onBeginTransfer();

	protected abstract void onDataTransfer(byte[] buffer, int read_size);
	
	protected abstract void onEndTransfer(int code);
	
	private OnHttpGetTaskListener mOnHttpGetTaskListener;
	
	protected void notifyTaskStarted() {
		if (mOnHttpGetTaskListener != null)
			mOnHttpGetTaskListener.onTaskStarted(this);
	}
	
	protected void notifyTaskFinished(int code) {
		mIsRunning = false;
		
		if (mOnHttpGetTaskListener != null)
			mOnHttpGetTaskListener.onTaskFinished(this, code);
	}
	
	public void setOnHttpGetTaskListener(OnHttpGetTaskListener listener) {
		mOnHttpGetTaskListener = listener;
	}
	
	// listener called in non-main thread
	public interface OnHttpGetTaskListener {
		void onTaskStarted(HttpGetTask task);
		void onTaskFinished(HttpGetTask task, int code);
	}
	
	private OnTaskListener mOnTaskListener;
	public void setOnTaskListener(OnTaskListener listener) {
		mOnTaskListener = listener;
	}
	
	public void notifyTaskBegin() {
		if (mOnTaskListener != null)
			mOnTaskListener.onTaskBegin(this);
	}
	
	public void notifyTaskEnd(int code) {
		if (mOnTaskListener != null)
			mOnTaskListener.onTaskEnd(this, code);
	}
	
	// listener called in main thread
	public interface OnTaskListener {
		void onTaskBegin(HttpGetTask task);
		void onTaskEnd(HttpGetTask task, int code);
	}
}
