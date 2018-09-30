package com.routon.smartcampus.schoolcompare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
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
import com.routon.smartcampus.homework.CorrectHomeworkActivity;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.schoolcompare.ClassCompareActivity.MapComparator;
import com.routon.smartcampus.student.ClassSelListViewAdapter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class CompareScoreQueryActivity extends BaseActivity implements OnClickListener{
	
	private ListView gradeCompareListview;
	private CompareClassTypeBean classTypeBean;
	private ListView classListView;
	private View dropdownMask;
	private LinearLayout mCorrectLL;
	private TextView mCorrectRemark;
	private FrameLayout mCorrectDropdown;
	private ProgressDialog progressDialog;
	private static String TAG = "CompareScoreQueryActivity";
	private RatingCycleAdapter mAdapter;
	private List<RatingCycleBean> cycleBeans;
	private boolean isRatingCycle=true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compare_score_query_layout);
		
        classTypeBean= (CompareClassTypeBean) getIntent().getSerializableExtra("classTypeBean");
        mRatingId =classTypeBean.id;
		initView();
		initData();
	}
	
    private void initView() {
    	mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				CompareScoreQueryActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};

		ImageView backBut = (ImageView) findViewById(R.id.back_btn);
		TextView titleView = (TextView) findViewById(R.id.title_view);
		
		classListView = (ListView) findViewById(R.id.dropdown_listview);
		dropdownMask = findViewById(R.id.dropdown_view);
		mCorrectDropdown = (FrameLayout) findViewById(R.id.dropdown_layout);
		mCorrectLL = (LinearLayout) findViewById(R.id.tv_layout);
		mCorrectRemark = (TextView) findViewById(R.id.tv_class);
		
		dropdownMask.setOnClickListener(this);
		mCorrectLL.setOnClickListener(this);
		
		gradeCompareListview = (ListView) findViewById(R.id.grade_compare_listview);
		backBut.setOnClickListener(mBackListener);
		titleView.setText("成绩查询");
		
		classListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position==0) {
					gradeCompareListview.setAdapter(mAdapter);
					isRatingCycle=true;
					if (!mPopViewShow) {
						listViewInAnim();
					}
					mCorrectRemark.setText(classList.get(position));
				}else {
					getHistoryData(mRatingId,classIdList.get(position),position);
				}
				
			}
		});
		
		gradeCompareListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (isRatingCycle) {
					Intent intent=new Intent(CompareScoreQueryActivity.this, HistoryQueryActivity.class);
					intent.putExtra("cycleBeanId", cycleBeans.get(position).id);
					intent.putExtra("ratingId", mRatingId);
					startActivity(intent);
				}
				
			}
		});
		
	}
    
	private void initData() {

		classList = new ArrayList<String>();
		classIdList = new ArrayList<String>();
		classList.add("所有");
		classIdList.add("0");
		
		List<ClassCompareBean> beans=new ArrayList<ClassCompareBean>();
		beans.addAll(classTypeBean.classCompareBeanList);
		Collections.sort(beans, new MapComparator());
		for (int i = 0; i < beans.size(); i++) {
			classIdList.add(String.valueOf(beans.get(i).groupId));
			classList.add(beans.get(i).groupName);
		}
		
		
		
		getRatingCycleData(mRatingId);
		
	}
	
	class MapComparator implements Comparator<ClassCompareBean>{  
		  
	    public int compare(ClassCompareBean lhs, ClassCompareBean rhs) {  
	        return lhs.groupName.compareTo(rhs.groupName);  
	    }  
	  
	} 
	
	private void getRatingCycleData(String ratingId) {

		 String urlString = SmartCampusUrlUtils.getSchoolRatingCycleQueryUrl(ratingId);

		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {


					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								cycleBeans = new ArrayList<RatingCycleBean>();
								JSONArray jsonArray = response.getJSONArray("datas");
								
								for (int i = 0; i < jsonArray.length(); i++) {
									RatingCycleBean bean=new RatingCycleBean(jsonArray.getJSONObject(i));
									cycleBeans.add(bean);
								}
								
								mAdapter = new RatingCycleAdapter(CompareScoreQueryActivity.this,cycleBeans);
								gradeCompareListview.setAdapter(mAdapter);
								isRatingCycle=true;
								
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(CompareScoreQueryActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(CompareScoreQueryActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							try {
								Toast.makeText(CompareScoreQueryActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	private void getHistoryData(String ratingId, String groupId, final int position) {

		 String urlString = SmartCampusUrlUtils.getSchoolRatingHistoryUrl(ratingId,groupId);

		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								List<HistoryScoreBean> beans = new ArrayList<HistoryScoreBean>();
								
								JSONArray jsonArray = response.getJSONArray("datas");
								for (int i = 0; i < jsonArray.length(); i++) {
									HistoryScoreBean bean=new HistoryScoreBean(jsonArray.getJSONObject(i));
									beans.add(bean);
								}
								
								HistoryScoreAdapter historyScoreAdapter = new HistoryScoreAdapter(CompareScoreQueryActivity.this,beans);
									gradeCompareListview.setAdapter(historyScoreAdapter);
									isRatingCycle=false;
								
								if (!mPopViewShow) {
									listViewInAnim();
								}
								
								mCorrectRemark.setText(classList.get(position));
								
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(CompareScoreQueryActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(CompareScoreQueryActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							try {
								Toast.makeText(CompareScoreQueryActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
								gradeCompareListview.setAdapter(new HistoryScoreAdapter(CompareScoreQueryActivity.this,null));
								isRatingCycle=false;
								if (!mPopViewShow) {
									listViewInAnim();
								}
								mCorrectRemark.setText(classList.get(position));
								
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.tv_layout:
			dropdownClick();
			break;
		case R.id.dropdown_view:
			if (!mPopViewShow) {
				listViewInAnim();
			}
			break;

		default:
			break;
		}
		
	}
	
	private void dropdownClick() {
		if (mPopViewShow) {
			if (classList != null) {
				classListView.setAdapter(new ClassSelListViewAdapter(CompareScoreQueryActivity.this, classList));
				listViewOutAnim();
			}
		} else {
			listViewInAnim();
		}
	}
	private boolean mPopViewShow = true;
	private String  mRatingId;
	private List<String> classList;
	private List<String> classIdList;
	private void listViewOutAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.VISIBLE);
		classListView.startAnimation(AnimationUtils.loadAnimation(CompareScoreQueryActivity.this, R.anim.dd_menu_in));
		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(CompareScoreQueryActivity.this, R.anim.dd_mask_in));
		mCorrectDropdown.setVisibility(View.VISIBLE);
		mPopViewShow = false;
	}
	private void listViewInAnim() {
		
		classListView.clearAnimation();
		classListView.setVisibility(View.GONE);
		classListView.startAnimation(AnimationUtils.loadAnimation(CompareScoreQueryActivity.this, R.anim.dd_menu_out));
		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(CompareScoreQueryActivity.this, R.anim.dd_mask_out));
		mCorrectDropdown.startAnimation(AnimationUtils.loadAnimation(CompareScoreQueryActivity.this, R.anim.dd_mask_out));
		mCorrectDropdown.setVisibility(View.GONE);
	
		mPopViewShow = true;

	}
	
	private void showLoadDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(CompareScoreQueryActivity.this, "", "...loading...");
		}
	}

	private void hideLoadDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;

		}
	}


}
