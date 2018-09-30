package com.routon.smartcampus.gradetrack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.ClassAllGradeBean;
import com.routon.smartcampus.bean.ClassAllGradeBean.StudentGradeBean;
import com.routon.smartcampus.bean.ClassCourseExamBean;
import com.routon.smartcampus.bean.ClassCourseExamsDataBean;
import com.routon.smartcampus.bean.ClassCourseGradeBean;
import com.routon.smartcampus.bean.ClassExamBean;
import com.routon.smartcampus.bean.ClassExamsDataBean;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.coursetable.CourseTableHelper;
import com.routon.smartcampus.flower.NewStudentBadgeListAdapter.onClickListener;
import com.routon.smartcampus.gradetrack.GradeTrackTitle.BackClickListner;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.student.ClassSelListViewAdapter;
import com.routon.smartcampus.utils.MyBundleName;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.routon.widgets.Toast;

public class SubjectExamActivity extends BaseActivity implements OnClickListener{
	private int totalReponse = 0;
	private String[] schoolDate;//学校本学期的开始时间和结束时间
	private StudentBean mStudentBean = null;
	private CourseTableHelper mCourseTableHelper;
	private SubjectExamListAdapter adapter;
	private List<ClassCourseExamsDataBean> classCourseExamsDataBeans = new ArrayList<ClassCourseExamsDataBean>();//单科成绩
	private List<ClassExamBean> classExamBeans = new ArrayList<ClassExamBean>();//综合成绩
	private ArrayList<String> examTimes = new ArrayList<String>();//所有考试时间
	private ArrayList<String> allCourses = new ArrayList<String>();//当前学生参加的所有考试科目
	private ArrayList<StudentGrade> studentAllGrades = new ArrayList<StudentGrade>();//所有学生的所有考试成绩
	private List<Integer> mClassGroupIdList;
	private List<String> mClassList;
	private int classGroupId;
	private int studentId;
	private SharedPreferences mSharedPreferences;
	private Editor mEditor;
	public int mAppType;//App类型，1为家长版，2为老师版
	
	private ListView mListView;
	private ListView classListView;
	private GradeTrackTitle mGradeTrackTitle;
	private FrameLayout mFrameLayoutClass;
	private View mDropdownMask;
	
