package com.routon.smartcampus.student;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.AwardBean;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.AwardListAdapter.Callback;
import com.routon.smartcampus.utils.MyBundleName;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class ExchangePrizeActivity extends CustomTitleActivity{

	private String TAG = "ExchangePrizeActivity";
	private ArrayList<AwardBean> awardListData = new ArrayList<AwardBean>();

	private ProgressDialog progressDialog;
	private ListView awardListView;
	private TextView studentNameView;
	private TextView prizeNum;
	private StudentBean studentBean;
	//可用积分
	private int mBonusPoints;
	private String studentName;
	private int studentId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exchange_layout);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {

			studentBean = (StudentBean) bundle.getSerializable(MyBundleName.STUDENT_BEAN);
			
			studentId = bundle.getInt(MyBundleName.STUDENT_ID);
			studentName = bundle.getString(MyBundleName.STUDENT_NAME);
			mBonusPoints = bundle.getInt(MyBundleName.STUDENT_BONUS_POINTS);
			
			
		}

		initView();
		initData();
	}
	private boolean idExchange=false;
	private void initView() {
		initTitleBar("奖品兑换");
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		setTitleBackBtnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra(MyBundleName.STUDENT_BADGE_IS_RETRACT, idExchange);
				setResult(MyBundleName.STUDENT_BADGE_DETAIL_RESULT, intent);
				finish();
			}
		});
		awardListView = (ListView) findViewById(R.id.award_list);
		studentNameView = (TextView) findViewById(R.id.prize_student_name);
		prizeNum = (TextView) findViewById(R.id.student_prize_num);
		setTitleNextImageBtnClickListener(R.drawable.ic_exchange_history, new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(ExchangePrizeActivity.this, ExchangeHistoryActivity.class);
				intent.putExtra(MyBundleName.STUDENT_BEAN, studentBean);
				startActivity(intent);
			}
		});
	}

	private void initData() {

		studentNameView.setText(studentName);
		prizeNum.setText(mBonusPoints + "分");


		 getAwardListData();
		
	}

	private void getAwardListData() {
		String urlString = SmartCampusUrlUtils.getAwardListUrl();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						try {
							if (response.getInt("code") == 0) {

								awardListData = new ArrayList<AwardBean>();
								JSONArray array = response.getJSONArray("datas");
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									AwardBean bean = new AwardBean(obj);
									awardListData.add(bean);
									
								}

								awardListView.setAdapter(new AwardListAdapter(ExchangePrizeActivity.this, awardListData,mBonusPoints,new myCallback()));
								
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(ExchangePrizeActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(ExchangePrizeActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {

						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	class myCallback implements Callback{

		@Override
		public void click(View v) {
			AwardBean awardBean = awardListData.get((Integer) v.getTag());		
			 exchangeAward(studentId,awardBean);
		}
		
	}
	private void exchangeAward(int id, final AwardBean awardBean) {
		progressDialog = ProgressDialog.show(ExchangePrizeActivity.this, "", "...loading...");
		// "http://edu.wanlogin.com:8086/edu/easyad/client/badge/exchange.htm?";
		String urlString = SmartCampusUrlUtils.getExchangeAwardURl() +"?studentId="+id+"&awardId="+awardBean.id;
		Log.d(TAG,"exchangeAward urlString:"+urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						try {
							
							if (response.getInt("code") == 0) {								
								Toast.makeText(ExchangePrizeActivity.this,"成功兑换奖品："+awardBean.name , Toast.LENGTH_LONG).show();
								mBonusPoints = mBonusPoints-awardBean.bonuspoint;
								prizeNum.setText(String.valueOf(mBonusPoints)+"分");
								awardListView.setAdapter(new AwardListAdapter(ExchangePrizeActivity.this, awardListData,mBonusPoints,new myCallback()));
								
								Intent intent = new Intent(StudentListFragment.ACTION_STUDENT_UPDATE);
//								intent.putExtra(MyBundleName.STUDENT_BONUS_POINTS, mBonusPoints);
								ExchangePrizeActivity.this.sendBroadcast(intent);
								idExchange = true;
							} else if (response.getInt("code") == -2 ) {
								InfoReleaseApplication.returnToLogin(ExchangePrizeActivity.this);
							} else {
								Toast.makeText(ExchangePrizeActivity.this, response.getString("msg"), Toast.LENGTH_LONG).show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}


}
