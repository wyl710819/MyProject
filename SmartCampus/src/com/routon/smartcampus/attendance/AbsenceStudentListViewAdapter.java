package com.routon.smartcampus.attendance;

import java.io.File;
import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.utils.StudentHelper;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AbsenceStudentListViewAdapter extends BaseAdapter{

	private Context mContext;
	private List<AttendanceBean> mDataList;

	public AbsenceStudentListViewAdapter(Context context, List<AttendanceBean> absenceStudentBeans) {
		this.mContext=context;
		this.mDataList=absenceStudentBeans;
	}

	@Override
	public int getCount() {
		return mDataList==null ? 0 : mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ViewHolder viewHolder;
		if (convertView==null) {
			viewHolder=new ViewHolder();
			convertView=View.inflate(mContext,R.layout.absence_student_item, null);
			viewHolder.student_img=(ImageView) convertView.findViewById(R.id.student_img);
			viewHolder.name_text=(TextView) convertView.findViewById(R.id.name_text);
			viewHolder.absence_count_text=(TextView) convertView.findViewById(R.id.absence_count_text);
			viewHolder.taxis_text=(TextView) convertView.findViewById(R.id.taxis_text);
			viewHolder.arrow_text=(TextView) convertView.findViewById(R.id.arrow_text);
			
			
			convertView.setTag(viewHolder);
		}else {
			viewHolder=(ViewHolder) convertView.getTag();
		}
		
		AttendanceBean bean=mDataList.get(position);
		
		
		viewHolder.name_text.setText(bean.empName+"同学");
		setAbsenceText(viewHolder.absence_count_text,String.valueOf(bean.absenceCount));
		viewHolder.taxis_text.setText(String.valueOf(bean.absenceTaxis));
		viewHolder.arrow_text.setText(">");	
		StudentHelper.loadStudentImage(bean.imgSavePath, bean.imgUrl, mContext, viewHolder.student_img);
		
		return convertView;
	}
	
	private static class ViewHolder{
		public TextView name_text;
		public ImageView student_img;
		public TextView taxis_text;
		public TextView absence_count_text;
		public TextView arrow_text;
	}
	
	private void setAbsenceText(TextView name_text,String absenceNum) {
		String info = mContext.getResources().getString(R.string.student_absence_text);
		String infotext = String.format(info, absenceNum);
		int index[] = new int[1];
		index[0] = infotext.indexOf(absenceNum);
		SpannableStringBuilder style = new SpannableStringBuilder(infotext);

		style.setSpan(new ForegroundColorSpan(Color.RED), index[0], index[0] + (absenceNum).length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		name_text.setText(style);
	}

}