	private final String TAG = "SubjectExamActivity";
	private final int UPDATE_LIST = 1;
	private final int GET_CLASS_FINISHED =2;
	public static final int SCHOOL_TYPE =2;
	public static final int FAMILY_TYPE = 1;
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case UPDATE_LIST:
				totalReponse++;
				if(totalReponse == 1)
				{
					for(int i=0;i<allCourses.size();i++)
					getCourseExams( allCourses.get(i));
				}
				else if(totalReponse == allCourses.size()+1)
				{
					hideProgressDialog();
					totalReponse = 0;
					adapter = new SubjectExamListAdapter(SubjectExamActivity.this, classExamBeans,classCourseExamsDataBeans);
					mListView.setAdapter(adapter);
					mListView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
							getStudentAllGrades();
							if(position+1<=classExamBeans.size())//综合考试
							{
								ClassExamBean classExamBean = classExamBeans.get(position);
								//点击之后将点击状态设为true，key为点击item的title+content
								String title = classExamBean.examTime.split(" ")[0]+" 综合";
								String content = classExamBean.examName+"成绩";
								mEditor.putBoolean(title+content, true);
								mEditor.commit();
								ArrayList<String> examAllCourses = classExamBean.examAllCourses;//考试所有科目
								if(!examAllCourses.contains("总分"))
									examAllCourses.add("总分");
								ArrayList<CourseGrades> allCourseGrades = new ArrayList<CourseGrades>();
								for(int i=0;i<examAllCourses.size();i++)
									allCourseGrades.add(new CourseGrades(examAllCourses.get(i)));
								List<ClassAllGradeBean> classAllGradeBeans = classExamBean.classAllGrades;
								for(int i = 0;i<classAllGradeBeans.size();i++)
								{
									ClassAllGradeBean classAllGradeBean = classAllGradeBeans.get(i);
									List<StudentGradeBean> studentGrades = classAllGradeBean.studentGrades;//考试课程数目
									for(int j=0;j<studentGrades.size();j++)
									{
										StudentGradeBean studentGradeBean = studentGrades.get(j);
										allCourseGrades.get(j).grades.add(new StudentGrade
												(classAllGradeBean.sid, classAllGradeBean.name, 
												classAllGradeBean.rank,studentGradeBean.grades));
									}
									if(classAllGradeBean.sid == studentId)
									{
										allCourseGrades.get(allCourseGrades.size()-1).rank = classAllGradeBean.rank;
										allCourseGrades.get(allCourseGrades.size()-1).position = i;
									}
									allCourseGrades.get(allCourseGrades.size()-1).grades.add(new StudentGrade
											(classAllGradeBean.sid, classAllGradeBean.name, 
											classAllGradeBean.rank,classAllGradeBean.totalGrades));
								}
								for(int i = 0;i<allCourseGrades.size()-1;i++)//获取单科成绩排名
								{
									if(allCourseGrades.get(allCourseGrades.size()-1).rank > 0)
									{
										int locatePosition = allCourseGrades.get(allCourseGrades.size()-1).rank-1;//当前学生所处列表位置
										allCourseGrades.get(i).rank = getRankInArray(locatePosition, allCourseGrades.get(i).grades);
									}
								}
								getExamTimes();
								Intent intent = new Intent(SubjectExamActivity.this, ClassGradesActivity.class);
								intent.putParcelableArrayListExtra("grades", allCourseGrades);
								intent.putExtra("title", "综合");
								intent.putStringArrayListExtra("examTimes", examTimes);
								intent.putParcelableArrayListExtra("studentAllGrades", studentAllGrades);
								intent.putStringArrayListExtra("examAllCourses", examAllCourses);
								intent.putExtra(MyBundleName.STUDENT_BEAN, mStudentBean);
								intent.putExtra("position", position);
								intent.putExtra(MyBundleName.TYPE, mAppType);
								startActivity(intent);
							}
							else //单科考试
							{
								ClassCourseExamsDataBean classCourseExamsDataBean = classCourseExamsDataBeans.get(position-classExamBeans.size());
								//点击之后将点击状态设为true，key为点击item的title
								String title = classCourseExamsDataBean.courseName;
								String content = classCourseExamsDataBean.classExams.get(0).examTime.split(" ")[0]+"更新";
								mEditor.putBoolean(title+content, true);
								mEditor.commit();
								List<ClassCourseExamBean> classCourseExamBeans = classCourseExamsDataBean.classExams;
								ArrayList<CourseGrades> allCourseGrades = new ArrayList<CourseGrades>();
								for(int i=0;i<classCourseExamBeans.size();i++)
								{
									List<ClassCourseGradeBean> classCourseGradeBeans = classCourseExamBeans.get(i).classCourseGrades;
									CourseGrades courseGrades = new CourseGrades();
									Log.d(TAG, "examTime="+classCourseExamBeans.get(i).examTime);
									String year = classCourseExamBeans.get(i).examTime.split(" ")[0].split("-")[0];
									String month = classCourseExamBeans.get(i).examTime.split(" ")[0].split("-")[1];
									String day = classCourseExamBeans.get(i).examTime.split(" ")[0].split("-")[2];
									courseGrades.course = month+"."+day;
									for(int j=0;j<classCourseGradeBeans.size();j++)
									{
										ClassCourseGradeBean classCourseGradeBean = classCourseGradeBeans.get(j);
										if(classCourseGradeBean.sid == studentId)
										{
											courseGrades.rank = classCourseGradeBeans.get(j).rank;
											courseGrades.position = j;
										}
										courseGrades.grades.add(new StudentGrade
												(classCourseGradeBean.sid, classCourseGradeBean.name,
												classCourseGradeBean.rank, classCourseGradeBeans.get(j).grades));
									}
									allCourseGrades.add(courseGrades);
								}
								ArrayList<StudentGrade> studentAllGradesCourse = new ArrayList<StudentGrade>();
								for(int i = 0;i<studentAllGrades.size();i++)//取学生当前点击科目的所有成绩
								{
									if(studentAllGrades.get(i).course.equals(classCourseExamsDataBean.courseName))
										studentAllGradesCourse.add(studentAllGrades.get(i));
								}
								Collections.reverse(studentAllGradesCourse);//默认取得数据按时间倒序排列
								Collections.reverse(allCourseGrades);
								Intent intent = new Intent(SubjectExamActivity.this, ClassGradesActivity.class);
								intent.putParcelableArrayListExtra("grades", allCourseGrades);
								intent.putExtra("title", classCourseExamsDataBean.courseName);
								intent.putParcelableArrayListExtra("studentAllGrades", studentAllGradesCourse);
								intent.putExtra(MyBundleName.STUDENT_BEAN, mStudentBean);
								intent.putExtra(MyBundleName.TYPE, mAppType);
								startActivity(intent);
							}
						}
					});
				}
				break;
			case GET_CLASS_FINISHED:
				getSchoolTime();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_subject_exam);
		Intent intent = getIntent();
		if(intent != null)
		{
			mStudentBean = (StudentBean) intent.getSerializableExtra(MyBundleName.STUDENT_BEAN);
			mAppType = intent.getIntExtra(MyBundleName.TYPE, FAMILY_TYPE);
			if(mStudentBean != null)
			{
				classGroupId = (int) mStudentBean.groupId;
				studentId = mStudentBean.sid;
			}
			else {
				studentId = 0;
			}
		}
		if( mAppType == FAMILY_TYPE && mStudentBean == null)
		{
			Toast.makeText(this, "没有绑定学生信息", Toast.LENGTH_SHORT).show();
			return;
		}
		initData();
		initView();
		getClassListData();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		adapter.notifyDataSetChanged();
	}
	
	public void initData()
	{
		mCourseTableHelper = new CourseTableHelper(this);
		mSharedPreferences = getSharedPreferences("subjectitemclick", MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		mClassGroupIdList = new ArrayList<Integer>();
		mClassList = new ArrayList<String>();
		mGradeTrackTitle = (GradeTrackTitle)findViewById(R.id.subject_exam_title);
		classListView = (ListView)findViewById(R.id.list_grade_class);
	}
	
	public void initView()
	{
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SubjectExamActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		if(mAppType == FAMILY_TYPE)
			mGradeTrackTitle.setTitleImgVisible(View.GONE, View.GONE);
		else if(mAppType == SCHOOL_TYPE)
			mGradeTrackTitle.setTitleImgVisible(View.VISIBLE, View.GONE);
		mGradeTrackTitle.setClickListner(new BackClickListner() {

			@Override
			public void onBackClick(View view) {
				finish();
			}

			@Override
			public void onTxtClick(View view) {
				if(classListView.getVisibility() == View.VISIBLE)
				{
					listViewInAnim();
				}
				else
				{
					listViewOutAnim();
				}
			}

			@Override
			public void onMenuClick(View view) {
			}
		});
		mGradeTrackTitle.setTitle("学情跟踪");
		mListView = (ListView)findViewById(R.id.list_subject_exam);
		mFrameLayoutClass = (FrameLayout)findViewById(R.id.dropdown_grade_class);
		classListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				classGroupId = mClassGroupIdList.get(position);
				mGradeTrackTitle.setText(mClassList.get(position));
				showProgressDialog();
				getAllExams();
				listViewInAnim();
			}
		});
		mDropdownMask = findViewById(R.id.dropdown_mask_subject_exam);
		mDropdownMask.setOnClickListener(this);
	}
	
	//获取所有成绩数据
	public void getAllExams()
	{
		classCourseExamsDataBeans.clear();
		classExamBeans.clear();
		examTimes.clear();
		allCourses.clear();
		studentAllGrades.clear();
		totalReponse = 0;
		String url = null;
		if(mAppType == FAMILY_TYPE)
			url = SmartCampusUrlUtils.getAllExamsUrl(String.valueOf(studentId));
		else 
		{
			if(classGroupId == 0)
				return;
			url = SmartCampusUrlUtils.getTeacherAllExamsUrl(String.valueOf(classGroupId));
		}
		if(schoolDate != null)
		{
			if(!TextUtils.isEmpty(schoolDate[0]) && (!TextUtils.isEmpty(schoolDate[1])))
				url = url+"&beginTime="+schoolDate[0]+"&endTime="+schoolDate[1];
		}
		Log.d(TAG, "url="+url);
		CookieJsonRequest request = new CookieJsonRequest(Method.GET, url, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response="+response);
						int code = response.optInt("code");
						if(code == 0)
						{
							ClassExamsDataBean classExamsDataBean = new ClassExamsDataBean(response.optJSONObject("datas"));
							List<ClassExamBean> mClassExamBeans = classExamsDataBean.classExams;
							if(mClassExamBeans.size() == 0)
							{
								hideProgressDialog();
								Toast.makeText(SubjectExamActivity.this, "当前学期没有录入考试成绩", Toast.LENGTH_SHORT).show();
								adapter = new SubjectExamListAdapter(SubjectExamActivity.this, classExamBeans,classCourseExamsDataBeans);
								mListView.setAdapter(adapter);
								return;
							}
							for(int i = 0;i<mClassExamBeans.size();i++)
							{
								ClassExamBean classExamBean = mClassExamBeans.get(i);
								for (int j = 0; j < classExamBean.examAllCourses.size(); j++) {
									if (!allCourses.contains(classExamBean.examAllCourses.get(j)))
										allCourses.add(classExamBean.examAllCourses.get(j));
								}
								if (classExamBean.examType.equals("综合考试"))// 只是因为...........
									classExamBeans.add(classExamBean);
							}
							mHandler.sendEmptyMessage(UPDATE_LIST);
						}
						else if (code == -1) {
							hideProgressDialog();
							Toast.makeText(SubjectExamActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
						}
						else if(code == -2){
							hideProgressDialog();
							Toast.makeText(SubjectExamActivity.this, "请登录", Toast.LENGTH_SHORT).show();
						}
					}
				}, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideProgressDialog();
						Log.d(TAG, "error="+error.toString());
						if(InfoReleaseApplication.showNetWorkFailed(SubjectExamActivity.this)==false)
							Toast.makeText(SubjectExamActivity.this, "网络连接失败",Toast.LENGTH_SHORT).show();
						else Toast.makeText(SubjectExamActivity.this, "成绩查询失败", Toast.LENGTH_SHORT).show();
					}
				});
		request.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(request); 	
	}
	
	
	//获取单科成绩数据
	public void getCourseExams(final String course) {
		String url = null;
		if(mAppType == FAMILY_TYPE)
			url = SmartCampusUrlUtils.getCourseExamUrl(String.valueOf(studentId), course);
		else url= SmartCampusUrlUtils.getTeacherCourseExamUrl(String.valueOf(classGroupId), course);
		if(schoolDate != null)
		{
			if(!TextUtils.isEmpty(schoolDate[0]) && (!TextUtils.isEmpty(schoolDate[1])))
				url = url+"&beginTime="+schoolDate[0]+"&endTime="+schoolDate[1];
		}
		Log.d(TAG, "url=" + url);
		CookieJsonRequest request = new CookieJsonRequest(Method.GET, url,
				null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						int code = response.optInt("code");
						if (code == 0) {
							ClassCourseExamsDataBean classCourseExamsDataBean = new ClassCourseExamsDataBean(response.optJSONObject("datas"));
							if(classCourseExamsDataBean.classExams.size()>0)
							{
								classCourseExamsDataBeans.add(classCourseExamsDataBean);
							}
							mHandler.sendEmptyMessage(UPDATE_LIST);
						} else if (code == -1) {
							hideProgressDialog();
							Toast.makeText(SubjectExamActivity.this, "查询失败",Toast.LENGTH_SHORT).show();
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideProgressDialog();
						Log.d(TAG, "error=" + error.toString());
						if(InfoReleaseApplication.showNetWorkFailed(SubjectExamActivity.this)==false)
							Toast.makeText(SubjectExamActivity.this, "网络连接失败",Toast.LENGTH_SHORT).show();
						else Toast.makeText(SubjectExamActivity.this, "成绩查询失败", Toast.LENGTH_SHORT).show();
					}
				});
		request.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(request);
	}
	
	
	//获取教师班级信息
	private void getClassListData() {
		showProgressDialog();
		if(mAppType == FAMILY_TYPE)
		{
			mHandler.sendEmptyMessage(GET_CLASS_FINISHED);
			return;
		}
		mClassList.clear();
		mClassGroupIdList.clear();
		GroupListData.getClassListData(SubjectExamActivity.this, new DataResponse.Listener<ArrayList<GroupInfo>>() {

			

			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				// TODO Auto-generated method stub

				for (int i = 0; i < classGroups.size(); i++) {
					mClassList.add(classGroups.get(i).getName());
					mClassGroupIdList.add(classGroups.get(i).getId());
				}
				if (mClassList.size() > 0) {
					mGradeTrackTitle.setText(mClassList.get(0));
					classGroupId = mClassGroupIdList.get(0);
					classListView.setAdapter(new ClassSelListViewAdapter(SubjectExamActivity.this,mClassList));
					mHandler.sendEmptyMessage(GET_CLASS_FINISHED);
				}
				else
				{
					hideProgressDialog();
					Toast.makeText(SubjectExamActivity.this, "没用绑定班级信息", Toast.LENGTH_SHORT).show();
				}
			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				Toast.makeText(SubjectExamActivity.this, "获取班级信息失败", Toast.LENGTH_SHORT).show();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideProgressDialog();
				Toast.makeText(SubjectExamActivity.this, "获取班级信息失败", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	//下载学校作息时间表得到学期时间
	public void getSchoolTime()
	{
		mCourseTableHelper.getSchoolAttendance(classGroupId, new CourseTableHelper.Listener<String>() {

			@Override
			public void onResponse(String response) {
				if (response.equals("Failed")) {
					Toast.makeText(SubjectExamActivity.this, "网络连接失败",Toast.LENGTH_SHORT).show();
					mCourseTableHelper.dismissDialog();
					hideProgressDialog();
					return;
				}
				Calendar calendar=Calendar.getInstance();
				 mCourseTableHelper.getSchoolTimeTableAboutXmls(classGroupId,calendar,new CourseTableHelper.Listener<String>(){

						@Override
						public void onResponse(String response) {
							// TODO Auto-generated method stub
							Log.d(TAG,"获取数据成功");
							schoolDate = mCourseTableHelper.getSchoolBeginEndTime();
							if(schoolDate != null)
								Log.d(TAG, "beginDate="+schoolDate[0]+"   endDate="+schoolDate[1]);
							getAllExams();
						}
	                	
	                },new CourseTableHelper.ErrorListener(){

						@Override
						public void onResponse(String errorMsg) {
							// TODO Auto-generated method stub
							Toast.makeText(SubjectExamActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
							Log.d(TAG,"errorMsg="+errorMsg);
							getAllExams();
							
						}
	                });
				
			}
		});
	}
	
	//单科成绩排名
	public int getRankInArray(int locatePosition,List<StudentGrade> list)
	{
		int rank = 1;
		int num = list.get(locatePosition).grade;
		for(int i=0;i<list.size();i++)
		{
			if(list.get(i).grade>num)
				rank++;
		}
		return rank;
	}
	
	//得到当前id学生所有成绩和考试时间
	public void getStudentAllGrades()
	{
		studentAllGrades.clear();
		for(int i = 0;i<classCourseExamsDataBeans.size();i++)
		{
			ClassCourseExamsDataBean classCourseExamsDataBean = classCourseExamsDataBeans.get(i);
			List<ClassCourseExamBean> classCourseExamBeans = classCourseExamsDataBean.classExams;
			for(int j = 0;j<classCourseExamBeans.size();j++)
			{
				ClassCourseExamBean classCourseExamBean = classCourseExamBeans.get(j);
				String examTime = classCourseExamBean.examTime;
				List<ClassCourseGradeBean> classCourseGradeBeans = classCourseExamBean.classCourseGrades;
				for(int k = 0;k<classCourseGradeBeans.size();k++)
				{
					ClassCourseGradeBean classCourseGradeBean = classCourseGradeBeans.get(k);
					studentAllGrades.add(new StudentGrade(examTime,
							classCourseExamsDataBean.courseName,
							classCourseGradeBean.grades,
							classCourseGradeBean.sid,classCourseGradeBean.name));
				}
			}
		}
	}
	
	//得到综合考试时间
	public void getExamTimes()
	{
		examTimes.clear();
		for(int i=0;i<classExamBeans.size();i++)
		{
			ClassExamBean classExamBean = classExamBeans.get(i);
			if(!examTimes.contains(classExamBean.examTime))
			{
				examTimes.add(classExamBean.examTime);
			}
		}
	}
	
	private void listViewOutAnim() {
		classListView.clearAnimation();
		classListView.setVisibility(View.VISIBLE);
		classListView.startAnimation(AnimationUtils.loadAnimation(SubjectExamActivity.this, R.anim.dd_menu_in));
		mFrameLayoutClass.setVisibility(View.VISIBLE);
		mDropdownMask.setVisibility(View.VISIBLE);
		mDropdownMask.startAnimation(AnimationUtils.loadAnimation(SubjectExamActivity.this, R.anim.dd_mask_in));
	}
	private void listViewInAnim() {
		
		classListView.clearAnimation();
		classListView.setVisibility(View.GONE);
		classListView.startAnimation(AnimationUtils.loadAnimation(SubjectExamActivity.this, R.anim.dd_menu_out));
		mFrameLayoutClass.setVisibility(View.GONE);
		mFrameLayoutClass.startAnimation(AnimationUtils.loadAnimation(SubjectExamActivity.this, R.anim.dd_mask_out));
		mDropdownMask.setVisibility(View.GONE);
		mDropdownMask.startAnimation(AnimationUtils.loadAnimation(SubjectExamActivity.this, R.anim.dd_mask_out));


	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dropdown_mask_subject_exam:
			if(classListView.getVisibility() == View.VISIBLE)
			{
				listViewInAnim();
			}
			break;

		default:
			break;
		}
	}

}
