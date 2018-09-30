package com.routon.smartcampus.homework;

import java.util.List;

import com.routon.edurelease.R;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class FamilyHomeworkImgListViewAdapter extends BaseAdapter {

	private Context mContext;
	private List<String> imgUrlList;

	public FamilyHomeworkImgListViewAdapter(Context context, List<String> imgUrlList) {
		this.mContext = context;
		this.imgUrlList = imgUrlList;
	}

	@Override
	public int getCount() {
		return imgUrlList == null ? 0 : imgUrlList.size();
	}

	@Override
	public Object getItem(int position) {
		return imgUrlList.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.homework_img_listview_item, null);

			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.homework_item_img);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// int avatorW = (int)
		// mContext.getResources().getDimension(R.dimen.homework_image_w);
		// int avatorH = (int)
		// mContext.getResources().getDimension(R.dimen.homework_image_h);
		if( imgUrlList.get(position) != null && imgUrlList.get(position).isEmpty() == false ){
			Picasso.with(mContext).load(imgUrlList.get(position)).placeholder(R.drawable.empty_photo)
				.error(R.drawable.empty_photo).into(viewHolder.imageView);
		}

		return convertView;
	}

	public class ViewHolder {
		ImageView imageView;
	}

}
