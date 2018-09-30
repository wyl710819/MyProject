package com.routon.ad.pkg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HttpPostTask implements Runnable {
	private static final String TAG = "HttpPostTask";
	
	protected String mUrl;
	protected Object mContext;
	protected int mConnectTimeOut;

	private String mFileType;
	private String mFileName;

	public HttpPostTask(String url, Object context,
			String fileType, String fileName, OnHttpPostTaskListener listener) {
		Log.v(TAG, "new HttpPostTask: " + fileName);
		mUrl = url;
		mContext = context;
		this.mOnHttpPostTaskListener = listener;
		
		this.mFileType = fileType;
		this.mFileName = fileName;
	}

	@Override
	public void run() {

		execPostRequest(mUrl);
	}
	
	private void execPostRequest(String url) {
		Log.v(TAG, "post url: " + url);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpUriRequest httpMethod = new HttpPost(url);
		MultipartEntityBuilder meb = MultipartEntityBuilder.create();
		
		//MultipartEntity reqEntity = new MultipartEntity();

//		FileBody bin = new FileBody(new File(mFileName));		
//		meb.addPart(mFileType, bin);
		//((HttpPost) httpMethod).setEntity(reqEntity);
		meb.addBinaryBody("filename", new File(mFileName));
		((HttpPost) httpMethod).setEntity(meb.build());

		httpMethod.setHeader("Accept-Encoding", "gzip");
		try {
			HttpResponse httpResponse = httpClient.execute(httpMethod);
			
			int httpAckCode = httpResponse.getStatusLine().getStatusCode();
			Log.v(TAG, "code:" + httpAckCode + " url:" + url);
			switch (httpAckCode) {
			case 200:
			default:
				InputStream is = null;
				try {
					is = httpResponse.getEntity().getContent();
					byte[] buffer = new byte[8192];
					int count = 0;
					while ((count = is.read(buffer)) != -1) {
//						fos.write(buffer, 0, count);
					}
				} catch (Exception e) {
					Log.e(TAG, "Exception1: " + e.getMessage());
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (Exception e) {
						}
					}
				}
				break;
			}
			notifyFinished(0);
		} catch (ConnectTimeoutException e) {
			Log.e(TAG, "ConnectTimeoutException: " + e.getMessage());
			notifyFinished(1);
		} catch (ConnectException e) {
			Log.e(TAG, "ConnectException: " + e.getMessage());
			e.printStackTrace();
			notifyFinished(2);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "IOException: " + e.getMessage());
			notifyFinished(3);
		} catch (Exception e) {
			Log.e(TAG, "Exception4: " + e.getMessage());
			notifyFinished(4);
		}
	}

//	private void execPostRequest(String addr) {
//		URL url;
//
//		try {
//			url = new URL(addr);
//		} catch (MalformedURLException e) {			
//			return;
//		}
//
//		HttpURLConnection urlConn = null;
//		try {
//			urlConn = (HttpURLConnection) url.openConnection();
//		} catch (IOException e) {			
//			return;
//		}
//
//		try {
//			urlConn.setConnectTimeout(mConnectTimeOut);
//			
//			String BOUNDARY = "fPOYkzzvAFyrs0az-3ZTeh1gtCXJLzKPM52p"; // 定义数据分隔线
//			
//			// 发送POST请求必须设置如下两行
//			urlConn.setDoOutput(true);
//			urlConn.setDoInput(true);
//			urlConn.setUseCaches(false);
//			urlConn.setRequestMethod("POST");
//			urlConn.setRequestProperty("connection", "Keep-Alive");
//			urlConn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
//			urlConn.setRequestProperty("Charsert", "UTF-8");
//			
//			if (mFileType != null) {
//				urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
//			
//				StringBuilder sb = new StringBuilder();
//				//sb.append("First boundary: ");
//				sb.append("--");
//				sb.append(BOUNDARY);
//				sb.append("\r\n");
//				sb.append("Content-Disposition: form-data; name=\"xml\"; filename=\"" + getFileName() + "\"\r\n");
//				sb.append("Content-Type: application/octet-stream\r\n");
//				sb.append("Content-Transfer-Encoding: binary\r\n");
//				sb.append("\r\n");
//				byte[] data = sb.toString().getBytes();
//				OutputStream out = new DataOutputStream(urlConn.getOutputStream());
//				out.write(data);
//				writeData(out);
//				
//				sb = new StringBuilder();
//				//sb.append("Last boundary: ");
//				sb.append("\r\n");
//				sb.append("--");
//				sb.append(BOUNDARY);
//				sb.append("--");
//				sb.append("\r\n");
//				data = sb.toString().getBytes();
//				out.write(data);
//				out.flush();
//				out.close();
//			}
//			else {
//				urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//				
//				OutputStream out = new DataOutputStream(urlConn.getOutputStream());
//				writeData(out);
//				out.flush();
//				out.close();
//			}
//
//			int responseCode = urlConn.getResponseCode();
//			
//			if (responseCode == HttpURLConnection.HTTP_OK) {
//				InputStream is = urlConn.getInputStream();
//				
//				mContentLength = urlConn.getContentLength();
//				processInput(mResponse, is);
//
//				is.close();
//			} 
//		} catch (SocketTimeoutException e) {
//			
//		} catch (Exception e) { 
//			
//		} finally {
//			urlConn.disconnect();			
//		}
//	}
//	
	private String getFileName() {
		return mFileName != null ? mFileName : "post.txt";
	}
	
//	protected void processInput(HttpResponse mResponse, InputStream is) {
//		
//		if (mResponseProcessor != null) {
//			updateReadSize(0);
//			mResponse.result = mResponseProcessor.onProcessResponse(this, is);
//		}
//		else {
//			try {
//				ByteArrayOutputStream bos = new ByteArrayOutputStream(4000);
//				byte[] buffer = new byte[8192];
//				while (true) {
//					int read_size = is.read(buffer);
//					if (read_size <= 0)
//						break;
//					
//					bos.write(buffer, 0, read_size);
//					updateReadSize(bos.size());
//				}
//				bos.close();
//				
//				mResponse.result = bos.toByteArray();
//				
//			} catch (Exception e) {
//				mResponse.code = HttpResponse.ERR_TRANSFER_DATA;
//			}
//		}
//	}
	
//	protected void writeData(OutputStream os) {
//		byte[] data = mData;
//		
//		try {
//			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
//			int bytes = 0;
//			byte[] bufferOut = new byte[1024];
//		
//			while ((bytes = in.read(bufferOut)) != -1) {
//				os.write(bufferOut, 0, bytes);
//			}
//			in.close();
//		} catch (Exception e) {			
//			e.printStackTrace();
//		}
//	}
	
	private OnHttpPostTaskListener mOnHttpPostTaskListener;
	
	private void notifyFinished(int errcode) {
		if (mOnHttpPostTaskListener != null) {
			mOnHttpPostTaskListener.onTaskFinished(this, errcode);
		}
	}
	
	public interface OnHttpPostTaskListener {
		void onTaskFinished(HttpPostTask task, int code);
	}

}
