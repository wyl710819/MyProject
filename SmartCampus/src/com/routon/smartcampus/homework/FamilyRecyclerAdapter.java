package com.routon.smartcampus.homework;

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
import com.routon.edurelease.R;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FamilyRecyclerAdapter extends Adapter<ViewHolder> {

	private Context mContext;
	private List<FeedbackHomeworkFileBean> mDatas;
	private final int PIC_ITEM = 1;
	private final int AUDIO_ITEM = 2;
	String fileType = "";

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private Boolean delImgShow;
	private String viewType;
	

	public FamilyRecyclerAdapter(Context context, List<FeedbackHomeworkFileBean> familyDataList,Boolean delImgShow, String viewType) {
		super();
		this.mContext = context;
		this.mDatas = familyDataList;
		this.delImgShow = delImgShow;
		this.viewType = viewType;

		imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.empty_photo)
				.showImageOnFail(R.drawable.empty_photo).resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();

	}

	@Override
	public int getItemCount() {

		return mDatas == null ? 0 : mDatas.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (mDatas.size() == 0) {
			return 0;
		} else if (mDatas.get(position).fileUrl.equals("pic")) {
			return PIC_ITEM;
		} else if (mDatas.get(position).fileUrl.equals("audio")) {
			return AUDIO_ITEM;
		} else {
			return super.getItemViewType(position);
		}
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		if (holder instanceof FamilyRecyclerAddHolder) {
			final FamilyRecyclerAddHolder addHolder = (FamilyRecyclerAddHolder) holder;
			if (mDatas.get(position).fileUrl.equals("pic")) {
				addHolder.picItemRl.setVisibility(View.VISIBLE);
				addHolder.audioItemRl.setVisibility(View.GONE);

				if (mItemClickListener != null) {
					addHolder.picItemRl.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mItemClickListener.itemClick(v, position, "add_pic", null);
						}
					});

				}
			} else if (mDatas.get(position).fileUrl.equals("audio")) {
				addHolder.picItemRl.setVisibility(View.GONE);
				addHolder.audioItemRl.setVisibility(View.VISIBLE);

				if (mItemLongClickListener != null) {

					addHolder.audioItemRl.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							mItemLongClickListener.itemLongClick(v, event, addHolder);
							return false;
						}
					});

					// addHolder.audioItemRl.setOnClickListener(new
					// OnClickListener() {
					//
					// @Override
					// public void onClick(final View v) {
					//
					// mItemLongClickListener.itemLongClick(v,addHolder);
					// Handler handler = new Handler();
					// handler.postDelayed(new Runnable() {
					// @Override
					// public void run() {
					//
					// }
					// }, 1000);
					// }
					// });
				}

			}

		} else {
			final FamilyRecyclerImgHolder imgHolder = (FamilyRecyclerImgHolder) holder;
			String fileUrl = mDatas.get(position).fileUrl;

			if (mDatas.get(position).fileType == 1) {
				imgHolder.audioItemRl.setVisibility(View.GONE);
				imgHolder.picItemRl.setVisibility(View.VISIBLE);

				if (fileUrl != null
//						&& fileUrl.startsWith("http")) {
//					Picasso.with(mContext).load(fileUrl).error(R.drawable.default_pic)
//							.placeholder(R.drawable.default_pic).fit().into(imgHolder.homeworkImgView);
//				} else if (fileUrl != null && fileUrl.startsWith("/")
						) {
					if (fileUrl.startsWith("/")) {
						fileUrl = "file://" + fileUrl;
					}
					

					imageLoader.displayImage(fileUrl, imgHolder.homeworkImgView, options,
							new SimpleImageLoadingListener() {
								@Override
								public void onLoadingStarted(String imageUri, View view) {
									imgHolder.spinner.setVisibility(View.VISIBLE);
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

									imgHolder.spinner.setVisibility(View.GONE);
								}

								@Override
								public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
									imgHolder.spinner.setVisibility(View.GONE);
								}
							});

				} else {
					imgHolder.homeworkImgView.setImageResource(R.drawable.default_pic);
				}

				if (mItemClickListener != null) {
					imgHolder.homeworkImgView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mItemClickListener.itemClick(v, position, "pic_preview", imgHolder);
						}
					});

				}

			} else if (mDatas.get(position).fileType == 2) {

				imgHolder.audioItemRl.setVisibility(View.VISIBLE);
				imgHolder.picItemRl.setVisibility(View.GONE);

				if (fileUrl != null && fileUrl.startsWith("http")) {
					imgHolder.recordTimeView.setText(mDatas.get(position).audioLength+"s");
				} else if (fileUrl != null && fileUrl.startsWith("/")) {
					imgHolder.recordTimeView.setText(mDatas.get(position).audioLength+"s");
				} else {

				}

				if (mItemClickListener != null) {
					imgHolder.audioItemRl.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							mItemClickListener.itemClick(v, position, "audio_play", imgHolder);
						}
					});

				}

			}
			
			imgHolder.delImg.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mDelClickListener!=null) {
						mDelClickListener.delClick(v,position);
					}
				}
			});

		}

	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup arg0, int viewType) {
		View view;

		if (viewType == PIC_ITEM || viewType == AUDIO_ITEM) {
			view = LayoutInflater.from(mContext).inflate(R.layout.family_homework_add_item, null);
			return new FamilyRecyclerAddHolder(view);
		} else {
			view = LayoutInflater.from(mContext).inflate(R.layout.family_homework_img_item, null);
			return new FamilyRecyclerImgHolder(view);
		}

	}

	class FamilyRecyclerImgHolder extends ViewHolder {

		private ImageView homeworkImgView;
		private ProgressBar spinner;
		public ImageView delImg;
		public ImageView playRecordImg;
		public TextView recordTimeView;
		private RelativeLayout picItemRl;
		private RelativeLayout audioItemRl;
		private RelativeLayout audioItemChildRl;

		public FamilyRecyclerImgHolder(View itemView) {
			super(itemView);
			picItemRl = (RelativeLayout) itemView.findViewById(R.id.pic_item_rl);
			homeworkImgView = (ImageView) itemView.findViewById(R.id.family_homework_img_view);
			spinner = (ProgressBar) itemView.findViewById(R.id.loading);

			audioItemRl = (RelativeLayout) itemView.findViewById(R.id.audio_item_rl);
			delImg = (ImageView) itemView.findViewById(R.id.delete_homework_record);
			playRecordImg = (ImageView) itemView.findViewById(R.id.play_record_img);
			recordTimeView = (TextView) itemView.findViewById(R.id.tv_record_time);
			audioItemChildRl = (RelativeLayout) itemView.findViewById(R.id.audio_item_child_rl);
			
			if (delImgShow) {
				delImg.setVisibility(View.VISIBLE);
			}else {
				delImg.setVisibility(View.GONE);
			}
			
			if (viewType.equals("tacher")) {
				audioItemChildRl.setBackgroundResource(R.drawable.record_blue_rectangle_shape);
			}else if (viewType.equals("family")) {
				audioItemChildRl.setBackgroundResource(R.drawable.record_yellow_rectangle_shape);
			}
		}

	}

	public class FamilyRecyclerAddHolder extends ViewHolder {

		private RelativeLayout picItemRl;
		private RelativeLayout audioItemRl;
		public TextView tvRecordTime;
		public ImageView addRecordImg;

		public FamilyRecyclerAddHolder(View itemView) {
			super(itemView);
			picItemRl = (RelativeLayout) itemView.findViewById(R.id.add_pic_item_rl);
			audioItemRl = (RelativeLayout) itemView.findViewById(R.id.add_audio_item_rl);
			tvRecordTime = (TextView) itemView.findViewById(R.id.record_time);
			addRecordImg = (ImageView) itemView.findViewById(R.id.add_record_img);
			
		}

	}

	private int time;
	private Timer mTimer;
	private static final int RECORDTIME = 0;
	private int recordTime;
	private int totalRecordTime;

	// 开始计时
	public void startTimer(final FamilyRecyclerAddHolder holder) {
		time = 0;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				time++;

				Message msg = new Message();
				msg.what = RECORDTIME;
				msg.arg1 = time;
				msg.obj = holder;
				handler.sendMessage(msg);
			}
		}, 0, 1000);
	}

	// 关闭计时器
	public void closeTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
			recordTime = 0;
		}
	}

	private Handler handler = new Handler() {
		FamilyRecyclerAddHolder holder = null;

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RECORDTIME:
				recordTime = msg.arg1;
				holder = (FamilyRecyclerAddHolder) msg.obj;
				holder.tvRecordTime.setText(recordTime + "s");
			}
		}
	};

	private OnItemClickListener mItemClickListener;
	private DisplayImageOptions options;

	public interface OnItemClickListener {
		public void itemClick(View v, int position, String type, FamilyRecyclerImgHolder imgHolder);
	}

	public void setItemClickListener(OnItemClickListener mItemClickListener) {
		this.mItemClickListener = mItemClickListener;
	}

	
	
	private OnItemLongClickListener mItemLongClickListener;

	public interface OnItemLongClickListener {
		public void itemLongClick(View v, MotionEvent event, FamilyRecyclerAddHolder addHolder);
	}

	public void setItemLongClickListener(OnItemLongClickListener mItemLongClickListener) {
		this.mItemLongClickListener = mItemLongClickListener;
	}
	
	
	
	private OnDelClickListener mDelClickListener;

	public interface OnDelClickListener {
		public void delClick(View v, int position);
	}

	public void setDelClickListener(OnDelClickListener mDelClickListener) {
		this.mDelClickListener = mDelClickListener;
	}

}
