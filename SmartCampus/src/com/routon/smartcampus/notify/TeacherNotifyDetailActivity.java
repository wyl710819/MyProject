package com.routon.smartcampus.notify;

import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TeacherNotifyDetailActivity extends CustomTitleActivity {

	static final String TAG = "TeacherNotifyDetailActivity";
	Context m_context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teacher_notify_detail);
		m_context = this;
		
		initView();
		initData();
	}
	
	void initView() {
		initTitleBar(R.string.notify_detail_title);
		
		setTitleBackBtnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		setTitleNextImageBtnClickListener(R.drawable.ic_forward,
				new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

	}
	
	void initData() {
		
	}
}
