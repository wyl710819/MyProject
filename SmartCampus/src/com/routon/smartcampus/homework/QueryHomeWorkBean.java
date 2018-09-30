package com.routon.smartcampus.homework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.routon.smartcampus.homework.NewAddHomeWorkBean;;;


public class QueryHomeWorkBean implements Serializable{

	public String assignmentTime = " ";
	public String className;
	public String description=" ";
	
	public int hid;
	public List<String> imgList=new ArrayList<String>();
	
	public NewAddHomeWorkBean newAddHomeWorkBean = null;
	QueryHomeWorkBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		
		description=obj.optString("description");
		assignmentTime=obj.optString("assignmentTime");
		assignmentTime=assignmentTime.substring(0,10);
		hid=obj.optInt("hId");
		className=obj.optString("className");
		
		//关联图片
//				JSONArray resJsonArray = obj.optJSONArray("fileUrl");
//				if( resJsonArray != null ){
//					imgList = new String[resJsonArray.length()];
//					for( int i = 0; i < resJsonArray.length(); i++ ){
//						JSONObject picres = resJsonArray.optJSONObject(i);
//						imgList[i] = picres.optString("url");
//					}
//				}
		JSONArray fileObj=obj.optJSONArray("fileUrl");
		for (int i = 0; i < fileObj.length(); i++) {
			if(fileObj!=null){
				JSONObject fileUrl = fileObj.optJSONObject(i);
				imgList.add(fileUrl.optString("url"));
			}
			
		}
		Log.d("XX",imgList.size()+"   ");
	}
	QueryHomeWorkBean(){
		
	}
}
