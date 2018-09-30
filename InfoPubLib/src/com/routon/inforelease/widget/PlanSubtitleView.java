package com.routon.inforelease.widget;

import com.routon.inforelease.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class PlanSubtitleView extends RelativeLayout {

	public PlanSubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public PlanSubtitleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlanSubtitleView(Context context) {
		this(context, null);
	}

	private void init() {
		View.inflate(getContext(), R.layout.plan_edit_lv_item, this);
	}

}
