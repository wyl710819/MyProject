package com.routon.smartcampus.student;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.bean.HistoryAwardBean;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

public class ExchangeHistoryActivity extends CustomTitleActivity{
	private ListView listExchangeHistory;
	private TextView tvExchangeDetail;
	private TextView tvExchangeName;
	private ProgressDialog progressDialog;
	
	private StudentBean studentBean;
	private List<HistoryAwardBean> historyAwardBeans;
	private HistoryAwardAdapter adapter;
	
	private final String TAG = "ExchangeHistoryActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exchange_history);
		initData();
		initView();
	}
	
	public void initData(){
		studentBean = (StudentBean) getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
		historyAwardBeans = new ArrayList<>();
		getExchangeHistory();
	}
	
	public void initView(){
		initTitleBar("奖品兑换记录");
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		setTitleBackBtnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		tvExchangeName = (TextView)findViewById(R.id.tv_exchange_history_name);
		tvExchangeDetail = (TextView)findViewById(R.id.tv_exchange_history_detail);
		tvExchangeName.setText(studentBean.empName);
		listExchangeHistory = (ListView)findViewById(R.id.list_exchange_history);
	}
	
	public void getExchangeHistory(){
		progressDialog = ProgressDialog.show(ExchangeHistoryActivity.this, "", "...loading...");
		String urlString = SmartCampusUrlUtils.getExchangeHistoryUrl(String.valueOf(studentBean.sid));
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
							JSONArray jsonArray = response.optJSONArray("datas");
							Gson gson = new Gson();
							for(int i=0;i<jsonArray.length();i++){
								historyAwardBeans.add(gson.fromJson(jsonArray.optJSONObject(i).toString(), HistoryAwardBean.class));
							}
							adapter = new HistoryAwardAdapter(ExchangeHistoryActivity.this, historyAwardBeans);
							listExchangeHistory.setAdapter(adapter);
							setExchangeDetail();
						} else if (code == -2) {
							InfoReleaseApplication.returnToLogin(ExchangeHistoryActivity.this);
						} else {
							Log.e(TAG, response.optString("msg"));
							Toast.makeText(ExchangeHistoryActivity.this,response.optString("msg"),Toast.LENGTH_LONG).show();
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

	public void setExchangeDetail(){
		int sumGrade = 0;
		for(int i=0;i<historyAwardBeans.size();i++){
			HistoryAwardBean historyAwardBean = historyAwardBeans.get(i);
			sumGrade += historyAwardBean.getUsebonuspoint();
		}
		tvExchangeDetail.setText("已兑换奖品"+historyAwardBeans.size()+"件 共使用积分"+sumGrade);
	}
}
