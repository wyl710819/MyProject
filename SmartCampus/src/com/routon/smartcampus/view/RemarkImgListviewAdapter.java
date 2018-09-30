package com.routon.smartcampus.view;

import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.routon.edurelease.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class RemarkImgListviewAdapter extends BaseAdapter {

	private Context mContext;
	private List<String> mList;
	private LayoutInflater mInflater;

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;

	public RemarkImgListviewAdapter(Context context, List<String> imgList) {
		this.mContext = context;
		this.mList = imgList;
		mInflater = LayoutInflater.from(context);

		imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.empty_photo)
				.showImageOnFail(R.drawable.empty_photo).resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();

	}

	@Override
	public int getCount() {
		return mList == null ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.remark_img_item, null);
			holder.remarkAddRecordView = (RelativeLayout) convertView.findViewById(R.id.remark_add_record_item_view);
			holder.remarkAddView = (RelativeLayout) convertView.findViewById(R.id.remark_add_item_view);
			holder.remarkImgView = (ImageView) convertView.findViewById(R.id.remark_img_item_view);
			holder.spinner = (ProgressBar) convertView.findViewById(R.id.loading);
			holder.recordDisplayRl = (RelativeLayout) convertView.findViewById(R.id.record_display);
			holder.remarkImgFmView = (FrameLayout) convertView.findViewById(R.id.remark_fm_item_view);
			holder.deleteRecord = (ImageView) convertView.findViewById(R.id.delete_homework_img);
			holder.recordDisplayRl.setVisibility(View.GONE);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (mList.get(position).equals("null")) {
			holder.remarkImgFmView.setVisibility(View.GONE);
			holder.remarkAddView.setVisibility(View.VISIBLE);
			holder.remarkAddRecordView.setVisibility(View.GONE);
		} else if (mList.get(position).equals("record_null")) {
			holder.remarkImgFmView.setVisibility(View.GONE);
			holder.remarkAddView.setVisibility(View.GONE);
			holder.remarkAddRecordView.setVisibility(View.GONE);// 注释掉录音按钮
		} else {
			holder.remarkAddView.setVisibility(View.GONE);
			holder.remarkImgFmView.setVisibility(View.VISIBLE);
			holder.remarkAddRecordView.setVisibility(View.GONE);

			String path = mList.get(position);
			if (path != null && path.startsWith("/")) {
				path = "file://" + path;
			}

			imageLoader.displayImage(path, holder.remarkImgView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					holder.spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					
					holder.spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					holder.spinner.setVisibility(View.GONE);
				}
			});

		}
		
		holder.deleteRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mDeleteClickListener != null) {
					mDeleteClickListener.deleteClick(v, position);
				}
			}
		});
		
		

		return convertView;
	}

	private MyDeleteClickListener mDeleteClickListener = null;

	public interface MyDeleteClickListener {
		public void deleteClick(View v, int position);
	}

	public void setDeleteClickListener(MyDeleteClickListener mDeleteClickListener) {
		this.mDeleteClickListener = mDeleteClickListener;
	}

	private class ViewHolder {
		public ImageView deleteRecord;
		public ImageView remarkImgView;
		public RelativeLayout remarkAddView;
		public RelativeLayout remarkAddRecordView;
		public ProgressBar spinner;
		public RelativeLayout recordDisplayRl;
		public FrameLayout remarkImgFmView;
	}
}