package com.routon.smartcampus.gradetrack;

import com.routon.edurelease.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GradeTrackTitle extends LinearLayout{
	private ImageView mImageView;
	private TextView mTextViewTitle;
	private TextView mTextViewTxt;
	private RelativeLayout mLinearLayoutCenter;
	private ImageView mImageViewGrid;
	private BackClickListner mBackClickListner;
	private BackClickListner mBackClickListnerGrid;
	
	public GradeTrackTitle(Context context)
	{
		super(context);
	}
	

	public GradeTrackTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		View view = LayoutInflater.from(context).inflate(R.layout.grade_track_title, this);
		mImageView = (ImageView)view.findViewById(R.id.imgbtn_back);
		mImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mBackClickListner != null)
				{
					mBackClickListner.onBackClick(arg0);
				}
			}
		});
		mTextViewTitle = (TextView)view.findViewById(R.id.txt_gradetrack_title);
		mLinearLayoutCenter = (RelativeLayout)view.findViewById(R.id.relativelayout_gradetrack_title_center);
		mLinearLayoutCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mBackClickListner != null)
				{
					mBackClickListner.onTxtClick(v);
				}
			}
		});
		mImageViewGrid = (ImageView)view.findViewById(R.id.img_show_grid);
		mImageViewGrid.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mBackClickListner != null)
				{
					mBackClickListner.onMenuClick(arg0);
				}
			}
		});
		mTextViewTxt = (TextView)view.findViewById(R.id.txt_grade_class);
	}
	
	public void setTitle(String title)
	{
		mTextViewTitle.setText(title);
	}
	
	public void setText(String txt)
	{
		mTextViewTxt.setText(txt);
	}
	
	public void setTitleImgVisible(int txtVisibility,int imgVisibility)
	{
		mLinearLayoutCenter.setVisibility(txtVisibility);
		mImageViewGrid.setVisibility(imgVisibility);
	}
	
	public void setClickListner(BackClickListner backClickListner)
	{
		mBackClickListner = backClickListner;
	}
	
	
	public interface BackClickListner
	{
		void onBackClick(View view);
		void onTxtClick(View view);
		void onMenuClick(View view);
	}
	
	public void setMenuImg(int resId)
	{
		mImageViewGrid.setImageResource(resId);
	}
}
