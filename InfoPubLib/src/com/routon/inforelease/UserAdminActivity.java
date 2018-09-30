package com.routon.inforelease;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.ModifyPwdDialogHelper.DialogAnswerInterface;
import com.routon.inforelease.json.AuthenobjBean;
import com.routon.inforelease.json.ResourceUploadBean;
import com.routon.inforelease.json.ResourceUploadBeanParser;
import com.routon.inforelease.json.ResourceUploadobjBean;
import com.routon.inforelease.net.NetWorkRequest;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.MaterialParams;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.widget.PicSelHelper;
import com.routon.inforelease.widget.SettingItem;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;
import com.routon.utils.UtilHelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.routon.widgets.Toast;

/**
 * 用户管理
 * @author xiaolp
 *
 */
public class UserAdminActivity extends CustomTitleActivity{
	private static final String TAG = "UserAdminActivity";
//	private AlertDialog mModifyPwdDialog = null;
	private ModifyPwdDialogHelper mModifyPwdDialogHelper = null;
	private AlertDialog mModifyDataDialog = null;
	
	private AuthenobjBean mData = null;
	private String mNewPhone;
	private String mNewAddress;
	private String mNewRealName;
	private String mNewPortrait;
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		hideProgressDialog();
		hideModifyDataDialog();
	}
	
	private void showModifyDataDialog(){
		if( mData.phoneNum == null || mData.phoneNum.trim().length() == 0 ){//未绑定手机号，提示用户绑定手机号
			Toast.makeText(this, R.string.bind_phone_num, Toast.LENGTH_LONG).show();
			return;
		}
		mModifyPwdDialogHelper.showModifyPwdDialog(mData.phoneNum,false,false,new ModifyPwdDialogHelper.DialogAnswerInterface() {
			
			@Override
			public void ok_callback() {
				// TODO Auto-generated method stub
				//修改密码成功，重新登陆
				InfoReleaseApplication.returnToLogin(UserAdminActivity.this,false);
			}
		});
	}
	
	private static final int MSG_MODIFY_USER_INFO = 2;
	
	Handler handler = new Handler(){
		 @Override
        public void handleMessage(Message msg) {
			 hideProgressDialog();
			 if( msg.arg2 != 0 ){//获取网络数据过程中出错
//       	  		Log.d(TAG,"loginin error");
       	  		if( InfoReleaseApplication.showNetWorkFailed(UserAdminActivity.this) == true ){
       	  			InfoReleaseApplication.showNetDataFailedTip(UserAdminActivity.this);
       	  		}
       	  		return;
			 }else{
	               if( msg.arg1 == MSG_MODIFY_USER_INFO ){
					if( msg.obj != null  && msg.obj instanceof String ){
	          	  		BaseBean bean = BaseBeanParser.parseBaseBean(msg.obj.toString());
	          	  		if( bean == null ){
	          	  			Toast.makeText(UserAdminActivity.this, R.string.modify_data_failed, Toast.LENGTH_LONG).show();
	          	  		}else if( bean.code == 0 ){//修改用户数据成功
	          	  			hideModifyDataDialog();
		          	  		mData.phoneNum = mNewPhone;
		          	  		mData.address = mNewAddress;
		          	  		mData.realName = mNewRealName;
		          	  		mData.portrait = mNewPortrait;
		          	  		updateBeanData();
		          	  		
		          	  		Editor editor = getSharedPreferences(InfoReleaseApplication.USERINFO, Context.MODE_PRIVATE).edit();//获取编辑器
		          	  		editor.putString(InfoReleaseApplication.TAG_PORTRAIT,mNewPortrait);
		          	  		editor.commit();
	          	  			
	          	  			Toast.makeText(UserAdminActivity.this, R.string.modify_data_success, Toast.LENGTH_LONG).show();
	          	  		}else if( bean.code == -2 ){
	          	  			InfoReleaseApplication.returnToLogin(UserAdminActivity.this);
              	  			return;
              	  		}else{//修改用户数据失败信息提示
	          	  			Toast.makeText(UserAdminActivity.this, bean.msg, Toast.LENGTH_LONG).show();
	          	  		}
	          	  	} 
				 }
	        }
		 }
	};
	
	public final static int TYPE_PHONE = 0;
	public final static int TYPE_ADDRESS = 1;
	public final static int TYPE_REALNAME = 2;
	
	
	//修改手机号和地址
	private void showModifyDataDialog(final int type){
		AlertDialog.Builder builder =new AlertDialog.Builder(this);
	      
		LayoutInflater inflater = getLayoutInflater();
		final View layout = inflater.inflate(R.layout.dialog_modify_data, null);
		EditText edit = (EditText)(layout.findViewById(R.id.edit));
		if( type == TYPE_PHONE ){
			edit.setInputType(InputType.TYPE_CLASS_PHONE);
			edit.setText(mData.phoneNum);
		}else if( type == TYPE_ADDRESS ){
			edit.setText(mData.address);
		}else if( type == TYPE_REALNAME ){
			edit.setText(mData.realName);
		}
		builder.setView(layout);
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
				if( data == null || data.trim().isEmpty() ){//输入数据为空
					Toast.makeText(UserAdminActivity.this, R.string.data_is_null, Toast.LENGTH_LONG).show();				
					return;
				}
				if( type == TYPE_PHONE ){//手机号输入
					if( UtilHelper.isPhone(data) == false ){//手机号码输入错误
						Toast.makeText(UserAdminActivity.this, R.string.phone_number_format_data, Toast.LENGTH_LONG).show();				
						return;
					}
				}
				showProgressDialog();
				
				mNewPhone = mData.phoneNum;
				mNewAddress = mData.address;
				mNewRealName = mData.realName;
				if( type == TYPE_PHONE ){
					mNewPhone = data;
				}else if( type == TYPE_ADDRESS ){
					mNewAddress = data;					 
				}else if( type == TYPE_REALNAME ){
					mNewRealName = data;					 
				}
//				Log.d(TAG,"mNewPhone:"+mNewPhone+",mNewAddress:"+mNewAddress);
				saveUser();
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
	
	private void saveUser(){
		String url = UrlUtils.getUserSaveUrl(mData.userId, mData.userName, mNewRealName, mData.email, mNewPhone,mNewAddress,null,mNewPortrait);
		Log.d(TAG,"saveUser url:"+url);
		HttpClientDownloader.getInstance().getResultFromUrlWithSession(url,handler,MSG_MODIFY_USER_INFO);
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
	
	private void updateBeanData(){
		SettingItem item = (SettingItem)(findViewById(R.id.phoneitem));
		item.setInfo(mData.phoneNum);
		
		item = (SettingItem)(findViewById(R.id.addressitem));
		item.setInfo(mData.address);
	}
	
	private PicSelHelper mPicSelHelper = null;
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult requestCode:" + requestCode
				+ ",resultCode:" + resultCode);
		if( true == mPicSelHelper.handleActivityResult(requestCode,resultCode,data) ){
			return;
		}
		switch (requestCode) {
		case PicSelHelper.PHOTO_CUT:
			if (resultCode == RESULT_OK) {
				if( mPicSelHelper.getImageUri() != null ){
					setProfileImage(mPicSelHelper.getImageUri());
					sendProfileImage(mPicSelHelper.getImageUri().getPath());
				}
			}
			break;
		default:
			break;
		}
	}
	
	//头像上传
	private boolean sendProfileImage(final String path) {
			showProgressDialog();
			String urlString = UrlUtils.getResourceUploadUrl();
			Log.i(TAG, "URL:" + urlString);
			Map<String, File> files = new HashMap<String, File>();

			Map<String, String> params = new HashMap<String, String>();

			int fileType = MaterialParams.TYPE_USER_PORTRAIT;
			params.put("fileType", Integer.toString(fileType));
			files.put("file1", new File(path));

			NetWorkRequest.UploadFiles(this,urlString, files, params, new Listener<String>() {

				@Override
				public void onResponse(String response) {
					

					ResourceUploadBean bean = ResourceUploadBeanParser
							.parseResourceUploadBean(response);
					if (bean == null) {
						reportToast("上传头像失败!");
						hideProgressDialog();
						return;
					}
					Log.d(TAG, response);
					if (bean.code == 0) {
						if (bean.obj != null && bean.obj.size() > 0) {
							
							ArrayList<String> resIds = new ArrayList<String>();
							for (ResourceUploadobjBean fileInfo : bean.obj) {
								if (fileInfo.status == 0) {
									resIds.add(Integer.toString(fileInfo.fileId));
								}
							}
							if (resIds.size() == 0) {
								reportToast("上传头像失败!");
								hideProgressDialog();
								return;
							}
							String resId = resIds.get(0);
							mNewPortrait = resId;
							File newFile = ImageUtils.getProfilePhoto(UserAdminActivity.this,mNewPortrait,null);
							mTmpFile.renameTo(newFile);
							
							//图片上传成功后设置才生效
							saveUser();
						}
					} else if (bean.code == -2) {
						returnToLogin();
						hideProgressDialog();
					} else {
						reportToast("上传头像失败!");
						hideProgressDialog();
					}
				}

			}, new ErrorListener() {

				@Override
				public void onErrorResponse(VolleyError error) {
					hideProgressDialog();
					if( true == InfoReleaseApplication.showNetWorkFailed(UserAdminActivity.this) ){
						reportToast("上传头像失败!");
                	}
				}

			}, null);
			return true;
	}
	
	private void setProfileImage(Uri uri){
		ImageView imageview = (ImageView) findViewById(R.id.profilephoto_iv);
		imageview.setImageResource(1);
		imageview.setImageURI(uri);
	}
	
	File mTmpFile = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_useradmin);
		mModifyPwdDialogHelper = new ModifyPwdDialogHelper(this);
		
		mPicSelHelper = new PicSelHelper(this);
		mPicSelHelper.setCircleCrop(true);
		
		this.initTitleBar(R.string.userinfo);
		
		AuthenobjBean data = InfoReleaseApplication.authenobjData;
		File file = null;
		Uri uri = null;
		
		if( data.portrait != null &&  data.portrait.isEmpty() == false ){
			file = ImageUtils.getProfilePhoto(this,data.portrait,null);
			uri = Uri.fromFile(file);
			if( file.exists() == true && uri != null ){
				this.setProfileImage(uri);
				
			}
		}	
		
		//临时图片保存路径
		mTmpFile = new File(this.getExternalCacheDir(),"temp.png");
		if( mTmpFile .exists() == false ){
			try {
				mTmpFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mPicSelHelper.setDestUri(Uri.fromFile(mTmpFile));
		
		mData = data;
		setData(data);
		mNewPhone = mData.phoneNum;
		mNewAddress = mData.address;
		mNewRealName = mData.realName;
		mNewPortrait = mData.portrait;
		
		View profilephotoView = findViewById(R.id.profilephtoto_rl);
		profilephotoView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mPicSelHelper.showAddPicDialog();
			}
		});
		
		SettingItem item = (SettingItem)(findViewById(R.id.modifyitem));
		item.setMoreClicked(true);
		item.setName(R.string.modifypwd);
		item.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showModifyDataDialog();
			}
		});
		
		item = (SettingItem)(findViewById(R.id.phoneitem));
		item.setMoreClicked(true);
		item.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mModifyPwdDialogHelper.showUpdatePhoneDialog(new DialogAnswerInterface(){

					@Override
					public void ok_callback() {
						// TODO Auto-generated method stub
						//刷新电话号码
						((SettingItem)(findViewById(R.id.phoneitem))).setInfo(mData.phoneNum);
					}
					
				},false,null,null);
			}
		});
		
		item = (SettingItem)(findViewById(R.id.addressitem));
		item.setMoreClicked(true);
		item.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showModifyDataDialog(TYPE_ADDRESS);
			}
		});
		
		Button logoutBtn = (Button)findViewById(R.id.btn_logout);
		logoutBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				logout();
			}
		});
	}
	
	void logout(){
		HttpClientDownloader.getInstance().loginOut(handler,0);
		//发出logout请求后，直接跳转到登陆界面
		InfoReleaseApplication.saveUserInfo(UserAdminActivity.this,"","");
		InfoReleaseApplication.returnToLogin(UserAdminActivity.this,false);
		Toast.makeText(UserAdminActivity.this, R.string.logout_success, Toast.LENGTH_LONG).show();
	}
	
	void setData(AuthenobjBean data){
		if( data != null ){
			SettingItem item = (SettingItem)(findViewById(R.id.userinfoitem));
			item.setInfo(data.userName);
			item.setName(R.string.username);
			
			item = (SettingItem)(findViewById(R.id.phoneitem));
			item.setInfo(data.phoneNum);
			item.setName(R.string.phonenum);
			
			item = (SettingItem)(findViewById(R.id.addressitem));
			item.setInfo(data.address);
			item.setName(R.string.address);
		}else{
			SettingItem item = (SettingItem)(findViewById(R.id.userinfoitem));
			item.setName(R.string.username);
			
			item = (SettingItem)(findViewById(R.id.phoneitem));
			item.setName(R.string.phonenum);
			
			item = (SettingItem)(findViewById(R.id.addressitem));
			item.setName(R.string.address);
		}
	}
}
