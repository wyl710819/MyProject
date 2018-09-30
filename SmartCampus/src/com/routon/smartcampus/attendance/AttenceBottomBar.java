package com.routon.smartcampus.attendance;

import com.routon.edurelease.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AttenceBottomBar extends LinearLayout{
	
	private TextView tvStart;
	private TextView tvSearch;
	private TextView tvExit;
	private Context mContext;
	
	public static final int TYPE_START = 1;
	public static final int TYPE_SEARCH = 2;
	
	private AttenceBottomClickListener attenceBottomClickListener;

	public AttenceBottomBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.bottom_bar_attence, this);
		tvStart = (TextView)findViewById(R.id.tv_attence_start_index);
		tvSearch = (TextView)findViewById(R.id.tv_attence_search_index);
		tvExit = (TextView)findViewById(R.id.tv_attence_exit_index);
		tvStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(attenceBottomClickListener != null){
					attenceBottomClickListener.onStartClick(v);
				}
			}
		});
		tvSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(attenceBottomClickListener != null){
					attenceBottomClickListener.onSearchClick(v);
				}
			}
		});
		tvExit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(attenceBottomClickListener != null){
					attenceBottomClickListener.onExitClick(v);
				}
			}
		});
	}
	
	public void setType(int type){
		switch (type) {
		case TYPE_START:
			Drawable startDrawAble = getResources().getDrawable(R.drawable.ic_student_attence_select_index);
			Drawable searchDrawAble = getResources().getDrawable(R.drawable.ic_search_attence_index);
			startDrawAble.setBounds(0, 0, startDrawAble.getMinimumWidth(), startDrawAble.getMinimumHeight());
			searchDrawAble.setBounds(0, 0, searchDrawAble.getMinimumWidth(), searchDrawAble.getMinimumHeight());
			tvStart.setTextColor(Color.WHITE);
			tvStart.setCompoundDrawables(null, startDrawAble,null, null);
			tvSearch.setTextColor(Color.parseColor("#666666"));
			tvSearch.setCompoundDrawables(null, searchDrawAble, null, null);
			break;
		case TYPE_SEARCH:
			Drawable startSelectDrawAble = getResources().getDrawable(R.drawable.ic_student_attence_index);
			Drawable searchSelectDrawAble = getResources().getDrawable(R.drawable.ic_search_attence_select_index);
			startSelectDrawAble.setBounds(0, 0, startSelectDrawAble.getMinimumWidth(), startSelectDrawAble.getMinimumHeight());
			searchSelectDrawAble.setBounds(0, 0, searchSelectDrawAble.getMinimumWidth(), searchSelectDrawAble.getMinimumHeight());
			tvSearch.setTextColor(Color.WHITE);
			tvSearch.setCompoundDrawables(null, searchSelectDrawAble, null, null);
			tvStart.setTextColor(Color.parseColor("#666666"));
			tvStart.setCompoundDrawables(null, startSelectDrawAble,null, null);
		default:
			break;
		}
	}

	public void setAttenceBottomClickListener(AttenceBottomClickListener attenceBottomClickListener){
		this.attenceBottomClickListener = attenceBottomClickListener;
	}
}
