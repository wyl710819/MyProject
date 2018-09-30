package com.routon.smartcampus.view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.routon.edurelease.R;
import com.routon.smartcampus.flower.PopOnClickListener;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.routon.widgets.Toast;

public class TakePhotoPopWin extends PopupWindow {

	private View view;
	private Context mContext;
	public ArrayList<String> imgList;
	private RemarkImgListviewAdapter remarkImgListviewAdapter;
	private TextView imgCountView;
	private HorizontalListView listView;
	private EditText editText;
	private ImageView addImgView;
	private TextView awardView;
	private Button addBtn;
	private Button delBtn;
	private TextView bonusPointsTextView;
	public String remarkTextStr;
	public int bonusPoints;
	//录音
	private MediaRecorder mMediaRecorder = null;  
	 //录音所保存的文件
    private File mAudioFile;
  //录音文件保存位置
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio/";
	public TakePhotoPopWin(Context context, final PopOnClickListener popOnClickListener, String editStr,
			String titleNextBtnText, String editHintStr, int bonuspoint) {
		this.mContext = context;

		init(popOnClickListener, titleNextBtnText, editHintStr, bonuspoint);

		if (editStr != null) {
			remarkTextStr = editStr;
			editText.setText(editStr);
			editText.setSelection(editStr.length());
		} else {
			editText.setHint(editHintStr);
		}

	}

	private void init(final PopOnClickListener popOnClickListener, String titleNextBtnText, String editHintStr, int point) {
		// TODO Auto-generated method stub
		this.view = LayoutInflater.from(mContext).inflate(R.layout.badge_remark_add_layout, null);

		editText = (EditText) view.findViewById(R.id.remark_edit_view);
		awardView = (TextView) view.findViewById(R.id.remark_award_text);
		addImgView = (ImageView) view.findViewById(R.id.remark_edit_img);
		imgCountView = (TextView) view.findViewById(R.id.remark_img_count_text);
		listView = (HorizontalListView) view.findViewById(R.id.remark_img_listview);
		
		addBtn = (Button) view.findViewById(R.id.add_btn);
		delBtn = (Button) view.findViewById(R.id.del_btn);
		bonusPointsTextView = (TextView) view.findViewById(R.id.bonuspoint_text);
		bonusPointsTextView.setText(""+point);
		
		this.bonusPoints = point;
		addBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bonusPoints++;
				bonusPointsTextView.setText(""+bonusPoints);
			}
		});
		
		delBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bonusPoints--;
				bonusPointsTextView.setText(""+bonusPoints);
			}
		});
		
		awardView.setText(titleNextBtnText);

		imgList = new ArrayList<String>();
		imgList.add("null");
		imgList.add("record_null");
		imgCountView.setText(imgList.size() - 2 + "/9");
		remarkImgListviewAdapter = new RemarkImgListviewAdapter(mContext, imgList);
		listView.setAdapter(remarkImgListviewAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == imgList.size() - 2) {// 添加图片
					if (imgList.size() < 11) {
						popOnClickListener.lastItemtemClick();
					} else {
						Toast.makeText(mContext, "最多只能添加９张图片！", Toast.LENGTH_SHORT).show();
					}

				}else if(position == imgList.size() - 1){
					Toast.makeText(mContext, "长按录音！", Toast.LENGTH_SHORT).show();
				} else  {
					popOnClickListener.itemClick(position);
				}
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(position==imgList.size()-1){
					Toast.makeText(mContext, "你点击了录音", Toast.LENGTH_LONG).show();
					startRecord();
				}
				return false;
			}

			
		});
		addImgView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				popOnClickListener.saveRemark(v);

			}
		});

		awardView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popOnClickListener.awardClick();

			}
		});
//		saveRemark.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				popOnClickListener.saveRemark();
//			}
//		});
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				remarkTextStr = String.valueOf(s);
			}
		});
		// 设置外部可点击
		this.setOutsideTouchable(true);

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

	public TakePhotoPopWin(Context context, PopOnClickListener popOnClickListener,
			String editStr, String titleNextBtnText, String editHintStr, ArrayList<String> savemages,
			String saveRemarkString, int bonuspoint) {
		
		this.mContext = context;
		init(popOnClickListener, titleNextBtnText, editHintStr, bonuspoint);
		
		if (editStr != null) {
			remarkTextStr = editStr;
			editText.setText(editStr);
			editText.setSelection(editStr.length());
		} else {
			editText.setHint(editHintStr);
		}
		
		
		if (savemages!=null && savemages.size()>0) {
			imgList.clear();
			for (int i = 0; i < savemages.size(); i++) {
				imgList.add(savemages.get(i));
			}
			imgCountView.setText(imgList.size() - 2 + "/9");
			remarkImgListviewAdapter.notifyDataSetChanged();
		}
		if (saveRemarkString!=null) {
			remarkTextStr=saveRemarkString;
			editText.setText(saveRemarkString);
			editText.setSelection(saveRemarkString.length());
		}
		
	}

	public void addImgList(ArrayList<String> imgs) {
		imgList.addAll(0, imgs);
		imgCountView.setText(imgList.size() - 2 + "/9");
		remarkImgListviewAdapter.notifyDataSetChanged();

	}

	public ArrayList<String> getRemarkImages() {
		return imgList;
	}

	public String getRemarkText() {
		return remarkTextStr;
	}
	
	public int getBonusPoints() {
		return bonusPoints;
	}

	public void setBonusPoints(int bonusPoints) {
		this.bonusPoints = bonusPoints;
	}

	public void updateImgList(ArrayList<String> imgs) {
		imgList.clear();
		imgList.addAll(imgs);
		imgCountView.setText(imgList.size() - 2 + "/9");
		remarkImgListviewAdapter.notifyDataSetChanged();

	}
	//开始录音
	private void startRecord() {
		// TODO Auto-generated method stub
		 mMediaRecorder = new MediaRecorder();  
         mAudioFile = new File(mFilePath + System.currentTimeMillis() + ".mp3");
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
         mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
         //设置保存文件格式为MP4
         mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
         //设置采样频率,44100是所有安卓设备都支持的频率,频率越高，音质越好，当然文件越大
         mMediaRecorder.setAudioSamplingRate(44100);
         //设置声音数据编码格式,音频通用格式是AAC
         mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
         //设置编码频率
         mMediaRecorder.setAudioEncodingBitRate(96000);
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
}
