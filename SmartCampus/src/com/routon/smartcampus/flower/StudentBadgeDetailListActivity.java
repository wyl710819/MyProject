package com.routon.smartcampus.flower;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.edurelease.R;
import com.routon.smartcampus.flower.StudentBadgeListAdapter.OnItemRetractListener;
import com.routon.smartcampus.utils.MyBundleName;

public class StudentBadgeDetailListActivity extends CustomTitleActivity{
	
	private final String TAG = "StudentBadgeDetailListActivity";
	
	private StudentBadgeListAdapter adapter;
	public static ArrayList<StudentBadge> studentBadges = new ArrayList<StudentBadge>();
	private ListView listView;
	private boolean isRetract = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		listView = new ListView(this);
		setContentView(listView);
		
		Bundle bundle = getIntent().getExtras();
		final int studentId = bundle.getInt(MyBundleName.STUDENT_ID);
//		studentBadges =  bundle.getParcelableArrayList(CommonBundleName.STUDENT_BADGE_LIST);
//		
//		Log.i(TAG, "length:"+studentBadges.size());
//		for (StudentBadge st : studentBadges){
//			st.dump();
//			
//		}
		
		Log.i(TAG, "count:"+studentBadges.size());
		initTitleBar("小红花详细列表");
		
		adapter = new StudentBadgeListAdapter(this, studentBadges);
		adapter.setOnItemRetractListener(new OnItemRetractListener() {
			
			@Override
			public void onItemRetract(final int position) {
//				retractBadge(studentId, studentBadges.get(position).id, position);
				
				new AlertDialog.Builder(StudentBadgeDetailListActivity.this)
					.setTitle("是否撤消当前小红花！")
					.setNegativeButton("取消", null)
					.setPositiveButton("撤消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							isRetract = true;
							studentBadges.remove(position);
							adapter.setDatas(studentBadges);
							adapter.notifyDataSetChanged();
						}
					}).show();
			}
		});
		listView.setAdapter(adapter);
		
		setTitleNextBtnClickListener(getResources().getString(R.string.menu_finish), new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra(MyBundleName.STUDENT_BADGE_IS_RETRACT, isRetract);
				setResult(MyBundleName.STUDENT_BADGE_DETAIL_RESULT, intent);
				finish();
			}
		});
		
		ListViewAnimationFactory.setListViewLayoutAnim(listView, this, R.anim.listview_item_slid_right);
	}

	protected void onDestroy() {
		super.onDestroy();
		studentBadges.clear();
	};
	
	ProgressDialog progressDialog;
	private void retractBadge(int studentId, int badgeId, final int position){
		String urlString ="";// SmartCampusUrlUtils.getUndoBadgeUrl(studentId, badgeId);
		progressDialog = new ProgressDialog(this);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
	                Request.Method.POST, urlString, null, new Response.Listener<JSONObject>() {  
	                    @Override  
	                    public void onResponse(JSONObject response) {  
	                        Log.d(TAG, "response="+response);  
	                        if (progressDialog!=null && progressDialog.isShowing()) {  
	                        	progressDialog.dismiss();  
	                        }	                        
							try {
								if(response.getInt("code") == 0){
									isRetract = true;
									studentBadges.remove(position);
									adapter.setDatas(studentBadges);
									listView.notify();
									
		                        }else if(response.getInt("code") == -2){
		                        	InfoReleaseApplication.returnToLogin(StudentBadgeDetailListActivity.this);
		                        }else{//失败
		                        	Log.e(TAG, response.getString("msg"));  
		                        	Toast.makeText(StudentBadgeDetailListActivity.this, response.getString("msg"), Toast.LENGTH_LONG).show();
		                        	
		                        }

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                        
	                    }  
	                },   
	                new Response.ErrorListener() {  
	                    @Override  
	                    public void onErrorResponse(VolleyError arg0) {  
	                    	Log.e(TAG, "sorry,Error"); 
	                    	Toast.makeText(StudentBadgeDetailListActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
	                    	if (progressDialog!=null && progressDialog.isShowing()) {  
	                    		progressDialog.dismiss();  
	                        }  

	                    }  
	                });  
	        
	        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
	        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 		
	}

}
