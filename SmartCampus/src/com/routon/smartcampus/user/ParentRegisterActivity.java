package com.routon.smartcampus.user;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.zxing.Result;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.util.TimeUtils;
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.StudentCaptureActivity;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.QRCodeUtil;
import com.routon.widgets.Toast;
import com.squareup.picasso.Picasso;

public class ParentRegisterActivity extends CustomTitleActivity{
	private static final String TAG = "ParentRegisterActivity";
	TextView mQrContentTv = null;
	Calendar mCalendar = null;
	TextView mSelTimeView = null;
	EditText mNameView = null;
	ImageView mImageView = null;
	Button mNextButton = null; 
	TextView mNameTipView = null;
	TextView mTimeTipView = null;
	TextView mCardTipView = null;
	Button mCardSelButton = null; 
	private ArrayList<StudentBean> mStudentDataList = new ArrayList<StudentBean>();
	private int mSelIndex = -1;
	
	//家长注册阶段　
	public final static int TYPE_PARENT_REGISTER = 0;
	//孩子管理中添加孩子阶段
	public final static int TYPE_ADD_CHILD = 1;
	private int mType = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parent_register);
		mType = getIntent().getIntExtra(MyBundleName.TYPE, 0);
		
		if( mType == TYPE_ADD_CHILD ){
			initTitleBar(R.string.add_child);
		}else{
			initTitleBar(R.string.parent_register_title);
		}
		
		mNextButton = (Button) findViewById(R.id.next_step_tv);
		if(mNextButton != null){
			if( mType == TYPE_ADD_CHILD ){
				mNextButton.setText("完成");
			}else{
				mNextButton.setText("下一步");
			}
			mNextButton.setVisibility(View.VISIBLE);
			mNextButton.setEnabled(false);
			mNextButton.setTextColor(this.getResources().getColor(R.color.gray));
			mNextButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if( mType == TYPE_ADD_CHILD ){
						StudentBean addBean = mStudentDataList.get(mSelIndex);
						if( addBean != null ){
							for( StudentBean bean:SmartCampusApplication.mStudentDatas ){
								if( bean.sid == addBean.sid ){
									ParentRegisterActivity.this.reportToast("已经存在孩子列表中");
									return;
								}
							}
						}
						
						Intent intent = new Intent();
						intent.putExtra(MyBundleName.STUDENT_BEAN, addBean);
						ParentRegisterActivity.this.setResult(Activity.RESULT_OK, intent);
						ParentRegisterActivity.this.finish();
					}else{
						Intent intent = new Intent();
						intent.setClass(ParentRegisterActivity.this, PhoneAndVertifyActivity.class);
						intent.putExtra(MyBundleName.STUDENT_ID, mStudentDataList.get(mSelIndex).sid);
						ParentRegisterActivity.this.startActivity(intent);
					}
				}
			});
		}
		
		
		mQrContentTv = (TextView) this.findViewById(R.id.qrcode_image_content);
		
		mImageView = (ImageView)this.findViewById(R.id.student_image);
		mImageView.setImageResource(R.drawable.default_student);
		
		mNameView = (EditText) findViewById(R.id.student_name);
		mNameView.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				handler.removeMessages(0);
				handler.sendEmptyMessageDelayed(0, 3000);
			}
			
		});
		
		
		
		mSelTimeView = (TextView) findViewById(R.id.student_birthday);
		mSelTimeView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if( mCalendar == null ){
					mCalendar = Calendar.getInstance();
				}
				int year = mCalendar.get(Calendar.YEAR);
				int month = mCalendar.get(Calendar.MONTH);
				int day = mCalendar.get(Calendar.DAY_OF_MONTH);
				new DatePickerDialog(ParentRegisterActivity.this, onDateSetListener, year, month, day).show();
			}
		});
		
		View selCodeView = this.findViewById(R.id.select_code_image);
		selCodeView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openGallery();
			}
		});
		selCodeView.requestFocus();
		selCodeView.setFocusable(true);
		
		mNameTipView = (TextView) this.findViewById(R.id.child_name_tv);
		mTimeTipView = (TextView) this.findViewById(R.id.child_date_tv);
		mCardTipView = (TextView) this.findViewById(R.id.student_card_tv);
		mCardSelButton = (Button) this.findViewById(R.id.student_card_btn);
		mCardSelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				   Intent intent = new Intent();
				   intent.setClass(ParentRegisterActivity.this,StudentCaptureActivity.class);
				   startActivityForResult(intent, QRCODE_STUDENT_CARD_SEL_REQUEST_CODE);
			}
		});
	}
	
	// handler类接收数据  
    Handler handler = new Handler() {  
        public void handleMessage(Message msg) {  
            if (msg.what == 0) {  
            	searchStudent(true,true);
            }  
        };  
    };  
	
	private void searchStudent(boolean afterGetCode,boolean afterEditName){
		mNextButton.setEnabled(false);
		mNextButton.setTextColor(Color.GRAY);
		if( mStudentDataList.size() == 0 ){
			if( afterGetCode == true ){
				if( mStudentFaildMsg != null ){
					this.reportToast(mStudentFaildMsg);
				}else{
					this.reportToast("此班级二维码无法获取到学生列表,请选取有效的班级二维码");
				}
			}
			return;
		}
		String name = mNameView.getText().toString();
		if( name.isEmpty() ){
			if( afterEditName == true ){
				this.reportToast("孩子姓名不能为空");
			}
			return; 
		}
		String date = null;
		String date1 = null;
		if( mCalendar != null ){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			date = sdf.format(mCalendar.getTime());
			date1 = sdf1.format(mCalendar.getTime());
		}
		Log.d(TAG,"searchStudent name:"+name);
		mNextButton.setEnabled(false);
		mNextButton.setTextColor(Color.GRAY);
		mSelIndex = -1;
		ArrayList<Integer>  selIndexs = new ArrayList<Integer>();
		boolean compareDate = false;
		for( int i = 0 ; i < mStudentDataList.size(); i++ ){
			StudentBean student = mStudentDataList.get(i);
			//Log.d(TAG,"searchStudent name:"+name+",student.empName:"+student.empName);
			if( student.empName.trim().equals(name.trim()) ){
				/*if( student.birthday != null && student.birthday.isEmpty() == false ){//如果生日存在，则优先匹配生日
					compareDate = true;
					if( date1 != null && student.birthday.contains(date1) ){
						mSelIndex = i;
						selIndexs.add(i);
						break;
					}
				}else if( student.staffCode != null && student.staffCode.isEmpty() == false ){//如果学号存在，则比较学号中是否存在生日信息
					compareDate = true;
					if( date != null && student.staffCode.contains(date) ){
						mSelIndex = i;
						selIndexs.add(i);
						break;
					}
				}else{*/
					mSelIndex = i;
					selIndexs.add(i);
				//}
			}
		}
		Log.d(TAG,"searchStudent name:"+name+",mSelIndex:"+mSelIndex);
		if( mSelIndex < 0 ){//没有查找到学生
			if( afterEditName == true || afterGetCode == true ){
				if( compareDate == false ){
					this.reportToast("在"+mQrContentTv.getText().toString()+"没有查找到学生"+name);
				}else{
					if( date == null){
						this.reportToast("请输入生日");
					}else{
						this.reportToast("学生"+name+"的生日错误，请输入正确的生日");
					}
				}
			}
			mImageView.setImageResource(R.drawable.default_student);
			return;
		}else if( selIndexs.size() == 1 ){//根据姓名查找到一个学生
			this.reportToast("在"+mQrContentTv.getText().toString()+"查找到学生"+name);
			String url = mStudentDataList.get(mSelIndex).imgUrl;
			if( url != null && url.isEmpty() == false ){
				Picasso.with(this).load(url)
					.placeholder(R.drawable.default_student).fit().into(mImageView);
			}else{
				mImageView.setImageResource(R.drawable.default_student);
			}
			mNextButton.setEnabled(true);
			mNextButton.setTextColor(Color.WHITE);
		}else{//根据姓名查找到多个学生
			mSelIndex = -1;
			if( mCalendar == null ){//没有输入生日，请求输入生日过滤学生
				if( afterEditName == true || afterGetCode == true ){			
					this.reportToast("请输入孩子生日,"+"在"+mQrContentTv.getText().toString()+"找到多个"+name);
				}
			}else{//输入生日过滤学生
				for( int j = 0; j < selIndexs.size(); j++ ){
					if( mCalendar != null ){
						if(  mStudentDataList.get(selIndexs.get(j)).birthday != null && date1 != null ){
							if( mStudentDataList.get(selIndexs.get(j)).birthday.contains(date1) ){
								mSelIndex = selIndexs.get(j);
								break;
							}
							
						}else if(  mStudentDataList.get(selIndexs.get(j)).staffCode != null && date != null ){
							if( mStudentDataList.get(selIndexs.get(j)).staffCode.contains(date)  ){
								mSelIndex = selIndexs.get(j);
								break;
							}						
						}
						else if(  mStudentDataList.get(selIndexs.get(j)).studentCode != null && date != null){
							if( mStudentDataList.get(selIndexs.get(j)).studentCode.contains(date) ){
								mSelIndex = selIndexs.get(j);
								break;
							}						
						}
					}
				}
				if( mSelIndex < 0 ){//根据生日过滤的学生没有查找到
					if( afterEditName == true || afterGetCode == true ){
						this.reportToast("在"+mQrContentTv.getText().toString()+"没有查找到生日为"+date+"的学生"+name);
					}
					mImageView.setImageResource(R.drawable.default_student);
					return;
				}else{//根据生日过滤的学生查找到了
					this.reportToast("在"+mQrContentTv.getText().toString()+"查找到生日为"+date+"的学生"+name);
					String url = mStudentDataList.get(mSelIndex).imgUrl;
					if( url != null && url.isEmpty() == false ){
						Picasso.with(this).load(url)
							.placeholder(R.drawable.default_student).fit().into(mImageView);
					}else{
						mImageView.setImageResource(R.drawable.default_student);
					}
					mNextButton.setEnabled(true);
					mNextButton.setTextColor(Color.WHITE);
				}
			}
		}
	}
	
	private void searchStudent(int sid){
		mNextButton.setEnabled(false);
		mNextButton.setTextColor(Color.GRAY);
		if( mStudentDataList.size() == 0 ){		
			this.reportToast("此班级二维码无法获取到学生列表,请选取有效的班级二维码");
			return;
		}
		if( sid <= 0 ){	
			this.reportToast("扫描二维码非有效学生卡二维码");
			return; 
		}
		if( sid == 0 ){	
			this.reportToast("扫描二维码非有效学生卡二维码");
			return; 
		}

		mSelIndex = -1;
		for( int i = 0 ; i < mStudentDataList.size(); i++ ){
			StudentBean student = mStudentDataList.get(i);
			if( student.sid == sid  ){
				mSelIndex = i;
				break;
			}
		}
				
		if( mSelIndex == -1  ){
			this.reportToast("在"+mQrContentTv.getText().toString()+"没有查找到这个学生");
			return;
		}else{
			this.reportToast("在"+mQrContentTv.getText().toString()+"查找到学生"+mStudentDataList.get(mSelIndex).empName);
		}
		Log.d(TAG,"searchStudent name:"+mStudentDataList.get(mSelIndex).empName+",mSelIndex:"+mSelIndex);
		String url = mStudentDataList.get(mSelIndex).imgUrl;
		if( url != null && url.isEmpty() == false ){
			Picasso.with(this).load(url)
				.placeholder(R.drawable.default_student).fit().into(mImageView);
		}else{
			mImageView.setImageResource(R.drawable.default_student);
		}
		mNextButton.setEnabled(true);
		mNextButton.setTextColor(Color.WHITE);
		
	}
	
	private String mStudentFaildMsg = null;
	
	private void initStudentCard(int visible){
		mCardTipView.setVisibility(visible);
		mCardSelButton.setVisibility(visible);
	}
	
	private void initStudentName(int visible){
//		mTimeTipView.setVisibility(visible);
//		mNameView.setVisibility(visible);
//		mNameTipView.setVisibility(visible);
//		mSelTimeView.setVisibility(visible);
	}
	
	private void getStudentListData(final String groupIds) {// 获取指定班级学生列表		
		String urlString = SmartCampusUrlUtils.getStudentListCmdUrl(groupIds,null);
		showProgressDialog();

		Log.d(TAG, "getStudentListData urlString:"+urlString);
		mStudentFaildMsg = null;
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {	
						hideProgressDialog();
						int code = response.optInt("code",-1);
						Log.d(TAG,"getStudentListData response:"+response);
						if ( code == 0) {
							mStudentDataList.clear();
							JSONArray array = response.optJSONArray("datas");
							if( array != null ){
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
								//班级二维码获取到学生数据中有学生卡号
								if( mStudentDataList != null && mStudentDataList.get(0).ctrlId != null 
										&& mStudentDataList.get(0).ctrlId.isEmpty() == false ){
									Log.d(TAG,"student card");
									mImageView.setImageResource(R.drawable.default_student);
									initStudentCard(View.VISIBLE);
									initStudentName(View.INVISIBLE);
								}else{
									initStudentCard(View.INVISIBLE);
									initStudentName(View.VISIBLE);
									searchStudent(true,false);
								}
							}else{
								initStudentCard(View.INVISIBLE);
								initStudentName(View.VISIBLE);
							}
							
						} else {
							mStudentFaildMsg = "获取班级信息失败，请选取有效的班级二维码";
							Toast.makeText(ParentRegisterActivity.this, mStudentFaildMsg, Toast.LENGTH_LONG).show();

						}									
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {			
						//先判断网络状况
						InfoReleaseApplication.showNetWorkFailed(ParentRegisterActivity.this);
						mStudentFaildMsg = "获取班级信息失败，请检查网络状况";
						Toast.makeText(ParentRegisterActivity.this,mStudentFaildMsg , Toast.LENGTH_LONG).show(50);
						hideProgressDialog();
					}
				});
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

	    @Override
	    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	    	mCalendar.set(Calendar.YEAR, year);
	    	mCalendar.set(Calendar.MONTH, monthOfYear);
	    	mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	    	SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);	
	    	mSelTimeView.setText(sdf.format(mCalendar.getTime()));
	    	searchStudent(true,true);
	    }
	};
	
	public static final int QRCODE_IMAGE_SEL_REQUEST_CODE = 0;
	public static final int QRCODE_STUDENT_CARD_SEL_REQUEST_CODE  = 1;
	/**打开相册*/
	private void openGallery() {
	     Intent picture = new Intent(Intent.ACTION_PICK,   android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	     startActivityForResult(picture, QRCODE_IMAGE_SEL_REQUEST_CODE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if( requestCode == QRCODE_IMAGE_SEL_REQUEST_CODE && resultCode == RESULT_OK ){
			 final Uri selectedImage = data.getData();
			 final String pathResult = ImageUtils.getPath(ParentRegisterActivity.this,selectedImage);
			 if( selectedImage != null ){
				  new Thread(new Runnable() {
		                @Override
		                public void run() {
		                    Result result = QRCodeUtil.scanningImage(pathResult);
		                    if( result == null ){
		                        runOnUiThread(new Runnable(){  
		      		              
	                                @Override  
	                                public void run() {  
	                                    //更新UI  
	                                	mQrContentTv.setText("未识别到有效二维码图片");
	                                	mQrContentTv.setTextColor(Color.RED);
	                                }  
	                                  
	                            });
		                    }else{
		                    	final String[] texts = result.getText().split(";");
		                    	if( texts.length >= 3 ){
		                    		 //这儿是耗时操作，完成之后更新UI；  
		                            runOnUiThread(new Runnable(){  
		                            	
		                            	@Override  
		                                public void run() {  
		                                	mQrContentTv.setText(texts[1]); 
		                                	mQrContentTv.setTextColor(Color.BLACK);
		                                	getStudentListData(texts[2]);
		                                }  
		                                  
		                            });  
		                    	}
		                    }

		                }
		            }).start();
		        }
			 }else if( requestCode == QRCODE_STUDENT_CARD_SEL_REQUEST_CODE && resultCode == RESULT_OK ){
				 int sid = data.getIntExtra(StudentCaptureActivity.INTENT_SID_DATA,-1);
				 searchStudent(sid);
			 }
		}  
}
