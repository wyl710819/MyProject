package com.routon.smartcampus.gradetrack;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.gradetrack.GradeTrackTitle.BackClickListner;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.HistogramView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

public class HistogramRevealActivity extends BaseActivity{
	
	private GradeTrackTitle mGradeTrackTitle;
	private LinkedHashMap<String, Integer> studentMap = new LinkedHashMap<String, Integer>();
	private LinkedHashMap<String, Integer> averageMap = new LinkedHashMap<String, Integer>();
	private boolean isBadge=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_histogram_reveal);
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				HistogramRevealActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		mGradeTrackTitle = (GradeTrackTitle)findViewById(R.id.hist_pic_title);
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
			}
		});
		FrameLayout.LayoutParams params;
		if (isBadge) {
			params=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, dp2px(350));
		}else {
			params=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp2px(350));
		}
		
		HistogramView histogramView=(HistogramView) findViewById(R.id.histogram_view);
		
		histogramView.setLayoutParams(params);
		
		this.setTouchUnDealView(histogramView);
		Intent intent = getIntent();
		ArrayList<String> examAllCourses = intent.getStringArrayListExtra("examAllCourses");//课程
		ArrayList<Integer> studentGrades = intent.getIntegerArrayListExtra("studentGrades");//学生每个课程成绩
		ArrayList<Integer> averageGrades = intent.getIntegerArrayListExtra("averageGrades");//班级每个课程平均成绩
		ArrayList<String> examTimes = intent.getStringArrayListExtra("examTimes");//所有考试时间
		String picTitle = intent.getStringExtra("picTitle");
		mGradeTrackTitle.setTitle(picTitle);
		String studentName = intent.getStringExtra(MyBundleName.STUDENT_BEAN);
		isBadge = intent.getBooleanExtra("is_badge",false);
		TextView hisStudent = (TextView)findViewById(R.id.txt_his_student);
		hisStudent.setText(studentName);
		
		if (isBadge) {
			int maxCount = intent.getIntExtra("max_count",0);
			
			String[] grade = null;
			if (maxCount<=8) {
				grade= new String[]{"10", "8", "6","4","2","0"};
			}else if (maxCount>8 &&maxCount<=20) {
				grade= new String[]{"25", "20", "15","10","5","0"};
			}else if (maxCount>20 &&maxCount<=40) {
				grade= new String[]{"50", "40", "30","20","10","0"};
			}else if (maxCount>40 && maxCount<=80) {
				int c=maxCount/6;
				grade= new String[]{"100", "80", "60","40","20","0"};
			}else if (maxCount>80) {
				
				int a=(maxCount+50)/5;
				int b=((a/10)+1)*10;
				grade= new String[]{5*b+"",4*b+"", 3*b+"",2*b+"",b+"","0"};
			}
			
			
      	  histogramView.setSteps(grade);
		}
		
		if(examAllCourses.size()>1)//综合成绩
		{
			for (int i = 0; i < examAllCourses.size()-1; i++) {
				studentMap.put(examAllCourses.get(i), studentGrades.get(i));
				averageMap.put(examAllCourses.get(i), averageGrades.get(i));
			}
			histogramView.setYSteps(false);
		}
		else if(examAllCourses.size() == 1)//单科成绩
		{
			for(int i = 0;i < examTimes.size();i++)
			{
				studentMap.put(examTimes.get(i), studentGrades.get(i));
				averageMap.put(examTimes.get(i), averageGrades.get(i));
			}
			histogramView.setYSteps(true);
		}

		histogramView.setStudentMap(studentMap);

		histogramView.setAverageMap(averageMap);
		
		  
	}
	
	private int dp2px(double value) {
		float v = getResources().getDisplayMetrics().density;
		return (int) (v * value + 0.5f);
	}

}
