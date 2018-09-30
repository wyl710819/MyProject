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

public class HistoryScoreAdapter extends BaseAdapter{

	private Context mContext;
	private List<HistoryScoreBean> mDataList;

	public HistoryScoreAdapter(Context context, List<HistoryScoreBean> beans) {
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
			convertView = View.inflate(mContext, R.layout.history_score_item, null);
			viewHolder.compareTime=(TextView) convertView.findViewById(R.id.compare_time);
			viewHolder.compareScore=(TextView) convertView.findViewById(R.id.compare_score);
			viewHolder.compareTaxis=(TextView) convertView.findViewById(R.id.compare_taxis);
			viewHolder.flagImg=(ImageView) convertView.findViewById(R.id.flag_img);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		HistoryScoreBean bean = mDataList.get(position);
		viewHolder.compareTime.setText(bean.startTime+"—"+bean.endTime);
		viewHolder.compareScore.setText(String.valueOf(bean.score));
		viewHolder.compareTaxis.setText(String.valueOf(bean.place));
		if (bean.isIssued==1&&bean.redflagImgUrl!=null) {
			viewHolder.compareTaxis.setVisibility(View.GONE);
			viewHolder.flagImg.setVisibility(View.VISIBLE);
			Picasso.with(mContext).load(bean.redflagImgUrl).error(R.drawable.empty_photo).into(viewHolder.flagImg);
		}else {
			viewHolder.flagImg.setVisibility(View.GONE);
			viewHolder.compareTaxis.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView compareTime;
		TextView compareScore;
		TextView compareTaxis;
		ImageView flagImg;
		
	}
}
