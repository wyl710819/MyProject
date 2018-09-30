package com.routon.inforelease.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.content.Context;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Volley;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.classinfo.ClassInfoZipDownListener;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStack;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStringRequest;

public class NetWorkRequest {
	public static void UploadFiles(final Context mContext,final String url, final Map<String, File> files,
			final Map<String, String> params,
			final Listener<String> responseListener,
			final ErrorListener errorListener, final Object tag) {
		if (null == url || null == responseListener) {
			return;
		}

		RequestQueue singleQueue = Volley.newRequestQueue(mContext,
				new MultiPartStack());
		MultiPartStringRequest multiPartRequest = new MultiPartStringRequest(
				Request.Method.POST, url, responseListener, errorListener) {

			@Override
			public Map<String, File> getFileUploads() {
				return files;
			}

			@Override
			public Map<String, String> getStringUploads() {
				return params;
			}

		};

		multiPartRequest.setRetryPolicy(new RetryPolicy() {

			@Override
			public void retry(VolleyError error) throws VolleyError {
				Toast.makeText(mContext, "网络连接失败!",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public int getCurrentTimeout() {
				// TODO Auto-generated method stub
				return HttpClientDownloader.TIMEOUT;
			}

			@Override
			public int getCurrentRetryCount() {
				// TODO Auto-generated method stub
				return 2;
			}
		});
		multiPartRequest.setCookie(HttpClientDownloader.getInstance()
				.getCookie());
		singleQueue.add(multiPartRequest);
	}
	
	//下载模板编辑信息ZIP包
	public static void downloadZip(final Context context,final String editPkgUrl,final String fileName,final ClassInfoZipDownListener listener)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					URL url=new URL(editPkgUrl);
					HttpURLConnection conn=(HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(HttpClientDownloader.TIMEOUT);     //设置连接超时时间
					conn.setReadTimeout(HttpClientDownloader.TIMEOUT);
					FileOutputStream fOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
					InputStream inputStream=conn.getInputStream();
					byte[] buffer=new byte[1024];
					int hasRead=0;
					while((hasRead=inputStream.read(buffer))>0)
					{
						fOutputStream.write(buffer, 0, hasRead);
					}
					conn.disconnect();
					fOutputStream.close();
					inputStream.close();
					if(listener!=null)
						listener.onFinish();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					if(listener!=null)
						listener.onError();
				}
			}
		}).start();
	}
}
