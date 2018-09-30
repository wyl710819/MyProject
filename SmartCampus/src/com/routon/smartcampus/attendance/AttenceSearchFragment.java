package com.routon.smartcampus.attendance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.coursetable.TeacherCourseBean;
import com.routon.smartcampus.coursetable.WeekCalendarListener;
import com.routon.smartcampus.coursetable.WeekCalendarView;
import com.routon.smartcampus.coursetable.TeacherCourseBean.MyCourse;
import com.routon.smartcampus.network.SmartCampusUrlUtils;

public class AttenceSearchFragment extends Fragment{

	private ListView monAmListView;
	private ListView tuesAmListView;
	private ListView wednsAmListView;
	private ListView thurAmListView;
	private ListView friAmListView;	
	private ListView monPmListView;
	private ListView tuesPmListView;
	private ListView wednsPmListView;
	private ListView thurPmListView;
	private ListView friPmListView;
	private WeekCalendarView weekCalendarView;
	private SearchClassAttenceActivity searchClassAttenceActivity;
	private List<TeacherCourseBean> allCoursesList;
	private List<TeacherCourseBean.MyCourse> monAmCourseList;
	private List<TeacherCourseBean.MyCourse> tuesAmCourseList;
	private List<TeacherCourseBean.MyCourse> wednsAmCourseList;
	private List<TeacherCourseBean.MyCourse> thurAmCourseList;
	private List<TeacherCourseBean.MyCourse> friAmCourseList;
	private List<TeacherCourseBean.MyCourse> monPmCourseList;
	private List<TeacherCourseBean.MyCourse> tuesPmCourseList;
	private List<TeacherCourseBean.MyCourse> wednsPmCourseList;
	private List<TeacherCourseBean.MyCourse> thurPmCourseList;
	private List<TeacherCourseBean.MyCourse> friPmCourseList;
	private ClassAttenceAdapter classAttenceAdapter;
	private ProgressDialog progressDialog;
	private int classGroupId;
	private String teacherId;
	private String className;
	private String startDay;
	private String endDay;
	private String today;
	private SimpleDateFormat sdf;
	private Date nowDate;

