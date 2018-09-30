package com.routon.smartcampus.leave;

import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.bean.FamilyLeaveBean;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FamilyLeaveAdapter extends BaseAdapter{
	private Context context;
	private List<FamilyLeaveBean> familyLeaveBeans;
	
	public FamilyLeaveAdapter(Context context, List<FamilyLeaveBean> familyLeaveBeans){
		this.context = context;
		this.familyLeaveBeans = familyLeaveBeans;
	}

	@Override
	public int getCount() {
		return familyLeaveBeans.size();
	}

	@Override
	public Object getItem(int position) {
		return familyLeaveBeans.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder viewHolder = null;
		FamilyLeaveBean familyLeaveBean = familyLeaveBeans.get(position);
		if(convertView == null){
			view = LayoutInflater.from(context).inflate(R.layout.item_family_leave_result, null, false);
			viewHolder = new ViewHolder(view);
			convertView = view;
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
			view = convertView;
		}
		String startTime = familyLeaveBean.getStartTime().substring(0, familyLeaveBean.getStartTime().length()-3);
		String endTime = familyLeaveBean.getEndTime().substring(0, familyLeaveBean.getEndTime().length()-3);
		viewHolder.timeTv.setText(startTime+"至"+endTime);
		viewHolder.reasonTv.setText("请假事由:"+familyLeaveBean.getReason());
		if(familyLeaveBean.getStatus() == 0){
			viewHolder.statusTv.setTextColor(Color.parseColor("#14ba23"));
			viewHolder.statusTv.setText("待审批");
		}else if(familyLeaveBean.getStatus() == 1){
			viewHolder.statusTv.setTextColor(Color.parseColor("#999999"));
			viewHolder.statusTv.setText("已批准");
		}else if(familyLeaveBean.getStatus() == 2){
			viewHolder.statusTv.setTextColor(Color.parseColor("#fd2222"));
			viewHolder.statusTv.setText("已驳回");
		}
		return view;
	}
	
	class ViewHolder{
		TextView timeTv;
		TextView reasonTv;
		TextView statusTv;
		
		public ViewHolder(View view){
			timeTv = (TextView)view.findViewById(R.id.tv_family_leave_item_time);
			reasonTv = (TextView)view.findViewById(R.id.tv_family_leav_item_reason);
			statusTv = (TextView)view.findViewById(R.id.tv_family_leav_item_status);
		}
	}

}
