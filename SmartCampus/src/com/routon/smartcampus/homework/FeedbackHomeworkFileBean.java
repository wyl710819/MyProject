package com.routon.smartcampus.homework;



import java.io.Serializable;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class FeedbackHomeworkFileBean implements Serializable{
	
	
	private static final long serialVersionUID = -4184694326032835054L;
	public String fileUrl;
	public boolean isLocal;
	public int fileType;
	public int audioLength=0;
	
	public String fileId="";
	public String fileTypeId="";
	public String fileIdparams="0";
	
	
	public FeedbackHomeworkFileBean(JSONObject obj) {
		fileId = obj.optString("fileId");
		fileIdparams = obj.optString("fileIdparams");
		fileUrl = obj.optString("url");
		int type = obj.optInt("type");
		fileTypeId=fileId+"_"+type;
		if (type==166) {
			fileType=1;
		}else if(type==172){
			fileType=2;
		}
		
		if (fileIdparams!=null && !fileIdparams.equals("null")&& !fileIdparams.equals("") && fileIdparams.contains("=")) {
			String params=fileIdparams.substring(fileIdparams.indexOf("=")+1, fileIdparams.length());
			audioLength=Integer.valueOf(params);
		}
		
		isLocal=false;
	}
	
	
	public FeedbackHomeworkFileBean(String _fileUrl,boolean _isLocal,int _fileType){
		fileUrl=_fileUrl;
		isLocal=_isLocal;
		fileType=_fileType;
	}

}
