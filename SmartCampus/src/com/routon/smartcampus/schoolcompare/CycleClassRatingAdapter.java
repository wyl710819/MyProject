package com.routon.smartcampus.schoolcompare;

import java.util.List;

import com.routon.edurelease.R;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CycleClassRatingAdapter extends BaseAdapter{

	private Context mContext;
	private List<CycleClassRatingBean> mDataList;
	public boolean showIndicatior = true;

	public CycleClassRatingAdapter(Context context, List<CycleClassRatingBean> beans) {
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
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.cycle_class_item, null);
			viewHolder.className=(TextView) convertView.findViewById(R.id.class_name);
			viewHolder.classScore=(TextView) convertView.findViewById(R.id.class_score);
			viewHolder.classTaxis=(TextView) convertView.findViewById(R.id.class_taxis);
			viewHolder.arrow_text=(TextView) convertView.findViewById(R.id.arrow_text);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if(showIndicatior){
			viewHolder.arrow_text.setVisibility(View.VISIBLE);
		}else{
			viewHolder.arrow_text.setVisibility(View.INVISIBLE);			
		}
		
		CycleClassRatingBean bean = mDataList.get(position);
		viewHolder.className.setText(bean.groupName);
		viewHolder.classScore.setText(String.valueOf(bean.totalScore));
		viewHolder.classTaxis.setText(String.valueOf(bean.rank));
		return convertView;
	}
	
	private static class ViewHolder {
		TextView className;
		TextView classScore;
		TextView classTaxis;
		TextView arrow_text;
		
	}
}
