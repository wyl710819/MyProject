package com.routon.smartcampus.homework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.routon.edurelease.R;
import com.routon.smartcampus.homework.ImgClassListviewAdapter.MyOnPlayClickListener;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.HorizontalListView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ClassAdapter extends BaseAdapter {

	private Context mContext;
	private List<QueryClassHomeworkBean> classHomeworkList;
	ImgClassListviewAdapter mImgClassListviewAdapter;
	private MediaPlayer mediaPlayer;
	private AnimationDrawable animDrawable;
	// 当前是否正在播放
	private volatile boolean isPlaying=false;
	private static final int RECORDTIME=0;
	private Timer mTimer;
	private int currentTime=0;//实时播放时间
	private TextView recordTimeView;
	private String autioTime=null;
	public ClassAdapter(Context context,
			List<QueryClassHomeworkBean> classHomeworkList) {
		this.mContext = context;
		this.classHomeworkList = classHomeworkList;
	}

	private ClassOnClickListener mClassClickListener = null;

	public interface ClassOnClickListener {
		public void clickModify(View v, int position);

		public void clickCorrect(View v, int position);
	}

	public void setClassListener(ClassOnClickListener mClassClickListener) {
		this.mClassClickListener = mClassClickListener;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub

		return classHomeworkList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return classHomeworkList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Holder holder;
		if (convertView == null) {
			holder = new Holder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.class_item, null);
			holder.tvClass = (TextView) convertView
					.findViewById(R.id.tv_class_homework);
			holder.btnModify = (TextView) convertView
					.findViewById(R.id.btn_modify);
			holder.btnCorrect = (TextView) convertView
					.findViewById(R.id.btn_correct);
			holder.ivAdd = (ImageView) convertView
					.findViewById(R.id.select_add);
			holder.ivRight = (ImageView) convertView
					.findViewById(R.id.right_arrow);
			holder.description = (TextView) convertView
					.findViewById(R.id.class_description);

			holder.imgHorizontalListView = (HorizontalListView) convertView
					.findViewById(R.id.class_img_listview);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		if (classHomeworkList.size() > 2) {
			holder.description.setSingleLine(true);
			holder.description.setEllipsize(TruncateAt.END);
		} else {
			holder.description.setSingleLine(false);
		}
		final QueryClassHomeworkBean bean = classHomeworkList.get(position);
		if (bean.resourseList != null && bean.resourseList.size() > 0) {
			holder.imgHorizontalListView.setVisibility(View.VISIBLE);
			mImgClassListviewAdapter = new ImgClassListviewAdapter(
					bean.resourseList, mContext);
			holder.imgHorizontalListView.setAdapter(mImgClassListviewAdapter);
			mImgClassListviewAdapter.notifyDataSetChanged();
			holder.imgHorizontalListView
					.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View view,
								int position, long id) {
							// TODO Auto-generated method stub
							if (bean.resourseList.get(position).type == 172) {
								
							} else if (bean.resourseList.get(position).type == 166) {
								imageBrower(position, bean.resourseList);
							}
						}
					});
		} else {
			holder.imgHorizontalListView.setVisibility(View.GONE);
			mImgClassListviewAdapter = new ImgClassListviewAdapter(
					bean.resourseList, mContext);
			holder.imgHorizontalListView.setAdapter(mImgClassListviewAdapter);
		}
		mImgClassListviewAdapter.setPlayClickListener(new MyOnPlayClickListener() {
			
			@Override
			public void playClick(View v, int position, ImageView imgView,TextView tvRecordTimeVIew,List<QueryClassHomeworkBean.HomeworkResourse> mList) {
				// TODO Auto-generated method stub
				if(!isPlaying){
					autioTime=mList.get(position).fileIdparams.substring(mList.get(position).fileIdparams.lastIndexOf("=")+1, 
							mList.get(position).fileIdparams.length());
					animDrawable = (AnimationDrawable)imgView.getBackground();
					if(animDrawable!=null){
						animDrawable.start();
					}
					recordTimeView=tvRecordTimeVIew;
					//播放语音
					startPlayRecord(mList.get(position).fileUrl,tvRecordTimeVIew,autioTime,position);
				}else{
					playEndOrFail(tvRecordTimeVIew,autioTime,position);
				}
				
			}
		});
		holder.ivAdd.setImageResource(R.drawable.select_add);
		holder.tvClass.setText(bean.className);
		holder.ivRight.setImageResource(R.drawable.right_gray);
		// bean.description!=null && !bean.description.equals("null") &&
		// !bean.description.equals("")
		if (bean.hId != 0) {
			holder.description.setText(bean.description);
			if (bean.description.equals("")) {
				holder.description.setVisibility(View.GONE);
			} else {
				holder.description.setVisibility(View.VISIBLE);
			}
			holder.btnModify.setVisibility(View.VISIBLE);
			holder.btnCorrect.setVisibility(View.VISIBLE);
			holder.ivAdd.setVisibility(View.GONE);
			holder.tvClass.setTextColor(Color.parseColor("#b94645"));
			// holder.ivRight.setImageResource(R.drawable.right_red);
			holder.ivRight.setVisibility(View.GONE);

			holder.btnModify.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (mClassClickListener != null) {
						mClassClickListener.clickModify(v, position);
					}
				}
			});
			holder.btnCorrect.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (mClassClickListener != null) {
						mClassClickListener.clickCorrect(v, position);
					}
				}
			});

		} else {
			holder.btnModify.setVisibility(View.GONE);
			holder.btnCorrect.setVisibility(View.GONE);
			holder.description.setVisibility(View.GONE);
			holder.ivRight.setVisibility(View.VISIBLE);
			holder.ivAdd.setVisibility(View.VISIBLE);
			holder.tvClass.setTextColor(Color.parseColor("#786b62"));
			holder.ivRight.setImageResource(R.drawable.right_gray);
		}
		return convertView;
	}

	private void imageBrower(int position,
			List<QueryClassHomeworkBean.HomeworkResourse> imgList) {
		Intent intent = new Intent(mContext, ImageBrowerActivity.class);
		FeedbackWorkBean feedBean = null;
		Bundle bundle = new Bundle();
		ArrayList<FeedbackWorkBean> list = new ArrayList<FeedbackWorkBean>();
		for (int i = 0; i < imgList.size(); i++) {
			if (imgList.get(i).type == 166) {
				feedBean = new FeedbackWorkBean();
				feedBean.fileUrl = imgList.get(i).fileUrl;
				feedBean.fileType = 1;
				list.add(feedBean);
			}

		}
		int positionTag=0;
		for (int i = 0; i < list.size(); i++) {
			if (imgList.get(position).fileUrl==list.get(i).fileUrl) {
				positionTag=i;
			}
		}
		
		
		bundle.putSerializable(MyBundleName.BADGE_REMARK_PIC_LIST, list);
		bundle.putInt(MyBundleName.BADGE_REMARK_PIC_POSITION, positionTag);
		bundle.putString("homework", "homework");
		intent.putExtras(bundle);
		mContext.startActivity(intent);

	}

	class Holder {
		TextView tvClass;
		TextView btnModify;
		TextView btnCorrect;
		ImageView ivAdd;
		ImageView ivRight;
		HorizontalListView imgHorizontalListView;
		TextView description;

	}
	int currTime;
	// 播放录音
	private void startPlayRecord(String path,final TextView tvRecordTimeView,final String autioTime,final int position) {
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
						// TODO Auto-generated method stub
						playEndOrFail(tvRecordTimeView,autioTime,position);
					}
				});
				mediaPlayer.setOnErrorListener(new OnErrorListener() {

					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						// TODO Auto-generated method stub
						playEndOrFail(tvRecordTimeView,autioTime,position);
						return true;
					}
				});
				mediaPlayer.setVolume(1, 1);
				// 是否循环播放
				mediaPlayer.setLooping(false);
				// 准备及播放
				mediaPlayer.prepare();
				mediaPlayer.start();
				isPlaying=true;
				mTimer =new Timer();
				mTimer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(mediaPlayer!=null){
							currTime=mediaPlayer.getCurrentPosition()/1000;
							Message msg = new Message();
							msg.what = RECORDTIME;
							msg.arg1 = currTime+1;
							handler.sendMessage(msg);
						}
						
					}
				},0,50);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 停止播放或者播放异常处理
	private void playEndOrFail(TextView tvRrecordTime,String autioTime,int position) {
		isPlaying = false;
		// if (isEnd) {
		// mHandler.sendEmptyMessage(Constant.PLAY_COMPLETION);
		// } else {
		// mHandler.sendEmptyMessage(Constant.PLAY_ERROR);
		// }
		if(!autioTime.equals("null")){
			if(Integer.valueOf(autioTime)<10){
				recordTimeView.setText("00:0"+autioTime);
			}else{
				recordTimeView.setText("00:"+autioTime);
			}
		}else{
			recordTimeView.setText("00:00");
		}
		
		if(animDrawable!=null){
			animDrawable.selectDrawable(0);
			animDrawable.stop();
		}
		if(mTimer!=null){
			mTimer.cancel();
			mTimer.purge();
		}
		if (null != mediaPlayer) {
			mediaPlayer.setOnCompletionListener(null);
			mediaPlayer.setOnErrorListener(null);
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	private Handler handler=new Handler(){
		public void handleMessage(Message message){
			switch(message.what){
			case RECORDTIME:
				currentTime=message.arg1;
				if(currentTime<10){
					recordTimeView.setText("00:0"+currentTime);
				}else{
					recordTimeView.setText("00:"+currentTime);
				}
				
			}
		}
	};

}
