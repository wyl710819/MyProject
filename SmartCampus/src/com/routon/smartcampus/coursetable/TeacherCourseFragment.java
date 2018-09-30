package com.routon.smartcampus.coursetable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.routon.smartcampus.coursetable.TeacherCourseBean.MyCourse;
import com.routon.smartcampus.network.SmartCampusUrlUtils;

public class TeacherCourseFragment extends Fragment{
	
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
	private CourseAdapter courseAdapter;
	private ProgressDialog progressDialog;
	private ClassCourseActivity classCourseActivity;
	private int classGroupId;
	private String teacherId;
	private int userId;
	
	private static final String TAG="TeacherCourseFagment";
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_teacher_course, container, false);
		classGroupId=getArguments().getInt("classGroupId", 0);
		teacherId = getArguments().getString("sid", "");
		if(!teacherId.equals("")){
			userId = InfoReleaseApplication.authenobjData.userId;
		}
		Log.d(TAG, "classGroupId:"+classGroupId);
		initView(view);
		initData();
		return view;
	}

	private void initView(View view) {
		WeekCalendarView weekCalendarView = (WeekCalendarView) view.findViewById(R.id.weekCalendarView);
		weekCalendarView.setOnChangeListener(new WeekCalendarListener() {

			@Override
			public void WeekCalendarClickListener(String dateStr) {
				
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
		if(classGroupId != 0){
			getTeacherTimetableLists(classGroupId);
		}else {
			getTeacherCourseLists(userId);
		}
	}

	// 初始化adapter,绘制网格线
	private void initAdapter(List<MyCourse> mAmCourseList,
			List<MyCourse> mPmCourseList, ListView mAmListView,
			ListView mPmListView) {
		// TODO Auto-generated method stub
		courseAdapter = new CourseAdapter(mAmCourseList, getContext(), teacherId);
		mAmListView.setAdapter(courseAdapter);
		courseAdapter = new CourseAdapter(mPmCourseList, getContext(), teacherId);
		mPmListView.setAdapter(courseAdapter);
	}

	public void getTeacherTimetableLists(int classGroupId) {
		clearData();
		String urlString = SmartCampusUrlUtils.getClassCoursesUrl(String.valueOf(classGroupId));
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
								JSONArray jsonArray = datas.optJSONArray("timetables");
								int len = jsonArray.length();
								List<TeacherCourseBean> allCoursesList = new ArrayList<TeacherCourseBean>();
								for (int k = 1; k <= 5; k++) {
									allCoursesList.add(null);
									showCourseDetial(k,allCoursesList);
								}
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) jsonArray.get(i);
									TeacherCourseBean bean = new TeacherCourseBean(obj);
									// 判断week是周几，存在week将数据放入对应日期集合，不存在则赋空
									for (int k = 1; k <= 5; k++) {
										if (bean != null && bean.week == k) {
											allCoursesList.set(k - 1, bean);
										}
									}
								}
								for (int i = 0; i < allCoursesList.size(); i++) {
									if (allCoursesList.get(i) != null) {
										showCourseDetial(allCoursesList.get(i).week,allCoursesList);
									}
								}
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(classCourseActivity);
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

						if (InfoReleaseApplication.showNetWorkFailed(classCourseActivity) == true) {
							// showToast("按日期查询作业失败!");
						}
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	public void getTeacherCourseLists(int teacherId) {
		clearData();
		String urlString=SmartCampusUrlUtils.getTeacherTimetablesUrl(String.valueOf(teacherId));
		Log.d(TAG, "url:"+urlString);
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
				Log.d(TAG, "response=" + response);
				hideMyProgressDialog();
				try {
					if (response.getInt("code") == 0) {
					JSONObject datas=response.optJSONObject("datas");
					JSONArray jsonArray = datas.optJSONArray("timetables");				
					int len = jsonArray.length();
					List<TeacherCourseBean> allCoursesList =new ArrayList<TeacherCourseBean>();
					for(int k=0;k<5;k++){
						allCoursesList.add(null);
					}		
							for(int i = 0; i< len; i++){
								JSONObject obj = (JSONObject) jsonArray.get(i);								
								TeacherCourseBean bean=new TeacherCourseBean(obj);
								//判断week是周几，存在week将数据放入对应日期集合，不存在则赋空
								for(int k=1;k<=5;k++){
									if(bean!=null&&bean.week==k){
										allCoursesList.set(k-1,bean);
									}
								}
							}
							for(int i=0;i<allCoursesList.size();i++){
								if(allCoursesList.get(i)!=null){
									showCourseDetial(allCoursesList.get(i).week, allCoursesList);
								}								
							}

					} else if (response.getInt("code") == -2) {
						InfoReleaseApplication.returnToLogin(classCourseActivity);									
					} else {																		
						Log.e(TAG, response.getString("msg"));
//							showToast(response.getString("msg"));
						}
				      } catch (JSONException e) {
							e.printStackTrace();
				     }
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");						
						if( InfoReleaseApplication.showNetWorkFailed(classCourseActivity) == true ){
//							showToast("按日期查询作业失败!");
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
		if(allCoursesList.get(week-1) != null){
			for (int j = 0; j < allCoursesList.get(week - 1).courseList.size(); j++) {
				if (allCoursesList.get(week - 1).courseList.get(j).ampm == 0) {
					mAmCourseList.add(allCoursesList.get(week - 1).courseList.get(j));
				} else {
					mPmCourseList.add(allCoursesList.get(week - 1).courseList.get(j));
				}
			}
		}
		courseAdapter = new CourseAdapter(mAmCourseList, getContext(), teacherId);
		mAmListView.setAdapter(courseAdapter);
		courseAdapter = new CourseAdapter(mPmCourseList, getContext(), teacherId);
		mPmListView.setAdapter(courseAdapter);
	}

	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(classCourseActivity, "","...loading...");
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
	@Deprecated
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.classCourseActivity=(ClassCourseActivity) activity;
	};
}
