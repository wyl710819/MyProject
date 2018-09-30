package com.routon.smartcampus.flower;

import org.json.JSONObject;

public class RemarkPictureMaterialBean {
	
	public String formFileKey;
	public String originalFileName;
	public int fileId;
	public String ftpFileName;
	public int status;
	public String uploadMsg;

	
	public RemarkPictureMaterialBean(JSONObject jsonObject) {
		try {
			formFileKey=jsonObject.optString("formFileKey");
			fileId=jsonObject.optInt("fileId");
			originalFileName=jsonObject.optString("originalFileName");
			ftpFileName=jsonObject.optString("ftpFileName");
			status=jsonObject.optInt("status");
			uploadMsg=jsonObject.optString("uploadMsg");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	RemarkPictureMaterialBean(){
		
	}
}
