package com.routon.smartcampus.schoolcompare;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.homework.WeekCalendarListener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.schoolcompare.ClassCompareBean.SubprojectScoreBean;
import com.routon.smartcampus.view.WeekCalendarView;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class ClassCompareActivity extends BaseActivity implements OnClickListener {

	private String dateParam;
	private ListView gradeListView;
	private ArrayList<List<ClassCompareBean>> gradeList;
	private static String TAG = "ClassCompareActivity";
	private ProgressDialog progressDialog;
	public static CompareClassTypeBean classTypeBean;
	private boolean isCompareFinish = false;
	private boolean nextButShow=false;
	private List<ClassCompareBean> classCompareBeanList=new ArrayList<ClassCompareBean>();
	private int ratingMode;//1先选取班级，2先选取评分项
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_compare_layout);

		classTypeBean = (CompareClassTypeBean) getIntent().getSerializableExtra("classTypeBean");
		ratingMode = classTypeBean.ratingMode;
		initView();
		initData();
	}

	private void initView() {
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ClassCompareActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};

		ImageView backBut = (ImageView) findViewById(R.id.back_btn);
		TextView titleView = (TextView) findViewById(R.id.title_view);
		ImageView nextBtn = (ImageView) findViewById(R.id.next_btn);
		gradeListView = (ListView) findViewById(R.id.lv_grade_compare_listview);

		WeekCalendarView weekCalendarView = (WeekCalendarView) findViewById(R.id.weekCalendarView);
		weekCalendarView.updateAdapter(classTypeBean.ratingTime,true);
		this.setTouchUnDealView(weekCalendarView);
		backBut.setOnClickListener(mBackListener);
		titleView.setText(classTypeBean.name);
		nextBtn.setVisibility(View.VISIBLE);
		nextBtn.setOnClickListener(this);

		weekCalendarView.setOnChangeListener(new WeekCalendarListener() {

			@Override
			public void WeekCalendarClickListener(String dateStr) {
				
				
				dateParam=dateFormChange(dateStr);
				getClassCompareData(classTypeBean.id,dateParam,ratingMode);
				
			}

		});
		
		int userId = InfoReleaseApplication.authenobjData.userId;
		
		for (int i = 0; i < classTypeBean.subprojectBeanList.size(); i++) {
			if (useList(classTypeBean.subprojectBeanList.get(i).userIds,userId)) {
				nextButShow = true;
			}
			
		}

	}
	
	private String  dateFormChange(String dateStr) {
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-M-d");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = sdf.parse(dateStr, pos);
		SimpleDateFormat sdf2= new SimpleDateFormat("yyyy-MM-dd");
		return sdf2.format(strtodate);
	}

	private void initData() {
		//此用户任班主任的班级id数组
		String[]  headTeacherClasses=InfoReleaseApplication.authenobjData.headTeacherClasses;
		headTeacherClassIds = Arrays.asList(headTeacherClasses);
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		dateParam = sdf.format(date);
		noncedateParam = sdf.format(date);
		
//		getTestData();
		getClassCompareData(classTypeBean.id,dateParam,ratingMode);
	}


	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.next_btn:
			showPopupMenu(v);
			break;

		default:
			break;
		}

	}

	private void showPopupMenu(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		if (isCompareFinish) {
			inflater.inflate(R.menu.compare_history_query, popup.getMenu());
		}else {
			if (!nextButShow) {
				inflater.inflate(R.menu.compare_history_query, popup.getMenu());
			}else {
				inflater.inflate(R.menu.school_compare_menu, popup.getMenu());
			}
			
		}
		

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {// 完成评比
				if (item.getItemId() == R.id.finish_compare) {
					showConfirmDialog();
					
				} else if (item.getItemId() == R.id.history_query) {// 历史成绩查询
					Intent intent=new Intent(ClassCompareActivity.this, CompareScoreQueryActivity.class);
					classTypeBean.classCompareBeanList=classCompareBeanList;
					intent.putExtra("classTypeBean", classTypeBean);
					startActivity(intent);
				} else if (item.getItemId() == R.id.semester_query) {// 学期总评
					Intent intent=new Intent(ClassCompareActivity.this, HistoryQueryActivity.class);
					intent.putExtra("ratingId", classTypeBean.id);
					startActivity(intent);
				}
				return false;
			}

		});
		popup.show();
	}

	private void getClassCompareData(String id, String param, final int ratingMode) {

		 String urlString = SmartCampusUrlUtils.getSchoolRatingQueryUrl(id,param);

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
								
								
								JSONObject jsonObject = response.getJSONObject("datas");
								int finished=jsonObject.optInt("finished");
								if (finished==1) {
									isCompareFinish=true;
								}else {
									isCompareFinish=false;
								}
								List<ClassCompareBean> classCompareBeanList = new ArrayList<ClassCompareBean>();

								ArrayList<SubprojectBean> ratingItems = new ArrayList<SubprojectBean>();
 								JSONArray itemsJsonArray = jsonObject.getJSONArray("items");
								for(int i=0; i<itemsJsonArray.length(); i++){
									JSONObject obj = itemsJsonArray.getJSONObject(i);
									SubprojectBean bean = new SubprojectBean(obj);
									ratingItems.add(bean);
								}																

								JSONArray jsonArray = jsonObject.getJSONArray("groups");
								for (int i = 0; i < jsonArray.length(); i++) {

									JSONObject obj = (JSONObject) jsonArray.get(i);
									ClassCompareBean bean = new ClassCompareBean(obj);
									
									ArrayList<SubprojectBean> subprojectList=new ArrayList<SubprojectBean>();
									for (int j = 0; j < ratingItems.size(); j++) {
										SubprojectBean sBean=ratingItems.get(j);
										SubprojectBean subprojectBean=new SubprojectBean();
										subprojectBean.id=sBean.id;
										subprojectBean.score=-9999;
										subprojectBean.maxScore=sBean.maxScore;
										subprojectBean.name=sBean.name;
										subprojectBean.weight=sBean.weight;
										subprojectBean.userIds=sBean.userIds;
										subprojectBean.isGrade=sBean.isGrade;
										subprojectBean.isPermit = sBean.isPermit;
										subprojectBean.itemAvg = sBean.itemAvg;
										subprojectBean.minScore = sBean.minScore;
										subprojectBean.initScore = sBean.initScore;
										subprojectBean.operateStep = sBean.operateStep;
										for(int k=0;k<bean.subprojectScoreBeanList.size();k++){
											SubprojectScoreBean scoreBean = bean.subprojectScoreBeanList.get(k);
											if(subprojectBean.id == scoreBean.subprojectId){
												subprojectBean.score = scoreBean.subprojectScore;
											}
										}
										if(subprojectBean.score == -9999){
											subprojectBean.score = subprojectBean.initScore;
										}
										subprojectList.add(subprojectBean);	
										Log.i("songjian", "ratingItem: "+subprojectBean.name+"   itemAvg:"+subprojectBean.itemAvg+"  score:"+subprojectBean.score);
									}
									
									bean.ratingId=classTypeBean.id;
									bean.ratingDate=dateParam;
									bean.subprojectBeanList=subprojectList;
									if (headTeacherClassIds.contains(bean.groupId)) {
										bean.isHeadTeacher=true;
									}
									
									classCompareBeanList.add(bean);
								}
								if(ratingMode == 1){
									taxisAndGrading(classCompareBeanList);
								}else {
									final List<SubprojectBean> subprojectBeans = classCompareBeanList.get(0).subprojectBeanList;
									SubProjectAdapter subProjectAdapter = new SubProjectAdapter(ClassCompareActivity.this, 
											subprojectBeans);
									final CompareClassTypeBean compareClassTypeBean = new CompareClassTypeBean();
									compareClassTypeBean.classCompareBeanList = classCompareBeanList;
									gradeListView.setBackgroundResource(R.drawable.compare_list_bag);
									gradeListView.setAdapter(subProjectAdapter);
									gradeListView.setOnItemClickListener(new OnItemClickListener() {

										@Override
										public void onItemClick(AdapterView<?> arg0, View arg1,
												int position, long arg3) {
											Intent intent=new Intent(ClassCompareActivity.this, ClassMarkActivity.class);
											intent.putExtra("isCompareFinish", isCompareFinish);
											intent.putExtra("ratingMode", 2);
											intent.putExtra("selectId", subprojectBeans.get(position).id);
											intent.putExtra("compareClassTypeBean", compareClassTypeBean);
											startActivityForResult(intent, 1);
										}
									});
								}

							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(ClassCompareActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								showToast(response.getString("msg"));
								GradeCompareAdapter gradeAdapter = new GradeCompareAdapter(ClassCompareActivity.this, null,0,0,null);
								gradeListView.setAdapter(gradeAdapter);
							}

						} catch (JSONException e) {
							GradeCompareAdapter gradeAdapter = new GradeCompareAdapter(ClassCompareActivity.this, null,0,0,null);
							gradeListView.setAdapter(gradeAdapter);
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						showToast("网络连接失败!");
						GradeCompareAdapter gradeAdapter = new GradeCompareAdapter(ClassCompareActivity.this, null,0,0,null);
						gradeListView.setAdapter(gradeAdapter);
						hideLoadDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	public void setSubprojectScore(ClassCompareBean bean) {//设置子选项分数
		if(bean.subprojectScoreBeanList!=null && bean.subprojectScoreBeanList.size()>0 && bean.subprojectBeanList!=null && bean.subprojectBeanList.size()>0){
				for (int i = 0; i < bean.subprojectScoreBeanList.size(); i++) {
					SubprojectScoreBean scoreBean=bean.subprojectScoreBeanList.get(i);
					for (int j = 0; j < bean.subprojectBeanList.size(); j++) {
						SubprojectBean subprojectBean=bean.subprojectBeanList.get(j);
						if (scoreBean.subprojectId==subprojectBean.id) {
							subprojectBean.score=scoreBean.subprojectScore;
							if (subprojectBean.score!=0) {
								subprojectBean.isGrade=true;
							}
							bean.compareScore+=scoreBean.subprojectScore;
						}
					}
				}
		}else {
			
		}
	}
	
	private void taxisAndGrading(List<ClassCompareBean> classCompareBeanList) {//排序或按年级
		this.classCompareBeanList=classCompareBeanList;
		for (int i = 0; i < classCompareBeanList.size(); i++) {
				setSubprojectScore(classCompareBeanList.get(i));
		}
		gradeList=new ArrayList<List<ClassCompareBean>>();
		if (isCompareFinish) {//排序
			Collections.sort(classCompareBeanList, new Comparator<ClassCompareBean>() {
				@Override
				public int compare(ClassCompareBean lhs, ClassCompareBean rhs) {
					if (lhs.compareScore > rhs.compareScore) {
						return -1;
					}
					if (lhs.compareScore == rhs.compareScore) {
						return 0;
					}
					return 1;
				}
			});
			int taxisTag=0;
			for (int i = 0; i < classCompareBeanList.size(); i++) {
				if (i==0) {
					classCompareBeanList.get(i).compareTaxis=i+1;
				}else if (classCompareBeanList.get(i).compareScore==classCompareBeanList.get(i-1).compareScore) {
					taxisTag+=1;
					classCompareBeanList.get(i).compareTaxis=classCompareBeanList.get(i-1).compareTaxis;
				} else {
					classCompareBeanList.get(i).compareTaxis=classCompareBeanList.get(i-1).compareTaxis+1+taxisTag;
					taxisTag=0;
				}
			}
			
			gradeList.add(classCompareBeanList);
			GradeCompareAdapter gradeAdapter = new GradeCompareAdapter(ClassCompareActivity.this, gradeList,0,0,classCompareBeanList);
			gradeAdapter.isTaxis=true;
			gradeAdapter.mActivity=this;
			gradeListView.setAdapter(gradeAdapter);
		}else {
			Map<String,List<ClassCompareBean>> map = new HashMap<String,List<ClassCompareBean>>();
			Collection collection = (Collection)classCompareBeanList;
			for (Object aCollection : collection) {
				ClassCompareBean bean = (ClassCompareBean) aCollection;
			    String key = bean.parent;
			    if (map.containsKey(key)) {
			        List<ClassCompareBean> value = map.get(key);
			        value.add(bean);
			    } else {
			        List<ClassCompareBean> tmpValue = new ArrayList<ClassCompareBean>();
			        tmpValue.add(bean);
			        map.put(key, tmpValue);
			    }
			}
				
			for (String str : map.keySet()) {
				List<ClassCompareBean> beanList=map.get(str);
				Collections.sort(beanList, new MapComparator());
				gradeList.add(beanList);
			}
			map.clear();
			GradeCompareAdapter gradeAdapter = new GradeCompareAdapter(ClassCompareActivity.this, gradeList, 0, 0,classCompareBeanList);
			gradeAdapter.isTaxis = false;
			gradeAdapter.mActivity = this;
			gradeListView.setAdapter(gradeAdapter);
			
			
		}
	}

	class MapComparator implements Comparator<ClassCompareBean>{  
		  
	    public int compare(ClassCompareBean lhs, ClassCompareBean rhs) {  
	        return lhs.groupName.compareTo(rhs.groupName);  
	    }  
	  
	} 
	
	// 完成评比
	private void finishCompare(String id) {
		
		 String urlString = SmartCampusUrlUtils.getSchoolRatingFinishUrl(id);

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
									
									showToast("完成评比成功");
									isCompareFinish=true;

								} else if (response.getInt("code") == -2) {
									InfoReleaseApplication.returnToLogin(ClassCompareActivity.this);
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
							showToast("网络连接失败!");
							hideLoadDialog();
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	
	
	

	private void showLoadDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(ClassCompareActivity.this, "", "...loading...");
		}
	}

	private void hideLoadDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;

		}
	}

	
