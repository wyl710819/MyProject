package com.routon.smartcampus.guide;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

public class GuideHelper {
    public static final String READ_GUIDE_INFO = "readGuide";
    public static final String READ_GUIDE_IMAGES_TAG = "images";
    public static final int PARENT_ROLE = 1;
    public static final int TEACHER_ROLE = 0;
    
//    public static String[] getImages(Context context){
//    	SharedPreferences pref = context.getSharedPreferences(READ_GUIDE_INFO, Context.MODE_PRIVATE);
//        String readImages = pref.getString(READ_GUIDE_IMAGES_TAG, "");
//        if( readImages.isEmpty() ){
//        	return null;
//        }
//        return readImages.split(";");
//    }
    
    public static void saveAddImages(Context context,String[] images){
    	SharedPreferences pref = context.getSharedPreferences(READ_GUIDE_INFO, Context.MODE_PRIVATE);
        String readImages = pref.getString(READ_GUIDE_IMAGES_TAG, "");
        
    	for( int i= 0; i < images.length; i++ ){   		
    		readImages += images[i];
    		readImages += ";";
    	}
    	pref.edit().putString(READ_GUIDE_IMAGES_TAG, readImages).commit();
    	
    }
    
    public static void saveImages(Context context,String[] images){
    	SharedPreferences pref = context.getSharedPreferences(READ_GUIDE_INFO, Context.MODE_PRIVATE);
    	String readImages = "";
        
    	for( int i= 0; i < images.length; i++ ){   		
    		readImages += images[i];
    		readImages += ";";
    	}
    	pref.edit().putString(READ_GUIDE_IMAGES_TAG, readImages).commit();
    	
    }
    
    public static final String ASSET_DIR_FILE = "files";
    
    public static String[] getImagesFromAssetFile(Context context) {
  	  AssetManager am = context.getResources().getAssets(); 
        try {
			String[] files = am.list(ASSET_DIR_FILE);
			return files;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
    public static String[] getValidMainImages(Context context,String[] images,int role){
    	return getValidImages(context,images,"main",role);
    }
    
    private static String[] getValidImages(Context context,String[] images,String centerTitle,int role){
    	if( images == null || centerTitle ==  null || centerTitle.isEmpty() == true || context == null ){
    		return null;
    	}
    	for( int i = 0; i < images.length;i++){
    		Log.d("GuideHelper","getValidImages images:"+images[i]);
    	}
    	ArrayList<String> validImages = new ArrayList<String>();
    	for( int i = 0; i < images.length; i++ ){
    		String filename = images[i];
    		String[] names = filename.split("_");
    		if( names.length >= 2 && names[1].equals(centerTitle) && names[0].equals(String.valueOf(role)) ){
    			validImages.add(filename);
    		}
    	}
    	for( int i = 0; i < validImages.size();i++){
    		Log.d("GuideHelper","getValidImages validImages:"+validImages.get(i));
    	}
    	if( validImages.size() == 0 ){
    		return null;
    	}
    	SharedPreferences pref = context.getSharedPreferences(READ_GUIDE_INFO, Context.MODE_PRIVATE);
    	String readImages = pref.getString(READ_GUIDE_IMAGES_TAG, "");
    	int validImagesRead = 0;
    	if( readImages != null && readImages.isEmpty() == false ){
	    	for(int i = 0; i < validImages.size();i++){
	    		String filename = validImages.get(i);
	    		if( readImages.contains(filename) == true ){
	    			validImagesRead++;
	    		}
	    	}
    	}
    	Log.d("GuideHelper","getValidImages validImagesRead:"+validImagesRead);
    	//所有图片都读取过，则返回null，都是无效图片
    	if( validImagesRead == validImages.size()){
    		return  null;
    	}
    	//否则返回所有图片
    	return (String[]) validImages.toArray(new String[validImages.size()]);
    }
    
    public static String[] getValidMenuImages(Context context,String[] images,int menutype,int role){
    	return getValidImages(context,images,String.valueOf(menutype),role);
    }

}
