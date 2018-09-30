package com.routon.smartcampus.student;

import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.bean.HistoryAwardBean;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryAwardAdapter extends BaseAdapter{
	
	private Context context;
	private List<HistoryAwardBean> historyAwardBeans;
	
	public HistoryAwardAdapter(Context context, List<HistoryAwardBean> historyAwardBeans){
		this.context = context;
		this.historyAwardBeans = historyAwardBeans;
	}

	@Override
	public int getCount() {
		return historyAwardBeans.size();
	}

	@Override
	public Object getItem(int arg0) {
		return historyAwardBeans.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HistoryAwardBean bean = historyAwardBeans.get(position);
		View view = null;
		ViewHolder viewHolder = null;
		if(convertView == null){
			view = LayoutInflater.from(context).inflate(R.layout.item_exchange_history, parent, false);
			viewHolder = new ViewHolder(view);
			convertView = view;
			convertView.setTag(viewHolder);
		}else {
			view = convertView;
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Picasso.with(context).load(bean.getAward().getImgUrl()).into(viewHolder.imgAward);
		viewHolder.tvAwardName.setText(bean.getAward().getName());
		viewHolder.tvAwardTime.setText(bean.getCreatetime());
		viewHolder.tvAwardDetail.setText(bean.getUsebonuspoint()+"积分兑换  "+bean.getTeacherName());
		return view;
	}
	
	class ViewHolder{
		TextView tvAwardName;
		ImageView imgAward;
		TextView tvAwardTime;
		TextView tvAwardDetail;
		
		public ViewHolder(View view){
			tvAwardName = (TextView)view.findViewById(R.id.tv_exchange_award_name);
			imgAward = (ImageView)view.findViewById(R.id.img_exchange_award_image);
			tvAwardTime = (TextView)view.findViewById(R.id.tv_exchange_award_time);
			tvAwardDetail = (TextView)view.findViewById(R.id.tv_exchange_award_detail);
		}
	}

}
