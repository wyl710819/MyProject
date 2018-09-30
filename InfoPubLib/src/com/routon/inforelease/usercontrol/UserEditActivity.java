package com.routon.inforelease.usercontrol;

import java.lang.reflect.Field;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.R.id;
import com.routon.inforelease.R.layout;
import com.routon.inforelease.R.string;
import com.routon.inforelease.R.style;
import com.routon.inforelease.json.AuthenBean;
import com.routon.inforelease.json.AuthenBeanParser;
import com.routon.inforelease.json.UserListdatasBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.GroupSelectActivity;
import com.routon.inforelease.widget.GroupSelActivity;
import com.routon.inforelease.widget.SettingItem;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;
import com.routon.utils.UtilHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class UserEditActivity extends CustomTitleActivity {
	private UserListdatasBean mBean = null;
//	private String TAG = "useredit";
	private static int MSG_CREATE_USER = 0;
	private static int MSG_MODIFY_USER = 3;
	
	
	private void confirm(){
		if( true == checkUserNameValid() && true == checkRealNameValid()
				&& true == checkPhoneValid() && true == checkGroupIdsValid() ){
			showProgressDialog();
			if( mGroupIds != null && mGroupIds.endsWith(",")){
				mGroupIds = mGroupIds.substring(0, mGroupIds.length()-1);
			}
			if( mBean == null ){//新增用户机器
				String url = UrlUtils.getUserSaveUrl(((EditText) this.findViewById(R.id.et_username)).getText().toString(),
						((EditText) this.findViewById(R.id.et_realname)).getText().toString(), 
						((EditText) this.findViewById(R.id.et_email)).getText().toString(), 
						((EditText) this.findViewById(R.id.et_phone)).getText().toString(), 
						((EditText) this.findViewById(R.id.et_address)).getText().toString(), 
						mGroupIds);
				HttpClientDownloader.getInstance().getResultFromUrlWithSession(url,handler,MSG_CREATE_USER);
			}else{
				String saveUrl = UrlUtils.getUserSaveUrl(mBean.userid,((EditText) this.findViewById(R.id.et_username)).getText().toString(),
						((EditText) this.findViewById(R.id.et_realname)).getText().toString(), 
						((EditText) this.findViewById(R.id.et_email)).getText().toString(), 
						((EditText) this.findViewById(R.id.et_phone)).getText().toString(), 
						((EditText) this.findViewById(R.id.et_address)).getText().toString(),mGroupIds);
				HttpClientDownloader.getInstance().getResultFromUrlWithSession(saveUrl,handler,MSG_MODIFY_USER);
			}
		}
	}
	
	private boolean checkGroupIdsValid(){
		if( mGroupIds == null || mGroupIds.trim().length() == 0 ){
			Toast.makeText(this, R.string.sel_empty_group, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	private boolean checkUserNameValid(){
		EditText et_username = ((EditText) this.findViewById(R.id.et_username));
		String name = et_username.getText().toString();
		if( name == null || name.trim().length() == 0 ){
			Toast.makeText(this, R.string.name_is_not_empty, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	private boolean checkRealNameValid(){
		EditText et_realname = ((EditText) this.findViewById(R.id.et_realname));
		String name = et_realname.getText().toString();
		if( name == null || name.trim().length() == 0 ){
			Toast.makeText(this, R.string.realname_is_not_empty, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	private boolean checkPhoneValid(){
		EditText et_phone = ((EditText) this.findViewById(R.id.et_phone));
		return UtilHelper.checkPhoneValid(et_phone.getText().toString(),this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_useredit);
		
		RelativeLayout groupLayout = (RelativeLayout)(findViewById(R.id.rl_group));
		groupLayout.setClickable(true);
		groupLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("select_param", mGroupIds);
				intent.setClass(UserEditActivity.this, GroupSelActivity.class);
				UserEditActivity.this.startActivityForResult(intent,0);
			}
		});
		
		((TextView) this.findViewById(R.id.tv_username)).setText(Html.fromHtml(getResources().getString(R.string.username_must)));
		((TextView) this.findViewById(R.id.tv_realname)).setText(Html.fromHtml(getResources().getString(R.string.realname_must)));
		//手机号改成必填
		((TextView) this.findViewById(R.id.tv_phone)).setText(Html.fromHtml(getResources().getString(R.string.phone_must)));
		((TextView) this.findViewById(R.id.tv_group)).setText(Html.fromHtml(getResources().getString(R.string.group_must)));
		
		//用户名不能修改
		EditText et_username = ((EditText) this.findViewById(R.id.et_username));
		et_username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if( hasFocus == false ){//失去焦点时
					checkUserNameValid();
				}
			}
		});
		
		EditText et_realname = ((EditText) this.findViewById(R.id.et_realname));
		et_realname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if( hasFocus == false ){//失去焦点时
					checkRealNameValid();
				}
			}
		});
		
		EditText et_phone = ((EditText) this.findViewById(R.id.et_phone));
		et_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if( hasFocus == false ){//失去焦点时
					checkPhoneValid();
				}
			}
		});
		
		mBean = (UserListdatasBean) (getIntent().getSerializableExtra("detail"));
		if( mBean == null ){
			this.initTitleBar(R.string.user_create);
		}else{
			this.initTitleBar(R.string.user_edit);
			
			et_username.setText(mBean.username);	
			et_realname.setText(mBean.realname);	
			et_phone.setText(mBean.phonenum);
			
			EditText et_address = ((EditText) this.findViewById(R.id.et_address));
			et_address.setText(mBean.address);
			
			EditText et_email = ((EditText) this.findViewById(R.id.et_email));
			et_email.setText(mBean.email);
			
			mGroupIds = mBean.groupids;
			
			//用户名不能修改
			et_username.setEnabled(false);
		}
		
		this.setTitleNextBtnClickListener(this.getString(R.string.menu_save), new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				confirm();
			}
		});	
		
		
	}
	
