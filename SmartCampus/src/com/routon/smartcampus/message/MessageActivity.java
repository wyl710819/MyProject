package com.routon.smartcampus.message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.util.TimeUtils;

public class MessageActivity extends CustomTitleActivity{
	private MessageAdapter mAdapter = null;
	private ListView mListView = null;
	private static final String TAG = "MessageActivity";
	private SharedPreferences mPref = null;
	private ArrayList<MessageData> mMessageData = null;
	public static final String MESSAGE_DATA_TAG = "messagedata";
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_message);
		this.initTitleBar(R.string.menu_message_text);
		
		mMessageData = this.getIntent().getParcelableArrayListExtra(MESSAGE_DATA_TAG);
		
		mPref = this.getSharedPreferences(MessageDataHelper.INFO, Context.MODE_PRIVATE);
		
		mListView = (ListView) this.findViewById(R.id.listview);
		
		mAdapter = new MessageAdapter(this,mMessageData);
		mListView.setAdapter(mAdapter);
		
		//设置界面获取通知失败，重新获取数据
		if( mMessageData == null ){
			getPushMsgList();
		}
	}
	
	 @Override
	 protected void onPause() {
		super.onPause();
		Date date = new Date();
		
		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		Log.d("MessageData","onPause sdf.format(date)):"+sdf.format(date)+",MessageDataHelper.getUpdateTimeStr():"+MessageDataHelper.getUpdateTimeStr());
		mPref.edit().putString(MessageDataHelper.getUpdateTimeStr(), sdf.format(date)).commit();
	}
	
	private void getPushMsgList(){
		this.showProgressDialog();
		MessageDataHelper.getPushMsgListData(this, new DataResponse.Listener<ArrayList<MessageData>>() {

			@Override
			public void onResponse(ArrayList<MessageData> response) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				mMessageData = response;
			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				hideProgressDialog();
				reportToast("获取通知列表失败");
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				hideProgressDialog();
			}
		});
	}
}
