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

import com.routon.inforelease.R;
import com.routon.inforelease.json.ResourceListdatasBean;

public class ImageListAdapter extends BaseAdapter {
	private List<ResourceListdatasBean> mImageList;

	private Context mContext;
	private LayoutInflater mInflater;
	
	public ImageListAdapter(Context context, List<ResourceListdatasBean> imageList) {
		mContext = context;
		if (imageList != null)
			mImageList = imageList;
		else
			mImageList = new ArrayList<ResourceListdatasBean>();
		
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mImageList.size();
	}

	@Override
	public Object getItem(int position) {
		return mImageList.get(position);
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
			view = mInflater.inflate(R.layout.plan_make_picture_item, parent, false);
			holder = new ViewHolder();
			holder.checkbox = (CheckBox) view.findViewById(R.id.plan_check_box);
			holder.image = (ImageView) view.findViewById(R.id.image_item);
			view.setTag(holder);
			
			holder.checkbox.setOnCheckedChangeListener(mOnPlanCheckChangeListener);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		ResourceListdatasBean bean = mImageList.get(position);
		holder.checkbox.setTag(bean);
//		holder.checkbox.setChecked(bean.isChecked());
		holder.image.setImageURI(Uri.parse(bean.content));
		
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
		ImageView image;
	}
}
