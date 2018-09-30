package com.routon.smartcampus.homework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.routon.edurelease.R;
import com.routon.smartcampus.flower.PopOnClickListener;
import com.routon.smartcampus.homework.HomeworkImgListviewAdapter.MyDeleteClickListener;
import com.routon.smartcampus.homework.HomeworkImgListviewAdapter.MyOnTouchListener;
import com.routon.smartcampus.utils.MyConstant;
import com.routon.smartcampus.homework.HomeworkImgListviewAdapter.MyOnItemClickListener;
import com.routon.smartcampus.view.HorizontalListView;

import com.routon.widgets.Toast;

import android.Manifest;
import android.R.anim;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddHomeworkPopWin extends PopupWindow {

	private View view;
	private Context mContext;
	public ArrayList<FeedbackWorkBean> imgList;
	private HomeworkImgListviewAdapter remarkImgListviewAdapter;
	private TextView imgCountView;
	private HorizontalListView listView;
	private EditText editText;
	private ImageView addImgView;
	private TextView awardView;
	public String descriptionText;
	private Button addBtn;
	private Button delBtn;
	//录音
	private MediaRecorder mMediaRecorder = null;  
	//录音所保存的文件
    private File mAudioFile;
	//录音文件保存位置
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
	private String recordPath;
	private MediaPlayer mediaPlayer;
	private long startTime=0;
	private long endTime=0;
	private AnimationDrawable animDrawable;
	 //当前是否正在播放
    private volatile boolean isPlaying=false;
    private ImageView imgView;
    private boolean isDeleteRes=false;
    private static final int RECORDTIME=1;
    private Timer mTimer;
	private int currentTime=0;//实时播放时间
	private TextView recordTimeView;
	private String autioTime=null;
	private ArrayList<String>myImgList=new ArrayList<String>();//预览图片集合
    public AddHomeworkPopWin(Context context,
			final PopOnClickListener popOnClickListener, String editStr,
			String titleNextBtnText, ArrayList<FeedbackWorkBean> savemages,
			String saveRemarkString) {
		this.mContext = context;
		this.view = LayoutInflater.from(context).inflate(
				R.layout.badge_remark_add_layout, null);
		LayoutInflater factory = LayoutInflater.from(mContext); 

		View layout = factory.inflate(R.layout.remark_img_item, null); 
		imgView=(ImageView) layout.findViewById(R.id.play_record_img);
		imgList = new ArrayList<FeedbackWorkBean>();
		editText = (EditText) view.findViewById(R.id.remark_edit_view);
		awardView = (TextView) view.findViewById(R.id.remark_award_text);
		addImgView = (ImageView) view.findViewById(R.id.remark_edit_img);
		addImgView.setImageResource(R.drawable.add_insert_picture);
		imgCountView = (TextView) view.findViewById(R.id.remark_img_count_text);
		listView = (HorizontalListView) view
				.findViewById(R.id.remark_img_listview);
		addBtn = (Button) view.findViewById(R.id.add_btn);
		delBtn = (Button) view.findViewById(R.id.del_btn);
		addBtn.setVisibility(View.GONE);
		delBtn.setVisibility(View.GONE);
		editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				128) });

		awardView.setText(titleNextBtnText);
		awardView.setTextSize(14);
		awardView.setBackgroundResource(R.drawable.assing_homework_bag);
		awardView.setTextColor(Color.WHITE);
		awardView.setGravity(Gravity.CENTER);
		remarkImgListviewAdapter = new HomeworkImgListviewAdapter(mContext,
				imgList);
		imgList = new ArrayList<FeedbackWorkBean>();
		FeedbackWorkBean bean=new FeedbackWorkBean();
		bean.fileUrl="null";
		imgList.add(bean);
		bean=new FeedbackWorkBean();
		bean.fileUrl="record_null";
		imgList.add(bean);
		imgCountView.setText(imgList.size() - 2 + "/9");
		remarkImgListviewAdapter = new HomeworkImgListviewAdapter(mContext,
				imgList);
		remarkImgListviewAdapter.setTouchListener(new MyOnTouchListener() {
			

			@Override
			public void click(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				switch (event.getAction()){
					case MotionEvent.ACTION_DOWN:
						startTime=System.currentTimeMillis();
						startRecord();
						break;
					case MotionEvent.ACTION_UP:
						
						if(imgList.size()-2==9){
							Toast.makeText(mContext, "最多只能添加9条内容！",
									Toast.LENGTH_SHORT).show();
							return;
						}
						
						
						endTime=System.currentTimeMillis();
						FeedbackWorkBean bean=new FeedbackWorkBean();
						
						if((int)((endTime-startTime)/1000)>MyConstant.HOMEWORK_RECORD_MIN_LENGTH){
							bean.fileType=2;
							bean.fileUrl=recordPath;
							bean.audioLength=Math.round((endTime-startTime)/1000+(float)0.5);
							if (bean.audioLength>MyConstant.HOMEWORK_RECORD_MAX_LENGTH) {
								bean.audioLength=MyConstant.HOMEWORK_RECORD_MAX_LENGTH;
							}
							imgList.add(0,bean);
							remarkImgListviewAdapter.notifyDataSetChanged();
							imgCountView.setText(imgList.size() - 2 + "/9");
							stopRecord();
						}else{
							stopRecord();
							Toast.makeText(mContext, "录音时间太短,请长按录音!", Toast.LENGTH_LONG).show();
						}
						
						break;
				}
			}
		});
		listView.setAdapter(remarkImgListviewAdapter);

		if (editStr != null) {
			descriptionText = editStr;
			editText.setText(editStr);
			editText.setSelection(editStr.length());
		} else {
			editText.setHint("请输入");
		}
		if (savemages != null && savemages.size() > 0) {
			imgList.clear();
			FeedbackWorkBean bean1=new FeedbackWorkBean();
			bean1.fileUrl="null";
			imgList.add(bean1);
			bean1=new FeedbackWorkBean();
			bean1.fileUrl="record_null";
			imgList.add(bean1);
			
			for (int i = 0; i < savemages.size(); i++) {
				FeedbackWorkBean bean2=new FeedbackWorkBean();
				if(savemages.get(i)!=null){
					bean2.fileUrl=savemages.get(i).fileUrl;
					bean2.fileType=savemages.get(i).fileType;
					bean2.audioLength=savemages.get(i).audioLength;
					bean2.isLocal=savemages.get(i).isLocal;
					bean2.fileId=savemages.get(i).fileId;
					imgList.add(0,bean2);
				}
				
			}
			imgCountView.setText(imgList.size() - 2 + "/9");
			remarkImgListviewAdapter.notifyDataSetChanged();
		}
		if (saveRemarkString != null) {
			descriptionText = saveRemarkString;
			editText.setText(saveRemarkString);
			editText.setSelection(saveRemarkString.length());
		}

		//语音播放点击事件
		remarkImgListviewAdapter.setItemClickListener(new MyOnItemClickListener() {
			
			@Override
			public void itemClick(View v, int position, ImageView imgView,TextView tvRecordTime) {
				// TODO Auto-generated method stub
			
					if(imgList.get(position).fileType==2){
						if(!isPlaying){
							autioTime=String.valueOf(imgList.get(position).audioLength);
							animDrawable = (AnimationDrawable)imgView.getBackground();
							if(animDrawable!=null){
								animDrawable.start();
							}
							recordTimeView=tvRecordTime;
							startPlayRecord(imgList.get(position).fileUrl,tvRecordTime,autioTime,position);
						}else{
							playEndOrFail(tvRecordTime, autioTime, position);
						}
					}
			  }
		});
		//删除图片或者语音
		remarkImgListviewAdapter.setDeleteClickListener(new MyDeleteClickListener() {
			
			@Override
			public void deleteClick(View v, int position) {
				// TODO Auto-generated method stub
				imgList.remove(position);
				imgCountView.setText(imgList.size() - 2 + "/9");
				isDeleteRes=true;
				remarkImgListviewAdapter.notifyDataSetChanged();
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(isDeleteRes){
					isDeleteRes=false;//判断是否点击了删除图片按钮，如果点击不再执行item点击事件
					return;
				}
				if (position == imgList.size() - 2) {// 添加图片
					if (imgList.size() < 11) {
						popOnClickListener.lastItemtemClick();
					} else {
						Toast.makeText(mContext, "最多只能添加9张图片！",
								Toast.LENGTH_SHORT).show();
					}

				}else if(position == imgList.size() - 1){
//					Toast.makeText(mContext, "长按录音！", Toast.LENGTH_SHORT).show();
				}  
				else {
					if(imgList.get(position).fileType==2){
//						startPlayRecord(imgList.get(position).fileUrl);
					}else{
						myImgList.clear();
						getMyImgList(imgList);
						if(myImgList.size()>0){
							for(int i=0;i<myImgList.size();i++){
								if(myImgList.get(i)==imgList.get(position).fileUrl){
									popOnClickListener.itemClick(i);
								}
							}
							
						}
						
					}
					
				}
			}
		});

		addImgView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (imgList.size() < 11) {
					popOnClickListener.lastItemtemClick();
				} else {
					Toast.makeText(mContext, "最多只能添加9张图片！", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		awardView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popOnClickListener.awardClick();

			}
		});

		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				descriptionText = String.valueOf(s);
			}
		});
		// 设置外部可点击
		this.setOutsideTouchable(true);
		// this.view.setOnTouchListener(new View.OnTouchListener() {
		//
		// public boolean onTouch(View v, MotionEvent event) {
		//
		// int height = view.findViewById(R.id.pop_layout).getTop();
		//
		// int y = (int) event.getY();
		// if (event.getAction() == MotionEvent.ACTION_UP) {
		// if (y < height) {
		//
		// }
		// }
		// return true;
		// }
		// });

		/* 设置弹出窗口特征 */
		// 设置视图
		this.setContentView(this.view);
		// 设置弹出窗体的宽和高
		this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

		// 设置弹出窗体可点击
		this.setFocusable(true);

		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// 设置弹出窗体的背景
		this.setBackgroundDrawable(dw);

		// 设置弹出窗体显示时的动画，从底部向上弹出
		this.setAnimationStyle(R.style.take_photo_anim);

	}
    //获取预览图片集合//
    public void getMyImgList(ArrayList<FeedbackWorkBean> imgList){
    	for(int i=0;i<imgList.size();i++){
    		if(imgList.get(i).fileType!=2){
        		myImgList.add(imgList.get(i).fileUrl);
        	}
    	}
    	
    }
	public void addImgList(ArrayList<FeedbackWorkBean> imgs) {
		imgList.addAll(0, imgs);
		imgCountView.setText(imgList.size() - 2 + "/9");
		remarkImgListviewAdapter.notifyDataSetChanged();

	}

	public ArrayList<FeedbackWorkBean> getRemarkImages() {
		return imgList;
	}

	public String getRemarkText() {
		return descriptionText;
	}

	public void updateImgList(ArrayList<FeedbackWorkBean> imgs) {
		imgList.clear();
		imgList.addAll(imgs);
		imgCountView.setText(imgList.size() - 2 + "/9");
		remarkImgListviewAdapter.notifyDataSetChanged();

	}
	
	//开始录音
		private void startRecord() {
			recordTime2 = 0;
			myTimer = new Timer();
			myTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					
					if (recordTime2==MyConstant.HOMEWORK_RECORD_MAX_LENGTH) {
						
						stopRecord();
						return;
					}
					
					recordTime2++;

					
				}
			}, 0, 1000);
			
			
			// TODO Auto-generated method stub
			 mMediaRecorder = new MediaRecorder();  
			 mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		          //设置保存文件格式为MP4
		     mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		          //设置采样频率,44100是所有安卓设备都支持的频率,频率越高，音质越好，当然文件越大
		     mMediaRecorder.setAudioSamplingRate(44100);
		          //设置声音数据编码格式,音频通用格式是AAC
		     mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		          //设置编码频率
		     mMediaRecorder.setAudioEncodingBitRate(96000);
		     recordPath=mFilePath + System.currentTimeMillis() + ".amr";
	         mAudioFile = new File(recordPath);
	         //创建父文件夹
	         mAudioFile.getParentFile().mkdirs();
	         
	         try {
					mAudioFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	         //配置mMediaRecorder相应参数
	         //从麦克风采集声音数据
	       
	         //设置录音保存的文件
	         mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());
	         //开始录音
	         try {
					mMediaRecorder.prepare();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	         mMediaRecorder.start();
	          //KeyPoint：setOnLongClickListener中return的值决定是否在长按后再加一个短按动作，true为不加短按,false为加入短按  
		}
		//停止录音
		public void stopRecord(){
			if(mMediaRecorder!=null){
				try{
//					mMediaRecorder.stop();
					mMediaRecorder.reset();
				}catch (IllegalStateException e) {  
					mMediaRecorder=null;
					mMediaRecorder=new MediaRecorder();
				}
				mMediaRecorder.release();
				mMediaRecorder=null;
			}
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
		private void playEndOrFail(TextView tvRecordTime,String autioTime,int position) {
			isPlaying = false;
			// if (isEnd) {
			// mHandler.sendEmptyMessage(Constant.PLAY_COMPLETION);
			// } else {
			// mHandler.sendEmptyMessage(Constant.PLAY_ERROR);
			// }
			if(Integer.valueOf(autioTime)<10){
				recordTimeView.setText("00:0"+autioTime);
			}else{
				recordTimeView.setText("00:"+autioTime);
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
		private Timer myTimer;
		private int recordTime2;
}
