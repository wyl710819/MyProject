package com.routon.smartcampus.notify;

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
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class TeacherNotifyListActivity extends CustomTitleActivity {

	static final String TAG = "TeacherNotifyListActivity";
	Context m_context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teacher_notify_list);
		m_context = this;
		
		initView();
		initData();
	}
	
	ListView m_tnlv;
	
	void initView() {
		initTitleBar(R.string.notify_list_title);
		
		setTitleBackBtnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		setTitleNextImageBtnClickListener(com.routon.inforelease.R.drawable.ic_add,
				new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(TeacherNotifyListActivity.this, NotifyIssueActivity.class);
				startActivity(intent);

			}
		});
		
		m_tnlv = (ListView) findViewById(R.id.teacher_notify_lv1);

	}
	
	void initData() {
		
		getTeacherNotifyListData(null);
	}

	void getTeacherNotifyListData(String flagId) {
		Log.i(TAG, "getTeacherNotifyListData("+flagId+")");
		
		String urlString = SmartCampusUrlUtils.getSchoolNotifyList(flagId);
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
	            
	            // slrd_mc = cursor;
	            if (code==0) {
	            	Log.i(TAG, "update teacher notify list ranking data");
                    TeacherNotifyListAdapter slca = new TeacherNotifyListAdapter(m_context, cursor, 0);
                    m_tnlv.setAdapter(slca);
	            } else {
	            	m_tnlv.setAdapter(null);
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
