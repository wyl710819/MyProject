package com.routon.smartcampus.leave;

import com.routon.edurelease.R;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StudentLeaveRankingAdapter extends CursorAdapter {


	public StudentLeaveRankingAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
	}
	String getTimeText(int totalTime) {
		int hour = totalTime / 60;
		int min = totalTime % 60;
		if (hour == 0) {
			return String.valueOf(min) + "分";
		} else {
			return String.valueOf(hour) + "时" + String.valueOf(min) + "分";
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView lstv1 = (TextView) view.findViewById(R.id.leave_stat_tv1);
		TextView lstv2 = (TextView) view.findViewById(R.id.leave_stat_tv2);
		TextView lstv3 = (TextView) view.findViewById(R.id.leave_stat_tv3);
		TextView lstv4 = (TextView) view.findViewById(R.id.leave_stat_tv4);

		int ranking = cursor.getInt(cursor.getColumnIndex("ranking"));
		String studentName = cursor.getString(cursor.getColumnIndex("studentName"));
		String leaveTime = cursor.getString(cursor.getColumnIndex("leaveTime"));
		int totalTime = cursor.getInt(cursor.getColumnIndex("totalTime"));
		
		lstv1.setText(String.valueOf(ranking));
		lstv2.setText(studentName);
		lstv3.setText(leaveTime+"次");
		lstv4.setText(getTimeText(totalTime));
		
		switch (ranking) {
		case 1:
			lstv1.setTextColor(0xfffd2222);
			lstv1.setTypeface(Typeface.DEFAULT_BOLD);
			break;
		case 2:
			lstv1.setTextColor(0xfffd7a22);
			lstv1.setTypeface(Typeface.DEFAULT_BOLD);
			break;
		case 3:
			lstv1.setTextColor(0xffffc000);
			lstv1.setTypeface(Typeface.DEFAULT_BOLD);
			break;
        default:
			lstv1.setTextColor(0xff999999);
			lstv1.setTypeface(Typeface.DEFAULT);
            break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater li = LayoutInflater.from(context);
		View v = li.inflate(R.layout.student_leave_ranking_item, parent, false);
		return v;
	}

}
