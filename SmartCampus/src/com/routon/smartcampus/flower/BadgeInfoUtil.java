package com.routon.smartcampus.flower;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.utils.FTPUtils;
import com.routon.smartcampus.utils.FileUtil;
public class BadgeInfoUtil {
	public static final int MAX_CUSTOM_FLOWERS = 12;
	private static ArrayList<Badge> mFlowersList = null;
	//常用小红花
	private static ArrayList<BadgeInfo> mCustomFlosers = new ArrayList<BadgeInfo>();
	//自定义评语
	private static ArrayList<BadgeInfo> mDefineEvas = new ArrayList<BadgeInfo>();
	
	public static void clearDatas(){
		mFlowersList = null;
		if( mCustomFlosers != null ){
			mCustomFlosers.clear();
		}
		if( mDefineEvas != null ){
			mDefineEvas.clear();
		}
	}
	
	public static String getStringFromPath(String fileName){
		File fs = new File(fileName);
		if (!fs.exists()) {
			try {
				fs.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		String result = "";
		 
        try {
            FileInputStream f = new FileInputStream(fileName);
            BufferedReader bis = new BufferedReader(new InputStreamReader(f));
            String line = "";
            while ((line = bis.readLine()) != null) {
                result += line;
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }    
        return result; 
    }

	public static void saveDatasToFile(String fileName) {
		try {
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < mCustomFlosers.size(); i++) {
				BadgeInfo info = mCustomFlosers.get(i);
				JSONObject remarkObj = new JSONObject();
				remarkObj.put("resTitle", info.badgeTitle);
				remarkObj.put("resRemark", info.badgeRemark);
				remarkObj.put("resId", info.badgeTitleId);
				remarkObj.put("badgeId", info.badgeId);
				remarkObj.put("type", info.type);
				remarkObj.put("resBounsPoint", info.bonuspoint);
				jsonArray.put(remarkObj);
			}
			
			for (int i = 0; i < mDefineEvas.size(); i++) {
				BadgeInfo info = mDefineEvas.get(i);
				JSONObject remarkObj = new JSONObject();
				remarkObj.put("resTitle", info.badgeTitle);
				remarkObj.put("resRemark", info.badgeRemark);
				remarkObj.put("resId", info.badgeTitleId);
				remarkObj.put("badgeId", info.badgeId);
				remarkObj.put("type", info.type);
				remarkObj.put("resBounsPoint", info.bonuspoint);
				jsonArray.put(remarkObj);
			}

			// 写入文件
			Writer writer = null;
			try {
				File file = new File(fileName);

				OutputStream out = new FileOutputStream(file);
				writer = new OutputStreamWriter(out);
				writer.write(jsonArray.toString());
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	
	public static void setCustomFlowers(ArrayList<BadgeInfo> customFlowers){
		mCustomFlosers = customFlowers;
	}
	
	public static ArrayList<BadgeInfo> getCustomFlowers(){
		return mCustomFlosers;
	}
	
	public static void addCustomFlower(BadgeInfo info){
		mCustomFlosers.add(0,info);
	}
	
	public static ArrayList<BadgeInfo> getDefineEvas(){
		return mDefineEvas;
	}
	
	public static void addDefineEva(BadgeInfo info){
		mDefineEvas.add(0,info);
	}
	
	public static void removeDefineEva(BadgeInfo info){
		mDefineEvas.remove(info);
	}
	
	public static ArrayList<Badge> getFlowerList(){
		return mFlowersList;
	}
	
	public static void setFlowerList(ArrayList<Badge> flowers){
		mFlowersList = flowers;
	}
	
	public interface UploadFileListener{
		public void uploadFile(boolean success);
	}
	
	public interface DownloadFileListener{
		public void downloadFile(boolean success);
	}
	
	public static void getFtpBadegData(final String ftpUrl, final String port, final String fileName,final DownloadFileListener listener) {
		new Thread(new Runnable() {	
			@Override
			public void run() {
				
				String sdcardPath = FileUtil.getSDPath();
                File destDir = new File(sdcardPath+"/Android/data/com.routon.edurelease/badge_json");
                if (destDir.exists() == false) {
                	destDir.mkdir();
    			}			
				FTPUtils ftpUtils = FTPUtils.getInstance();
				boolean flag = ftpUtils.initFTPSetting(ftpUrl, Integer.valueOf(port), "appdata", "#@&5jEbFm2h$x&U*");
				boolean downFileFlag = false;
				String filePath=destDir.getAbsolutePath()+"/"+fileName;
				File file = new File(filePath);
				if(file.exists() == true){
					file.delete();
				}
				if (flag ) {
					downFileFlag=ftpUtils.downLoadFile(filePath,fileName);
				}
				if ( downFileFlag == false ) {	//下载失败
					listener.downloadFile(false);
					return;	
				}	
				listener.downloadFile(true);
				BadgeInfoUtil.parseDownloadBadgeInfo(filePath);	
			}
		}).start();
	}
	
	public static void uploadFile(final UploadFileListener listener) {// 上传新生成的json文件
		new Thread(new Runnable() {

			@Override
			public void run() {
				String sdcardPath = FileUtil.getSDPath();
				File destDir = new File(sdcardPath + "/Android/data/com.routon.edurelease/badge_json");
				if (destDir.exists() == false) {
					destDir.mkdir();
				}
				String fileName = InfoReleaseApplication.authenobjData.userId + ".json";
				String filePath=destDir.getAbsolutePath()+"/"+fileName;
				
				saveDatasToFile(filePath);

				FTPUtils ftpUtils = FTPUtils.getInstance();
				boolean flag = ftpUtils.initFTPSetting(SmartCampusApplication.ftpUrl,
						Integer.valueOf(SmartCampusApplication.port), SmartCampusApplication.ftpUserName,
						SmartCampusApplication.ftpUserpwd);
				boolean uploadFileFlag = false;
				if (flag) {
					uploadFileFlag = ftpUtils.uploadFile(filePath, fileName);
				} 
				if (uploadFileFlag) {
					if( listener != null ){
						listener.uploadFile(true);
					}
//					Intent intent=new Intent();
//					setResult(RESULT_OK, intent);
//					BadgeInfoUtil.setCustomFlowers(badgeList);
//					finish();
				} else {
					if( listener != null ){
						listener.uploadFile(false);
					}
				}

			}
		}).start();

	}
	
	public static void parseDownloadBadgeInfo(String filePath){
		parseDownloadBadgeInfo(mFlowersList,filePath);
	}
	
	public static void parseDownloadBadgeInfo(ArrayList<Badge> flowersList,String filePath){
		mCustomFlosers.clear();
		mDefineEvas.clear();
		if( flowersList == null ){
			return;
		}
		mFlowersList = flowersList;
		String jsonData;
		jsonData = getStringFromPath(filePath);
		if( jsonData == null || jsonData.isEmpty() == true ){
			return;
		}
        
		//解析数据                         
        JSONArray array;
		try {
			array = new JSONArray(jsonData);
			int len = array.length();
			for (int i = 0; i < len; i++) {
				JSONObject obj = (JSONObject) array.get(i);
				
				OftenBadgeBean bean = new OftenBadgeBean(obj);
				if (flowersList!=null) {
					for (int j = 0; j < flowersList.size(); j++) {
						if ( flowersList.get(j).id == bean.badgeId ) {
							bean.imgUrl = flowersList.get(j).imgUrl;
							bean.prop=flowersList.get(j).prop;
							
						}
					}
				}
				if (bean.type == BadgeInfo.TYPE_CUSTOMFLOWER ) {
					mCustomFlosers.add(bean);
				}else{
					mDefineEvas.add(bean);
				}
			}
		}catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
