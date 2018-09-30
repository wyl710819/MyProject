package com.routon.smartcampus.guestbook;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

public class GuestbookBean {

	public int type=0;
	public String sid;
	public String msg;
	public String createTime;
	public String readTime;
	private static String timeStr="今天";
	
	
	public GuestbookBean(){
		
	}
	
	
	public GuestbookBean(JSONObject object){
		sid=object.optString("sid");
		msg=getGBKString(object.optString("msg"));
		String createTimeStr=object.optString("createTime");
		createTime=getStringToDate(createTimeStr)+" "+createTimeStr.substring(createTimeStr.indexOf(" ")+1,createTimeStr.length());
		String readTimeStr=object.optString("readTime");
		if (readTimeStr!=null && !readTimeStr.equals("")) {
			readTime=getStringToDate(readTimeStr)+" "+readTimeStr.substring(readTimeStr.indexOf(" ")+1,readTimeStr.length());
		}
		
		
	}
	
	public static String getGBKString(String str) {  
		String gbk ="";
		try {
			URLDecoder urlDecoder=new URLDecoder();
			gbk=urlDecoder.decode(str, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gbk;

	  }  
	

	public static String  getStringToDate(String dateString) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date date = new Date();
	    try{
	        date = dateFormat.parse(dateString);
	    } catch(ParseException e) {
	        e.printStackTrace();
	    }
	    
	    Calendar mCalendar = Calendar.getInstance();
	    mCalendar.setTimeInMillis(date.getTime());
	    
	    switch (mCalendar.get(Calendar.AM_PM)) {
		case 0:
			timeStr="上午";
			break;
		case 1:
			timeStr="下午";
			break;

		default:
			break;
		}
	    return timeStr;
	}





	
}
