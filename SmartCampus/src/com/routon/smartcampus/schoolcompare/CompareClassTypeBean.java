package com.routon.smartcampus.schoolcompare;

import java.io.Serializable;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CompareClassTypeBean implements Serializable{
	
	public String name;
	public String id;
	public String redflagImgUrl;
	public int redflagImgFileId;
	public int ratingMode;//1先选取班级，2先选取评分项
	public List<SubprojectBean> subprojectBeanList;
	public List<ClassCompareBean> classCompareBeanList;
	public String nearestRatingStartTime="";
	public String nearestRatingFinishTime;
	public String ratingTime="";
	public String ratingStartTime;
	public CompareClassTypeBean (JSONObject object){
		
		try {
			id=object.optString("id");
			name=object.optString("name");
			ratingMode = object.optInt("ratingMode");
			redflagImgFileId=object.optInt("redflagImgFileId");
			redflagImgUrl=object.optString("redflagImgUrl");
			nearestRatingStartTime=object.optString("nearestRatingStartTime");
			nearestRatingFinishTime=object.optString("nearestRatingFinishTime");
			
			if (nearestRatingStartTime.contains(" ")) {
				nearestRatingStartTime=nearestRatingStartTime.substring(0, nearestRatingStartTime.indexOf(" "));
			}
			
			if (nearestRatingFinishTime!=null && !nearestRatingFinishTime.isEmpty()&&!nearestRatingFinishTime.equals("null")) {
				if (nearestRatingFinishTime.contains(" ")) {
					nearestRatingFinishTime=nearestRatingFinishTime.substring(0, nearestRatingFinishTime.indexOf(" "));
				}
				ratingTime=dateFormChange(nearestRatingFinishTime);
				ratingStartTime=getMorn(nearestRatingFinishTime);
			}else {
				ratingTime=dateFormChange(getYesterday(nearestRatingStartTime));
				ratingStartTime=nearestRatingStartTime;
			}
			
			
			JSONArray jsonArray=object.getJSONArray("items");
			if (jsonArray!=null) {
				subprojectBeanList=new ArrayList<SubprojectBean>();
				for (int i = 0; i < jsonArray.length(); i++) {
					SubprojectBean bean=new SubprojectBean(jsonArray.getJSONObject(i));
					subprojectBeanList.add(bean);
				}
			}
			JSONArray groups = object.getJSONArray("groups");
			if(groups != null){
				classCompareBeanList = new ArrayList<>();
				for(int i=0;i<groups.length();i++){
					ClassCompareBean classCompareBean = new ClassCompareBean(groups.getJSONObject(i));
					classCompareBeanList.add(classCompareBean);
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	private String getMorn(String finishTime) {
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = sdf.parse(finishTime, pos);
		
		Calendar c = Calendar.getInstance();  
		c.getTime();
        c.setTime(strtodate);  
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = c.getTime();
        
		return sdf.format(tomorrow);
	}
	
	private String getYesterday(String startTime) {
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = sdf.parse(startTime, pos);
		
		Calendar c = Calendar.getInstance();  
		c.getTime();
        c.setTime(strtodate);  
        c.add(Calendar.DAY_OF_MONTH, -1);
        Date tomorrow = c.getTime();
        
		return sdf.format(tomorrow);
	}

	public CompareClassTypeBean() {
		
	}
	private String  dateFormChange(String dateStr) {
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = sdf.parse(dateStr, pos);
		SimpleDateFormat sdf2= new SimpleDateFormat("yyyy-M-d");
		return sdf2.format(strtodate);
	}
}
