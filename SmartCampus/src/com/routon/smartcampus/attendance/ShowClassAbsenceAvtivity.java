package com.routon.smartcampus.attendance;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

public class ShowClassAbsenceAvtivity extends BaseFragmentActivity{
	
	private TextView dateTv;
	private TextView classTv;
	private TextView absenceNumTv;
	private ImageView backImg;
	private GridView mStudentGridView;
	private Dialog mWaitDialog = null;
	
	private String date;
	private String classInfo;
	private int absenceNum;
	private int groupId;
	private int lesson;
	
	private static final String TAG = "ShowClassAbsenceAvtivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_show_class_absence);
		initData();
		initView();
	}
	
	public void initData(){
		Intent intent = getIntent();
		date = intent.getStringExtra("day");
		classInfo = intent.getStringExtra("class");
		absenceNum = intent.getIntExtra("absence", 0);
		lesson = intent.getIntExtra("lesson", 0);
		groupId = intent.getIntExtra("groupId", 0);
	}
	
	public void initView(){
		dateTv = (TextView)findViewById(R.id.tv_absence_calendar_tag);
		classTv = (TextView)findViewById(R.id.tv_absence_class_tag);
		absenceNumTv = (TextView)findViewById(R.id.tv_absence_absence_tag);
		dateTv.setText(date);
		classTv.setText(classInfo);
		absenceNumTv.setText(absenceNum+"人");
		backImg = (ImageView)findViewById(R.id.img_absence_back);
		backImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mStudentGridView = (GridView)findViewById(R.id.gv_absence_student);
		getStudentAttendanceListData(groupId, lesson, date);
		setMoveBackEnable(true);
	}
	
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
		AttendanceRankingAdapter adapter = new AttendanceRankingAdapter(ShowClassAbsenceAvtivity.this, 
				datalist);
		mStudentGridView.setAdapter(adapter);
		mStudentGridView.setNumColumns(3);
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
										Toast.makeText(ShowClassAbsenceAvtivity.this, "获取学生数据失败", Toast.LENGTH_SHORT).show();
										return;
									}
									for (int i = 0; i < mAllStudentDataList.length; i++) {
										if (mAllStudentDataList[i].groupId.equals(String.valueOf(groupId))) {
											classStudentData=mAllStudentDataList[i];
										}
									}
									initStudentGridView(bean.studentlist,classStudentData);
								}else{
									Toast.makeText(ShowClassAbsenceAvtivity.this, "无缺勤学生", Toast.LENGTH_SHORT).show();
									AttendanceRankingAdapter adapter = new AttendanceRankingAdapter(ShowClassAbsenceAvtivity.this, 
											new ArrayList<AttendanceBean>());
									mStudentGridView.setAdapter(adapter);
								}
							}else if ( code == -2) {
								InfoReleaseApplication.returnToLogin(ShowClassAbsenceAvtivity.this);
							} else {
								String msg = response.optString("msg");
								if( msg == null || msg.isEmpty() == true ){
									Toast.makeText(ShowClassAbsenceAvtivity.this, "获取缺勤数据失败", Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(ShowClassAbsenceAvtivity.this, msg, Toast.LENGTH_LONG).show();
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
								Toast.makeText(ShowClassAbsenceAvtivity.this, "获取考勤数据失败", Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(ShowClassAbsenceAvtivity.this, msg, Toast.LENGTH_LONG).show();
							}
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}
	
	public void hideProgressDialog(){
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	public void showProgressDialog(){
		if( mWaitDialog == null ){
			mWaitDialog = new Dialog(this,R.style.new_circle_progress);    
			mWaitDialog.setContentView(R.layout.dialog_wait);    
			mWaitDialog.setCancelable(true);
			mWaitDialog.show();
		}
	}

}
