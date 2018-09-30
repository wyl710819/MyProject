package com.routon.inforelease.plan;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.R;
import com.routon.inforelease.json.FindAdPeriodsBean;
import com.routon.inforelease.json.FindAdPeriodsBeanParser;
import com.routon.inforelease.json.FindAdPeriodsperiodsBean;
import com.routon.inforelease.plan.adapter.TimeListAdapter;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.TimeUtils;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

public class TimeSettingActivity extends CustomTitleActivity {
	private static final String TAG = "TimeSetting";
	
	private ListView mListTime;
	private List<TimeData> mTimeDataList = new ArrayList<TimeData>();

	private List<String> mAdIds;
	
	// for offline
	private boolean mIsOfflineMode = false;
	private ArrayList<FindAdPeriodsperiodsBean> mPeriodsList;
	private CheckBox mSelAllCb;
	private boolean mIsPic = false;
	private boolean mOffLinePlan=true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mAdIds = bundle.getStringArrayList("ids");
			
			mIsOfflineMode = bundle.getBoolean("offline_mode", false);
			mPeriodsList = bundle.getParcelableArrayList("periods");
			mIsPic = bundle.getBoolean("is_pic",false);
			mOffLinePlan=bundle.getBoolean(CommonBundleName.OFFLINE_TAG);
		}
		
		setContentView(R.layout.activity_plan_make_time_setting);
		initViews();
		
		if (!mIsOfflineMode) {
			if (mAdIds != null && mAdIds.size() > 0) {
				HttpClientDownloader.getInstance().findAdPeriods(mAdIds.get(0), mHandler, MSG_FIND_AD_PERIODS);
			}
		} else {
			for (FindAdPeriodsperiodsBean pb : mPeriodsList) {
				mTimeDataList.add(new TimeData(null, TimeUtils.getTime(pb.beginTime), TimeUtils.getTime(pb.endTime), pb.loops, pb.max));
			}
			mListTime.setAdapter(new TimeListAdapter(this, mTimeDataList));
		}
	}
	
	private void initViews() {
		mListTime = (ListView) findViewById(R.id.list_time);
		mSelAllCb = (CheckBox)findViewById(R.id.select_all);
		if( mIsPic == true ){
			mSelAllCb.setText(R.string.modify_all);
		}else{
			mSelAllCb.setText(R.string.modify_all_text);
		}
		findViewById(R.id.btn_add_time).setOnClickListener(mOnBtnClickedListener);
		findViewById(R.id.btn_del_time).setOnClickListener(mOnBtnClickedListener);
		
		
		// title bar	
		this.initTitleBar(R.string.play_time_setting);
		this.setTitleNextImageBtnClickListener(R.drawable.ok, mOnBtnClickedListener);
		
//		mListTime.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//
//			@Override
//			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//				showExtraActionDlg(position);
//				return true;
//			}
//		});
	}
	
	private void onAddTime() {
		mTimeDataList.add(new TimeData("Time", "00:00", "24:00", 1, 100));
		
		TimeListAdapter adapter = (TimeListAdapter)(mListTime.getAdapter());
		adapter.notifyDataSetChanged();
	}
	
//	private void showExtraActionDlg(final int position) {
//		AlertDialog dlg = new AlertDialog.Builder(this).setItems(R.array.plan_extra_action_array, new OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				switch (which) {
//				case 0:
//					onDeleteTime(position);
//					break;
//				}
//			}
//		}).create();
//		dlg.show();
//	}
	
//	void onDeleteTime(int position) {
//		mTimeDataList.remove(position);
//		TimeListAdapter adapter = (TimeListAdapter) mListTime.getAdapter();
//		adapter.notifyDataSetChanged();
//	}
	
	private void onSubmitAdPeriods() {
		if (mTimeDataList.size() == 0) {
			Toast.makeText(this, "empty ad periods", Toast.LENGTH_SHORT).show();
			return;
		}
		if (mIsOfflineMode) {
			ArrayList<FindAdPeriodsperiodsBean> periods = new ArrayList<FindAdPeriodsperiodsBean>();
			for (TimeData td : mTimeDataList) {
				FindAdPeriodsperiodsBean bean = new FindAdPeriodsperiodsBean();
				bean.loops = td.repeat_time;
				bean.max = td.max_repeat_time;
				if(mOffLinePlan)
				{
					bean.beginTime = TimeUtils.convertDate(td.start_time, TimeUtils.FORMAT_HH_mm, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
					bean.endTime = TimeUtils.convertDate(td.end_time, TimeUtils.FORMAT_HH_mm, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
				}
				else {
					bean.beginTime=td.start_time;
					bean.endTime=td.end_time;
				}
				periods.add(bean);
			}
			Intent data = new Intent();
			data.putParcelableArrayListExtra("periods", periods);
			data.putExtra("isChange", true);
			data.putExtra("modifyAll", mSelAllCb.isChecked());
			setResult(RESULT_OK, data);
			finish();
		} else {
			HttpClientDownloader.getInstance().adPeriods(mAdIds, mTimeDataList, mHandler, MSG_AD_PERIODS);
		}
	}

	private View.OnClickListener mOnBtnClickedListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if( v.getId() == R.id.next_step ){
				onSubmitAdPeriods();
			}else if( v.getId() == R.id.btn_add_time ){
				onAddTime();
			}else if( v.getId() == R.id.btn_del_time ){
				TimeListAdapter adapter = (TimeListAdapter) mListTime.getAdapter();
				int  count = adapter.getSelCount();
				if( count == 0 ){
					TimeSettingActivity.this.reportToast("至少选择一个时段");
					return;
				}else if( adapter.getCount() == count ){
					TimeSettingActivity.this.reportToast("至少保留一个时段");
					return;
				}
				adapter.deleteSelDatas();
			}
		}
	};
	
	private void onFindAdPeriods(String text) {
		if (text == null) {
			return;
		}

		FindAdPeriodsBean bean = FindAdPeriodsBeanParser.parseFindAdPeriodsBean(text);
		
		if (bean.code == 1) {
			for (FindAdPeriodsperiodsBean pb : bean.periods) {
				mTimeDataList.add(new TimeData(null, TimeUtils.getTime(pb.beginTime), TimeUtils.getTime(pb.endTime), pb.loops, pb.max));
			}
			mListTime.setAdapter(new TimeListAdapter(this, mTimeDataList));
		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast(bean.msg);
		}
	}
	
	private void onAdPeriods(String text) {
		Log.v(TAG, "ad periods: " + text);
		if (text == null) {
			return;
		}

		BaseBean bean = BaseBeanParser.parseBaseBean(text);
		if (bean.code == 1) {
			Toast.makeText(this, "change ad periods sucess", Toast.LENGTH_SHORT).show();
			Intent data = new Intent();
			data.putExtra("isChange", true);
			setResult(RESULT_OK, data);
			finish();
		} else if (bean.code == -2) {
			returnToLogin();
		} else {
			reportToast(bean.msg);
		}
	}
	
	private static final int MSG_FIND_AD_PERIODS = 0;
	private static final int MSG_AD_PERIODS = 1;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
			{
				switch (msg.arg1) {
				case MSG_FIND_AD_PERIODS:
					onFindAdPeriods((String) msg.obj);
					break;
					
				case MSG_AD_PERIODS:
					onAdPeriods((String) msg.obj);
					break;
				}
			}
			break;
			}
		}
	};

}
