package com.routon.inforelease;

import java.io.File;
import java.lang.reflect.Field;

import com.routon.inforelease.json.AuthenBean;
import com.routon.inforelease.json.AuthenBeanParser;
import com.routon.inforelease.json.AuthenobjBean;
import com.routon.inforelease.json.UserInfo;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.util.ImageUtils;
import com.routon.remotecontrol.BluetoothChatService;
import com.routon.update.UpdateManager;
import com.routon.utils.UtilHelper;
import com.routon.widgets.Toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 登陆界面
 * @author xiaolp
 *
 */
public class LoginActivity extends Activity {
//	private static String TAG = "Login";
	private ModifyPwdDialogHelper mModifyPwdDialogHelper = null;
	private static final int MSG_LOGIN_IN = 0;
	private static final int MSG_MODIFY_PHONE = 1;
	
	Handler handler = new Handler(){
		 @Override
         public void handleMessage(Message msg) {
			 hideProgressDialog();		
			 if( msg.arg2 != 0 ){
//    	  		Log.d(TAG,"loginin error");
    	  		//网络连接出错
    	  		if( InfoReleaseApplication.showNetWorkFailed(LoginActivity.this) == false ){
    	  			return;
    	  		}
			 }else{
                if( msg.arg1 == MSG_LOGIN_IN ){//登陆应答
                	if( msg.obj != null  && msg.obj instanceof String ){
                		Log.d("loginActivity","loginIn:"+msg.obj);
               	  		final AuthenBean bean = AuthenBeanParser.parseAuthenBean(msg.obj.toString());
               	  		if( bean != null ){
                   	  		InfoReleaseApplication.authenobjData = bean.obj;
	               	  		 if( bean.code == 0 ){//登陆成功
	               	  			 //phone num is null
		               	  		if( bean.obj == null || bean.obj.phoneNum == null || bean.obj.phoneNum.trim().length() == 0 ){//未绑定手机号，提示用户绑定手机号
		               	  			//force user to bind phone number
		               	  			showModifyDataDialog();
		               	  			//Toast.makeText(LoginActivity.this, R.string.bind_phone_num, Toast.LENGTH_LONG).show();
		               			}else{
		               				//存储portrait
		               				Editor editor = mSharedPrefrences.edit();//获取编辑器
		               				editor.putString(InfoReleaseApplication.TAG_PORTRAIT,bean.obj.portrait);
		               				editor.commit();
		               			
			               	        ImageUtils.downloadAndSaveProfilePhoto(LoginActivity.this.getApplicationContext(), 
			               	        		bean.obj.portraitUrl, bean.obj.portrait,null);
		               		
		               				loginIn(bean);
		               			}
	               	  			return;
	               	  		 }else{
	               	  			Toast.makeText(LoginActivity.this, bean.msg, Toast.LENGTH_LONG).show();
	               	  			return;
	               	  		 }
	               	    }      	  
                	}
                }else if( msg.arg1 == MSG_MODIFY_PHONE ){//绑定手机号码应答
                	 if( msg.obj != null  && msg.obj instanceof String ){
						 AuthenBean bean = AuthenBeanParser.parseAuthenBean(msg.obj.toString());
						 if( bean == null ){
	              	  			Toast.makeText(LoginActivity.this, R.string.bind_phone_failed, Toast.LENGTH_LONG).show();
              	  		}else if( bean.code == 0 ){//绑定手机号码成功   
              	  			InfoReleaseApplication.authenobjData.phoneNum = mPhone;
              	  			Toast.makeText(LoginActivity.this, R.string.bind_phone_success, Toast.LENGTH_LONG).show();
              	  			hideModifyDataDialog();
              	  			showModifyPwdDialog();
              	  		}else if( bean.code == -2 ){//失效
              	  			return;
              	  		}else{//失败信息提示
              	  			Toast.makeText(LoginActivity.this, bean.msg, Toast.LENGTH_LONG).show(); 	  			
              	  		}
						return;
					 }
                }
			 }
			 //未能处理的全部按照获取网络数据失败处理
			 InfoReleaseApplication.showNetDataFailedTip(LoginActivity.this);
		 }
	};
	
	private void showModifyPwdDialog(){
		AuthenobjBean bean = InfoReleaseApplication.authenobjData;
		mModifyPwdDialogHelper.showModifyPwdDialog(bean.phoneNum,false,false,new ModifyPwdDialogHelper.DialogAnswerInterface() {
			
			@Override
			public void ok_callback() {
				// TODO Auto-generated method stub
				//修改密码成功，重新登陆
				InfoReleaseApplication.returnToLogin(LoginActivity.this,false);
			}
		});
	}
	
