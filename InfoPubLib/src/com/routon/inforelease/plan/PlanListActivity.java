package com.routon.inforelease.plan;

import com.routon.common.BaseFragmentActivity;
import com.routon.inforelease.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class PlanListActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(@Nullable Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_plan_list_fragment);
		
		 FragmentManager fragmentManager = getSupportFragmentManager();
		 // 开启事务
		 FragmentTransaction transaction = fragmentManager.beginTransaction();
		 PlanListFragment programFragment = new PlanListFragment();
		 programFragment.setArguments(this.getIntent().getExtras());
		 transaction.add(R.id.fl_content, programFragment);
		 transaction.commit();
	}

}
