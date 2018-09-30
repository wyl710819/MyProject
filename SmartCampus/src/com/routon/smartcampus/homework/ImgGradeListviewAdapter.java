package com.routon.smartcampus.homework;

import java.io.File;
import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.routon.inforelease.util.ImageUtils;
import com.routon.inforelease.widget.ImageViewHelper;
import com.routon.edurelease.R;
import com.routon.smartcampus.flower.BadgeRemarkBean;
import com.routon.smartcampus.flower.RemarkImagePreviewActivity;
import com.routon.smartcampus.homework.ImgGradeListviewAdapter.MyOnPlayClickListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class ImgGradeListviewAdapter extends BaseAdapter {

	private Context mContext;
	private List<QueryGradeHomeworkBean.HomeworkResourse> mList;
	private LayoutInflater mInflater;

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private MyOnPlayClickListener  mPlayClickListener=null;
	public interface MyOnPlayClickListener{
		 public  void playClick(View v,int position,ImageView imgView,TextView tvView,List<QueryGradeHomeworkBean.HomeworkResourse> mList ); 
	}
	public void setPlayClickListener(MyOnPlayClickListener myOnPlayClickListener){
		this.mPlayClickListener=myOnPlayClickListener;
	}
	public ImgGradeListviewAdapter(
			List<QueryGradeHomeworkBean.HomeworkResourse> imgList,
			Context context) {
		this.mContext = context;
		this.mList = imgList;
		mInflater = LayoutInflater.from(context);

		imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
		options = new DisplayImageOptions.Builder()
				// .showImageForEmptyUri(R.drawable.empty_photo)
				// .showImageOnFail(R.drawable.empty_photo)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
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
			convertView = mInflater.inflate(R.layout.class_img_listview_item,
					parent, false);

			holder.remarkImgView = (ImageView) convertView
					.findViewById(R.id.class_img_item_view);
			holder.playRecordRl = (RelativeLayout) convertView
					.findViewById(R.id.class_record_display);
			holder.spinner = (ProgressBar) convertView
					.findViewById(R.id.class_loading);
			holder.classPlayRrecordImg=(ImageView) convertView.findViewById(R.id.class_play_record_img);
			holder.tvRecordTime=(TextView) convertView.findViewById(R.id.tv_record_time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//166图片 172音频 
		if (mList.get(position).type == 166) {
			holder.remarkImgView.setVisibility(View.VISIBLE);
			holder.playRecordRl.setVisibility(View.GONE);
			String path = mList.get(position).fileUrl;
			if (path != null && path.startsWith("/")) {

				path = "file://" + path;
			}

			imageLoader.displayImage(path, holder.remarkImgView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							holder.spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
							}
							// Toast.makeText(mContext, message,
							// Toast.LENGTH_SHORT).show();

							holder.spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							holder.spinner.setVisibility(View.GONE);
						}
					});
		} else if (mList.get(position).type == 172) {
			holder.playRecordRl.setVisibility(View.VISIBLE);
			String time=mList.get(position).fileIdparams.substring(mList.get(position).fileIdparams.lastIndexOf("=")+1, mList.get(position).fileIdparams.length());
			if(!time.equals("null")){
				if(Integer.valueOf(time)<10){
					holder.tvRecordTime.setText("00:0"+time);
				}else{
					holder.tvRecordTime.setText("00:"+time);
				}
			}else{
				holder.tvRecordTime.setText("00:00");
			}
			holder.remarkImgView.setVisibility(View.GONE);
		}
		holder.playRecordRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mPlayClickListener!=null){
					mPlayClickListener.playClick(v, position, holder.classPlayRrecordImg,holder.tvRecordTime,mList);
				}
			}
		});

		return convertView;
	}

	private class ViewHolder {
		public ImageView remarkImgView;
		public ProgressBar spinner;
		public RelativeLayout playRecordRl;
		public ImageView classPlayRrecordImg;
		public TextView tvRecordTime;

	}
}