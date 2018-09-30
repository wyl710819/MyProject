package com.routon.smartcampus.utils;

import java.io.File;

import android.content.Context;
import android.util.TypedValue;
import android.widget.ImageView;

import com.routon.edurelease.R;
import com.squareup.picasso.Picasso;

public class StudentHelper {
	public static void loadStudentImage(String localImageSavePath,String imageUrl,Context context,ImageView imageview){
		if( context == null || imageview == null ){
			return;
		}
		boolean load = false;
		if ( localImageSavePath != null ) {
			File imageFile = new File(localImageSavePath);
			if (imageFile.exists() == true) {
				Picasso.with(context).load("file://"+localImageSavePath)
				.placeholder(R.drawable.default_student).fit().into(imageview);
				load = true;
			} 
		}
		
		if( load == false ) {
			if(  imageUrl != null && imageUrl.isEmpty() == false ){
				Picasso.with(context).load(imageUrl)
				.placeholder(R.drawable.default_student).fit().into(imageview);
			}else{
				imageview.setImageResource(R.drawable.default_student);
			}
		}
	}
	
	public static int dp2px(Context context, int dp){
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (density*dp+0.5f);
	}
	
	public static int sp2px(Context context, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                value, context.getResources().getDisplayMetrics());
    }
}
