package com.routon.smartcampus.homework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.routon.common.BaseActivity;
import com.routon.edurelease.R;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.FamilyRecyclerAddHolder;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.FamilyRecyclerImgHolder;
import com.routon.smartcampus.homework.FamilyRecyclerAdapter.OnItemClickListener;
import com.squareup.picasso.Picasso;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FamilyFeedbackActivity extends BaseActivity {

	private static String TAG = "FamilyFeedbackActivity";
	private ImageView studentImgView;
	private TextView studentNameView;

	private RecyclerView familyRecyclerView;
	private RecyclerView teacherRecyclerView;
	private FamilyRecyclerAdapter tAdapter;
	private List<FeedbackHomeworkFileBean> tacherDataList;
	private EditText teacherRemarkEditText;
	private FamilyRecyclerImgHolder mImgHolder;
	private int[] shapeImgs=new int[]{R.drawable.shape_rank1,R.drawable.shape_rank2,R.drawable.shape_rank3,R.drawable.shape_rank4,R.drawable.shape_rank};

	private boolean isRecord;
	private AnimationDrawable animDrawable;
	private FamilyRecyclerAddHolder mAddHolder;
	private FamilyHomeworkBean  familyHomeworkBean;
	private String studentUrl;
	private String studentName;
	private TextView rateView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_homework_feedback);
		this.setMoveBackEnable(false);

		initView();
		initData();
	}

	@SuppressLint("NewApi")
	private void initView() {
		ImageView backBtn = (ImageView) findViewById(R.id.homework_back_btn);
		TextView frontBtn = (TextView) findViewById(R.id.front_b);
		frontBtn.setVisibility(View.GONE);
		TextView nextBtn = (TextView) findViewById(R.id.next_b);
		nextBtn.setVisibility(View.GONE);

		studentImgView = (ImageView) findViewById(R.id.student_img);
		studentNameView = (TextView) findViewById(R.id.student_name);

		rateView = (TextView) findViewById(R.id.family_rate_view);
		rateView.setVisibility(View.VISIBLE);
		
		TextView timeView = (TextView) findViewById(R.id.time_view);
		timeView.setVisibility(View.INVISIBLE);

		teacherRemarkEditText = (EditText) findViewById(R.id.teacher_correct_remark);
		familyRemarkEditText = (EditText) findViewById(R.id.family_homework_remark);
		familyRemarkEditText.setFocusable(false);
		teacherRemarkEditText.setFocusable(false);
		teacherRemarkEditText.setHint("");
		teacherRemarkEditText.setBackground(null);

		familyRecyclerView = (RecyclerView) findViewById(R.id.family_recyclerView);
		teacherRecyclerView = (RecyclerView) findViewById(R.id.teacher_recyclerView);
		LinearLayoutManager familyLayoutManager = new LinearLayoutManager(FamilyFeedbackActivity.this);
		familyLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		familyRecyclerView.setLayoutManager(familyLayoutManager);

		LinearLayoutManager teacherLayoutManager = new LinearLayoutManager(FamilyFeedbackActivity.this);
		teacherLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		teacherRecyclerView.setLayoutManager(teacherLayoutManager);

		
		

		mBackListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isSoftShowing()) {
					closeKeyboard(teacherRemarkEditText);
				}
				FamilyFeedbackActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);

			}
		};
		backBtn.setOnClickListener(mBackListener);

	}

	private void initData() {
		studentName = getIntent().getStringExtra("student_name");
		studentUrl = getIntent().getStringExtra("student_url");
		Bundle bundle = getIntent().getExtras();
        familyHomeworkBean = (FamilyHomeworkBean) bundle.getSerializable("feedback_correct");
		
		
		 setStudentInfo(0);
		 setTeacherData();
		   setFamilyData();
	}



	

	private void setTeacherData() {
		
		if (familyHomeworkBean.remark!=null && !familyHomeworkBean.remark.equals("null")) {
			teacherRemarkEditText.setText(familyHomeworkBean.remark);
		}
		
		final ArrayList<FeedbackHomeworkFileBean> imgBeanList = new ArrayList<FeedbackHomeworkFileBean>();
		for (int i = 0; i < familyHomeworkBean.correctResList.size(); i++) {
			if (familyHomeworkBean.correctResList.get(i).fileType==1) {
				imgBeanList.add(familyHomeworkBean.correctResList.get(i));
			}
		}
		

		tAdapter = new FamilyRecyclerAdapter(FamilyFeedbackActivity.this, familyHomeworkBean.correctResList, false,"tacher");

		tAdapter.setItemClickListener(new OnItemClickListener() {

			@Override
			public void itemClick(View v, int position, String type, FamilyRecyclerImgHolder imgHolder) {
				endPlay();
				if (type.equals("pic_preview")) {// 图片预览

					Intent intent = new Intent(FamilyFeedbackActivity.this, HomeworkPicActivity.class);
					Bundle bundle = new Bundle();
					bundle.putBoolean(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, false);
					bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION,
							imgBeanList.indexOf(familyHomeworkBean.correctResList.get(position)));
					bundle.putSerializable(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST,
							imgBeanList);
					bundle.putString("homework", "homework");
					intent.putExtras(bundle);
					startActivity(intent);
				} else if (type.equals("audio_play")) {// 录音播放
					
					mImgHolder = imgHolder;
					if (imgHolder != null) {
						animDrawable = (AnimationDrawable) imgHolder.playRecordImg.getBackground();
						if (animDrawable != null) {
							animDrawable.start();
						}
						startPlayRecord(familyHomeworkBean.correctResList.get(position).fileUrl, familyHomeworkBean.correctResList.get(position).audioLength,
								imgHolder.playRecordImg);
					}

				}

			}
		});

		teacherRecyclerView.setAdapter(tAdapter);
	}

	

	private void setFamilyData() {
		
		if (familyHomeworkBean.parent_remark!=null && !familyHomeworkBean.parent_remark.equals("null")) {
			familyRemarkEditText.setText(familyHomeworkBean.parent_remark);
		}
		

		final ArrayList<FeedbackHomeworkFileBean> familyImgs = new ArrayList<FeedbackHomeworkFileBean>();
		for (int i = 0; i < familyHomeworkBean.checkResList.size(); i++) {
			if (familyHomeworkBean.checkResList.get(i).fileType == 1) {
				familyImgs.add(familyHomeworkBean.checkResList.get(i));
			}
		}
		FamilyRecyclerAdapter fAdapter = new FamilyRecyclerAdapter(FamilyFeedbackActivity.this, familyHomeworkBean.checkResList,
				false,"family");
		fAdapter.setItemClickListener(new OnItemClickListener() {

			@Override
			public void itemClick(View v, int position, String type, FamilyRecyclerImgHolder imgHolder) {
				endPlay();
				if (type.equals("pic_preview")) {
					Intent intent = new Intent(FamilyFeedbackActivity.this, HomeworkPicActivity.class);
					Bundle bundle = new Bundle();
					bundle.putBoolean(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_ADD_PIC, false);
					bundle.putInt(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_POSITION,
							familyImgs.indexOf(familyHomeworkBean.checkResList.get(position)));
					bundle.putSerializable(com.routon.smartcampus.utils.MyBundleName.BADGE_REMARK_PIC_LIST, familyImgs);
					bundle.putString("homework", "homework");
					intent.putExtras(bundle);
					startActivity(intent);
				} else if (type.equals("audio_play")) {
					
					mImgHolder = imgHolder;
					if (imgHolder != null) {
						animDrawable = (AnimationDrawable) imgHolder.playRecordImg.getBackground();
						if (animDrawable != null) {
							animDrawable.start();
						}
						startPlayRecord(familyHomeworkBean.checkResList.get(position).fileUrl, familyHomeworkBean.checkResList.get(position).audioLength,
								imgHolder.playRecordImg);
					}
				} 

			}
		});

		familyRecyclerView.setAdapter(fAdapter);
	}

	private EditText familyRemarkEditText;


	private void setStudentInfo(int i) {
		
		if (studentUrl!=null) {
			Picasso.with(this).load(studentUrl).error(R.drawable.default_student)
			.placeholder(R.drawable.default_student).fit().into(studentImgView);
		}
		
		if (studentName != null) {
			studentNameView.setText(studentName);
		}
		
		
		List<String> rateStrList = new ArrayList<String>();
		rateStrList.add("优");
		rateStrList.add("良");
		rateStrList.add("中");
		rateStrList.add("差");
		rateStrList.add("未完成");
		
		if (familyHomeworkBean.rateStr==null || familyHomeworkBean.rateStr.equals("") || familyHomeworkBean.rateStr.equals("null")) {
			
			if (familyHomeworkBean.rate<=0) {
				rateView.setVisibility(View.GONE);
			}else {
				rateView.setText(rateStrList.get(familyHomeworkBean.rate-1));
			}
			
		}else {
			
			if (familyHomeworkBean.rate>0 && familyHomeworkBean.rate<=5) {
				rateView.setBackgroundResource(shapeImgs[familyHomeworkBean.rate-1]);
			}
			
			rateView.setText(familyHomeworkBean.rateStr);
		}
	}

	private Timer mTimer;
	private int recordTime;
	private MediaRecorder mMediaRecorder = null;
	// 录音所保存的文件
	private File mAudioFile;
	// 录音文件保存位置
	private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record/";
	private String recordPath;

	public void startRecord() {
		recordTime = 0;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				recordTime++;

				Message msg = new Message();
				msg.what = RECORD_AUDIO_TEXT;
				handler.sendMessage(msg);
			}
		}, 0, 1000);

		// TODO 录音
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		mMediaRecorder.setAudioSamplingRate(44100);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mMediaRecorder.setAudioEncodingBitRate(96000);
		recordPath = mFilePath + System.currentTimeMillis() + ".amr";
		Log.d("recordPath   :", recordPath);
		mAudioFile = new File(recordPath);
		// 创建父文件夹
		mAudioFile.getParentFile().mkdirs();

		try {
			mAudioFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 设置录音保存的文件
		mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());
		// 开始录音
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mMediaRecorder.start();
		// KeyPoint：setOnLongClickListener中return的值决定是否在长按后再加一个短按动作，true为不加短按,false为加入短按

	}

	//
	public void stopRecord() {
		if (recordTime < 3) {
			if (mAudioFile.exists()) {
				mAudioFile.delete();
			}

			Toast.makeText(FamilyFeedbackActivity.this, "录音时间太短,请长按录音!", Toast.LENGTH_SHORT).show();
		} else {
			FeedbackHomeworkFileBean bean = new FeedbackHomeworkFileBean(recordPath, true, 2);
			bean.audioLength = recordTime;
			bean.fileIdparams="audioDuration="+recordTime;
			tacherDataList.add(tacherDataList.size() - 2, bean);
			tAdapter.notifyDataSetChanged();
		}

		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
			recordTime = 0;
		}

		if (mMediaRecorder != null) {
			try {
				// mMediaRecorder.stop();
				mMediaRecorder.reset();
			} catch (IllegalStateException e) {
				mMediaRecorder = null;
				mMediaRecorder = new MediaRecorder();
			}
			mMediaRecorder.release();
			mMediaRecorder = null;
		}

	}

	private MediaPlayer mediaPlayer;
	private Timer mPlayTimer;
	private int playerTime;
	private int audioItemTime;
	private boolean isPlaying;
	private ImageView audioPlayImgView;

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
				
			}
		}, 0, 1000);

	}
	
	public static final int UPLOAD_AUDIO_TEXT = 3;
	public static final int RECORD_AUDIO_TEXT = 4;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case UPLOAD_AUDIO_TEXT:
				mImgHolder.recordTimeView.setText(playerTime + "s");
				break;
			case RECORD_AUDIO_TEXT:
				mAddHolder.tvRecordTime.setText(recordTime + "s");
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

	


	public int dp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}

	public int sp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
	}

	public static <T extends Comparable<T>> boolean compare(List<T> a, List<T> b) {
		if (a.size() != b.size())
			return false;
		Collections.sort(a);
		Collections.sort(b);
		for (int i = 0; i < a.size(); i++) {
			if (!a.get(i).equals(b.get(i)))
				return false;
		}
		return true;
	}
	private boolean isSoftShowing() {
        //获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
 
        return screenHeight - rect.bottom != 0;
    }
	
	public void closeKeyboard(View view) {
	    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	@Override
	protected void onDestroy() {
		endPlay();
		super.onDestroy();
		
	}
}
