package com.routon.smartcampus.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.widget.SettingItem;

public class ParentUserAdminActivity extends CustomTitleActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parent_useradmin);
		
		this.initTitleBar(R.string.userinfo);
		
		SettingItem item = (SettingItem)(findViewById(R.id.childmanageitem));
		item.setMoreClicked(true);
		item.setName("孩子管理");
		item.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ParentUserAdminActivity.this, ChildListActivity.class);
				startActivity(intent);
			}
		});
		
		item = (SettingItem)(findViewById(R.id.changeruleitem));
		item.setMoreClicked(true);
		item.setName("角色选择");
		item.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				UserHelper.showChangeRuleDialog(ParentUserAdminActivity.this);
			}
		});
	}
}