//	private void updateGroupNum(){
//		SettingItem groupItem = (SettingItem)(findViewById(R.id.item_group));
//		groupItem.setInfo(mGroupNum+"组");
//	}
	
	private String mGroupIds = null;
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		   case RESULT_OK:
		    Bundle b=data.getExtras(); //data为B中回传的Intent
		    mGroupIds = b.getString("groupids");
//		    mGroupNum = b.getInt("groupnum");
//		    updateGroupNum();
		    break;
		default:
		    break;
		    }
		}
	
	private AlertDialog mResetPwdDialog;		
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		hideProgressDialog();
		if( handler != null ){
			handler.removeCallbacksAndMessages(null);
		}
	}

	private void sendModifyUserBroadcast(AuthenBean bean){
		Intent intent = new Intent("modifyUser");  		
	  	UserListdatasBean userbean = new UserListdatasBean();
	  	userbean.userid = bean.obj.userId;
	  	userbean.username = bean.obj.userName;
	  	userbean.realname = bean.obj.realName;
	  	userbean.email = bean.obj.email;
	  	userbean.address = bean.obj.address;
	  	userbean.phonenum = bean.obj.phoneNum;
	  	userbean.groupids = mGroupIds; 		
	  	intent.putExtra("user",userbean);
		UserEditActivity.this.sendBroadcast(intent);
	}
	
	private void sendAddUserBroadcast(AuthenBean bean){
		Intent intent = new Intent("addUser");		
  		UserListdatasBean userbean = new UserListdatasBean();
//  		Log.d(TAG,"sendAddUserBroadcast userid:"+bean.obj.userId);
  		userbean.userid = bean.obj.userId;
  		userbean.username = bean.obj.userName;
  		userbean.realname = bean.obj.realName;
  		userbean.email = bean.obj.email;
  		userbean.address = bean.obj.address;
  		userbean.phonenum = bean.obj.phoneNum;	
  		userbean.groupids = mGroupIds;
//  		Log.d(TAG,"send add user broadcast mGroupIds:"+mGroupIds);
  		intent.putExtra("user",userbean);
		UserEditActivity.this.sendBroadcast(intent);
	}
	
	Handler handler = new Handler(){
		 @Override
       public void handleMessage(Message msg) {			 
			hideProgressDialog();
			
			 if( msg.arg2 != 0 ){//获取网络数据过程中出错
//      	  		Log.d(TAG,"loginin error");
      	  		boolean connected = HttpClientDownloader.isNetworkConnected(UserEditActivity.this);
      	  		if( connected == false ){
      	  			Toast.makeText(UserEditActivity.this, R.string.network_failed, Toast.LENGTH_LONG).show();
      	  		}else{
      	  			Toast.makeText(UserEditActivity.this, R.string.get_netdata_failed, Toast.LENGTH_LONG).show();
      	  		}
			 }else{
	               if( msg.arg1 == 1 ){//重置密码
						if( msg.obj != null  && msg.obj instanceof String ){
	              	  		BaseBean bean = BaseBeanParser.parseBaseBean(msg.obj.toString());
	              	  		if( bean == null ){
	              	  			Toast.makeText(UserEditActivity.this, R.string.modify_data_failed, Toast.LENGTH_LONG).show();
	              	  		}else if( bean.code == 0 ){//重置密码成功
	              	  			hideResetPwdDialog();
	              	  			Toast.makeText(UserEditActivity.this, R.string.modify_data_success, Toast.LENGTH_LONG).show();
	              	  		}else if( bean.code == -2 ){
	              	  			InfoReleaseApplication.returnToLogin(UserEditActivity.this,false);
	              	  			return;
	              	  		}else{//修改用户数据失败信息提示
	              	  			Toast.makeText(UserEditActivity.this, bean.msg, Toast.LENGTH_LONG).show();
	              	  		}
	              	  	} 
				 }else if( msg.arg1 == MSG_CREATE_USER ){	//新建用户				 
					 if( msg.obj != null  && msg.obj instanceof String ){
						 AuthenBean bean = AuthenBeanParser.parseAuthenBean(msg.obj.toString());
						 if( bean == null ){
	              	  			Toast.makeText(UserEditActivity.this, R.string.user_create_failed, Toast.LENGTH_LONG).show();
	              	  		}else if( bean.code == 0 ){//新增用户成功
	              	  			finish();
//	              	  			Log.d(TAG,"create user msg:"+msg.obj.toString());
	              	  			sendAddUserBroadcast(bean);
	              	  			Toast.makeText(UserEditActivity.this, R.string.user_create_success, Toast.LENGTH_LONG).show();
	              	  		}else if( bean.code == -2 ){//失效
	              	  			InfoReleaseApplication.returnToLogin(UserEditActivity.this);
	              	  			return;
	              	  		}else{//失败信息提示
	              	  			Toast.makeText(UserEditActivity.this, bean.msg, Toast.LENGTH_LONG).show();
	              	  		}
					 }
				 }else if( msg.arg1 == MSG_MODIFY_USER ){//修改用户信息
					
					 if( msg.obj != null  && msg.obj instanceof String ){
						 AuthenBean bean = AuthenBeanParser.parseAuthenBean(msg.obj.toString());
						 if( bean == null ){
	              	  			Toast.makeText(UserEditActivity.this, R.string.modify_data_failed, Toast.LENGTH_LONG).show();
	              	  		}else if( bean.code == 0 ){//修改用户成功	              	  		
	              	  			finish();
	              	  			sendModifyUserBroadcast(bean);              	  		
	              	  			Toast.makeText(UserEditActivity.this, R.string.modify_data_success, Toast.LENGTH_LONG).show();
	              	  		}else if( bean.code == -2 ){//失效
	              	  			InfoReleaseApplication.returnToLogin(UserEditActivity.this);
	              	  			return;
	              	  		}else{//失败信息提示
	              	  			Toast.makeText(UserEditActivity.this, bean.msg, Toast.LENGTH_LONG).show();
	              	  		}
					 }
				 }
	        }
		 }
	};
	
	private void hideResetPwdDialog(){
		if( mResetPwdDialog == null ) return;
		try 
		{
			Field field = mResetPwdDialog.getClass()
			.getSuperclass().getDeclaredField(
			"mShowing" );
			field.setAccessible( true );
			// 将mShowing变量设为false，表示对话框已关闭 
			field.set(mResetPwdDialog, true );
			mResetPwdDialog.dismiss();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mResetPwdDialog.dismiss();
		mResetPwdDialog = null;
	}
	
	private void showResetPwdDialog(){
		AlertDialog.Builder builder =new AlertDialog.Builder(this);
		builder.setTitle(R.string.reset_pwd);
		builder.setMessage(R.string.reset_pwd_msg);
		builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				try 
				{
					Field field = mResetPwdDialog.getClass()
					.getSuperclass().getDeclaredField(
					"mShowing" );
					field.setAccessible( true );
					// 将mShowing变量设为false，表示对话框已关闭 
					field.set(mResetPwdDialog, false );
					mResetPwdDialog.dismiss();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				showProgressDialog();
				String url = UrlUtils.getUserResetKeyUrl(mBean.userid, mBean.username);
				HttpClientDownloader.getInstance().getResultFromUrlWithSession(url,handler,1);
    	  	}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
    	  	}
		});
		mResetPwdDialog = builder.create();
		mResetPwdDialog.show();

	}

}
