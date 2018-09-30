package com.routon.smartcampus.schoolcompare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.homework.FamilyHomeworkAdapter;
import com.routon.smartcampus.homework.WeekCalendarListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.schoolcompare.ClassMarkAdapter.onChangeListener;
import com.routon.smartcampus.schoolcompare.ClassMarkAdapter.onItemChangeListener;
import com.routon.smartcampus.user.ParentRegisterActivity;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.homework.FamilyHomeworkAdapter.onCheckListener;
import com.routon.smartcampus.view.WeekCalendarView;
import com.routon.widgets.Toast;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ClassMarkActivity extends BaseActivity implements OnClickListener {
	private ClassCompareBean classCompareBean;
	private SubprojectBean subprojectBean;
	private CompareClassTypeBean compareClassTypeBean;
	private ListView markingView;
	private static String TAG = "ClassMarkingActivity";
	private ProgressDialog progressDialog;
	private boolean isCompareFinish;
	private List<SubprojectBean> subprojectBeans = null;
	private Vibrator mVibrator;
	private int ratingMode;
	private int selectId;
	private boolean scoreIsChange = false;
	private boolean isDataChange = false;
	private boolean nextButShow=false;
	private List<ClassCompareBean> classCompareBeanList;
	private ClassSubGradeAdapter adapter;
	private ImageView backBut;
	private TextView titleView;
	private TextView nextBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_marking_layout);
		isCompareFinish = getIntent().getBooleanExtra("isCompareFinish", false);
		ratingMode = getIntent().getIntExtra("ratingMode", 1);
		selectId = getIntent().getIntExtra("selectId", 0);
		compareClassTypeBean = (CompareClassTypeBean) getIntent().getSerializableExtra("compareClassTypeBean");
		if(compareClassTypeBean != null){
			classCompareBeanList = compareClassTypeBean.classCompareBeanList;
		}
		mVibrator=(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
		initView();
		initClassCompareBeanList();
		initData();
	}

	private void initView() {
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("score_change", isDataChange);
				ClassMarkActivity.this.setResult(Activity.RESULT_OK, intent);
				ClassMarkActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		backBut = (ImageView) findViewById(R.id.back_btn);
		titleView = (TextView) findViewById(R.id.title_view);
		nextBtn = (TextView) findViewById(R.id.delete_view);
		markingView = (ListView) findViewById(R.id.class_marking_listview);
		backBut.setOnClickListener(mBackListener);
		if (!isCompareFinish) {
			nextBtn.setVisibility(View.VISIBLE);
			nextBtn.setOnClickListener(this);			
		}
	}

	private void initData() {
		if(ratingMode == 1)
		{
			ArrayList<SubprojectBean> beans=new ArrayList<SubprojectBean>();
			ArrayList<SubprojectBean> unBeans=new ArrayList<SubprojectBean>();
			for(int i=0;i<classCompareBeanList.size();i++){
				if(classCompareBeanList.get(i).groupId == selectId){
					classCompareBean = classCompareBeanList.get(i);
				}
			}
			titleView.setText(classCompareBean.groupName);
			if(subprojectBeans == null){
				subprojectBeans = new ArrayList<SubprojectBean>();
				for (int i = 0; i < classCompareBean.subprojectBeanList.size(); i++) {
					
					if (/*useList(classCompareBean.subprojectBeanList.get(i).userIds,userId)*/
							classCompareBean.subprojectBeanList.get(i).isPermit) {
						beans.add(classCompareBean.subprojectBeanList.get(i));
					}else {
						unBeans.add(classCompareBean.subprojectBeanList.get(i));
					}
				}
				Collections.sort(beans, new Comparator<SubprojectBean>() {
					@Override
					public int compare(SubprojectBean lhs, SubprojectBean rhs) {
						if (lhs.score < rhs.score) {
							return -1;
						}
						if (lhs.score == rhs.score) {
							return 0;
						}
						return 1;
					}
				});			
				beans.addAll(unBeans);
				for (int i = 0; i < beans.size(); i++) {
					subprojectBeans.add(new SubprojectBean(beans.get(i).id,beans.get(i).score));
				}
			}else{
				beans.addAll(subprojectBeans);
			}
			classCompareBean.subprojectBeanList = beans;
			adapter = new ClassSubGradeAdapter(this, ratingMode, selectId, classCompareBeanList, isCompareFinish);
			markingView.setAdapter(adapter);
		}else {
			classCompareBean = classCompareBeanList.get(0);
			for(int i=0;i<classCompareBean.subprojectBeanList.size();i++){
				if(classCompareBeanList.get(0).subprojectBeanList.get(i).id == selectId){
					subprojectBean = classCompareBeanList.get(0).subprojectBeanList.get(i);
					titleView.setText(subprojectBean.name);
				}
			}
			adapter = new ClassSubGradeAdapter(this, ratingMode, selectId, classCompareBeanList, isCompareFinish);
			markingView.setAdapter(adapter);
		}
	}

	//如果平台没有步长、初始分、最低分，自己设置
	public void initClassCompareBeanList(){
		for(int i=0;i<classCompareBeanList.size();i++){
			ClassCompareBean classCompareBean = classCompareBeanList.get(i);
			List<SubprojectBean> subprojectBeans = classCompareBean.subprojectBeanList;
			for(int j=0;j<subprojectBeans.size();j++){
				SubprojectBean subprojectBean = subprojectBeans.get(j);
				if(Double.isNaN(subprojectBean.score)){
					subprojectBean.score = subprojectBean.maxScore-10;
				}
				if(Double.isNaN(subprojectBean.operateStep)){
					subprojectBean.operateStep = 1.0;
				}
				if(Double.isNaN(subprojectBean.minScore)){
					subprojectBean.minScore = subprojectBean.maxScore-10;
				}
			}
		}
	}
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.delete_view:
			submitRatingData();
			break;

		default:
			break;
		}

	}

	private void setChangeListener(ClassMarkAdapter adapter) {
		adapter.setOnChangeListener(new onChangeListener() {

			@Override
			public void onSelected(int position, double score) {
				setSubprojectScore(position, score);
				
			}
		});
		adapter.setOnItemChangeListener(new onItemChangeListener() {
			
			@Override
			public void onChange() {
//				mVibrator.vibrate(new long[]{1,8,1,8},-1);
			}
		});
	}

	private void setSubprojectScore(int position, double score) {
			subprojectBeans.get(position).score = score;
	}
	
	private void submitRatingData() {
		String urlString = SmartCampusUrlUtils.getSchoolRatingScoreUrl(classCompareBean.ratingDate);	
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if(ratingMode == 1){
			params.add(new BasicNameValuePair("groupId", String.valueOf(classCompareBean.groupId)));
			for (int i = 0; i < classCompareBean.subprojectBeanList.size(); i++) {
				SubprojectBean bean = classCompareBean.subprojectBeanList.get(i);
				if(bean.isPermit == false){
					continue;
				}
				params.add(new BasicNameValuePair("score", String.valueOf(bean.score)));
				params.add(new BasicNameValuePair("ratingItemId", String.valueOf(bean.id)));
				if(bean.score != bean.initScore){
					scoreIsChange = true;
				}
			}
		}else {
			params.add(new BasicNameValuePair("ratingItemId", String.valueOf(subprojectBean.id)));
			for(int i=0;i<classCompareBeanList.size();i++){
				ClassCompareBean selectClassCompareBean = classCompareBeanList.get(i);
				List<SubprojectBean> subprojectBeans = selectClassCompareBean.subprojectBeanList;
				for(int j=0;j<subprojectBeans.size();j++){
					SubprojectBean selectSubprojectBean = subprojectBeans.get(j);
					if(selectSubprojectBean.id == selectId){
						params.add(new BasicNameValuePair("groupId", String.valueOf(selectClassCompareBean.groupId)));
						params.add(new BasicNameValuePair("score", String.valueOf(selectSubprojectBean.score)));
					}
					if(selectSubprojectBean.score != selectSubprojectBean.initScore){
						scoreIsChange = true;
					}
				}
			}
		}
		if (!scoreIsChange) {
			Toast.makeText(ClassMarkActivity.this, "子项目分数未发生变化，保存失败", Toast.LENGTH_SHORT).show();
			return;
		}
		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString+"   params="+params);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								isDataChange=true;
								scoreIsChange = false;
								/*for (int i = 0; i < classCompareBean.subprojectBeanList.size(); i++) {
									for (int j = 0; j < subprojectBeans.size(); j++) {
										if (classCompareBean.subprojectBeanList.get(i).id == subprojectBeans.get(j).id
												&& classCompareBean.subprojectBeanList.get(i).score != subprojectBeans.get(j).score) {
											classCompareBean.subprojectBeanList.get(i).score= subprojectBeans.get(j).score;
										}
									}
								}*/
								Intent intent = new Intent();
								intent.putExtra("score_change", isDataChange);
								ClassMarkActivity.this.setResult(Activity.RESULT_OK, intent);
								ClassMarkActivity.this.finish();
								overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
								Toast.makeText(ClassMarkActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(ClassMarkActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(ClassMarkActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(ClassMarkActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	private void showLoadDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(ClassMarkActivity.this, "", "...loading...");
		}
	}

	private void hideLoadDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;

		}
	}
	
	private  boolean useList(int[] arr, int value) {
		for(int s: arr){
				 if(s==value)
				      return true;
				 }
		return false;
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("score_change", isDataChange);
		ClassMarkActivity.this.setResult(Activity.RESULT_OK, intent);
		ClassMarkActivity.this.finish();
//		super.onBackPressed();
	}

}
