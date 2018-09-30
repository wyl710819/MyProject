package com.routon.smartcampus.leave;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.network.SmartCampusUrlUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StudentLeaveCardAdapter extends CursorAdapter {

	final static String TAG = "StudentLeaveCardAdapter";
	StudentLeaveFragment m_parent;

	public StudentLeaveCardAdapter(Context context, Cursor c, int flags, StudentLeaveFragment parent) {
		super(context, c, flags);
		m_parent = parent;
	}

	void setStudentLeaveReview(String leaveId, int type) {
		Log.i(TAG, "setStudentLeaveReview("+leaveId+", "+type+")");
		
		String urlString = SmartCampusUrlUtils.setStudentLeaveReview(leaveId, String.valueOf(type));
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
                    cursor = new MatrixCursor(new String[] {"code", "msg"});
                    Object[] columnValues = {code, jo.getString("msg")};
                    cursor.addRow(columnValues);
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
	            m_parent.updateData();

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

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView sntv1 = (TextView) view.findViewById(R.id.student_name_tv1);
		TextView lttv1 = (TextView) view.findViewById(R.id.leave_time_text_tv1);
		TextView lrtv1 = (TextView) view.findViewById(R.id.leave_reason_text_tv1);
		ImageView lcciv1 = (ImageView) view.findViewById(R.id.student_leave_card_corner_iv1);
		RelativeLayout slc_rl1 = (RelativeLayout) view.findViewById(R.id.slc_rl1);
		RelativeLayout slc_rl2 = (RelativeLayout) view.findViewById(R.id.slc_rl2);
		RelativeLayout slc_rl3 = (RelativeLayout) view.findViewById(R.id.slc_rl3);
		RelativeLayout slc_rl4 = (RelativeLayout) view.findViewById(R.id.slc_rl4);

		final String leaveid = cursor.getString(cursor.getColumnIndex("_id"));
//		String sid = cursor.getString(cursor.getColumnIndex("sid"));
		String studentName = cursor.getString(cursor.getColumnIndex("studentName"));
		String startTime = cursor.getString(cursor.getColumnIndex("startTime"));
		String endTime = cursor.getString(cursor.getColumnIndex("endTime"));
		String reason = cursor.getString(cursor.getColumnIndex("reason"));
		int status = cursor.getInt(cursor.getColumnIndex("status"));
		
		sntv1.setText(studentName);
		lttv1.setText(startTime+" 到 "+endTime);
		lrtv1.setText(reason);
		if (status==0) {
			slc_rl1.setVisibility(View.VISIBLE);
			slc_rl2.setVisibility(View.GONE);
			slc_rl3.setVisibility(View.VISIBLE);
			slc_rl4.setVisibility(View.GONE);
			lcciv1.setVisibility(View.VISIBLE);
			slc_rl1.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					setStudentLeaveReview(leaveid, 1);
				}
				
			});
			slc_rl3.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					setStudentLeaveReview(leaveid, 2);
				}
				
			});
		} else if (status==1) {
			slc_rl1.setVisibility(View.GONE);
			slc_rl2.setVisibility(View.VISIBLE);
			slc_rl3.setVisibility(View.GONE);
			slc_rl4.setVisibility(View.GONE);
			lcciv1.setVisibility(View.INVISIBLE);
		} else if (status==2) {
			slc_rl1.setVisibility(View.GONE);
			slc_rl2.setVisibility(View.GONE);
			slc_rl3.setVisibility(View.GONE);
			slc_rl4.setVisibility(View.VISIBLE);
			lcciv1.setVisibility(View.INVISIBLE);
		} else {
			slc_rl1.setVisibility(View.GONE);
			slc_rl2.setVisibility(View.GONE);
			slc_rl3.setVisibility(View.GONE);
			slc_rl4.setVisibility(View.GONE);
			lcciv1.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater li = LayoutInflater.from(context);
		View v = li.inflate(R.layout.student_leave_card_item, parent, false);
		return v;
	}

}
