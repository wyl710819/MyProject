package com.routon.smartcampus.leave;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.network.SmartCampusUrlUtils;

import android.content.Context;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class StudentLeaveStatActivity extends CustomTitleActivity {

	static final String TAG = "StudentLeaveStatActivity";
	Context m_context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_leave_stat);
		m_context = this;

		initView();
		initData();
	}
	
	ListView studentLeaveRankingView;
	void initView() {
		initTitleBar("统计排行");

		setTitleBackBtnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		studentLeaveRankingView = (ListView) findViewById(R.id.leave_stat_lv1);
	}

	void initData() {
		String groupId = getIntent().getStringExtra("groupId");
		
		getStudentLeaveRankingData(groupId);
	}

	MatrixCursor slrd_mc;
	void getStudentLeaveRankingData(String groupid) {
		Log.i(TAG, "getStudentLeaveRankingData("+groupid+")");
		
		String urlString = SmartCampusUrlUtils.getStudentLeaveRanking(groupid);
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
	                			"studentName", "leaveTime", "totalTime", "ranking"
	                	});
	                	JSONArray datas = jo.optJSONArray("datas");
	                	if (datas==null) {
	                		Object[] columnValues = {1, jo.getString("msg"), null,
	                				null, null, null, null};
	                		cursor.addRow(columnValues);
	                		code = 1;
	                	} else {
	                        JSONObject dataobj;
	                        for (int i=0;i<datas.length();i++) {
	                            dataobj = datas.getJSONObject(i);
	                            Integer sid = dataobj.optInt("sid");
	                            String studentName = dataobj.optString("studentName");
	                            Integer leaveTime = dataobj.optInt("leaveTime");
	                            Integer totalTime = dataobj.optInt("totalTime");
	                            Integer ranking = dataobj.optInt("ranking");
	                            Object[] columnValues = {code, jo.getString("msg"), sid,
	                                studentName, leaveTime, totalTime, ranking};
	                            cursor.addRow(columnValues);
	                        }
	                        // when no record found, return code==1
	                        // we need to take special care of this case...
	                        if (cursor.getCount()==0) {
	                            Object[] columnValues = {1, jo.getString("msg"), null,
	                                    null, null, null, null};
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
	            
	            slrd_mc = cursor;
	            if (code==0) {
	            	Log.i(TAG, "update student leave ranking data");
                    StudentLeaveRankingAdapter slca = new StudentLeaveRankingAdapter(m_context, cursor, 0);
                    studentLeaveRankingView.setAdapter(slca);
	            } else {
	            	studentLeaveRankingView.setAdapter(null);
	            	cursor.moveToFirst();
	            	String msg = cursor.getString(cursor.getColumnIndex("msg"));
	            	Log.e(TAG, "code="+String.valueOf(code)+" msg="+msg);
	            }

			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.e(TAG, "Error on "+arg0.toString());
			}
		});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
}
