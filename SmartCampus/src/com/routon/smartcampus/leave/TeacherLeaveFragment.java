package com.routon.smartcampus.leave;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.widgets.Toast;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TeacherLeaveFragment extends BaseFragment implements OnClickListener{

	private PopupWindow mPopupWindow;
	private View popView;
	private ListView leaveListView;
	private static final String TAG = "TeacherLeaveFragment";
	private ProgressDialog progressDialog;
	private List<TeacherLeaveBean> leaveBeans=new ArrayList<TeacherLeaveBean>();
	private TextView leaveCountTv;
//	private TextView leaveTimeTv;
	private TextView evectionCountTv;
//	private TextView evectionTimeTv;
	private boolean isHeadTeacher;
//	public LeaveTimeUtil leaveTimeUtil;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_teacher_leave_layout, container, false);
		return view;
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if( InfoReleaseApplication.authenobjData.timetable_privilege == 1){
			isHeadTeacher = true;
		}
		
		initView(getView());

		initData();
	
	}

	private void initView(View view) {
		ImageView leaveAddBtn=(ImageView) view.findViewById(R.id.leave_add_btn);
		leaveAddBtn.setOnClickListener(this);
		
		ImageView leaveBackBtn=(ImageView) view.findViewById(R.id.leave_back_btn);
		leaveBackBtn.setOnClickListener(this);
		
		if (isHeadTeacher) {
			leaveBackBtn.setVisibility(View.GONE);
		}
		
		leaveListView = (ListView) view.findViewById(R.id.leave_listview);
		leaveCountTv = (TextView) view.findViewById(R.id.leave_count_tv);
//		leaveTimeTv = (TextView) view.findViewById(R.id.leave_time_tv);
		evectionCountTv = (TextView) view.findViewById(R.id.evection_count_tv);
//		evectionTimeTv = (TextView) view.findViewById(R.id.evection_time_tv);
		
		popView = getActivity().getLayoutInflater().inflate(R.layout.leave_menu_layout, null);
		
		mPopupWindow = new PopupWindow(popView, dip2px(140), dip2px(90));
		
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
	}
	

	private void initData() {
		
		getUserTeacherLeaveData();
		//获取学校作息时间表，需要根据作息时间来计算老师的请假时长
		/*String groupIds=InfoReleaseApplication.authenobjData.groupIds;
		int groupId = 0;
		if (groupIds!=null) {
			if (groupIds.contains(",")) {
				groupId=Integer.valueOf(groupIds.substring(0, groupIds.lastIndexOf(",")));
			}else {
				groupId=Integer.valueOf(groupIds);
			}
		}
		
		showMyProgressDialog();
		leaveTimeUtil = new LeaveTimeUtil(getContext(),groupId);
		leaveTimeUtil.getSchoolTime(new OnListener() {
			
			@Override
			public void onResponse() {
				getUserTeacherLeaveData();
			}
		},new OnErrorListener() {
			
			@Override
			public void onError() {
				getUserTeacherLeaveData();
			}
		});*/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.leave_add_btn:
			showPopupMenu(v);
			
			break;
		case R.id.leave_back_btn:
			getActivity().finish();
			
			break;

		default:
			break;
		}
		
	}

	
	/**
	 * 获取用户老师请假数据
	 * */
	private void getUserTeacherLeaveData() {
		showMyProgressDialog();
		
		String urlString = "";
	    urlString = SmartCampusUrlUtils.getTeacherLeaveUrl(null,null,null);
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
								
								
								JSONObject countJson=response.getJSONObject("count");
								int leaveCount=countJson.optInt("totalleavetime");
								int leaveTime=countJson.optInt("totalleavemin");
								int evectionCount=countJson.optInt("totaltriptime");
								int evectionTime=countJson.optInt("totaltripmin");
								
								
								JSONArray array = response.optJSONArray("datas");
								if (array==null) {
										Toast.makeText(getContext(), response.getString("msg"), Toast.LENGTH_LONG).show();
									return;
								}
								
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									TeacherLeaveBean bean=new TeacherLeaveBean(obj);
//									if (bean.catalog==0) {
//										leaveCount += 1;
////										leaveTime +=getTime(bean);
//										
//										leaveTime +=leaveTimeUtil.getTime(bean.startTime,bean.endTime);
//									}else if (bean.catalog==1) {
//										evectionCount += 1;
//										evectionTime +=leaveTimeUtil.getTime(bean.startTime,bean.endTime);
//									}
									
									leaveBeans.add(bean);
								}
								
								leaveCountTv.setText("请假："+leaveCount+"次   共计 "+getM(leaveTime));
								evectionCountTv.setText("出差："+evectionCount+"次   共计 "+getM(evectionTime));
								
							/*	if (leaveTime>0) {
									leaveCountTv.setText("请假："+leaveCount+"次   共计 "+getDiff(leaveTime));
								}else {
									leaveCountTv.setText("请假："+leaveCount+"次   共计 0时0分");
								}
								if (evectionTime>0) {
									evectionCountTv.setText("出差："+evectionCount+"次   共计 "+getDiff(evectionTime));
								}else {
									evectionCountTv.setText("出差："+evectionCount+"次   共计 0时0分");
								}*/
								
								Collections.reverse(leaveBeans);
								TeacherLeaveAdapter adapter=new TeacherLeaveAdapter(getContext(), leaveBeans);
								leaveListView.setAdapter(adapter);
								
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
	
	private long getTime(TeacherLeaveBean bean) {
		
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = format.parse(bean.startTime);
			endDate =format.parse(bean.endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		if (startDate==null ||endDate==null) {
			return 0;
		}
		return endDate.getTime()-startDate.getTime();
	}
	
	private String getDiff(long diff) {
		long nh = 1000 * 60 * 60;
	    long nm = 1000 * 60;
		
				// 计算差多少小时
			    long hour = diff / nh;
			    // 计算差多少分钟
			    long min = diff % nh / nm;
				return hour+"时"+min+"分";
			    
	}
	
	private String getM(int m) {
		
				// 计算差多少小时
			    long hour = m / 60;
			    // 计算差多少分钟
			    long min = m % 60;
				return hour+"时"+min+"分";
			    
	}
	
	private void showPopupMenu(View v) {
		mPopupWindow.showAsDropDown(v);

		
		popView.findViewById(R.id.teacher_leave_menu_btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {//请假
				Intent intent=new Intent(getContext(), TeacherLeaveActivity.class);
				intent.putExtra("leave_type", 0);
				startActivityForResult(intent, 1);
				mPopupWindow.dismiss();
			}
		});
		
		popView.findViewById(R.id.teacher_evection_menu_btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {//出差
				Intent intent=new Intent(getContext(), TeacherLeaveActivity.class);
				intent.putExtra("leave_type", 1);
				startActivityForResult(intent, 1);
				mPopupWindow.dismiss();
			}
		});
		
		
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		LeaveActivity activity=(LeaveActivity) getActivity();
//		Log.e("run", "isUpdateUserLeaveData=="+activity.isUpdateUserLeaveData);
		if (activity.isUpdateUserLeaveData) {
			getUserTeacherLeaveData();
			activity.isUpdateUserLeaveData=false;
		}
		super.onHiddenChanged(hidden);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {	
				getUserTeacherLeaveData();
				LeaveActivity activity=(LeaveActivity) getActivity();
				activity.isUpdateTeacherLeaveData=true;
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
