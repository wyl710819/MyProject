package com.routon.smartcampus.homework;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.pictureAdd.PictureAddActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.DataResponse;
import com.routon.edurelease.R;
import com.routon.smartcampus.MyLoginActivity;
import com.routon.smartcampus.flower.PopOnClickListener;
import com.routon.smartcampus.homework.ClassAdapter.ClassOnClickListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.ImgUploadUtil;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.utils.RecordUploadUtil;
import com.routon.smartcampus.utils.UploadImgListener;
import com.routon.smartcampus.view.WeekCalendarView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.view.WindowManager.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.routon.common.BaseActivity;

public class HomeworkActivity extends BaseActivity {
	private static String TAG = "HomeworkActivity";
	
	private ClassAdapter classAdapter;
	private ListView lv_class;

	private AddHomeworkPopWin takePhotoPopWin;
	private String editStr = null;
	private String titleNextBtnText="布置作业";
	private ProgressDialog progressDialog;
	private ProgressDialog progressDialogCourse;
	private ArrayList<FeedbackWorkBean> remarkImages = new ArrayList<FeedbackWorkBean>();
	private ArrayList<String> remarkUpLoadRecords=new ArrayList<String>();
	private ArrayList<String> remarkUpLoadImages=new ArrayList<String>();
	private List<String> pictureMaterialIds = new ArrayList<String>();
	private ArrayList<String> imgDatas = new ArrayList<String>();
	private RelativeLayout homework_main;
	private LayoutParams params;
	private int teacherId;
	private String courseName=null;
	private List<QueryClassHomeworkBean> classHomeworkList =new ArrayList<QueryClassHomeworkBean>();
	private ArrayList<FeedbackWorkBean> saveRemarkImages=new ArrayList<FeedbackWorkBean>();//保存已经添加未布置的图片和文字
	private String saveRemarkStr=null;
	public static String classTitle;
	public static final int UPLOADSTATUS=0;
	private int uploadStatus=0;//是否完成上传标识
	private int tempUploadStaus=0;
	private int isExistImg=0;//0不存在1存在
	private int isExistRecord=0;
	private String courses;//获取的课程名称，字符串
	private String course[];//存放课程数组
	private String description = "";
	private int homeworkId;
	private String classIdTemp;//班级id临时存放
	private String currDay;
	private String dateParam;
	private String dateParamSecond;//带时分秒日期
	private ImageView back_menu;
	private ArrayList<FeedbackWorkBean>imgList=new ArrayList<FeedbackWorkBean>();
	private ArrayList<FeedbackWorkBean>recordList=new ArrayList<FeedbackWorkBean>();
	private List<String>fileIdparamsList=new ArrayList<String>();
	private List<Integer>recordTimeList=new ArrayList<Integer>();
	
	//录音文件保存位置
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
	// private boolean isStart;// 是否是交接的月初
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.homework_main);
		teacherId=InfoReleaseApplication.authenobjData.userId;
		Log.d(TAG,"老师id:"+teacherId);
		initView();
		initData();
		
		
