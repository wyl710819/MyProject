package com.routon.smartcampus.homework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.routon.edurelease.R;
import com.routon.smartcampus.flower.RemarkImagePreviewActivity;
import com.routon.smartcampus.homework.FamilyHomeworkGridImgAdapter.ImgViewHolder;
import com.routon.smartcampus.homework.FamilyHomeworkGridImgAdapter.OnAudioClickListener;
import com.routon.smartcampus.homework.FamilyHomeworkGridImgAdapter.OnImgClickListener;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.HorizontalListView;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FamilyHomeworkAdapter extends BaseAdapter {

	private Context mContext;
	private List<FamilyHomeworkBean> mHomeworkBeans;
	private LayoutInflater mInflater;

	public FamilyHomeworkAdapter(Context context, List<FamilyHomeworkBean> homeworkBeans) {
		this.mContext = context;
		this.mHomeworkBeans = homeworkBeans;
		mInflater = LayoutInflater.from(context);
		endPlay();
	}

	@Override
	public int getCount() {
		return mHomeworkBeans == null ? 0 : mHomeworkBeans.size();
	}

	@Override
	public Object getItem(int position) {
		return mHomeworkBeans.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.family_homework_item, null);
			holder.teacherImg = (ImageView) convertView.findViewById(R.id.teacher_img);
			holder.checkBut = (TextView) convertView.findViewById(R.id.check_but);
			holder.rateView = (ImageView) convertView.findViewById(R.id.rate_view);
			holder.courseTeacherView = (TextView) convertView.findViewById(R.id.course_teacher_view);
			holder.descriptionTv = (TextView) convertView.findViewById(R.id.description_tv);
			holder.remarkTv = (TextView) convertView.findViewById(R.id.remark_tv);
			holder.gridView = (HorizontalListView) convertView.findViewById(R.id.gridView);
			holder.rateText = (TextView) convertView.findViewById(R.id.item_rate_text);
			

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final FamilyHomeworkBean bean = mHomeworkBeans.get(position);

		if( bean.teacherImageUrl != null && bean.teacherImageUrl.isEmpty() == false ){
			Picasso.with(mContext).load(bean.teacherImageUrl).placeholder(R.drawable.default_student)
				.error(R.drawable.default_student).into(holder.teacherImg);
		}

		holder.checkBut.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				endPlay();
					if( onCheckListener != null ){
						onCheckListener.onCheck(position);
					}
			}
		});
		holder.rateView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				endPlay();
					if( onRateClickListener != null ){
						onRateClickListener.onRateClick(position);
					}
			}
		});
		
		
		
		
		
		holder.courseTeacherView.setText(bean.courseName + "　" + bean.teacherName + "老师布置");
		holder.descriptionTv.setText(bean.description);
		if (bean.remark != null && !bean.remark.equals("") && !bean.remark.equals("null")) {
			holder.remarkTv.setVisibility(View.VISIBLE);
			holder.remarkTv.setText("附注：" + bean.remark);
		} else {
			holder.remarkTv.setVisibility(View.GONE);
		}
		if (bean.rateStr==null || bean.rateStr.equals("") ||bean.rateStr.equals("null")) {
		if (bean.rate == 1) {
			holder.rateView.setBackgroundResource(R.drawable.ic_homework_excellent);
			holder.rateView.setVisibility(View.VISIBLE);
			holder.checkBut.setVisibility(View.GONE);
		} else if (bean.rate == 2) {
			holder.rateView.setBackgroundResource(R.drawable.ic_homework_good);
			holder.rateView.setVisibility(View.VISIBLE);
			holder.checkBut.setVisibility(View.GONE);
		} else if (bean.rate == 3) {
			holder.rateView.setBackgroundResource(R.drawable.ic_homework_general);
			holder.rateView.setVisibility(View.VISIBLE);
			holder.checkBut.setVisibility(View.GONE);
		} else if (bean.rate == 4) {
			holder.rateView.setBackgroundResource(R.drawable.ic_homework_weak);
			holder.rateView.setVisibility(View.VISIBLE);
			holder.checkBut.setVisibility(View.GONE);
		} else if (bean.rate == 5) {
			holder.rateView.setBackgroundResource(R.drawable.ic_homework_unfinished);
			holder.rateView.setVisibility(View.VISIBLE);
			holder.checkBut.setVisibility(View.GONE);
		} else if (bean.rate == 0 ||bean.rate==-1) {
			holder.rateView.setVisibility(View.GONE);
			holder.rateText.setVisibility(View.GONE);
//			if (bean.isCheck) {
//				holder.checkBut.setText("已反馈");
//				holder.checkBut.setTextColor(mContext.getResources().getColor(R.color.black));
//				holder.checkBut.setBackgroundResource(0);
//				holder.checkBut.setVisibility(View.VISIBLE);
//			}else {
				holder.checkBut.setText("反馈");
				holder.checkBut.setTextColor(mContext.getResources().getColor(R.color.white));
				holder.checkBut.setBackgroundResource(R.drawable.homework_but_bag);
				holder.checkBut.setVisibility(View.VISIBLE);
//			}
		} }else {
			holder.checkBut.setVisibility(View.GONE);
			holder.rateView.setBackgroundResource(R.drawable.ic_homework_rate);
			holder.rateView.setVisibility(View.VISIBLE);
			if (bean.rateStr.length()==1) {
				holder.rateText.setTextSize(sp2px(7));
			}else if (bean.rateStr.length()==2) {
				holder.rateText.setTextSize(sp2px(5));
			}else if (bean.rateStr.length()==3) {
				holder.rateText.setTextSize(sp2px(4));
			}
			holder.rateText.setText(bean.rateStr);
			holder.rateText.setVisibility(View.VISIBLE);
		}

		if (bean.homeworkFileList != null && bean.homeworkFileList.size() > 0) {
			final List<FeedbackHomeworkFileBean> HomeworkBeans=new ArrayList<FeedbackHomeworkFileBean>();
			for (int i = 0; i < bean.homeworkFileList.size(); i++) {
				if (bean.homeworkFileList.get(i).fileType==1) {
					HomeworkBeans.add(bean.homeworkFileList.get(i));
				}
			}
			
			FamilyHomeworkGridImgAdapter imgAdapter=new FamilyHomeworkGridImgAdapter(mContext,bean.homeworkFileList);
			imgAdapter.setImgClickListener(new OnImgClickListener() {
				
				@Override
				public void imgClick(int position) {
					endPlay();
						imageBrower(HomeworkBeans.lastIndexOf(bean.homeworkFileList.get(position)), bean.homeworkImgUrls);
				}

			});
			imgAdapter.setAudioClickListener(new OnAudioClickListener() {
				
				@Override
				public void audioClick(View v, int position, ImgViewHolder imgHolder) {
					endPlay();
					mImgHolder = imgHolder;
					if (imgHolder != null) {
						animDrawable = (AnimationDrawable) imgHolder.playRecordImg.getBackground();
						if (animDrawable != null) {
							animDrawable.start();
						}
						
						startPlayRecord(bean.homeworkFileList.get(position).fileUrl, bean.homeworkFileList.get(position).audioLength,
								imgHolder.playRecordImg);
					}
				}
			});

//			holder.gridView.setOnItemClickListener(OnItemClickListener() {
//
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					imageBrower(position, bean.homeworkImgUrls);
//				}
//			});

			holder.gridView.setAdapter(imgAdapter);
		}
		

		return convertView;
	}
	
	private MediaPlayer mediaPlayer;
	private Timer mPlayTimer;
	private int playerTime;
	private int audioItemTime;
	private boolean isPlaying;
	private ImageView audioPlayImgView;
	private ImgViewHolder mImgHolder;
	private AnimationDrawable animDrawable;
	
	// 播放录音
		private void startPlayRecord(String path, int audioTime, final ImageView imgView) {
			this.audioPlayImgView=imgView;
			isPlaying=true;
			Log.d("path:", path);
			File file = null;
			if (path != null) {
				file = new File(path);
				mediaPlayer = new MediaPlayer();
				// 设置播放音频数据文件
				try {
					mediaPlayer.setDataSource(path);
					mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							

							playEndOrFail();
						}
					});
					mediaPlayer.setOnErrorListener(new OnErrorListener() {

						@Override
						public boolean onError(MediaPlayer mp, int what, int extra) {
							
							playEndOrFail();

							return true;
						}
					});
					mediaPlayer.setVolume(1, 1);
					// 是否循环播放
					mediaPlayer.setLooping(false);
					// 准备及播放
					mediaPlayer.prepare();
					mediaPlayer.start();
					updatePlayerTime(audioTime);
				} catch (IllegalArgumentException e) {
					playEndOrFail();
					e.printStackTrace();
				} catch (SecurityException e) {
					playEndOrFail();
					e.printStackTrace();
				} catch (IllegalStateException e) {
					playEndOrFail();
					e.printStackTrace();
				} catch (IOException e) {
					playEndOrFail();
					e.printStackTrace();
				}
			}
		}

		private void updatePlayerTime(int audioTime) {
			audioItemTime = audioTime;
			playerTime = audioTime;
			mPlayTimer = new Timer();
			mPlayTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					
					if (playerTime==0) {
						return;
					}
					playerTime--;

					Message msg = new Message();
					msg.what = UPLOAD_AUDIO_TEXT;
					handler.sendMessage(msg);
//					mImgHolder.recordTimeView.setText(playerTime + "s");
				}
			}, 0, 1000);

		}
		public static final int UPLOAD_AUDIO_TEXT = 3;
		private Handler handler = new Handler() {
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case UPLOAD_AUDIO_TEXT:
					mImgHolder.recordTimeView.setText(playerTime + "s");
					break;
				default:
					break;
				}

			}
		};
	private void endPlay(){
		if (isPlaying) {
			playEndOrFail();
		}
	}
	
	// 停止播放或者播放异常处理
		private void playEndOrFail() {
			isPlaying=false;
			if (null != mediaPlayer) {
				mediaPlayer.setOnCompletionListener(null);
				mediaPlayer.setOnErrorListener(null);
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
				mediaPlayer = null;

			}
			if (mPlayTimer != null) {
				mPlayTimer.cancel();
				mPlayTimer.purge();
				mPlayTimer = null;
				mImgHolder.recordTimeView.setText(audioItemTime + "s");
			}
			
			if (animDrawable!=null) {
				animDrawable.selectDrawable(0);
				animDrawable.stop();

				audioPlayImgView.setBackgroundResource(R.drawable.play_record_anim);
			}
			
		}

	private void imageBrower(int position, ArrayList<String> urls) {
		Intent intent = new Intent(mContext, RemarkImagePreviewActivity.class);

		Bundle bundle = new Bundle();

		bundle.putStringArrayList(MyBundleName.BADGE_REMARK_PIC_LIST, urls);
		bundle.putInt(MyBundleName.BADGE_REMARK_PIC_POSITION, position);

		intent.putExtras(bundle);
		mContext.startActivity(intent);

	}

	private static class ViewHolder {
		ImageView teacherImg;
		TextView checkBut;
		ImageView rateView;
		TextView courseTeacherView;
		TextView descriptionTv;
		TextView remarkTv;
		HorizontalListView gridView;
		TextView rateText;
	}
	
	public interface onCheckListener{
		public void onCheck(int position);
	}
	private onCheckListener onCheckListener = null;
	public void setOnCheckListener(onCheckListener listener){
		onCheckListener = listener;
	}
	
	public interface OnRateClickListener{
		public void onRateClick(int position);
	}
	private OnRateClickListener onRateClickListener = null;
	public void setOnRateClickListener(OnRateClickListener listener){
		onRateClickListener = listener;
	}

	private int sp2px(int value) {
		float v = mContext.getResources().getDisplayMetrics().scaledDensity;
		return (int) (v * value + 0.5f);
	}
}
