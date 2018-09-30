package com.routon.smartcampus.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.Volley;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStack;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStringRequest;
import com.routon.smartcampus.flower.RemarkPictureMaterialBean;

import android.app.Activity;
import android.content.Context;
import android.util.Log;


public class RecordUploadUtil {
	private static List<Integer> pictureMaterialIds;
	private static Map<String, File> fileMapList;
	private static List<String> sendImageList;
	private static UploadImgListener mUploadImgListener;
	private static Context mContext;

	public static void uploadRecord(Context context,List<String> records, UploadImgListener uploadImgListener) {
		mContext=context;
		mUploadImgListener = uploadImgListener;
		String urlString = UrlUtils.getResourceUploadUrl();

		fileMapList = new HashMap<String, File>();

		Map<String, String> params = new HashMap<String, String>();

		params.put("fileType", Integer.toString(172));

		sendImageList = new ArrayList<String>();
		for (int i = 0; i < records.size(); i++) {
			sendImageList.add(records.get(i));
			fileMapList.put("file" + (i + 1), new File(records.get(i)));
		}

		UploadFiles(urlString, fileMapList, params, mResonseListenerString, mErrorListener, null);

	}


	private static Listener<String> mResonseListenerString = new Listener<String>() {

		@Override
		public void onResponse(String response) {
			// if ( progressDialog != null &&progressDialog.isShowing() ) {
			// progressDialog.dismiss();
			// }

			try {
				JSONObject jsonObj = new JSONObject(response);
				Log.d("RecordUploadUtil",response);
				if (jsonObj.optInt("code") == 0) { // 返回成功
					
					// 获取上传成功的素材信息，取出素材id
					JSONArray array = jsonObj.getJSONArray("obj");
					pictureMaterialIds = new ArrayList<Integer>();
					List<RemarkPictureMaterialBean> pictureMaterialList = new ArrayList<RemarkPictureMaterialBean>();
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = (JSONObject) array.get(i);
						RemarkPictureMaterialBean remarkPictureMaterialBean = new RemarkPictureMaterialBean(obj);
						pictureMaterialList.add(remarkPictureMaterialBean);
						pictureMaterialIds.add(0);
					}

					if (pictureMaterialList != null && pictureMaterialList.size() > 0) {
						for (int j = 0; j < pictureMaterialList.size(); j++) {
							int imgFileKay = Integer.valueOf(pictureMaterialList.get(j).formFileKey.substring(4,
									pictureMaterialList.get(j).formFileKey.length()));
							pictureMaterialIds.set(imgFileKay - 1, pictureMaterialList.get(j).fileId);
						}

					}
					mUploadImgListener.uploadImgSuccessListener(pictureMaterialIds);
					// awardBadge();

				} else if( jsonObj.optInt("code") == -2 && mContext instanceof Activity ){// 失败
					InfoReleaseApplication.returnToLogin((Activity) mContext);
					mUploadImgListener.uploadImgErrorListener(jsonObj.getString("msg"));
				}else{
					mUploadImgListener.uploadImgErrorListener(jsonObj.getString("msg"));
				}
				//
				for (String key : fileMapList.keySet()) {
					if (fileMapList.get(key).exists()) {
						fileMapList.get(key).delete();
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	};

	private static ErrorListener mErrorListener = new ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError error) {
			error.printStackTrace();
			Log.d("error","错误"+error.toString());
			mUploadImgListener.uploadImgErrorListener("语音上传失败!");
		}
	};

	private static void UploadFiles(final String url, final Map<String, File> files, final Map<String, String> params,
			final Listener<String> responseListener, final ErrorListener errorListener, final Object tag) {
		if (null == url || null == responseListener) {
			return;
		}

		RequestQueue mSingleQueue = Volley.newRequestQueue(mContext, new MultiPartStack());
		MultiPartStringRequest multiPartRequest = new MultiPartStringRequest(Request.Method.POST, url, responseListener,
				errorListener) {

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
				mUploadImgListener.uploadImgErrorListener("网络连接失败!");
			}

			@Override
			public int getCurrentTimeout() {
				return 20 * 1000;
			}

			@Override
			public int getCurrentRetryCount() {
				return 2;
			}
		});
		Log.d("ImgUploadUtil","cookie:"+HttpClientDownloader.getInstance().getCookie());
		multiPartRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		mSingleQueue.add(multiPartRequest);
	}

}
