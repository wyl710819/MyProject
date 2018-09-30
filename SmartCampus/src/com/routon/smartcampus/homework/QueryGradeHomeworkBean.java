package com.routon.smartcampus.homework;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.routon.smartcampus.homework.HomeworkBean;


public class QueryGradeHomeworkBean implements Parcelable{

	public String assignmentTime = " ";
	public String className;
	public String description=" ";
	
	public int hid;
	public List<String> imgList=new ArrayList<String>();
	
	public HomeworkBean newAddHomeWorkBean = null;
	public ArrayList<HomeworkResourse> resourseList=new ArrayList<HomeworkResourse>();
	QueryGradeHomeworkBean(JSONObject obj){
		if( obj == null ){
			return;
		}
		
		description=obj.optString("description");
		assignmentTime=obj.optString("assignmentTime");
//		assignmentTime=assignmentTime.substring(0,10);
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
				HomeworkResourse resourse=new HomeworkResourse(fileObj.optJSONObject(i));
				if(resourse!=null){
					resourseList.add(resourse);
				}
				imgList.add(fileUrl.optString("url"));
			}
			
		}
//		Log.d("XX",imgList.size()+"   ");
	}
	QueryGradeHomeworkBean(){
		
	}
	QueryGradeHomeworkBean(Parcel par){
//		QueryGradeHomeworkBean  queryGradeBean=new QueryGradeHomeworkBean();
    	this.assignmentTime=par.readString();
    	this.className=par.readString();
   	    this.description=par.readString();
    	this.hid=par.readInt();
    	par.readList(imgList,List.class.getClassLoader());
    	par.readTypedList(resourseList,HomeworkResourse.CREATOR);
	}
	public static class HomeworkResourse implements Parcelable{
		public int type=0;
		public String fileIdparams;
		public String fileUrl;
		public HomeworkResourse(JSONObject obj){
			if(obj==null){
				return;
			}
			type=obj.optInt("type");
			fileIdparams=obj.optString("fileIdparams");
			fileUrl=obj.optString("url");
		}
		public HomeworkResourse(Parcel par) {
			// TODO Auto-generated constructor stub
			this.type=par.readInt();
			this.fileIdparams=par.readString();
			this.fileUrl=par.readString();
		}
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			// TODO Auto-generated method stub
			dest.writeInt(type);
			dest.writeString(fileIdparams);
			dest.writeString(fileUrl);
		}
		 public static final Creator<HomeworkResourse> CREATOR = new Creator<HomeworkResourse>() {  
			 
			   
		        @Override  
		        public HomeworkResourse createFromParcel(Parcel par) {
//		        	
		        	return new HomeworkResourse(par);      
		            
		        }  
		  
		        @Override  
		        public HomeworkResourse[] newArray(int size) {  
		            return new HomeworkResourse[size];  
		        }  
		    };  
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(assignmentTime);
		dest.writeString(className);
		dest.writeString(description);
		dest.writeInt(hid);
		dest.writeList(imgList);
		if(resourseList==null){
			resourseList=new ArrayList<HomeworkResourse>();
		}
		dest.writeTypedList(resourseList);
	}
	
	 public static final Creator<QueryGradeHomeworkBean> CREATOR = new Creator<QueryGradeHomeworkBean>() {  
		 
		   
	        @Override  
	        public QueryGradeHomeworkBean createFromParcel(Parcel par) {
//	        	QueryGradeHomeworkBean  queryGradeBean=new QueryGradeHomeworkBean();
//	        	queryGradeBean.assignmentTime=par.readString();
//	        	queryGradeBean.className=par.readString();
//	        	queryGradeBean.description=par.readString();
//	        	queryGradeBean.hid=par.readInt();
//	        	queryGradeBean.imgList=par.readList(imgList,ClassLoader.getSystemClassLoader());
	        	return new QueryGradeHomeworkBean(par);      
	            
	        }  
	  
	        @Override  
	        public QueryGradeHomeworkBean[] newArray(int size) {  
	            return new QueryGradeHomeworkBean[size];  
	        }  
	    };  
}
