package com.routon.smartcampus.selectcourse;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.schoolcompare.ClassCompareActivity;
import com.routon.smartcampus.schoolcompare.CompareClassTypeBean;
import com.routon.smartcampus.schoolcompare.SchoolCompareActivity;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.widgets.Toast;

public class SelectCourseActivity extends BaseActivity{
	
	private static String TAG = "SelectCourseActivity";
	private ListView selectList;
	private StudentBean mStudent;
	private List<SelectCourseBean> beanList;
	private ProgressDialog progressDialog;
	private Context mContext;
	
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mStudent = (StudentBean) this.getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
		super.onCreate(savedInstanceState);
		//mContext = getApplicationContext();
		setContentView(R.layout.activity_selectcourse);
		initView();
		getSelectCoursesData();
	}
	
private void initView() {
		
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SelectCourseActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		ImageView backBut=(ImageView) findViewById(R.id.back_btn);
		backBut.setOnClickListener(mBackListener);
		
		selectList = (ListView) findViewById(R.id.select_list);
		
		selectList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.v(TAG, "selectList onItemClick");
				Intent intent=new Intent(SelectCourseActivity.this, SelectingActivity.class);
				intent.putExtra("classTypeBean", beanList.get(position));
				startActivity(intent);
			}
		});
}
private void getSelectCoursesData() {
	Log.d(TAG, "mStudent.sid=" + mStudent.sid);
	String urlString = SmartCampusUrlUtils.getSelectCourseUrl("45487");//(""+mStudent.sid);

	showLoadDialog();
	Log.d(TAG, "urlString=" + urlString);
	CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
			new Response.Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject response) {
					Log.d(TAG, "response=" + response);
					hideLoadDialog();
					try {
					
						if (response.getInt("ret") == 0) {
							beanList=new ArrayList<SelectCourseBean>();
							JSONArray jsonArray = response.getJSONArray("items");
							if (jsonArray!=null) {
								for (int i = 0; i < jsonArray.length(); i++) {
									SelectCourseBean bean=new SelectCourseBean(jsonArray.getJSONObject(i));
									beanList.add(bean);
								}
							}
							
							SelectCourseAdapter adapter = new SelectCourseAdapter(SelectCourseActivity.this,beanList);
						    selectList.setAdapter(adapter);
						}
					}catch (JSONException e) {
						e.printStackTrace();
					}
				}
				}
	, new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError arg0) {
			Log.e(TAG, "sorry,Error");
			Toast.makeText(SelectCourseActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
			hideLoadDialog();
		}
	});
	jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
	InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
}
private void showLoadDialog(){
	if( progressDialog == null || !progressDialog.isShowing()){
		progressDialog = ProgressDialog.show(SelectCourseActivity.this, "", "...loading...");
	}
}

private void hideLoadDialog(){
	if (progressDialog != null && progressDialog.isShowing()) {
		progressDialog.dismiss();
		progressDialog = null;
		
	}
}

}
