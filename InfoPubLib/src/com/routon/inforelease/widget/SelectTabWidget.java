package com.routon.inforelease.widget;

import com.routon.inforelease.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SelectTabWidget extends LinearLayout {
	private ViewGroup mViewGroup;
	public static final int ONLINE_INDEX = 0;
	public static final int OFFLINE_INDEX = 1;
	
	public interface SelClickListener {
	    void onClick(int index);
	}
	
	public SelectTabWidget(Context context) {
		super(context);
		initView(context);
		// TODO Auto-generated constructor stub
	}

	public SelectTabWidget(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    initView(context);
	}

	public SelectTabWidget(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    initView(context);
	}
	
	private SelClickListener mListener;
	public void setOnSelClickListener(SelClickListener listener){
		mListener = listener;
	}
	
	private int mSelIndex = 0;
	public int getSelIndex(){
		return mSelIndex;
	}
	
	private int setFocusView(View focusView){
		int count = mViewGroup.getChildCount();
		int focusIndex = 0;
		for(int i = 0; i < count; i++){
			View view = mViewGroup.getChildAt(i);
			if( view instanceof TextView ){
				TextView textview = (TextView)view;
				if( textview == focusView ){
					textview.setTextColor(this.getResources().getColor(R.color.blue));
					focusIndex = i;
				}else{
					textview.setTextColor(this.getResources().getColor(R.color.gray));
				}
			}
		}	
		Log.d("select tab widget","setFocusView focusIndex:"+focusIndex);
		mSelIndex = focusIndex;
		return focusIndex;
	}
	
	private void initView(Context context){
		LayoutInflater.from(context).inflate(R.layout.layout_select_tab_widget, this); 
		mViewGroup = (ViewGroup) this.getChildAt(0);
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int focusIndex = setFocusView(v);
				if( mListener != null ){
					mListener.onClick(focusIndex);
				}
			}
		}; 
		int count = mViewGroup.getChildCount();
		int first = 0;
		Log.d("select tab widget","setFocusView init view count:"+count);
		for(int i = 0; i < count; i++){
			View view = mViewGroup.getChildAt(i);
			if( view instanceof TextView ){
				view.setOnClickListener(listener);
				if( first == 0 ){
					Log.d("select tab widget","setFocusView first:"+first);
					setFocusView(view);
					first++;
				}
			}
		}
		mSelIndex = 0;
		
    }
}
