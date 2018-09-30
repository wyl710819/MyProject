package com.routon.smartcampus.gradetrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.widget.PopupList;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.gradetrack.GradeTrackTitle.BackClickListner;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.RadoView;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;

public class ClassGradesActivity extends BaseActivity implements OnItemClickListener,OnScrollListener{
	
	private ArrayList<String> examAllCourses = new ArrayList<String>();//考试所有科目
	private ArrayList<Integer> studentGrades = new ArrayList<Integer>();//当前学生单次考试成绩
	private ArrayList<Integer> averageGrades = new ArrayList<Integer>();//当前单次考试平均成绩
	private ArrayList<String> examTimes = new ArrayList<String>();//所有考试时间
	private ArrayList<StudentGrade> studentAllGrades = new ArrayList<StudentGrade>();//所有学生所有考试成绩
	private ArrayList<StudentGrade> studentOneGrades = new ArrayList<StudentGrade>();//当前学生所有考试成绩
	private ArrayList<Integer> studentTheGrades = new ArrayList<Integer>();//当前学生所有考生成绩的分数
	private ArrayList<CourseGrades> allCourseGrades = new ArrayList<CourseGrades>();//所有考试成绩集合
	private ArrayList<CourseGrades> newAllCourseGrades = new ArrayList<CourseGrades>();//转换后的所有考试成绩集合
	private List<String> names = new ArrayList<String>();//姓名列表
	private List<ImageView> imageArrows = new ArrayList<ImageView>();
	private String title;
	private int type;
	private int position;
	private StudentBean mStudentBean;
	private String selectName;
	private int studentId;
	private int rank;//当前综合考试中学生排名
	private StudentNameListAdapter nameListAdapter;//姓名列表
	private ClassGradesListAdapter adapter;//成绩列表
	public int mAppType;//App类型，1为家长版，2为老师版
	private int scrollState;
	private int scrollLength;
	private boolean mListViewScroll;
	private boolean mListViewNameScroll;
	
	private ListView mListView;
	private ListView mListViewName;
	private LinearLayout mLinearLayoutCourse;
	private LinearLayout mLinearLayoutStudent;
	private GradeTrackTitle mGradeTrackTitle;
	private PopupWindow mPopupWindow;
	private SlowScrollView mHorizontalScrollView;
	