//		gestureDetector = new GestureDetector(this);
//		
//		dateAdapter = new DateAdapter(this, currentYear, currentMonth,
//				currentWeek, currentWeek == 1 ? true : false);
//		addGridView();
//		dayNumbers = dateAdapter.getDayNumbers();
//		gridView.setAdapter(dateAdapter);
//		selectPostion = dateAdapter.getTodayPosition();
//		gridView.setSelection(selectPostion);
//		flipper1.addView(gridView, 0);
//		this.setTouchUnDealView(flipper1);
		
		
	}
	
	private void initView() {
		
		lv_class=(ListView) findViewById(R.id.lv_class);
		back_menu = (ImageView) findViewById(R.id.back_btn);
		next_btn = (ImageView) findViewById(R.id.next_btn);
		
		TextView title_view=(TextView) findViewById(R.id.title_view);
		title_view.setText("作业");
		this.setTouchUnDealView(lv_class);
		
		WeekCalendarView weekCalendarView = (WeekCalendarView) findViewById(R.id.weekCalendarView);
		this.setTouchUnDealView(weekCalendarView);
		weekCalendarView.setOnChangeListener(new WeekCalendarListener() {

			@Override
			public void WeekCalendarClickListener(String dateStr) {
				dateParam = dateStr;
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date dateTemp=null;
				try {
					dateTemp = formatter.parse(dateParam);
					dateParam=formatter.format(dateTemp);
//					Log.d(TAG,"日期:"+dateParam);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				getClassHomeworkList(teacherId,dateParam);
			}
		});
		
		RelativeLayout.LayoutParams nextParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		nextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE); 
		next_btn.setLayoutParams(nextParams);
		
		next_btn.setImageResource(R.drawable.edit_red_icon);
		next_btn.setVisibility(View.VISIBLE);
		next_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showPopupMenu(v);
			}
		});
		
		
//		mBackListener = new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				HomeworkActivity.this.finish();
//				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
//			}
//		};
		back_menu.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				HomeworkActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		});
		
		lv_class.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(classHomeworkList.get(position).hId!=0){
					
					
				}else {
					if(courseName==null){
						showToast("您没有布置作业权限");
						return;
					}else{
						getGradeHomeworkList(teacherId, classHomeworkList.get(position).gradeId, classHomeworkList.get(position).classId, position);
					}
					
					
				}
				//跳转布置作业页面
				
