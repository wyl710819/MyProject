package com.routon.smartcampus.schoolcompare;

import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.view.RippleView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RatingCycleAdapter extends BaseAdapter {

	private Context mContext;
	private List<RatingCycleBean> mDataList;

	public RatingCycleAdapter(Context context, List<RatingCycleBean> beans) {
		this.mContext = context;
		this.mDataList = beans;
	}

	@Override
	public int getCount() {
		return mDataList == null ? 0 : mDataList.size();
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
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.marking_cycle_item, null);
			viewHolder.cycle_text = (TextView) convertView.findViewById(R.id.cycle_text);
			viewHolder.arrow_text = (TextView) convertView.findViewById(R.id.arrow_text);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		RatingCycleBean bean = mDataList.get(position);
		viewHolder.cycle_text.setText(bean.startTime+"—"+bean.finishTime);
		viewHolder.arrow_text.setText(">");
		
		return convertView;
	}

	private static class ViewHolder {
		TextView cycle_text;
		TextView arrow_text;
	}
}