	private static final String TAG="TeacherCourseFagment";
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_teacher_course, container, false);
		Bundle bundle = getArguments();
		classGroupId=bundle.getInt("classGroupId", 0);
		teacherId = String.valueOf(bundle.getInt("sid", 0));
		Log.d(TAG, "classGroupId="+classGroupId+"   className="+className+"   teacherId="+teacherId);
		initView(view);
		initData();
		return view;
	}

	private void initView(View view) {
		searchClassAttenceActivity.setWeekChangeListener(new WeekChangeListener() {
			
			@Override
			public void onPre() {
				weekCalendarView.moveRight();
			}
			
			@Override
			public void onNext() {
				weekCalendarView.moveLeft();
			}
		});
		weekCalendarView = (WeekCalendarView) view.findViewById(R.id.weekCalendarView);
		searchClassAttenceActivity.setTouchUnDealView(weekCalendarView);
		weekCalendarView.setMoveEnable();
		weekCalendarView.setOnChangeListener(new WeekCalendarListener() {


			@Override
			public void WeekCalendarClickListener(String dateStr) {
				Log.d(TAG, "select date="+dateStr);
				try {
					Date date = sdf.parse(dateStr);
					if(date.getTime() > nowDate.getTime()){
						Log.d(TAG, "所选时间大于当前时间");
						clearData();
						allCoursesList.clear();
						for (int k = 1; k <= 5; k++) {
							showCourseDetial(k,allCoursesList);
						}
						return;
					}
					if(date.getDate() == nowDate.getDate()){
						getTimeInterval(date);
						getClassWeekAttence(classGroupId, startDay, today);
					}else {
						getTimeInterval(date);
						getClassWeekAttence(classGroupId, startDay, endDay);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
		monAmListView=(ListView) view.findViewById(R.id.monday_am_course);
		tuesAmListView=(ListView) view.findViewById(R.id.tues_am_course);
		wednsAmListView=(ListView) view.findViewById(R.id.wednes_am_course);
		thurAmListView=(ListView) view.findViewById(R.id.thur_am_course);
		friAmListView=(ListView) view.findViewById(R.id.fri_am_course);
		
		monPmListView=(ListView) view.findViewById(R.id.monday_pm_course);
		tuesPmListView=(ListView) view.findViewById(R.id.tues_pm_course);
		wednsPmListView=(ListView) view.findViewById(R.id.wednes_pm_course);
		thurPmListView=(ListView) view.findViewById(R.id.thur_pm_course);
		friPmListView=(ListView) view.findViewById(R.id.fri_pm_course);
		Calendar calendar=Calendar.getInstance();
		int mWeek = calendar.get(Calendar.DAY_OF_WEEK); 
		switch(mWeek){
		case 2:
			monAmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			monPmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			break;
		case 3:
			tuesAmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			tuesPmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			break;
		case 4:
			wednsAmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			wednsPmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			break;
		case 5:
			thurAmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			thurPmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			break;
		case 6:
			friAmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			friPmListView.setBackgroundResource(R.drawable.teahcer_course_currday_blue_rectangle_shape);
			break;
		default:
			break;
		}
	}
	private void initData(){	
		allCoursesList = new ArrayList<TeacherCourseBean>();
		monAmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		tuesAmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		wednsAmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		thurAmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		friAmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		
		monPmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		tuesPmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		wednsPmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		thurPmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		friPmCourseList=new ArrayList<TeacherCourseBean.MyCourse>();
		//初始化adapter
		initAdapter(monAmCourseList,monPmCourseList,monAmListView,monPmListView);
		initAdapter(tuesAmCourseList,tuesPmCourseList,tuesAmListView,tuesPmListView);
		initAdapter(wednsAmCourseList,wednsPmCourseList,wednsAmListView,wednsPmListView);
		initAdapter(thurAmCourseList,thurPmCourseList,thurAmListView,thurPmListView);
		initAdapter(friAmCourseList,friPmCourseList,friAmListView,friPmListView);
		sdf = new SimpleDateFormat("yyyy-MM-dd");
		nowDate = new Date();
		today = sdf.format(nowDate);
		getTimeInterval(nowDate);
		getClassWeekAttence(classGroupId, startDay, today);
	}

	// 初始化adapter,绘制网格线
	private void initAdapter(List<MyCourse> mAmCourseList,
			List<MyCourse> mPmCourseList, ListView mAmListView,
			ListView mPmListView) {
		// TODO Auto-generated method stub
		classAttenceAdapter = new ClassAttenceAdapter(mAmCourseList, getContext(), teacherId, classGroupId);
		mAmListView.setAdapter(classAttenceAdapter);
		classAttenceAdapter = new ClassAttenceAdapter(mPmCourseList, getContext(), teacherId, classGroupId);
		mPmListView.setAdapter(classAttenceAdapter);
	}

	public void getClassWeekAttence(int classGroupId, String startDate, String endDate) {
		clearData();
		String urlString = SmartCampusUrlUtils.getClassWeekAttenceUrl(String.valueOf(classGroupId), startDate, endDate);
		Log.d(TAG, "url="+urlString);
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								JSONObject datas = response.optJSONObject("datas");
								JSONArray jsonArray = datas.optJSONArray("atts");
								int len = jsonArray.length();
								allCoursesList.clear();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) jsonArray.get(i);
									TeacherCourseBean bean = new TeacherCourseBean(obj);
									// 判断week是周几，存在week将数据放入对应日期集合，不存在则赋空
									for (int k = 1; k <= 5; k++) {
										if (bean != null && bean.week == k) {
											allCoursesList.add(bean);
										}
									}
								}
								for (int i = 0; i < allCoursesList.size(); i++) {
									if (allCoursesList.get(i) != null) {
										List<MyCourse> courses = allCoursesList.get(i).courseList;
										for(MyCourse course : courses){
											course.day = allCoursesList.get(i).day;
										}
									}
									showCourseDetial(allCoursesList.get(i).week,allCoursesList);
								}
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(searchClassAttenceActivity);
							} else {
								Log.e(TAG, response.getString("msg"));
								// showToast(response.getString("msg"));
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");

						if (InfoReleaseApplication.showNetWorkFailed(searchClassAttenceActivity) == true) {
							// showToast("按日期查询作业失败!");
						}
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void showCourseDetial(int week,List<TeacherCourseBean> allCoursesList) {
		switch (week) {
		case 1:
			loadCourseData(week, allCoursesList, monAmCourseList,
					monPmCourseList, monAmListView, monPmListView);
			break;
		case 2:
			loadCourseData(week, allCoursesList, tuesAmCourseList,
					tuesPmCourseList, tuesAmListView, tuesPmListView);
			break;
		case 3:
			loadCourseData(week, allCoursesList, wednsAmCourseList,
					wednsPmCourseList, wednsAmListView, wednsPmListView);
			break;
		case 4:
			loadCourseData(week, allCoursesList, thurAmCourseList,
					thurPmCourseList, thurAmListView, thurPmListView);
			break;
		case 5:
			loadCourseData(week, allCoursesList, friAmCourseList,
					friPmCourseList, friAmListView, friPmListView);
			break;
		default:
			break;
		}
	}

	private void loadCourseData(int week,List<TeacherCourseBean> allCoursesList,
			List<MyCourse> mAmCourseList, List<MyCourse> mPmCourseList,
			ListView mAmListView, ListView mPmListView) {
		if(allCoursesList.size()>week-1){
			if(allCoursesList.get(week-1) != null){
				for (int j = 0; j < allCoursesList.get(week - 1).courseList.size(); j++) {
					if (allCoursesList.get(week - 1).courseList.get(j).ampm == 0) {
						mAmCourseList.add(allCoursesList.get(week - 1).courseList.get(j));
					} else {
						mPmCourseList.add(allCoursesList.get(week - 1).courseList.get(j));
					}
				}
			}
		}
		classAttenceAdapter = new ClassAttenceAdapter(mAmCourseList, getContext(), teacherId, classGroupId);
		mAmListView.setAdapter(classAttenceAdapter);
		classAttenceAdapter = new ClassAttenceAdapter(mPmCourseList, getContext(), teacherId, classGroupId);
		mPmListView.setAdapter(classAttenceAdapter);
	}

	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(searchClassAttenceActivity, "","...loading...");
		}
	}

	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	public void clearData(){
		monAmCourseList.clear();
		tuesAmCourseList.clear();
		wednsAmCourseList.clear();
		thurAmCourseList.clear();
		friAmCourseList.clear();
		monPmCourseList.clear();
		tuesPmCourseList.clear();
		wednsPmCourseList.clear();
		thurPmCourseList.clear();
		friPmCourseList.clear();
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		searchClassAttenceActivity = (SearchClassAttenceActivity)context;
	}
	
    //根据当前日期获取本周周一和周五
	 private void getTimeInterval(Date date) {  
		 
		 Calendar cal = Calendar.getInstance();  
	     cal.setTime(date);  
	     // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了  
	     int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天  
	     if (1 == dayWeek) {  
	        cal.add(Calendar.DAY_OF_MONTH, -1);  
	     }  
	     System.out.println("要计算日期为:" + sdf.format(cal.getTime())); // 输出要计算日期  
	     // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一  
	     cal.setFirstDayOfWeek(Calendar.MONDAY);  
	     // 获得当前日期是一个星期的第几天  
	     int day = cal.get(Calendar.DAY_OF_WEEK);  
	     // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值  
	     cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);  
	     startDay = sdf.format(cal.getTime());  
	     Log.d(TAG, "所在周星期一的日期：" + startDay);  
	     cal.add(Calendar.DATE, 4);  
	     endDay = sdf.format(cal.getTime());  
	     Log.d(TAG, "所在周星期五的日期：" + endDay);  
	}

}
