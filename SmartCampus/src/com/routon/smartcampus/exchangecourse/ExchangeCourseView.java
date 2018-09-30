package com.routon.smartcampus.exchangecourse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.coursetable.TeacherCourseBean;
import com.routon.smartcampus.coursetable.TeacherCourseBean.MyCourse;
import com.routon.smartcampus.coursetable.WeekCalendarListener;
import com.routon.smartcampus.coursetable.WeekCalendarView;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

public class ExchangeCourseView extends RelativeLayout implements OnItemClickListener{
	
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
	private ProgressDialog progressDialog;
	private TextView weekModeText;

	private static final String TAG = "ExchangeCourseFagment";
	private Context mContext = null;
	private WeekCalendarView weekCalendarView = null;
	
	public ExchangeCourseView(Context context) {
        super(context);
        mContext = context;
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_exchange_course, this);
        initView(view);
        initData();
    }

	private void initView(View view) {
		weekModeText=(TextView)view.findViewById(R.id.weekModeText);
		weekCalendarView = (WeekCalendarView) view.findViewById(R.id.weekCalendarView);
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
//		Log.e(TAG, "##### monAmListView = "+monAmListView);

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
	
	public void initData(){
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
	}

	// 初始化adapter,绘制网格线
	private void initAdapter(List<MyCourse> mAmCourseList,
			List<MyCourse> mPmCourseList, ListView mAmListView,
			ListView mPmListView) {
		// TODO Auto-generated method stub
		ExchangeCourseAdapter amCourseAdapter = new ExchangeCourseAdapter(mAmCourseList, getContext());
		mAmListView.setAdapter(amCourseAdapter);
		ExchangeCourseAdapter pmCourseAdapter = new ExchangeCourseAdapter(mPmCourseList, getContext());
		mPmListView.setAdapter(pmCourseAdapter);
	}
	
	private boolean ifHasTwoWeek;
	private boolean weekEven = false; 
	public boolean hasTwoWeekCourses(){
		return ifHasTwoWeek;
	}
	
	public boolean getWeekEven(){
		return weekEven;
	}
	
	public interface ResultListener{
		public void onSuccess();
	}

	public void getTeacherTimetableLists(int myClassGroupId, boolean nowOrNextWeek,final ResultListener listener) {
//		Log.e(TAG, "getTeacherTimetableLists: myClassGroupId = "+myClassGroupId);
		clearData();
		((ExchangeCourseAdapter)(monAmListView.getAdapter())).notifyDataSetChanged();
		((ExchangeCourseAdapter)(tuesAmListView.getAdapter())).notifyDataSetChanged();
		((ExchangeCourseAdapter)(wednsAmListView.getAdapter())).notifyDataSetChanged();
		((ExchangeCourseAdapter)(thurAmListView.getAdapter())).notifyDataSetChanged();
		((ExchangeCourseAdapter)(friAmListView.getAdapter())).notifyDataSetChanged();
		((ExchangeCourseAdapter)(monPmListView.getAdapter())).notifyDataSetChanged();
		((ExchangeCourseAdapter)(tuesPmListView.getAdapter())).notifyDataSetChanged();
		((ExchangeCourseAdapter)(wednsPmListView.getAdapter())).notifyDataSetChanged();
		((ExchangeCourseAdapter)(friPmListView.getAdapter())).notifyDataSetChanged();
	
		String urlString = null;
		if ( nowOrNextWeek ==true) {//get this week timetable
			urlString = SmartCampusUrlUtils.getClassCoursesUrl(String.valueOf(myClassGroupId));
		}else{//get next week timetable
			urlString = SmartCampusUrlUtils.getClassCoursesUrlByDate(String.valueOf(myClassGroupId));
		}
		if( nowOrNextWeek == false ){
			 Calendar nextWeek = Calendar.getInstance();
		     nextWeek.setTime(new Date());
		     nextWeek.add(Calendar.DAY_OF_WEEK, 7);
		     Date nextWeekDate = nextWeek.getTime();
			 weekCalendarView.setCurrentDate(nextWeekDate);
		}
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if( getContext() == null ){
							return;
						}
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								JSONObject datas = response.optJSONObject("datas");
								weekEven =datas.optBoolean("week-even");
								//true: 有双周课程 false:无双周课程
								ifHasTwoWeek = datas.optBoolean("timetable-even");
								if( ifHasTwoWeek == true ){
									if ( weekEven == true ) {
										weekModeText.setText("中午 【双周】");
									}else if (weekEven==false) {
										weekModeText.setText("中午 【单周】");
									}
								}else{
									weekModeText.setText("中午");
								}
								
//								classGroupId=datas.optInt("classGroupId");
//								Log.e("classCourseUrl Fragment1111", "classGroupId = "+classGroupId + ", weekEven = " +weekEven);
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
								if( listener != null ){
									listener.onSuccess();
								}
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin((Activity)mContext);//exchangeCourseActivity
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(getContext(), "获取课程表失败:"+response.optString("msg"), Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
							Toast.makeText(getContext(), "获取课程表失败", Toast.LENGTH_LONG).show();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						if( getContext() == null ){
							return;
						}
						if (InfoReleaseApplication.showNetWorkFailed(mContext) == true) {//exchangeCourseActivity
							Toast.makeText(getContext(), "获取课程表失败", Toast.LENGTH_LONG).show();
						}
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	public void getTeacherCourseLists(int teacherId) {
		clearData();
//		Log.e(TAG, "##### getTeacherCourseLists teacherId = "+teacherId);
		String urlString=SmartCampusUrlUtils.getTeacherTimetablesUrl(String.valueOf(teacherId));
//		Log.e(TAG, "##### getTeacherCourseLists url:"+urlString);
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
//				Log.e(TAG, "getTeacherCourseLists response=" + response);
				hideMyProgressDialog();
				try {
					if (response.getInt("code") == 0) {
					JSONObject datas=response.optJSONObject("datas");
					boolean weekEven=datas.optBoolean("week-even");
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
						InfoReleaseApplication.returnToLogin((Activity)mContext);	//exchangeCourseActivity								
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
						if( InfoReleaseApplication.showNetWorkFailed((Activity)mContext) == true ){//exchangeCourseActivity
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
	private String mTeacherId = "";
	
	public void setTeacherId(String teacherId){
		if(teacherId == null ){
			return;
		}
		mTeacherId = teacherId;
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
		ExchangeCourseAdapter amCourseAdapter = new ExchangeCourseAdapter(mAmCourseList, getContext());
		amCourseAdapter.setTeacherSid(mTeacherId);
		mAmListView.setAdapter(amCourseAdapter);
		ExchangeCourseAdapter pmCourseAdapter = new ExchangeCourseAdapter(mPmCourseList, getContext());
		pmCourseAdapter.setTeacherSid(mTeacherId);
		mPmListView.setAdapter(pmCourseAdapter);
		mAmListView.setOnItemClickListener(this);
		mPmListView.setOnItemClickListener(this);
	}

	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show((Activity)mContext, "","...loading...");//exchangeCourseActivity
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
	
	public static class LessonCell{
		View view;
		TeacherCourseBean.MyCourse course;
		ListView listview;
		int weekeven;
		int week;
		public static void clear(LessonCell cell){
			cell.view.setSelected(false);
			((ExchangeCourseAdapter)(cell.listview.getAdapter())).notifyDataSetChanged();
		}
	}
	public interface onCellClick{
		void onCellClick(LessonCell cell);
		void cancelClick(View view);
	}
	private onCellClick mCellListener = null;
	public void setOnCellClick(onCellClick listener){
		mCellListener = listener;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Log.e("### onItemClick", "clicked this: position="+position+"     id="+id+"    view="+arg0.getId());
//		Log.e("### onItemClick", "classGroupIdTemp = "+classGroupIdTemp);
		LinearLayout rootBackGround=(LinearLayout)arg1.findViewById(R.id.exchange_detail_background);	
		if( rootBackGround.isSelected() == true ){//取消选中的课程
			rootBackGround.setSelected(false);
			((ExchangeCourseAdapter)(arg0.getAdapter())).notifyDataSetChanged();
			if( mCellListener != null ){
				mCellListener.cancelClick(rootBackGround);
			}
			return;
		}
		rootBackGround.setSelected(true);
		((ExchangeCourseAdapter)(arg0.getAdapter())).notifyDataSetChanged();
		
		if( mCellListener != null ){
			LessonCell cell = new LessonCell();
			cell.view = rootBackGround;
			if( weekEven == false){
				cell.weekeven = 0;
			}else{
				cell.weekeven = 1;
			}
			
			fillSelectedData(cell,arg0.getId(),position);
			mCellListener.onCellClick(cell);
		}
	}
	
	private void fillSelectedData(LessonCell cell,int viewId,int positon){
		List<TeacherCourseBean.MyCourse> listData = new ArrayList<TeacherCourseBean.MyCourse>();
		ListView myListView = null;
		int weekIndex = 0;
		switch (viewId) {
		case R.id.monday_am_course:
			listData = monAmCourseList;
			Log.e(TAG, "##### monday_am");
			weekIndex = 1;
			myListView = monAmListView;
			break;
		case R.id.tues_am_course:
			listData = tuesAmCourseList;
			Log.e(TAG, "##### tues_am");
			weekIndex = 2;
			myListView = tuesAmListView;
			break;
		case R.id.wednes_am_course:
			listData = wednsAmCourseList;
			Log.e(TAG, "##### wednes_am");
			weekIndex = 3;
			myListView = wednsAmListView;
			break;
		case R.id.thur_am_course:
			listData = thurAmCourseList;
			Log.e(TAG, "##### thur_am");
			weekIndex = 4;
			myListView = thurAmListView;
			break;
		case R.id.fri_am_course:
			listData = friAmCourseList;
			Log.e(TAG, "##### fri_am");
			weekIndex = 5;
			myListView = friAmListView;
			break;
		case R.id.monday_pm_course:
			listData = monPmCourseList;
			Log.e(TAG, "##### monday_pm");
			weekIndex = 1;
			myListView = monPmListView;
			break;
		case R.id.tues_pm_course:
			listData = tuesPmCourseList;
			Log.e(TAG, "##### tues_pm");
			weekIndex = 2;
			myListView = tuesPmListView;
			break;
		case R.id.wednes_pm_course:
			listData = wednsPmCourseList;
			Log.e(TAG, "##### wednes_pm");
			weekIndex = 3;
			myListView = wednsPmListView;
			break;
		case R.id.thur_pm_course:
			listData = thurPmCourseList;
			Log.e(TAG, "##### thur_pm");
			weekIndex = 4;
			myListView = thurPmListView;
			break;
		case R.id.fri_pm_course:
			listData = friPmCourseList;
			Log.e(TAG, "##### fri_pm");
			weekIndex = 5;
			myListView = friPmListView;
			break;
		}	
		Log.e(TAG, "##### myListView = "+myListView);		
		cell.course = listData.get(positon);
		cell.week = weekIndex;
		cell.listview = myListView;
		return;
	}
}

