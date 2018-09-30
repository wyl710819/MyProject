package com.routon.inforelease.plan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.R;
import com.routon.inforelease.json.ImgEffectsBean;
import com.routon.inforelease.json.ImgEffectsBeanParser;
import com.routon.inforelease.json.ImgEffectsitemsBean;
import com.routon.inforelease.json.PlanMaterialparamsBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.util.TimeUtils;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class ImageAttributeSettingActivity extends CustomTitleActivity {
	private static final String TAG = "ImageAttr";
	
	private TextView mTextStartTime;
	private TextView mTextEndTime;
	
	private EditText mEditElapsedTime;
	private Spinner mSpinnerEffect;
	
	private List<String> mAdIds;
	private String mStartTime;
	private String mEndTime;
	private int mElapsedTime;
	private String mEffect;
	//数据是否上传
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
			mElapsedTime = StringUtils.toInteger(bundle.getString(Integer.toString(AdParams.ELAPSE_TIME)), 30);
			mEffect = bundle.getString(Integer.toString(AdParams.IMAGE_EFFECT));	
			
			mSubmitMode = bundle.getBoolean("submit_mode", false);
		}
		Log.v(TAG, "mEffect: " + mEffect);
		
		setContentView(R.layout.activity_plan_make_picture_attribute);
		initViews();
		
		HttpClientDownloader.getInstance().getResultFromUrlWithSession(UrlUtils.getImgEffectsUrl(), mHandler, MSG_GET_IMAGE_EFFECTS);
	}

	private void initViews() {
		mTextStartTime = (TextView) findViewById(R.id.start_time);
		mTextEndTime = (TextView) findViewById(R.id.end_time);
		
		mEditElapsedTime = (EditText) findViewById(R.id.edit_elapse_time);
		mSpinnerEffect = (Spinner) findViewById(R.id.spinner_effect);
		
		mSelAllCb = (CheckBox)findViewById(R.id.select_all);
		
//		findViewById(R.id.btn_save_plan).setOnClickListener(mOnBtnClickedListener);
//		findViewById(R.id.btn_return).setOnClickListener(mOnBtnClickedListener);
		// title bar
		
		this.initTitleBar(R.string.image_attribute_setting);
		this.setTitleNextImageBtnClickListener(R.drawable.ok, mOnBtnClickedListener);
		
		mTextStartTime.setOnClickListener(mOnBtnClickedListener);
		mTextEndTime.setOnClickListener(mOnBtnClickedListener);
		
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
		
		mEditElapsedTime.setText(Integer.toString(mElapsedTime));
	}
	
	private void submitAdParam() {
		if ( mSubmitMode == false ) {
			finishAttributeSetting();
		} else {
			List<PlanMaterialparamsBean> params = new ArrayList<PlanMaterialparamsBean>();
			params.add(new PlanMaterialparamsBean(AdParams.BEGIN_TIME, mTextStartTime.getText().toString()));
			params.add(new PlanMaterialparamsBean(AdParams.END_TIME, mTextEndTime.getText().toString()));
			params.add(new PlanMaterialparamsBean(AdParams.ELAPSE_TIME, mEditElapsedTime.getText().toString()));
			ImgEffectsitemsBean item = (ImgEffectsitemsBean) mSpinnerEffect.getSelectedItem();
			params.add(new PlanMaterialparamsBean(AdParams.IMAGE_EFFECT, item.getKey()));
			
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
	
	private static final int MSG_SUBMIT_AD_PARAM = 0;
	private static final int MSG_GET_IMAGE_EFFECTS = 1;
	
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
					
				case MSG_GET_IMAGE_EFFECTS:
					onGetImageEffects((String) msg.obj);
					break;
				}
			}
			break;
			}
		}
	};
	
	private void finishAttributeSetting() {
		String startStr = mTextStartTime.getText().toString();
		String endStr= mTextEndTime.getText().toString();
		Calendar startTime = TimeUtils.getFormatCalendar(startStr,TimeUtils.FORMAT_yyyy_MM_dd);
		Calendar endTime = TimeUtils.getFormatCalendar(endStr,TimeUtils.FORMAT_yyyy_MM_dd);
//		if( TimeUtils.isDateBefore(startTime, Calendar.getInstance()) == true ){
//			this.reportToast("起始时间早于当前时间");
//			return;
//		}
		if( TimeUtils.isTimeBeforeTilMinute(endTime, startTime) == true ){
			this.reportToast("结束时间早于起始时间");
			return;
		}
		Intent data = new Intent();
		//translate 
		data.putExtra(Integer.toString(AdParams.BEGIN_TIME), mTextStartTime.getText().toString());
		data.putExtra(Integer.toString(AdParams.END_TIME), mTextEndTime.getText().toString());
		data.putExtra(Integer.toString(AdParams.ELAPSE_TIME), mEditElapsedTime.getText().toString());
		ImgEffectsitemsBean item = (ImgEffectsitemsBean) mSpinnerEffect.getSelectedItem();
		data.putExtra(Integer.toString(AdParams.IMAGE_EFFECT), item.getKey());
		data.putExtra("isChange", true);
		data.putExtra("modifyAll", mSelAllCb.isChecked());
		setResult(Activity.RESULT_OK, data);
		finish();
	}

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
			//Toast.makeText(this, "change ad param failed", Toast.LENGTH_SHORT).show();
			reportToast(bean.msg);
		}
	}
	
	private void onGetImageEffects(String text) {
		if (text == null) {
			return;
		}

		ImgEffectsBean bean = ImgEffectsBeanParser.parseImgEffectsBean(text);
		if (bean == null) {
			return;
		}
		
		if (bean.code == 1) {
			mSpinnerEffect.setAdapter(new ImgEffectAdapter(bean.imgEffects.items));
			if (mEffect == null) {
				mEffect = Integer.toString(bean.imgEffects.defaultKey);
			}
			int position = 0;
			for (ImgEffectsitemsBean item : bean.imgEffects.items) {		
				Log.v(TAG, "key: " + item.getKey());
				if (mEffect.equals(item.getKey())) {
					Log.v(TAG, "select pos: " + position);
					mSpinnerEffect.setSelection(position);
					break;
				}
				position++;
			}
			//mSpinnerEffect.setAdapter(new ArrayAdapter<ImgEffectsitemsBean>(this, android.R.layout.simple_expandable_list_item_1, bean.imgEffects.items));
		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast(bean.msg);
		}
	}
	
	private class ImgEffectAdapter extends BaseAdapter {
		private List<ImgEffectsitemsBean> mEffectItems;

		public ImgEffectAdapter(List<ImgEffectsitemsBean> items) {
			mEffectItems = items;
		}

		@Override
		public int getCount() {
			return mEffectItems != null ? mEffectItems.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mEffectItems != null ? mEffectItems.get(position) : null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView) convertView;
			if (view == null) {
				view = new TextView(parent.getContext());
				view.setTextSize(parent.getContext().getResources().getDimension(R.dimen.img_effects_font));
			}
			
			ImgEffectsitemsBean bean = mEffectItems.get(position);
			view.setText(bean.getValue());
			
			return view;
		}		
	}
}
