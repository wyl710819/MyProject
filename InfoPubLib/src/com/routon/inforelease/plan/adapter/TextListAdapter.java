package com.routon.inforelease.plan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.routon.inforelease.R;
import com.routon.inforelease.json.ResourceListdatasBean;

public class TextListAdapter extends BaseAdapter {
	private List<ResourceListdatasBean> mTextList;

	private Context mContext;
	private LayoutInflater mInflater;
	
	public TextListAdapter(Context context, List<ResourceListdatasBean> textList) {
		mContext = context;
		if (textList != null)
			mTextList = textList;
		else
			mTextList = new ArrayList<ResourceListdatasBean>();
		
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mTextList.size();
	}

	@Override
	public Object getItem(int position) {
		return mTextList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if (view == null) {
			view = mInflater.inflate(R.layout.plan_make_text_item, parent, false);
			holder = new ViewHolder();
			holder.checkbox = (CheckBox) view.findViewById(R.id.plan_check_box);
			holder.text = (TextView) view.findViewById(R.id.text_item);
			view.setTag(holder);
			
			holder.checkbox.setOnCheckedChangeListener(mOnPlanCheckChangeListener);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		ResourceListdatasBean bean = mTextList.get(position);
		holder.checkbox.setTag(bean);
//		holder.checkbox.setChecked(bean.isChecked());
		holder.text.setText(bean.content);
		
		return view;
	}
	
	private CompoundButton.OnCheckedChangeListener mOnPlanCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ResourceListdatasBean data = (ResourceListdatasBean) buttonView.getTag();
//			data.setChecked(isChecked);
		}
	};

	private class ViewHolder {
		CheckBox checkbox;
		TextView text;
	}
}
