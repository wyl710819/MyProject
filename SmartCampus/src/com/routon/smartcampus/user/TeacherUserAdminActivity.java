package com.routon.smartcampus.user;

import android.os.Bundle;
import android.view.View;

import com.routon.inforelease.R;
import com.routon.inforelease.UserAdminActivity;
import com.routon.inforelease.widget.SettingItem;

public class TeacherUserAdminActivity extends UserAdminActivity{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SettingItem item = (SettingItem)(findViewById(R.id.changerule));
		item.setMoreClicked(true);
		item.setName("角色选择");
		item.setVisibility(View.VISIBLE);
		item.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				UserHelper.showChangeRuleDialog(TeacherUserAdminActivity.this);
			}
		});
	}
}
