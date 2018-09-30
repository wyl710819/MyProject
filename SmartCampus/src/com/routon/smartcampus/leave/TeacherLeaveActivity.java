package com.routon.smartcampus.leave;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.TimeUtils;
import com.routon.inforelease.widget.DateTimePickerHelper;
import com.routon.smartcampus.leave.CouserReplaceAdapter.OnBtnListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class TeacherLeaveActivity extends CustomTitleActivity {
	
	private int leaveType;
	private ListView listview;
	private TextView teacherLeaveStartTime;
	private TextView teacherLeaveEndTime;
	private EditText teacherLeaveCause;
	private TextView teacherLeaveAmountTime;
	private SimpleDateFormat mShowSdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm);
	private Calendar mStartTime;
	private Calendar mEndTime;
	private static final String TAG = "TeacherLeaveActivity";
	private ProgressDialog progressDialog;

	private List<ReplaceTeacherBean> replaceTeacherBeans=new ArrayList<ReplaceTeacherBean>();
	private List<CourseReplaceBean> courseReplaceBeans=new ArrayList<CourseReplaceBean>();
	private int teacherId;
	private CouserReplaceAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teacher_leave_layout);
		
		leaveType = getIntent().getIntExtra("leave_type",0);
		teacherId = InfoReleaseApplication.authenobjData.userId;
		initView();
		initData();
	}

	

	private void initView() {
		if (leaveType==0) {
			initTitleBar("我要请假");
		}else {
			initTitleBar("我要出差");
		}
		
		setTitleBackground(this.getResources().getDrawable(R.drawable.leave_title_bg));
		this.setTitleNextBtnClickListener("确定", new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				leave();
			}
		});

		setTitleBackBtnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		
		TextView leaveTeacherTitle=(TextView) findViewById(R.id.leave_teacher_title);
		teacherLeaveStartTime = (TextView) findViewById(R.id.teacher_leave_start_time);
		teacherLeaveEndTime = (TextView) findViewById(R.id.teacher_leave_end_time);
		teacherLeaveCause = (EditText) findViewById(R.id.teacher_leave_cause);
		teacherLeaveAmountTime = (TextView) findViewById(R.id.teacher_leave_amount_time);
		TextView causeText = (TextView) findViewById(R.id.teacher_leave_cause_t);
		
		listview = (ListView) findViewById(R.id.teacher_leave_listview);
		
		if (leaveType==0) {
			leaveTeacherTitle.setText("请假时间及理由");
			causeText.setText("请假理由：");
		}else {
			leaveTeacherTitle.setText("出差时间及理由");
			causeText.setText("出差理由：");
		}
		
		setTouchUnDealView(teacherLeaveCause);
		initStartAndEndTime((String)teacherLeaveStartTime.getText(),(String)teacherLeaveEndTime.getText());
		
	}
	




	private void initData() {
		
	        getUserTeacherData(teacherLeaveStartTime.getText()+":00",teacherLeaveEndTime.getText()+":00");
//	        long amountTime=LeaveTimeUtil.getInstance().getTime(teacherLeaveStartTime.getText().toString(), teacherLeaveEndTime.getText().toString());
//	        teacherLeaveAmountTime.setText("共计："+getDiff(amountTime));
	}
	private String getDiff(long diff) {
		long nh = 1000 * 60 * 60;
	    long nm = 1000 * 60;
		
				// 计算差多少小时
			    long hour = diff / nh;
			    // 计算差多少分钟
			    long min = diff % nh / nm;
				return hour+"时"+min+"分";
			    
	}
	
	/**
	 * 获取用户老师某个时段内所有需要上的课
	 * */
	private void getUserTeacherData(String startTime,String endTime) {
		showMyProgressDialog();
		
		
		String urlString = "";
	    urlString = SmartCampusUrlUtils.getQueryUserTeacherClassUrl(String.valueOf(teacherId),null,null);
		
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("startTime", startTime));
	    params.add(new BasicNameValuePair("endTime", endTime));
	    
	    Log.d(TAG,"CommitUrl="+urlString);
	    
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					

					

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								courseReplaceBeans.clear();
								
								JSONArray array = response.optJSONArray("datas");
								if (array!=null) {
									int len = array.length();
									for (int i = 0; i < len; i++) {
										JSONObject obj = (JSONObject) array.get(i);
										CourseReplaceBean bean=new CourseReplaceBean(obj);
										courseReplaceBeans.add(bean);
									}
								}
								
								teacherLeaveAmountTime.setText("共计："+courseReplaceBeans.size()+"节课");
								
								if (mAdapter==null) {
									mAdapter = new CouserReplaceAdapter(TeacherLeaveActivity.this, courseReplaceBeans);
									listview.setAdapter(mAdapter);
								}else {
									mAdapter.notifyDataSetChanged();
								}
								
								
								mAdapter.setOnBtnListener(new OnBtnListener() {
									
									@Override
									public void onBtnClick(int position) {
										getReplaceTeacherData(courseReplaceBeans.get(position));
										
									}

									
								});
								
								
								
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(TeacherLeaveActivity.this);
								Toast.makeText(TeacherLeaveActivity.this, "登录已失效!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(TeacherLeaveActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Toast.makeText(TeacherLeaveActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();
						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	    
	}

	private void getReplaceTeacherData(final CourseReplaceBean courseReplaceBean) {
    showMyProgressDialog();
		
		String urlString = "";
	    urlString = SmartCampusUrlUtils.getQueryTeacherStatusUrl(String.valueOf(courseReplaceBean.lesson),courseReplaceBean.courseTime.substring(0, courseReplaceBean.courseTime.indexOf(" ")),String.valueOf(courseReplaceBean.schoolId));
	   
	    Log.d(TAG,"urlString="+urlString);
	    
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								replaceTeacherBeans.clear();
								
								JSONArray array = response.optJSONArray("datas");
								if (array!=null) {
									int len = array.length();
									for (int i = 0; i < len; i++) {
										JSONObject obj = (JSONObject) array.get(i);
										ReplaceTeacherBean bean=new ReplaceTeacherBean(obj);
										
										if (bean.userid!=teacherId) {
											replaceTeacherBeans.add(bean);
										}
										
									}
								}
								
								
								initDialog(courseReplaceBean);
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(TeacherLeaveActivity.this);
								Toast.makeText(TeacherLeaveActivity.this, "登录已失效!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(TeacherLeaveActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Toast.makeText(TeacherLeaveActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();
						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	

	private void leave() {
		
		
		if (teacherLeaveStartTime.getText()==null || teacherLeaveStartTime.getText().toString().trim().equals("")) {
			Toast.makeText(TeacherLeaveActivity.this, "请选择请假起始时间", Toast.LENGTH_SHORT).show();
			return;
		}
		if (teacherLeaveEndTime.getText()==null || teacherLeaveEndTime.getText().toString().trim().equals("")) {
			Toast.makeText(TeacherLeaveActivity.this, "请选择请假截止时间", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try {
			if (isWeekend(mStartTime.getTime()) ) {
				reportToast("请假起始时间为周六日，请重新输入");
				return;
			}
			
			if (isWeekend(mEndTime.getTime())) {
				reportToast("请假截止时间为周六日，请重新输入");
				return;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (teacherLeaveCause.getText()==null || teacherLeaveCause.getText().toString().trim().equals("")) {
			Toast.makeText(TeacherLeaveActivity.this, "请填写请假理由", Toast.LENGTH_SHORT).show();
			return;
		}
		/*for (int i = 0; i < courseReplaceBeans.size(); i++) {
			if (courseReplaceBeans.get(i).replaceTeacherName==null ||courseReplaceBeans.get(i).replaceTeacherName.equals("")) {
				Toast.makeText(TeacherLeaveActivity.this, "含有未指定代课老师的课程，请先指定代课老师", Toast.LENGTH_LONG).show();
				return;
			}
		}*/
		
		String startTime=teacherLeaveStartTime.getText()+":00";
		String endTime=teacherLeaveEndTime.getText()+":00";
		String leaveReason=teacherLeaveCause.getText().toString();
		showMyProgressDialog();
			
			String urlString = "";
		    urlString = SmartCampusUrlUtils.getCommitLeaveUrl(String.valueOf(teacherId), null, null, String.valueOf(leaveType), null);
		    
		   
		    
		    for (int i = 0; i < courseReplaceBeans.size(); i++) {
		    	if (courseReplaceBeans.get(i).replaceTeacherName!=null ||!courseReplaceBeans.get(i).replaceTeacherName.equals("")) {
		    		urlString+="&subTeacherId="+String.valueOf(courseReplaceBeans.get(i).subTeacherId);
			    	urlString+="&lessonDate="+String.valueOf(courseReplaceBeans.get(i).courseTime.substring(0, courseReplaceBeans.get(i).courseTime.indexOf(" ")));
			    	urlString+="&lesson="+String.valueOf(courseReplaceBeans.get(i).lesson);
				}
		    	
			}
		    
		    Log.d(TAG,"urlString="+urlString);
		    
		    List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("startTime", startTime));
		    params.add(new BasicNameValuePair("endTime", endTime));
		    params.add(new BasicNameValuePair("reason", leaveReason));
		    
			CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
					new Response.Listener<JSONObject>() {
						

						

						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "response=" + response);
							hideMyProgressDialog();
							try {
								if (response.getInt("code") == 0) {
									Toast.makeText(TeacherLeaveActivity.this, leaveType==0 ? "请假提交成功" : "出差提交成功", Toast.LENGTH_SHORT).show();
									setResult(RESULT_OK);
									finish();
									
								} else if (response.getInt("code") == -2) {
									InfoReleaseApplication.returnToLogin(TeacherLeaveActivity.this);
									Toast.makeText(TeacherLeaveActivity.this, "登录已失效!", Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(TeacherLeaveActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
											.show();
								}

							} catch (JSONException e) {
								e.printStackTrace();
								hideMyProgressDialog();
							}

						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Toast.makeText(TeacherLeaveActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
							hideMyProgressDialog();
							
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
		
		
	}
	
	private Dialog dialog;
	private void initDialog(final CourseReplaceBean bean) {
		AlertDialog.Builder builder = new Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.leave_sel_teacher_dialog, null);
        TextView dialogTitleTime=(TextView) v.findViewById(R.id.leave_dialog_title_time);
        TextView dialogTitleClass=(TextView) v.findViewById(R.id.leave_dialog_title_class);
        ListView leaveDialogLv=(ListView) v.findViewById(R.id.leave_dialog_lv);
        
        dialogTitleTime.setText(bean.courseTime);
        dialogTitleClass.setText(bean.className+" "+bean.courseName+" 代课老师");
       
        leaveDialogLv.setAdapter(new ReplaceTeacherAdapter(TeacherLeaveActivity.this,replaceTeacherBeans));
        
        leaveDialogLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				if (replaceTeacherBeans.get(position).teacherType==1) {
//					Toast.makeText(TeacherLeaveActivity.this, "请选择空闲的老师代课", Toast.LENGTH_SHORT).show();
//				}else {
					int tag=courseReplaceBeans.lastIndexOf(bean);
					bean.replaceTeacherName=replaceTeacherBeans.get(position).teacherName;
					bean.subTeacherId=replaceTeacherBeans.get(position).userid;
					courseReplaceBeans.set(tag, bean);
					mAdapter.notifyDataSetChanged();
					dialog.dismiss();
//				}
			}
		});
        
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);
        
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = dp2px(300);
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setGravity(Gravity.CENTER);//显示位置
		
	}


	
	private void initStartAndEndTime(String startTime, String endTime) {
		teacherLeaveStartTime.setText(startTime);
		mStartTime = TimeUtils.getFormatCalendar(startTime,TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		mEndTime = TimeUtils.getFormatCalendar(endTime,TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		
		if( mStartTime == null ){
			mStartTime = Calendar.getInstance();
			mEndTime = Calendar.getInstance();
			mEndTime.add(Calendar.DATE, 1);
		}

		if (mStartTime != null) {
			teacherLeaveStartTime.setText(mShowSdf.format(mStartTime.getTime()));
		}
		teacherLeaveStartTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDatePicker(true);
			}
		});
		
		teacherLeaveEndTime.setText(endTime);
		if (mEndTime != null) {
			teacherLeaveEndTime.setText(mShowSdf.format(mEndTime.getTime()));
		}
		teacherLeaveEndTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDatePicker(false);
			}
		});
	}
	
	private void showDatePicker(final boolean startTime) {
		//不能设置小于当前时间的时间
		 Calendar curTime = Calendar.getInstance();
		 Calendar initTime = null;
		 long maxTime = 0;
		 if( startTime == true ){
			 initTime = mStartTime;
		 }else{
			 initTime = mEndTime;
		 }		
		 Calendar showTime = curTime;
		 if( initTime != null ){
			 if( TimeUtils.isTimeBeforeTilMinute(curTime, initTime)  ){
				 showTime = initTime;
			 }
		 }
           
         // Build DateTimeDialog  
		 DateTimePickerHelper.showDateTimePicker(this,showTime,curTime.getTimeInMillis(),0, new DateTimePickerHelper.OnClickListener() {  
             @Override  
             public void onClick(Calendar time) {  

 				 if( startTime == true ){//开始时间设置
// 					 if( checkTime(time,mEndTime) == false ){
// 						 return;
// 					 }
 					 mStartTime = time;
 					teacherLeaveStartTime.setText(mShowSdf.format(mStartTime.getTime()));
// 					//设置起始时间时，默认结束时间为起始时间后一天
// 					 if( mEndTime == null ){
	 					 mEndTime = Calendar.getInstance();
	 					 mEndTime.setTime(mStartTime.getTime());
	 						mEndTime.add(Calendar.DATE,1);
	 						
	 					//修改结束时间
	 						teacherLeaveEndTime.setText(mShowSdf.format(mEndTime.getTime()));
	 						getUserTeacherData(teacherLeaveStartTime.getText()+":00",teacherLeaveEndTime.getText()+":00");
	 						
//	 						 long amountTime=LeaveTimeUtil.getInstance().getTime(teacherLeaveStartTime.getText().toString(), teacherLeaveEndTime.getText().toString());
//	 				        teacherLeaveAmountTime.setText("共计："+getDiff(amountTime));
// 					 }
 				 }else{//结束时间设置
 					//设置结束时间时，如果没有设置起始时间，默认起始时间为当前时间
 					 if( mStartTime == null ){
	 					//修改起始时间
	 					mStartTime = Calendar.getInstance();
	 					teacherLeaveStartTime.setText(mShowSdf.format(mStartTime.getTime()));
 					 }
 					 if( checkTime(mStartTime,time) == false ){
 						 return;
 					 }
 					 //修改结束时间
 					 mEndTime = time;
 					
 					 
 					teacherLeaveEndTime.setText(mShowSdf.format(mEndTime.getTime()));
 					getUserTeacherData(teacherLeaveStartTime.getText()+":00",teacherLeaveEndTime.getText()+":00");
 					
// 					 long amountTime=LeaveTimeUtil.getInstance().getTime(teacherLeaveStartTime.getText().toString(), teacherLeaveEndTime.getText().toString());
// 			        teacherLeaveAmountTime.setText("共计："+getDiff(amountTime));
 				 }
             }  
         });    
	}
	
	private boolean checkTime(Calendar startTime,Calendar endTime){
		if (startTime == null || endTime == null) {
			return false;
		}
		if (TimeUtils.isTimeBeforeTilMinute(endTime, startTime)) {
			reportToast("结束时间不能早于开始时间");
			return false;
		}
		
		return true;
	}
	
	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(TeacherLeaveActivity.this, "",
					"...loading...");
		}
	}

	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	public int dp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}

	public static boolean isWeekend(Date bDate) throws ParseException {
//        DateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
//        Date bdate = format1.parse(bDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(bDate);
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            return true;
        } else{
            return false;
        }
 
 }
}
