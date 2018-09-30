package com.routon.inforelease.snotice;



import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.routon.common.BaseFragmentActivity;
import com.routon.inforelease.R;

public class SNoticeListActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(@Nullable Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		this.setMoveBackEnable(true);
		
		setContentView(R.layout.activity_plan_list_fragment);
		
		 FragmentManager fragmentManager = getSupportFragmentManager();
		 // 开启事务
		 FragmentTransaction transaction = fragmentManager.beginTransaction();
		 SNoticeListFragment fragment = new SNoticeListFragment();
		 fragment.setArguments(this.getIntent().getExtras());
		 transaction.add(R.id.fl_content, fragment);
		 transaction.commit();
	}

}