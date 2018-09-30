package com.routon.smartcampus.schoolcompare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import com.routon.smartcampus.network.SmartCampusUrlUtils;

import android.R.integer;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.routon.widgets.Toast;

public class HistoryQueryActivity extends BaseActivity{
	private ListView compareScoreListview;
	private ProgressDialog progressDialog;
	private static String TAG = "HistoryQueryActivity";
	private int cycleBeanId;
	private String mRatingId;
	private List<CycleClassRatingBean> beans;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_query_layout);
		
		cycleBeanId = getIntent().getIntExtra("cycleBeanId",0);
		mRatingId = getIntent().getStringExtra("ratingId");
		initView();
		initData();
	}

	private void initView() {
    	mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				HistoryQueryActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};

		ImageView backBut = (ImageView) findViewById(R.id.back_btn);
		TextView titleView = (TextView) findViewById(R.id.title_view);
		
		compareScoreListview = (ListView) findViewById(R.id.compare_score_listview);
		backBut.setOnClickListener(mBackListener);
		if (cycleBeanId==0) {
			titleView.setText("学期总评");
		}else {
			titleView.setText("评比成绩");
			compareScoreListview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					if(classRatingBeans.size() > position){
						getClassPeroidItemScore(classRatingBeans.get(position).groupId, position);
					}
				}
			});
		}
	}
	
	private void getClassPeroidItemScore(final int groupId, final int position){
		String urlString = SmartCampusUrlUtils.getRatingClassPeroidScoreUrl(mRatingId, ""+cycleBeanId, ""+groupId);
		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				Log.d(TAG, "response=" + response);
				hideLoadDialog();
				try {
					if (response.getInt("code") == 0) {
						List<ClassPeroidItemScoreBean> scorebeans = new ArrayList<ClassPeroidItemScoreBean>();
						JSONArray jsonArray = response.getJSONArray("datas");
						for (int i = 0; i < jsonArray.length(); i++) {
							ClassPeroidItemScoreBean bean=new ClassPeroidItemScoreBean(jsonArray.getJSONObject(i));
							scorebeans.add(bean);
						}
						
						ArrayList<SubprojectBean> subprojectList=new ArrayList<SubprojectBean>();
						ArrayList<ClassCompareBean> classCompareBeans = new ArrayList<>();
						ClassCompareBean classCompareBean = new ClassCompareBean();
						for (int j = 0; j < ClassCompareActivity.classTypeBean.subprojectBeanList.size(); j++) {
							SubprojectBean sBean= ClassCompareActivity.classTypeBean.subprojectBeanList.get(j);
							SubprojectBean subprojectBean=new SubprojectBean();
							subprojectBean.id=sBean.id;
							subprojectBean.score=-9999;
							subprojectBean.maxScore=sBean.maxScore;
							subprojectBean.name=sBean.name;
							subprojectBean.isPermit = true;
							for(int i=0; i<scorebeans.size(); i++){
								ClassPeroidItemScoreBean cisBean = scorebeans.get(i);
								if(cisBean.ratingItemId == subprojectBean.id){
									subprojectBean.score = cisBean.itemTotalScore;
									subprojectBean.itemAvg = cisBean.avgTotalScore;
								}
							}
							subprojectList.add(subprojectBean);
						}					
						classCompareBean.subprojectBeanList = subprojectList;
						classCompareBean.groupName = beans.get(position).groupName;
						classCompareBean.groupId = groupId;
						classCompareBeans.add(classCompareBean);
						CompareClassTypeBean compareClassTypeBean = new CompareClassTypeBean();
						compareClassTypeBean.classCompareBeanList = classCompareBeans;
						Intent intent=new Intent(HistoryQueryActivity.this, ClassMarkActivity.class);
						intent.putExtra("isCompareFinish", true);
						intent.putExtra("ratingMode", 1);
						intent.putExtra("selectId", groupId);
						intent.putExtra("compareClassTypeBean", compareClassTypeBean);
						HistoryQueryActivity.this.startActivityForResult(intent, 1);
						
					} else if (response.getInt("code") == -2) {
						InfoReleaseApplication.returnToLogin(HistoryQueryActivity.this);
					} else {
						Log.e(TAG, response.getString("msg"));
						Toast.makeText(HistoryQueryActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
					}

				} catch (JSONException e) {
					e.printStackTrace();
					try {
						Toast.makeText(HistoryQueryActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
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
				Toast.makeText(HistoryQueryActivity.this, arg0.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				hideLoadDialog();
			}
		});
		
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
    
	private void initData() {
		
		getHistoryData(mRatingId,cycleBeanId);
	}
	
	private void getHistoryData(String ratingId,int peroidId) {

		 String urlString = SmartCampusUrlUtils.getRatingCycleClassQueryUrl(ratingId,peroidId);

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
								beans = new ArrayList<CycleClassRatingBean>();
								JSONArray jsonArray = response.getJSONArray("datas");
								for (int i = 0; i < jsonArray.length(); i++) {
									CycleClassRatingBean bean=new CycleClassRatingBean(jsonArray.getJSONObject(i));
									beans.add(bean);
								}
								
								CycleClassRatingAdapter cycleClassRatingAdapter=new CycleClassRatingAdapter(HistoryQueryActivity.this,listTaxis(beans));								
								if(cycleBeanId == 0){
									cycleClassRatingAdapter.showIndicatior = false;
								}else{
									cycleClassRatingAdapter.showIndicatior = true;
								}
								compareScoreListview.setAdapter(cycleClassRatingAdapter);
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(HistoryQueryActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(HistoryQueryActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							try {
								Toast.makeText(HistoryQueryActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();
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
	
	private List<CycleClassRatingBean> classRatingBeans;
	private List<CycleClassRatingBean> listTaxis(List<CycleClassRatingBean> beans){
		Collections.sort(beans, new Comparator<CycleClassRatingBean>() {
			@Override
			public int compare(CycleClassRatingBean lhs, CycleClassRatingBean rhs) {
				if (lhs.rank < rhs.rank) {
					return -1;
				}
				if (lhs.rank == rhs.rank) {
					return 0;
				}
				return 1;
			}
		});
		
		classRatingBeans = beans;
		return beans;
	}
	
	private void showLoadDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(HistoryQueryActivity.this, "", "...loading...");
		}
	}

	private void hideLoadDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;

		}
	}
}
