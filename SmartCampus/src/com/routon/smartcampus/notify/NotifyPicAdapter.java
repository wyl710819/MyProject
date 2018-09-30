package com.routon.smartcampus.notify;

import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.routon.edurelease.R;
import com.routon.smartcampus.view.RemarkImgListviewAdapter.MyDeleteClickListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;

public class NotifyPicAdapter extends BaseAdapter {

	private Context mContext;
	private List<String> mPicLists;
	
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;

	public NotifyPicAdapter(Context context, List<String> picLists) {

		this.mContext = context;
		this.mPicLists = picLists;
		
		imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.empty_photo)
				.showImageOnFail(R.drawable.empty_photo).resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public int getCount() {
		return mPicLists == null ? 0 : mPicLists.size();
	}

	@Override
	public Object getItem(int position) {
		return mPicLists.get(position);
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
			convertView = View.inflate(mContext, R.layout.notify_grid_item, null);
			viewHolder.picView = (ImageView) convertView.findViewById(R.id.pic_view);
			viewHolder.spinner = (ProgressBar) convertView.findViewById(R.id.loading);
			viewHolder.deleteBtn = (ImageView) convertView.findViewById(R.id.delete_pic_img);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		
		if (position==0) {
			viewHolder.picView.setScaleType(ScaleType.CENTER_INSIDE);
			viewHolder.picView.setImageResource(R.drawable.notify_addpic_icon);
			viewHolder.deleteBtn.setVisibility(View.GONE);
		}else {
			viewHolder.deleteBtn.setVisibility(View.VISIBLE);
			viewHolder.picView.setScaleType(ScaleType.CENTER_CROP);
			String path = mPicLists.get(position);
			if (path != null && path.startsWith("/")) {
				path = "file://" + path;
			}

			imageLoader.displayImage(path, viewHolder.picView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					viewHolder.spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					
					viewHolder.spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					viewHolder.spinner.setVisibility(View.GONE);
				}
			});
			
		}
		
		
			viewHolder.deleteBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mDeleteClickListener!=null) {
						mDeleteClickListener.deleteClick(v, position);
					}
				}
			});
		
		
		return convertView;
	}

	private class ViewHolder {
		public ImageView deleteBtn;
		public ProgressBar spinner;
		public ImageView picView;

	}
	
	private DeleteClickListener mDeleteClickListener = null;

	public interface DeleteClickListener {
		public void deleteClick(View v, int position);
	}

	public void setDeleteClickListener(DeleteClickListener mDeleteClickListener) {
		this.mDeleteClickListener = mDeleteClickListener;
	}
}
