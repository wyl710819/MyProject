package com.routon.smartcampus.attendance;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;

public class SearchClassAttenceActivity extends BaseFragmentActivity{
	
	private ImageView backImg;
	private ImageView preWeekImg;
	private ImageView nextWeekImg;
	private AttenceSearchFragment classAttenceFragment;
	
	private FragmentManager fm;
	private FragmentTransaction transaction;
	private int classGroupId;
	private int teacherId;
	private String className;
	private WeekChangeListener weekChangeListener;
	
	@Override
	protected void onCreate(@Nullable Bundle arg0) {
		super.onCreate(arg0);
		initData();
		initView();
	}
	
	public void initData(){
		fm = getSupportFragmentManager();
		Bundle bundle = getIntent().getExtras();
		classGroupId = bundle.getInt("classGroupId", 0);
		teacherId = bundle.getInt("sid", 0);
		className = bundle.getString("className");
	}
	
	public void initView(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_class_attence);
		backImg = (ImageView)findViewById(R.id.img_search_class_attence_back);
		backImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		preWeekImg = (ImageView)findViewById(R.id.img_week_before);
		nextWeekImg = (ImageView)findViewById(R.id.img_week_after);
		preWeekImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(weekChangeListener != null){
					weekChangeListener.onPre();
				}
			}
		});
		nextWeekImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(weekChangeListener != null){
					weekChangeListener.onNext();
				}
			}
		});
		showClassAttenceFragment(classGroupId);
		setMoveBackEnable(true);
	}
	
	public void showClassAttenceFragment(int classGroupId){
		Bundle bundle = new Bundle();
		bundle.putInt("classGroupId", classGroupId);
		bundle.putInt("sid", teacherId);
		classAttenceFragment = new AttenceSearchFragment();
		classAttenceFragment.setArguments(bundle);
		transaction = fm.beginTransaction();
		transaction.add(R.id.fl_search_class_attence_main, classAttenceFragment);
		transaction.commit();
	}
	
	public void setWeekChangeListener(WeekChangeListener weekChangeListener){
		this.weekChangeListener = weekChangeListener;
	}
	
}