//				Toast.makeText(getApplicationContext(), classList.get(position).toString(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void initData() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		dateParam = sdf.format(date);
		currDay=sdf.format(date);
		getCourseName(teacherId,2);
		getClassListData();
		if (Build.VERSION.SDK_INT > 22) {
            permissionForM();
        }
		
		
		
		rateStrList = new ArrayList<String>();
    	rateStrList.add("A、B、C、D");
    	rateStrList.add("A+、A、B+、B");
    	rateStrList.add("甲、乙、丙、丁");
    	rateStrList.add("优秀、良好、合格、欠佳");
		
		sharedPrefrences = this.getSharedPreferences("homewprkRate", Context.MODE_PRIVATE);
		homewprkRateStr = sharedPrefrences.getString("rateStr", "A、B、C、D、未完成");
		homewprkRateStr=homewprkRateStr.substring(0, homewprkRateStr.length()-4);
		
	}
    
	/**
	 * 查询班级列表
	 * */
	private void getClassListData() { 
		
    	showMyProgressDialog();
    	GroupListData.getClassListData(HomeworkActivity.this, new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub		 
				 
				if( classGroups.size() != 0 ){
					for(int i = 0;i < classGroups.size(); i++){
						QueryClassHomeworkBean bean=new QueryClassHomeworkBean();
						bean.classId=String.valueOf(classGroups.get(i).getId());
						bean.className=classGroups.get(i).getName();
						bean.gradeId=String.valueOf(classGroups.get(i).getPid());
						
						classHomeworkList.add(bean);
				}
					//Log.d(TAG,classIdList.size()+"");
					classAdapter=new ClassAdapter(HomeworkActivity.this,classHomeworkList);
					Log.d(TAG,classHomeworkList.size()+"");
					classAdapter.setClassListener(new ClassOnClickListener() {
						
						

						@Override
						public void clickModify(View v, final int position) {
							// TODO Auto-generated method stub
//							Toast.makeText(HomeworkActivity.this, "你点击了删除"+classHomeworkList.get(position).hId, Toast.LENGTH_SHORT).show();
							final AlertDialog.Builder normalDialog = 
						            new AlertDialog.Builder(HomeworkActivity.this);
						        
						        normalDialog.setMessage("您确定要删除该作业吗?");
						        normalDialog.setPositiveButton("确定", 
						            new DialogInterface.OnClickListener() {
						            @Override
						            public void onClick(DialogInterface dialog, int which) {
						                //...To-do
						            	deleteAssignHomework(classHomeworkList.get(position).classId, classHomeworkList.get(position).hId, dateParam);
						            }
						        });
						        normalDialog.setNegativeButton("取消", 
						            new DialogInterface.OnClickListener() {
						            @Override
						            public void onClick(DialogInterface dialog, int which) {
						                //...To-do
						            	
						            }
						        });
						        // 显示
						        normalDialog.show();
							
							
							
						}

						@Override
						public void clickCorrect(View v, int position) {
							// TODO Auto-generated method stub
//							Toast.makeText(HomeworkActivity.this, "你点击了批改"+position, Toast.LENGTH_SHORT).show();
							
							Intent intent=new Intent(HomeworkActivity.this, CorrectHomeworkActivity.class);
							intent.putExtra(MyBundleName.HOMEWORK_CLASS_ID, classHomeworkList.get(position).classId);
							intent.putExtra(MyBundleName.HOMEWORK_ID, classHomeworkList.get(position).hId);
							intent.putExtra(MyBundleName.HOMEWORK_CLASS_NAME,classHomeworkList.get(position).className);
							
							startActivity(intent);
						}
					});
					lv_class.setAdapter(classAdapter);
					//查询日期班级
					getClassHomeworkList(teacherId  , dateParam);
					

                }else{
                	showToast("班级列表为空");
                }
				hideMyProgressDialog(); 
			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideMyProgressDialog();
			}
		});  
	}
	
	
	private void showMyProgressDialog(){
		if( progressDialog == null || !progressDialog.isShowing()){
			progressDialog = ProgressDialog.show(HomeworkActivity.this, "", "...loading...");
		}
	}
	
	private void hideMyProgressDialog(){
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	private void showMyProgressDialogCourse(){
		if( progressDialogCourse == null || !progressDialogCourse.isShowing()){
			progressDialogCourse = ProgressDialog.show(HomeworkActivity.this, "", "...loading...");
		}
	}
	
	private void hideMyProgressDialogCourse(){
		if (progressDialogCourse != null && progressDialogCourse.isShowing()) {
			progressDialogCourse.dismiss();
			progressDialogCourse = null;
		}
	}
	/**
	 * 查询老师对应课程信息
	 * */
	
	private void getCourseName(int terUserId  , int type) {

				String urlString=SmartCampusUrlUtils.getCourseNameUrl(String.valueOf(terUserId), String.valueOf(type));
				

				showMyProgressDialogCourse();
				CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
						new Response.Listener<JSONObject>() {
							@Override
							public void onResponse(JSONObject response) {
								Log.d(TAG, "response=" + response);
								hideMyProgressDialogCourse();
								try {
									if (response.getInt("code") == 0) {
										
										JSONObject jobj = response.getJSONObject("datas");
										
									   courses=jobj.optString("courses").trim();
									   courseName = courses;
									   /* if (courses==null|| courses.trim().equals("")||courses.length()==0) {
									    	showToast("当前老师未关联课程!");
									    	if (takePhotoPopWin!=null && takePhotoPopWin.isShowing()) {
									    		takePhotoPopWin.dismiss();
											}
									    	hideMyProgressDialogCourse();
											return;
										}
									    course=courses.split(",");  
								        int maxSplit=5;  
								       
								        course=courses.split(",",maxSplit);  
//								        for(int j=0;j<course.length;j++){
//								        	Log.d(TAG,course[j]+"课程");
//								        }
								      
								       
								        	for (int i = 0; i < course.length; i++) {  
										           if(course[i].trim().equals("语文")||course[i].trim().equals("数学")||
										        		   course[i].trim().equals("英语")||course[i].trim().equals("物理")||course[i].trim().equals("化学")
										        		   ||course[i].trim().equals("语言与阅读")||course[i].trim().equals("数学与思维")){
										        	   courseName=course[i].trim();
										        	   String name=courseName;
										        	   Log.d(TAG,name);
										        	   hideMyProgressDialogCourse();
										        	   //AddNewHomework(teacherId,course[i],description,pictureMaterialIds);
										        	   return;
										           }else{
										        	   courseName=null;
										        	   hideMyProgressDialogCourse();
										           }
//										        	   showToast("您没有布置作业权限!");
//										        	   if (takePhotoPopWin!=null && takePhotoPopWin.isShowing()) {
//												    		takePhotoPopWin.dismiss();
//														}
//										           }
										           hideMyProgressDialogCourse();
										          
										        } */
								       

									} else if (response.getInt("code") == -2) {
										hideMyProgressDialogCourse();
										InfoReleaseApplication.returnToLogin(HomeworkActivity.this);
									
									} else {
										hideMyProgressDialogCourse();
										Log.e(TAG, response.getString("msg"));
										showToast (response.getString("msg"));
											
									}

								} catch (JSONException e) {
									hideMyProgressDialogCourse();
									e.printStackTrace();
								}

							}
						}, new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError arg0) {
								Log.e(TAG, "sorry,Error");
								if( InfoReleaseApplication.showNetWorkFailed(HomeworkActivity.this) == true ){
									showToast("查询课程息失败!");
								}
								hideMyProgressDialogCourse();
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
			 
			//showProgressDialog();

			CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "response=" + response);
							try {
								if (response.getInt("code") == 0) {
									if(mFilePath!=null){
										deleteAllFiles(new File(mFilePath));
									}
									JSONObject obj = response.getJSONObject("datas");
									pictureMaterialIds.clear();
									homeworkId=Integer.parseInt(obj.getString("hId"));
									 Date dateTemp = new Date();  
									 SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");  
									 dateParamSecond = dateParam+" "+sdf.format(dateTemp); 
									 Log.d(TAG,dateParam);
									//调布置作业接口
									AssingHomework(classIdTemp, homeworkId,dateParamSecond);
								
								} else if (response.getInt("code") == -2) {
									InfoReleaseApplication.returnToLogin(HomeworkActivity.this);
								} else {
									Log.e(TAG, response.getString("msg"));
									showToast(response.getString("msg"));

								}

							} catch (JSONException e) {
								e.printStackTrace();
								showToast("系统异常");
							}

						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.e(TAG, "sorry,Error");
							if( InfoReleaseApplication.showNetWorkFailed(HomeworkActivity.this) == true ){
								showToast("新建作业失败!");
							}
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

		}
	/**
	 * 布置作业接口
	 * */
		
		private void AssingHomework(String classId , Integer homeworkId,String dateTime) {
	        
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("dateTime", dateTime));
	        String urlString=null;
			
			urlString=SmartCampusUrlUtils.getAssingHomeworkUrl( String.valueOf(classId), String.valueOf(homeworkId),String.valueOf(dateTime));

			
			showMyProgressDialog();

			CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "response=" + response);
							hideMyProgressDialog();
							try {
								if (response.getInt("code") == 0) {

									showToast("布置作业成功!");
									 takePhotoPopWin.dismiss();
									 getClassHomeworkList(teacherId, dateParam);
									 classAdapter.notifyDataSetChanged();
//									mAdaptor.notifyDataSetChanged();
								} else if (response.getInt("code") == -2) {

									InfoReleaseApplication.returnToLogin(HomeworkActivity.this);
									
								} else {
									Log.e(TAG, response.getString("msg"));
									showToast(response.getString("msg"));

								}

							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.e(TAG, "sorry,Error");
							if( InfoReleaseApplication.showNetWorkFailed(HomeworkActivity.this) == true ){
								showToast( "布置作业失败!");
							}
							
							hideMyProgressDialog();

						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

		} 
	/**
	 * 删除布置作业接口 
	 *
	 * */
	private void deleteAssignHomework(String classId,int homeworkId,String dateTime){
		
		
		
		String urlString=SmartCampusUrlUtils.getDeleteAssignHomeworkUrl(String.valueOf(classId),String.valueOf(homeworkId),String.valueOf(dateTime));
		showMyProgressDialog();
		CookieJsonRequest  jsonObjectRequest=new CookieJsonRequest(Request.Method.POST,urlString,null,
				new Response.Listener<JSONObject>() {
				
					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						Log.d(TAG, "response=" + response);
						//hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								getClassHomeworkList(teacherId, dateParam);
								showToast("作业删除成功!");
								classAdapter.notifyDataSetChanged();
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(HomeworkActivity.this);
								hideMyProgressDialog();
								
							} else {
								Log.e(TAG, response.getString("msg"));
								showToast(response.getString("msg"));
								hideMyProgressDialog();

							}
				     	   
						} catch (JSONException e) {
							e.printStackTrace();
			     		
					}
				 }
			
				}, new Response.ErrorListener(){


					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						
						if( InfoReleaseApplication.showNetWorkFailed(HomeworkActivity.this) == true ){
							showToast("删除作业失败!");
						}
						hideMyProgressDialog();

					}
				
					
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
    /**
     * 按日期查询班级
     * */
	
    private void getClassHomeworkList(int teacherId  , String dateTime) {
    	
    	
			String urlString=SmartCampusUrlUtils.getClassHomeworkQueryUrl(String.valueOf(teacherId), String.valueOf(dateTime));
			showMyProgressDialog();
			CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
					new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
					Log.d(TAG, "response=" + response);
					hideMyProgressDialog();
					try {
						if (response.getInt("code") == 0) {
										
//						classHomeworkList.clear();
						Log.d(TAG,classHomeworkList.size()+"作业列表");				
						JSONArray jsonArray = response.optJSONArray("datas");
						if(jsonArray==null){
							for (int j = 0; j < classHomeworkList.size(); j++) {
								classHomeworkList.get(j).hId=0;
								classHomeworkList.get(j).description="";
								classHomeworkList.get(j).imgClassList.clear();
								classHomeworkList.get(j).resourseList.clear();
							}
							if(classAdapter!=null){
								classAdapter.notifyDataSetChanged();
							}
							
						}else{
							   
								int len = jsonArray.length();
								List<QueryClassHomeworkBean> homeworkList =new ArrayList<QueryClassHomeworkBean>();
								
									for(int i = 0; i< len; i++){
										JSONObject obj = (JSONObject) jsonArray.get(i);
										//作业
										QueryClassHomeworkBean bean=new QueryClassHomeworkBean(obj);
										//学生列表
										homeworkList.add(bean);
										}
									if(homeworkList.size()>0){
										
											for (int j = 0; j < classHomeworkList.size(); j++) {
												boolean isTag=false;
												classHomeworkList.get(j).imgClassList.clear();
												classHomeworkList.get(j).resourseList.clear();
												for (int i = 0; i < homeworkList.size(); i++) {
												
												if (classHomeworkList.get(j).classId.equals(homeworkList.get(i).classId)) {
													classHomeworkList.get(j).hId=homeworkList.get(i).hId;
													classHomeworkList.get(j).description=homeworkList.get(i).description;
													classHomeworkList.get(j).imgClassList.addAll(homeworkList.get(i).imgClassList);
													classHomeworkList.get(j).resourseList.addAll(homeworkList.get(i).resourseList);
													Log.d(TAG,classHomeworkList.get(j).imgClassList.size()+"图片");
													classAdapter.notifyDataSetChanged();
													isTag=true;
												}
											}
												if(!isTag){
													
													classHomeworkList.get(j).hId=0;
													classHomeworkList.get(j).description="";
													classHomeworkList.get(j).imgClassList.clear();
													classHomeworkList.get(j).resourseList.clear();
													isTag=false;
												}
										  }
									}
									
									
									
//									    Log.d(TAG,classHomeworkList.size()+"作业列表");
									if(classAdapter!=null){
										classAdapter.notifyDataSetChanged();
									}
								}

									} else if (response.getInt("code") == -2) {
	
										InfoReleaseApplication.returnToLogin(HomeworkActivity.this);
										
									} else {
										
										
										Log.e(TAG, response.getString("msg"));
										showToast(response.getString("msg"));
	
									}

							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.e(TAG, "sorry,Error");
							
							if( InfoReleaseApplication.showNetWorkFailed(HomeworkActivity.this) == true ){
								showToast("按日期查询作业失败!");
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
	
	private void getGradeHomeworkList(int teacherId , String gradeId,String classId,final int position) {

		String urlString=SmartCampusUrlUtils.getHomeworkQueryUrl(String.valueOf(teacherId), gradeId,classId);
		
		showMyProgressDialog();

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								
							   JSONArray   optArray = response.optJSONArray("datas");
							    if(optArray==null){
//										if(dateFormat.parse(currDay).getTime()<=dateFormat.parse(dateParam).getTime()){
										showPopWin(saveRemarkImages,saveRemarkStr);
										classIdTemp=classHomeworkList.get(position).classId;
//										}
//									else {
//											Toast.makeText(getApplicationContext(), "不能布置今天以前日期作业!", Toast.LENGTH_SHORT).show();
//										}
									
								}else {
									
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
									
//										if(dateFormat.parse(currDay).getTime()<=dateFormat.parse(dateParam).getTime()){
									Intent intent=new Intent(HomeworkActivity.this,AssignmentActivity.class);
									intent.putExtra("courseName", courseName);
									intent.putExtra("className", classHomeworkList.get(position).className);
									intent.putExtra("classId", classHomeworkList.get(position).classId);
									intent.putExtra("gradeId", classHomeworkList.get(position).gradeId);
									intent.putExtra("dateParam", dateParam);
									intent.putParcelableArrayListExtra("gradeHomeWorkList", gradeHomeWorkList);
//									Log.d(TAG,"当前日期:"+dateParam);
									HomeworkActivity.this.startActivityForResult(intent,3);
//										}
//										else {
//											Toast.makeText(getApplicationContext(), "不能布置今天以前日期作业!", Toast.LENGTH_SHORT).show();
//										}
								}
								
							}
								else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(HomeworkActivity.this);
								
							} else {
								Log.e(TAG, response.getString("msg"));
								showToast(response.getString("msg"));

							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						if( InfoReleaseApplication.showNetWorkFailed(HomeworkActivity.this) == true ){
							showToast("查询年级作业失败!");
						}
						hideMyProgressDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	
	public  void showPopWin(ArrayList<FeedbackWorkBean> savemages, String saveRemarkString) {
		// TODO Auto-generated method stub
		homework_main=(RelativeLayout) findViewById(R.id.homework_main);
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
			Intent intent = new Intent(HomeworkActivity.this, ImageBrowerActivity.class);
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
			HomeworkActivity.this.startActivityForResult(intent, 2);
		}

		@Override
		public void lastItemtemClick() {// 添加图片
			Intent intent = new Intent(HomeworkActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 14);
			HomeworkActivity.this.startActivityForResult(intent, 1);
		}

		@Override
		public void addImgClick(int position) {// 添加图片图标
			Intent intent = new Intent(HomeworkActivity.this, PictureAddActivity.class);
			intent.putExtra("img_count", takePhotoPopWin.imgList.size());
			intent.putExtra(CommonBundleName.FILE_TYPE_TAG, 15);
			HomeworkActivity.this.startActivityForResult(intent, 1);
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
							ImgUploadUtil.uploadImg(HomeworkActivity.this,remarkUpLoadImages,new UploadImgListener() {
								
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
									Toast.makeText(HomeworkActivity.this, errorStr, Toast.LENGTH_SHORT).show();
									hideMyProgressDialog();
								}
							});
						}
				       if(remarkUpLoadRecords.size()>0){
				    	   isExistRecord=1;
				    	   RecordUploadUtil.uploadRecord(HomeworkActivity.this,remarkUpLoadRecords, new UploadImgListener(){

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
									Toast.makeText(HomeworkActivity.this, errorStr, Toast.LENGTH_SHORT).show();
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

	private ImageView next_btn;

	private SharedPreferences sharedPrefrences;

	private List<String> rateStrList;

	private String homewprkRateStr;
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
			}else if(requestCode==3){
				//getClassListData();
				getClassHomeworkList(teacherId, dateParam);
			}
		  }
		}
	
//	private Toast mToast;  
    public void showToast(String text) {    
//        if(mToast == null) {    
//            mToast = Toast.makeText(HomeworkActivity.this, text, Toast.LENGTH_SHORT);    
//        } else {    
//            mToast.setText(text);      
//            mToast.setDuration(Toast.LENGTH_SHORT);    
//        }    
//        mToast.show();    
        Toast.makeText(HomeworkActivity.this, text, Toast.LENGTH_SHORT).show();
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
    //动态申请录音权限
    private void permissionForM() {
        if (ContextCompat.checkSelfPermission(HomeworkActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(HomeworkActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeworkActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        } 

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

    	 switch (requestCode) {  
	      case 0: {  
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
			        	if( permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
			        		Toast.makeText(HomeworkActivity.this, "读写权限申请失败，应用有些功能可能无法正常使用", Toast.LENGTH_SHORT).show();
			         	}else if( permissions[i].equals(Manifest.permission.RECORD_AUDIO)){
			        		Toast.makeText(HomeworkActivity.this, "麦克风权限申请失败，应用有些功能可能无法正常使用", Toast.LENGTH_SHORT).show();
			         	}
	        	    }
	           }
	       }
	      }
    	}
    }
    
    public int sp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
	}
    
//    @SuppressLint("NewApi")
	private void showPopupMenu(View v) {
    	
    	
    	
    	final Editor editor = sharedPrefrences.edit();//获取编辑器
		
    	
		PopupMenu popup = new PopupMenu(HomeworkActivity.this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.homework_rate_menu, popup.getMenu());
		
		Menu menu = popup.getMenu();
		
		for (int i = 0; i < rateStrList.size(); i++) {
			SpannableStringBuilder style=new SpannableStringBuilder(rateStrList.get(i));     	
	        Log.e("homewprkRateStr", homewprkRateStr);
	        if (homewprkRateStr!=null && rateStrList.get(i).equals(homewprkRateStr)) {
	        	style.setSpan(new ForegroundColorSpan(HomeworkActivity.this.getResources()
	    				.getColor(R.color.homework_green)),0,rateStrList.get(i).length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//	        	style.setSpan(new AbsoluteSizeSpan(sp2px(i==rateStrList.size()-1 ? 14 : 18)),0,rateStrList.get(i).length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
			}else {
//				 style.setSpan(new AbsoluteSizeSpan(sp2px(i==rateStrList.size()-1 ? 14 : 18)),0,rateStrList.get(i).length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
			}
	        menu.findItem(menu.getItem(i).getItemId()).setTitle(style);
		}
		
		
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Log.e("rateStr",item.getItemId()+"====="+R.id.rate1);
				
				switch (item.getItemId()) {
				case R.id.rate1:
					homewprkRateStr=rateStrList.get(0);
					editor.putString("rateStr",rateStrList.get(0)+"、未完成");
					editor.commit();
					break;
				case R.id.rate2:
					homewprkRateStr=rateStrList.get(1);
					editor.putString("rateStr",rateStrList.get(1)+"、未完成");
					editor.commit();
					break;
				case R.id.rate3:
					homewprkRateStr=rateStrList.get(2);
					editor.putString("rateStr",rateStrList.get(2)+"、未完成");
					editor.commit();
					break;
				case R.id.rate4:
					homewprkRateStr=rateStrList.get(3);
					editor.putString("rateStr",rateStrList.get(3)+"、未完成");
					editor.commit();
					break;

				default:
					break;
				}
				
				
				return false;
			}
		});
		
		popup.show();
	}
    public int dp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}
    
}