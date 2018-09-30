package com.routon.smartcampus.answerrelease;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.coursetable.CourseDataUtil;
import com.routon.smartcampus.coursetable.CourseTableHelper;
import com.routon.smartcampus.coursetable.CourseDataUtil.TimeTable;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.network.SmartCampusUrlUtils;

import android.view.MotionEvent;

public class FragmentAnswerTrueStuImgView extends Fragment{

	
	private static final String TAG="Fragment_AnswerTrueStuImgView";
	private ArrayList <StudentBean>optionStudentList=new ArrayList<StudentBean>();
	private ProgressDialog progressDialog;
	private GridViewAdapter stuImgGridAdapter;
	private GridView stuGridView;
	private TextView answerCount;
	private TextView tvAwardFlower;
	private TextView tvAnsTureBtn;
	private AnswerMainActivity answerMainActivity;
	private int answerOrDecision=0;
	private int questionId=0;
	private List <Integer> studentIdList=new ArrayList<Integer>();
	private String studentIds=null;
	List<NameValuePair> params=new ArrayList<NameValuePair>();
	//上报答题正确学生数据
	private String answerTime=null;
	private String rightAnswer=null;
	List<Integer> optionStudentSidList;
	private CourseTableHelper mCourseTableHelper = null;
	private Calendar calendar;
	private boolean isExist=false;
	private List<TimeTable> timetables;
	private String currDay=null;
	private ArrayList<String>beginTimeList=new ArrayList<String>();
	private ArrayList<String>endTimeList=new ArrayList<String>();
	private ArrayList<String>courseNameList=new ArrayList<String>();
	private CourseDataUtil courseDataUtil;
	private String nonceTime = null;
	View view;
	private int classId=0;
	private String courseName=null;
	private boolean isUpLoadSuccess=false;
	private boolean isAwardFlowerSuccess=false;
	private GestureDetector mDetector;
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private String badgeId;
	private String bonuspoint;
	private ArrayList<Badge> flowersList;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {
		view=inflater.inflate(R.layout.fragment_answer_line_view, container,false);
		fragmentManager=answerMainActivity.getSupportFragmentManager();
		stuGridView=(GridView) view.findViewById(R.id.gv_stu);
		answerCount=(TextView) view.findViewById(R.id.answer_count);
		tvAwardFlower=(TextView) view.findViewById(R.id.answer_flower_btn);
		tvAnsTureBtn=(TextView) view.findViewById(R.id.answer_true_btn);
		mCourseTableHelper = new CourseTableHelper(getActivity());
		calendar=Calendar.getInstance();
		courseDataUtil=new CourseDataUtil();
		mDetector=new GestureDetector(this.getActivity(), new MyOnGestureListener()); 
		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			return mDetector.onTouchEvent(event);//返回手势识别触发的事件
			
			}
			
		});

		tvAwardFlower.setOnClickListener(new OnClickListener() {
			
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isAwardFlowerSuccess){
				Toast.makeText(getActivity(), "已上颁发小红花，请勿重复!", Toast.LENGTH_SHORT).show();
				return;
			}
//			AwardFlower();
			getBadgeListData();
			}
		});
		Bundle bundle=getArguments();
		if(bundle!=null){
			optionStudentList=bundle.getParcelableArrayList("optionStudentList");
			answerOrDecision=bundle.getInt("answerOrDecision");
			answerTime=bundle.getString("answerTime");
			classId=bundle.getInt("classId");
			questionId=bundle.getInt("questionId");
			currDay=bundle.getString("currDay");
			nonceTime=bundle.getString("nonceTime");
			if(answerOrDecision==1){
				tvAnsTureBtn.setVisibility(View.VISIBLE);
				tvAwardFlower.setVisibility(View.VISIBLE);
			}else if(answerOrDecision==3){ 
				tvAnsTureBtn.setVisibility(View.INVISIBLE);
				tvAwardFlower.setVisibility(View.INVISIBLE);
			}
			if(optionStudentList.size()>0){
				if(optionStudentList.get(0).result!=null){
					SpannableStringBuilder span;
					answerCount.setTextColor(Color.parseColor("#b94645"));
					String ans=null;
					if(optionStudentList.get(0).result.equals("")){
						tvAwardFlower.setVisibility(View.INVISIBLE);
						tvAnsTureBtn.setVisibility(View.INVISIBLE);
						answerCount.setText(optionStudentList.size()+"位同学未选择");
						ans=optionStudentList.size()+"位同学未选择";
						span = new SpannableStringBuilder(ans);
						span.setSpan(new ForegroundColorSpan(Color.BLACK), ans.lastIndexOf("位"), ans.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
						answerCount.setText(span);
					}else{
						if(optionStudentList.get(0).result.equals("Y")){
							ans=optionStudentList.size()+"位同学选择"+" 同意";
							answerCount.setText(optionStudentList.size()+"位同学选择"+" 同意");
							span = new SpannableStringBuilder(ans);  
							span.setSpan(new ForegroundColorSpan(Color.BLACK), ans.lastIndexOf("位"), ans.lastIndexOf("择")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							span.setSpan(new ForegroundColorSpan(Color.parseColor("#3CB371")), ans.lastIndexOf("同"), ans.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
							answerCount.setText(span);
						}else if(optionStudentList.get(0).result.equals("N")){
							ans=optionStudentList.size()+"位同学选择"+" 反对";
							answerCount.setText(optionStudentList.size()+"位同学选择"+" 反对");
							span = new SpannableStringBuilder(ans);  
							span.setSpan(new ForegroundColorSpan(Color.BLACK), ans.lastIndexOf("位"), ans.lastIndexOf("择")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
							answerCount.setText(span);
						}else{
							tvAwardFlower.setVisibility(View.VISIBLE);
							tvAnsTureBtn.setVisibility(View.VISIBLE);
							answerCount.setText(optionStudentList.size()+"位同学选择"+optionStudentList.get(0).result);
							ans=optionStudentList.size()+"位同学选择"+optionStudentList.get(0).result;
							span = new SpannableStringBuilder(ans);  
							span.setSpan(new ForegroundColorSpan(Color.BLACK), ans.lastIndexOf("位"), ans.lastIndexOf("择")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
							answerCount.setText(span);
						}
						
					}
				}
				
				
			}
			
//			Log.d(TAG,optionStudentList.get(0).empName);
		}
		if(optionStudentList!=null&&optionStudentList.size()>0){
			for(int i=0;i<optionStudentList.size();i++){
				studentIdList.add(optionStudentList.get(i).sid);
				rightAnswer=optionStudentList.get(0).result;
			}
		}
		getSchoolAttendance();
		tvAnsTureBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "答题id:" + questionId + "答题时间:" + answerTime
						+ "正确答案:" + rightAnswer);
//				if(beginTimeList!=null&&beginTimeList.size()>0){
//					beginTimeList.clear();
//				}
//				if(endTimeList!=null&&beginTimeList.size()>0){
//					beginTimeList.clear();
//				}
//				if(courseNameList!=null&&courseNameList.size()>0){
//					courseNameList.clear();
//				}
//				calendar.set(Integer.parseInt(currDay.substring(0, 4)),Integer.parseInt(currDay.substring(5, 7)),Integer.parseInt(currDay.substring(8, currDay.length())));
				calendar=Calendar.getInstance();
				timetables=new ArrayList<TimeTable>();
				ArrayList<String> amcourses=new ArrayList<String>();
				ArrayList<String> pmcourses=new ArrayList<String>();
				timetables = mCourseTableHelper.getCourseData(calendar,true,true);
				
				amcourses = mCourseTableHelper.getAmCourseTimeAndName(timetables);
				pmcourses = mCourseTableHelper.getPmCourseTimeAndName(timetables);
				if(amcourses!=null&&amcourses.size()>0){
					for(int i=0;i<amcourses.size();i++){
						beginTimeList.add(amcourses.get(i).substring(0,amcourses.get(i).lastIndexOf("-")-1));
						endTimeList.add(amcourses.get(i).substring(amcourses.get(i).lastIndexOf("-")+2, amcourses.get(i).lastIndexOf("-")+7));
						courseNameList.add(amcourses.get(i).substring(amcourses.get(i).lastIndexOf(" "),amcourses.get(i).length()));
					}
				}
				if(pmcourses!=null&&pmcourses.size()>0){
					for(int i=0;i<pmcourses.size();i++){
						beginTimeList.add(pmcourses.get(i).substring(0,pmcourses.get(i).lastIndexOf("-")-1));
						endTimeList.add(pmcourses.get(i).substring(pmcourses.get(i).lastIndexOf("-")+2, pmcourses.get(i).lastIndexOf("-")+7));
						courseNameList.add(pmcourses.get(i).substring(pmcourses.get(i).lastIndexOf(" "),pmcourses.get(i).length()));
					}
				}
				
				courseName=getCourseName(beginTimeList,endTimeList,courseNameList);
				if(courseName!=null){
					courseName=courseName.trim();
				}
				CommitTrueAnswerStuInfo(courseName); 
			}
		});
		
		
		stuImgGridAdapter=new GridViewAdapter(getActivity(), optionStudentList);
		stuGridView.setAdapter(stuImgGridAdapter);
		stuGridView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			return mDetector.onTouchEvent(event);//返回手势识别触发的事件
			
			}
			
		});
		return view;
		
		
    }
	
	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		// TODO Auto-generated method stub
		if (enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.fragement_slide_right_to_left);
        } else {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.fragement_slide_left_to_right);
        }
	}
	protected String getCourseName(ArrayList<String> beginTimeList, ArrayList<String> endTimeList, ArrayList<String> courseNameList) {
		// TODO Auto-generated method stub
		for(int i=0;i<beginTimeList.size();i++){
			if (isBelongTime(nonceTime,
					beginTimeList.get(i),
					endTimeList.get(i))) {
				courseName = (String) courseNameList.get(i);
			}
		}
		return courseName;
	}
	/**
	 * 拼接小红花
	 * */
	public String listToString(List<Integer> list, char separator) {   
		StringBuilder sb = new StringBuilder();    
		for (int i = 0; i < list.size(); i++) { 
			
				sb.append(list.get(i)).append(separator);   
			
			
		}   
		return sb.toString().substring(0,sb.toString().length()-1);
	}
	
	/**
	 * 拼接回答正确学生id
	 * */
	public String listToString(List<Integer> list, String separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {

			sb.append(list.get(i)).append(separator);

		}
		return sb.toString().substring(0, sb.toString().length() - 8);
	}
	protected void AwardFlower(String badgeId,String bonuspoint) {
		// TODO Auto-generated method stub
		
		String urlString;
		urlString = SmartCampusUrlUtils.getBadugeIssueURl();
		params.add(new BasicNameValuePair("studentIds", listToString(studentIdList, ',')));
		params.add(new BasicNameValuePair("badgeId", badgeId));
		params.add(new BasicNameValuePair("count", String.valueOf(1)));
		params.add(new BasicNameValuePair("title", "回答正确"));
		params.add(new BasicNameValuePair("remark", "课堂上认真听讲、积极思考，回答问题正确"));
		params.add(new BasicNameValuePair("bonuspoint", bonuspoint));
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
//						if (progressDialog != null && progressDialog.isShowing()) {
//							progressDialog.dismiss();
//						}
						
						try {
							if (response.getInt("code") == 0) {
								Toast.makeText(getActivity(), "颁发成功", Toast.LENGTH_SHORT).show();
								isAwardFlowerSuccess=true;
								//上传答题数据
								CommitTrueAnswerStuInfo(getCourseName());
							} else if (response.getInt("code") == -2) {
								hideMyProgressDialog();
								InfoReleaseApplication.returnToLogin(getActivity());
							} else {
								hideMyProgressDialog();
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(getActivity(), "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();
						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	private void getBadgeListData() {
		String urlString = SmartCampusUrlUtils.getBadgeListUrl();
//		showProgressDialog();
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
//						hideProgressDialog();
						try {
							if (response.getInt("code") == 0) {

								flowersList = new ArrayList<Badge>();
								JSONArray array = response.getJSONArray("datas");
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									Badge flower = new Badge(obj);
									
									flowersList.add(flower);
									
								}
								if(flowersList!=null){
									badgeId=String.valueOf(flowersList.get(0).id);
									bonuspoint=String.valueOf(flowersList.get(0).bonuspoint);
								}
								AwardFlower(badgeId,bonuspoint);
//								refreshFlowerList(flowersList);
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
								hideMyProgressDialog();
							} else {// 失败
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(answerMainActivity,response.getString("msg"), Toast.LENGTH_SHORT).show();
								hideMyProgressDialog();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						if( InfoReleaseApplication.showNetWorkFailed(answerMainActivity) == true ){
							Toast.makeText(answerMainActivity, "获取数据失败", Toast.LENGTH_SHORT).show();
							hideMyProgressDialog();
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(getActivity(), "", "...loading...");
		}
	}
	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	@Override
	@Deprecated
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.answerMainActivity=(AnswerMainActivity) activity;
	};
	
	
    //获取课程名称
	private String getCourseName(){
		
		beginTimeList.clear();
		endTimeList.clear();
		courseNameList.clear();
		
		if (calendar==null) {
			calendar=Calendar.getInstance();
		}
		String courseNameStr="";
		timetables=new ArrayList<TimeTable>();
		ArrayList<String> amcourses=new ArrayList<String>();
		ArrayList<String> pmcourses=new ArrayList<String>();
		timetables = mCourseTableHelper.getCourseData(calendar,true,true);
		
		amcourses = mCourseTableHelper.getAmCourseTimeAndName(timetables);
		pmcourses = mCourseTableHelper.getPmCourseTimeAndName(timetables);
		if(amcourses!=null&&amcourses.size()>0){
			for(int i=0;i<amcourses.size();i++){
				beginTimeList.add(amcourses.get(i).substring(0,amcourses.get(i).lastIndexOf("-")-1));
				endTimeList.add(amcourses.get(i).substring(amcourses.get(i).lastIndexOf("-")+2, amcourses.get(i).lastIndexOf("-")+7));
				courseNameList.add(amcourses.get(i).substring(amcourses.get(i).lastIndexOf(" "),amcourses.get(i).length()));
			}
		}
		if(pmcourses!=null&&pmcourses.size()>0){
			for(int i=0;i<pmcourses.size();i++){
				beginTimeList.add(pmcourses.get(i).substring(0,pmcourses.get(i).lastIndexOf("-")-1));
				endTimeList.add(pmcourses.get(i).substring(pmcourses.get(i).lastIndexOf("-")+2, pmcourses.get(i).lastIndexOf("-")+7));
				courseNameList.add(pmcourses.get(i).substring(pmcourses.get(i).lastIndexOf(" "),pmcourses.get(i).length()));
			}
		}
		
		courseNameStr=getCourseName(beginTimeList,endTimeList,courseNameList);
		if(courseNameStr!=null){
			courseNameStr=courseNameStr.trim();
		}
		
		return courseNameStr;
		
	}
	
	
	
	
	/**
	 * 提交回答正确学生信息到平台
	 * */
	private void CommitTrueAnswerStuInfo(String courseName) {
		Log.e(TAG,"run:"+courseName);
		if(courseName==null){
			Toast.makeText(getActivity(), "课间休息时间!", Toast.LENGTH_SHORT).show();
			return;
		}
		if(isUpLoadSuccess){
			Toast.makeText(getActivity(), "已上传过该数据，请勿重复!", Toast.LENGTH_SHORT).show();
			return;
		}
		if(classId==0){
			Toast.makeText(answerMainActivity, "获取班级分组信息失败", Toast.LENGTH_LONG).show();
			return;
		}
		showMyProgressDialog();
		studentIds=listToString(studentIdList, "&stuIds=");
		Log.d(TAG,"学生id:"+studentIds);
		String urlString;
	    urlString = SmartCampusUrlUtils.getAnswereleaseUrl(questionId,
				courseName, rightAnswer,
				String.valueOf(classId),"","",0);
	    Log.d(TAG,"CommitUrl="+urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								Toast.makeText(getActivity(), "已将学生答题数据上报平台!", Toast.LENGTH_SHORT).show();
								isUpLoadSuccess=true;
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(getActivity());
								Toast.makeText(getActivity(), "登录已失效!", Toast.LENGTH_SHORT).show();
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(getActivity(), "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();
						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	    
	}
	/**
	 * 获取课程表数据
	 * */
	void getSchoolAttendance(){
		
		mCourseTableHelper.getSchoolAttendance(classId, new CourseTableHelper.Listener<String>(){

			@Override
			public void onResponse(String response) {
				// TODO Auto-generated method stub
				Calendar calendar=Calendar.getInstance();
				 mCourseTableHelper.getCourseTableAboutXmls(classId,calendar,new CourseTableHelper.Listener<String>(){

						@Override
						public void onResponse(String response) {
							// TODO Auto-generated method stub
//							Log.d(TAG,"获取数据成功");
							isExist = true;
						}
	                	
	                },new CourseTableHelper.ErrorListener(){

						@Override
						public void onResponse(String errorMsg) {
							// TODO Auto-generated method stub
//							showToast( errorMsg);
							isExist=false;
						}
	                	
	                });
			}
			
		});
	}
	
	public static boolean isBelongTime(String nowTime, String beginTime,
			String endTime) {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		Date nowDate = null;
		Date beginDate = null;
		Date endDate = null;
		try {
			nowDate = df.parse(nowTime);
			beginDate = df.parse(beginTime);
			endDate = df.parse(endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return nowDate.getTime() >= beginDate.getTime()
				&& nowDate.getTime() <= endDate.getTime();
	}
	
	
	private class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override//此方法必须重写且返回真，否则onFling不起效
		public boolean onDown(MotionEvent e) {
		return true;
		}
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2,
				float velocityX, float velocityY){
			float distanceX = e2.getRawX()-e1.getRawX();
			float distanceY = Math.abs(e2.getRawY() - e1.getRawY());
			if(distanceX > distanceY){
				closeAnswerTureFragment();
			}
			
			return super.onFling(e1, e2, velocityX, velocityY);
			
		}
   }
	public void closeAnswerTureFragment(){
		FragmentAnswerTrueStuImgView fragmentAnswerTrueStuImgView=new FragmentAnswerTrueStuImgView();
		transaction = fragmentManager.beginTransaction();
//		transaction.setCustomAnimations(R.anim.fragement_slide_left_to_right, 0);
		transaction.remove(fragmentManager.findFragmentByTag("FragmentAnswerTrueStuImgView"));
		transaction.commit();
	}
	
}
