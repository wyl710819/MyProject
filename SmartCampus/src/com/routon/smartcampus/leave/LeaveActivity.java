package com.routon.smartcampus.leave;


import java.util.List;

import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.smartcampus.exchangecourse.ExchangeFragment;
import com.routon.smartcampus.leave.LeaveBottomBar.OnBtnClickListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;

public class LeaveActivity extends BaseFragmentActivity{
	private static final String TAG = "LeaveActivity";
	private Fragment userFragment = null;
	private Fragment teacherFragment = null;
	private Fragment exchangeFragment = null;
	private boolean isHeadTeacher;
	public boolean isUpdateUserLeaveData;
	public boolean isUpdateTeacherLeaveData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_leave_layout);
		if (InfoReleaseApplication.authenobjData.timetable_privilege==1) {
			isHeadTeacher = true;
		}
		initView();
		initData();
	}
	
	private void initView() {
		LeaveBottomBar leaveBottomBar=(LeaveBottomBar) findViewById(R.id.bottom_bar_leave);
		
		
		//底部按钮的回调
		leaveBottomBar.setOnBtnClickListener(new OnBtnClickListener() {
			
			@Override
			public void onUserLeaveClick() {
				 initFragment(1);
			}
			
			@Override
			public void onTeacherLeaveClick() {
				 initFragment(2);
			}
			@Override
			public void onTeacherReplaceClick() {
				initFragment(3);
			}
		
			@Override
			public void onLeaveExitClick() {
				finish();
			}
		});

		
		if (!isHeadTeacher) {
			leaveBottomBar.setVisibility(View.GONE);
		}
		
	}
	
	private void initData() {
		initFragment(1);
	}
	
	private void initFragment(int type) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		
		if (userFragment != null) {
			transaction.hide(userFragment);
		}
		
		if( teacherFragment != null ){
			transaction.hide(teacherFragment);
		}
		
		if( exchangeFragment != null ){
			transaction.hide(exchangeFragment);
		}
		
		 

		if (type == 1) {
			if (userFragment == null) {
				userFragment = new TeacherLeaveFragment();
				 transaction.add(R.id.leave_fl_main,userFragment,
						 "teacherLeave");
			 } else {
				 transaction.show(userFragment);
			 }
			
		}else if (type == 2) {
			if (teacherFragment == null) {
				teacherFragment = new TeacherFragment();
				transaction.add(R.id.leave_fl_main,teacherFragment, "studentLeave");
			} else {
				transaction.show(teacherFragment);
			}	 
		}else if (type == 3) {
			if (exchangeFragment == null) {
				
				exchangeFragment = new ExchangeFragment();
				transaction.add(R.id.leave_fl_main,exchangeFragment,
						 "exchangeLeave");
			 } else {
				 transaction.show(exchangeFragment);
			 }
//			if (studentFragment == null) {
//				studentFragment = new StudentLeaveFragment();
//				transaction.add(R.id.leave_fl_main,studentFragment, "studentLeave");
//			} else {
//				transaction.show(studentFragment);
//			}	 
		}
		
		transaction.commit();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FragmentManager fm = getSupportFragmentManager();
		List<Fragment> frags = fm.getFragments();

		for (Fragment f : frags) {
			if (f != null && f.isVisible() ) {
				f.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

}
