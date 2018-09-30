package com.routon.inforelease.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
	public static String FORMAT_yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
	public static String FORMAT_yyyy_MM_dd_HH_mm = "yyyy-MM-dd HH:mm";
	public static String FORMAT_yyyy_MM_dd = "yyyy-MM-dd";
	public static String FORMAT_HH_mm_ss = "HH:mm:ss";
	public static String FORMAT_HH_mm = "HH:mm";
	public static String FORMAT_yyyyMMddHHmmss = "yyyyMMddHHmmss";
	public static String FORMAT_yyyyMMdd = "yyyyMMdd";
	public static String FORMAT_HHmmss = "HHmmss";

	/*
	 * yyyy-MM-dd HH:mm:ss  to yyyy-MM-dd
	 */
	public static String getDate(String date_time) {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_yyyy_MM_dd_HH_mm_ss);
		Date date = null;
		try {
			date = sdf.parse(date_time);
			SimpleDateFormat new_sdf = new SimpleDateFormat(FORMAT_yyyy_MM_dd);
			return new_sdf.format(date);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		return date_time;
	}
	
	/*
	 * yyyy-MM-dd HH:mm:ss  to yyyy-MM-dd
	 */
	public static String getDate(String date_time, String date_format) {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_yyyy_MM_dd_HH_mm_ss);
		Date date = null;
		try {
			date = sdf.parse(date_time);
			SimpleDateFormat new_sdf = new SimpleDateFormat(date_format);
			return new_sdf.format(date);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		return date_time;
	}
	
	public static String convertDate(String date_time, String src_format, String dest_format) {
		SimpleDateFormat sdf = new SimpleDateFormat(src_format);
		Date date = null;
		try {
			date = sdf.parse(date_time);
			SimpleDateFormat new_sdf = new SimpleDateFormat(dest_format);
			return new_sdf.format(date);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		return date_time;
	}
	
	/*
	 * yyyy-MM-dd HH:mm:ss  to HH:mm
	 */
	public static String getTime(String date_time) {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_yyyy_MM_dd_HH_mm_ss);
		Date date = null;
		try {
			date = sdf.parse(date_time);
			SimpleDateFormat new_sdf = new SimpleDateFormat(FORMAT_HH_mm);
			return new_sdf.format(date);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		return date_time;
	}
	
	/*
	 * yyyy-MM-dd HH:mm:ss  to format
	 */
	public static String getTime(String date_time, String time_format) {
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_yyyy_MM_dd_HH_mm_ss);
		Date date = null;
		try {
			date = sdf.parse(date_time);
			SimpleDateFormat new_sdf = new SimpleDateFormat(time_format);
			return new_sdf.format(date);
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		
		return date_time;
	}
	
	public static Calendar getFormatCalendar(String timeStr,String format){	
		if( timeStr == null || timeStr.isEmpty() == true ) return null;
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(sdf.parse(timeStr));
			return calendar;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getFormatCalendarStr(String initStr,String format){	
		if( initStr == null || initStr.isEmpty() == true ) return null;
		
		
		SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_yyyy_MM_dd);
		Date date = null;
		try {
			date = sdf.parse(initStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if( date == null ){
			sdf = new SimpleDateFormat(FORMAT_yyyy_MM_dd_HH_mm_ss);
			try {
				date = sdf.parse(initStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if( date == null ){
			sdf = new SimpleDateFormat(FORMAT_yyyy_MM_dd_HH_mm);
			try {
				date = sdf.parse(initStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if( date == null ){
			sdf = new SimpleDateFormat(FORMAT_yyyyMMddHHmmss);
			try {
				date = sdf.parse(initStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if( date == null ){
			sdf = new SimpleDateFormat(FORMAT_yyyyMMdd);
			try {
				date = sdf.parse(initStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if( date != null ){
			SimpleDateFormat resultSdf = new SimpleDateFormat(format);
			return resultSdf.format(date);
		}
		return null;
	}
	
	public static boolean isDateBefore(Calendar time1,Calendar time2){
		if( time1 == null || time2 == null ) return false;
		time1.set(Calendar.HOUR_OF_DAY, 0);
		time1.set(Calendar.MINUTE, 0);
		time1.set(Calendar.SECOND, 0);
		time1.set(Calendar.MILLISECOND, 0);
		
		time2.set(Calendar.HOUR_OF_DAY, 0);
		time2.set(Calendar.MINUTE, 0);
		time2.set(Calendar.SECOND, 0);
		time2.set(Calendar.MILLISECOND, 0);
		
		return time1.before(time2);
	}
	
	public static int getDayCount(Calendar time1,Calendar time2){
		double dayCount = (time1.getTimeInMillis()-time2.getTimeInMillis())/(1000*3600*24);//从间隔毫秒变成间隔天数
		return (int)dayCount;
	}
	
	public static boolean isTimeBeforeTilMinute(Calendar time1,Calendar time2){
		if( time1 == null || time2 == null ) return false;
		time1.set(Calendar.SECOND, 0);
		time1.set(Calendar.MILLISECOND, 0);
		
		time2.set(Calendar.SECOND, 0);
		time2.set(Calendar.MILLISECOND, 0);
		
		return time1.before(time2);
	}
}
