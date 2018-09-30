package com.routon.smartcampus;

import java.io.File;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.ModifyPwdDialogHelper;
import com.routon.inforelease.json.AuthenBean;
import com.routon.inforelease.json.AuthenBeanParser;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.util.PermissionUtils;
import com.routon.remotecontrol.BluetoothChatService;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.guide.GuideActivity;
import com.routon.smartcampus.guide.GuideHelper;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.user.PhoneAndVertifyActivity;
import com.routon.smartcampus.user.UserInfoData;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.update.UpdateManager;
import com.routon.widgets.Toast;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 登陆界面
 * @author xiaolp
 * 
 */
public class MyLoginActivity extends BaseActivity {
	
	private static String TAG = "MyLoginActivity";
	private ModifyPwdDialogHelper mModifyPwdDialogHelper = null;
	
	 @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if( mReceiver != null ){
			this.unregisterReceiver(mReceiver);
		}
		hideProgressDialog();
		BluetoothChatService.getInstance().stop();
	}
	
	public void startMainActivity(){	
		mUserInfoData.resetUserRole();
		int role = GuideHelper.TEACHER_ROLE;
		if( this.mStudentDataList.size() > 0 ){
			SmartCampusApplication.mStudentDatas = mStudentDataList;
			role = GuideHelper.PARENT_ROLE;
		}
		
		String[] validImages = GuideHelper.getValidMainImages(this, GuideHelper.getImagesFromAssetFile(this),role);
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(this.getPackageName(),"com.routon.smartcampus.MainActivity"));
		if(validImages == null || validImages.length == 0 ){//不需要引导页
			startActivity(intent);
		}else{//需要引导页
			Intent guideIntent = new Intent();
			guideIntent.setClass(this, GuideActivity.class);
			guideIntent.putExtra(GuideActivity.INTENT_URI_TAG, intent.toURI());
			guideIntent.putExtra(GuideActivity.IMAGES_ARRAY_TAG, validImages);
			startActivity(guideIntent);
		}	
		this.finish();
		
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		Log.d(TAG,"onResume");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		Log.d(TAG, "onResumeXGPushClickedResult:" + click);
		if (click != null) { // 判断是否来自信鸽的打开方式
//			Toast.makeText(this, "通知被点击:" + click.toString(),
//					Toast.LENGTH_SHORT).show();
			Log.i(TAG, "----------" + click.toString());
			
			if(SmartCampusApplication.mFamilyVersion == true){
				startMainActivity();
			}
		}
	}
	
	
	UserInfoData mUserInfoData = null;
	private void loadAccount(){
		//读取存储的帐号和密码	
		String name = mUserInfoData.getTeacherName();
		String pwd = mUserInfoData.getTeacherPwd();
		String portrait = mUserInfoData.getTeacherPortrait();
		String parentPortrait = mUserInfoData.getParentProtrait();

		if( mIsParentRule == true ){
			 name = mUserInfoData.getParentPhone();
			 pwd = mUserInfoData.getParentVerifyNum();
			 mUserEdit.setHint("学籍号/手机号");
			 mPwdEdit.setHint("密码");
			 
		}else{
			mUserEdit.setHint("用户名/手机号");
			mPwdEdit.setHint("密码");
		}
		mUserEdit.setText(name);
		mPwdEdit.setText(pwd);
		
		if( portrait != null ){
			setTeacherProfileImage(portrait.trim());
		}
		
		if( parentPortrait != null ){
			setParentProfileImage(parentPortrait.trim(),null);
		}
		
	}
	
	private boolean mIsParentRule = false;
	
	private ImageView mTeacherProfileIv = null;
	private ImageView mParentProfileIv = null;
	private View mSelParentProfileIv = null;
	private View mSelTeacherProfileIv = null;
	private TextView mForgetPwdTv = null;
	private TextView mGetVerifyCodeTv = null;
	private EditText mUserEdit = null;
	private EditText mPwdEdit = null;
	private Button mLoginBtn = null;
	
	private void changeRule(){
		if( mIsParentRule == true ){
			mSelTeacherProfileIv.setVisibility(View.GONE);
			mSelParentProfileIv.setVisibility(View.VISIBLE);
			mForgetPwdTv.setVisibility(View.VISIBLE);
			mGetVerifyCodeTv.setVisibility(View.GONE);
		}else{
			mSelTeacherProfileIv.setVisibility(View.VISIBLE);
			mSelParentProfileIv.setVisibility(View.GONE);
			mForgetPwdTv.setVisibility(View.VISIBLE);
			mGetVerifyCodeTv.setVisibility(View.GONE);
		}
		loadAccount();
	}
	
	private void initView(){
		int role = mUserInfoData.getUserRole();
		mIsParentRule = mUserInfoData.getIsParentRule();
		if( role == UserInfoData.UNKOWN_USER_ROLE || role == UserInfoData.ALL_USER_ROLE ){//未选择角色或者选择了两个角色
			findViewById(R.id.student_role_ll).setVisibility(View.VISIBLE);
			findViewById(R.id.teacher_role_ll).setVisibility(View.VISIBLE);
		}else if( role == UserInfoData.TEACHER_USER_ROLE ){
			findViewById(R.id.student_role_ll).setVisibility(View.GONE);
			findViewById(R.id.teacher_role_ll).setVisibility(View.VISIBLE);
			mIsParentRule = false;
		}else if( role == UserInfoData.PARNT_USER_ROLE ){
			findViewById(R.id.student_role_ll).setVisibility(View.VISIBLE);
			findViewById(R.id.teacher_role_ll).setVisibility(View.GONE);
			mIsParentRule = true;
		}
		
		
		mTeacherProfileIv = (ImageView) findViewById(R.id.teacher_profilephoto_iv);
		mParentProfileIv = (ImageView) findViewById(R.id.parent_profilephoto_iv);
		mSelParentProfileIv = findViewById(R.id.sel_parent_profilephoto_iv);
		mSelTeacherProfileIv = findViewById(R.id.sel_teacher_profilephoto_iv);
		mForgetPwdTv = (TextView) findViewById(R.id.forget_passwd_text);
		mGetVerifyCodeTv = (TextView) findViewById(R.id.get_verify_code_tv);
		
//		TextView switchPlatformTv = (TextView) findViewById(R.id.switch_platform);
//		switchPlatformTv.setVisibility(View.VISIBLE);
//		switchPlatformTv.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
////				UrlUtils.server_address = "edu.wanlogin.com:8086/edu";
//				if(UrlUtils.server_address.equals("172.16.41.191/ad") ){
//					UrlUtils.server_address = "edu.wanlogin.com:8086/edu";
//					reportToast("切换到edu.wanlogin.com:8086/edu平台");
//				}else{
//					UrlUtils.server_address = "172.16.41.191/ad";
//					reportToast("切换到172.16.41.191/ad平台");
//				}
//			}
//		});
		
		
		mUserEdit = (EditText)(findViewById(R.id.username_edit));
		mPwdEdit = (EditText)(findViewById(R.id.password_edit));
		mModifyPwdDialogHelper = new ModifyPwdDialogHelper(this);
		
		mLoginBtn = (Button)this.findViewById(R.id.signin_button);
		
		changeRule();
		
		mTeacherProfileIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mIsParentRule = false;
				changeRule();
			}
		});
		mParentProfileIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mIsParentRule = true;
				changeRule();
			}
		});	
		
		mLoginBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				loginClicked();
			}
			
		});
		
		mForgetPwdTv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mModifyPwdDialogHelper.showModifyPwdDialog(null,true,mIsParentRule,new ModifyPwdDialogHelper.DialogAnswerInterface() {		
					@Override
					public void ok_callback() {
						// TODO Auto-generated method stub
						//修改密码成功，重新登陆
						loadAccount();
					}
				});
			}
		});
		
		mGetVerifyCodeTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MyLoginActivity.this, PhoneAndVertifyActivity.class);
				intent.putExtra(MyBundleName.NEXT_BTN_TEXT, "确　定");
				MyLoginActivity.this.startActivity(intent);
			}
		});
		
		//自动登陆按钮选中
