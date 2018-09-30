package com.routon.smartcampus.utils;

import java.io.File;
import java.io.FileOutputStream;
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
import com.routon.inforelease.plan.MaterialParams;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStack;
import com.routon.inforelease.plan.create.velloyExpand.MultiPartStringRequest;
import com.routon.smartcampus.flower.RemarkPictureMaterialBean;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

public class ImgUploadUtil {
	private static List<Integer> pictureMaterialIds;
	private static Map<String, File> fileMapList;
	private static List<String> sendImageList;
	private static UploadImgListener mUploadImgListener;
	private static Context mContext;

	public static void uploadImg(Context context,List<String> images, UploadImgListener uploadImgListener) {
		mContext=context;
		mUploadImgListener = uploadImgListener;
		String urlString = UrlUtils.getResourceUploadUrl();

		fileMapList = new HashMap<String, File>();

		Map<String, String> params = new HashMap<String, String>();

		params.put("fileType", Integer.toString(MaterialParams.TYPE_BADGE_REMARK));

		sendImageList = new ArrayList<String>();
		for (int i = 0; i < images.size() - 2; i++) {
			sendImageList.add(images.get(i));
			fileMapList.put("file" + (i + 1), getimageFile(images.get(i)));
		}

		UploadFiles(urlString, fileMapList, params, mResonseListenerString, mErrorListener, null);

	}
	
	public static void uploadPic(Context context,List<String> images, UploadImgListener uploadImgListener) {
		mContext=context;
		mUploadImgListener = uploadImgListener;
		String urlString = UrlUtils.getResourceUploadUrl();

		fileMapList = new HashMap<String, File>();

		Map<String, String> params = new HashMap<String, String>();

		params.put("fileType", Integer.toString(MaterialParams.TYPE_BADGE_REMARK));

		sendImageList = new ArrayList<String>();
		for (int i = 0; i < images.size(); i++) {
			sendImageList.add(images.get(i));
			fileMapList.put("file" + (i + 1), getimageFile(images.get(i)));
		}

		UploadFiles(urlString, fileMapList, params, mResonseListenerString, mErrorListener, null);

	}
	

	private static final String exteranlPath = "/smartRelease_photos/";

	private static String getExternalStorageDirectory() {
		return Environment.getExternalStorageDirectory() + exteranlPath;
	}

	private static File getimageFile(String srcPath) {

		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为H空

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float ww = 1080;
		float hh = 1920;
		int dw = 1080;
		int dh = 1920;
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int resampleSize = 1;// be=1表示不缩放
		if (w > 1080 || h > 1920) {// 如果宽度大的话根据宽度固定大小缩放
			float sw = newOpts.outWidth / ww;
			float sh = newOpts.outHeight / hh;
			if (sw > sh) {
				dh = (int) (newOpts.outHeight / sw);
				resampleSize = (int) sw;
			} else {
				dw = (int) (newOpts.outWidth / sh);
				resampleSize = (int) sh;
			}
		}

		String filename = new File(srcPath).getName();

		String smartRelease = getExternalStorageDirectory();
		File dir = new File(smartRelease);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String out_file_path = getExternalStorageDirectory() + filename;

		newOpts.inSampleSize = resampleSize;// 设置缩放比例
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		w = newOpts.outWidth;
		h = newOpts.outHeight;
		dw = 1080;
		dh = 1920;
		if (w > 1080 || h > 1920) {// 如果宽度大的话根据宽度固定大小缩放
			float sw = newOpts.outWidth / ww;
			float sh = newOpts.outHeight / hh;
			if (sw > sh) {
				dw = 1080;
				dh = (int) (newOpts.outHeight / sw);
			} else {
				dw = (int) (newOpts.outWidth / sh);
				dh = 1920;
			}
		} else {
			dw = newOpts.outWidth;
			dh = newOpts.outHeight;
		}
		Bitmap new_bitmap = zoomBitmap(bitmap, dw, dh);
		// bitmap.recycle();
		// compressImage(bitmap);//压缩好比例大小后再进行质量压缩
		// Log.i(TAG, "--------------out_put_file:"+out_file_path);
		// 保存入sdCard
		File file2 = new File(out_file_path);
		
		if (new_bitmap!=null) {
			
			try {
				FileOutputStream out = new FileOutputStream(file2);
				CompressFormat format;
				if (filename.endsWith("png") || filename.endsWith("PNG")) {
					format = CompressFormat.PNG;
				} else {
					format = CompressFormat.JPEG;
				}
				if (new_bitmap.compress(format, 100, out)) {
					out.flush();
					out.close();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			new_bitmap.recycle();
			bitmap.recycle();
		}else {
			isImgError = true;
		}

		return file2;
	}

	private static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		if (bitmap==null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}

	private static Listener<String> mResonseListenerString = new Listener<String>() {

		@Override
		public void onResponse(String response) {
			// if ( progressDialog != null &&progressDialog.isShowing() ) {
			// progressDialog.dismiss();
			// }

			try {
				JSONObject jsonObj = new JSONObject(response);
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
			if (isImgError) {
				mUploadImgListener.uploadImgErrorListener("包含异常图片,上传失败!");
			}else {
				mUploadImgListener.uploadImgErrorListener("网络连接失败!");
			}
			
		}
	};
	private static boolean isImgError=false;

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
				
				if (isImgError) {
					mUploadImgListener.uploadImgErrorListener("包含异常图片,上传失败!");
				}else {
					mUploadImgListener.uploadImgErrorListener("网络连接失败!");
				}
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
		Log.d("RecordUploadUtil","cookie:"+HttpClientDownloader.getInstance().getCookie());
		multiPartRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		mSingleQueue.add(multiPartRequest);
	}

	public static void uploadImgs(Context context,List<String> images, UploadImgListener uploadImgListener) {
		mContext=context;
		mUploadImgListener = uploadImgListener;
		String urlString = UrlUtils.getResourceUploadUrl();

		fileMapList = new HashMap<String, File>();

		Map<String, String> params = new HashMap<String, String>();

		params.put("fileType", Integer.toString(MaterialParams.TYPE_HOMEWORK_PICTURE));

		sendImageList = new ArrayList<String>();
		for (int i = 0; i < images.size(); i++) {
			sendImageList.add(images.get(i));
			fileMapList.put("file" + (i + 1), getimageFile(images.get(i)));
		}

		UploadFiles(urlString, fileMapList, params, mResonseListenerString, mErrorListener, null);

	}
	
	public static void uploadNotifyImgs(Context context,List<String> images, UploadImgListener uploadImgListener) {
		mContext=context;
		mUploadImgListener = uploadImgListener;
		String urlString = UrlUtils.getResourceUploadUrl();

		fileMapList = new HashMap<String, File>();

		Map<String, String> params = new HashMap<String, String>();

		params.put("fileType", Integer.toString(MaterialParams.TYPE_NOTIFY_PICTURE));

		sendImageList = new ArrayList<String>();
		for (int i = 0; i < images.size(); i++) {
			sendImageList.add(images.get(i));
			fileMapList.put("file" + (i + 1), getimageFile(images.get(i)));
		}

		UploadFiles(urlString, fileMapList, params, mResonseListenerString, mErrorListener, null);

	}
}
