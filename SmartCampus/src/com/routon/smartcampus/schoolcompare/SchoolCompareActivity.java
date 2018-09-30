package com.routon.smartcampus.schoolcompare;

import java.util.ArrayList;
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
import com.routon.inforelease.json.AuthenobjBean;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.network.SmartCampusUrlUtils;

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

public class SchoolCompareActivity extends BaseActivity{
	
	private static String TAG = "SchoolCompareActivity";
	private ListView compareList;
	private ProgressDialog progressDialog;
	private TextView titleView;
	private List<CompareClassTypeBean> beanList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_school_compare_layout);
		
		
		initView();
		initData();
	}

	private void initView() {
		
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SchoolCompareActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		
		ImageView backBut=(ImageView) findViewById(R.id.back_btn);
		titleView = (TextView) findViewById(R.id.title_view);
		titleView.setText("校务评比");
		compareList = (ListView) findViewById(R.id.compare_list);
		
		backBut.setOnClickListener(mBackListener);
		
		compareList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent=new Intent(SchoolCompareActivity.this, ClassCompareActivity.class);
				intent.putExtra("classTypeBean", beanList.get(position));
				startActivity(intent);
			}
		});
		
	}
	
	private void initData() {
		
		String[] schools = InfoReleaseApplication.authenobjData.schools;
		if (schools!=null && schools.length>0) {
			titleView.setText(schools[0]+"校务评比");
		}
		
		int is=InfoReleaseApplication.authenobjData.userId;
		Log.e("=========", is+"");
//		getTestData();
		getCompareClassType();
	}



	private void getCompareClassType() {
		
		String urlString = SmartCampusUrlUtils.getSchoolRatingListUrl("");

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
								beanList=new ArrayList<CompareClassTypeBean>();
								JSONArray jsonArray = response.getJSONArray("datas");
								if (jsonArray!=null) {
									for (int i = 0; i < jsonArray.length(); i++) {
										CompareClassTypeBean bean=new CompareClassTypeBean(jsonArray.getJSONObject(i));
										beanList.add(bean);
									}
								}
								
								compareList.setAdapter(new SchoolCompareAdapter(SchoolCompareActivity.this,beanList));
								if(beanList.size() == 1){
									Intent intent=new Intent(SchoolCompareActivity.this, ClassCompareActivity.class);
									intent.putExtra("classTypeBean", beanList.get(0));
									startActivity(intent);
								}
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(SchoolCompareActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(SchoolCompareActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(SchoolCompareActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}

	
	private void showLoadDialog(){
		if( progressDialog == null || !progressDialog.isShowing()){
			progressDialog = ProgressDialog.show(SchoolCompareActivity.this, "", "...loading...");
		}
	}
	
	private void hideLoadDialog(){
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
			
		}
	}
	
	/*private void getTestData() {
		AuthenobjBean beans = InfoReleaseApplication.authenobjData;
		JSONObject json = null;
		try {
			json=new JSONObject("{code:0,msg:成功,datas:[{id:1,name:文明班级,groups:[{groupId:101,groupName:一（1）班},{groupId:102,groupName:一（2）班},{groupId:103,groupName:一（3）班},"
					                                                                                                           + "{groupId:201,groupName:二（1）班},{groupId:202,groupName:二（2）班},{groupId:203,groupName:二（3）班},"
					                                                                                                           + "{groupId:301,groupName:三（1）班},{groupId:302,groupName:三（2）班},{groupId:303,groupName:三（3）班}],"
					                                                                                                                    + "redflagImgFileId:701,redflagImgUrl:701usl,"
					                                                                                                                    + "items:[{id:1,name:佩戴红领巾,maxScore:10,weight:1,userIds:[303]},"
					                                                                                                                              + "{id:2,name:晨读响亮,maxScore:10,weight:1,userIds:[303]},"
					                                                                                                                              + "{id:3,name:爱护眼睛,maxScore:20,weight:1,userIds:[23707]},"
					                                                                                                                              + "{id:4,name:强身健体,maxScore:10,weight:1,userIds:[303]},"
					                                                                                                                              + "{id:5,name:午休安静,maxScore:10,weight:1,userIds:[23707]},"
					                                                                                                                              + "{id:6,name:课堂规范,maxScore:10,weight:1,userIds:[23707]},"
					                                                                                                                              + "{id:7,name:测试数据,maxScore:20,weight:1,userIds:[303]},"
					                                                                                                                              + "{id:8,name:认真听讲,maxScore:10,weight:1,userIds:[23707]}]},"
					                                                                                       + "{id:2,name:阳光班级,groups:[{groupId:101,groupName:一（1）班},{groupId:102,groupName:一（2）班},{groupId:103,groupName:一（3）班},"
					                                                                                                                                 + "{groupId:401,groupName:四（1）班},{groupId:402,groupName:四（2）班},{groupId:403,groupName:四（3）班},"
					                                                                                                                                 + "{groupId:501,groupName:五（1）班},{groupId:502,groupName:五（2）班},{groupId:503,groupName:五（3）班}],"
					                                                                                                                    + "redflagImgFileId:701,redflagImgUrl:701usl,"
					                                                                                                                    + "items:[{id:9,name:ＸＸＸＸＸ,maxScore:10,weight:1,userIds:[23707]},"
					                                                                                                                              + "{id:10,name:ＸＸＸＸＸ,maxScore:10,weight:1,userIds:[23708]},"
					                                                                                                                              + "{id:11,name:ＸＸＸＸＸ,maxScore:15,weight:1,userIds:[23708]},"
					                                                                                                                              + "{id:12,name:ＸＸＸＸＸ,maxScore:10,weight:1,userIds:[23707]},"
					                                                                                                                              + "{id:13,name:ＸＸＸＸＸ,maxScore:10,weight:1,userIds:[23707]},"
					                                                                                                                              + "{id:14,name:ＸＸＸＸＸ,maxScore:15,weight:1,userIds:[23708]},"
					                                                                                                                              + "{id:15,name:ＸＸＸＸＸ,maxScore:10,weight:1,userIds:[23708]},"
					                                                                                                                              + "{id:16,name:ＸＸＸＸＸ,maxScore:10,weight:1,userIds:[23707]}]}]}");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
		if (json.getInt("code") == 0) {
			beanList=new ArrayList<CompareClassTypeBean>();
			JSONArray jsonArray = json.getJSONArray("datas");
			if (jsonArray!=null) {
				for (int i = 0; i < jsonArray.length(); i++) {
					CompareClassTypeBean bean=new CompareClassTypeBean(jsonArray.getJSONObject(i));
					beanList.add(bean);
				}
			}
			
			compareList.setAdapter(new SchoolCompareAdapter(SchoolCompareActivity.this,beanList));
			
		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}*/
}