	private String mPhone = null;
	
	
	private AlertDialog mModifyDataDialog = null;
	//修改手机号和地址
	private void showModifyDataDialog(){
		AlertDialog.Builder builder =new AlertDialog.Builder(this);
	      
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.dialog_modify_data, null);
		EditText edit = (EditText)(layout.findViewById(R.id.edit));
		edit.setInputType(InputType.TYPE_CLASS_PHONE);
		builder.setView(layout);
		builder.setTitle(R.string.bind_phone_num);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
					try 
					{
						Field field = mModifyDataDialog.getClass()
						.getSuperclass().getDeclaredField(
						"mShowing" );
						field.setAccessible( true );
						// 将mShowing变量设为false，表示对话框已关闭 
						field.set(mModifyDataDialog, false );
						mModifyDataDialog.dismiss();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					EditText edit = (EditText)(layout.findViewById(R.id.edit));
					String data = edit.getText().toString();
					if( data == null || data.isEmpty() ){//输入数据为空
						Toast.makeText(LoginActivity.this, R.string.data_is_null, Toast.LENGTH_LONG).show();				
						return;
					}
					data = data.trim();
					
					if( UtilHelper.isPhone(data) == false ){//手机号码输入错误
						Toast.makeText(LoginActivity.this, R.string.phone_number_format_data, Toast.LENGTH_LONG).show();				
						return;
					}
					showProgressDialog();	
					mPhone = data;
					AuthenobjBean bean = InfoReleaseApplication.authenobjData;
					String url = UrlUtils.getUserSaveUrl(bean.userId, 
							bean.userName, bean.realName, bean.email, data,bean.address,null);						
					HttpClientDownloader.getInstance().getResultFromUrlWithSession(url,handler,MSG_MODIFY_PHONE);
	    	  	}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					hideModifyDataDialog();
	    	  	}
			});
			mModifyDataDialog = builder.create();
			mModifyDataDialog.show();
	}
	
	private void hideModifyDataDialog(){
		if( mModifyDataDialog == null ) return;
		try 
		{
			Field field = mModifyDataDialog.getClass()
			.getSuperclass().getDeclaredField(
			"mShowing" );
			field.setAccessible( true );
			// 将mShowing变量设为false，表示对话框已关闭 
			field.set(mModifyDataDialog, true );
			mModifyDataDialog.dismiss();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mModifyDataDialog.dismiss();
		mModifyDataDialog = null;
	}
	
	 @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if( handler != null ){
			handler.removeCallbacksAndMessages(null);
		}
		if( mReceiver != null ){
			this.unregisterReceiver(mReceiver);
		}
		hideProgressDialog();
		BluetoothChatService.getInstance().stop();
	}
	
	public void loginIn(AuthenBean bean){	
		Intent intent = new Intent();
		intent.setClass(LoginActivity.this, MainActivity.class);
		LoginActivity.this.startActivity(intent);
		this.finish();
	}
	
	private Dialog mWaitDialog = null;
	
	private void hideProgressDialog(){
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	private void showProgressDialog(){
		mWaitDialog = new Dialog(this,R.style.new_circle_progress);    
		mWaitDialog.setContentView(R.layout.dialog_wait);    
		mWaitDialog.show();
	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		Log.d(TAG,"onResume");
	}
	
	private SharedPreferences mSharedPrefrences = null;
	
	private void loadAccount(){
		//读取存储的帐号和密码	
		mSharedPrefrences = getSharedPreferences(InfoReleaseApplication.USERINFO, Context.MODE_PRIVATE); //私有数据
		String name = mSharedPrefrences.getString(InfoReleaseApplication.TAG_NAME,"");
		String pwd = mSharedPrefrences.getString(InfoReleaseApplication.TAG_PWD,"");
		String portrait = mSharedPrefrences.getString(InfoReleaseApplication.TAG_PORTRAIT,null);
		boolean isCheck = mSharedPrefrences.getBoolean(InfoReleaseApplication.TAG_ISCHECK, true);
		boolean isAutoCheck = mSharedPrefrences.getBoolean(InfoReleaseApplication.TAG_AUTOCHECK, false);
//		Log.d(TAG,"load account pwd:"+pwd);
		EditText et_name = (EditText)(findViewById(R.id.username_edit));
		EditText et_pwd = (EditText)(findViewById(R.id.password_edit));
		CheckBox rem_pw = (CheckBox) findViewById(R.id.cb_mima);  
		CheckBox auto_login = (CheckBox) findViewById(R.id.cb_auto);  
		et_name.setText(name);
		if( isCheck == true ){
			et_pwd.setText(pwd);
		}
		rem_pw.setChecked(isCheck);
		auto_login.setChecked(isAutoCheck);
		
		if( portrait != null ){
			setProfileImage(portrait.trim());
		}
	}
	
	private void initView(){
		final EditText et_name = (EditText)(findViewById(R.id.username_edit));
		final EditText et_pwd = (EditText)(findViewById(R.id.password_edit));
		final CheckBox rem_pw = (CheckBox) findViewById(R.id.cb_mima);  
		rem_pw.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				mSharedPrefrences.edit().putBoolean(InfoReleaseApplication.TAG_ISCHECK,rem_pw.isChecked()).commit();
			}
			
		});
		final CheckBox auto_login = (CheckBox) findViewById(R.id.cb_auto);  
		auto_login.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				//add by xiaolp
				//auto login 
				if( auto_login.isChecked() == true ){
					rem_pw.setChecked(true);
					rem_pw.setEnabled(false);
					mSharedPrefrences.edit().putBoolean(InfoReleaseApplication.TAG_ISCHECK,rem_pw.isChecked()).commit();
				}else{
					rem_pw.setEnabled(true);
				}
				mSharedPrefrences.edit().putBoolean(InfoReleaseApplication.TAG_AUTOCHECK,auto_login.isChecked()).commit();
			}
			
		});
		
		findViewById(R.id.password_edit).setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if( hasFocus == true ){//获取焦点
					String newName = et_name.getText().toString();
					//输入用户名未记录
//					String name = null;
//					if( InfoReleaseApplication.authenobjData != null ){
//						name = InfoReleaseApplication.authenobjData.userName;
//					}
					String name = mSharedPrefrences.getString(InfoReleaseApplication.TAG_NAME,"");
					if( newName != null && newName.trim().length() > 0 && name != null && newName.equals(name) == false ){
						et_pwd.setText("");
					}
				}
			}
		});
		
		mModifyPwdDialogHelper = new ModifyPwdDialogHelper(this);

		
		Button loginBtn = (Button)this.findViewById(R.id.signin_button);
		loginBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				loginClicked();
			}
			
		});
		
		TextView forgetView = (TextView)this.findViewById(R.id.forget_passwd_text);
		forgetView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				Log.d(TAG, "forget passwd text clicked");
				//提示用户找回密码
