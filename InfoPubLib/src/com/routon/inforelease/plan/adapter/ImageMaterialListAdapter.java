package com.routon.inforelease.plan.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.PlanMaterialrowsBean;
import com.routon.inforelease.plan.create.LruBitmapCache;
import com.routon.inforelease.plan.create.PicPreviewActivity;

public class ImageMaterialListAdapter extends MaterialListAdapter {
	private static final String TAG = "ImageMaterialListAdapter";
	
	private ImageLoader mImageLoader;
	
	public ImageMaterialListAdapter(Context context, List<PlanMaterialrowsBean> imageList) {
		super(context, imageList);
		
		// 初始化mImageLoader，并且传入了自定义的内存缓存
		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new LruBitmapCache()); // 初始化一个loader对象，可以进行自定义配置
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder(); 
			convertView = mInflater.inflate(R.layout.pictures_select_item, null);

			holder.imageView = (NetworkImageView)convertView.findViewById(R.id.image);
			holder.checkBox = (CheckBox)convertView.findViewById(R.id.item_select);
			holder.checkBox.setOnCheckedChangeListener(mOnPlanCheckChangeListener);		
			holder.imageView.setOnClickListener(mOnImageViewClickListener);

			convertView.setTag(holder);
			
		}else{
			holder = (ViewHolder) convertView.getTag();
		}

		PlanMaterialrowsBean dataItem = (PlanMaterialrowsBean) getItem(position);
		
		holder.checkBox.setTag(dataItem);
		holder.imageView.setTag(dataItem);
		
		holder.imageView.setDefaultImageResId(R.drawable.default_pic);
		holder.imageView.setErrorImageResId(R.drawable.default_pic);
		
		String imagePath = dataItem.thumbnail;
//		Log.i(TAG, "imagePath:"+imagePath);
		holder.checkBox.setChecked(selectMaterails.contains(dataItem));
		
		int width = mContext.getResources().getDimensionPixelSize(R.dimen.pic_select_item_w);
		int height = mContext.getResources().getDimensionPixelSize(R.dimen.pic_select_item_h);
		String smallImageUrl = new String();
		if (imagePath.length() > 0) {
			int lastIndexOfDot = imagePath.lastIndexOf("."); 
			int tailLength = imagePath.length() - lastIndexOfDot;
			smallImageUrl += imagePath.substring(0, lastIndexOfDot);
			smallImageUrl += "_" + width + "x" + height;
			smallImageUrl += imagePath.substring(imagePath.length()-tailLength, imagePath.length());
			Log.i(TAG, "-------position:" +position+ "  smallImageUrl:"+smallImageUrl);
		}
		
		// 开始加载网络图片
		holder.imageView.setImageUrl(smallImageUrl, mImageLoader);
		
		if (!mEditable)
			holder.checkBox.setVisibility(View.GONE);
		
		return convertView;
	}
	
	private CompoundButton.OnCheckedChangeListener mOnPlanCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			PlanMaterialrowsBean dataItem = (PlanMaterialrowsBean) buttonView.getTag();
			
			if(selectMaterails.contains(dataItem) && isChecked == false){
				selectMaterails.remove(dataItem);
			}
			
			if(isChecked && !selectMaterails.contains(dataItem)){
				Log.v(TAG, "add select: " + dataItem);
				selectMaterails.add(dataItem);
			}
		}
	};
	
	private View.OnClickListener mOnImageViewClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			PlanMaterialrowsBean dataItem = (PlanMaterialrowsBean) v.getTag();
//			if(selectMaterails.contains(dataItem)){
//				selectMaterails.remove(dataItem);
//			} else {
//				Log.v(TAG, "add select: " + dataItem);
//				selectMaterails.add(dataItem);
//			}
//			
//			notifyDataSetChanged();
			
			Intent previewIntent = new Intent(mContext, PicPreviewActivity.class);
			previewIntent.putExtra("path", dataItem.thumbnail);
			mContext.startActivity(previewIntent);
		}
	};

	private class ViewHolder {
		CheckBox checkBox;
		NetworkImageView imageView;
	}
}
