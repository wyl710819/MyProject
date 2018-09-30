package com.routon.inforelease.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;

public class ImageViewHelper {
	public static void setSpecialBitmap(ImageView imageview,Bitmap bitmap,LayoutParams defaultParams){
		DisplayMetrics  dm = new DisplayMetrics();    
		((Activity) imageview.getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm); 
		int screenWidth = dm.widthPixels;
//		RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//				LayoutParams.MATCH_PARENT);
		if (bitmap!=null) {			
			imageview.setImageBitmap(bitmap);
			float num= (float)bitmap.getWidth()/bitmap.getHeight(); 
			if (bitmap.getHeight()>bitmap.getWidth()) {
				RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						screenWidth*bitmap.getHeight()/bitmap.getWidth());
				imageview.setLayoutParams(imgParams);
				imageview.setScaleType(ScaleType.FIT_CENTER);
			}else if ( num < 1.2) {
				RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						screenWidth*bitmap.getWidth()/bitmap.getHeight());
				imageview.setLayoutParams(imgParams);
				imageview.setScaleType(ScaleType.FIT_CENTER);
			}else{
				imageview.setLayoutParams(defaultParams);
			}
		}else {
			imageview.setLayoutParams(defaultParams);
		}
	}
}
