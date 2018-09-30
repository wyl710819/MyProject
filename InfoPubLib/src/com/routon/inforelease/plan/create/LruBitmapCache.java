package com.routon.inforelease.plan.create;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.util.ImageUtils;

public class LruBitmapCache implements ImageCache {
	private static final String TAG = "LruBitmapCache";
	
	private LruCache<String, Bitmap> mMemoryCache;
	
	public static Bitmap cacheBitmap;

	public LruBitmapCache() {
		mMemoryCache = new LruCache<String, Bitmap>(InfoReleaseApplication.memoryCacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}

	@Override
	public Bitmap getBitmap(String url) {
		Log.v(TAG, "get cache bitmap url: " + url);
		//因为volley会给Url加上一个头，这里做一个替换以获得正确地址
        String key = url.replaceFirst("#W[0-9]*#H[0-9]*", "");
        Log.v(TAG, "key: " + key);
        if (key.startsWith("file://")) {
        	return ImageUtils.decodeThumbFile(key);
        }
		return mMemoryCache.get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		mMemoryCache.put(url, bitmap);
	}

}
