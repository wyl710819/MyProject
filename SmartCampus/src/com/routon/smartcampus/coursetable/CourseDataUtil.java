package com.routon.smartcampus.coursetable;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.routon.inforelease.util.TimeUtils;

import android.net.ParseException;
import android.util.Log;
import android.view.View;


public class CourseDataUtil {
	public static final String TAG = "CourseDataUtil";
	public String beginDate;
	public String endDate;
	public class SchoolTime {
		public String name;
		public String lesson;
		public String beginTime;
		public String endTime;
		public String beginTimeCheck;
		public String endTimeCheck;
		public String id;
		public String week;

		public SchoolTime(String _name, String _lesson, String _beginTime, String _endTime, String _beginTimeCheck, String _endTimeCheck, String _id,String _week) {
			name = _name;
			lesson = _lesson;
			beginTime = _beginTime;
			endTime = _endTime;
			beginTimeCheck = _beginTimeCheck;
			endTimeCheck = _endTimeCheck;
			id = _id;
			week=_week;
			
		}
	}
	
	public class DayTimeTable{
		public ArrayList<TimeTable> mTimeTablesList;
		public DayTimeTable(){
			mTimeTablesList = new ArrayList<TimeTable>();
		}
	}
	
	public class TimeTable {
		public String id;
		public String lesson;
		public String ampm;
		public String teacherName;
		public String teacherDevId;
		public String lessonName;
		public String lessonStartTime;
		public String lessonEndTime;
		public String teacherId;


		public TimeTable( String _id,  String _lesson, String _ampm, String _teacherName, 
				String _teacherDevId, String _lessonName,String _lessonStartTime,String _lessonEndTime,String _teacherId) {
			id = _id;
			lesson = _lesson;
			ampm = _ampm;
			teacherName = _teacherName;
			teacherDevId = _teacherDevId;
			lessonName=_lessonName;
			lessonStartTime=_lessonStartTime;
			lessonEndTime=_lessonEndTime;
			teacherId=_teacherId;
			
		}
		
		public String getFormatCourseStr(){
			return "第"+lesson+"节课 "+lessonName;
		}
	}
	
	 public class Holiday{
    	public String beginTime;
    	public String endTime;
    	
    	public Holiday(String _beginTime,String _endTime){
    		beginTime=_beginTime;
    		endTime=_endTime;
    	}
    } 
    public class Duty{
    	public String beginTime;
    	public String endTime;
    	public String week;
    	public String day;
    	
    	public Duty(String _beginTime,String _endTime,String _week,String _day){
    		beginTime=_beginTime;
    		endTime=_endTime;
    		week=_week;
    		day=_day;
    	}
    	
    }
	
