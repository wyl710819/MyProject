package com.routon.inforelease.usercontrol;

import com.routon.common.BaseFragmentActivity;
import com.routon.inforelease.R;
import com.routon.inforelease.R.id;
import com.routon.inforelease.R.layout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class UserListActivity extends BaseFragmentActivity {

	@Override
	protected void onCreate(@Nullable Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		this.setMoveBackEnable(true);
		
		setContentView(R.layout.activity_plan_list_fragment);
		
		 FragmentManager fragmentManager = getSupportFragmentManager();
		 // 开启事务
		 FragmentTransaction transaction = fragmentManager.beginTransaction();
		 UserListFragment fragment = new UserListFragment();
		 fragment.setReturnEnable(true);
		 transaction.add(R.id.fl_content, fragment);
		 transaction.commit();
	}

}
