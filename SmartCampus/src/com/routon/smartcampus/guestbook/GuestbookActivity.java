package com.routon.smartcampus.guestbook;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.flower.StudentBadge;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.schoolcompare.CompareClassTypeBean;
import com.routon.smartcampus.schoolcompare.SchoolCompareActivity;
import com.routon.smartcampus.schoolcompare.SchoolCompareAdapter;
import com.routon.smartcampus.utils.MyBundleName;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class GuestbookActivity extends BaseActivity {

	private static String TAG = "GuestbookActivity";
	private ListView guestbookView;
	private List<GuestbookBean> dataList;
	private TextView msgBut;
	private EditText msgEditview;
	private GuestbookAdapter mAdapter;
	private ProgressDialog progressDialog;
	private StudentBean studentBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guestbook_layout);

		studentBean = (StudentBean) getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);

		initView();
		initData();
	}

	private void initView() {

		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GuestbookActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};

		ImageView backBut = (ImageView) findViewById(R.id.back_btn);
		TextView titleView = (TextView) findViewById(R.id.title_view);
		titleView.setText("留言簿");
		backBut.setOnClickListener(mBackListener);

		guestbookView = (ListView) findViewById(R.id.guestbook_lv);
		msgBut = (TextView) findViewById(R.id.msg_but);
		msgEditview = (EditText) findViewById(R.id.msg_editview);
		msgBut.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (msgEditview.getText().toString().trim().equals("")) {
					Toast.makeText(GuestbookActivity.this, "留言内容为空", Toast.LENGTH_SHORT).show();
					return;
				} else {

					addMsg(String.valueOf(studentBean.sid), msgEditview.getText().toString());
					msgEditview.setText("");

					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
							Context.INPUT_METHOD_SERVICE);
					try {
						inputMethodManager.hideSoftInputFromWindow(
								GuestbookActivity.this.getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		});
	}

	private void initData() {
		dataList=new ArrayList<GuestbookBean>();
		GuestbookBean bean=new GuestbookBean();
		bean.type=2;
		dataList.add(bean);
		mAdapter = new GuestbookAdapter(GuestbookActivity.this, dataList);
		mAdapter.studentBean=studentBean;
		guestbookView.setAdapter(mAdapter);
		getMsgList(String.valueOf(studentBean.sid));

	}
	
	public static String getUTF8String(String str) {  
	    // A StringBuffer Object  
	    StringBuffer sb = new StringBuffer();  
	    sb.append(str);  
	    String strString = "";  
	    String strUTF8="";  
	    try {  
	    	strString = new String(sb.toString().getBytes("UTF-8"));  
	    	strUTF8 = URLEncoder.encode(strString, "UTF-8");  
	    } catch (UnsupportedEncodingException e) {  
	    e.printStackTrace();  
	    }  
	    return strUTF8;  
	    }  
	

	private void addMsg(String sid, final String msg) {

		List<NameValuePair> params=new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("msg", getUTF8String(msg)));
		
		String urlString = SmartCampusUrlUtils.getAddMsgboardUrl(sid, getUTF8String(msg));

		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								
								GuestbookBean userBean = new GuestbookBean();
								userBean.createTime=getTime();
								userBean.msg=msg;
								if (dataList!=null) {
									dataList.add(userBean);
									
								}else {
									dataList=new ArrayList<GuestbookBean>();
									dataList.add(userBean);
								}
								
								if (mAdapter==null) {
									mAdapter = new GuestbookAdapter(GuestbookActivity.this, dataList);
									mAdapter.studentBean=studentBean;
									guestbookView.setAdapter(mAdapter);
								}else {
									mAdapter.notifyDataSetChanged();
								}
								
								if (mAdapter.getCount()>0) {
									guestbookView.setSelection(mAdapter.getCount()-1);
								}
								
								Toast.makeText(GuestbookActivity.this, "留言成功！", Toast.LENGTH_SHORT)
								.show();
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(GuestbookActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(GuestbookActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
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
						Toast.makeText(GuestbookActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	private void getMsgList(String sid) {

		String urlString = SmartCampusUrlUtils.getMsgboardListUrl(sid);

		showLoadDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {


					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideLoadDialog();
						try {
							if (response.getInt("code") == 0) {
								
								JSONArray jsonArray = response.getJSONArray("datas");
								if (jsonArray!=null) {
									for (int i = 0; i < jsonArray.length(); i++) {
										GuestbookBean bean=new GuestbookBean(jsonArray.getJSONObject(i));
										dataList.add(bean);
										if (bean.readTime!=null && !bean.readTime.equals("")) {
											GuestbookBean childBean = new GuestbookBean();
											childBean.msg="知道了！";
											childBean.createTime=bean.readTime;
											childBean.type=1;
											dataList.add(childBean);
										}
									}
								}
								
								if (mAdapter==null) {
									mAdapter = new GuestbookAdapter(GuestbookActivity.this, dataList);
									mAdapter.studentBean=studentBean;
									guestbookView.setAdapter(mAdapter);
								}else {
									mAdapter.notifyDataSetChanged();
								}
								if (mAdapter.getCount()>0) {
									guestbookView.setSelection(mAdapter.getCount()-1);
								}
								
								
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(GuestbookActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(GuestbookActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
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
						Toast.makeText(GuestbookActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	private void showLoadDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(GuestbookActivity.this, "", "...loading...");
		}
	}

	private void hideLoadDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;

		}
	}
	
	private String getTime(){
		Date d = new Date();  
        System.out.println(d);  
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");  
        String dateNowStr = sdf.format(d);  
		return dateNowStr;
	}


}
