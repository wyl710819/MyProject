package com.routon.smartcampus.gradetrack;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class TwoListLayout extends LinearLayout{
	private int width;
    private boolean isHorizonMove = false;
    private Context context;

	public TwoListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		int width = (getResources().getDisplayMetrics().widthPixels-
				ClassGradesListAdapter.dip2px(context, (float)(30+6-1)))/6;
	}
	
	
	/**
	 * @return 返回true则中断传递子view事件
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return false;
	}

    /*public boolean onTouchEvent(MotionEvent event) {  
		
    	Log.d("MainActivity", "x:"+event.getX()+"  y:"+event.getY());
    	if(event.getAction() == MotionEvent.ACTION_DOWN)
    	{
    		if(event.getX()>=10 && event.getX() <= 160 && 
    				event.getY() >=0 && event.getY() <= 110)
    			return true;
    			
    	}
    	Log.d("MainActivity", "x:"+event.getX()+"  y:"+event.getY());
    	int count = getChildCount();
    	for(int i = 0;i<count;i++)
    	{
    		View child = getChildAt(i);
    		child.dispatchTouchEvent(event);
    	}
        return true;  
    } */ 
	
}
