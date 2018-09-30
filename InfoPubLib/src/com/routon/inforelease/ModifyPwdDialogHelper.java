package com.routon.inforelease;

import java.lang.reflect.Field;

import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.widget.CountdownButton;
import com.routon.inforelease.widget.LoginHelper;
import com.routon.utils.UtilHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.routon.widgets.Toast;

public class ModifyPwdDialogHelper{
	
	public interface DialogAnswerInterface{
		void ok_callback();
	}
	
	private Activity mActivity;
	private Dialog mModifyPwdDialog;
	private DialogAnswerInterface mDialogAnswerInterface;
	private CountdownButton mGetVertifyBtn;
	private static final String TAG = "ModifyPwdDialogHelper";
	
	private Dialog mWaitDialog = null;
	
	private void hideProgressDialog(){
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	private void showProgressDialog(){
		mWaitDialog = new Dialog(mActivity,R.style.new_circle_progress);    
		mWaitDialog.setContentView(R.layout.dialog_wait);    
		mWaitDialog.show();
	}
	
	public ModifyPwdDialogHelper(Activity activity) {
		mActivity = activity;
		// TODO Auto-generated constructor stub
	}
	
	public void setModifyDialogShowingSate(boolean showing){
		try 
		{
			Field field = mModifyPwdDialog.getClass()
			.getSuperclass().getDeclaredField(
			"mShowing" );
			field.setAccessible( showing );
			// 将mShowing变量设为false，表示对话框已关闭 
			field.set(mModifyPwdDialog, showing );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void hideModifyDialog(){
		Log.d(TAG,"hideModifyDialog");
		if( mModifyPwdDialog == null ) return;
		setModifyDialogShowingSate(true);
		mModifyPwdDialog.dismiss();
		mModifyPwdDialog = null;
		
	}
	
	private boolean checkVertifyValid(String inputVertify){
		if( checkGetVertify() == false ) return false;
		if( inputVertify!= null && inputVertify.trim().length() > 0 ){//输入非空验证码
			return true;
		}
		Toast.makeText(mActivity, R.string.vertify_num_is_empty, Toast.LENGTH_SHORT).show();
		return false;
	}
	
	private boolean mGetVertify = false;
	
	private boolean checkGetVertify(){
		if( mGetVertify == false  ){//请先获取手机验证码
			Toast.makeText(mActivity, R.string.click_vertify_num, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	public void sendBindPhoneRequest(final String phone,final String pwd,final String vertifyCode){
		String updatePhoneUrl = UrlUtils.getUpdatePhoneUrl(phone,vertifyCode,pwd);
		Log.d(TAG,"sendBindPhoneRequest:"+updatePhoneUrl);
		showProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, updatePhoneUrl, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						int code = response.optInt("code",-1);
						Log.d(TAG,"response:"+response);
						hideProgressDialog();
						if ( code == 0) {	
							//保存新修改的密码
							if(InfoReleaseApplication.authenobjData != null ){							
								String name = mActivity.getSharedPreferences(InfoReleaseApplication.USERINFO, Context.MODE_PRIVATE).getString(InfoReleaseApplication.TAG_NAME,null);
								if( name.equals(InfoReleaseApplication.authenobjData.phoneNum)){//如果保存的name是手机号码，更新保存的手机号码
									mActivity.getSharedPreferences(InfoReleaseApplication.USERINFO, Context.MODE_PRIVATE).edit().putString(InfoReleaseApplication.TAG_NAME,phone).commit();
								}
								InfoReleaseApplication.authenobjData.phoneNum = phone;
							}
							InfoReleaseApplication.saveUserInfo(mActivity,null,pwd);
							hideModifyDialog();
							if( mDialogAnswerInterface != null ){
								mDialogAnswerInterface.ok_callback();
							}
							if(phone == null || phone.isEmpty() == true){
								Toast.makeText(mActivity, "修改密码成功,请重新登录", Toast.LENGTH_LONG).show();
							}else if( pwd == null || pwd.isEmpty() == true ){
								Toast.makeText(mActivity, "绑定手机号码成功", Toast.LENGTH_LONG).show();
							}else{
								Toast.makeText(mActivity, "绑定手机号码成功，修改密码成功", Toast.LENGTH_LONG).show();
							}							 				
						}else if( code == -2 ){
							InfoReleaseApplication.returnToLogin(mActivity);
						}else{
							Toast.makeText(mActivity, "绑定电话号码失败:"+response.optString("msg"),Toast.LENGTH_LONG).show();
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {	
						hideProgressDialog();
						//先判断网络状况
						if(false == InfoReleaseApplication.showNetWorkFailed(mActivity)){
							return;
						}
						Toast.makeText(mActivity, "绑定电话号码失败:"+arg0.getMessage(),Toast.LENGTH_LONG).show();
					}
				});
		 jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	public void sendResetPwdRequest(final String phone,final String pwd,final String vertifyCode,final boolean isParent){
		String url = UrlUtils.getResetPwdUrl(null,phone,pwd,vertifyCode);
		if( isParent == true && url.contains("app=smartcampus")){
			url = url.replace("app=smartcampus", "app=jz");
		}
		Log.d(TAG,"sendResetPwdRequest:"+url);
		showProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						int code = response.optInt("code",-1);
						Log.d(TAG,"response:"+response);
						hideProgressDialog();
						if ( code == 0) {	
							//保存新修改的密码
							if( isParent == false ){
								InfoReleaseApplication.saveUserInfo(mActivity,null,pwd);
							}else{
								InfoReleaseApplication.saveParentUserInfo(mActivity, phone, pwd);
							}
							hideModifyDialog();
							if( mDialogAnswerInterface != null ){
								mDialogAnswerInterface.ok_callback();
							}
							Toast.makeText(mActivity, "修改密码成功", Toast.LENGTH_LONG).show();							 				
						}else if( code == -2 ){
							InfoReleaseApplication.returnToLogin(mActivity);
						}else{
							Toast.makeText(mActivity, "修改密码失败:"+response.optString("msg"),Toast.LENGTH_LONG).show();
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {	
						hideProgressDialog();
						//先判断网络状况
						if(false == InfoReleaseApplication.showNetWorkFailed(mActivity)){
							return;
						}
						Toast.makeText(mActivity, "绑定电话号码失败:"+arg0.getMessage(),Toast.LENGTH_LONG).show();
					}
				});
		 jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	public void showModifyPwdDialog(String phonenum,boolean phoneEditable,boolean isParent,DialogAnswerInterface answer){
		showModifyPwdDialog(phonenum,phoneEditable,false,isParent,answer);
	}
	
	public void showUpdatePhoneDialog(DialogAnswerInterface answer,final boolean isParent,final String sid,String title){
		mDialogAnswerInterface = answer;
		if( mModifyPwdDialog == null ){
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle(title);
			LayoutInflater inflater = mActivity.getLayoutInflater();
			final View layout = inflater.inflate(R.layout.dialog_modify_pwd, null);
			
			final EditText et_phone = (EditText)layout.findViewById(R.id.phone_edit);
			et_phone.setText("");
			et_phone.setEnabled(true);
			et_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if( hasFocus == false ){//失去焦点时
						UtilHelper.checkPhoneValid(et_phone.getText().toString(),mActivity);
					}
				}
			});
			
			final EditText et_vertify = (EditText)layout.findViewById(R.id.et_vertify);
			et_vertify.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if( hasFocus == false ){//失去焦点时
						checkVertifyValid(et_vertify.getText().toString());//验证码初步验证正确
					}else{//获取焦点时先检查是否点击了获取验证码
						checkGetVertify();
					}
				}
			});
			
			layout.findViewById(R.id.newpwd_ll).setVisibility(View.GONE);
			layout.findViewById(R.id.confirmnewpwd_ll).setVisibility(View.GONE);
			
			mGetVertifyBtn = (CountdownButton)(layout.findViewById(R.id.btn_get_vertify));
			mGetVertifyBtn.setBeforeText("获取验证码");
			mGetVertifyBtn.setAfterText("秒");
			
			mGetVertifyBtn.setOnClickListener(new CountdownButton.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if( UtilHelper.checkPhoneValid(et_phone.getText().toString(),mActivity)== true ){//有效的手机号码
						mGetVertify = true;
						//获取验证码
						LoginHelper.getVertifyNum(mActivity,et_phone.getText().toString(),mGetVertifyBtn);				
					}
				}
			});
			builder.setView(layout);
			builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {				
					mModifyPwdDialog.dismiss();
					
					if( UtilHelper.checkPhoneValid(et_phone.getText().toString(),mActivity) == true && 
							checkVertifyValid(et_vertify.getText().toString()) == true ){
						if( isParent == true ){
							sendUpdateParentPhone(sid,et_phone.getText().toString(),et_vertify.getText().toString());
						}else{
							sendBindPhoneRequest(et_phone.getText().toString(),null,et_vertify.getText().toString());
						}
						return;					
					}

	    	  	}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					mModifyPwdDialog.dismiss();
	    	  	}
			});
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					setModifyDialogShowingSate(true);
					mModifyPwdDialog = null;
				}
			});
			mModifyPwdDialog = builder.create();
		}
		mModifyPwdDialog.show();
	}
	
	private String mPhone = null;
	
	public String getPhone(){
		return mPhone;
	}
	
	public void sendUpdateParentPhone(final String sid,final String phone,final String vertifyCode){
		String updatePhoneUrl = UrlUtils.getUpdateParentPhoneUrl(sid,phone,vertifyCode);
		Log.d(TAG,"sendBindPhoneRequest:"+updatePhoneUrl);
		showProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, updatePhoneUrl, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						int code = response.optInt("code",-1);
						Log.d(TAG,"response:"+response);
						hideProgressDialog();
						if ( code == 0) {	
							hideModifyDialog();
							mPhone = phone;
							if( mDialogAnswerInterface != null ){
								mDialogAnswerInterface.ok_callback();
							}
							Toast.makeText(mActivity, "绑定手机号码成功", Toast.LENGTH_LONG).show();											 				
						}else if( code == -2 ){
							InfoReleaseApplication.returnToLogin(mActivity);
						}else{
							Toast.makeText(mActivity, "绑定电话号码失败:"+response.optString("msg"),Toast.LENGTH_LONG).show();
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {	
						hideProgressDialog();
						//先判断网络状况
						if(false == InfoReleaseApplication.showNetWorkFailed(mActivity)){
							return;
						}
						Toast.makeText(mActivity, "绑定电话号码失败:"+arg0.getMessage(),Toast.LENGTH_LONG).show();
					}
				});
		 jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		 InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	public void showModifyPwdDialog(String phonenum,boolean phoneEditable,final boolean needBindPhone,final boolean isParent,DialogAnswerInterface answer){
		mDialogAnswerInterface = answer;
		Log.d(TAG,"showModifyPwdDialog");
		if( mModifyPwdDialog == null ){
			Log.d(TAG,"showModifyPwdDialog create");
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
      
			LayoutInflater inflater = mActivity.getLayoutInflater();
			final View layout = inflater.inflate(R.layout.dialog_modify_pwd, null);
			
			final EditText et_phone = (EditText)layout.findViewById(R.id.phone_edit);
			if( phoneEditable == false ){
				et_phone.setText(phonenum);
				et_phone.setEnabled(false);
			}
			et_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if( hasFocus == false ){//失去焦点时
						UtilHelper.checkPhoneValid(et_phone.getText().toString(),mActivity);
					}
				}
			});
			
			final EditText et_vertify = (EditText)layout.findViewById(R.id.et_vertify);
			et_vertify.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if( hasFocus == false ){//失去焦点时
						checkVertifyValid(et_vertify.getText().toString());//验证码初步验证正确
					}else{//获取焦点时先检查是否点击了获取验证码
						checkGetVertify();
					}
				}
			});
			
			final EditText et_newpwd = (EditText)layout.findViewById(R.id.newpwd);
			et_newpwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if( hasFocus == false ){//失去焦点时
						
					}
				}
			});

			final EditText et_confirmnewpwd = (EditText)layout.findViewById(R.id.confirmnewpwd);
			et_confirmnewpwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if( hasFocus == false ){//失去焦点时
						
					}
				}
			});
			
			mGetVertifyBtn = (CountdownButton)(layout.findViewById(R.id.btn_get_vertify));
			mGetVertifyBtn.setBeforeText("获取验证码");
			mGetVertifyBtn.setAfterText("秒");
			
			mGetVertifyBtn.setOnClickListener(new CountdownButton.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if( UtilHelper.checkPhoneValid(et_phone.getText().toString(),mActivity)== true ){//有效的手机号码
						mGetVertify = true;
						//获取验证码
						LoginHelper.getVertifyNum(mActivity,et_phone.getText().toString(),mGetVertifyBtn);				
					}
				}
			});
			builder.setView(layout);
			builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					mModifyPwdDialog.dismiss();
					if( UtilHelper.checkPhoneValid(et_phone.getText().toString(),mActivity) == true && 
							checkVertifyValid(et_vertify.getText().toString()) == true &&
							UtilHelper.checkPwdValid(mActivity, et_newpwd.getText().toString()) == true){
						String newpwdStr = et_newpwd.getText().toString();
						String confirmnewpwdStr = et_confirmnewpwd.getText().toString();
						if( newpwdStr == null || newpwdStr.isEmpty() || confirmnewpwdStr == null || confirmnewpwdStr.isEmpty() ){
							Toast.makeText(mActivity, R.string.newpwd_cannot_be_empty, Toast.LENGTH_SHORT).show();
							return;
						}
						if( newpwdStr.equals(confirmnewpwdStr) == false ){
							Toast.makeText(mActivity, R.string.newpwd_is_not_equal, Toast.LENGTH_SHORT).show();
							return;
						}
						if( needBindPhone == true ){
							sendBindPhoneRequest(et_phone.getText().toString(),et_newpwd.getText().toString(),et_vertify.getText().toString());
						}else{
							sendResetPwdRequest(et_phone.getText().toString(),et_newpwd.getText().toString(),et_vertify.getText().toString(),isParent);
						}
						return;					
					}

	    	  	}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					mModifyPwdDialog.dismiss();
	    	  	}
			});
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					setModifyDialogShowingSate(true);
					mModifyPwdDialog = null;
					Log.d(TAG,"mModifyPwdDialog == NULL");
				}
			});
			mModifyPwdDialog = builder.create();
			Log.d(TAG,"showModifyPwdDialog create end");
		}
		Log.d(TAG,"showModifyPwdDialog show end");
		mModifyPwdDialog.show();
	}
}
