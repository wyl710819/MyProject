package com.routon.smartcampus.leave;

import java.util.ArrayList;

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
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.ClassSelListViewAdapter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class StudentLeaveFragment extends BaseFragment implements OnClickListener {

	static final String TAG = "StudentLeaveFragment";

	ArrayList<String> mClassList;
	int mSelClassIndex = 0;
	boolean isClassListShow = true;
	TextView classTextView;
	ListView classListView;
	FrameLayout dropdownFl;
	View dropdownMask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_student_leave, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		initView(getView());

		initData();

	}
	
	ListView studentLeaveCardView;
	void initView(View view) {

		ImageView leave_stat_button=(ImageView) view.findViewById(R.id.leave_stat_icon_button1);
		leave_stat_button.setOnClickListener(this);
		ImageView back_button=(ImageView) view.findViewById(R.id.back_icon_button1);
		back_button.setOnClickListener(this);
		classTextView = (TextView) view.findViewById(R.id.tv_class);
		dropdownFl = (FrameLayout) view.findViewById(R.id.dropdown_fl);
		classListView = (ListView) view.findViewById(R.id.dropdown_listview);
		
		LinearLayout classSelView = (LinearLayout) view.findViewById(R.id.tv_class_ll);
		classSelView.setOnClickListener(this);

		dropdownMask = view.findViewById(R.id.dropdown_mask);
		dropdownMask.setOnClickListener(this);

		classListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelClassIndex = position;
				listViewOutAnim();
				classTextView.setText(mClassList.get(position));
                Integer groupId = m_classGroupIdList.get(position);
                m_classid = String.valueOf(groupId);
                getStudentLeaveCardData(m_classid);
			}
		});
		
		studentLeaveCardView = (ListView) view.findViewById(R.id.student_leave_lv1);
	}
	
	void initData() {
		m_classid = "0";
		
		//getClassListData();
		
		String[] headTeacherClasses = InfoReleaseApplication.authenobjData.headTeacherClasses;
		String groupId = headTeacherClasses[0];
		m_classid = groupId;
		getStudentLeaveCardData(groupId);
		
	}
	
	void updateData() {
		getStudentLeaveCardData(m_classid);
	}
	
	MatrixCursor slcd_mc;
	void getStudentLeaveCardData(String groupid) {
		Log.i(TAG, "getStudentLeaveCardData("+groupid+")");
		
		String urlString = SmartCampusUrlUtils.getStudentLeaveInfo(groupid);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				// TODO Auto-generated method stub
	            MatrixCursor cursor;
	            int code;
	            try {
	                JSONObject jo = response;
	                code = jo.getInt("code");
	                Log.i(TAG, response.toString());
	                if (code == 0) {
	                	cursor = new MatrixCursor(new String[] {"code", "msg", "_id",
	                			"sid", "studentName", "startTime", "endTime", "reason", "status"
	                	});
	                	JSONArray datas = jo.optJSONArray("datas");
	                	if (datas==null) {
	                		Object[] columnValues = {1, jo.getString("msg"), null,
	                				null, null, null, null, null, null};
	                		cursor.addRow(columnValues);
	                		code = 1;
	                	} else {
	                        JSONObject dataobj;
	                        for (int i=datas.length()-1; i>=0; i--) {
	                            dataobj = datas.getJSONObject(i);
	                            Integer leaveid = dataobj.optInt("leaveId");
	                            Integer sid = dataobj.optInt("sid");
	                            String studentName = dataobj.optString("studentName");
	                            String startTime = dataobj.optString("startTime");
	                            String endTime = dataobj.optString("endTime");
	                            String reason = dataobj.optString("reason");
	                            Integer status = dataobj.optInt("status");
	                            Object[] columnValues = {code, jo.getString("msg"), leaveid,
	                                sid, studentName, startTime, endTime, reason, status};
	                            cursor.addRow(columnValues);
	                        }
	                        // when no record found, return code==1
	                        // we need to take special care of this case...
	                        if (cursor.getCount()==0) {
	                            Object[] columnValues = {1, jo.getString("msg"), null,
	                                    null, null, null, null, null, null};
	                            cursor.addRow(columnValues);
	                            code = 1;
	                        }
	                	}
	                } else {
	                    cursor = new MatrixCursor(new String[] {"code", "msg"});
	                    Object[] columnValues = {code, jo.getString("msg")};
	                    cursor.addRow(columnValues);
	                }
	            } catch (JSONException e) {
	            	Log.e(TAG, e.toString());
	            	code = -4;
	                cursor = new MatrixCursor(new String[] {"code", "msg"});
	                Object[] columnValues = {code, "平台故障"};
	                cursor.addRow(columnValues);
	            } catch (java.lang.NullPointerException e) {
	            	Log.e(TAG, e.toString());
	            	code = -3;
	                cursor = new MatrixCursor(new String[] {"code", "msg"});
	                Object[] columnValues = {code, "网络访问超时"};
	                cursor.addRow(columnValues);
	            }
	            
	            slcd_mc = cursor;
	            if (code==0) {
	            	Log.i(TAG, "update student leave data");
                    StudentLeaveCardAdapter slca = new StudentLeaveCardAdapter(getContext(), cursor, 0, 
                    		StudentLeaveFragment.this);
                    studentLeaveCardView.setAdapter(slca);
	            } else {
	            	studentLeaveCardView.setAdapter(null);
	            	cursor.moveToFirst();
	            	String msg = cursor.getString(cursor.getColumnIndex("msg"));
	            	Log.e(TAG, "code="+String.valueOf(code)+" msg="+msg);
	            }

			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.e(TAG, "Error on "+arg0.toString());
				if( getOwnActivity() == null ){
					return;
				}
			}
		});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

    ProgressDialog progressDialog;

    void showLoadDialog(){
    	Log.d(TAG, "showLoadDialog");
		if (progressDialog == null) {
			progressDialog = ProgressDialog.show(getContext(), "", "...loading...");
			progressDialog.show();
		} else {
			progressDialog.show();
		}
	}
	void hideLoadDialog(){
		Log.d(TAG,"hideLoadDialog");
		if (progressDialog != null) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			
		}
	}

	String m_classid;
	// 获取指定班级的数据。 
	void getAllClassStudentListData(ArrayList<Integer> classGroupIdList, int updateIndex) {
		// so this should be an error
		Integer groupId = classGroupIdList.get(updateIndex);
		m_classid = String.valueOf(groupId);
		
		Log.e(TAG, "Current Class groupId is "+m_classid);
		//getStudentLeaveCardData(m_classid);
		return;

	}
	void getCurrentClass(final ArrayList<Integer> classGroupIdList) {
		String urlString = SmartCampusUrlUtils.getCurrentClass();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				Log.d(TAG, "getCurrentClass response=" + response);
				if( getOwnActivity() == null ){
					return;
				}
				int code = response.optInt("code");
				int updateIndex = 0;
				if (code == 0) {
					JSONObject data = response.optJSONObject("datas");
					if (data != null) {
						int groupId = data.optInt("groupId");
						// Log.d(TAG, "groupId="+groupId);
						for (int i = 0; i < classGroupIdList.size(); i++) {
							if (classGroupIdList.get(i).intValue() == groupId) {
								updateIndex = i;
								break;
							}
						}
					}
					// Log.d(TAG, "updateIndex="+updateIndex);
				} else if (code == -2) {
					InfoReleaseApplication.returnToLogin(StudentLeaveFragment.this.getActivity());
				} else {
					reportToast(response.optString("msg"));
				}
				getAllClassStudentListData(classGroupIdList, updateIndex);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				if( getOwnActivity() == null ){
					return;
				}
				Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
				// 获取当前班级失败，获取所有学生数据
				getAllClassStudentListData(classGroupIdList, 0);
			}
		});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	ArrayList<Integer> m_classGroupIdList;
	void getClassListData() {
		showLoadDialog();
		GroupListData.getClassListData(getOwnActivity(), new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub
				if( getOwnActivity() == null ){
					Log.e(TAG, "unexpected error here!");
					return;
				}
				m_classGroupIdList = new ArrayList<Integer>();
				mClassList = new ArrayList<String>();
				for (int i = 0; i < classGroups.size(); i++) {
					mClassList.add(classGroups.get(i).getName());
					m_classGroupIdList.add(classGroups.get(i).getId());
				}
				if (classGroups.size() > 0) {
					// 获取当前班级
					getCurrentClass(m_classGroupIdList);
					if (mClassList.size()>0) {
						mSelClassIndex = 0;
						classTextView.setText(mClassList.get(0));
					}
					classListView.setAdapter(new ClassSelListViewAdapter(getContext(), mClassList));
					hideLoadDialog();
				} else {
					hideLoadDialog();
				}

			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideLoadDialog();
				if( getOwnActivity() == null ){
					return;
				}
				
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideLoadDialog();
				if( getOwnActivity() == null ){
					return;
				}
				
			}
		});
	}
	void dropdownClick() {
		if (isClassListShow) {
			if (mClassList != null) {
				classListView.setAdapter(new ClassSelListViewAdapter(getContext(), mClassList));
				listViewInAnim();
			}
		} else {
			listViewOutAnim();
		}
	}
	void listViewInAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.VISIBLE);
		classListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_in));
		dropdownMask.setVisibility(View.VISIBLE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_in));
		dropdownFl.setVisibility(View.VISIBLE);
		isClassListShow = false;
	}

	void listViewOutAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.GONE);
		classListView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_menu_out));
		dropdownMask.setVisibility(View.GONE);
		dropdownMask.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		dropdownFl.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dd_mask_out));
		dropdownFl.setVisibility(View.GONE);
		isClassListShow = true;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_icon_button1:
			getActivity().finish();
			break;
		case R.id.leave_stat_icon_button1:
			Intent intent = new Intent();
			intent.setClass(getActivity(), StudentLeaveStatActivity.class);
			intent.putExtra("groupId", m_classid);
			startActivity(intent);
			break;
		case R.id.tv_class_ll:// 班级选择
			dropdownClick();
			break;
		case R.id.dropdown_mask:
			if (!isClassListShow) {
				listViewOutAnim();
			}
			break;

		default:
			break;
		}
	}

}
