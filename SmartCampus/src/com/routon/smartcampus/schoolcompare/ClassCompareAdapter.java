package com.routon.smartcampus.schoolcompare;

import java.text.DecimalFormat;
import java.util.List;

import com.routon.edurelease.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ClassCompareAdapter extends BaseAdapter{

	private Context mContext;
	private List<ClassCompareBean> mDataList;
	public boolean isTaxis;
	public boolean isGradeList;

	public ClassCompareAdapter(Context context, List<ClassCompareBean> classStr) {
		this.mContext=context;
		this.mDataList=classStr;
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
		ViewHolder viewHolder;
		if (convertView==null) {
			viewHolder=new ViewHolder();
			convertView=View.inflate(mContext, R.layout.class_compare_item, null);
			viewHolder.calss_name_text=(TextView) convertView.findViewById(R.id.calss_name_text);
			viewHolder.arrow_text=(TextView) convertView.findViewById(R.id.arrow_text);
			viewHolder.score_text=(TextView) convertView.findViewById(R.id.score_text);
			viewHolder.taxis_text=(TextView) convertView.findViewById(R.id.taxis_text);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
		}
		
		ClassCompareBean bean=mDataList.get(position);
		
		viewHolder.calss_name_text.setText(bean.groupName);
		DecimalFormat df = new DecimalFormat("0.0");
		viewHolder.score_text.setText(String.valueOf(df.format(bean.compareScore)));
		viewHolder.arrow_text.setText(">");
		
		
		if (bean.isHeadTeacher) {
			viewHolder.calss_name_text.setTextColor(mContext.getResources().getColor(R.color.text_red));
			viewHolder.score_text.setTextColor(mContext.getResources().getColor(R.color.text_red));
			viewHolder.taxis_text.setTextColor(mContext.getResources().getColor(R.color.text_red));
		}
		
		if (isGradeList) {
			viewHolder.taxis_text.setVisibility(View.GONE);
			viewHolder.score_text.setVisibility(View.GONE);
		}else {
			if (isTaxis) {
				int rank = (int) bean.compareTaxis;
				viewHolder.taxis_text.setText(String.valueOf(rank));
				viewHolder.taxis_text.setVisibility(View.VISIBLE);
			}else {
				viewHolder.taxis_text.setVisibility(View.GONE);
			}
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView calss_name_text;
		TextView arrow_text;
		TextView score_text;
		TextView taxis_text;
	}

}
