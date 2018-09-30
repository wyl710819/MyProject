package com.routon.smartcampus.flower;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.gradetrack.GradeTrackTitle;
import com.routon.smartcampus.gradetrack.GradeTrackTitle.BackClickListner;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.BadgeRadarView;
import com.routon.smartcampus.view.RadoView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class BadgeRadarRevealActivity extends BaseActivity{

	private LinkedHashMap<String, Integer> studentMap = new LinkedHashMap<String, Integer>();
	private LinkedHashMap<String, Integer> averageMap = new LinkedHashMap<String, Integer>();
	private GradeTrackTitle mGradeTrackTitle;
	private boolean isBadge=false;
	private int maxCount;
	private int maxCountIndex;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_badge_radar_reveal);
		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				BadgeRadarRevealActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		};
		mGradeTrackTitle = (GradeTrackTitle)findViewById(R.id.rado_pic_title);
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
		BadgeRadarView radoView=(BadgeRadarView) findViewById(R.id.radoView);
		Intent intent = getIntent();
		ArrayList<String> examAllCourses = intent.getStringArrayListExtra("examAllCourses");//课程
		ArrayList<Integer> studentGrades = intent.getIntegerArrayListExtra("studentGrades");//学生每个课程成绩
		ArrayList<Integer> averageGrades = intent.getIntegerArrayListExtra("averageGrades");//班级每个课程平均成绩
		ArrayList<Integer> propTags = intent.getIntegerArrayListExtra("prop_tag");
		String picTitle = intent.getStringExtra("picTitle");
		String studentName= intent.getStringExtra(MyBundleName.STUDENT_BEAN);
		maxCount = intent.getIntExtra("max_count", 0);
		maxCountIndex = intent.getIntExtra("max_count_index", 0);
		isBadge = intent.getBooleanExtra("is_badge",false);
		
		mGradeTrackTitle.setTitle(picTitle);
		TextView radoStudent = (TextView)findViewById(R.id.txt_rado_student);
		radoStudent.setText(studentName);
		for(int i = 0;i<examAllCourses.size()-1;i++)
		{
			studentMap.put(examAllCourses.get(i), studentGrades.get(i));
			averageMap.put(examAllCourses.get(i), averageGrades.get(i));
		}
		radoView.setMaxCount(maxCount,maxCountIndex,propTags);
        radoView.setStudentMap(studentMap);
        radoView.setAverageMap(averageMap);


          if (isBadge) {
        	  String[] grade = null;
        	if (maxCount<=30) {
        		grade= new String[]{"5", "10", "15", "20","25","30","35","40"};
			}else if (30<maxCount&&maxCount<=70) {
				grade= new String[]{"10", "20", "30","40","50","60","70","80"};
			}else if (70<maxCount) {
				int a=(maxCount+50)/8;
				int b=((a/10)+1)*10;
				grade= new String[]{b+"",2*b+"",3*b+"",4*b+"",5*b+"",6*b+"",7*b+"",8*b+"",};
				
			}
        	  
//              radoView.setGrade(grade);
		}

         

    
	}


}