	public List<TimeTable> getCourseData(Calendar calendar, boolean isExist,boolean isEvenWeek){	
		Date date = calendar.getTime();			
		Log.d(TAG,"getCourseData date:"+date.toString());
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
    	int weekDay = calendar.get(Calendar.DAY_OF_WEEK)-1;
  
    	Log.d(TAG,"weekDay:"+weekDay);
    	
		//周一到周五,检查是否是特殊放假日期
		if( weekDay >= 1 && weekDay <= 5 ){
     	    boolean isInHoliday = isInHoliday(date);
     	    if( isInHoliday == false&&mTimeTables[weekDay-1]!=null ){
     	    	ArrayList<TimeTable> mTimeTablesListTemp=new ArrayList<TimeTable>();
     	    	
     	    	if(!isExist){
     	    		for(int i=0;i<mTimeTables.length;i++){
     	    			mTimeTables[i].mTimeTablesList.clear();
     	     	    	mTimeTables[i]=null;
     	     	    	
     	    		}
     	    		mTimeTablesListTemp=null;
     	    	}else{
     	    		if (isEvenWeek) {//双周
     	    			if (isEven&&mTimeTables[weekDay-1+5]!=null&&mTimeTables[weekDay-1+5].mTimeTablesList!=null) {
     	    				mTimeTablesListTemp.addAll(mTimeTables[weekDay-1+5].mTimeTablesList);
						}else {
							mTimeTablesListTemp.addAll(mTimeTables[weekDay-1].mTimeTablesList);
						}
     	    			
					}else {
						mTimeTablesListTemp.addAll(mTimeTables[weekDay-1].mTimeTablesList);
					}
     	    		
     	    			
     	    	}
     	    	
     	    	return mTimeTablesListTemp;	
     		    
     	    }else{		   
     		    Log.d(TAG,"当天无课程安排");
     		    return null;
     	    }
        }else {//周末,检查是否是补课日期
        	int dutyWeekDay = getDutyWeekDay(date);
        	if( dutyWeekDay > 0&&mTimeTables[dutyWeekDay-1]!=null ){
        		return mTimeTables[dutyWeekDay-1].mTimeTablesList;
        	}else{
        		Log.d(TAG,"当天无课程安排");
        		return null;
        	}      
     	 }
			
	}
	 
	
	public int getDutyWeekDay(Date date){
		if( mDuty == null ) return -1;
		String startDuty = null;
		String endDuty = null;
		Date startDay = null;
		Date endDay = null;
		for( int index = 0; index < mDuty.size();index++ ){
	        	startDuty = mDuty.get(index).beginTime;
	        	endDuty = mDuty.get(index).endTime;
	        	SimpleDateFormat f = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
	        	try {
					startDay = f.parse(startDuty);
					endDay = f.parse(endDuty);	       
		   		   	//在补课时间安排内
					if( date.getTime() >= startDay.getTime() && date.getTime() <= endDay.getTime() ){
						return Integer.parseInt(mDuty.get(index).week);
					}
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  	
	    }
		return -1;
	}
	
	public boolean isInHoliday(Date date){
		if( mHoliday == null ) {
			return false;
		}
		String startHoliday = null;
		String endHoliday = null;
		Date startDay = null;
		Date endDay = null;

		for(int a = 0;a < mHoliday.size();a++){ 		  
   		   	startHoliday = mHoliday.get(a).beginTime;
         	endHoliday = mHoliday.get(a).endTime;
   		   	SimpleDateFormat f = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
   		   	try {
				startDay = f.parse(startHoliday);
				endDay = f.parse(endHoliday); 	       
	   		   	//在放假时间安排内
				if( date.getTime() >= startDay.getTime() && date.getTime() <= endDay.getTime() ){
					return true;
				}
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  		   	
		}
		//不在放假时间安排内
		return false;

	}
	
	public void parseXml() {		
		try {
			InputStream isTimeTable = CourseDataUtil.class.getClassLoader().getResourceAsStream("timetable.xml");
			InputStream isSchoolTime=CourseDataUtil.class.getClassLoader().getResourceAsStream("schooltime.xml");
			InputStream isHoliday=CourseDataUtil.class.getClassLoader().getResourceAsStream("holiday.xml");
			parseTimeTableXml(isTimeTable);
//			parseSchoolTimeXML(isSchoolTime);
			parseHolidyXml(isHoliday);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (XmlPullParserException e1) {
			e1.printStackTrace();
		}
		 
	} 
	
	public String getTimeSlot(TimeTable timetable){
		if( mSchoolTimes != null ){
			for( SchoolTime schooltime: mSchoolTimes ){
				if( timetable.lesson.equals(schooltime.lesson) ){
					String time = "";
					if( schooltime.beginTime.length() == 8 ){
						time += schooltime.beginTime.substring(0, 5);
					}else{
						time += schooltime.beginTime;
					}
					time += " - ";
					if( schooltime.endTime.length() == 8 ){
						time += schooltime.endTime.substring(0, 5);
					}else{
						time += schooltime.endTime;
					}
					return time;
				}
			}
		}
		return null;
	} 
	public  List<SchoolTime> getSchoolTimes(List<TimeTable> timetable){
		if( mSchoolTimes != null ){
			mSchoolTimeList.addAll(mSchoolTimes);
		}
		return mSchoolTimeList;
	}
	public List<SchoolTime> mSchoolTimes = new ArrayList<SchoolTime>();
	public List<SchoolTime> mSchoolTimeList = new ArrayList<SchoolTime>();
	public String workStarTime;
	public String workEndTime;
	protected void parseSchoolTimeXML(InputStream is,Calendar calendar) throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
//		Calendar calendar = Calendar.getInstance();			
//		Log.d(TAG,"getCourseData date:"+date.toString());
    	int myWeek = calendar.get(Calendar.DAY_OF_WEEK)-1;
    	
			int cnt = 0;
			XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
			pullParserFactory.setNamespaceAware(true);
			XmlPullParser xpp = pullParserFactory.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			// eventType = xpp.next();
			do {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					// System.out.println("Start document");
				} else if (eventType == XmlPullParser.END_DOCUMENT) {
					// System.out.println("End document");
					return;
				} else if (eventType == XmlPullParser.START_TAG) {
					// System.out.println("XmlPullParser.START_TAG");
					if (xpp.getName().equals("root")) {
						cnt = xpp.getAttributeCount();
						
					} else if (xpp.getName().equals("schooltime")) {
						cnt = xpp.getAttributeCount();
						beginDate = xpp.getAttributeValue(null, "beginDate");
						endDate = xpp.getAttributeValue(null, "endDate");
						workStarTime = xpp.getAttributeValue(null, "workStarTime");
						workEndTime = xpp.getAttributeValue(null, "workEndTime");
//						if (cnt > 0) {
//							mName = xpp.getAttributeValue(null, "name");
//							
//						}
					} else if (xpp.getName().equals("course")) {
						cnt = xpp.getAttributeCount();
						String name = "";
						String lesson = null;
						String beginTime = null;
						String endTime = null;
						String beginTimeCheck = null;
						String endTimeCheck = null;
						String id = null;
						String week=null;
						if (cnt > 0) {
							name = xpp.getAttributeValue(null, "name");
							lesson = xpp.getAttributeValue(null, "lesson");
							beginTime = xpp.getAttributeValue(null, "beginTime");
							endTime = xpp.getAttributeValue(null, "endTime");
							beginTimeCheck = xpp.getAttributeValue(null, "beginTimeCheck");
							endTimeCheck = xpp.getAttributeValue(null, "endTimeCheck");
							id = xpp.getAttributeValue(null, "id");
							week=xpp.getAttributeValue(null,"week");
							String arrWeek[]=null;
							if(beginTimeCheck!=null&&beginTimeCheck.equals("true")){
								if(week!=null){
									arrWeek=week.split("\\|");
								}
								
								if(arrWeek!=null){
									for(int i=0;i<arrWeek.length;i++){
										if(arrWeek[i].equals(String.valueOf(myWeek))){
											mSchoolTimes.add(new SchoolTime(name, lesson, beginTime,endTime,beginTimeCheck,endTimeCheck,id,week));
											Log.d(TAG,"parseSchoolTimeXML name:"+name+",lesson:"+lesson+"week: "+week+"beginTime: "+beginTime+"check:"+beginTimeCheck);
										}
									}
								}else{
									mSchoolTimes.add(new SchoolTime(name, lesson, beginTime,endTime,beginTimeCheck,endTimeCheck,id,week));
									Log.d(TAG,"parseSchoolTimeXML name:"+name+",lesson:"+lesson+"week: "+week+"beginTime: "+beginTime+"check:"+beginTimeCheck);
								}
								
							}
//							mSchoolTimes.add(new SchoolTime(name, lesson, beginTime,endTime,beginTimeCheck,endTimeCheck,id,week));
							
							// System.out.println("url:"+url);
							
						}
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					// System.out.println("XmlPullParser.END_TAG");
					if (xpp.getName().equals("root")) {
						break;
					}
				} else if (eventType == XmlPullParser.TEXT) {
				}
				eventType = xpp.next();
			} while (eventType != XmlPullParser.END_DOCUMENT);
		
			//loginAddData();
		
		
	}

	 private DayTimeTable[] mTimeTables = new DayTimeTable[10];
	 
	 protected void parseTimeTableXml(InputStream is) throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		int courseDay=0;//解析有课程天数
		int cnt = 0;
		
		XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
		pullParserFactory.setNamespaceAware(true);
		XmlPullParser xpp = pullParserFactory.newPullParser();
		xpp.setInput(is, "UTF-8");
		int eventType = xpp.getEventType();
		for( int i = 0; i < mTimeTables.length; i++ ){
			
			mTimeTables[i] = new DayTimeTable();
			mTimeTables[i].mTimeTablesList.clear();
		}
		Log.d(TAG,"parseTimeTableXml");
		// eventType = xpp.next();
		do {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				// System.out.println("Start document");
			} else if (eventType == XmlPullParser.END_DOCUMENT) {
				// System.out.println("End document");
				return;
			} else if (eventType == XmlPullParser.START_TAG) {
				// System.out.println("XmlPullParser.START_TAG");
				if (xpp.getName().equals("root")) {
					cnt = xpp.getAttributeCount();
					
				} else if (xpp.getName().equals("timetable")) {
					cnt = xpp.getAttributeCount();
					
				}else if (xpp.getName().equals("timetable-even")) {
					cnt = xpp.getAttributeCount();
					isEven = true;
					
				}else if (xpp.getName().equals("courses")) {
					courseDay++;
					cnt = xpp.getAttributeCount();
					
				} else if(xpp.getName().equals("course")){
					cnt = xpp.getAttributeCount();
					
					String id = "";
					String lesson = null;
					String ampm = null;
					String teacherName = null;
					String teacherDevId = null;
					String lessonName = null;
					String lessonStartTime=null;
					String lessonEndTime=null;
					String teacherId=null;
					if (cnt > 0) {
						
						id = xpp.getAttributeValue(null, "id");
						lesson = xpp.getAttributeValue(null, "lesson");
						ampm = xpp.getAttributeValue(null, "ampm");
						teacherName = xpp.getAttributeValue(null, "teacherName");
						teacherDevId = xpp.getAttributeValue(null, "teacherDevId");
						teacherId=xpp.getAttributeValue(null, "sid");
						lessonName = xpp.nextText();
						Log.d(TAG,"parseTimeTableXml lesson:"+lesson+",teacherName:"+teacherName+",courseDay:"+courseDay);
						mTimeTables[courseDay-1].mTimeTablesList.add(new TimeTable(id, lesson, ampm,teacherName,teacherDevId,lessonName,lessonStartTime,lessonEndTime,teacherId));
					}
					
				}
				
			} else if (eventType == XmlPullParser.END_TAG) {
				// System.out.println("XmlPullParser.END_TAG");
				if (xpp.getName().equals("root")) {					
					//TimeTablesAll.add(TimeTables);
					break;
				}
			} else if (eventType == XmlPullParser.TEXT) {
			}
			eventType = xpp.next();
		} while (eventType != XmlPullParser.END_DOCUMENT);
		//loginAddData();
			
	}
	
