package com.routon.smartcampus.notify;

import java.util.List;

import com.routon.edurelease.R;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class NotifyClassAdapter extends BaseAdapter {

	private Context mContext;
	private List<NotifyClassBean> mDatas;
	

	public NotifyClassAdapter(Context context, List<NotifyClassBean> classBeans) {

		this.mContext = context;
		this.mDatas = classBeans;
	}

	@Override
	public int getCount() {
		return mDatas == null ? 0 : mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.notify_class_item, null);
			viewHolder.className = (TextView) convertView.findViewById(R.id.class_name);
			viewHolder.classBox = (CheckBox) convertView.findViewById(R.id.class_box);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		final NotifyClassBean bean=mDatas.get(position);
		viewHolder.className.setText(bean.className);
		viewHolder.classBox.setChecked(bean.isChecked);
		
		viewHolder.classBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOnCheckedListener!=null) {
					mOnCheckedListener.onChecked(position, !bean.isChecked);
				}
				
			}
		});
		
		return convertView;
	}


	public interface OnCheckedListener{
		public void onChecked(int i,boolean b);
	};
	private OnCheckedListener mOnCheckedListener;
	
	public void setOnCheckedListener(OnCheckedListener onCheckedListener){
		mOnCheckedListener=onCheckedListener;
	}
	
	private class ViewHolder {
		public CheckBox classBox;
		public TextView className;

	}
	
}
