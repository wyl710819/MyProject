package com.routon.smartcampus.gradetrack;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class SlowScrollView extends HorizontalScrollView{

	public SlowScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void fling(int velocityX) {
		super.fling(velocityX/100);
	}

}
