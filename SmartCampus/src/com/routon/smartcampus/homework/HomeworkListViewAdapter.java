package com.routon.smartcampus.homework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.edurelease.R;
import com.routon.smartcampus.flower.RemarkImagePreviewActivity;
import com.routon.smartcampus.homework.ImgGradeListviewAdapter.MyOnPlayClickListener;

import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.MyBundleName;
import com.routon.smartcampus.view.HorizontalListView;
import com.routon.smartcampus.view.NoScrollGridView;
import com.routon.widgets.Toast;

import android.app.Activity;
import android.app.ProgressDialog;
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

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class HomeworkListViewAdapter extends BaseAdapter {

	private ArrayList<QueryGradeHomeworkBean> mList = new ArrayList<QueryGradeHomeworkBean>();
	private LayoutInflater mInflater;
	private Context mContext;
	private ProgressDialog progressDialog;
	private Long classId;
	ImgGradeListviewAdapter mImgGradeListviewAdapter;
	private MediaPlayer mediaPlayer;
	private AnimationDrawable animDrawable;
	// 当前是否正在播放
	private volatile boolean isPlaying;
	private static final int RECORDTIME=0;
	private Timer mTimer;
	private int currentTime=0;//实时播放时间
	private TextView recordTimeView;
	private int currTime;
	private String autioTime=null;
	// public interface onClickListener{
	// public static int CANCEL_TYPE = 0;
	// public static int ADD_TYPE = 1;
	// public void onClick(int position,int type);
	// }

	// private onClickListener mOnClickListener = null;
	// public void setOnClickListener(onClickListener listener){
	// mOnClickListener = listener;
	// }
	private MyOnClickListener mClickListener = null;

	public interface MyOnClickListener {
		public void click(View v, int position);
	}

	public HomeworkListViewAdapter(Context context,
			ArrayList<QueryGradeHomeworkBean> list, Long classId) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		this.classId = classId;
		this.mList = list;

	}

	@Override
	public int getCount() {
		return mList == null ? 0 : Math.min(5, mList.size());
	}

	@Override
	public QueryGradeHomeworkBean getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hid;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_homework_listview,
					null);

			holder.description_content = (TextView) convertView
					.findViewById(R.id.tv_description_content);
			holder.gridView = (HorizontalListView) convertView
					.findViewById(R.id.gv_images);
			holder.assignBtn = (TextView) convertView.findViewById(R.id.assign);
			holder.timeTv = (TextView) convertView
					.findViewById(R.id.tv_assign_time);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final QueryGradeHomeworkBean bean = getItem(position);

		int avatorW = (int) mContext.getResources().getDimension(
				R.dimen.recentissued_avator_w);
		int avatorH = (int) mContext.getResources().getDimension(
				R.dimen.recentissued_avator_h);

		holder.timeTv.setText(mList.get(position).assignmentTime);
		holder.assignBtn.setText("布置作业");
		String description = "";
		holder.gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (bean.resourseList.get(position).type == 172) {
//					// 播放语音
//					startPlayRecord(bean.resourseList.get(position).fileUrl);
				} else if (bean.resourseList.get(position).type == 166) {
					imageBrower(position, bean.resourseList);
				}
			}
		});
		holder.assignBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mClickListener != null) {
					mClickListener.click(v, position);
				}
			}
		});

		// holder.assignBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// AssingHomework(classId, mList.get(position).hid);
		// //Toast.makeText(mContext, "你点击了第"+(position+1)+"项",
		// Toast.LENGTH_SHORT).show();
		// }
		// });

		if (bean.description != null && bean.description.trim().length() > 0) {

			description += bean.description;
		}

		if ((description == null || description.trim().isEmpty() == true)
				&& (bean.imgList == null || bean.imgList.size() == 0)) {

			holder.description_content.setVisibility(View.GONE);
			holder.gridView.setVisibility(View.GONE);
			holder.assignBtn.setVisibility(View.VISIBLE);

		}
//			else if(description == null || description.trim().isEmpty() == true&& bean.imgList.size()>0){
//			holder.description_content.setVisibility(View.GONE);
//			holder.gridView.setVisibility(View.VISIBLE);
//			holder.assignBtn.setVisibility(View.VISIBLE);
//		} 
		else {
			if(description == null || description.trim().isEmpty() == true){
				holder.description_content.setVisibility(View.GONE);
			}else{
				holder.description_content.setVisibility(View.VISIBLE);
			}
				holder.gridView.setVisibility(View.VISIBLE);
				holder.assignBtn.setVisibility(View.VISIBLE);
				holder.description_content.setText(description);
				if (bean.imgList != null && bean.imgList.size() > 0) {
					holder.gridView.setVisibility(View.VISIBLE);
					mImgGradeListviewAdapter=new ImgGradeListviewAdapter(
							bean.resourseList, mContext) ;
					holder.gridView.setAdapter(mImgGradeListviewAdapter);
					holder.gridView
							.setOnItemClickListener(new AdapterView.OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									if (bean.resourseList.get(position).type == 172) {
										
									} else if (bean.resourseList.get(position).type == 166) {
										imageBrower(position, bean.resourseList);
									}
								}
							});
				} else {
					holder.gridView.setVisibility(View.GONE);
				}
		}
		if(mImgGradeListviewAdapter!=null){
			mImgGradeListviewAdapter
			.setPlayClickListener(new MyOnPlayClickListener() {

				@Override
				public void playClick(View v, int position,
						ImageView imgView,TextView tvRecordTimeVIew,List<QueryGradeHomeworkBean.HomeworkResourse> mList) {
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
		}
		
		return convertView;
	}

	private void imageBrower(int position,
			List<QueryGradeHomeworkBean.HomeworkResourse> imgList) {
		Intent intent = new Intent(mContext, ImageBrowerActivity.class);

		Bundle bundle = new Bundle();
		ArrayList<FeedbackWorkBean> list = new ArrayList<FeedbackWorkBean>();
		FeedbackWorkBean feedBean = null;
		// for(int i=0;i<imgList.size();i++){
		// list.add(imgList.get(i));
		// }
		for (int i = 0; i < imgList.size(); i++) {
			if (imgList.get(i).type == 166) {
				feedBean = new FeedbackWorkBean();
				feedBean.fileUrl = imgList.get(i).fileUrl;
				feedBean.fileType = 1;
				list.add(feedBean);
			}

		}
		bundle.putParcelableArrayList(MyBundleName.BADGE_REMARK_PIC_LIST, list);
		bundle.putInt(MyBundleName.BADGE_REMARK_PIC_POSITION, position);
		bundle.putString("homework", "homework");
		intent.putExtras(bundle);
		mContext.startActivity(intent);

	}

	private static class ViewHolder {

		TextView timeTv;
		TextView description_content;
		HorizontalListView gridView;
		TextView assignBtn;

	}

	public void setListener(MyOnClickListener mClickListener) {
		this.mClickListener = mClickListener;
	}

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
