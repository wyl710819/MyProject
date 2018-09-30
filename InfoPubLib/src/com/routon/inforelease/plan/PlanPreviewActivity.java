package com.routon.inforelease.plan;

import java.util.ArrayList;
import com.routon.inforelease.R;
import com.routon.inforelease.widget.PlanPreviewView;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlanPreviewActivity extends Activity{
	private ArrayList<String> mPicUrlList;
	private String mCurImagePath;
	private PlanPreviewView mPlanPreviewView;
	private String mSubtitle;
	private String mTextBgAlpha;
	private String mTextBgColor;
	private String mTextColor;
	public static final String INTENTDATA_IMAGE_PATH = "path";
	public static final String INTENTDATA_PICLIST = "pic_list";
	public static final String INTENTDATA_SUBTITLE = "subtitle";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.plan_preview_layout);


		Bundle bundle = PlanPreviewActivity.this.getIntent().getExtras();
		if (bundle != null) {			
			mCurImagePath = bundle.getString(INTENTDATA_IMAGE_PATH);		
			mPicUrlList = bundle.getStringArrayList(INTENTDATA_PICLIST);
			mSubtitle = bundle.getString(INTENTDATA_SUBTITLE);	
			mTextColor = bundle.getString(Integer.toString(AdParams.TEXT_COLOR), "#042398");
			mTextBgColor = bundle.getString(Integer.toString(AdParams.TEXT_BG_COLOR), "#ffffff");
			mTextBgAlpha = bundle.getString(Integer.toString(AdParams.TEXT_BG_ALPHA), "80");
		}
		
		initView();
		PlanPreviewView.OnPreviewClickListener listener = new PlanPreviewView.OnPreviewClickListener() {
			
			@Override
			public void onPreviewClickListener(View v, int position) {
				// TODO Auto-generated method stub
				if ( findViewById(R.id.titlebar).getVisibility() != View.VISIBLE ) {
    			 	findViewById(R.id.titlebar).setVisibility(View.VISIBLE);
   				}else {
   					findViewById(R.id.titlebar).setVisibility(View.INVISIBLE);
   				}
			}
		}; 
		Log.d("PlanPreviewActivity","onCreate mPicUrlList:"+mPicUrlList+",mCurImagePath:"+mCurImagePath);
		if ( mPicUrlList != null ) {	
			if( mCurImagePath == null && mPicUrlList.size() > 0 ){
				mCurImagePath = mPicUrlList.get(0);
			}
			mPlanPreviewView.setItems(mPicUrlList, mCurImagePath,listener,PlanPreviewView.TYPE_PLANPREVIEW);
		}else {
			mPicUrlList=new ArrayList<String>();
			mPicUrlList.add("0");
			mPlanPreviewView.setItems(mPicUrlList, mPicUrlList.get(0),listener,PlanPreviewView.TYPE_PLANPREVIEW);
		}
		updatePreviewText(mSubtitle);
	}
	
	private void updatePreviewText(String text){
		mPlanPreviewView.setText(text,true,mTextColor,mTextBgColor,mTextBgAlpha,22);
	}


	private void initView() {
		mPlanPreviewView = (PlanPreviewView) findViewById(R.id.pic_vp_view);
		TextView titleText = (TextView) findViewById(R.id.title);
		ImageButton backButton = (ImageButton) findViewById(R.id.back_btn);
		ImageButton nextButton = (ImageButton) findViewById(R.id.next_step);
		titleText.setText(R.string.preview_pics_title);
		nextButton.setVisibility(View.GONE);	
		
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	
		RelativeLayout planPic = (RelativeLayout) findViewById(R.id.plan_pic_preview);
		int windowWidth = getWindowManager().getDefaultDisplay().getWidth();
		int windowHeight = getWindowManager().getDefaultDisplay().getHeight();
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(windowHeight, windowWidth);
		planPic.setLayoutParams(layoutParams);
		planPic.setPivotX(windowWidth / 2);
		planPic.setPivotY(windowWidth / 2);
		planPic.setRotation(90);
	}
    
	
	
}

