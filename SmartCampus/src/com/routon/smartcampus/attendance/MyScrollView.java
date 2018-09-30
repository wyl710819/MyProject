package com.routon.smartcampus.attendance;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView{
	
	private int downX, downY;
	private int mTouchSlop;
	private View mTouchUnDealView;
	 
	public MyScrollView(Context context) {
	    this(context, null);
	}
	 
	public MyScrollView(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}
	 
	public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
	    super(context, attrs, defStyleAttr);
	    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}
	
	public void setTouchUnDealView(View view){
		mTouchUnDealView = view;
	}
	 
 
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
	    int action = ev.getAction();
	    switch (action) {
	        case MotionEvent.ACTION_DOWN:
	            downX = (int) ev.getRawX();
	            downY = (int) ev.getRawY();
	            break;
	        case MotionEvent.ACTION_MOVE:
	            int moveY = (int) ev.getRawY();
	            // 判断是否滑动，若滑动就拦截事件
	            if( mTouchUnDealView != null ){
	       		 Rect r = new Rect();
	       		 mTouchUnDealView.getGlobalVisibleRect(r);
	       		 if( r.contains((int)(ev.getX()), (int)(ev.getY())) ){
	       			 return false;
	       		 }
	       	 }
	            break;
	        default:
	            break;
	    }
	 
	    return super.onInterceptTouchEvent(ev);
	}

}
