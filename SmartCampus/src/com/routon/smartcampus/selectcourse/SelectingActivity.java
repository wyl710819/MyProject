package com.routon.smartcampus.selectcourse;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.selectcourse.CheckGroupAdapter.ViewHolder;
import com.routon.widgets.Toast;

public class SelectingActivity extends BaseActivity{
	private ArrayList<String> datalist;
	private ArrayList<SelectPlansBean> mSelectPlanBeans;
	private ArrayList<StudentSelectBean> mStudentSelectBeans;
	private GridView mGridview;
	private CheckGroupAdapter mAdapter;
	private SelectCourseBean mSelectCourse;
	private ProgressDialog progressDialog;
	private static String TAG = "SelectingActivity";
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mSelectCourse = (SelectCourseBean) getIntent().getSerializableExtra("classTypeBean");
		setContentView(R.layout.activity_selecting_layout);
		initViews();
		initDatas();
	}

	public void initViews()
	{
		//		datalist = new ArrayList<String>();
		//		String []stringsdata = {"物理","化学","生物","地理","历史","政治"};
		//		for(int i=0;i<6;i++)
		//		{
		//			datalist.add(stringsdata[i]);
		//		}
		TextView textview = (TextView)findViewById(R.id.name);
		textview.setText(mSelectCourse.entityName);
		TextView descview = (TextView)findViewById(R.id.describe);
		descview.setText("1、必须选择1个分类下的课程。\n2、选完所有学科后，请点击提交选课结果进行提交，否则选课无效。\n3、在选课进行时间内，随时可以修改所选学科（修改完后也必须进行提交）。");
		mGridview = (GridView)findViewById(R.id.gridview);

		mGridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ViewHolder holder = (ViewHolder) view.getTag(); 
				holder.cb.toggle(); 

				mAdapter.getIsSelected().put(position, holder.cb.isChecked()); 
			}

		});
		Button submitbtn = (Button)findViewById(R.id.submit_btn);
		submitbtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mAdapter!=null){
					HashMap<Integer,Boolean> selectedmap = mAdapter.getIsSelected();
					int count=0;
					String subjectIds =null;
					String subjectNames =null;
					for(int i=0;i<selectedmap.size();i++)
					{
						if(selectedmap.get(i))
						{
							if(subjectIds==null)
							{
								subjectIds=mSelectPlanBeans.get(0).subjectsList.get(i).subjectId;
								subjectNames=mSelectPlanBeans.get(0).subjectsList.get(i).subjectName;
							}
							else
							{
								subjectIds=subjectIds+","+mSelectPlanBeans.get(0).subjectsList.get(i).subjectId;
								subjectNames=subjectNames+","+mSelectPlanBeans.get(0).subjectsList.get(i).subjectName;
							}
							count++;
						}

					}
					if(count==3)
					{
						submitSelecting(subjectIds,subjectNames);
					}
					else
					{
						Toast.makeText(SelectingActivity.this, "请选三个！", Toast.LENGTH_SHORT).show();
					}


				}
			}
		});
	}
	public void initDatas()
	{
		getSubjectsData();
		getStudentsData();
	}
	private void submitSelecting(String subjectids,String subjectnames)
	{
		String urlString = SmartCampusUrlUtils.getSelectSubjectSubmitUrl(mSelectCourse.entityId,"45487","胡桐",subjectids,subjectnames,subjectids,subjectnames);//(""+mStudent.sid);
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
						Toast.makeText(SelectingActivity.this, "提交成功", Toast.LENGTH_SHORT).show();

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
				Toast.makeText(SelectingActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
				hideLoadDialog();
			}
		});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void getSubjectsData() {

		String urlString = SmartCampusUrlUtils.getSelectSubjectsUrl(mSelectCourse.entityId);//(""+mStudent.sid);

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
						JSONArray jsonArray = response.getJSONArray("items");
						if (jsonArray!=null) {
							mSelectPlanBeans=new ArrayList<SelectPlansBean>();
							for (int i = 0; i < jsonArray.length(); i++) {
								SelectPlansBean bean=new SelectPlansBean(jsonArray.getJSONObject(i));
								mSelectPlanBeans.add(bean);
							}
						}
						if(mSelectPlanBeans!=null && mSelectPlanBeans.size()>=1)
						{
							ArrayList<SelectSubjectsBean> subjectlist = mSelectPlanBeans.get(0).subjectsList;
							mAdapter=  new CheckGroupAdapter(subjectlist,SelectingActivity.this);
							if(mStudentSelectBeans!=null)
							{
								for(int i=0; i <mStudentSelectBeans.size();i++)
								{
									for(int j= 0; j<subjectlist.size(); j++)
									{
										if( mStudentSelectBeans.get(i).subjectId.equals(subjectlist.get(j).subjectId))
										{
											mAdapter.setChecked(j,true);
										}
									}
								}
							}
							mGridview.setAdapter(mAdapter);
						}

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
				Toast.makeText(SelectingActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
				hideLoadDialog();
			}
		});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	private void getStudentsData()
	{
		String urlString = SmartCampusUrlUtils.getStudentCoursesUrl(mSelectCourse.entityId,"45487");

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
						JSONArray jsonArray = response.getJSONArray("items");
						if (jsonArray!=null) {
							mStudentSelectBeans=new ArrayList<StudentSelectBean>();
							for (int i = 0; i < jsonArray.length(); i++) {
								StudentSelectBean bean=new StudentSelectBean(jsonArray.getJSONObject(i));
								mStudentSelectBeans.add(bean);
							}
						}
						if(mAdapter!=null)
						{
							ArrayList<SelectSubjectsBean> subjectlist = mAdapter.getDatas();
							for(int i=0; i <mStudentSelectBeans.size();i++)
							{
								for(int j= 0; j<subjectlist.size(); j++)
								{
									if( mStudentSelectBeans.get(i).subjectId.equals(subjectlist.get(j).subjectId))
									{
										mAdapter.setChecked(j,true);

									}
								}
							}
							mAdapter.notifyDataSetChanged();
						}

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
				Toast.makeText(SelectingActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
				hideLoadDialog();
			}
		});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	private void showLoadDialog(){
		if( progressDialog == null || !progressDialog.isShowing()){
			progressDialog = ProgressDialog.show(SelectingActivity.this, "", "...loading...");
		}
	}

	private void hideLoadDialog(){
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;

		}
	}
}
