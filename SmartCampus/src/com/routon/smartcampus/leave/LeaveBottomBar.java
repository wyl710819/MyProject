package com.routon.smartcampus.leave;

import com.routon.edurelease.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LeaveBottomBar extends LinearLayout {

	private Context mContext;
	private OnBtnClickListener listener;
	private ImageView teachetLeaveImg;
	private ImageView userLeaveImg;
	private ImageView leaveExitImg;
	private TextView teachetLeaveTv;
	private TextView userLeaveTv;
	private TextView leaveExitTv;
	private ImageView teachetReplaceImg;
	private TextView teachetReplaceTv;

	public LeaveBottomBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.bottom_bar_leave, this);

		RelativeLayout userLeaveBtn = (RelativeLayout) findViewById(R.id.user_leave_rl);
		RelativeLayout teacherLeaveBtn = (RelativeLayout) findViewById(R.id.teacher_leave_rl);
		RelativeLayout teacherReplaceBtn = (RelativeLayout) findViewById(R.id.teacher_replace_rl);
		RelativeLayout leaveExitBtn = (RelativeLayout) findViewById(R.id.leave_exit_rl);
		
		userLeaveImg = (ImageView) findViewById(R.id.user_leave_img);
		teachetLeaveImg = (ImageView) findViewById(R.id.teacher_leave_img);
		teachetReplaceImg = (ImageView) findViewById(R.id.teacher_replace_img);
		leaveExitImg = (ImageView) findViewById(R.id.leave_exit_img);
		
		userLeaveTv = (TextView) findViewById(R.id.user_leave_tv);
		teachetLeaveTv = (TextView) findViewById(R.id.teacher_leave_tv);
		teachetReplaceTv = (TextView) findViewById(R.id.teacher_replace_tv);
		leaveExitTv = (TextView) findViewById(R.id.leave_exit_tv);
		
		userLeaveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener!=null) {
					setViewType(1);
					listener.onUserLeaveClick();
				}
			}
		});
		
		teacherLeaveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (listener!=null) {
					setViewType(2);
					listener.onTeacherLeaveClick();
				}
			}
		});
		
		teacherReplaceBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener!=null) {
					setViewType(3);
					listener.onTeacherReplaceClick();
				}
			}
		});
		
		leaveExitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener!=null) {
					setViewType(4);
					listener.onLeaveExitClick();
				}
			}
		});
	}
	
	public void setViewType(int type){
		switch (type) {
		case 1:
			userLeaveImg.setImageResource(R.drawable.user_leave_sel);
			userLeaveTv.setTextColor(Color.parseColor("#2784ff"));
			
			teachetLeaveImg.setImageResource(R.drawable.teacher_leave);
			teachetLeaveTv.setTextColor(Color.parseColor("#666666"));
			leaveExitImg.setImageResource(R.drawable.leave_exit);
			leaveExitTv.setTextColor(Color.parseColor("#666666"));
			teachetReplaceImg.setImageResource(R.drawable.teacher_replace);
			teachetReplaceTv.setTextColor(Color.parseColor("#666666"));
			
			break;
		case 2:
			
			teachetLeaveImg.setImageResource(R.drawable.teacher_leave_sel);
			teachetLeaveTv.setTextColor(Color.parseColor("#2784ff"));
			
			userLeaveImg.setImageResource(R.drawable.user_leave);
			userLeaveTv.setTextColor(Color.parseColor("#666666"));
			leaveExitImg.setImageResource(R.drawable.leave_exit);
			leaveExitTv.setTextColor(Color.parseColor("#666666"));
			teachetReplaceImg.setImageResource(R.drawable.teacher_replace);
			teachetReplaceTv.setTextColor(Color.parseColor("#666666"));
			
			break;
		case 3:
			
			teachetReplaceImg.setImageResource(R.drawable.teacher_replace_sel);
			teachetReplaceTv.setTextColor(Color.parseColor("#2784ff"));
			
			userLeaveImg.setImageResource(R.drawable.user_leave);
			userLeaveTv.setTextColor(Color.parseColor("#666666"));
			teachetLeaveImg.setImageResource(R.drawable.teacher_leave);
			teachetLeaveTv.setTextColor(Color.parseColor("#666666"));
			leaveExitImg.setImageResource(R.drawable.leave_exit);
			leaveExitTv.setTextColor(Color.parseColor("#666666"));
			
			break;
		case 4:
			leaveExitImg.setImageResource(R.drawable.leave_exit_sel);
			leaveExitTv.setTextColor(Color.parseColor("#2784ff"));
			
			teachetLeaveImg.setImageResource(R.drawable.teacher_leave);
			teachetLeaveTv.setTextColor(Color.parseColor("#666666"));
			userLeaveImg.setImageResource(R.drawable.user_leave);
			userLeaveTv.setTextColor(Color.parseColor("#666666"));
			teachetReplaceImg.setImageResource(R.drawable.teacher_replace);
			teachetReplaceTv.setTextColor(Color.parseColor("#666666"));
			break;
		default:
			break;
		}
	}
	
	interface OnBtnClickListener{
		public void onUserLeaveClick();
		public void onTeacherLeaveClick();
		public void onTeacherReplaceClick();
		public void onLeaveExitClick();
	}
	
	public void setOnBtnClickListener(OnBtnClickListener listener){
		this.listener = listener;
	}

}
