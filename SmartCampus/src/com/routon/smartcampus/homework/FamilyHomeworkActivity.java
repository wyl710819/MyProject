package com.routon.smartcampus.homework;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import com.routon.smartcampus.SmartCampusApplication;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.flower.PopOnClickListener;
import com.routon.smartcampus.homework.FamilyHomeworkAdapter.OnRateClickListener;
import com.routon.smartcampus.homework.FamilyHomeworkAdapter.onCheckListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.ImgUploadUtil;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.RecordUploadUtil;
import com.routon.smartcampus.utils.UploadImgListener;
import com.routon.smartcampus.view.WeekCalendarView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FamilyHomeworkActivity extends BaseActivity {
	private static String TAG = "FamilyHomeworkActivity";

	private ListView homeworkLv;
	private ProgressDialog progressDialog;
	private String dateParam;

	private List<FamilyHomeworkBean> familyHomeworkBeans;
	private FamilyHomeworkAdapter homeworkAdapter;

	private String studentId;

	private String studentName="";
	private String studentUrl="";
	private RelativeLayout homework_main;
	private AddHomeworkPopWin takePhotoPopWin;
	private String editStr = null;
	private String titleNextBtnText="提交反馈";
	private LayoutParams params;
	private ArrayList<FeedbackWorkBean>imgList=new ArrayList<FeedbackWorkBean>();
	private ArrayList<FeedbackWorkBean>recordList=new ArrayList<FeedbackWorkBean>();
	private List<String>fileIdparamsList=new ArrayList<String>();
	private List<Integer>recordTimeList=new ArrayList<Integer>();
	private ArrayList<FeedbackWorkBean> remarkImages = new ArrayList<FeedbackWorkBean>();
	private ArrayList<String> remarkUpLoadRecords=new ArrayList<String>();
	private ArrayList<String> remarkUpLoadImages=new ArrayList<String>();
	private List<String> pictureMaterialIds = new ArrayList<String>();
	private List<ArrayList<FeedbackWorkBean> > allHomeworkLists=new ArrayList<ArrayList<FeedbackWorkBean>>();
	
	private List<String> saveParentRemarkList=new ArrayList<String>();
	public static final int UPLOADSTATUS=0;
	private String parent_remark = "";
	private int uploadStatus=0;//是否完成上传标识
	private int tempUploadStaus=0;
	private int isExistImg=0;//0不存在1存在
	private int isExistRecord=0;
	private ArrayList<FeedbackWorkBean> saveRemarkImages=new ArrayList<FeedbackWorkBean>();//保存已经添加未布置的图片和文字
	private String saveRemarkStr;
	private int homeworkId;
	private int mPosition;
	private ArrayList<String> imgDatas = new ArrayList<String>();
	//录音文件保存位置
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.activity_famile_homework_layout);
		
		
		StudentBean bean = (StudentBean) getIntent().getSerializableExtra(MyBundleName.STUDENT_BEAN);
		if (bean!=null) {
			studentId = String.valueOf(bean.sid);
			studentName = bean.empName;
			
			studentUrl = bean.imgUrl;
		}

		initView();
		initData();
	}

	private void initView() {
		ImageView backBtn = (ImageView) findViewById(R.id.back_btn);
		TextView titleView = (TextView) findViewById(R.id.title_view);
		if( SmartCampusApplication.mFamilyVersion == false ){
			titleView.setText(studentName+"的作业");
		}else{
			titleView.setText("家庭作业");
		}
		WeekCalendarView weekCalendarView = (WeekCalendarView) findViewById(R.id.weekCalendarView);
		this.setTouchUnDealView(weekCalendarView);
		weekCalendarView.setOnChangeListener(new WeekCalendarListener() {

			@Override
			public void WeekCalendarClickListener(String dateStr) {
				dateParam = dateStr;
				getCorrectHomeworkStudentList(dateParam, studentId);
			}
		});
		homeworkLv = (ListView) findViewById(R.id.homework_lv);
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				FamilyHomeworkActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		setMoveBackEnable(false);
		homeworkLv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				Intent intent = new Intent(FamilyHomeworkActivity.this, FamilyHomeworkDetailsActivity.class);
//				FamilyHomeworkBean homeworkBean = familyHomeworkBeans.get(position);
//				Bundle bundle = new Bundle();
//				bundle.putSerializable(MyBundleName.HOMEWORK_BEAN, homeworkBean);
//				bundle.putString(MyBundleName.HOMEWORK_DATE, dateParam);
//				intent.putExtras(bundle);
//				startActivity(intent);
			}
		});
		backBtn.setOnClickListener(mBackListener);
	}

	private void initData() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		dateParam = sdf.format(date);
		getCorrectHomeworkStudentList(dateParam, studentId);
	}

	/**
	 * 获取作业列表
	 * 
	 * @param dateStr
	 * @param sId
	 */
	private void getCorrectHomeworkStudentList(String dateStr, String sId) {

		String urlString = SmartCampusUrlUtils.getFamilyHomeworkListUrl(dateStr, sId);

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
								if (familyHomeworkBeans == null) {
									familyHomeworkBeans = new ArrayList<FamilyHomeworkBean>();
								} else {
									familyHomeworkBeans.clear();
								}

								JSONArray jsonArray = response.getJSONArray("datas");
								int len = jsonArray.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) jsonArray.get(i);
									FamilyHomeworkBean bean = new FamilyHomeworkBean(obj);
									familyHomeworkBeans.add(bean);
								}
								allHomeworkLists.clear();
								saveParentRemarkList.clear();
								Collections.reverse(familyHomeworkBeans);
								for(int i=0;i<familyHomeworkBeans.size();i++){
									if(familyHomeworkBeans.get(i).isCheck==true){
										ArrayList<FeedbackWorkBean> homeworkList=new ArrayList<FeedbackWorkBean>();
										if(familyHomeworkBeans.get(i).checkResList.size()>0){
											for(int j=0;j<familyHomeworkBeans.get(i).checkResList.size();j++){
												FeedbackWorkBean feedBean=new FeedbackWorkBean();
												feedBean.audioLength=familyHomeworkBeans.get(i).checkResList.get(j).audioLength;
												feedBean.fileType=familyHomeworkBeans.get(i).checkResList.get(j).fileType;
												feedBean.fileUrl=familyHomeworkBeans.get(i).checkResList.get(j).fileUrl;
												feedBean.isLocal=false;
												feedBean.fileId=familyHomeworkBeans.get(i).checkResList.get(j).fileId;
												homeworkList.add(feedBean);
											}
										}else{
											homeworkList.add(null);
										}
										if(!familyHomeworkBeans.get(i).parent_remark.equals("null")){
											saveParentRemarkList.add(familyHomeworkBeans.get(i).parent_remark);
										}else{
											saveParentRemarkList.add(null);
										}
//										Log.d(TAG,"size:"+homeworkList.size()+"-"+homeworkList.get(0).fileUrl);
										allHomeworkLists.add(homeworkList);
									}else {
										saveParentRemarkList.add(null);
										allHomeworkLists.add(null);
									}
//									feedBean.parent_remark=familyHomeworkBeans.get(i)
								}
								if (homeworkAdapter == null) {
									homeworkAdapter = new FamilyHomeworkAdapter(FamilyHomeworkActivity.this,
											familyHomeworkBeans);
									setItemCheckListener(homeworkAdapter);
									homeworkLv.setAdapter(homeworkAdapter);
								} else {
									homeworkAdapter.notifyDataSetChanged();
									if (!homeworkLv.isStackFromBottom()) {
										homeworkLv.setStackFromBottom(true);
									}
									homeworkLv.setStackFromBottom(false);
								}

							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(FamilyHomeworkActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(FamilyHomeworkActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();
								if( familyHomeworkBeans != null ){
									familyHomeworkBeans.clear();
								}
								if (homeworkAdapter != null) {
									homeworkAdapter.notifyDataSetChanged();
								}
							}

						} catch (JSONException e) {
							e.printStackTrace();

							showToast("当前日期老师没有布置作业");
							if (homeworkAdapter != null) {
								homeworkAdapter.notifyDataSetChanged();
							}
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(FamilyHomeworkActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void setItemCheckListener(FamilyHomeworkAdapter homeworkAdapter) {
		homeworkAdapter.setOnCheckListener(new onCheckListener() {
			
			@Override
			public void onCheck(int position) {
//					checkHomework(position);
				saveRemarkImages.clear();
				saveRemarkStr="";
				mPosition=position;
			    homeworkId=familyHomeworkBeans.get(position).hId;
			    if(allHomeworkLists.size()>0 && allHomeworkLists.get(position)!=null &&allHomeworkLists.get(position).size()>1){
			    	
			    	for (int i = 0; i < allHomeworkLists.get(position).size(); i++) {
						if (!allHomeworkLists.get(position).get(i).isLocal && allHomeworkLists.get(position).get(i).fileType==2) {
							//40224_audioDuration=5
							fileIdparamsList.add(allHomeworkLists.get(position).get(i).fileId+"_audioDuration="+allHomeworkLists.get(position).get(i).audioLength);
						}
					}
			    	
			    	
			    	saveRemarkImages=allHomeworkLists.get(position);
			    }
			    if(saveParentRemarkList.size()>0&& saveParentRemarkList.get(position)!=null){
			    	
			    	saveRemarkStr=saveParentRemarkList.get(position);
			    }
			    
			    
			    
				showPopWin(saveRemarkImages,saveRemarkStr);
			}

		});
		
		homeworkAdapter.setOnRateClickListener(new OnRateClickListener() {
			
			@Override
			public void onRateClick(int position) {
				Intent intent = new Intent(FamilyHomeworkActivity.this, FamilyFeedbackActivity.class);
				intent.putExtra("student_name", studentName);
				intent.putExtra("student_url", studentUrl);
				Bundle bundle = new Bundle();
				bundle.putSerializable("feedback_correct", familyHomeworkBeans.get(position));
				intent.putExtras(bundle);
				startActivity(intent);
				
			}
		});
		
	}
	
	private void checkHomework(final Integer position,final int homeworkId,List<String> fileIds,String parent_remark,List<String>fileIdparams) {
		String fileId=null;
        String urlString=null; 
        String fileIdparam=null;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("parent_remark", parent_remark));
		if(fileIds!=null&&fileIds.size()>0&&fileIdparams!=null&&fileIdparams.size()>0){
			fileId=listToString(fileIds,',');
			fileIdparam=listToString(fileIdparams, ',');
			urlString = SmartCampusUrlUtils.getFamilyCheckHomeworkUrl(studentId, String.valueOf(homeworkId),"1",String.valueOf(fileId),String.valueOf(fileIdparam));
		}else if(fileIds!=null&&fileIds.size()>0&&fileIdparams.size()==0){
			fileId=listToString(fileIds,',');
			urlString = SmartCampusUrlUtils.getFamilyCheckHomeworkUrl(studentId, String.valueOf(homeworkId),"1",String.valueOf(fileId),null);
		}else if(fileIds.size()>0&&fileIdparams!=null&&fileIdparams.size()>0){
			fileIdparam=listToString(fileIdparams, ',');
			urlString = SmartCampusUrlUtils.getFamilyCheckHomeworkUrl(studentId, String.valueOf(homeworkId),"1",null,String.valueOf(fileIdparam));
		}else{
			urlString = SmartCampusUrlUtils.getFamilyCheckHomeworkUrl(studentId, String.valueOf(homeworkId),"1",null,null);
		}
//		String urlString = SmartCampusUrlUtils.getFamilyCheckHomeworkUrl(studentId, String.valueOf(familyHomeworkBeans.get(position).hId),"1");

		
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
								takePhotoPopWin.dismiss();
								if(mFilePath!=null){
									deleteAllFiles(new File(mFilePath));
								}
								Toast.makeText(FamilyHomeworkActivity.this, "反馈成功",
										Toast.LENGTH_SHORT).show();
//								familyHomeworkBeans.get(position).isCheck=true;
								homeworkAdapter.notifyDataSetChanged();
								getCorrectHomeworkStudentList(dateParam, studentId);
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(FamilyHomeworkActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(FamilyHomeworkActivity.this, response.getString("msg"),
										Toast.LENGTH_LONG).show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(FamilyHomeworkActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	//图片filed 集合转为以逗号相隔的字符串
		public String listToString(List<String> list, char separator) {   
			StringBuilder sb = new StringBuilder();    
				for (int i = 0; i < list.size(); i++) { 
						
					sb.append(list.get(i)).append(separator);   
						
					}   
				return sb.toString().substring(0,sb.toString().length()-1);
		}
	public  void showPopWin(ArrayList<FeedbackWorkBean> savemages, String saveRemarkString) {
		// TODO Auto-generated method stub
		homework_main=(RelativeLayout) findViewById(R.id.family_rl);
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
				saveRemarkImages=takePhotoPopWin.getRemarkImages();
				saveRemarkStr = takePhotoPopWin.getRemarkText();
			}
		});
	
	}
	
	private PopOnClickListener popOnClickListener = new PopOnClickListener() {

		@Override
		public void itemClick(int position) {// 图片预览
			Intent intent = new Intent(FamilyHomeworkActivity.this, ImageBrowerActivity.class);
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
			FamilyHomeworkActivity.this.startActivityForResult(intent, 2);
		}

		@Override
		public void lastItemtemClick() {// 添加图片
			Intent intent = new Intent(FamilyHomeworkActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 14);
			FamilyHomeworkActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void addImgClick(int position) {// 添加图片图标
			Intent intent = new Intent(FamilyHomeworkActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 15);
			FamilyHomeworkActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void awardClick() {

			remarkUpLoadRecords.clear();
			recordTimeList.clear();
			remarkUpLoadImages.clear();
			pictureMaterialIds.clear();
			if (takePhotoPopWin.getRemarkImages() != null && takePhotoPopWin.getRemarkImages().size() > 2) {
				parent_remark = takePhotoPopWin.getRemarkText();
				remarkImages = takePhotoPopWin.getRemarkImages();

				for (int i = 0; i < remarkImages.size(); i++) {
					if(remarkImages.get(i).fileType==2){//语音
						if(remarkImages.get(i).isLocal==false){
							pictureMaterialIds.add(remarkImages.get(i).fileId+"_172");
						}else{
							remarkUpLoadRecords.add(remarkImages.get(i).fileUrl);
						}
						recordTimeList.add(remarkImages.get(i).audioLength);
					}else{//图片
						if(remarkImages.get(i).isLocal==false){
							pictureMaterialIds.add(remarkImages.get(i).fileId+"_166");
						}else{
							remarkUpLoadImages.add(remarkImages.get(i).fileUrl);
						}
						
					}
				}
				showLoadDialog();
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
//						sendImages(remarkImages);
						if(remarkUpLoadImages.size()>2){
							isExistImg=1;
							ImgUploadUtil.uploadImg(FamilyHomeworkActivity.this,remarkUpLoadImages,new UploadImgListener() {
								
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
									Toast.makeText(FamilyHomeworkActivity.this, errorStr, Toast.LENGTH_SHORT).show();
									hideLoadDialog();
								}
							});
						}
				       if(remarkUpLoadRecords.size()>0){
				    	   isExistRecord=1;
				    	   RecordUploadUtil.uploadRecord(FamilyHomeworkActivity.this,remarkUpLoadRecords, new UploadImgListener(){

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
									Toast.makeText(FamilyHomeworkActivity.this, errorStr, Toast.LENGTH_SHORT).show();
									hideLoadDialog();
								}
								
							});
				       }
						
					}
				}, 200);
				
				if(remarkUpLoadRecords.size()==0 && remarkUpLoadImages.size()==2){
					if (takePhotoPopWin.getRemarkText() != null && !takePhotoPopWin.getRemarkText().trim().equals("")) {
						parent_remark = takePhotoPopWin.getRemarkText();
						
					}
					checkHomework(mPosition,homeworkId, pictureMaterialIds, parent_remark,fileIdparamsList);
				}
				
				
				
			} else {
				if (takePhotoPopWin.getRemarkText() != null && !takePhotoPopWin.getRemarkText().trim().equals("")) {
					parent_remark = takePhotoPopWin.getRemarkText();
					checkHomework(mPosition,homeworkId, pictureMaterialIds, parent_remark,fileIdparamsList);
//					AddNewHomework(teacherId,courseName,description,pictureMaterialIds,fileIdparamsList);
//					getCourseName(teacherId, 2);
					//awardBadge(1);
				} else {
//					if (issuedId != 0) {
					showToast("反馈信息不能为空");
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
						checkHomework(mPosition,homeworkId, pictureMaterialIds,parent_remark, fileIdparamsList);
						uploadStatus=0;
						tempUploadStaus=0;
						remarkUpLoadRecords.clear();
						remarkUpLoadImages.clear();
					}
				}else if(remarkUpLoadImages.size()>2&&remarkUpLoadRecords.size()==0){
						//语图片仅存在
						checkHomework(mPosition,homeworkId, pictureMaterialIds, parent_remark,fileIdparamsList);
						remarkUpLoadRecords.clear();
						remarkUpLoadImages.clear();
						uploadStatus=0;
						tempUploadStaus=0;
				}else if(remarkUpLoadImages.size()==2&&remarkUpLoadRecords.size()>0){//仅存在语音
						checkHomework(mPosition,homeworkId, pictureMaterialIds, parent_remark,fileIdparamsList);
						remarkUpLoadRecords.clear();
						remarkUpLoadImages.clear();
						uploadStatus=0;
						tempUploadStaus=0;
				}
			}
		}
	};
	private void showLoadDialog(){
		if( progressDialog == null || !progressDialog.isShowing()){
			progressDialog = ProgressDialog.show(FamilyHomeworkActivity.this, "", "...loading...");
		}
	}
	
	private void hideLoadDialog(){
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
			
		}
	}
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
	private Toast mToast;  
    public void showToast(String text) {  
    	Toast.makeText(FamilyHomeworkActivity.this, text, Toast.LENGTH_SHORT).show();
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
