package com.routon.smartcampus.flower;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.flower.OftenBadgeAdapter.OnAwardListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.routon.widgets.Toast;

/**
 * 小红花颁发界面
 */
public class BadgeSelectActivity extends CustomTitleActivity {

	private String TAG = "BadgeSelectActivity";
	private Context mContext;
	private ArrayList<StudentBean> allStudents;
	public static final int BADGE_MANAGE_REQUEST_CODE = 2;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flower_select_activity);
		mContext = this;
		initView();
		initData();
		
	}

	private void initView() {

		initTitleBar("小红花颁发");
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		this.setTitleNextBtnClickListener("编辑", new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BadgeSelectActivity.this, BadgeManageActivity.class);
				startActivityForResult(intent, BADGE_MANAGE_REQUEST_CODE);
			}
		});

		setTitleBackBtnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				setResult(7, intent);
				finish();
			}
		});
		

		//header view
		//student list and total tip custom flower
		//学生列表以及常用小红花提示
		badgeOftenView = (ListView) findViewById(R.id.badge_often_lv);
		View headerview = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.badge_select_headview, null, false);
		badgeOftenView.addHeaderView(headerview);
		mStudentGridView = (GridView) headerview.findViewById(R.id.students_list_grid_view);
		
		//footer view
		//badge grid view
		//一级分类小红花列表
		View footview = ((LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.badge_select_footview, null, false);
		badgeOftenView.addFooterView(footview);
		mBadgeGridView = (GridView) footview.findViewById(R.id.badge_view);
	}
	
	private void initData() {
		Bundle bundle = getIntent().getExtras();
		student = (StudentBean) bundle.getSerializable(MyBundleName.STUDENT_BEAN);
		allStudents = (ArrayList<StudentBean>) bundle.getSerializable(MyBundleName.MULTI_STUDENT_BEANS);
		
		if (student == null && allStudents != null) {
			student = allStudents.get(allStudents.size() - 1);
		}

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float density = dm.density;
		
		//常用小红花数据
		oftenBadgeAdapter = new OftenBadgeAdapter(mContext, BadgeInfoUtil.getCustomFlowers(),density, false);
		oftenBadgeAdapter.setOnAwardListener(new OnAwardListener() {
			
			@Override
			public void onAwardClick(int position) {//颁发按钮
				awardBadge(BadgeInfoUtil.getCustomFlowers().get(position));
			}
		});
		badgeOftenView.setAdapter(oftenBadgeAdapter);
		
		//设置横向滚动学生列表
		RemarkTitleStudentItemAdapter adapter = new RemarkTitleStudentItemAdapter(mContext, allStudents);
		int size = allStudents.size();
		int itemWidth = (int) (90 * density);
		int allWidth = (int) (95 * size * density);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(allWidth,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mStudentGridView.setLayoutParams(params);// 设置GirdView布局参数
		mStudentGridView.setColumnWidth(itemWidth);// 列表项宽
		mStudentGridView.setHorizontalSpacing((int) (5*density));// 列表项水平间距
		mStudentGridView.setNumColumns(size);// 总长度
		mStudentGridView.setAdapter(adapter);
		setTouchUnDealView(mStudentGridView);
		
		//一级分类小红花数据
		FlowerGridAdapter flowerGridAdapter = new FlowerGridAdapter(mContext, BadgeInfoUtil.getFlowerList());
		mBadgeGridView.setAdapter(flowerGridAdapter);
		mBadgeGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				// 小红花一级分类跳评语界面
				Intent intent = new Intent(BadgeSelectActivity.this, BadgeRemarkActivity.class);
				intent.putExtra(MyBundleName.BADGE_INFO, BadgeInfoUtil.getFlowerList().get(arg2));
				intent.putExtra(MyBundleName.STUDENT_BEAN, student);
				intent.putExtra(MyBundleName.MULTI_STUDENT_BEANS, allStudents);
				Bundle bundle = new Bundle();
				intent.putExtras(bundle);
				startActivityForResult(intent, 1);
			}
		});
		int badgeCount = BadgeInfoUtil.getFlowerList().size();
		int height = (int) (density*132*(badgeCount/3+((badgeCount%3==0)?0:1)));
		LinearLayout.LayoutParams footviewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				height);
		
		mBadgeGridView.setLayoutParams(footviewParams);// 设置GirdView布局参数
	}


	@Override
	protected void onResume() {
		super.onResume();
		oftenBadgeAdapter.notifyDataSetChanged();
	}
	
	//颁发常用小红花
	private void awardBadge(final BadgeInfo badgeInfo) {
		List<NameValuePair> params=new ArrayList<NameValuePair>();
		String urlString;
		
			urlString = SmartCampusUrlUtils.getBadugeIssueURl();
			String studentIds = "";
			Log.e("allStudents", "=="+allStudents.size());
			if(allStudents != null){
				for(int i=0; i<allStudents.size();i++){
					if(i != 0){
						studentIds +=",";
					}	
					studentIds += String.valueOf(allStudents.get(i).sid);
				}
				params.add(new BasicNameValuePair("studentIds", studentIds));
			}else {
				return;
			}
			if (badgeInfo != null) {
					params.add(new BasicNameValuePair("badgeId", String.valueOf(badgeInfo.badgeId)));
					params.add(new BasicNameValuePair("count", String.valueOf(1)));
					params.add(new BasicNameValuePair("title", badgeInfo.badgeTitle));
					params.add(new BasicNameValuePair("remark", badgeInfo.badgeRemark));
					params.add(new BasicNameValuePair("bonuspoint", String.valueOf(badgeInfo.bonuspoint)));
					params.add(new BasicNameValuePair("resId", String.valueOf(badgeInfo.badgeTitleId)));
			}
		

		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(this, "", "...loading...");
		}

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						try {
							if (response.getInt("code") == 0) {
								Intent intent = new Intent();
								Toast.makeText(BadgeSelectActivity.this, "颁发成功", Toast.LENGTH_SHORT).show();
									
								intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_INTEGRAL, badgeInfo.bonuspoint);
								intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_COUNT, 1);
								ArrayList<String> urls=new ArrayList<String>();
								urls.add(badgeInfo.imgUrl);
								ArrayList<String> names=new ArrayList<String>();
								names.add(badgeInfo.badgeTitle);
								intent.putStringArrayListExtra(MyBundleName.STUDENT_BADGE_SELECT_URLS, urls);
								intent.putStringArrayListExtra(MyBundleName.STUDENT_BADGE_SELECT_NAME, names);			
								setResult(RESULT_OK, intent);
								BadgeSelectActivity.this.finish();
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(BadgeSelectActivity.this);
								finish();
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(BadgeSelectActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
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
						Toast.makeText(BadgeSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private ProgressDialog progressDialog;
	

	private int totalCount = 0;
	private int totalIntegral = 0;
	private ArrayList<String> badgeUrls = new ArrayList<String>();
	private ArrayList<String> badgeNames = new ArrayList<String>();

	private StudentBean student;

	private GridView mStudentGridView;
	private GridView mBadgeGridView;

	private ListView badgeOftenView;

	private OftenBadgeAdapter oftenBadgeAdapter;


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				int badgeCount = data.getIntExtra(MyBundleName.STUDENT_BADGE_SELECT_COUNT, 0);
				String badgeUrl = data.getStringExtra(MyBundleName.STUDENT_BADGE_SELECT_URLS);
				String badgeName = data.getStringExtra(MyBundleName.STUDENT_BADGE_SELECT_NAME);
				int badgeIntegral = data.getIntExtra(MyBundleName.STUDENT_BADGE_SELECT_INTEGRAL, 0);
				totalCount += badgeCount;
				totalIntegral += badgeIntegral;
				badgeUrls.add(badgeUrl);
				badgeNames.add(badgeName);
				backStudentList();
			}else if (requestCode == BADGE_MANAGE_REQUEST_CODE ) {//从常用小红花管理界面返回
				oftenBadgeAdapter.setData(BadgeInfoUtil.getCustomFlowers());
				oftenBadgeAdapter.notifyDataSetChanged();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	

	private void backStudentList() {
		if (totalCount != 0) {
			Intent intent = new Intent();
			intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_INTEGRAL, totalIntegral);
			intent.putExtra(MyBundleName.STUDENT_BADGE_SELECT_COUNT, totalCount);
			intent.putStringArrayListExtra(MyBundleName.STUDENT_BADGE_SELECT_URLS, badgeUrls);
			intent.putStringArrayListExtra(MyBundleName.STUDENT_BADGE_SELECT_NAME, badgeNames);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			Intent intent = new Intent();
			setResult(7, intent);
			finish();
		}
	}
	
	@Override  
    public void onBackPressed() {  
		Intent intent = new Intent();
		setResult(7, intent);
		finish();
    }  

}
