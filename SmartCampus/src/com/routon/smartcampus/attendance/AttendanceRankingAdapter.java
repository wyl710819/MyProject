package com.routon.smartcampus.attendance;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.routon.edurelease.R;
import com.routon.smartcampus.utils.StudentHelper;
import com.squareup.picasso.Picasso;

public class AttendanceRankingAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<AttendanceBean> mStudentList;
	public boolean mShowAbsenceCount = true;

	public AttendanceRankingAdapter(Context context, ArrayList<AttendanceBean> studentList) {
		this.mStudentList = studentList;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return mStudentList==null ? 0 : mStudentList.size();
	}

	@Override
	public Object getItem(int position) {
		return mStudentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View contentView, ViewGroup viewGroup) {
		final ViewHolder holder;
		if (contentView == null) {
			holder = new ViewHolder();
			contentView = View.inflate(mContext, R.layout.grid_item_view, null);
			holder.name_text = (TextView) contentView.findViewById(R.id.item_name);
			holder.student_img = (ImageView) contentView.findViewById(R.id.item_img);
			holder.info_text = (TextView) contentView.findViewById(R.id.item_info);
			holder.info_text.setVisibility(View.GONE);
			contentView.findViewById(R.id.badge_info_rl).setVisibility(View.GONE);

			contentView.setTag(holder);
		} else {
			holder = (ViewHolder) contentView.getTag();
		}
		

		AttendanceBean studentBean = mStudentList.get(position);
		
		StudentHelper.loadStudentImage(studentBean.imgSavePath, studentBean.imgUrl, mContext, holder.student_img);
		
		holder.name_text.setText(studentBean.empName);	
		return contentView;

	}

	private class ViewHolder {
		public TextView name_text;
		public ImageView student_img;
		public TextView info_text;
	}
}
