package com.routon.smartcampus.homework;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.LruBitmapCache;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.FamilyRecyclerImgHolder;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.OnItemClickListener;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FamilyHomeworkGridImgAdapter extends BaseAdapter {

	private Context mContext;
	private List<FeedbackHomeworkFileBean> mDatas;
	private static ImageLoader mImageLoader;
	
	public FamilyHomeworkGridImgAdapter(Context context, List<FeedbackHomeworkFileBean> datas) {
		this.mContext = context;
		this.mDatas = datas;
		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue,
				new LruBitmapCache());
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
		final ImgViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ImgViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.homework_imgcontent_item, null);

			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.homework_item_img);
			viewHolder.audioItemRl=(RelativeLayout) convertView.findViewById(R.id.class_record_display);
			viewHolder.recordTimeView=(TextView) convertView.findViewById(R.id.tv_record_time);
			viewHolder.playRecordImg=(ImageView) convertView.findViewById(R.id.class_play_record_img);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ImgViewHolder) convertView.getTag();
		}

//		int avatorW = (int) mContext.getResources().getDimension(R.dimen.badge_remark_image_w);
//		int avatorH = (int) mContext.getResources().getDimension(R.dimen.badge_remark_image_h);
		
//		String imagePath=imgUrlList.get(position);
//		if (imagePath.length() > 0) {
//			String smallImageUrl = new String();
//			int lastIndexOfDot = imagePath.lastIndexOf(".");
//			int tailLength = imagePath.length() - lastIndexOfDot;
//			smallImageUrl += imagePath.substring(0, lastIndexOfDot);
//			smallImageUrl += "_" + avatorW + "x" + avatorH;
//			smallImageUrl += imagePath.substring(imagePath.length()
//					- tailLength, imagePath.length());
//
//			// 开始加载网络图片
//			viewHolder.imageView.setImageUrl(smallImageUrl, mImageLoader);
//		}
		
		if (mDatas.get(position).fileType==1) {
			viewHolder.imageView.setVisibility(View.VISIBLE);
			viewHolder.audioItemRl.setVisibility(View.GONE);
			if (mDatas.get(position).fileUrl!=null) {
				Picasso.with(mContext).load(mDatas.get(position).fileUrl).placeholder(R.drawable.empty_photo)
				.error(R.drawable.empty_photo).into(viewHolder.imageView);
			}
			
		}else if (mDatas.get(position).fileType==2){
			viewHolder.imageView.setVisibility(View.GONE);
			viewHolder.audioItemRl.setVisibility(View.VISIBLE);

			viewHolder.recordTimeView.setText(mDatas.get(position).audioLength+"s");
			
		}
		
		if (mImgClickListener != null) {
			viewHolder.imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mImgClickListener.imgClick(position);
				}
			});

		}
		
		if (mAudioClickListener != null) {
			viewHolder.audioItemRl.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mAudioClickListener.audioClick(v, position, viewHolder);
				}
			});

		}
		

		return convertView;
	}

	public class ImgViewHolder {
		ImageView playRecordImg;
		ImageView imageView;
		RelativeLayout audioItemRl;
		TextView recordTimeView;
	}
	
	private OnImgClickListener mImgClickListener;

	public interface OnImgClickListener {
		public void imgClick(int position);
	}

	public void setImgClickListener(OnImgClickListener mImgClickListener) {
		this.mImgClickListener = mImgClickListener;
	}
	
	private OnAudioClickListener mAudioClickListener;

	public interface OnAudioClickListener {
		public void audioClick(View v, int position, ImgViewHolder imgHolder);
	}

	public void setAudioClickListener(OnAudioClickListener mAudioClickListener) {
		this.mAudioClickListener = mAudioClickListener;
	}

}