	public static final String TAG = "ClassGradesActivity";
	public static final int ALL_COURSE = 1;//综合成绩
	public static final int ONE_COURSE = 2;//单科成绩
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_class_grades);
		initData();
		initView();
		Log.d(TAG, "scrollView width:"+mHorizontalScrollView.getMeasuredWidth()+
				"   listView width:"+mListView.getMeasuredWidth());
	}
	
	public void initData()
	{
		Intent intent = getIntent();
		examTimes = intent.getStringArrayListExtra("examTimes");
		studentAllGrades = intent.getParcelableArrayListExtra("studentAllGrades");
		examAllCourses = intent.getStringArrayListExtra("examAllCourses");
		title = intent.getStringExtra("title");
		mStudentBean = (StudentBean) intent.getSerializableExtra(MyBundleName.STUDENT_BEAN);
		if(mStudentBean == null)
		{
			studentId = 0;
			selectName = "";
		}
		else 
		{
			studentId = mStudentBean.sid;
			selectName = mStudentBean.empName;
		}
		getTheSelectStudentGrades(studentId);
		mAppType = intent.getIntExtra(MyBundleName.TYPE, SubjectExamActivity.FAMILY_TYPE);
		mListViewName = (ListView)findViewById(R.id.list_student_name);
		mLinearLayoutStudent = (LinearLayout)findViewById(R.id.linearlayout_student_name);
		if(mAppType == SubjectExamActivity.FAMILY_TYPE)
		{
			mLinearLayoutStudent.setVisibility(View.GONE);
			scrollLength = (getResources().getDisplayMetrics().widthPixels-
					ClassGradesListAdapter.dip2px(this, 30)-12)/5;
		}
		else if (mAppType == SubjectExamActivity.SCHOOL_TYPE) {
			mLinearLayoutStudent.setVisibility(View.VISIBLE);
			scrollLength = (getResources().getDisplayMetrics().widthPixels-
					ClassGradesListAdapter.dip2px(this, 111)-9)/4;
		}
		allCourseGrades = intent.getParcelableArrayListExtra("grades");
		if(title.equals("综合") && examAllCourses != null)
		{
			type = ALL_COURSE;
			newAllCourseGrades.add(allCourseGrades.get(allCourseGrades.size()-1));
			for(int i=0;i<allCourseGrades.size()-1;i++)
				newAllCourseGrades.add(allCourseGrades.get(i));
		}
		else 
		{
			type = ONE_COURSE;
			for(int i=allCourseGrades.size()-1;i>=0;i--)
				newAllCourseGrades.add(allCourseGrades.get(i));
		}
		position = intent.getIntExtra("position", 0);
	    rank = newAllCourseGrades.get(0).position;
		for(int i = 0;i<allCourseGrades.size();i++)
		{
			CourseGrades courseGrades = allCourseGrades.get(i);
			Log.d(TAG, "courseName="+courseGrades.course+"   rank="+rank);
			if(rank >=0 && type == ALL_COURSE)
			{
				studentGrades.add(courseGrades.grades.get(rank).grade);
				averageGrades.add(getTheAverage(courseGrades.grades));
			}
		}
		adapter = new ClassGradesListAdapter(this, newAllCourseGrades,rank,type,mAppType);
		nameListAdapter = new StudentNameListAdapter(this, names);
	}
	
	public void initView()
	{
		mLinearLayoutCourse = (LinearLayout)findViewById(R.id.linearlayout_class_grade_title);
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ClassGradesActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		final View root = getLayoutInflater().inflate(R.layout.popupwindow_grade_track, null);
		mPopupWindow = new PopupWindow(root,RadoView.dip2px(this, 80),LinearLayout.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mGradeTrackTitle = (GradeTrackTitle)findViewById(R.id.class_grades_title);
		mGradeTrackTitle.setTitle(title);
		if(type == ALL_COURSE)
			mGradeTrackTitle.setMenuImg(R.drawable.menu_grade_track_menu);
		else mGradeTrackTitle.setMenuImg(R.drawable.grade_track_menu_zigzag);
		if(mAppType == SubjectExamActivity.FAMILY_TYPE)
			mGradeTrackTitle.setTitleImgVisible(View.GONE,View.VISIBLE);
		else mGradeTrackTitle.setTitleImgVisible(View.GONE, View.GONE);
		mGradeTrackTitle.setClickListner(new BackClickListner() {
			
			@Override
			public void onBackClick(View view) {
				finish();
			}

			@Override
			public void onTxtClick(View view) {
			}

			@Override
			public void onMenuClick(View view) {
				if(type == ONE_COURSE)//单科目只显示折线图
				{
					
					Intent zigIntent = new Intent(ClassGradesActivity.this,ZigzagRevealActivity.class);
					zigIntent.putStringArrayListExtra("examTimes", examTimes);
					zigIntent.putParcelableArrayListExtra("studentAllGrades", studentOneGrades);
					zigIntent.putExtra("examType", ONE_COURSE);
					zigIntent.putExtra("picTitle","成绩折线图");
					zigIntent.putExtra(MyBundleName.STUDENT_BEAN, selectName);
					startActivity(zigIntent);
				}
				else
				{
					if(mPopupWindow.isShowing())
						return;
					showPopupWindow(root,view);
				}
			}
			
		});
		mLinearLayoutCourse.removeAllViews();
		View viewIndex = getLayoutInflater().inflate(R.layout.list_flipper_txt, null,false);
		TextView tvIndex = (TextView)viewIndex.findViewById(R.id.txt_list_flipper);
		tvIndex.setText("序号");
		tvIndex.setVisibility(View.VISIBLE);
		int heightIndex = ClassGradesListAdapter.dip2px(this, 36.5f);
		LinearLayout.LayoutParams lpIndex = new LinearLayout.LayoutParams(scrollLength, heightIndex);
		lpIndex.setMargins(0, 0, 3, 0);
		tvIndex.setBackgroundResource(R.drawable.background_subject_one);
		mLinearLayoutCourse.addView(viewIndex, lpIndex);
		for(int i=0;i<newAllCourseGrades.size();i++)
		{
			View view = getLayoutInflater().inflate(R.layout.list_flipper_txt, null,false);
			final TextView textView = (TextView)view.findViewById(R.id.txt_list_flipper);
			final ImageView imageView = (ImageView)view.findViewById(R.id.img_arrow_down);
			imageArrows.add(imageView);
			textView.setTag(i);
			textView.setVisibility(View.VISIBLE);
			textView.setText(newAllCourseGrades.get(i).course);
			int height = ClassGradesListAdapter.dip2px(this, 36.5f);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(scrollLength, height);
			if(i != newAllCourseGrades.size()-1)
				lp.setMargins(0, 0, 3, 0);
			if(i == 0 && type == ALL_COURSE)
				textView.setBackgroundResource(R.drawable.background_subject_sum);
			else textView.setBackgroundResource(R.drawable.background_subject_one);
			Log.d(TAG, ""+(Integer)textView.getTag());
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.d(TAG, ""+(Integer)textView.getTag());
					if(mAppType == SubjectExamActivity.FAMILY_TYPE)
						return;
					getTheIndexInfo((Integer)textView.getTag());
				}
			});
			mLinearLayoutCourse.addView(view,lp);
		}
		Log.d(TAG, "mLinearLayoutCourse :"+mLinearLayoutCourse.getMeasuredWidth());
		mListView = (ListView)findViewById(R.id.list_class_grades);
		mListView.setAdapter(adapter);
		mListViewName.setAdapter(nameListAdapter);
		if(mAppType == SubjectExamActivity.SCHOOL_TYPE)
			getTheIndexInfo(0);
		this.setTouchUnDealView((LinearLayout)findViewById(R.id.linearlayout_class_grade_main));
		mListView.setOnItemClickListener(this);
		mListViewName.setOnItemClickListener(this);
		mHorizontalScrollView = (SlowScrollView)findViewById(R.id.horizontalScrollView);
		mListView.setOnScrollListener(this);
		mListViewName.setOnScrollListener(this);
	}
	
	

	//取平均成绩
	public int getTheAverage(List<StudentGrade> list)
	{
		int sum = 0;
		for(int i =0;i<list.size();i++)
		{
			sum += list.get(i).grade;
		}
		return sum/list.size();
	}
	
	
	public void showPopupWindow(View root,View view)
	{
		mPopupWindow.showAsDropDown(view,0,RadoView.dip2px(this, 6));
		root.findViewById(R.id.img_show_rado).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startRadoActivity();
				mPopupWindow.dismiss();
			}
		});
		root.findViewById(R.id.img_show_zigzag).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startZigzagActivity();
				mPopupWindow.dismiss();
			}
		});
		root.findViewById(R.id.img_show_hist).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startHistActivity();
				mPopupWindow.dismiss();
			}
		});
	}
	

	public void getTheSelectStudentGrades(int stuId)
	{
		studentOneGrades.clear();
		studentTheGrades.clear();
		for(int i=0;i<studentAllGrades.size();i++)
		{
			StudentGrade studentGrade = studentAllGrades.get(i);
			if(studentGrade.studentId == stuId)
			{
				selectName = studentGrade.studentName;
				studentOneGrades.add(studentGrade);
			}
		}
		for(int i=0;i<studentOneGrades.size();i++)
		{
			studentTheGrades.add(studentOneGrades.get(i).grade);
		}
	}
	
	//根据点击标签进行排序
	public void getTheIndexInfo(int index)
	{
		for(int i=0;i<newAllCourseGrades.size();i++)
		{
			if(i == index)
			{
				imageArrows.get(i).setVisibility(View.VISIBLE);
			}
			else imageArrows.get(i).setVisibility(View.INVISIBLE);
		}
		names.clear();
		List<StudentGrade> studentGrades= newAllCourseGrades.get(index).grades;
		if(studentGrades.size()>1)
		{
			Collections.sort(studentGrades, new Comparator<StudentGrade>(){  
				@Override
				public int compare(StudentGrade lhs, StudentGrade rhs) {
					if (lhs.grade > rhs.grade) {
						return -1;
					}
					if (lhs.grade == rhs.grade) {
						return 0;
					}
					return 1; 
				}  
	        }); 
		}
		for(int i=0;i<studentGrades.size();i++)
			names.add(studentGrades.get(i).studentName);
		for(int i=0;i<newAllCourseGrades.size();i++)
		{
			CourseGrades courseGrades = newAllCourseGrades.get(i);
			List<StudentGrade> newStudentGrades = new ArrayList<StudentGrade>();
			for(int j=0;j<studentGrades.size();j++)
			{
				StudentGrade studentGrade = studentGrades.get(j);
				for(int k=0;k<courseGrades.grades.size();k++)
				{
					if(courseGrades.grades.get(k).studentId == studentGrade.studentId)
					{
						newStudentGrades.add(courseGrades.grades.get(k));
						break;
					}
					if(k == courseGrades.grades.size() - 1)
						newStudentGrades.add(new StudentGrade
								(studentGrade.studentId, studentGrade.studentName, 
								studentGrade.rank,0));
				}
			}
			courseGrades.grades = newStudentGrades;
		}
		nameListAdapter.notifyDataSetChanged();
		adapter.notifyDataSetChanged();
	}

	
	private PopupList showOnePopupList(View anchorView, int contextPosition, PopupWindow.OnDismissListener listener) {
		List<String> popupMenuItemList = new ArrayList<String>();
		popupMenuItemList.add("折线图");

		int[] location = new int[2];
		anchorView.getLocationOnScreen(location);
		float x = 0;
		if(newAllCourseGrades.size() >= 4)
			x = getResources().getDisplayMetrics().widthPixels/2;
		else x = ClassGradesListAdapter.dip2px(ClassGradesActivity.this, 55)+scrollLength*newAllCourseGrades.size()/2;
		final float y = location[1] + anchorView.getHeight() / 2;
		PopupList popupList = new PopupList(this);
		popupList.setTextPaddingLeft(popupList.dp2px(14));
		popupList.setTextPaddingRight(popupList.dp2px(14));
		popupList.setOnDismissListener(listener);
		popupList.showPopupListWindow(anchorView, contextPosition, x, y, popupMenuItemList,
				new PopupList.PopupListListener() {

					@Override
					public void onPopupListClick(View contextView, int contextPosition, int position) {

						if(position == 0)//折线图
						{
							Intent zigIntent = new Intent(ClassGradesActivity.this,ZigzagRevealActivity.class);
							zigIntent.putStringArrayListExtra("examTimes", examTimes);
							zigIntent.putParcelableArrayListExtra("studentAllGrades", studentOneGrades);
							zigIntent.putExtra("examType", ONE_COURSE);
							zigIntent.putExtra("picTitle","成绩折线图");
							zigIntent.putExtra(MyBundleName.STUDENT_BEAN, selectName);
							startActivity(zigIntent);
						}

					}

					@Override
					public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
						return true;
					}
				});
		return popupList;
	}
	
	private PopupList showPopupList(View anchorView, int contextPosition, PopupWindow.OnDismissListener listener) {
		List<String> popupMenuItemList = new ArrayList<String>();
		popupMenuItemList.add("雷达图");
		popupMenuItemList.add("折线图");
		popupMenuItemList.add("柱状图");

		int[] location = new int[2];
		anchorView.getLocationOnScreen(location);
		float x = 0;
		if(newAllCourseGrades.size() >= 4)
			x = getResources().getDisplayMetrics().widthPixels/2;
		else x = ClassGradesListAdapter.dip2px(ClassGradesActivity.this, 55)+scrollLength*newAllCourseGrades.size()/2;
		final float y = location[1] + anchorView.getHeight() / 2;
		PopupList popupList = new PopupList(this);
		popupList.setTextPaddingLeft(popupList.dp2px(14));
		popupList.setTextPaddingRight(popupList.dp2px(14));
		popupList.setOnDismissListener(listener);
		popupList.showPopupListWindow(anchorView, contextPosition, x, y, popupMenuItemList,
				new PopupList.PopupListListener() {

					@Override
					public void onPopupListClick(View contextView, int contextPosition, int position) {

						if(position == 0)//雷达图
						{
							startRadoActivity();
						}
						else if(position == 1)//折线图
						{
							startZigzagActivity();
						}
						else if(position == 2)//柱状图
						{
							startHistActivity();
						}

					}

					@Override
					public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
						return true;
					}
				});
		return popupList;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(mAppType == SubjectExamActivity.FAMILY_TYPE)
			return;
		studentGrades.clear();
		for(int i = 0;i<allCourseGrades.size();i++)
		{
			CourseGrades courseGrades = allCourseGrades.get(i);
			studentGrades.add(courseGrades.grades.size()>=position+1?
					courseGrades.grades.get(position).grade:0);
		}
		int stuId =  newAllCourseGrades.get(0).grades.get(position).studentId;
		getTheSelectStudentGrades(stuId);
		if(mAppType == SubjectExamActivity.SCHOOL_TYPE && type == ONE_COURSE)
		{
			showOnePopupList(view, position, new PopupWindow.OnDismissListener() {
				
				@Override
				public void onDismiss() {
				}
			});
		}
		else if(mAppType == SubjectExamActivity.SCHOOL_TYPE && type == ALL_COURSE) {
			showPopupList(view, position, new PopupWindow.OnDismissListener() {
				
				@Override
				public void onDismiss() {
				
				}
			});
		}
	}
	
	public void startRadoActivity()
	{
		Intent radoIntent = new Intent(ClassGradesActivity.this, RadoRevealActivity.class);
		radoIntent.putStringArrayListExtra("examAllCourses", examAllCourses);
		radoIntent.putIntegerArrayListExtra("studentGrades", studentGrades);
		radoIntent.putIntegerArrayListExtra("averageGrades", averageGrades);
		radoIntent.putExtra("picTitle", "成绩雷达图");
		radoIntent.putExtra(MyBundleName.STUDENT_BEAN, selectName);
		startActivity(radoIntent);
	}
	
	public void startZigzagActivity()
	{
		Intent zigIntent = new Intent(ClassGradesActivity.this,ZigzagRevealActivity.class);
		zigIntent.putStringArrayListExtra("examAllCourses", examAllCourses);
		zigIntent.putStringArrayListExtra("examTimes", examTimes);
		zigIntent.putParcelableArrayListExtra("studentAllGrades", studentOneGrades);
		zigIntent.putExtra("examType", ALL_COURSE);
		zigIntent.putExtra("picTitle", "成绩折线图");
		zigIntent.putExtra(MyBundleName.STUDENT_BEAN, selectName);
		zigIntent.putExtra("position", position);
		startActivity(zigIntent);
	}
	
	public void startHistActivity()
	{
		Intent hisIntent = new Intent(ClassGradesActivity.this,HistogramRevealActivity.class);
		hisIntent.putStringArrayListExtra("examAllCourses", examAllCourses);
		hisIntent.putIntegerArrayListExtra("studentGrades", studentGrades);
		hisIntent.putIntegerArrayListExtra("averageGrades", averageGrades);
		hisIntent.putStringArrayListExtra("examTimes", examTimes);
		hisIntent.putExtra("picTitle", "成绩柱状图");
		hisIntent.putExtra(MyBundleName.STUDENT_BEAN, selectName);
		startActivity(hisIntent);
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		ClassGradesActivity.this.scrollState = scrollState;
		if(view == mListView)
		{
			Log.d(TAG, "mListView");
			mListViewScroll = true;
			mListViewNameScroll = false;
		}
		else if(view == mListViewName)
		{
			Log.d(TAG, "mListViewName");
			mListViewScroll = false;
			mListViewNameScroll = true;
		}
	}

	//根据滑动状态来判断当前滑动的ListView，避免两个ListView相互调用
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if(view == mListView && mListViewScroll == false)
			return;
		else if(view == mListViewName && mListViewNameScroll == false)
			return;
		if (scrollState == SCROLL_STATE_IDLE) {  
            return;  
        } 
		Log.d(TAG, "ListView Scroll");
		View subView = view.getChildAt(0);  
        if (subView != null) {  
            final int top = subView.getTop();
            if(view == mListView)
            {
            	mListViewName.setSelectionFromTop(firstVisibleItem, top);  
            }
            else
            {
            	mListView.setSelectionFromTop(firstVisibleItem, top);
            }

        }  
	}

}
