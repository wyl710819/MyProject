package com.routon.smartcampus.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.widget.CountdownButton;
import com.routon.inforelease.widget.LoginHelper;
import com.routon.edurelease.R;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.utils.UtilHelper;
import com.routon.widgets.Toast;

public class PhoneAndVertifyActivity extends CustomTitleActivity{
	private static final String TAG = "PhoneAndVertifyActivity";
	private int mStudentId = -1;
	private EditText mPhoneEdit = null;
	private EditText mVertifyEdit = null;
	private CountdownButton mGetVertifyNum = null;
	private EditText mPwdEdt;
	private EditText mEnsurePwdEdt;
	
	private void saveAccount(){
		UserInfoData data = new UserInfoData(this);
		data.setIsParentRule(true);
		data.setParentPhone(mPhoneEdit.getText().toString());
		data.setParentVerifyNum(mPwdEdt.getText().toString());
		data.setParentFirstLogin(false);
		updateAccount();
	}
	
	private void updateAccount(){
		Intent intent = new Intent();
		intent.setAction(InfoReleaseApplication.LOGIN_ACCOUNT_UPDATE);
		this.sendBroadcast(intent);
	}
	
	private void updatePhoto(final String phone, final String vertifyCode, final String pwd){
		String url = SmartCampusUrlUtils.getUpdatePhoneCmdUrl(mStudentId, phone);
		this.showProgressDialog();
		Log.d(TAG,"updatePhoto url:"+url);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						Log.d(TAG,"response:"+response);
						int code = response.optInt("code",-1);
						if ( code == 0) {		
							if( mStudentId == -1 ){//不需要绑定学生号码
								hideProgressDialog();
								saveAccount();
								InfoReleaseApplication.returnToLogin(PhoneAndVertifyActivity.this, false);
								hideProgressDialog();
							}else{
								register(phone, vertifyCode, pwd);
							}
						} else {
							Toast.makeText(PhoneAndVertifyActivity.this, "注册失败", Toast.LENGTH_LONG).show();
							hideProgressDialog();
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {			
						
						InfoReleaseApplication.showNetWorkFailed(PhoneAndVertifyActivity.this);
						Toast.makeText(PhoneAndVertifyActivity.this, "注册失败", Toast.LENGTH_LONG).show();
						hideProgressDialog();
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void register(final String phone,String vertifyNum, String pwd){
		String url = SmartCampusUrlUtils.getParentAuthenCmdUrl();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("phone", phone));
		params.add(new BasicNameValuePair("verifyCode", vertifyNum));
		params.add(new BasicNameValuePair("pwd", pwd));
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, url, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						Log.d(TAG,"response:"+response);
						int code = response.optInt("code",-1);
						if ( code == 0) {		
							saveAccount();
							InfoReleaseApplication.returnToLogin(PhoneAndVertifyActivity.this, false);
							Toast.makeText(PhoneAndVertifyActivity.this, "注册成功", Toast.LENGTH_LONG).show();	
						} else {
							String msg = response.optString("msg");
							if(  msg != null && msg.isEmpty() == false ){
								Toast.makeText(PhoneAndVertifyActivity.this, msg, Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(PhoneAndVertifyActivity.this, "注册失败", Toast.LENGTH_LONG).show();
							}
							hideProgressDialog();
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {			
						//先判断网络状况
						if( true == InfoReleaseApplication.showNetWorkFailed(PhoneAndVertifyActivity.this) ){
							Toast.makeText(PhoneAndVertifyActivity.this, "注册失败", Toast.LENGTH_LONG).show();
						}
						hideProgressDialog();
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_and_vertify);
		
		this.initTitleBar("家长注册");
		
		mStudentId = this.getIntent().getIntExtra(MyBundleName.STUDENT_ID, -1);
		String nextBtnText = this.getIntent().getStringExtra(MyBundleName.NEXT_BTN_TEXT);
		
		mPhoneEdit = (EditText) this.findViewById(R.id.phone_edit);
		mVertifyEdit = (EditText) this.findViewById(R.id.vertify_edit);
		mPwdEdt = (EditText)findViewById(R.id.edt_first_pwd);
		mEnsurePwdEdt = (EditText)findViewById(R.id.edt_ensure_pwd);
		mGetVertifyNum = (CountdownButton) this.findViewById(R.id.get_vertify_num_text);
		mGetVertifyNum.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String phone = mPhoneEdit.getText().toString();
				if( UtilHelper.checkPhoneValid(phone,PhoneAndVertifyActivity.this)== true ){//有效的手机号码
					//获取验证码
					LoginHelper.getVertifyNum(PhoneAndVertifyActivity.this,phone,mGetVertifyNum);		
				}
			}
		});
		
		
		Button registerBtn = (Button) this.findViewById(R.id.btn_register);
		if( nextBtnText != null && nextBtnText.isEmpty() == false ){
			registerBtn.setText(nextBtnText);
		}
		registerBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String phone = mPhoneEdit.getText().toString();
				String vertifyCode  = mVertifyEdit.getText().toString();
				String pwd = mPwdEdt.getText().toString();
				String ensurePwd = mEnsurePwdEdt.getText().toString();
				if( UtilHelper.checkPhoneValid(phone,PhoneAndVertifyActivity.this)== false ){
					return;
				}
				if(!UtilHelper.checkPwdValid(PhoneAndVertifyActivity.this, pwd) ||
						!UtilHelper.checkPwdValid(PhoneAndVertifyActivity.this, ensurePwd)){
					return;
				}
				if(!pwd.equals(ensurePwd)){
					Toast.makeText(PhoneAndVertifyActivity.this, "两次密码输入不相等", Toast.LENGTH_SHORT).show();
					return;
				}
				if( vertifyCode == null || vertifyCode.trim().length() == 0  ){//空验证码
					Toast.makeText(PhoneAndVertifyActivity.this, R.string.vertify_num_is_empty, Toast.LENGTH_SHORT).show();
					return;
				}		
				updatePhoto(phone, vertifyCode, pwd);
			}
		});
	}
	
	
}
