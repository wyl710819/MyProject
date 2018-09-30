package com.routon.smartcampus.attendance;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.routon.widgets.Toast;
import com.routon.smartcampus.bean.StudentBean;
public class StudentAttendanceActivity extends CustomTitleActivity {
	private ListView mListView = null;
	private final static String TAG = "StudentAttendanceActivity";
	private StudentBean mStudentBean;
	private int studentId;
	private String mAppType;
	private String studentName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_attendance);
		Bundle bundle = getIntent().getExtras();
		mAppType= bundle.getString(MyBundleName.STUDENT_APP_TYPE);
		if(mAppType!=null&&mAppType.equals(MyBundleName.TYPE_ATTENDANCE)){
			 studentName = bundle.getString(MyBundleName.STUDENT_NAME);
			 studentId = bundle.getInt(MyBundleName.STUDENT_ID);
		}else{
			Intent intent = getIntent();
			if(intent != null)
			{
				mStudentBean = (StudentBean) intent.getSerializableExtra(MyBundleName.STUDENT_BEAN);
				studentId=mStudentBean.sid;
				studentName=mStudentBean.empName;
			}
			if(mStudentBean == null)
			{
				Toast.makeText(this, "没有绑定学生信息", Toast.LENGTH_LONG).show();
			    return;
			}
			
		}
		
		
		
//		studentId=mStudentBean.sid;
		initTitleBar(studentName+"的缺勤记录");
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));
		
		mListView = (ListView) this.findViewById(R.id.listview);
		
		getStudentAttendanceInfo(studentId,"2017-09-01 00:00:00","2017-12-01 00:00:00");
		
//		mListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_student_attendance_item,getData()));
		
 	}
	
//	private List<String> getData(){
//		ArrayList<String> datas = new ArrayList<String>();
//		datas.add("2017-03-02 第1节英语课");
//		datas.add("2017-03-02 第2节英语课");
//		datas.add("2017-03-02 第2节英语课");
//		datas.add("2017-03-02 第2节英语课");
//		datas.add("2017-03-02 第2节英语课");
//		return datas;
//	}
	
	private void getStudentAttendanceInfo(int studentId,String beginTime,String endTime){
		
//		List<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("beginTime", beginTime));
//		params.add(new BasicNameValuePair("endTime", endTime));
		String urlString = null;
		
		if(mAppType!=null&&mAppType.equals(MyBundleName.TYPE_ATTENDANCE)){
			urlString = SmartCampusUrlUtils.getStudentAttendanceUrl(String.valueOf(studentId),null,null);
		}else {
			urlString = SmartCampusUrlUtils.getCmdStudentAttendanceUrl(String.valueOf(studentId),null,null);
		}
		
		
		final ProgressDialog progressDialog = ProgressDialog.show(this, "", "...正在获取数据...");
		Log.e(TAG, "getStudentＡttendanceInfo urlString:"+urlString);
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
									ArrayList<AttendanceRecordBean> attenBeanList=new ArrayList<AttendanceRecordBean>();
									JSONObject jsonObject = response.optJSONObject("datas");
//									if(jsonObject==null){
//										Toast.makeText(StudentAttendanceActivity.this, "该学生无缺勤记录!", Toast.LENGTH_SHORT).show();
//									}else{
										JSONArray jsonAttence=jsonObject.optJSONArray("absence");
										if(jsonAttence!=null&&jsonAttence.length()!=0){
											ArrayList<String> datas = new ArrayList<String>();
											for(int i=0;i<jsonAttence.length();i++){
												JSONObject obj = (JSONObject) jsonAttence.get(i);
												AttendanceRecordBean bean=new AttendanceRecordBean(obj);
												attenBeanList.add(bean);
												String attenceRecord=bean.day+"第"+bean.lesson+"节"+bean.course+"课";
												datas.add(attenceRecord);
											}
											mListView.setAdapter(new ArrayAdapter<String>(StudentAttendanceActivity.this, R.layout.list_student_attendance_item,datas));
										}else{
											Toast.makeText(StudentAttendanceActivity.this, "该学生无缺勤记录!", Toast.LENGTH_SHORT).show();
										}
//									}
									
		                        }else if(response.getInt("code") == -2){
		                        	Toast.makeText(StudentAttendanceActivity.this, response.getString("msg"), Toast.LENGTH_SHORT).show();

		                        	InfoReleaseApplication.returnToLogin(StudentAttendanceActivity.this);
		                        }else{//失败
		                        	Log.e(TAG, response.getString("msg"));  
		                        	Toast.makeText(StudentAttendanceActivity.this, response.getString("msg"), Toast.LENGTH_LONG).show();
		                        	
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
	                    	Toast.makeText(StudentAttendanceActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
	                    	if (progressDialog!=null && progressDialog.isShowing()) {  
	                    		progressDialog.dismiss();  
	                        }  

	                    }  
	                });  
	        
	        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
	        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 		
	}
}
