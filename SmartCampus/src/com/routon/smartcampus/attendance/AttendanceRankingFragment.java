package com.routon.smartcampus.attendance;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonArrayRequest;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.TimeUtils;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.ClassListBean;
import com.routon.smartcampus.coursetable.CourseDataUtil.TimeTable;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.coursetable.CourseTableHelper;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.ClassSelListViewAdapter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AttendanceRankingFragment extends Fragment  implements OnClickListener {
	private static final String TAG = "AttendanceActivity";
	private ArrayList<GroupInfo> mClassGroups = new ArrayList<GroupInfo>();
	private ArrayList<TimeTable> mCourseGroups = new ArrayList<TimeTable>();
	private int mClassId;//选中的班级
	private Calendar mSelCalendar;//选中的日期
	private int mLessonId;//选中的课程索引
	private CourseTableHelper mCourseTableHelper = null;
	private boolean isEvenWeek;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_attendance_ranking, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mCourseTableHelper = new CourseTableHelper(getOwnActivity());
		
		initViews(getView());

		initData();
	}
	
	private void initData() {
		getClassListData();	
	}
	
	private Activity getOwnActivity(){
		return this.getActivity();
	}
	
	private void getClassListData() {

		String urlString = SmartCampusUrlUtils.getClassListURl();
		showProgressDialog();
		CookieJsonArrayRequest jsonObjectRequest = new CookieJsonArrayRequest(urlString,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						Log.d(TAG, "response=" + response);
//						hideProgressDialog();
						mClassGroups.clear();
						ClassListBean.parseGroupList(mClassGroups,response);

						ArrayList<String> classlist = new ArrayList<String>();
						for (int i = 0; i < mClassGroups.size(); i++) {
							classlist.add(mClassGroups.get(i).getName());
						}
						if ( classlist!=null && classlist.size()>0) {
							mClassText.setText(classlist.get(0));
							mClassId = mClassGroups.get(0).getId();
							mClassListView.setAdapter(new ClassSelListViewAdapter(getOwnActivity(), classlist));						
						}
						
						mDateText.setEnabled(true);
						mClassId = mClassGroups.get(0).getId();
						//获取班级课程表
						getEvenWeek(mClassId);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						hideProgressDialog();
						Log.e(TAG, "sorry,Error");
						Toast.makeText(getOwnActivity(), "网络连接失败!", Toast.LENGTH_LONG).show();

					}
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	void getSchoolAttendance(){
		
		mCourseTableHelper.getSchoolAttendance(mClassId, new CourseTableHelper.Listener<String>(){

			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				mCourseTableHelper.getAndParseTimeTableXml(mClassId, new CourseTableHelper.Listener<String>(){

					@Override
					public void onResponse(String response) {
//						 hideProgressDialog();
						// TODO Auto-generated method stub
						mCourseGroups.clear();
						ArrayList<TimeTable> courseList=(ArrayList<TimeTable>) mCourseTableHelper.getCourseData(mSelCalendar,true,isEvenWeek);
						if (courseList!=null) {
							mCourseGroups.addAll(courseList);
							//reset course text
							setSelCourse(0);
							//init course list
							initCourseList();
							//get student absence list data
							getStudentAttendanceListData(mClassId,mLessonId,mShowSdf.format(mSelCalendar.getTime()));
							mCourseText.setClickable(true);
						}else {
							mCourseText.setText("无课程");
							mCourseText.setClickable(false);
							mCourseText.setEnabled(false);
							
							AttendanceRankingAdapter adapter = new AttendanceRankingAdapter(getContext(), 
									new ArrayList<AttendanceBean>());
							mStudentGridView.setAdapter(adapter);
						}
						
					}
					
				}, new CourseTableHelper.ErrorListener() {
					
					@Override
					public void onResponse(String errorMsg) {
						// TODO Auto-generated method stub
						 hideProgressDialog();
						 mStudentGridView.setAdapter(null);
					}
				});
			}
			
		});
	}
	
	private View mDropdownMask = null;
	private FrameLayout mClassDropdownFl = null;
	private FrameLayout mCourseDropdownFl = null;
	private ListView mClassListView = null;
	private ListView mCourseListView = null;
	private TextView mClassText = null;
	private TextView mCourseText = null;
	private TextView mDateText = null;
	private boolean mPopViewShow = true;
	//学生排名列表
	private ListView mRankingListView = null;
	private GridView mStudentGridView = null;
	private boolean isClassListViewShow=true;
	private boolean isCourseListViewShow=true;
	
	private void initViews(View view) {
		
		mDropdownMask = view.findViewById(R.id.dropdown_mask);
		mDropdownMask.setOnClickListener(this);
		mRankingListView = (ListView)view.findViewById(R.id.student_name_listview);
		mStudentGridView = (GridView)view.findViewById(R.id.student_name_gridview);		
		
		mClassText = (TextView)view.findViewById(R.id.class_edit_tv);
		mClassText.setOnClickListener(this);
		mClassDropdownFl = (FrameLayout) view.findViewById(R.id.dropdown_fl_class);
		mCourseDropdownFl = (FrameLayout) view.findViewById(R.id.dropdown_fl_course);
		mClassListView = (ListView) view.findViewById(R.id.dropdown_listview_class);
		mCourseListView = (ListView) view.findViewById(R.id.dropdown_listview_course);
		mClassListView.setOnItemClickListener(new OnItemClickListener() {
			

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {				
				mClassText.setText(mClassGroups.get(position).getName());	
				mClassId = mClassGroups.get(position).getId();
				
				//reset course text
				mCourseText.setText("");
				mCourseText.setEnabled(false);
				
				dropdownClick(0);
				
				//重新获取课程信息
				getSchoolAttendance();
			}
		});
		
		mCourseListView.setOnItemClickListener(new OnItemClickListener() {
			

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		
				setSelCourse(position);
				dropdownClick(1);
				
				getStudentAttendanceListData(mClassId,mLessonId,mShowSdf.format(mSelCalendar.getTime()));
			}
		});
		
		mCourseText = (TextView)view.findViewById(R.id.course_edit_tv);
		mCourseText.setText("");
		mCourseText.setOnClickListener(this);
		mCourseText.setEnabled(false);
		mLessonId = 1;
		
		//init date text current date
		mSelCalendar = Calendar.getInstance();
		mDateText = (TextView)view.findViewById(R.id.date_edit_tv);
		mDateText.setOnClickListener(this);
		mDateText.setEnabled(false);
		setDateText(mSelCalendar);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dropdown_mask:
			if (!mPopViewShow) {
				if (!isClassListViewShow) {
					listViewOutAnim(0);
				} else if (!isCourseListViewShow){
					listViewOutAnim(1);
				}
			}
			break;
		case R.id.class_edit_tv:
			dropdownClick(0);
			break;
		case R.id.course_edit_tv:
			dropdownClick(1);
			break;
		case R.id.date_edit_tv:
			showDatePicker();
			break;

		default:
			break;
		}
	}
	
	private void setListViewPosition(FrameLayout classDropdownView, View leftview) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) classDropdownView.getLayoutParams();		
		int[] location = new int[2];  
		leftview.getLocationOnScreen(location);     
        params.setMargins(leftview.getWidth(), 0, 0, 0);  
        classDropdownView.setLayoutParams(params);		
	}
	
	private void listViewOutAnim(int type) {
		if (type == 0) {
			mClassListView.clearAnimation();
			mClassListView.setVisibility(View.GONE);
			mClassListView.startAnimation(AnimationUtils.loadAnimation(getOwnActivity(), R.anim.dd_menu_out));
			mClassDropdownFl.startAnimation(AnimationUtils.loadAnimation(getOwnActivity(), R.anim.dd_mask_out));
			mClassDropdownFl.setVisibility(View.GONE);
			isClassListViewShow=true;
		}else if (type == 1) {
			mCourseListView.clearAnimation();
			mCourseListView.setVisibility(View.GONE);
			mCourseListView.startAnimation(AnimationUtils.loadAnimation(getOwnActivity(), R.anim.dd_menu_out));
			mCourseDropdownFl.startAnimation(AnimationUtils.loadAnimation(getOwnActivity(), R.anim.dd_mask_out));
			mCourseDropdownFl.setVisibility(View.GONE);
			isCourseListViewShow=true;
		}
		mDropdownMask.setVisibility(View.GONE);
		mDropdownMask.startAnimation(AnimationUtils.loadAnimation(getOwnActivity(), R.anim.dd_mask_in));
		mPopViewShow = true;
	}
	
	private void listViewInAnim(int type) {
		if (type == 0) {
			mClassListView.clearAnimation();
			mClassListView.setVisibility(View.VISIBLE);
			mClassListView.startAnimation(AnimationUtils.loadAnimation(getOwnActivity(), R.anim.dd_menu_in));
			mClassDropdownFl.setVisibility(View.VISIBLE);
			isClassListViewShow=false;
			setListViewPosition(mClassDropdownFl,this.getView().findViewById(R.id.class_tv));
		}else if (type == 1) {
			mCourseListView.clearAnimation();
			mCourseListView.setVisibility(View.VISIBLE);
			mCourseListView.startAnimation(AnimationUtils.loadAnimation(getOwnActivity(), R.anim.dd_menu_in));
			mCourseDropdownFl.setVisibility(View.VISIBLE);
			isCourseListViewShow=false;
			setListViewPosition(mCourseDropdownFl,this.getView().findViewById(R.id.course_tv));
		}

		mDropdownMask.setVisibility(View.VISIBLE);
		mDropdownMask.startAnimation(AnimationUtils.loadAnimation(getOwnActivity(), R.anim.dd_mask_in));
		mPopViewShow = false;

	}
	
	private SimpleDateFormat mShowSdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);
	
	private void showDatePicker() {
		// 不能设置小于当前时间的时间
		Calendar curTime = Calendar.getInstance();
		final DatePickerDialog dateDialog = new DatePickerDialog(getOwnActivity(),
				new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						// TODO Auto-generated method stub
					}
				}, curTime.get(Calendar.YEAR), curTime.get(Calendar.MONTH),
				curTime.get(Calendar.DAY_OF_MONTH));
		//设置查询时间不能超过今天
		dateDialog.getDatePicker().setMaxDate(curTime.getTimeInMillis());
		dateDialog.setButton(DialogInterface.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				// TODO Auto-generated method stub
				// 确认修改时间
				final DatePicker datePicker = dateDialog.getDatePicker();
				final Calendar confirmTime = Calendar.getInstance();
				confirmTime.set(datePicker.getYear(), datePicker.getMonth(),
						datePicker.getDayOfMonth(), 0, 0);
				
				dateStr = setDateText(confirmTime);			
				mSelCalendar = confirmTime;
				getEvenWeek(mClassId);
				
//				setSelCourse(0);
//				
//				getStudentAttendanceListData(mClassId,mLessonId,dateStr);
			}
		});
		dateDialog.show();
	}
	
	
	private String setDateText(Calendar time){
		dateStr = mShowSdf.format(time.getTime());
		mDateText.setText(dateStr);
		return dateStr;
	}
	
	private void dropdownClick(int type) {
		if ( mPopViewShow) {
			listViewInAnim(type);
		} else {
			listViewOutAnim(type);
		}
	}
	
	private ProgressDialog progressDialog = null;
	private String dateStr="";
	
	private void initStudentGridView(ArrayList<AttendanceBean> datalist, ClassStudentData classStudentData){
		
		if (classStudentData!=null) {
			ArrayList<AttendanceBean> studentdatalist=classStudentData.studentdatalist;
			for (int i = 0; i < datalist.size(); i++) {
				for (int j = 0; j < studentdatalist.size(); j++) {
					if (studentdatalist.get(j).sid==datalist.get(i).sid) {
						datalist.get(i).absenceCount=studentdatalist.get(j).absenceCount;
						datalist.get(i).imgSavePath=studentdatalist.get(j).imgSavePath;
					}
				}
			}
		}
		
		AttendanceRankingAdapter adapter = new AttendanceRankingAdapter(getContext(), 
				datalist);
		mStudentGridView.setAdapter(adapter);
		mStudentGridView.setNumColumns(3);
		mStudentGridView.setVisibility(View.VISIBLE);
		mRankingListView.setVisibility(View.GONE);
	}
	
	
	//设置选择的课程
	private void setSelCourse(int position){
		mCourseText.setText(mCourseGroups.get(position).getFormatCourseStr());
		mLessonId = Integer.parseInt(mCourseGroups.get(position).lesson);	
	}
	
	private void initCourseList(){
		int weekday = mSelCalendar.get(Calendar.DAY_OF_WEEK) - 1;//周几
		ArrayList<String> datas = new ArrayList<String>();
		for( TimeTable bean: mCourseGroups ){
			datas.add(bean.getFormatCourseStr());
		}	
		mCourseListView.setAdapter(new ClassSelListViewAdapter(getOwnActivity(), datas));
		mCourseText.setEnabled(true);
	}
	
	private void getStudentAttendanceListData(final Integer groupId,int lessonId, String timeStr) {	
		showProgressDialog();
		String urlString = SmartCampusUrlUtils.getAttendResultUrl(String.valueOf(groupId),timeStr, String.valueOf(lessonId));
		Log.d(TAG,"getStudentAttendanceListData urlString:"+urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "response=" + response);
							 hideProgressDialog();
							int code = response.optInt("code");
							if( code == 0 ){//成功
								StudentAttendanceBean bean = StudentAttendanceBean.parseStudentAttendanceBean(response.optJSONObject("datas"));
								if( bean != null && bean.studentlist != null && bean.studentlist.size() > 0 ){
									ClassStudentData[] mAllStudentDataList=AttendanceApplication.mAllStudentDataList;
									ClassStudentData classStudentData = null;
									if (mAllStudentDataList==null) {
										Toast.makeText(getActivity(), "获取学生数据失败", Toast.LENGTH_SHORT).show();
										return;
									}
									for (int i = 0; i < mAllStudentDataList.length; i++) {
										if (mAllStudentDataList[i].groupId.equals(String.valueOf(groupId))) {
											classStudentData=mAllStudentDataList[i];
										}
									}
									initStudentGridView(bean.studentlist,classStudentData);
								}else{
									Toast.makeText(getOwnActivity(), "无缺勤学生", Toast.LENGTH_SHORT).show();
									AttendanceRankingAdapter adapter = new AttendanceRankingAdapter(getContext(), 
											new ArrayList<AttendanceBean>());
									mStudentGridView.setAdapter(adapter);
								}
							}else if ( code == -2) {
								InfoReleaseApplication.returnToLogin(getOwnActivity());
							} else {
								String msg = response.optString("msg");
								if( msg == null || msg.isEmpty() == true ){
									Toast.makeText(getOwnActivity(), "获取缺勤数据失败", Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(getOwnActivity(), msg, Toast.LENGTH_LONG).show();
								}
							}

						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
							 hideProgressDialog();
							String msg = arg0.getMessage();
							if( msg == null || msg.isEmpty() == true ){
								Toast.makeText(getOwnActivity(), "获取考勤数据失败", Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(getOwnActivity(), msg, Toast.LENGTH_LONG).show();
							}
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}
	
	
	private void getEvenWeek(int groupId) {	
		showProgressDialog();
		String urlString = SmartCampusUrlUtils.getWeekeven(groupId,dateStr);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "response=" + response);
							hideProgressDialog();
							int code = response.optInt("code");
							if( code == 0 ){
								
								try {
									JSONObject jsonObject = response.getJSONObject("datas");
									int weekeven=jsonObject.optInt("weekeven");
									if (weekeven==2) {
										isEvenWeek=true;
									}else {
										isEvenWeek=false;
									}
									getSchoolAttendance();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}else if ( code == -2) {
								InfoReleaseApplication.returnToLogin(getOwnActivity());
							} else {
								String msg = response.optString("msg");
							    Toast.makeText(getOwnActivity(), msg, Toast.LENGTH_LONG).show();
								
							}

						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
							hideProgressDialog();
						    Toast.makeText(getOwnActivity(), "获取课程表失败",Toast.LENGTH_SHORT).show();		
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}
	
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if (hidden) {
			if (!mPopViewShow) {
				if (!isClassListViewShow) {
					listViewOutAnim(0);
				} else if (!isCourseListViewShow){
					listViewOutAnim(1);
				}

			}
		}
	}
	
	private void showProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(getActivity(), "", "...loading...");
		}
	}

	private void hideProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

}
