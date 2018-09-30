package com.routon.smartcampus.leave;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.bean.FamilyLeaveBean;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

public class FamilyLeaveActivity extends BaseFragmentActivity{
	private ImageView backImg;
	private ImageView addImg;
	private ListView dataList;
	
	private StudentBean mStudentBean;
	private ArrayList<FamilyLeaveBean> mFamilyLeaveBeans;
	private FamilyLeaveAdapter adapter;
	private String headTeacher;
	
	private final static String TAG = "FamilyLeaveActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initData();
	}
	
	public void initView(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_family_leave);
		setMoveBackEnable(true);
		backImg = (ImageView)findViewById(R.id.img_family_leave_back);
		backImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		addImg = (ImageView)findViewById(R.id.img_family_leave_add);
		addImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FamilyLeaveActivity.this, FamilyLeaveAddActivity.class);
				intent.putExtra(MyBundleName.STUDENT_BEAN, mStudentBean);
				if(!TextUtils.isEmpty(headTeacher)){
					intent.putExtra("headTeacher", headTeacher);
				}
				startActivity(intent);
			}
		});
		dataList = (ListView)findViewById(R.id.lv_family_leave_data);
	}
	
	public void initData(){
		mStudentBean = (StudentBean) getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
		mFamilyLeaveBeans = new ArrayList<>();
		getFamilyLeaveData();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		getFamilyLeaveData();
	}

	public void getFamilyLeaveData(){
		String url = SmartCampusUrlUtils.getFamilyLeaveData(mStudentBean.sid);
		Log.d(TAG, "url="+url);
		JsonObjectRequest request = new JsonObjectRequest(url, null, 
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response="+response);
						int code = response.optInt("code");
						String msg = response.optString("msg");
						if(code == 0){
							JSONObject datas = response.optJSONObject("datas");
							headTeacher = datas.optString("headTeacher");
							JSONArray leave = datas.optJSONArray("leave");
							Gson gson = new Gson();
							Type type =new TypeToken<ArrayList<FamilyLeaveBean>>(){}.getType();
							mFamilyLeaveBeans = gson.fromJson(leave.toString(), type);
							Collections.sort(mFamilyLeaveBeans, new Comparator<FamilyLeaveBean>() {

								@Override
								public int compare(FamilyLeaveBean o1, FamilyLeaveBean o2) {
									int result = o1.getStartTime().compareTo(o2.getStartTime());
									if (result != 0)
										return -result;
										
                                    result = o1.getEndTime().compareTo(o2.getEndTime());
									if (result != 0)
										return -result;

                                    result = o1.getReason().compareTo(o2.getReason());
									return -result;
								}
								
							});
							adapter = new FamilyLeaveAdapter(FamilyLeaveActivity.this, mFamilyLeaveBeans);
							dataList.setAdapter(adapter);
						}else {
							Toast.makeText(FamilyLeaveActivity.this, msg, Toast.LENGTH_SHORT).show();
						}
					}
				}, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						
					}
				});
		InfoReleaseApplication.requestQueue.add(request);
	}
}