//	private Toast mToast;
	private List<String> headTeacherClassIds;
private String noncedateParam;
    public void showToast(String text) {    
//        if(mToast == null) {    
//            mToast = Toast.makeText(ClassCompareActivity.this, text, Toast.LENGTH_SHORT);    
//        } else {    
//            mToast.setText(text);      
//            mToast.setDuration(Toast.LENGTH_SHORT);    
//        }    
//        mToast.show();    
        Toast.makeText(ClassCompareActivity.this, text, Toast.LENGTH_SHORT).show(); 
    }    
        
    public void cancelToast() {    
//            if (mToast != null) {    
//                mToast.cancel();    
//            }    
    }    
        
    public void onBackPressed() {    
            cancelToast();    
            super.onBackPressed();    
        } 
    
    private void showConfirmDialog() {
    	final AlertDialog.Builder normalDialog = 
	            new AlertDialog.Builder(this);
	        
    	
	        normalDialog.setMessage("确认完成"+classTypeBean.ratingStartTime+"至"+noncedateParam+"的评比？");
	        normalDialog.setPositiveButton("确定", 
	            new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	finishCompare(classTypeBean.id);
	            }
	        });
	        normalDialog.setNegativeButton("取消", 
	            new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	
	            }
	        });
	        normalDialog.show();

	}
    
    private  boolean useList(int[] arr, int value) {
		for(int s: arr){
				 if(s==value)
				      return true;
				 }
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				if (data.getBooleanExtra("score_change", false)) {
					getClassCompareData(classTypeBean.id,dateParam,ratingMode);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
