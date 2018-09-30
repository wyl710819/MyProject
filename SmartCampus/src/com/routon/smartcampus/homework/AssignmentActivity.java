package com.routon.smartcampus.homework;



import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.edurelease.R;
import com.routon.smartcampus.flower.PopOnClickListener;
import com.routon.smartcampus.flower.RemarkImagePreviewActivity;
import com.routon.smartcampus.homework.HomeworkListViewAdapter.MyOnClickListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.ImgUploadUtil;
import com.routon.smartcampus.utils.RecordUploadUtil;
import com.routon.smartcampus.utils.UploadImgListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import com.routon.widgets.Toast;
import android.widget.TextView;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class AssignmentActivity extends BaseActivity implements OnClickListener{

	private TextView tvClassName;
	
	private ImageView add_homework;
	
	private String TAG = "AssignmentActivity";

	private AddHomeworkPopWin takePhotoPopWin;
	private String editStr = null;
	private String titleNextBtnText="布置作业";
	private ProgressDialog progressDialog;
	private String description = "";
	private ArrayList<FeedbackWorkBean> remarkImages = new ArrayList<FeedbackWorkBean>();
	private ArrayList<String> remarkImageList = new ArrayList<String>();
	private List<String> pictureMaterialIds =new ArrayList<String>();;
	private ArrayList<String> imgDatas = new ArrayList<String>();
	private RelativeLayout homework_main;
	private LayoutParams params;
	private ImageView backMenu;
	
	private String dateParam;//传进来的日期

	private ArrayList<FeedbackWorkBean> saveRemarkImages;//保存已经添加未布置的图片和文字
	private String saveRemarkStr;
	private ArrayList<QueryGradeHomeworkBean> gradeHomeWorkList;
	private Long classId;
	private Long gradeId;
	private int homeworkId;
	private HomeworkListViewAdapter mHomeworkListAdapter;
	private ListView mHomeworkListView;
	private int teacherId;
	private String courseName;
	private ArrayList<FeedbackWorkBean>imgList=new ArrayList<FeedbackWorkBean>();
	private ArrayList<FeedbackWorkBean>recordList=new ArrayList<FeedbackWorkBean>();
	public static final int UPLOADSTATUS=0;
	private int uploadStatus=0;//是否完成上传标识
	private int tempUploadStaus=0;
	private ArrayList<String> remarkUpLoadRecords=new ArrayList<String>();
	private ArrayList<String> remarkUpLoadImages=new ArrayList<String>();
	private int isExistImg=0;
	private int isExistRecord=0;
	private List<String>fileIdparamsList=new ArrayList<String>();
	private List<Integer>recordTimeList=new ArrayList<Integer>();
	//录音文件保存位置
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.assignment_page);
		//获取登录老师id
		teacherId=InfoReleaseApplication.authenobjData.userId;
		
		initView();
       
		
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String className = bundle.getString("className");
        tvClassName.setText("给"+className+"布置作业");
        courseName=bundle.getString("courseName");
        classId=Long.valueOf(bundle.getString("classId")).longValue();
        gradeId=Long.valueOf(bundle.getString("gradeId")).longValue();
        dateParam=bundle.getString("dateParam");
        gradeHomeWorkList=bundle.getParcelableArrayList("gradeHomeWorkList");
        Log.d(TAG,"年级id:"+gradeId+"班级id:"+classId+"日期:"+dateParam);
        backMenu.setOnClickListener(this);