	private List<Holiday> mHoliday=new ArrayList<Holiday>();
	private List<Duty> mDuty=new ArrayList<Duty>();
	//识别是是哪个标签下的item
	int dayCnt;
	private boolean isEven=false;

	protected void parseHolidyXml(InputStream is) throws XmlPullParserException, IOException{
		int cnt=0;
		XmlPullParserFactory pullParserFactory =XmlPullParserFactory.newInstance();
		pullParserFactory.setNamespaceAware(true);
		XmlPullParser xpp=pullParserFactory.newPullParser();
		xpp.setInput(is,"UTF-8");
		int eventType=xpp.getEventType();
		do{
			if(eventType==XmlPullParser.START_DOCUMENT){
				
			}else if(eventType==XmlPullParser.END_DOCUMENT){
				return;
			}else if(eventType==XmlPullParser.START_TAG){
				if(xpp.getName().equals("root")){
					cnt=xpp.getAttributeCount();
				}else if(xpp.getName().equals("holiday")){
					cnt=xpp.getAttributeCount();
					dayCnt=1;
				}else if(xpp.getName().equals("item")){
					cnt=xpp.getAttributeCount();
					String beginTime=null;
					String endTime=null;
					String week="";
					String day="";
					if(cnt>0){
						
						beginTime=xpp.getAttributeValue(null,"beginTime");
						endTime=xpp.getAttributeValue(null, "endTime");
						week=xpp.getAttributeValue(null,"week");
						day=xpp.getAttributeValue(null,"day");
						if(dayCnt==1){
							mHoliday.add(new Holiday(beginTime,endTime));
						}else if(dayCnt==2){
							mDuty.add(new Duty(beginTime,endTime,week,day));
						}
						
					}
				}else if(xpp.getName().equals("duty")){
					cnt=xpp.getAttributeCount();
					dayCnt=2;					
				}
//				else if(xpp.getName().equals("item")){
//					cnt=xpp.getAttributeCount();
//					String beginTime=null;
//					String endTime=null;
//					String week="";
//					String day="";
//					if(cnt>0){
//						beginTime=xpp.getAttributeValue(null,"beginTime");
//						endTime=xpp.getAttributeValue(null,"endTime");
//						week=xpp.getAttributeValue(null,"week");
//						day=xpp.getAttributeValue(null,"day");
//						mDuty.add(new Duty(beginTime,endTime,week,day));
//					}
//				}
			}else if(eventType==XmlPullParser.END_TAG){
				if(xpp.getName().equals("root")){
					for(int i=0;i<mDuty.size();i++){
						Log.i("XML",mHoliday.get(i).toString()+"------------");
					}
				}
				
			}else if(eventType==XmlPullParser.TEXT){
				
			}
			eventType = xpp.next();
		}while(eventType!=XmlPullParser.END_DOCUMENT);
	}
    
}
