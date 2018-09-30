package com.routon.smartcampus.schoolcompare;

import java.util.List;

import com.routon.edurelease.R;
import com.routon.edurelease.R.layout;
import com.routon.inforelease.plan.adapter.ClassInfoEditListAdapter.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SubProjectAdapter extends BaseAdapter{
	private Context context;
	private List<SubprojectBean> subprojectBeans;
	
	public SubProjectAdapter(Context context, List<SubprojectBean> subprojectBeans){
		this.context = context;
		this.subprojectBeans = subprojectBeans;
	}
	
	@Override
	public int getCount() {
		return subprojectBeans == null?0:subprojectBeans.size();
	}
	
	@Override
	public Object getItem(int arg0) {
		return subprojectBeans.get(arg0);
	}
	
	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.class_compare_item, parent, false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		SubprojectBean subprojectBean = subprojectBeans.get(position);
		viewHolder.txtTitle.setText(subprojectBean.name);
		viewHolder.txtArrow.setText(">");
		return convertView;
	}
	
	class ViewHolder{
		TextView txtTitle;
		TextView txtArrow;
		public ViewHolder(View view){
			txtTitle = (TextView)view.findViewById(R.id.calss_name_text);
			txtArrow = (TextView)view.findViewById(R.id.arrow_text);
		}
	}

}
