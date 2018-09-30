package com.routon.common;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;

public class CustomTitleActivity extends BaseActivity {
	public boolean mBusyState = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTheme(R.style.create_title_bar);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	}
	
	public void initTitleBar(int titleId){
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.create_titlebar);
		this.setTitleText(titleId);	
		setDefaultBackListener();
	}
	
	public void initTitleBar(String title){
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.create_titlebar);
		this.setTitleText(title);
		setDefaultBackListener();
	}
	
	private void setDefaultBackListener(){
		//默认返回按键显示，且返回事件 finish处理
		setTitleBackBtnClickListener(new View.OnClickListener() {
					
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CustomTitleActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
			}
		});
	}
   
   protected void returnToLogin() {
		InfoReleaseApplication.returnToLogin(this);
   }
	
	protected void reportToast(int resId) {
		String text = this.getResources().getString(resId);
		reportToast(text);
	}
	
	protected void reportToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	private void setTitleText(int resid){
		TextView titleTextView = (TextView) findViewById(R.id.title);
		if(titleTextView != null){
			titleTextView.setText(resid);
			titleTextView.setVisibility(View.VISIBLE);
		}
	}
	
	private void setTitleText(String text){
		TextView titleTextView = (TextView) findViewById(R.id.title);
		if(titleTextView != null){
			titleTextView.setText(text);
			titleTextView.setVisibility(View.VISIBLE);
		}
	}
	
	public void setTitleBackground(Drawable drawable){
		RelativeLayout titleLayout = (RelativeLayout) findViewById(R.id.titlebar);
		if(titleLayout != null){
			titleLayout.setBackground(drawable);
			titleLayout.setVisibility(View.VISIBLE);
		}
	}
	
	public void setTitleBackBtnClickListener(OnClickListener listener){
		ImageButton	backBtn = (ImageButton) findViewById(R.id.back_btn);
		if(backBtn != null){
			backBtn.setVisibility(View.VISIBLE);
			backBtn.setOnClickListener(listener);
			mBackListener = listener;
		}
	}
	
	public void setTitleBackBtnHide(){
		ImageButton	backBtn = (ImageButton)findViewById(R.id.back_btn);
		if(backBtn != null){
			backBtn.setVisibility(View.INVISIBLE);
			mBackListener = null;
		}
	}
	
	public void setTitleNextImageBtnClickListener(int resId,OnClickListener listener){
		ImageButton nextButton = (ImageButton) findViewById(R.id.next_step);
		if(nextButton != null){
			nextButton.setImageResource(resId);
			nextButton.setVisibility(View.VISIBLE);
			nextButton.setOnClickListener(listener);
		}
	}
	
	public void setTitleNextBtnClickListener(String title,OnClickListener listener){
		Button nextButton = (Button) findViewById(R.id.next_step_tv);
		if(nextButton != null){
			nextButton.setText(title);
			nextButton.setVisibility(View.VISIBLE);
			nextButton.setOnClickListener(listener);
		}
	}
}
