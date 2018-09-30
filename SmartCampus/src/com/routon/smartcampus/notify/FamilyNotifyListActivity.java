package com.routon.smartcampus.notify;

import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

public class FamilyNotifyListActivity extends CustomTitleActivity {

	static final String TAG = "TeacherNotifyListActivity";
	Context m_context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_family_notify_list);
		m_context = this;
		
		initView();
		initData();
	}
	
	void initView() {
		initTitleBar(R.string.notify_list_title);
		
		setTitleBackBtnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});


	}
	
	void initData() {
		
	}
}
