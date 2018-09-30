package com.routon.inforelease.plan.create;

import java.util.ArrayList;

import com.routon.inforelease.R;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.widget.ClassPreviewView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ClassPreviewActivity extends Activity {
	private ArrayList<String> picUrlList;
	private String imagePath;
//	private ProgressDialog progressDialog;
	private ClassPreviewView picVp;
	private RelativeLayout titleLayout;
	private ClassInfoListdatasBean mClassInfoData;
	private boolean isVisible;
	private String mStartBy;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.class_preview_layout);

		// progressDialog = ProgressDialog.show(this, "", "...Loading...");

		Bundle bundle = ClassPreviewActivity.this.getIntent().getExtras();
		if (bundle != null) {
			imagePath = bundle.getString("path");
			picUrlList = bundle.getStringArrayList("content_list");
			mClassInfoData = (ClassInfoListdatasBean) bundle.getSerializable("class_info_datas");
			mStartBy=bundle.getString("startBy");
			
		}
		

		initView();
		if (picUrlList != null) {
			if (mStartBy!=null && mStartBy.equals("class_info_edit")) {
				mTitle = bundle.getString("title","");
				mSubTitle1 = bundle.getString("subtitle1","");
				mSubTitle2 = bundle.getString("subtitle2","");
				mDesc = bundle.getString("desc","");
				picVp.updateText(mTitle, mSubTitle1, mSubTitle2, mDesc);
			}
			
			picVp.setItems(picUrlList, imagePath, mClassInfoData,new ClassPreviewView.OnPreviewClickListener() {
				
				@Override
				public void onPreviewClickListener(View v, int position) {
					// TODO Auto-generated method stub
					if (!isVisible) {
	   					titleLayout.setVisibility(View.VISIBLE);
	   					isVisible=true;
	   				}else {
	   					titleLayout.setVisibility(View.INVISIBLE);
	   					isVisible=false;
	   				}
				}
			},ClassPreviewView.TYPE_CLASSPREVIEW);
		}
		

	}

	private void initView() {
		picVp = (ClassPreviewView) findViewById(R.id.pic_vp_view);
		titleLayout=(RelativeLayout) findViewById(R.id.titlebar);
		TextView titleText = (TextView) findViewById(R.id.title);
		ImageButton backButton = (ImageButton) findViewById(R.id.back_btn);
		ImageButton nextButton = (ImageButton) findViewById(R.id.next_step);
		titleText.setText(R.string.preview_pics_title);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});	

		nextButton.setVisibility(View.GONE);

		RelativeLayout classPic = (RelativeLayout) findViewById(R.id.class_pic_preview);
		int windowWidth = getWindowManager().getDefaultDisplay().getWidth();
		int windowHeight = getWindowManager().getDefaultDisplay().getHeight();
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(windowHeight, windowWidth);
		classPic.setLayoutParams(layoutParams);
		classPic.setPivotX(windowWidth / 2);
		classPic.setPivotY(windowWidth / 2);
		classPic.setRotation(90);
	}

	private String mTitle;
	private String mSubTitle1;
	private String mSubTitle2;
	private String mDesc;

}
