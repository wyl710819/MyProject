package com.routon.smartcampus.flower;

import java.util.ArrayList;

import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.utils.MyBundleName;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

//添加常用小红花一级分类选择界面
public class BadgeAddSelActivity extends CustomTitleActivity{
	private GridView gridView;
	private ArrayList<BadgeInfo> badgeList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_badge_sel);
		
		initView();
		initData();
	}

	private void initView() {
		
		initTitleBar("添加常用小红花");
		setTitleBackground(this.getResources().getDrawable(R.drawable.student_title_bg));

		setTitleBackBtnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		gridView = (GridView) findViewById(R.id.badge_gridView);
		
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(BadgeAddSelActivity.this, BadgeRemarkActivity.class);
				intent.putExtra(MyBundleName.BADGE_INFO, BadgeInfoUtil.getFlowerList().get(position));
				intent.putExtra(MyBundleName.BADGE_INFO_LIST,badgeList);
				startActivityForResult(intent, 1);
			}
		});
	}

	private void initData() {
		badgeList = (ArrayList<BadgeInfo>) this.getIntent().getSerializableExtra(MyBundleName.BADGE_INFO_LIST);
		FlowerGridAdapter adapter = new FlowerGridAdapter(BadgeAddSelActivity.this, BadgeInfoUtil.getFlowerList());
		gridView.setAdapter(adapter);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 1) {//添加常用小红花完毕后，销毁当前界面	
				setResult(RESULT_OK, data);
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
		
	}

}
