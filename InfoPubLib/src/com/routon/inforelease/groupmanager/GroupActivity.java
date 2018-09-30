package com.routon.inforelease.groupmanager;

import com.routon.common.BaseFragmentActivity;
import com.routon.inforelease.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class GroupActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(@Nullable Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_plan_list_fragment);
		
		this.setMoveBackEnable(true);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		// 开启事务
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		GroupFragment fragment = new GroupFragment();
		fragment.setReturnEnable(true);
		fragment.setArguments(this.getIntent().getExtras());
		transaction.add(R.id.fl_content, fragment);
		transaction.commit();
	}

}
