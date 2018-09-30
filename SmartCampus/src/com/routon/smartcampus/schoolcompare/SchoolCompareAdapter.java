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

public class SchoolCompareAdapter extends BaseAdapter{

	private Context mContext;
	private List<CompareClassTypeBean> mDataList;

	public SchoolCompareAdapter(Context context, List<CompareClassTypeBean> classStr) {
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
			convertView=View.inflate(mContext, R.layout.school_compare_item, null);
			viewHolder.compare_type_img=(ImageView) convertView.findViewById(R.id.compare_type_img);
			viewHolder.calss_type_text=(TextView) convertView.findViewById(R.id.calss_type_text);
			viewHolder.arrow_text=(TextView) convertView.findViewById(R.id.arrow_text);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
		}
		
		CompareClassTypeBean bean=mDataList.get(position);
		viewHolder.calss_type_text.setText(bean.name);
		viewHolder.arrow_text.setText(">");
		
		if( bean.redflagImgUrl != null && bean.redflagImgUrl.isEmpty() == false ){
			Picasso.with(mContext).load(bean.redflagImgUrl).placeholder(R.drawable.default_student)
				.error(R.drawable.default_student).into(viewHolder.compare_type_img);
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		ImageView compare_type_img;
		TextView calss_type_text;
		TextView arrow_text;
	}

}
