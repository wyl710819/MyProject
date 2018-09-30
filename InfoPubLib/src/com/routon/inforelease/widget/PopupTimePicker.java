package com.routon.inforelease.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TimePicker;

import com.routon.inforelease.R;

public class PopupTimePicker {
	private PopupWindow mPopWin;
	
	private TimePicker mTimePicker;

	public PopupTimePicker(Context context) {
		PopupWindow popWin = new PopupWindow();
		mPopWin = popWin;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.pop_time_picker, null);
		mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
		mTimePicker.setIs24HourView(true);
		view.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOnTimePickedListener != null) {
					mOnTimePickedListener.onTimePicked(mTimePicker.getCurrentHour().intValue(), mTimePicker.getCurrentMinute().intValue());
				}
				mPopWin.dismiss();
			}
		});
		
		popWin.setContentView(view);		
		popWin.setOutsideTouchable(true);
		popWin.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popWin.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
	}
	
	public void show(View anchor) {
		mPopWin.showAsDropDown(anchor);
	}
	
	private OnTimePickedListener mOnTimePickedListener;
	
	public interface OnTimePickedListener {
		void onTimePicked(int hour, int minute);
	}
}
