package com.routon.smartcampus.leave;

import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class StudentLeaveActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.student_leave_activity);
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		StudentLeaveFragment slf = new StudentLeaveFragment();
		ft.add(R.id.slf1, slf);
		ft.commit();
		setMoveBackEnable(true);
		
	}

}