//		if( mAutoCb.isChecked() == true ){//自动登录，升级未完成
//			loginClicked();
//			UpdateManager.mUpdateFlag = false;
//		}else{
			//检测软件更新
		    UpdateManager manager = new UpdateManager(this);
		    manager.checkUpdate();
//		}
	}
	
	private ArrayList<StudentBean> mStudentDataList = new ArrayList<StudentBean>();
	
	//家长登录
	public void parentLoginOn(final String account, final String pwd, final boolean parentFirstLogin){
		showProgressDialog();
		String loginPwd = "";
		if (parentFirstLogin) {//家长帐号第一次登录，有保存记录，兼容老版本，将密码修改为验证码，修改后，将parentFirstLogin修改为false
			loginPwd = "111111";
		}else {//家长帐号本地保存验证码已修改
			//家长帐号第一次登录，无保存数据
			loginPwd = pwd;
		}
		String url = SmartCampusUrlUtils.getParentLoginUrl(account,loginPwd);
		Log.d(TAG, "url="+url+" parentFirstLogin="+parentFirstLogin);

		HttpClientDownloader.getInstance().loginIn(this,url,new HttpClientDownloader.NetworkListener() {
			
			@Override
			public void onResponse(String result, boolean success) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				if( success == false ){
	    	  		//网络连接出错
	    	  		if( InfoReleaseApplication.showNetWorkFailed(MyLoginActivity.this) == false ){
	    	  			return;
	    	  		}
				}
				if( result == null ){
					reportToast("登录失败");
					return;
				}
				try {
					JSONObject response = new JSONObject(result);
					int code = response.optInt("code");
					if(code == 0){
						JSONObject obj = response.optJSONObject("obj");
						JSONArray children = obj.optJSONArray("childrens");
						int userId = obj.optInt("userId");
						String userName = obj.optString("userName");
						//修改家长密码
						if(parentFirstLogin){
							resetParentPwd(String.valueOf(userId), userName, pwd);
						}
						mStudentDataList.clear();
						String parentphone = null;
						if( children != null ){
							int len = children.length();						
							for (int i = 0; i < len; i++) {
								JSONObject child = (JSONObject) children.opt(i);
								if( child != null ){
									StudentBean bean = new StudentBean(child);
									parentphone = String.valueOf(bean.parentPhone);
									if( bean != null ){
										mStudentDataList.add(bean);
									}
								}
							}									
						}
						if( mStudentDataList.size() == 0 ){
							reportToast("无法获取到正确的学生列表");
							return;
						}
						//学生家长手机号不为空
						if( parentphone != null && parentphone.isEmpty() == false && parentphone.equals("null") == false
								&& parentphone.equals("NULL") == false && parentphone.equals("0") == false  ){
							//登录帐号不是手机号,是学生学籍号
							if( account.equals(parentphone) == false ){
								getStudentListData(parentphone);
							}else{//登录帐号是手机号
								startMain(parentphone,response.optString("msg"),true);
							}
						}else{//家长手机号为空
							//绑定家长手机号码
							bindParentPhoneForStudent(String.valueOf(mStudentDataList.get(0).sid));
//							startMain(account,response.optString("msg"),false);
						}	
						return;
					}else {
						String msg = response.optString("msg");
						Toast.makeText(MyLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
						return;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				reportToast("登录失败");
			}
		});
	}
	
	private void bindParentPhoneForStudent(String sid){
		mModifyPwdDialogHelper.showUpdatePhoneDialog(new ModifyPwdDialogHelper.DialogAnswerInterface() {
			
			@Override
			public void ok_callback() {
				// TODO Auto-generated method stub
				getStudentListData(mModifyPwdDialogHelper.getPhone());
			}
		},true,sid,"绑定手机号码");
	}
	
	private void getStudentListData(final String parentphone) {// 获取指定班级学生列表		
		String urlString = SmartCampusUrlUtils.getStudentListCmdUrl(null,parentphone);
		showProgressDialog();

		Log.d(TAG, "getStudentListData urlString:"+urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						hideProgressDialog();
						int code = response.optInt("code",-1);
						Log.d(TAG,"getStudentListData response:"+response);
						if ( code == 0) {				
							JSONArray array = response.optJSONArray("datas");
							if( array != null && array.length() > 0 ){
								mStudentDataList.clear();
								int len = array.length();						
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.opt(i);
									if( obj != null ){
										StudentBean bean = new StudentBean(obj);
										if( bean != null ){
											mStudentDataList.add(bean);			
										}
										
									}
								}							
							}
							startMain(parentphone,response.optString("msg"),true);
							
						} else {
							String msg = response.optString("msg");
							Toast.makeText(MyLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {			
						//先判断网络状况
						InfoReleaseApplication.showNetWorkFailed(MyLoginActivity.this);
						Toast.makeText(MyLoginActivity.this, "获取登录信息失败，请检查网络状况", Toast.LENGTH_LONG).show();
						hideProgressDialog();
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	public void startMain(String account,String msg,boolean accountIsPhone){
		if( mStudentDataList.size() > 0 ){
			SmartCampusApplication.mStudentDatas = mStudentDataList;
			SmartCampusApplication.mFamilyVersion = true;
			int selIndex = SmartCampusApplication.getSelIndex(mUserInfoData);
			StudentBean bean = SmartCampusApplication.mStudentDatas.get(selIndex);
			saveNameAndPwdAndPortrait(String.valueOf(bean.sid), bean.imgUrl,bean.imageLastUpdateTime);
			if( accountIsPhone == true ){
				updateParentLoginTime(account);
			}
			saveAccount();
			startMainActivity();
		}else{
			if( msg.isEmpty() == false ){
				Toast.makeText(MyLoginActivity.this, msg, Toast.LENGTH_LONG).show();	
			}
			Toast.makeText(MyLoginActivity.this, "此帐号未绑定学生信息，请重新注册", Toast.LENGTH_LONG).show();
		}
	}
	
	//家长更换密码
	public void resetParentPwd(String userId, String userName, String pwd){
		String url = SmartCampusUrlUtils.getParentChangePwdUrl(userId, userName, "111111", pwd, pwd);
		Log.d(TAG, url);
		showProgressDialog();
		CookieJsonRequest request = new CookieJsonRequest(Method.GET, url, null, 
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						hideProgressDialog();
						Log.d(TAG, response.toString());
						int code = response.optInt("code");
						if(code == 0){
							mUserInfoData.setParentFirstLogin(false);
						}else {
							String msg = response.optString("msg");
							Toast.makeText(MyLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
						}
					}
				}, 
				new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						InfoReleaseApplication.showNetWorkFailed(MyLoginActivity.this);
						Toast.makeText(MyLoginActivity.this, "获取登录信息失败，请检查网络状况", Toast.LENGTH_LONG).show();
						hideProgressDialog();
					}
				});
		request.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(request);
	}
	
	private void parentLogin(final String phone,String vertifyNum){
		String remName = mUserInfoData.getParentPhone();
		String remPwd= mUserInfoData.getParentVerifyNum();
		boolean parentFirstLogin = mUserInfoData.getParentFirstLogin();
		if( remName == null || remName.isEmpty() || remPwd == null || remPwd.isEmpty() ){
			Log.d(TAG, "没有注册数据");
			parentFirstLogin = false;
		}	
		String name = mUserEdit.getText().toString().trim();
		String pwd = mPwdEdit.getText().toString().trim();
		if(!TextUtils.isEmpty(remName)&&!TextUtils.isEmpty(name)&&!name.equals(remName)){
			parentFirstLogin = false;
		}
		if(!TextUtils.isEmpty(remPwd)&&!TextUtils.isEmpty(pwd)&&!pwd.equals(remPwd)){
			parentFirstLogin = false;
		}
		parentLoginOn(name, pwd, parentFirstLogin);
	}
	
	private void updateParentLoginTime(String phone){
		String urlString = SmartCampusUrlUtils.getCmdUpdateLoginTimeUrl(phone);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						Log.d(TAG,"updateParentLoginTime response:"+response);		
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {			
						
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void saveNameAndPwdAndPortrait(String portrait,String portraitUrl,String imageLastUpdateTime){
		String name = mUserEdit.getText().toString().trim();
		String pwd = mPwdEdit.getText().toString().trim();
		//保存用户名和密码
		if( mIsParentRule == true ){
			mUserInfoData.setParentPhone(name);
			mUserInfoData.setParentVerifyNum(pwd);
			mUserInfoData.setParentPortrait(portrait);
		}else{
			mUserInfoData.setTeacherName(name);
			mUserInfoData.setTeacherPwd(pwd);
			mUserInfoData.setTeacherPortrait(portrait);			
		}
		mUserInfoData.setIsParentRule(mIsParentRule);
	    ImageUtils.downloadAndSaveProfilePhoto(MyLoginActivity.this.getApplicationContext(), portraitUrl, portrait,imageLastUpdateTime);
	}
	
	private void saveAccount(){
		String name = mUserEdit.getText().toString().trim();
		String pwd = mPwdEdit.getText().toString().trim();
		//保存用户名和密码
		if( mIsParentRule == true ){
			mUserInfoData.setParentPhone(name);
			mUserInfoData.setParentVerifyNum(pwd);
			mUserInfoData.setParentFirstLogin(false);
		}else{
			mUserInfoData.setTeacherName(name);
			mUserInfoData.setTeacherPwd(pwd);
		}
		mUserInfoData.setIsParentRule(mIsParentRule);
	}
	
	private void loginClicked(){
		Log.d(TAG,"loginClicked mIsParentRule:"+mIsParentRule);
		final String name = ((TextView)(MyLoginActivity.this.findViewById(R.id.username_edit))).getText().toString().trim();
		final String pwd = ((TextView)(MyLoginActivity.this.findViewById(R.id.password_edit))).getText().toString().trim();
		if( name == null || name.isEmpty() ){
			Toast.makeText(MyLoginActivity.this, MyLoginActivity.this.getResources().getString(R.string.name_is_not_empty), 
					Toast.LENGTH_SHORT).show();
			return;
		}
		if( pwd == null || pwd.isEmpty() ){
			Toast.makeText(MyLoginActivity.this, MyLoginActivity.this.getResources().getString(R.string.password_is_not_empty), 
					Toast.LENGTH_SHORT).show();
			return;
		}		
		if(mIsParentRule == false && pwd.equals("111111")){//提醒用户修改原始密码
			showNeedModifyPwdDialog(name,pwd);
			return;
		}
		Log.d(TAG,"loginClicked mIsParentRule:"+mIsParentRule);
		if( mIsParentRule == false ){
			teacherLogin(name,pwd);
		}else{
			parentLogin(name,pwd);
		}
	}
	
	private void showNeedModifyPwdDialog(final String name,final String  pwd){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = getLayoutInflater().inflate(R.layout.dialog_login_pwd_tip, null);
		builder.create();
		builder.setView(view);
		final Dialog dialog = builder.show();
		TextView txtChange = (TextView)view.findViewById(R.id.txt_dialog_login_pwd_tip_change);
		TextView txtContinue = (TextView)view.findViewById(R.id.txt_dialog_login_pwd_tip_continue);
		txtChange.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				//修改密码成功后，重新登录
				mModifyPwdDialogHelper.showModifyPwdDialog(null,true,mIsParentRule,new ModifyPwdDialogHelper.DialogAnswerInterface() {		
					@Override
					public void ok_callback() {
						// TODO Auto-generated method stub
						//修改密码成功
						startMainActivity();
					}
				});
			}
		});
		txtContinue.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				teacherLogin(name,pwd);
			}
		});
	}
	
	private void teacherLoginProc(String result){
		Log.d(TAG,"teacherLoginProc:"+result);
  		final AuthenBean bean = AuthenBeanParser.parseAuthenBean(result.toString());
  		if( bean != null ){
  			InfoReleaseApplication.authenobjData = bean.obj;
  			if( bean.code == 0 ){//登陆成功
	  			 //phone num is null
	  			 Log.d("loginActivity"," bean.obj.phoneNum:"+ bean.obj.phoneNum
	  				+",size:"+bean.obj.phoneNum.length());
	  			 saveNameAndPwdAndPortrait(bean.obj.portrait,bean.obj.portraitUrl,null);
	  			 SmartCampusApplication.mFamilyVersion = false;
	   	  		 if( bean.obj == null || bean.obj.phoneNum == null || bean.obj.phoneNum.trim().length() == 0 
	   	  				|| bean.obj.phoneNum.equals("null") || bean.obj.phoneNum.equals("NULL") ){//未绑定手机号，提示用户绑定手机号
	   	  			 //force user to bind phone number
	   	  			 mModifyPwdDialogHelper.showModifyPwdDialog(null,true,true,false,new ModifyPwdDialogHelper.DialogAnswerInterface() {		
		   				@Override
		   				public void ok_callback() {
		   					// TODO Auto-generated method stub
		   					//修改密码成功
		   					startMainActivity();
		   				}
	   	  			 });
	   			 }else{
	   				//存储portrait	   			
	       	         startMainActivity();
	   			 }
	  			 return;
	  		 }else{
	  			 Toast.makeText(MyLoginActivity.this, bean.msg, Toast.LENGTH_LONG).show();
	  			 return;
	  		 }
  		}   
	}
	
	private void teacherLogin(final String name,final String pwd){
		Log.d("loginActivity","sendLogininRequest");
		//显示ProgressDialog   
		showProgressDialog();
		HttpClientDownloader.getInstance().loginIn(this,UrlUtils.getAuthUrl(name,pwd),new HttpClientDownloader.NetworkListener() {
			
			@Override
			public void onResponse(String result, boolean success) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				if( success == false ){
	    	  		//网络连接出错
	    	  		if( InfoReleaseApplication.showNetWorkFailed(MyLoginActivity.this) == false ){
	    	  			return;
	    	  		}
				}
				if( result != null ){
					teacherLoginProc(result);
				}else{
					reportToast("登录失败");
				}
			}
		});	
	}
	
	BroadcastReceiver mReceiver = null;
	
	//register broadcast
	private void registerBroadcast(){
//		Log.d(TAG,"registerBroadcast");
		IntentFilter intentFilter = new IntentFilter();
		//register new terminal,refresh terminal list
		intentFilter.addAction(InfoReleaseApplication.LOGIN_ACCOUNT_UPDATE);
		if( mReceiver == null ){
			mReceiver = new BroadcastReceiver() {
		        @Override
		        public void onReceive(Context context, Intent intent){
		        	if( intent != null ){
		        		if( intent.getAction() != null || intent.getAction().equals(InfoReleaseApplication.LOGIN_ACCOUNT_UPDATE)){
		        			loadAccount();
		        		}
		        	}
		        }
			};
		}
		registerReceiver(mReceiver, intentFilter);
	}
	
	void setTeacherProfileImage(String name){
		File file = ImageUtils.getProfilePhoto(this,name,null);
		if( file != null ){
			Uri uri = Uri.fromFile(file);
			if( file.exists() == true && uri != null ){
				Log.d("LoginActivity","111 setProfileImageuri:"+uri+",name:"+name);
				mTeacherProfileIv.setImageURI(uri);
			}
		}
	}
	
	void setParentProfileImage(String studentId,String imageLastUpdateTime){
		File file = ImageUtils.getProfilePhoto(this,studentId,imageLastUpdateTime);
		if( file != null ){
			Uri uri = Uri.fromFile(file);
			if( file.exists() == true && uri != null ){
				mParentProfileIv.setImageURI(uri);
			}
		}
	}
	
	//静态变量初始化
	private void resetStaticDatas(){
		SmartCampusApplication.resetStaticDatas();
		GroupListData.resetStatic();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		
		resetStaticDatas();
		
		Log.d(TAG,"onCreate");
		mUserInfoData = new UserInfoData(this);
		InfoReleaseApplication.setLoginComponent(new ComponentName(this.getPackageName(),"com.routon.smartcampus.MyLoginActivity"));
		UrlUtils.app = "smartcampus";
//		UrlUtils.server_address = "edu.wanlogin.com:8086/edu";
		UrlUtils.server_address = "172.16.41.191/ad";
		UpdateManager.mUpdateFlag = true;
		   
		
		UrlUtils.readServerAddress(this);
		
		registerBroadcast();

		initView();
		
		//android6.0以上增加运行时权限判断
		if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){  
		    new PermissionUtils(this).needPermission(200);  
		}  
		
