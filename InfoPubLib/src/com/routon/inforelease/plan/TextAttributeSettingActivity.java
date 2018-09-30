package com.routon.inforelease.plan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.R;
import com.routon.inforelease.json.PlanMaterialparamsBean;
import com.routon.inforelease.util.TimeUtils;
import com.routon.inforelease.widget.ColorPickerDialog;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class TextAttributeSettingActivity extends CustomTitleActivity {
	private TextView mTextStartTime;
	private TextView mTextEndTime;
	private TextView mTextColorView;
	private TextView mTextBgColorView;
	private EditText mEditAlpha;
	
	private List<String> mAdIds;
	private String mStartTime;
	private String mEndTime;
	private String mTextColor;
	private String mTextBgColor;
	private String mTextBgAlpha;

	private boolean mSubmitMode;
	
	private CheckBox mSelAllCb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mAdIds = bundle.getStringArrayList("ids");
			mStartTime = TimeUtils.getDate(bundle.getString(Integer.toString(AdParams.BEGIN_TIME)));
			mEndTime = TimeUtils.getDate(bundle.getString(Integer.toString(AdParams.END_TIME)));
			mTextColor = bundle.getString(Integer.toString(AdParams.TEXT_COLOR), "#042398");
			mTextBgColor = bundle.getString(Integer.toString(AdParams.TEXT_BG_COLOR), "#ffffff");
			mTextBgAlpha = bundle.getString(Integer.toString(AdParams.TEXT_BG_ALPHA), "80");
			
			mSubmitMode = bundle.getBoolean("submit_mode", false);
		}
		
		setContentView(R.layout.activity_plan_make_text_attribute);
		initViews();
	}

	private void initViews() {
		mTextStartTime = (TextView) findViewById(R.id.start_time);
		mTextEndTime = (TextView) findViewById(R.id.end_time);
		mTextColorView = (TextView) findViewById(R.id.btn_select_text_color);
		mTextBgColorView = (TextView) findViewById(R.id.btn_select_background_color);
		mEditAlpha = (EditText) findViewById(R.id.edit_background_alpha);
		mSelAllCb = (CheckBox)findViewById(R.id.select_all);
		
		mEditAlpha.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				String t = s.toString();
				int v = StringUtils.toInteger(t, 0);
				if (v < 0) {
					mEditAlpha.setText("0");
				} else if (v > 255) {
					mEditAlpha.setText("255");
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
//		findViewById(R.id.btn_save_plan).setOnClickListener(mOnBtnClickedListener);
//		findViewById(R.id.btn_return).setOnClickListener(mOnBtnClickedListener);
//		findViewById(R.id.btn_select_start_time).setOnClickListener(mOnBtnClickedListener);
//		findViewById(R.id.btn_select_end_time).setOnClickListener(mOnBtnClickedListener);
		// title bar
		
		this.initTitleBar(R.string.text_attribute_setting);
		this.setTitleNextImageBtnClickListener(R.drawable.ok, mOnBtnClickedListener);

		mTextStartTime.setOnClickListener(mOnBtnClickedListener);
		mTextEndTime.setOnClickListener(mOnBtnClickedListener);
		mTextColorView.setOnClickListener(mOnBtnClickedListener);
		mTextBgColorView.setOnClickListener(mOnBtnClickedListener);

		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);
		if (mStartTime == null) {
			mStartTime = sdf.format(calendar.getTime());
		}
		mTextStartTime.setText(mStartTime);
		if (mEndTime == null) {
			calendar.add(Calendar.YEAR, 1);
			mEndTime = sdf.format(calendar.getTime());
		}
		mTextEndTime.setText(mEndTime);
		
		mTextColorView.setBackgroundColor(Color.parseColor(mTextColor));
		mTextBgColorView.setBackgroundColor(Color.parseColor(mTextBgColor));
		
		mTextColorView.setText(mTextColor);
		mTextBgColorView.setText(mTextBgColor);
		mEditAlpha.setText(mTextBgAlpha);
	}
	
	private void submitAdParam() {
		if ( mSubmitMode == false ) {
			finishAttributeSetting();
		} else {
			List<PlanMaterialparamsBean> params = new ArrayList<PlanMaterialparamsBean>();
			params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, mTextStartTime.getText().toString()));
			params.add(new PlanMaterialparamsBean(AdParams.END_TIME, mTextEndTime.getText().toString()));
			params.add(new PlanMaterialparamsBean(AdParams.TEXT_COLOR, mTextColorView.getText().toString()));
			params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_COLOR, mTextBgColorView.getText().toString()));
			params.add(new PlanMaterialparamsBean(AdParams.TEXT_BG_ALPHA, mEditAlpha.getText().toString()));
			
			HttpClientDownloader.getInstance().adParam(mAdIds, params, mHandler, MSG_SUBMIT_AD_PARAM);
		}
	}
	
	private View.OnClickListener mOnBtnClickedListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if( v.getId() == R.id.next_step ){
				submitAdParam();
			}else if( v.getId() == R.id.start_time ){
				Calendar calendar = Calendar.getInstance();
				String s = mTextStartTime.getText().toString();
				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);
				try {
					calendar.setTime(sdf.parse(s));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				showDatePicker(R.id.start_time, calendar);
			}else if( v.getId() == R.id.end_time ){
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.YEAR, 1);
				String s = mTextEndTime.getText().toString();
				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);
				try {
					calendar.setTime(sdf.parse(s));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				showDatePicker(R.id.end_time, calendar);
			}else if( v.getId() == R.id.btn_select_text_color ){
				showColorPickerDialog(R.id.btn_select_text_color, mTextColor);
			}else if( v.getId() == R.id.btn_select_background_color ){
				showColorPickerDialog(R.id.btn_select_background_color, mTextBgColor);
			}
		}
	};
	
	private void showDatePicker(final int id, Calendar calendar) {		
		DatePickerDialog dlg = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd);
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, monthOfYear, dayOfMonth);
				if( id == R.id.start_time ){
					mTextStartTime.setText(sdf.format(calendar.getTime()));
				}else if( id == R.id.end_time ){
					mTextEndTime.setText(sdf.format(calendar.getTime()));
				}
			}
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));	
		dlg.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis()-1000);
		dlg.show();
	}
	
	private void showColorPickerDialog(final int id, String color) {
		int initalColor = Color.parseColor(color);
		ColorPickerDialog dlg = new ColorPickerDialog(this, "Picker", initalColor, new ColorPickerDialog.OnColorChangedListener() {
			
			@Override
			public void colorChanged(int color) {
				String strColor = String.format("#%06X", 0xFFFFFF & color);
				if( id == R.id.btn_select_text_color ){
					mTextColorView.setBackgroundColor(color);
					mTextColorView.setText(strColor);
				}else if( id == R.id.btn_select_background_color ){
					mTextBgColorView.setBackgroundColor(color);
					mTextBgColorView.setText(strColor);
				}
			}
		});
		dlg.show();
	}
	
	private static final int MSG_SUBMIT_AD_PARAM = 0;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
			{
				switch (msg.arg1) {
				case MSG_SUBMIT_AD_PARAM:
					onSubmitAdParam((String) msg.obj);
					break;
				}
			}
			break;
			}
		}
	};
	
	protected void onSubmitAdParam(String text) {
		Log.v("adparam", "ad param: " + text);
		if (text == null) {
			return;
		}
		
		BaseBean bean = BaseBeanParser.parseBaseBean(text);
		if (bean == null) {
			return;
		}
		
		if (bean.code == 1) {			
			//Toast.makeText(this, "change ad param sucess", Toast.LENGTH_SHORT).show();
			finishAttributeSetting();
		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast(bean.msg);
		}
	}
	
	private void finishAttributeSetting() {
		String startStr = mTextStartTime.getText().toString();
		String endStr= mTextEndTime.getText().toString();
		Calendar startTime = TimeUtils.getFormatCalendar(startStr,TimeUtils.FORMAT_yyyy_MM_dd);
		Calendar endTime = TimeUtils.getFormatCalendar(endStr,TimeUtils.FORMAT_yyyy_MM_dd);
		if( TimeUtils.isTimeBeforeTilMinute(endTime, startTime) == true ){
			this.reportToast("结束时间早于起始时间");
			return;
		}
		Intent data = new Intent();
		data.putExtra(Integer.toString(AdParams.BEGIN_TIME), mTextStartTime.getText().toString());
		data.putExtra(Integer.toString(AdParams.END_TIME), mTextEndTime.getText().toString());
		data.putExtra(Integer.toString(AdParams.TEXT_COLOR), mTextColorView.getText().toString());
		data.putExtra(Integer.toString(AdParams.TEXT_BG_COLOR), mTextBgColorView.getText().toString());
		data.putExtra(Integer.toString(AdParams.TEXT_BG_ALPHA), mEditAlpha.getText().toString());
		data.putExtra("isChange", true);
		data.putExtra("modifyAll", mSelAllCb.isChecked());
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}
