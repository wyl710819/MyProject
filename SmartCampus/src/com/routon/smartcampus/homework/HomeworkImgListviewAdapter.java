package com.routon.smartcampus.homework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import com.routon.smartcampus.utils.MyConstant;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class HomeworkImgListviewAdapter extends BaseAdapter{

	private Context mContext;
	private List<FeedbackWorkBean> mList;
	private LayoutInflater mInflater;
	
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private MyOnTouchListener  mTouchListener=null;
	private MyOnItemClickListener  mItemClickListener=null;
	private MyDeleteClickListener mDeleteClickListener=null;
	private Timer mTimer;
	private int time;
	private int recordTime;
	private int totalRecordTime;
	private static final int RECORDTIME=0;
	//ontouch录音
	public interface MyOnTouchListener{
		 public  void click(View v,MotionEvent event); 
	}
	public void setTouchListener(MyOnTouchListener mTouchListener){
		this.mTouchListener=mTouchListener;
	}
	//item点击事件图片预览或者播放语音
	public interface MyOnItemClickListener{
		 public  void itemClick(View v,int position,ImageView imgView,TextView tvRecordTime); 
	}
	public void setItemClickListener(MyOnItemClickListener mItemClickListener){
		this.mItemClickListener=mItemClickListener;
	}
	//删除图片或者语音
	public interface MyDeleteClickListener{
		public void deleteClick(View v,int position);
	}
	public void setDeleteClickListener(MyDeleteClickListener mDeleteClickListener){
		this.mDeleteClickListener=mDeleteClickListener;
	}
	public HomeworkImgListviewAdapter(Context context, List<FeedbackWorkBean> imgList) {
		this.mContext=context;
		this.mList=imgList;
		mInflater = LayoutInflater.from(context);
		
		
		 imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
	    	options = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.empty_photo)
					.showImageOnFail(R.drawable.empty_photo)
					.resetViewBeforeLoading(true)
					.cacheOnDisc(true)
					.imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.displayer(new FadeInBitmapDisplayer(300))
					.build();
		
	}

	@Override
	public int getCount() {
		return mList==null ? 0 : mList.size();
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
	public View getView( final int position, View convertView, ViewGroup parent) {
		
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.remark_img_item, null);
			holder.remarkAddRecordView=(RelativeLayout) convertView.findViewById(R.id.remark_add_record_item_view);
			holder.remarkAddView=(RelativeLayout) convertView.findViewById(R.id.remark_add_item_view);
			holder.remarkImgView= (ImageView) convertView.findViewById(R.id.remark_img_item_view);
			holder.spinner = (ProgressBar) convertView.findViewById(R.id.loading);
			holder.addRecordImg=(ImageView) convertView.findViewById(R.id.add_record_img);
			holder.tvRecordTime=(TextView) convertView.findViewById(R.id.record_time);
			holder.recordDisplayRl=(RelativeLayout) convertView.findViewById(R.id.record_display);
			holder.tvTotalRecordTime=(TextView) convertView.findViewById(R.id.tv_record_time);
			holder.playRecordImg=(ImageView) convertView.findViewById(R.id.play_record_img);
			holder.remarkImgFmView=(FrameLayout) convertView.findViewById(R.id.remark_fm_item_view);
			holder.deleteImg=(ImageView) convertView.findViewById(R.id.delete_homework_img);
			holder.deleteRecord=(ImageView) convertView.findViewById(R.id.delete_homework_record);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (mList.get(position).fileUrl.equals("null")) {
			holder.remarkImgFmView.setVisibility(View.GONE);
			holder.remarkAddView.setVisibility(View.VISIBLE);
			holder.recordDisplayRl.setVisibility(View.GONE);
			holder.remarkAddRecordView.setVisibility(View.GONE);
		}else if(mList.get(position).fileUrl.equals("record_null")){
			holder.remarkImgFmView.setVisibility(View.GONE);
			holder.remarkAddView.setVisibility(View.GONE);
			holder.recordDisplayRl.setVisibility(View.GONE);
			holder.remarkAddRecordView.setVisibility(View.VISIBLE);//注释掉录音按钮
		}else {
			holder.remarkAddView.setVisibility(View.GONE);
			
			holder.remarkAddRecordView.setVisibility(View.GONE);
			 String path = mList.get(position).fileUrl;
				if( path != null && path.startsWith("/")){
					path = "file://"+path;
				}
			if(mList.get(position).fileType!=2){
				holder.remarkImgFmView.setVisibility(View.VISIBLE);
				holder.recordDisplayRl.setVisibility(View.GONE);
				 imageLoader.displayImage(path, holder.remarkImgView, options, new SimpleImageLoadingListener() {
	     				@Override
	     				public void onLoadingStarted(String imageUri, View view) {
	     					holder.spinner.setVisibility(View.VISIBLE);
	     				}

	     				@Override
	     				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
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
//	     					Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

	     					holder.spinner.setVisibility(View.GONE);
	     				}

	     				@Override
	     				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
	     					holder.spinner.setVisibility(View.GONE);
	     				}
	     			});
			
			}
			
			else if(mList.get(position).fileType==2){
//				holder.remarkImgView.setImageResource(R.drawable.add_btn);
				holder.remarkImgFmView.setVisibility(View.GONE);
				holder.recordDisplayRl.setVisibility(View.VISIBLE);
				if(mList.get(position).audioLength<10){
					holder.tvTotalRecordTime.setText("00:0"+mList.get(position).audioLength);
				}else{
					holder.tvTotalRecordTime.setText("00:"+mList.get(position).audioLength);
				}
			}
		}
		holder.remarkAddRecordView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(mTouchListener!=null){
					if(position==mList.size()-1){
						mTouchListener.click(v, event);
					}
					
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					
					holder.tvRecordTime.setVisibility(View.VISIBLE);
					holder.addRecordImg.setImageResource(R.drawable.record_pressed);
					startTimer(holder);
					break;
				case MotionEvent.ACTION_UP:
					holder.tvRecordTime.setVisibility(View.GONE);
					holder.addRecordImg.setImageResource(R.drawable.record_normal);
					totalRecordTime=recordTime;
					
					closeTimer();
					break;
				default:
					break;
				}
				return true;
			}
		});
		holder.recordDisplayRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mItemClickListener!=null){
					mItemClickListener.itemClick(v, position, holder.playRecordImg,holder.tvTotalRecordTime);
				}
			}
		});
		
		holder.deleteImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mDeleteClickListener!=null){
					mDeleteClickListener.deleteClick(v, position);
				}
			}
		});
		holder.deleteRecord.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mDeleteClickListener!=null){
					mDeleteClickListener.deleteClick(v, position);
				}
			}
		});
		return convertView;
	}
		
	
	private class ViewHolder {
		public ImageView remarkImgView;
		public RelativeLayout remarkAddView;
		public RelativeLayout remarkAddRecordView;
		public ProgressBar spinner;
		public ImageView addRecordImg;
		public TextView tvRecordTime;
		public RelativeLayout recordDisplayRl;
		public TextView tvTotalRecordTime;
		public ImageView playRecordImg;
		public FrameLayout remarkImgFmView;
		public ImageView deleteRecord;
		public ImageView deleteImg;

	}
	
			// 开始计时
			public void startTimer(final ViewHolder holder) {
				time = 0;
				mTimer = new Timer();
				mTimer.schedule(new TimerTask() {

					@Override
					public void run() {
						time++;

						Message msg = new Message();
						msg.what = RECORDTIME;
						msg.arg1 = time;
						msg.obj=holder;
						handler.sendMessage(msg);
					}
				}, 0, 1000);
			}

			// 关闭计时器
			public void closeTimer() {
				if (mTimer != null) {
					mTimer.cancel();
					mTimer.purge();
					
					recordTime=0;
				}
			}
			private Handler handler=new Handler(){
				ViewHolder holder=null;
				public void handleMessage(Message msg){
					switch(msg.what){
					case RECORDTIME:
						recordTime=msg.arg1;
						
						holder=(ViewHolder) msg.obj;
						holder.tvRecordTime.setText(recordTime+"s");
						
						if (recordTime==MyConstant.HOMEWORK_RECORD_MAX_LENGTH) {
							mTimer.cancel();
							mTimer.purge();
							mTimer = null;
							return ;
						}
					}
				}
			};
}