//				Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.find_passwd),
//						Toast.LENGTH_LONG).show();
				mModifyPwdDialogHelper.showModifyPwdDialog(null,true,false,null);
			}
		});
		
		//自动登陆按钮选中
		if( auto_login.isChecked() == true ){
			loginClicked();
			UpdateManager.mUpdateFlag = false;
		}else{
			//检测软件更新
		    UpdateManager manager = new UpdateManager(this);
		    manager.checkUpdate();
		}
	}
	
	private void loginClicked(){
		String name = ((TextView)(LoginActivity.this.findViewById(R.id.username_edit))).getText().toString().trim();
		String pwd = ((TextView)(LoginActivity.this.findViewById(R.id.password_edit))).getText().toString().trim();
		if( name == null || name.isEmpty() ){
			Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.name_is_not_empty), 
					Toast.LENGTH_SHORT).show();
			return;
		}
		if( pwd == null || pwd.isEmpty() ){
			Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.password_is_not_empty), 
					Toast.LENGTH_SHORT).show();
			return;
		}
		CheckBox rem_pw = (CheckBox) findViewById(R.id.cb_mima);  
	
		//保存用户名和密码
		Editor editor = mSharedPrefrences.edit();//获取编辑器
		editor.putString(InfoReleaseApplication.TAG_NAME,name);
		editor.putString(InfoReleaseApplication.TAG_PWD, pwd);
		editor.commit();
		
		HttpClientDownloader.getInstance().loginIn(UrlUtils.getAuthUrl(name,pwd), handler, 0);
		//显示ProgressDialog   
		showProgressDialog();
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
	
	void setProfileImage(String name){
		File file = ImageUtils.getProfilePhoto(this,name,null);
		if( file != null ){
			Uri uri = Uri.fromFile(file);
			Log.d("LoginActivity","setProfileImageuri:"+uri+",name:"+name);
			if( file.exists() == true && uri != null ){
				Log.d("LoginActivity","111 setProfileImageuri:"+uri+",name:"+name);
				ImageView imageview = (ImageView) findViewById(R.id.profilephoto_iv);
				imageview.setImageResource(1);
				imageview.setImageURI(uri);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		
//		InfoReleaseApplication.isEduPlatform = this.getIntent().getBooleanExtra("isEduPlatform", false);
		Log.d("LoginActivity","InfoReleaseApplication.isEduPlatform:"+InfoReleaseApplication.isEduPlatform);
		
		UrlUtils.readServerAddress(this);
		
		loadAccount();
		
		registerBroadcast();
		
		initView();
	}
}
