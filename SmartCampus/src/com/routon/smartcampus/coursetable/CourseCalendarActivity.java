/*package com.routon.smartcampus.coursetable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;

import android.graphics.drawable.BitmapDrawable;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.util.TimeUtils;
import com.routon.smartcampus.answerrelease.BluetoothFragment;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.coursetable.CourseDataUtil.TimeTable;
import com.routon.smartcampus.coursetable.calendarview.CalendarView;
import com.routon.smartcampus.coursetable.calendarview.DateBean;
import com.routon.smartcampus.coursetable.calendarview.listener.OnMonthItemClickListener;
import com.routon.smartcampus.coursetable.calendarview.listener.OnPagerChangeListener;
import com.routon.smartcampus.homework.HomeworkActivity;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.ClassSelListViewAdapter;
import com.routon.smartcampus.student.StudentListFragment;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

public class CourseCalendarActivity extends FragmentActivity implements OnClickListener{
	private CalendarView calendarView;
	private CourseTableHelper mCourseTableHelper = null;
	public static final String TAG = "CourseCalendarActivity";
	private StudentBean mStudent = null;
	private String appType=null;
	private ProgressDialog progressDialog;
	private ProgressDialog progressDialog1;
	private ListView classListView;
	private ArrayList<String> mClassList;
	private TextView classTextView;
	FrameLayout mClassDropdown;
	private boolean mPopViewShow = true;
	private View dropdownMask;
	private LinearLayout classCoursetableLL;
	private ArrayList<Integer> classGroupIdList;
	private List<Integer>positionListAm;
	private List<Integer>positionListPm;
	private String teacherName=null;
	private int teacherId=-1;
	private int userId;
	private int classId=0;
	private List<TimeTable> timetables;
	private boolean mShowCourseDetail = false;
	private boolean isShowPop=false;
	private TextView title_view;
	Calendar calendar;
	TextView  title;
	private boolean isExist=false; 
	private String dateStr="";
	private boolean isEvenWeek;
	private SimpleDateFormat mShowSdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);
	
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private TeacherCourseFragment teacherCourseFragment;
	private LinearLayout calRoot;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.activity_course_calendar);
		Bundle bundle = getIntent().getExtras();
		appType=bundle.getString("AppType");
		
		
		title_view = (TextView) findViewById(R.id.title_view);
		mClassDropdown=(FrameLayout) findViewById(R.id.dropdown_coursetable_class);
		classListView=(ListView)findViewById(R.id.dropdown_listview_coursetable_class);
		classTextView=(TextView) findViewById(R.id.coursetable_class_tv);
		dropdownMask = findViewById(R.id.dropdown_mask_coursetable);
		calRoot=(LinearLayout) findViewById(R.id.cal_root);
		classCoursetableLL=(LinearLayout) findViewById(R.id.tv_class_linear);
		mCourseTableHelper = new CourseTableHelper(this);
		title = (TextView) findViewById(R.id.title_date);
		
		
		if(appType!=null&&appType.equals("TeacherCourseTable")){
			
		   
			dropdownMask.setOnClickListener(this);
			calendarView = (CalendarView) findViewById(R.id.calendar);
		    calendarView.init();
		    
		   // this.setMoveBackEnable(false);
		    
		    DateBean d = calendarView.getDateInit();

	        title.setText(d.getSolar()[0] + "年" + d.getSolar()[1] + "月" + d.getSolar()[2] + "日");
	        calendar = Calendar.getInstance();
	        calendar.set(d.getSolar()[0], d.getSolar()[1]-1, d.getSolar()[2]);
			setDateText(calendar);
			
			teacherName=InfoReleaseApplication.authenobjData.realName;
			userId=InfoReleaseApplication.authenobjData.userId;
			
			getTeacherId(userId,2);
			mStudent = (StudentBean) this.getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
			title_view.setText("课程表");
			classTextView.setVisibility(View.VISIBLE);
			mClassDropdown.setVisibility(View.VISIBLE);
			classCoursetableLL.setVisibility(View.VISIBLE);
			
			getClassListData();
			
			classCoursetableLL.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dropdownClick();
				}
			});
			classListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?>  parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					transaction = fragmentManager.beginTransaction();
					if(position==0){
						classTextView.setText("本周课程");
						if(teacherCourseFragment!=null){
							transaction.show(teacherCourseFragment);
							calRoot.setVisibility(View.GONE);
						}
						
					}else{
						if(teacherCourseFragment!=null&&teacherCourseFragment.isVisible()){
							transaction.hide(teacherCourseFragment);
							calRoot.setVisibility(View.VISIBLE);
						}
						classId=classGroupIdList.get(position-1);
						Log.d(TAG,"班级id"+classId);
						classTextView.setText(mClassList.get(position));
					}
					transaction.commit();
					listViewInAnim();
//					getSchoolAttendance(calendar);
//					getEvenWeek(classId);
				}
			});
			//initTitleBar(this.getString(R.string.menu_course));
			
		}else{
			
			mStudent = (StudentBean) this.getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
			
			
			    calendarView = (CalendarView) findViewById(R.id.calendar);
		        calendarView.init();
			    DateBean d = calendarView.getDateInit();

		        title.setText(d.getSolar()[0] + "年" + d.getSolar()[1] + "月" + d.getSolar()[2] + "日");
		        calendar = Calendar.getInstance();
		        calendar.set(d.getSolar()[0], d.getSolar()[1]-1, d.getSolar()[2]);
		        setDateText(calendar);
			if( mStudent == null ){
			
				title_view.setText("课表");
				RelativeLayout.LayoutParams layoutParams=
					    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
				title_view.setLayoutParams(layoutParams);
			//	initTitleBar(this.getString(R.string.menu_course));
			}else{
				classId=(int) mStudent.groupId;
//				getEvenWeek(classId);
//				getSchoolAttendance(calendar);
				title_view.setText("课表");
				
				RelativeLayout.LayoutParams layoutParams=
					    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
				title_view.setLayoutParams(layoutParams);
			//	initTitleBar(mStudent.empName+"的课表");
			}
		}
		
//          getSchoolAttendance(calendar);
            calendarView.setOnPagerChangeListener(new OnPagerChangeListener() {
            @Override
            public void onPagerChanged(int[] date) {
                title.setText(date[0] + "年" + date[1] + "月" + date[2] + "日");
            }
        });
   
        calendarView.setOnItemClickListener(new OnMonthItemClickListener() {
            @Override
            public void onMonthItemClick(View view, DateBean date) {
            	timetables=new ArrayList<TimeTable>();
            	if( mShowCourseDetail == true ){
            		return;
            	}
            	if(isShowPop==true){
            		return;
            	}
            	if (!mPopViewShow) {
        			listViewInAnim();
        		}
            	
                title.setText(date.getSolar()[0] + "年" + date.getSolar()[1] + "月" + date.getSolar()[2] + "日");
                calendar.set(date.getSolar()[0], date.getSolar()[1]-1, date.getSolar()[2]);
                setDateText(calendar);
                getEvenWeek(classId);
               
//				ArrayList<String> amcourses = mCourseTableHelper.getAmCourseStrs(timetables);
//				ArrayList<String> pmcourses = mCourseTableHelper.getPmCourseStrs(timetables);
//				showCourseDetail(amcourses,pmcourses);
				
				
//                getSchoolAttendance(calendar);
               
//                showCourseDetail();
            }
        });
        ImageView back_menu = (ImageView) findViewById(R.id.back_btn);
        back_menu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				CourseCalendarActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		});
        //教师课表  启动老师fragment
        if(appType!=null&&appType.equals("TeacherCourseTable")){
        	calRoot.setVisibility(View.GONE);
        	fragmentManager = getSupportFragmentManager();
        	showTeacherCourseFragment();
        }
	}
	private void showTeacherCourseFragment() {
		// TODO Auto-generated method stub
		transaction = fragmentManager.beginTransaction();
		teacherCourseFragment = (TeacherCourseFragment) fragmentManager.findFragmentByTag("TeacherCourseFragment");
		if(teacherCourseFragment==null){
			teacherCourseFragment=new TeacherCourseFragment();
			transaction.add(R.id.framlayout_teachercourse, teacherCourseFragment, "TeacherCourseFragment");
			transaction.commit();
		}else{
			transaction.show(teacherCourseFragment);
			transaction.commit();
		}
	}
	private String setDateText(Calendar time){
		dateStr = mShowSdf.format(time.getTime());
		return dateStr;
	}
	private void getEvenWeek(int groupId) {	
//		showProgressDialog();
		showCicleProgressDialog();
		String urlString = SmartCampusUrlUtils.getWeekeven(groupId,dateStr);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "response=" + response);
							hideCicleProgressDialog();
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
									getSchoolAttendance(calendar);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}else if ( code == -2) {
								InfoReleaseApplication.returnToLogin(CourseCalendarActivity.this);
							} else {
								String msg = response.optString("msg");
							    Toast.makeText(CourseCalendarActivity.this, msg, Toast.LENGTH_LONG).show();
								
							}

						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
							hideCicleProgressDialog();
						    Toast.makeText(CourseCalendarActivity.this, "获取数据失败",Toast.LENGTH_SHORT).show();
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}
	
	
	private void getTeacherId(int userId, int type) {
		// TODO Auto-generated method stub
		String urlString=SmartCampusUrlUtils.getCourseNameUrl(String.valueOf(userId),String.valueOf(type));
		showProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						Log.d(TAG, "response=" + response);
						hideProgressDialog();
						try {
							if(response.getInt("code")==0){
								JSONObject  obj=response.getJSONObject("datas");
								teacherId=obj.optInt("sid");
								Log.d(TAG,teacherId+"老师id");
							}else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(CourseCalendarActivity.this);
							
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(CourseCalendarActivity.this, "查询老师信息失败!", Toast.LENGTH_SHORT).show();
									
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
			
		},  new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.e(TAG, "sorry,Error");
				if( InfoReleaseApplication.showNetWorkFailed(CourseCalendarActivity.this) == true ){
					Toast.makeText(CourseCalendarActivity.this, "查询老师信息失败!", Toast.LENGTH_SHORT).show();
				}
				hideProgressDialog();
				
			}
		});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void getClassListData() {
		showMyProgressDialog();
		GroupListData.getClassListData(CourseCalendarActivity.this, new DataResponse.Listener<ArrayList<GroupInfo>>() {

			

			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
				classGroupIdList = new ArrayList<Integer>();
				mClassList = new ArrayList<String>();
				mClassList.add("本周课程");
				for (int i = 0; i < classGroups.size(); i++) {
					mClassList.add(classGroups.get(i).getName());
					classGroupIdList.add(classGroups.get(i).getId());
				}
				Log.d("XXX",mClassList.size()+"");
				if (classGroups.size() > 0) {
					// 获取当前班级
					//getCurrentClass(classGroupIdList);
					if (mClassList.size()>0) {
						classTextView.setText(mClassList.get(0));
						classId=classGroupIdList.get(0);
						Log.d(TAG,"班级Id:"+classId);
//						getEvenWeek(classId);
//						getSchoolAttendance(calendar);
					}
					
					classListView.setAdapter(new ClassSelListViewAdapter(CourseCalendarActivity.this, mClassList));
					hideMyProgressDialog();
				} else {
					hideMyProgressDialog();
				}

			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
			}
		});
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.dropdown_mask_coursetable:
			if (!mPopViewShow) {
				listViewInAnim();
			}
		}
	}
	private void dropdownClick() {
		if (mPopViewShow) {
			if (mClassList != null) {
				classListView.setAdapter(new ClassSelListViewAdapter(CourseCalendarActivity.this, mClassList));
				listViewOutAnim();
			}
		} else {
			listViewInAnim();
		}
	}


	private void listViewOutAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.VISIBLE);
		classListView.startAnimation(AnimationUtils.loadAnimation(CourseCalendarActivity.this, R.anim.dd_menu_in));
		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(CourseCalendarActivity.this, R.anim.dd_mask_in));
		mClassDropdown.setVisibility(View.VISIBLE);
		mPopViewShow = false;
	}
	private void listViewInAnim() {
		
		classListView.clearAnimation();
		classListView.setVisibility(View.GONE);
		classListView.startAnimation(AnimationUtils.loadAnimation(CourseCalendarActivity.this, R.anim.dd_menu_out));
		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(CourseCalendarActivity.this, R.anim.dd_mask_out));
		mClassDropdown.startAnimation(AnimationUtils.loadAnimation(CourseCalendarActivity.this, R.anim.dd_mask_out));
		mClassDropdown.setVisibility(View.GONE);
		
	

//		mDropdownMask.setVisibility(View.VISIBLE);
//		mDropdownMask.startAnimation(AnimationUtils.loadAnimation(CourseCalendarActivity.this, R.anim.dd_mask_in));
		mPopViewShow = true;

	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		hideMyProgressDialog();
	}
	private void showProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(CourseCalendarActivity.this, "", "...loading...");
		}
	}

	private void hideProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	public Dialog mWaitDialog = null;
	
	public void hideCicleProgressDialog(){
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	public void showCicleProgressDialog(){
		if( mWaitDialog == null ){
			mWaitDialog = new Dialog(CourseCalendarActivity.this,R.style.new_circle_progress);    
			mWaitDialog.setContentView(R.layout.dialog_wait);    
			mWaitDialog.setCancelable(true);
			mWaitDialog.show();
		}
	}
	private void showMyProgressDialog() {
		if (progressDialog1 == null || !progressDialog1.isShowing()) {
			progressDialog1 = ProgressDialog.show(CourseCalendarActivity.this, "", "...loading...");
		}
	}
	private void hideMyProgressDialog() {
		if (progressDialog1 != null && progressDialog1.isShowing()) {
			progressDialog1.dismiss();
			progressDialog1 = null;
		}
	}
	void getSchoolAttendance(final Calendar calendar){
		if( mStudent == null ){
//			return;
		}
		mCourseTableHelper.getSchoolAttendance(classId, new CourseTableHelper.Listener<String>(){

			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				 mCourseTableHelper.getCourseTableAboutXmls(classId,calendar,new CourseTableHelper.Listener<String>(){

						@Override
						public void onResponse(String response) {
							// TODO Auto-generated method stub
							Log.d(TAG,"获取数据成功");
							isExist=true;
							timetables = mCourseTableHelper.getCourseData(calendar,isExist,isEvenWeek);
							
//							timetables = mCourseTableHelper.getCourseData(calendar);
//							
							ArrayList<String> amcourses = mCourseTableHelper.getAmCourseStrs(timetables);
							ArrayList<String> pmcourses = mCourseTableHelper.getPmCourseStrs(timetables);
							showCourseDetail(amcourses,pmcourses);
						}
	                	
	                },new CourseTableHelper.ErrorListener(){

						@Override
						public void onResponse(String errorMsg) {
							// TODO Auto-generated method stub
							showToast( errorMsg);
							isExist=false;
						}
	                	
	                });
			}
			
		});
	}
	

	public void showCourseDetail(ArrayList<String> amdatas,ArrayList<String> pmdatas){
		 // TODO: 2016/5/17 构建一个popupwindow的布局
		
        View popupView = this.getLayoutInflater().inflate(R.layout.popupwindow_course_detail, null);

        // TODO: 2016/5/17 为了演示效果，简单的设置了一些数据，实际中大家自己设置数据即可，相信大家都会。
        ListView amListView = (ListView) popupView.findViewById(R.id.am_courceDetail);
        
//        ArrayList<String> datas = new ArrayList<String>();
//        datas.add("08:10-09:00 语文 朱自清老师");
//        datas.add("09:10-10:00 数学 陈景润老师");
//        datas.add("10:10-11:00 数学 薄冰老师");
//        datas.add("11:10-12:00 数学 薄冰老师");
        
        if( amdatas != null ){
        	positionListAm=new ArrayList<Integer>();
        	for(int i=0;i<amdatas.size();i++){
        		if(teacherId!=-1){
        			if(String.valueOf(teacherId).equals(timetables.get(i).teacherId)){
                		positionListAm.add(i);
                	}
        		}
            	
            }
        	amListView.setAdapter(new CourseDetialAdapter(this, amdatas,positionListAm,appType));
        }
        
        ListView pmListView = (ListView) popupView.findViewById(R.id.pm_courceDetail);
        
//        ArrayList<String> pmdatas = new ArrayList<String>();
//        pmdatas.add("08:10-09:00 语文 朱自清老师");
//        pmdatas.add("09:10-10:00 数学 陈景润老师");
//        pmdatas.add("10:10-11:00 数学 薄冰老师");
//        pmdatas.add("11:10-12:00 数学 薄冰老师");
        if( pmdatas != null ){
        	positionListPm=new ArrayList<Integer>();
        	for(int i=amdatas.size();i<timetables.size();i++){
        		if(teacherId!=-1){
        			
        		
            	if(String.valueOf(teacherId).equals(timetables.get(i).teacherId)){
            		positionListPm.add(i-amdatas.size());
            	}
        	   }
            }
        	pmListView.setAdapter(new CourseDetialAdapter(CourseCalendarActivity.this,pmdatas,positionListPm,appType));
        }
        
        if( amdatas == null || pmdatas == null ){
        	if(!isExist){
        		
        		showToast("无课程表!");
				return;
			}else {
				mShowCourseDetail = true;
				popupView.findViewById(R.id.day_off_tv).setVisibility(View.VISIBLE);
	        	popupView.findViewById(R.id.courseArea).setVisibility(View.GONE);
			}
        	
        }

        // TODO: 2016/5/17 创建PopupWindow对象，指定宽度和高度
        PopupWindow window = new PopupWindow(popupView, (int)(this.getResources().getDimension(R.dimen.course_detail_window_width)),
        		(int)(this.getResources().getDimension(R.dimen.course_detail_window_height)),true);
        window.setAnimationStyle(R.style.PopupAnimation);
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.update();
        window.setBackgroundDrawable(new BitmapDrawable());
        window.showAtLocation(calendarView, Gravity.CENTER, 0, 0);
        isShowPop=true;
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
              WindowManager.LayoutParams lp = getWindow().getAttributes();
              lp.alpha = 1f;
              getWindow().setAttributes(lp);
              mShowCourseDetail = false;
              isShowPop=false;
            }
          });
        WindowManager.LayoutParams lp =getWindow().getAttributes();  
        lp.alpha = 0.4f;  
        getWindow().setAttributes(lp);
	}
	
	public void lastMonth(View view) {
        calendarView.lastMonth();
    }

    public void nextMonth(View view) {
        calendarView.nextMonth();
    }
//    private Toast mToast;  
    public void showToast(String text) {  
        Toast.makeText(CourseCalendarActivity.this, text, Toast.LENGTH_SHORT).show();         
    }    
        
    public void cancelToast() {    
            
        }    
        
    public void onBackPressed() {    
            cancelToast();    
            super.onBackPressed();    
        } 
	
}
*/