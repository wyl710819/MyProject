package com.routon.smartcampus.leave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseFragment;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.leave.TeacherLeaveReviewAdapter.OnConfirmListener;
import com.routon.smartcampus.leave.TeacherLeaveReviewAdapter.OnRejectListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class TeacherFragment extends BaseFragment {

	private static final String TAG = "TeacherFragment";
	private ProgressDialog progressDialog;
	private ListView teacherLeaveLv;
	private List<TeacherLeaveBean> leaveBeans=new ArrayList<TeacherLeaveBean>();
	private TeacherLeaveReviewAdapter mAdapter;
	private String[] schoolIds;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_teacher_layout, container, false);
		return view;
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initView(getView());

		initData();
	
	}

	private void initView(View view) {
		teacherLeaveLv = (ListView) view.findViewById(R.id.teacher_leave_lv);
	}
	

	private void initData() {
		schoolIds = InfoReleaseApplication.authenobjData.schoolIds;
		if (schoolIds==null || schoolIds.length<=0) {
			return;
		}
		getTeacherLeaveData(schoolIds[0]);
	}


	
	/**
	 * 获取老师请假数据
	 * @param schoolIds 
	 * */
	private void getTeacherLeaveData(String schoolId) {
		showMyProgressDialog();
		
		String urlString = "";
	    urlString = SmartCampusUrlUtils.getTeacherLeaveUrl(null,null,schoolId);
	    Log.d(TAG,"CommitUrl="+urlString);
	    
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								leaveBeans.clear();
								JSONArray array = response.optJSONArray("datas");
								if (array==null) {
										Toast.makeText(getContext(), response.getString("msg"), Toast.LENGTH_LONG).show();
									return;
								}
								
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									TeacherLeaveBean bean=new TeacherLeaveBean(obj);
									leaveBeans.add(bean);
									
								}
								Collections.reverse(leaveBeans);
								mAdapter = new TeacherLeaveReviewAdapter(getContext(), leaveBeans);
								
								mAdapter.setOnConfirmListener(new OnConfirmListener() {//批准
									
									@Override
									public void onConfirmClick(int position) {
										leaveReview(leaveBeans.get(position).id,1,position);
									}
								});
								
								mAdapter.setOnRejectListener(new OnRejectListener() {//驳回
									
									@Override
									public void onRejectClick(int position) {
										leaveReview(leaveBeans.get(position).id,2,position);
									}
								});
								
								teacherLeaveLv.setAdapter(mAdapter);
								
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
								Toast.makeText(getContext(), "登录已失效!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(getContext(), response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}



					
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Toast.makeText(getContext(), "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();
						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	    
	}
	
	
	/**
	 * 审批请假
	 * */
	private void leaveReview(int leaveId,final int type,final int position) {
		showMyProgressDialog();
		
		String urlString = "";
	    urlString = SmartCampusUrlUtils.getLeaveReviewUrl(String.valueOf(leaveId),String.valueOf(type));
	    Log.d(TAG,"CommitUrl="+urlString);
	    
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								reportToast("审批成功");
								leaveBeans.get(position).status=type;
								mAdapter.notifyDataSetChanged();
								LeaveActivity activity=(LeaveActivity) getActivity();
								activity.isUpdateUserLeaveData=true;
								
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
								Toast.makeText(getContext(), "登录已失效!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(getContext(), response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}



					
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Toast.makeText(getContext(), "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();
						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	    
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		LeaveActivity activity=(LeaveActivity) getActivity();
		if (activity.isUpdateTeacherLeaveData) {
			if (schoolIds==null || schoolIds.length<=0) {
				return;
			}
			getTeacherLeaveData(schoolIds[0]);
			activity.isUpdateTeacherLeaveData=false;
		}
		super.onHiddenChanged(hidden);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {	
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private int dip2px(float dpValue) {

		final float scale = getResources().getDisplayMetrics().density;

		return (int) (dpValue * scale + 0.5f);

	}
	
	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(getContext(), "",
					"...loading...");
		}
	}

	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
}
