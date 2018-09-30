package com.routon.inforelease.plan.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.classinfo.ClassInfoEditActivity;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.ClassInfoListfilesBean;
import com.routon.inforelease.plan.create.LruBitmapCache;
import com.routon.inforelease.plan.create.PicPreviewActivity;
import com.routon.inforelease.widget.ClassPreviewView;

public class ClassInfoEditListAdapter extends CheckedListAdapter<ClassInfoListfilesBean> {
	private ImageLoader mImageLoader;
	protected boolean mEditable = true;
	
	private String mTitle;
	private String mSubTitle1;
	private String mSubTitle2;
	private String mDesc;

	public ClassInfoEditListAdapter(Context context, ArrayList<ClassInfoListfilesBean> mResIds) {
		super(context, mResIds);
		
		// 初始化mImageLoader，并且传入了自定义的内存缓存
		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new LruBitmapCache()); // 初始化一个loader对象，可以进行自定义配置
	}
	
	public void setEditable(boolean editable) {
		mEditable = editable;
	}
	
	public void setDatas(List<ClassInfoListfilesBean> datas) {
		this.mMaterialList = datas;
		this.notifyDataSetChanged();
	}
	
	public void updateText(String title, String subTitle1, String subTitle2, String desc) {
		mTitle = title;
		mSubTitle1 = subTitle1;
		mSubTitle2 = subTitle2;
		mDesc = desc;
		
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder(); 
			convertView = mInflater.inflate(R.layout.layout_class_info_edit_grid_item, null);
			holder.imageView = (ClassPreviewView) convertView.findViewById(R.id.image);
//			holder.checkBox = (CheckBox)convertView.findViewById(R.id.item_select);
//			holder.imageView.setOnClickListener(mOnClickListener);
			
			holder.titleView = (TextView)convertView.findViewById(R.id.item_title);
			holder.subTitle1View = (TextView) convertView.findViewById(R.id.item_subtitle1);
			holder.subTitle2View = (TextView) convertView.findViewById(R.id.item_subtitle2);
			holder.descView = (TextView) convertView.findViewById(R.id.item_desc);
			
			convertView.setTag(holder);			
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
//		if( mEditable == false ){
//			holder.checkBox.setVisibility(View.GONE);
//		}
		
		holder.titleView.setText(mTitle);
		holder.subTitle1View.setText(mSubTitle1);
		holder.subTitle2View.setText(mSubTitle2);
		holder.descView.setText(mDesc);
		
//		holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				ClassInfoListfilesBean dataItem = (ClassInfoListfilesBean) getItem(position);
//				
//				if(selectMaterails.contains(dataItem) && isChecked == false){
//					selectMaterails.remove(dataItem);
//				}
//				
//				if(isChecked && !selectMaterails.contains(dataItem)){
//					selectMaterails.add(dataItem);
//				}
//			}
//		});

		ClassInfoListfilesBean dataItem = (ClassInfoListfilesBean) getItem(position);
		
//		holder.checkBox.setChecked(selectMaterails.contains(dataItem));
		List<String> imagePathList=new ArrayList<String>();
		String imagePath ;
		
		String smallImageUrl = new String();
		
		int width = mContext.getResources().getDimensionPixelSize(R.dimen.pic_select_item_w);
		int height = mContext.getResources().getDimensionPixelSize(R.dimen.pic_select_item_h);
		for (int i = 0; i < mMaterialList.size(); i++) {
			imagePath=mMaterialList.get(i).content;
			if (imagePath.length() > 0) {
				int lastIndexOfDot = imagePath.lastIndexOf("."); 
				int tailLength = imagePath.length() - lastIndexOfDot;
				smallImageUrl += imagePath.substring(0, lastIndexOfDot);
				smallImageUrl += "_" + width + "x" + height;
				smallImageUrl += imagePath.substring(imagePath.length()-tailLength, imagePath.length());
				imagePathList.add(smallImageUrl);
				smallImageUrl="";
			}
		}
		
		
		
		
		// 开始加载网络图片
//		holder.imageView.setImageUrl(smallImageUrl, mImageLoader);
//		holder.imageView.setTag(dataItem);
//		holder.imageView.setItems(imagePathList, imagePathList.get(0),"preview",dataItem);
		return convertView;
	}
	
//	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
//		
//		@Override
//		public void onClick(View v) {
//			ClassInfoListfilesBean bean = (ClassInfoListfilesBean) v.getTag();
//			Intent previewIntent = new Intent(mContext, PicPreviewActivity.class);
//			previewIntent.putExtra("path", bean.content);
//			mContext.startActivity(previewIntent);
//		}
//	};

	public class ViewHolder{
		ClassPreviewView imageView;
//		CheckBox checkBox;
		
		TextView titleView;
		TextView subTitle1View;
		TextView subTitle2View;
		TextView descView;
	}	

}
