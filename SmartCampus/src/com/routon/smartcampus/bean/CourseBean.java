package com.routon.smartcampus.bean;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;


//课程数据
public class CourseBean {
	public int weekday;//周一到周五
	public String name;
	public int lesson;//节次id
	
	
	public static ArrayList<String> getUnrepeatedCourses(ArrayList<CourseBean> datas){
		ArrayList<String> strArray = new ArrayList<String>();
		for( CourseBean bean:datas){
			if( strArray.contains(bean.name) == false ){
				strArray.add(bean.name);
			}
		}
		return strArray;
	}
	
	public static ArrayList<String> parseUnrepeatedClassCourseBean(XmlPullParser response) throws Exception{
		return getUnrepeatedCourses(parseClassCourseBean(response));
	}
	
	public String getFormatCourseStr(){
		return "第"+lesson+"节课 "+name;
	}
	
	public static ArrayList<CourseBean> parseClassCourseBean(XmlPullParser response) throws Exception{
		ArrayList<CourseBean> datas = new ArrayList<CourseBean>();

		int eventType = response.getEventType();
		String day = null;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
				case XmlPullParser.START_TAG:
					String nodeName = response.getName();
					if ("course".equals(nodeName)) {
						String lesson = response.getAttributeValue(null,"lesson");
						String name = response.nextText();
						
						CourseBean bean = new CourseBean();
						bean.name = name;
						bean.lesson = Integer.parseInt(lesson);
						if( day != null ){
							if( day.length() == 1 ){
								bean.weekday = Integer.parseInt(day);
							}else{
								
							}
						}
						datas.add(bean);
					}else if ("courses".equals(nodeName)) {
						day = response.getAttributeValue(null,"day");
					}
					break;
				case XmlPullParser.END_TAG:
					if ("timetable".equals(response.getName())) {
						return datas;
					}
		            break;
			}
			eventType = response.next();
		 
		}
	
		return datas;
	}

}
