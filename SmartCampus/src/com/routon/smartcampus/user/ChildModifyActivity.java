package com.routon.smartcampus.user;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.net.NetWorkRequest;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.widget.CountdownButton;
import com.routon.inforelease.widget.LoginHelper;
import com.routon.inforelease.widget.PicSelHelper;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.utils.UtilHelper;

public class ChildModifyActivity extends CustomTitleActivity{
	private static final String TAG = "ChildModifyActivity";
	private StudentBean mStudent;
	private ImageView mProfileIv = null;
	private PicSelHelper mPicSelHelper = null;
	private EditText mVerifyEt = null;
	private CountdownButton mGetVertifyNum = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_child_modify);
		
		mVerifyEt = (EditText) (this.findViewById(R.id.vertify_edit));
		
		mStudent = (StudentBean) this.getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
		
		initTitleBar("修改照片");
		mPicSelHelper = new PicSelHelper(this);
		//临时图片保存路径
		File tmpFile = new File(this.getExternalCacheDir(),"temp.png");
		tmpFile.delete();
		mPicSelHelper.setDestUri(Uri.fromFile(tmpFile));
		
		mPicSelHelper.setCutImageMaxSize(354,472);
		if( mStudent != null ){
//			TextView nameTv = (TextView)(this.findViewById(R.id.student_name));
//			
//			nameTv.setText(mStudent.empName);
//			
//			TextView classTv = (TextView)(this.findViewById(R.id.student_class));
//			classTv.setText(mStudent.grade+mStudent.staffGroup);
			
			TextView phoneTv = (TextView)(this.findViewById(R.id.student_parentphone));
			phoneTv.setText(String.valueOf(mStudent.parentPhone));
			
			mProfileIv = (ImageView)(this.findViewById(R.id.student_image));
			File file = ImageUtils.getProfilePhoto(this, String.valueOf(mStudent.sid), mStudent.imageLastUpdateTime);
			if( file.exists() == true ){
				mProfileIv.setImageURI(Uri.fromFile(file));
			}else{
				mProfileIv.setImageResource(R.drawable.default_student);
			}
		
			mProfileIv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mPicSelHelper.showAddPicDialog();
				}
			});
			
			this.setTitleNextBtnClickListener("上传", new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					sendProfileImage(mPicSelHelper.getImageUri().getPath());
				}
			});
			
			mGetVertifyNum = (CountdownButton) this.findViewById(R.id.get_vertify_num_text);
			mGetVertifyNum.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String phone = String.valueOf(mStudent.parentPhone);
					if( UtilHelper.checkPhoneValid(phone,ChildModifyActivity.this)== true ){//有效的手机号码
						//获取验证码
						LoginHelper.getVertifyNum(ChildModifyActivity.this,phone,mGetVertifyNum);		
					}
				}
			});
		}
	}
	
	//头像上传
	private boolean sendProfileImage(final String path) {
		String verifyCode = mVerifyEt.getText().toString().trim();
		if( verifyCode == null || verifyCode.isEmpty() ){
			this.reportToast("验证码不能为空");
			return false;
		}
		File file = new File(path);
		if( file.exists() == false ){
			this.reportToast("请选择图片");
			return false;
		}
			showProgressDialog();
			String urlString = SmartCampusUrlUtils.getUpdatePhotoCmdUrl();
			Log.i(TAG, "URL:" + urlString);
			Map<String, File> files = new HashMap<String, File>();
			Map<String, String> params = new HashMap<String, String>();

			params.put("sid", String.valueOf(mStudent.sid));
			params.put("phone", String.valueOf(mStudent.parentPhone));
			params.put("verifyCode", verifyCode);
			files.put("photo", file);

			NetWorkRequest.UploadFiles(this,urlString, files, params, new Listener<String>() {
					@Override
					public void onResponse(String response) {
						hideProgressDialog();

						BaseBean bean = BaseBeanParser
								.parseBaseBean(response);
						if (bean == null) {
							reportToast("上传照片失败!");
							hideProgressDialog();
							return;
						}
						Log.d(TAG, response);
						if (bean.code == 0) {
							reportToast("上传照片成功!");
							File dcimFile = ImageUtils.getProfilePhoto(ChildModifyActivity.this,String.valueOf(mStudent.sid),
									mStudent.imageLastUpdateTime);
							final File portraitFile = ImageUtils.getProfilePhoto(ChildModifyActivity.this,String.valueOf(mStudent.sid),null);
							ImageUtils.copyFile(path,dcimFile.getAbsolutePath());
							ImageUtils.copyFile(dcimFile.getAbsolutePath(),portraitFile.getAbsolutePath());
							ChildModifyActivity.this.setResult(Activity.RESULT_OK);
							ChildModifyActivity.this.finish();
						} else {
							if ( bean.msg != null && bean.msg.isEmpty() == false ){
								reportToast(bean.msg);
							}else{
								reportToast("上传照片失败!");
							}
						}
					}

				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideProgressDialog();
						if( true == InfoReleaseApplication.showNetWorkFailed(ChildModifyActivity.this) ){
							reportToast("上传头像失败!");
	                	}
					}

				}, null);
				return true;
		}
		
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if( true == mPicSelHelper.handleActivityResult(requestCode,resultCode,data) ){
			return;
		}
		switch (requestCode) {
		case PicSelHelper.PHOTO_CUT:
			if (resultCode == RESULT_OK) {
				if( mPicSelHelper.getImageUri() != null ){
					mProfileIv.setImageResource(1);	
					mProfileIv.setImageURI(mPicSelHelper.getImageUri());
				}
			}
			break;
		}
	}
}