//		FlowerUtil.cleanFlowers(this);
	
	}
	
	@SuppressLint("Override") 
	public void onRequestPermissionsResult(int requestCode,  
	                              String permissions[], int[] grantResults) {  
	   switch (requestCode) {  
	      case 200: {  
	         // If request is cancelled, the result arrays are empty.  
	         if (grantResults.length > 0  
	               && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  
	  
	            // permission was granted, yay! Do the  
	            // contacts-related task you need to do.  
	        //同意给与权限  可以再此处调用拍照  
	            Log.i("用户同意权限", "user granted the permission!");  
	  
	         } else if(grantResults.length > 0 && permissions.length > 0 ){    
	           for( int i = 0; i< permissions.length; i++ ){
	        	   Log.i("用户不同意权限", "user denied the permission!"+permissions[i]);
	        	// permission denied, boo! Disable the  
		            // f用户不同意 可以给一些友好的提示  
	        	    if( grantResults[i] == PackageManager.PERMISSION_DENIED ){
			        	if( permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
			        		Toast.makeText(MyLoginActivity.this, "读写权限申请失败，应用有些功能可能无法正常使用", Toast.LENGTH_SHORT).show();
			         	}else if( permissions[i].equals(Manifest.permission.CAMERA)){
			        		Toast.makeText(MyLoginActivity.this, "拍照权限申请失败，应用有些功能可能无法正常使用", Toast.LENGTH_SHORT).show();
			         	}else if( permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)){
			        		Toast.makeText(MyLoginActivity.this, "定位权限申请失败，应用有些功能可能无法正常使用", Toast.LENGTH_SHORT).show();
			         	}else if( permissions[i].equals(Manifest.permission.RECORD_AUDIO)){
			        		Toast.makeText(MyLoginActivity.this, "麦克风权限申请失败，应用有些功能可能无法正常使用", Toast.LENGTH_SHORT).show();
			         	}
	        	    }
	           }
	         }  
	         return;  
	      }  
	  
	      // other 'case' lines to check for other  
	      // permissions this app might request  
	   }  
	}  
}