//        add_homework.setOnClickListener(this);
        initData();
        
		
			
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){

		case R.id.next_btn:
		
			showPopWin(saveRemarkImages,saveRemarkStr);
			
			break;
		case R.id.back_btn:
			AssignmentActivity.this.finish();
			overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
		default:
			break;
		}
	}
	
	private void initView() {
		mHomeworkListView=(ListView) findViewById(R.id.homework_listview);
		tvClassName=(TextView) findViewById(R.id.title_view);
		backMenu=(ImageView) findViewById(R.id.back_btn);
		add_homework=(ImageView) findViewById(R.id.next_btn);
		add_homework.setVisibility(View.VISIBLE);
		
	    add_homework.setOnClickListener(this);
		//homeworkAdapter=new HomeWorkListViewAdapter(this.getApplicationContext(), mList);
	    //左滑退出
//		mBackListener = new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				AssignmentActivity.this.finish();
//				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
//			}
//		};
//		backMenu.setOnClickListener(mBackListener);
		
	}
	private void initData() {
		

		//班级id  23707 24063 23858 23859
		//年级id  23671
		//获取年级其他班级作业列表
//		getGradeHomeworkList(teacherId,gradeId,classId);
		mHomeworkListAdapter=new HomeworkListViewAdapter(AssignmentActivity.this, gradeHomeWorkList,classId);
		mHomeworkListAdapter.setListener(new MyOnClickListener() {
			//回调adapter中声明的点击方法
			@Override
			public void click(View v, int position) {
				// TODO Auto-generated method stub
			   Date dateTemp = new Date();  
			   SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");  
			   dateParam = dateParam+" "+sdf.format(dateTemp); 
			   Log.d(TAG,dateParam);
			   AssingHomework(classId, gradeHomeWorkList.get(position).hid,dateParam);
			}
			
			
		});
		Log.d(TAG,"班级id:"+classId);
		mHomeworkListView.setAdapter(mHomeworkListAdapter);
		mHomeworkListAdapter.notifyDataSetInvalidated();
		Log.d(TAG,"年级作业列表:"+gradeHomeWorkList.size());
		
		
	}

	public  void showPopWin(ArrayList<FeedbackWorkBean> savemages, String saveRemarkString) {
		// TODO Auto-generated method stub
		homework_main=(RelativeLayout) findViewById(R.id.assign_page);
		if (takePhotoPopWin!=null && takePhotoPopWin.isShowing()) {
			takePhotoPopWin.dismiss();
		}
		takePhotoPopWin = new AddHomeworkPopWin(this, popOnClickListener, editStr, titleNextBtnText,savemages,saveRemarkString);
		// 设置Popupwindow显示位置（从底部弹出）
//		takePhotoPopWin.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
//		takePhotoPopWin.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		takePhotoPopWin.showAtLocation(homework_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		params = getWindow().getAttributes();
		// 当弹出Popupwindow时，背景变半透明
		params.alpha = 0.7f;
		getWindow().setAttributes(params);

		// 设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
		takePhotoPopWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				params = getWindow().getAttributes();
				params.alpha = 1f;
				getWindow().setAttributes(params);
//				saveRemarkImages=takePhotoPopWin.getRemarkImages();
//				saveRemarkStr = takePhotoPopWin.getRemarkText();
			}
		});
	
	}
	
	private PopOnClickListener popOnClickListener = new PopOnClickListener() {

		@Override
		public void itemClick(int position) {// 图片预览
			Intent intent = new Intent(AssignmentActivity.this, ImageBrowerActivity.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, true);
			bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION, position);
			recordList.clear();
			imgList.clear();
			if(takePhotoPopWin.imgList.size()>0){
				for(int i=0;i<takePhotoPopWin.imgList.size();i++){
					recordList.add(null);
					if(takePhotoPopWin.imgList.get(i).fileType!=2){
						imgList.add(takePhotoPopWin.imgList.get(i));
					}else if(takePhotoPopWin.imgList.get(i).fileType==2){
						recordList.set(i, takePhotoPopWin.imgList.get(i));
					}
				}
			}
			bundle.putParcelableArrayList(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
					imgList);
			bundle.putString("homework", "homework");
			intent.putExtras(bundle);
			AssignmentActivity.this.startActivityForResult(intent, 2);
		}

		@Override
		public void lastItemtemClick() {// 添加图片
			Intent intent = new Intent(AssignmentActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 14);
			AssignmentActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void addImgClick(int position) {// 添加图片图标
			Intent intent = new Intent(AssignmentActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 15);
			AssignmentActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void awardClick() {// 布置作业

			remarkUpLoadRecords.clear();
			recordTimeList.clear();
			remarkUpLoadImages.clear();
			pictureMaterialIds.clear();
			if (takePhotoPopWin.getRemarkImages() != null && takePhotoPopWin.getRemarkImages().size() > 2) {
				description = takePhotoPopWin.getRemarkText();
				remarkImages = takePhotoPopWin.getRemarkImages();

				for (int i = 0; i < remarkImages.size(); i++) {
					if(remarkImages.get(i).fileType==2){
						remarkUpLoadRecords.add(remarkImages.get(i).fileUrl);
						recordTimeList.add(remarkImages.get(i).audioLength);
					}else{
						remarkUpLoadImages.add(remarkImages.get(i).fileUrl);
					}
				}
				showMyProgressDialog();
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
//						sendImages(remarkImages);
						if(remarkUpLoadImages.size()>2){
							isExistImg=1;
							ImgUploadUtil.uploadImg(AssignmentActivity.this,remarkUpLoadImages,new UploadImgListener() {
								
								@Override
								public void uploadImgSuccessListener(List<Integer> imgFileIdList) {
									// TODO Auto-generated method stub
									tempUploadStaus++;
									Message msg = new Message();
									msg.what = UPLOADSTATUS;
									msg.arg1 = tempUploadStaus;
									handler.sendMessage(msg);
									if (imgFileIdList!=null && imgFileIdList.size()>0) {
										for (int j = 0; j < imgFileIdList.size(); j++) {
											pictureMaterialIds.add(imgFileIdList.get(j)+"_166");
										}
									}
									//getCourseName(teacherId,2);
//									AddNewHomework(teacherId,courseName,description,pictureMaterialIds);

								}
							
								@Override
								public void uploadImgErrorListener(String errorStr) {
									// TODO Auto-generated method stub
									Toast.makeText(AssignmentActivity.this, errorStr, Toast.LENGTH_SHORT).show();
									hideMyProgressDialog();
								}
							});
						}
				       if(remarkUpLoadRecords.size()>0){
				    	   isExistRecord=1;
				    	   RecordUploadUtil.uploadRecord(AssignmentActivity.this,remarkUpLoadRecords, new UploadImgListener(){

								@Override
								public void uploadImgSuccessListener(
										List<Integer> imgFileIdList) {
									// TODO Auto-generated method stub
									tempUploadStaus++;
									Message msg = new Message();
									msg.what = UPLOADSTATUS;
									msg.arg1 = tempUploadStaus;
									handler.sendMessage(msg);
									if (imgFileIdList!=null && imgFileIdList.size()>0) {
										for (int j = 0; j < imgFileIdList.size(); j++) {
											pictureMaterialIds.add(imgFileIdList.get(j)+"_172");
											fileIdparamsList.add(imgFileIdList.get(j)+"_audioDuration="+recordTimeList.get(j));
										}
									}
//									AddNewHomework(teacherId,courseName,description,pictureMaterialIds);
								}

								@Override
								public void uploadImgErrorListener(String errorStr) {
									// TODO Auto-generated method stub
									Toast.makeText(AssignmentActivity.this, errorStr, Toast.LENGTH_SHORT).show();
									hideMyProgressDialog();
								}
								
							});
				       }
						
					}
				}, 200);
			} else {
				if (takePhotoPopWin.getRemarkText() != null && !takePhotoPopWin.getRemarkText().trim().equals("")) {
					description = takePhotoPopWin.getRemarkText();
					AddNewHomework(teacherId,courseName,description,pictureMaterialIds,fileIdparamsList);
//					getCourseName(teacherId, 2);
					//awardBadge(1);
				} else {
//					if (issuedId != 0) {
					showToast("作业信息不能为空");
//					} else {
//						awardBadge(1);
//					}
				}

			}

		}

		@Override
		public void saveRemark(View v) {
			// TODO Auto-generated method stub
			
		}

	};
	private Handler handler=new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case UPLOADSTATUS:
				uploadStatus=msg.arg1;
				//判断是否语音图片都存在
				if(remarkUpLoadImages.size()>2&&remarkUpLoadRecords.size()>0){
					if(uploadStatus==2&&isExistImg==1&&isExistRecord==1){
						AddNewHomework(teacherId,courseName,description,pictureMaterialIds,fileIdparamsList);
						uploadStatus=0;
						tempUploadStaus=0;
						remarkUpLoadRecords.clear();
						remarkUpLoadImages.clear();
					}
				}else if(remarkUpLoadImages.size()>2&&remarkUpLoadRecords.size()==0){
						//语图片仅存在
						AddNewHomework(teacherId,courseName,description,pictureMaterialIds,fileIdparamsList);
						remarkUpLoadRecords.clear();
						remarkUpLoadImages.clear();
						uploadStatus=0;
						tempUploadStaus=0;
				}else if(remarkUpLoadImages.size()==2&&remarkUpLoadRecords.size()>0){//仅存在语音
						AddNewHomework(teacherId,courseName,description,pictureMaterialIds,fileIdparamsList);
						remarkUpLoadRecords.clear();
						remarkUpLoadImages.clear();
						uploadStatus=0;
						tempUploadStaus=0;
				}
			}
		}
	};
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ArrayList<FeedbackWorkBean> mListDatas=new ArrayList<FeedbackWorkBean>();
		FeedbackWorkBean bean=null;
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {
				imgDatas = data.getStringArrayListExtra("img_data");
				
				for(int i=0;i<imgDatas.size();i++){
					bean=new FeedbackWorkBean();
					bean.fileUrl=imgDatas.get(i);
					mListDatas.add(bean);
				}
				takePhotoPopWin.addImgList(mListDatas);
			} else if (requestCode == 2) {
				mListDatas = data.getParcelableArrayListExtra("img_data");
				for(int i=0;i<recordList.size();i++){
					if(recordList.get(i)!=null){
						mListDatas.add(0,recordList.get(i));
					}
				}
				takePhotoPopWin.updateImgList(mListDatas);
			}
		}

	}
	

	private ProgressDialog progressDialog1;
	private void showMyProgressDialog(){
		if( progressDialog1 == null || !progressDialog1.isShowing()){
			progressDialog1 = ProgressDialog.show(AssignmentActivity.this, "", "...loading...");
		}
	}
	
	private void hideMyProgressDialog(){
		if (progressDialog1 != null && progressDialog1.isShowing()) {
			progressDialog1.dismiss();
			progressDialog1 = null;
		}
	}
	
	public String listToString(List<String> list, char separator) {   
		StringBuilder sb = new StringBuilder();    
		for (int i = 0; i < list.size(); i++) { 
			
				sb.append(list.get(i)).append(separator);   
			
			
		}   
		return sb.toString().substring(0,sb.toString().length()-1);
		}
	
	/**
	 * 新建作业接口
	 * */
	private void AddNewHomework(int teacherId , String course , String description, List<String> fileIds,List<String>fileIdparams) {
		 String fileId=null;
	        String urlString=null;
	        String fileIdparam=null;
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("description", description));
			if(fileIds!=null&&fileIds.size()>0&&fileIdparams!=null&&fileIdparams.size()>0){
				fileId=listToString(fileIds,',');
				fileIdparam=listToString(fileIdparams, ',');
				urlString=SmartCampusUrlUtils.getAddHomeworkUrl(String.valueOf(teacherId), String.valueOf(course), String.valueOf(description),String.valueOf(fileId),String.valueOf(fileIdparam));
			}else if(fileIds!=null&&fileIds.size()>0&&fileIdparams.size()==0){
				fileId=listToString(fileIds,',');
				urlString=SmartCampusUrlUtils.getAddHomeworkUrl(String.valueOf(teacherId), String.valueOf(course), String.valueOf(description),String.valueOf(fileId),null);
			}else if(fileIds.size()>0&&fileIdparams!=null&&fileIdparams.size()>0){
				fileIdparam=listToString(fileIdparams, ',');
				urlString=SmartCampusUrlUtils.getAddHomeworkUrl(String.valueOf(teacherId), String.valueOf(course), String.valueOf(description),null,String.valueOf(fileIdparam));
			}else{
				urlString=SmartCampusUrlUtils.getAddHomeworkUrl(String.valueOf(teacherId), String.valueOf(course), String.valueOf(description),null,null);
			}
		 
		
				
		//showProgressDialog1();

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						//hideProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								if(mFilePath!=null){
									deleteAllFiles(new File(mFilePath));
								}
								JSONObject obj = response.getJSONObject("datas");

									homeworkId=Integer.parseInt(obj.getString("hId"));
									
								    Date dateTemp = new Date();  
								    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");  
								    dateParam = dateParam+" "+sdf.format(dateTemp); 
								    Log.d(TAG,dateParam);
									//调布置作业接口
									AssingHomework( classId, homeworkId,dateParam);

								
