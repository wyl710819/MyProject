package com.routon.smartcampus.coursetable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.bean.CourseBean;
import com.routon.smartcampus.coursetable.CourseDataUtil.SchoolTime;
import com.routon.smartcampus.coursetable.CourseDataUtil.TimeTable;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.FileRequest;
import com.routon.smartcampus.utils.XMLRequest;
import com.routon.widgets.Toast;

//课程表帮助类
public class CourseTableHelper {
	public ProgressDialog progressDialog;
	private static final String TAG = "CourseTableHelper";
	private Calendar mCalendar=null;
	   /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        public void onResponse(T response);
    }
    
    public interface ErrorListener{
        /** Called when a response is received. */
        public void onResponse(String errorMsg);
    }
	
    public void showDialog(Context mContext)
    {
    	progressDialog = new ProgressDialog(mContext);
    	progressDialog.setTitle("");
    	progressDialog.setMessage("...loading...");
    	progressDialog.show();
    }
    
    public void dismissDialog()
    {
    	if(progressDialog != null)
    	{
    		if(progressDialog.isShowing())
    			progressDialog.dismiss();
    	}
    }
//	public static void getTimeTable(final Context context,String url,final CourseTableHelper.Listener<ArrayList<CourseBean>> listener){
//		final ProgressDialog progressDialog = ProgressDialog.show(context, "", "...loading...");
//		
//		XMLRequest xmlRequest = new XMLRequest(url,new Response.Listener<XmlPullParser>() {
//					@Override
//					public void onResponse(XmlPullParser response) {
//							if (progressDialog != null && progressDialog.isShowing()) {
//								progressDialog.dismiss();
//							}
//					
//							//获取课程表
//							ArrayList<CourseBean> datas;
//							try {
//								datas = CourseBean.parseClassCourseBean(response);
//								if( datas != null && datas.size() > 0 ){
//									listener.onResponse(datas);								
//								}
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//					}
//			}, new Response.ErrorListener() {
//						@Override
//						public void onErrorResponse(VolleyError error) {
//							Log.e("TAG", error.getMessage(), error);
//							Toast.makeText(context, "无法获取到此班级的课程表", Toast.LENGTH_LONG).show();
//							if (progressDialog != null && progressDialog.isShowing()) {
//								progressDialog.dismiss();
//							}
//						}
//				});
//			xmlRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
//			InfoReleaseApplication.requestQueue.add(xmlRequest);
//	}
	
	public static File getCacheDir(Context context,String groupId){
		File cacheDir = context.getCacheDir();
		File groupCacheDir = new File(cacheDir,groupId);
		if( groupCacheDir.exists() == false ){
			groupCacheDir.mkdirs();
		}
		return groupCacheDir;
	}
	
	public ArrayList<String> getAmCourseStrs(List<TimeTable> datas){
		if( datas == null ) return null;
		ArrayList<String> courses = new ArrayList<String>();
		for( TimeTable timetable:datas){
			Log.d(TAG,"getAmCourseStrs timetable:"+timetable.ampm);
			if( timetable.ampm.equals("0") ){
				String str=" ";
				if(mCourseDateUtil.getTimeSlot(timetable)!=null&&timetable.lessonName!=null){
				if(timetable.teacherName==null){
					str = mCourseDateUtil.getTimeSlot(timetable) + " "+timetable.lessonName+" ";
				}
				else{
					str = mCourseDateUtil.getTimeSlot(timetable) + " "+timetable.lessonName+" "+timetable.teacherName;
				}
			   }
				courses.add(str);
			}
		}
		return courses;
	}
	
	public ArrayList<String> getPmCourseStrs(List<TimeTable> datas){
		if( datas == null ) return null;
		ArrayList<String> courses = new ArrayList<String>();
		for( TimeTable timetable:datas){
			if( timetable.ampm.equals("1") ){
				String str=" ";
				if(mCourseDateUtil.getTimeSlot(timetable)!=null&&timetable.lessonName!=null){
					if(timetable.teacherName==null){
						str = mCourseDateUtil.getTimeSlot(timetable) + " "+timetable.lessonName+" ";
					}
					else{
						str = mCourseDateUtil.getTimeSlot(timetable) + " "+timetable.lessonName+" "+timetable.teacherName;
					}
					// str = mCourseDateUtil.getTimeSlot(timetable) + " "+timetable.lessonName+" "+timetable.teacherName;
				}
				
				courses.add(str);
			}
		}
		return courses;
	}
	public ArrayList<String> getAmCourseTimeAndName(List<TimeTable> datas){
		if( datas == null ) return null;
		ArrayList<String> courses = new ArrayList<String>();
		for( TimeTable timetable:datas){
			if( timetable.ampm.equals("0") ){
				String str = mCourseDateUtil.getTimeSlot(timetable) + " "+timetable.lessonName;
				courses.add(str);
			}
		}
		return courses;
	}
	public ArrayList<String> getPmCourseTimeAndName(List<TimeTable> datas){
		if( datas == null ) return null;
		ArrayList<String> courses = new ArrayList<String>();
		for( TimeTable timetable:datas){
			if( timetable.ampm.equals("1") &&mCourseDateUtil.getTimeSlot(timetable)!=null){
				String str = mCourseDateUtil.getTimeSlot(timetable) + " "+timetable.lessonName;
				courses.add(str);
			}
		}
		return courses;
	}
	public List<TimeTable> getCourseData(Calendar calendar, boolean isExist,boolean isEvenWeek){	
		if( mCourseDateUtil == null ){
			return null;
		}
		return mCourseDateUtil.getCourseData(calendar,isExist,isEvenWeek);
	}
	
	/**
	 * 
	 * @return 学校本学期的开始日期和结束日期
	 */
	public String[] getSchoolBeginEndTime()
	{
		if( mCourseDateUtil == null)
			return null;
		return new String[]{mCourseDateUtil.beginDate,mCourseDateUtil.endDate};
	}
	
	public String[] getWorkEndTime()
	{
		
		if( mCourseDateUtil == null)
			return null;
		return new String[]{mCourseDateUtil.workStarTime,mCourseDateUtil.workEndTime};
	}
	
	public List<SchoolTime> getSchoolTimes()
	{
		if( mCourseDateUtil == null)
			return null;
		return mCourseDateUtil.getSchoolTimes(null);
	}
	
	//下载学校作息时间表
	public static void downloadSchoolTimeFile(final Context context,String version,String url,String groupId,
			final CourseTableHelper.Listener<String> listener,final CourseTableHelper.ErrorListener errorListener){
		File classCacheDir = getCacheDir(context,groupId);
		String filename = "schooltime."+version+".xml";
		Log.d("TAG","作息表:"+url);
		schoolTimeCacheFile = new File(classCacheDir,filename);
				
		if( schoolTimeCacheFile != null && schoolTimeCacheFile.exists() == true ){//学校作息时间表存在
			if( listener != null ){
				listener.onResponse(schoolTimeCacheFile.getAbsolutePath());
			}
			return;
		}
		FileRequest fileRequest = new FileRequest(url,schoolTimeCacheFile.getAbsolutePath(),new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
							if( listener != null ){
								listener.onResponse(response);
							}
					}
			}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							Log.e("TAG", error.getMessage(), error);
							errorListener.onResponse("无法获取到学校作息时间表");
						}
				});
		fileRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(fileRequest);	
	}
	
	//下载节假日时间表,特别的放假信息和补假信息
	public static void downloadHolidayFile(final Context context,String version,String url,String groupId,
			final CourseTableHelper.Listener<String> listener,final CourseTableHelper.ErrorListener errorListener){
		File classCacheDir = getCacheDir(context,groupId);
		String filename = "holiday."+version;
		Log.d("TAG","节假日表"+url);
		holidayCacheFile = new File(classCacheDir,filename);
		if( holidayCacheFile != null && holidayCacheFile.exists() == true ){//学校作息时间表存在
			if( listener != null ){
				listener.onResponse(holidayCacheFile.getAbsolutePath());
			}
			return;
		}
		FileRequest fileRequest = new FileRequest(url,holidayCacheFile.getAbsolutePath(),new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
							if( listener != null ){
								listener.onResponse(response);
							}
					}
			}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							Log.e("TAG", error.getMessage(), error);
							errorListener.onResponse("无法获取到节假日时间表");
						}
				});
		fileRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(fileRequest);	
	}
	
	//下载班级课程表
	public static void downloadTimeTableFile(final Context context,String version,String url,String groupId,
			final CourseTableHelper.Listener<String> listener,final CourseTableHelper.ErrorListener errorListener){
		File classCacheDir = getCacheDir(context,groupId);
		String filename = "timetable."+version;
		courseCacheFile = new File(classCacheDir,filename);
		Log.d("TAG","课表:"+url);
		if( courseCacheFile != null && courseCacheFile.exists() == true ){//课程表存在
			if( listener != null ){
				listener.onResponse(courseCacheFile.getAbsolutePath());
			}
			return;
		}
		FileRequest fileRequest = new FileRequest(url,courseCacheFile.getAbsolutePath(),new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.d("TAG","下载课程表成功");
						if( listener != null ){
							listener.onResponse(response);
						}
					}
			}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							Log.e("TAG", error.getMessage(), error);
							errorListener.onResponse("无法获取到此班级的课程表");
						}
				});
		fileRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(fileRequest);	
	}
	
	private Context mContext = null;
	private int mUnDownloadComplete = 0;
	private CourseDataUtil mCourseDateUtil = null;
	
	public CourseTableHelper(Context context){
		mContext = context;
		mCourseDateUtil = new CourseDataUtil();
	}
	
	private String mTimetableUrl = null;
	private String mTimetableVersion = null;
	private String mSchooltimeUrl = null;
	private String mSchooltimeVersion = null;
	private String mHolidayUrl = null;
	private String mHolidayVersion = null;
	
	private ProgressDialog mProgressDialog = null;
	private static File holidayCacheFile;
	private static File courseCacheFile;
	private static File schoolTimeCacheFile;
	
	private void handleErrorListener(CourseTableHelper.ErrorListener errorListener,String errorMsg){
		mUnDownloadComplete = 0;
		// TODO Auto-generated method stub
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		errorListener.onResponse(errorMsg);
	}
	
	private void handleListener(CourseTableHelper.Listener<String> listener,String msg){
		if( mUnDownloadComplete <= 0 ){//不处理了
			return;
		}
	
		mUnDownloadComplete--;
		if( mUnDownloadComplete <= 0  ){
			// TODO Auto-generated method stub
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			if( listener != null ){
				listener.onResponse(msg);				
			}
		}
	}

	
	public void getSchoolTimeTableAboutXmls(int groupId,final Calendar calendar,final CourseTableHelper.Listener<String> listener,final CourseTableHelper.ErrorListener errorListener){
		if( mSchooltimeVersion == null || mSchooltimeVersion.isEmpty() || mSchooltimeUrl == null || mSchooltimeUrl.isEmpty() ){
			if( errorListener != null ){
				errorListener.onResponse("学校作息时间表地址为空,无法下载学校作息时间表");
			}
			return;
		}
		mUnDownloadComplete = 0;
		
		mUnDownloadComplete++;
		
		//下载学校作息时间表
		downloadSchoolTimeFile(mContext,mSchooltimeVersion,mSchooltimeUrl,String.valueOf(groupId),new CourseTableHelper.Listener<String>(){

			@Override
			public void onResponse(
					String response) {
				// TODO Auto-generated method stub
				//解析学校作息时间表
				Log.d(TAG,"getCourseTableAboutXmls downloadSchoolTimeFile");
				File file = new File(response);  
				try {
					FileInputStream is = new FileInputStream(file);
					mCourseDateUtil.parseSchoolTimeXML(is,calendar);
					handleListener(listener,response);	
					mSchooltimeVersion=null;
					mSchooltimeUrl=null;
					
					return;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "学校作息时间表文件不存在";
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "学校作息时间表文件解析出错";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "学校作息时间表文件解析出错";
				}			
				handleErrorListener(errorListener,response);		
			}
			
		},new CourseTableHelper.ErrorListener(){

			@Override
			public void onResponse(
					String errorMsg) {
				// TODO Auto-generated method stub
				if( errorMsg == null || errorMsg.isEmpty() == true ){
					errorMsg = "下载学校作息时间表失败";
					
				}
				handleErrorListener(errorListener,errorMsg);
			}
			
		});
	}
	
	
	public void getCourseTableAboutXmls(int groupId,final Calendar calendar,
			final CourseTableHelper.Listener<String> listener,final CourseTableHelper.ErrorListener errorListener){
		
		mCalendar=calendar;
		if( mTimetableVersion == null || mTimetableVersion.isEmpty() || mTimetableUrl == null || mTimetableUrl.isEmpty() ){
			if( errorListener != null ){
				errorListener.onResponse("课程表地址为空,无法下载课程表");
			}
			return;
		}
		if( mSchooltimeVersion == null || mSchooltimeVersion.isEmpty() || mSchooltimeUrl == null || mSchooltimeUrl.isEmpty() ){
			if( errorListener != null ){
				errorListener.onResponse("学校作息时间表地址为空,无法下载学校作息时间表");
			}
			return;
		}
		Log.d(TAG,"getCourseTableAboutXmls begin");
//		mProgressDialog = ProgressDialog.show(mContext, "", "...loading...");
		mUnDownloadComplete = 0;
		
		mUnDownloadComplete++;
		mUnDownloadComplete++;
		
		if( mHolidayVersion != null && mHolidayUrl != null && mHolidayUrl.isEmpty() == false && mHolidayVersion.isEmpty() == false ){
			mUnDownloadComplete++;
			//下载节假日时间表,特别的放假信息和补假信息
			downloadHolidayFile(mContext,mHolidayVersion,mHolidayUrl,String.valueOf(groupId),new CourseTableHelper.Listener<String>(){
	
				@Override
				public void onResponse(
						String response) {
					// TODO Auto-generated method stub
					//解析学校作息时间表
					File file = new File(response);  
					try {
						FileInputStream is = new FileInputStream(file);
						mCourseDateUtil.parseHolidyXml(is);
						mHolidayVersion=null;
						mHolidayUrl=null;
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						response = "学校作息时间表文件不存在";
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						response = "学校作息时间表文件解析出错";
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						response = "学校作息时间表文件解析出错";
					}	
					handleListener(listener,response);
				}
				
			},new CourseTableHelper.ErrorListener(){//节假日日期表下载失败没有关系,不影响课程表显示
	
				@Override
				public void onResponse(
						String errorMsg) {
					// TODO Auto-generated method stub
					handleListener(listener,null);	
				}
				
			});
		}
		
		//下载班级课程表
		downloadTimeTableFile(mContext,mTimetableVersion,mTimetableUrl,String.valueOf(groupId),new CourseTableHelper.Listener<String>(){

			@Override
			public void onResponse(
					String response) {
				Log.d(TAG,"getCourseTableAboutXmls downloadTimeTableFile");
				//解析课程表文件
				File file = new File(response);  
				try {
					FileInputStream is = new FileInputStream(file);
					mCourseDateUtil.parseTimeTableXml(is);
					
					handleListener(listener,response);	
					mTimetableVersion=null;
					mTimetableUrl=null;
					
					return;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "课程表文件不存在";
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "课程表文件解析出错";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "课程表文件解析出错";
				}			
				handleErrorListener(errorListener,response);			
			}
			
		},new CourseTableHelper.ErrorListener(){

			@Override
			public void onResponse(
					String errorMsg) {			
				if( errorMsg == null || errorMsg.isEmpty() == true ){
					errorMsg = "下载课程表失败";
					
				}
				handleErrorListener(errorListener,errorMsg);
			}
			
		});
		
		
		//下载学校作息时间表
		downloadSchoolTimeFile(mContext,mSchooltimeVersion,mSchooltimeUrl,String.valueOf(groupId),new CourseTableHelper.Listener<String>(){

			@Override
			public void onResponse(
					String response) {
				// TODO Auto-generated method stub
				//解析学校作息时间表
				Log.d(TAG,"getCourseTableAboutXmls downloadSchoolTimeFile");
				File file = new File(response);  
				try {
					FileInputStream is = new FileInputStream(file);
					mCourseDateUtil.parseSchoolTimeXML(is,calendar);
					handleListener(listener,response);	
					mSchooltimeVersion=null;
					mSchooltimeUrl=null;
					
					return;
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "学校作息时间表文件不存在";
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "学校作息时间表文件解析出错";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response = "学校作息时间表文件解析出错";
				}			
				handleErrorListener(errorListener,response);		
			}
			
		},new CourseTableHelper.ErrorListener(){

			@Override
			public void onResponse(
					String errorMsg) {
				// TODO Auto-generated method stub
				if( errorMsg == null || errorMsg.isEmpty() == true ){
					errorMsg = "下载学校作息时间表失败";
					
				}
				handleErrorListener(errorListener,errorMsg);
			}
			
		});
		
		
	}
	
	public  void getAndParseTimeTableXml(int groupId,final CourseTableHelper.Listener<String> listener,final CourseTableHelper.ErrorListener errorListener){
		final ProgressDialog progressDialog = ProgressDialog.show(mContext, "", "...loading...");
		
		//下载班级课程表
		downloadTimeTableFile(mContext,mTimetableVersion,mTimetableUrl,String.valueOf(groupId),new CourseTableHelper.Listener<String>(){

			@Override
			public void onResponse(
					String response) {
				// TODO Auto-generated method stub
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				if( response != null ){
					File file = new File(response);  
					FileInputStream is;
					try {
						is = new FileInputStream(file);
						mCourseDateUtil.parseTimeTableXml(is);
						listener.onResponse("课程表解析成功");
						mTimetableVersion=null;
						mTimetableUrl=null;
						return;
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						errorListener.onResponse("课程表文件不存在");
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						errorListener.onResponse("课程表文件解析出错");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						errorListener.onResponse("课程表文件解析出错");
					}  					
				}
			}
			
		},new CourseTableHelper.ErrorListener(){

			@Override
			public void onResponse(
					String errorMsg) {
				// TODO Auto-generated method stub
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				if( errorMsg == null || errorMsg.isEmpty() == true ){
					errorListener.onResponse("下载课程表文件失败");
				}else{
					errorListener.onResponse(errorMsg);
				}
			}
			
		});
	}
	
	//获取终端考勤信息接口
	public void getSchoolAttendance(final int groupId,final CourseTableHelper.Listener<String> listener){	

		String urlString = SmartCampusUrlUtils.getSchoolAttendanceUrl(groupId,null);	
		Log.d(TAG, "getSchoolAttendance urlString " + urlString);
		Log.d("TAG", "getSchoolAttendance urlString " + urlString);
		final String groupStr = String.valueOf(groupId);
			
		XMLRequest xmlRequest = new XMLRequest(urlString,new Response.Listener<XmlPullParser>() {
					@Override
					public void onResponse(XmlPullParser response) {
						try {
							int eventType = response.getEventType();
//							Log.d(TAG, "getSchoolAttendance　response is " + response.getText());
							while (eventType != XmlPullParser.END_DOCUMENT) {
								switch (eventType) {
								case XmlPullParser.START_TAG:
									String nodeName = response.getName();
									if ("timetable".equals(nodeName)) {
										//获取班级课程表
										mTimetableUrl = response.getAttributeValue(null, "url");
										mTimetableVersion = response.getAttributeValue(null, "version");
										
//										getTimeTable(context,timetableurl,listener);
										Log.d(TAG, "getSchoolAttendance　timetableurl is " 
												+ mTimetableUrl+",timetableVersion"+mTimetableVersion);
									}else if ("schooltime".equals(nodeName)) {
										mSchooltimeUrl = response.getAttributeValue(null, "url");
										mSchooltimeVersion = response.getAttributeValue(null, "version");

										Log.d(TAG, "getSchoolAttendance　schooltimeUrl is " 
												+ mSchooltimeUrl+",schooltimeVersion"+mSchooltimeVersion);
									}else if ("holiday".equals(nodeName)) {
										//获取班级课程表
										mHolidayUrl = response.getAttributeValue(null, "url");
										mHolidayVersion = response.getAttributeValue(null, "version");
										Log.d(TAG, "getSchoolAttendance　holidayUrl is " 
												+ mHolidayUrl+",holidayVersion"+mHolidayVersion);
									}
									break;
								}
								eventType = response.next();
							}
							if( listener != null ){
								listener.onResponse("获取终端考勤信息成功");
							}
							
						} catch (XmlPullParserException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("TAG", error.getMessage(), error);
						listener.onResponse("Failed");
						Toast.makeText(mContext, "无法获取到此班级的考勤信息", Toast.LENGTH_LONG).show();
					}
				});
		xmlRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(xmlRequest);		
	}
}
