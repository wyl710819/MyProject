package com.routon.smartcampus.gradetrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.gradetrack.GradeTrackTitle.BackClickListner;
import com.routon.smartcampus.view.ZigzagLineView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class ZigzagRevealActivity extends BaseActivity{
	private int examType;
	private int position;
	private GradeTrackTitle mGradeTrackTitle;
	private ArrayList<String> examTimes = new ArrayList<String>();//所有考试时间
	private ArrayList<StudentGrade> studentAllGrades = new ArrayList<StudentGrade>();//当前学生所有考试成绩
	private ArrayList<String> examAllCourses = new ArrayList<String>();
	private List<int[]> achievementList = new ArrayList<int[]>();
	private boolean isBadge=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_zigzag_reveal);
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ZigzagRevealActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		mGradeTrackTitle = (GradeTrackTitle)findViewById(R.id.zigzag_pic_title);
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
		ZigzagLineView zigzagLineView=(ZigzagLineView) findViewById(R.id.zigzagline_view);
		Intent intent = getIntent();
		String picTitle = intent.getStringExtra("picTitle");
		mGradeTrackTitle.setTitle(picTitle);
		studentAllGrades = intent.getParcelableArrayListExtra("studentAllGrades");
		examType = intent.getIntExtra("examType", 0);
		isBadge = intent.getBooleanExtra("is_badge", false);
		if(examType == ClassGradesActivity.ONE_COURSE)//单科考试成绩
		{
			for(int i = 0;i < studentAllGrades.size();i++)
			{
				StudentGrade studentGrade = studentAllGrades.get(i);
				if(!examTimes.contains(studentGrade.time))
					examTimes.add(studentGrade.time);
				if(!examAllCourses.contains(studentGrade.course))
					examAllCourses.add(studentGrade.course);
			}
			String[] dateList = new String[examTimes.size()];//日期
			for(int i =0;i<examTimes.size();i++)
				dateList[i] = examTimes.get(i);
			int[] grades = new int[dateList.length];
			for (int i = 0; i < dateList.length; i++) {
				for(int j=0;j<studentAllGrades.size();j++)
				{
					if(studentAllGrades.get(j).time.equals(dateList[i]))
						grades[i] = studentAllGrades.get(j).grade;
				}
			}
			achievementList.add(grades);
			zigzagLineView.setData(achievementList, dateList, examAllCourses,0,isBadge);
		}
		else if(examType == ClassGradesActivity.ALL_COURSE)//综合考试成绩
		{
			examTimes = intent.getStringArrayListExtra("examTimes");
			position = intent.getIntExtra("position", 0);
			if (!isBadge) {
				Collections.reverse(examTimes);
			}
			examAllCourses = intent.getStringArrayListExtra("examAllCourses");
			String[] dateList = new String[examTimes.size()];//日期
			for(int i =0;i<examTimes.size();i++)
				dateList[i] = examTimes.get(i);
			int[] sumAchievement = new int[dateList.length];
			
			if (!isBadge) {
				for (int i = 0; i < examAllCourses.size() - 1; i++) {
					int[] grades = new int[dateList.length];
					for (int j = 0; j < dateList.length; j++) {
						for(int k=0;k<studentAllGrades.size();k++)
						{
							
							StudentGrade studentGrade = studentAllGrades.get(k);
							
							if(studentGrade.course.equals(examAllCourses.get(i))&&studentGrade.time.equals(dateList[j]))
							{
								
								grades[j] = studentGrade.grade;
								sumAchievement[j] += grades[j];
							}
						}
					}
					achievementList.add(grades);
				}
				achievementList.add(sumAchievement);
			}else {
				for (int i = 0; i < examAllCourses.size(); i++) {
					int[] grades = new int[dateList.length];
					for (int j = 0; j < dateList.length; j++) {
						for(int k=0;k<studentAllGrades.size();k++)
						{
							
							StudentGrade studentGrade = studentAllGrades.get(k);
							
							if(studentGrade.course.equals(examAllCourses.get(i))&&studentGrade.time.equals(dateList[j]))
							{
								
								grades[j] = studentGrade.grade;
								sumAchievement[j] += grades[j];
							}
						}
					}
					achievementList.add(grades);
				}
			}
			
			
			zigzagLineView.setData(achievementList, dateList, examAllCourses,position,isBadge);
		}
		
	}

}