//								mAdaptor.notifyDataSetChanged();
							} else if (response.getInt("code") == -2) {
								hideMyProgressDialog();
								InfoReleaseApplication.returnToLogin(AssignmentActivity.this);
								
							} else {
								hideMyProgressDialog();
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(AssignmentActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
								

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						//Log.e(TAG, "sorry,Error");
						if( InfoReleaseApplication.showNetWorkFailed(AssignmentActivity.this) == true ){
							Toast.makeText(AssignmentActivity.this, "新建作业失败!", Toast.LENGTH_LONG).show();
						}
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	/**
	 * 布置作业接口
	 * */
	private void AssingHomework(Long classId , Integer homeworkId,String dateTime) {
        
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("dateTime", dateTime));
		
		
        String urlString=null;
		
		urlString=SmartCampusUrlUtils.getAssingHomeworkUrl( String.valueOf(classId), String.valueOf(homeworkId),String.valueOf(dateTime));
		Log.d(TAG,urlString);
		
		//showProgressDialog1();
		
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {

								 Toast.makeText(AssignmentActivity.this, "布置作业成功!",
										 Toast.LENGTH_LONG).show();
								 if(takePhotoPopWin!=null){
									 if(takePhotoPopWin.isShowing()){
										 takePhotoPopWin.dismiss();
									 }
								 }
								 
								 
			    				 setResult(RESULT_OK);
								 finish();
								 
//								mAdaptor.notifyDataSetChanged();
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(AssignmentActivity.this);
								
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(AssignmentActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
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
						if( InfoReleaseApplication.showNetWorkFailed(AssignmentActivity.this) == true ){
							Toast.makeText(AssignmentActivity.this, "布置作业失败!", Toast.LENGTH_LONG).show();
						}
						
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	} 

	/**
	 * 查询年级作业接口
	 * */
	private void getGradeHomeworkList(int teacherId  , Long gradeId,final Long classId) {

		String urlString=SmartCampusUrlUtils.getHomeworkQueryUrl(String.valueOf(teacherId), String.valueOf(gradeId),String.valueOf(classId));
		
		showMyProgressDialog();

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								final ArrayList<QueryGradeHomeworkBean> gradeHomeWorkList=new ArrayList<QueryGradeHomeworkBean>();
								JSONArray jsonArray = response.getJSONArray("datas");
								int len = jsonArray.length();
								for(int i = 0; i< len; i++){
									JSONObject obj = (JSONObject) jsonArray.get(i);
									//作业
									QueryGradeHomeworkBean queryHomeWorkBean=new QueryGradeHomeworkBean(obj);
									//学生列表
									
									gradeHomeWorkList.add(queryHomeWorkBean);
								}
								sortClass sort = new sortClass();  
						        Collections.sort(gradeHomeWorkList,sort);  
								mHomeworkListAdapter=new HomeworkListViewAdapter(AssignmentActivity.this, gradeHomeWorkList,classId);
								mHomeworkListAdapter.setListener(new MyOnClickListener() {
									//回调adapter中声明的点击方法
									@Override
									public void click(View v, int position) {
										// TODO Auto-generated method stub
									   Date dateTemp = new Date();  
									   SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");  
									   dateParam = dateParam+" "+sdf.format(dateTemp); 
									   Log.d(TAG,dateParam);
									   AssingHomework(classId, gradeHomeWorkList.get(position).hid,dateParam);
									}
									
									
								});
								Log.d(TAG,"班级id:"+classId);
								mHomeworkListView.setAdapter(mHomeworkListAdapter);
								mHomeworkListAdapter.notifyDataSetInvalidated();
								Log.d(TAG,"年级作业列表:"+gradeHomeWorkList.size());
								//mAdaptor.notifyDataSetChanged();
								
								

							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(AssignmentActivity.this);
								
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(AssignmentActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
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
						if( InfoReleaseApplication.showNetWorkFailed(AssignmentActivity.this) == true ){
							Toast.makeText(AssignmentActivity.this, "查询年级作业失败!", Toast.LENGTH_LONG).show();
						}
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	
	
	//对查询到的作业进行排序
	class sortClass implements Comparator{  
	    public int compare(Object arg0,Object arg1){  
	    	QueryGradeHomeworkBean user0 = (QueryGradeHomeworkBean)arg0;  
	    	QueryGradeHomeworkBean user1 = (QueryGradeHomeworkBean)arg1;  
	        int flag = user0.assignmentTime.compareTo(user1.assignmentTime)*(-1);  
	        return flag;  
	    }  
	}  
	 //删除录制的本地音频
	 public static void deleteAllFiles(File root) {  
	       File files[] = root.listFiles();  
	       if (files != null)  
	           for (File f : files) {  
	               if (f.isDirectory()) { // 判断是否为文件夹  
	                   deleteAllFiles(f);  
	                   try {  
	                       f.delete();  
	                   } catch (Exception e) {  
	                   }  
	               } else {  
	                   if (f.exists()) { // 判断是否存在  
	                       try {  
	                           f.delete();  
	                       } catch (Exception e) {  
	                       }  
	                   }  
	               }  
	           }  
	   } 
	
	
//	private Toast mToast;  
    public void showToast(String text) {    
//        if(mToast == null) {    
//            mToast = Toast.makeText(AssignmentActivity.this, text, Toast.LENGTH_SHORT);    
//        } else {    
//            mToast.setText(text);      
//            mToast.setDuration(Toast.LENGTH_SHORT);    
//        }    
//        mToast.show();   
        Toast.makeText(AssignmentActivity.this, text, Toast.LENGTH_SHORT).show();  
    }    
        
//    public void cancelToast() {    
//            if (mToast != null) {    
//                mToast.cancel();    
//            }    
//        }    
        
    public void onBackPressed() {    
//            cancelToast();    
            super.onBackPressed();    
        } 


}