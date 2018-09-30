package com.routon.smartcampus.flower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.StudentNameListAdpter;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;
import com.tencent.android.tpush.common.t;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class StudentHornorRollForParentActivity extends CustomTitleActivity{
	
	private final String TAG = "StudentHornorRollForParentActivity";
	private int studentId;
	private long sgroupId;
	private String studentName;
	
	private Context mContext;
	private ListView mListView;

	private ArrayList<StudentBean> mStudentBadgeCountBeanList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_honorroll_for_parent);
	
		mContext = this;
		Bundle bundle = getIntent().getExtras();
		studentId = bundle.getInt(MyBundleName.STUDENT_ID);
		sgroupId = bundle.getLong(MyBundleName.STUDENT_GROUP_ID, -1);
		
		if (SmartCampusApplication.mFamilyVersion == false) {
			initTitleBar("小红花统计");	
		}else {
			initTitleBar("小红花");	
		}
		
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		
		mListView = (ListView) findViewById(R.id.student_listview);
		
		getStudentListData(sgroupId);
	}
	
	private ProgressDialog progressDialog;
	private void getStudentListData(final long sgroupId2) {//
	    progressDialog = ProgressDialog.show(mContext, "", "...loading...");
		String urlString = SmartCampusUrlUtils.getCmdStudentBadgeCountListURl()+"?groupId=" + sgroupId2;
		Log.i(TAG, urlString);		
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						int code = response.optInt("code");
						if (code == 0) {							
							mStudentBadgeCountBeanList = new ArrayList<StudentBean>();
							JSONArray array = response.optJSONArray("datas");
							if( array == null ){
								Toast.makeText(mContext, R.string.get_student_data_failed, Toast.LENGTH_LONG).show();
								return;
							}
							for (int i = 0; i < array.length(); i++) {
								JSONObject obj = (JSONObject) array.opt(i);
								StudentBean bean = new StudentBean(obj);
								bean.subclassPoint=bean.bonuspoints;
								
								mStudentBadgeCountBeanList.add(bean);
							}
							
							ArrayList<StudentBean> taxisList=new ArrayList<StudentBean>();
							if (mStudentBadgeCountBeanList!=null) {
								taxisList=getTaxisList(mStudentBadgeCountBeanList);
								StudentNameListAdpter adpter = new StudentNameListAdpter(mContext, mStudentBadgeCountBeanList);
								if (SmartCampusApplication.mFamilyVersion == false) {
									adpter.isShowName=true;
								}
								
								adpter.showStudentImage = false;
								adpter.showStudentFullName = false;
								adpter.focusStudentId = studentId;
								mListView.setAdapter(adpter);
							}
						} else if (code == -2) {
							InfoReleaseApplication.returnToLogin(StudentHornorRollForParentActivity.this);
						} else {
							Toast.makeText(mContext, response.optString("msg"), Toast.LENGTH_LONG).show();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						//先判断网络状况
						if( true == InfoReleaseApplication.showNetWorkFailed(mContext) ){
							Toast.makeText(mContext, R.string.get_student_data_failed, Toast.LENGTH_LONG).show();
						}						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);	
	}

	private ArrayList<StudentBean> getTaxisList(ArrayList<StudentBean> badgeCountBeanList) {
		Collections.sort(badgeCountBeanList, new Comparator<StudentBean>() {
			@Override
			public int compare(StudentBean lhs, StudentBean rhs) {
				if (lhs.subclassPoint > rhs.subclassPoint) {
					return -1;
				}
				if (lhs.subclassPoint == rhs.subclassPoint) {
					return 0;
				}
				return 1;
			}
		});
		int taxisTag=0;
		for (int i = 0; i < badgeCountBeanList.size(); i++) {
			if (i==0) {
				badgeCountBeanList.get(i).ranking=i+1;
			}else if (badgeCountBeanList.get(i).subclassPoint==badgeCountBeanList.get(i-1).subclassPoint) {
				taxisTag+=1;
				badgeCountBeanList.get(i).ranking=badgeCountBeanList.get(i-1).ranking;
			} else {
				badgeCountBeanList.get(i).ranking=badgeCountBeanList.get(i-1).ranking+1+taxisTag;
				taxisTag=0;
			}
		}
		
		
		return badgeCountBeanList;
	}
}
