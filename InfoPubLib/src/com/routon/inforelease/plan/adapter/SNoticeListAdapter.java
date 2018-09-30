package com.routon.inforelease.plan.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;

import com.routon.inforelease.R;
import com.routon.inforelease.json.SNoticeListrowsBean;
import com.routon.inforelease.snotice.SNoticeAddActivity;
import com.routon.inforelease.util.TimeUtils;

public class SNoticeListAdapter extends CheckedListAdapter<SNoticeListrowsBean> {
//	private List<String> mPublishIds = null;
	
	private Boolean isShowPublishFlag = true;
	
	public void setIsShowPublishFlag(Boolean isShowPublishFlag) {
		this.isShowPublishFlag = isShowPublishFlag;
	}
	
//	public void setPublishIds(List<String> ids){
//		mPublishIds = ids;
//	}

	public SNoticeListAdapter(Context context, List<SNoticeListrowsBean> datas) {
		super(context, datas);
	}
	
	public void setDatas(List<SNoticeListrowsBean> datas) {
		this.mMaterialList = datas;
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder(); 
			convertView = mInflater.inflate(R.layout.snotice_item, null);

			holder.detailTextView = (TextView)convertView.findViewById(R.id.detail_textview);
			holder.startTimeTextView = (TextView)convertView.findViewById(R.id.start_time_textview);
			holder.endTimeTextView = (TextView)convertView.findViewById(R.id.end_time_textview);
			holder.publishTipTextView = (TextView)convertView.findViewById(R.id.publish_tip_textview);
			
			convertView.setTag(holder);
			
		}else{
			holder = (ViewHolder) convertView.getTag();
		}

//		holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				SNoticeListrowsBean dataItem = (SNoticeListrowsBean) getItem(position);
//				
//				if(selectMaterails.contains(dataItem) && isChecked == false){
//					selectMaterails.remove(dataItem);
//				}
//				
//				if(isChecked && !selectMaterails.contains(dataItem)){
//					selectMaterails.add(dataItem);
//				}
//			}
//		});
		
//			holder.frameLayout.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					Intent intent=new Intent(mContext, SNoticeAddActivity.class);
//					SNoticeListrowsBean noticeListrowsbean=mMaterialList.get(position);
//					intent.putExtra("noticeId", noticeListrowsbean.id);
//					intent.putExtra("groupsId", noticeListrowsbean.groups);
//					intent.putExtra("noticeText", noticeListrowsbean.notice);
//					intent.putExtra("isEditable", mEditable);
//					mContext.startActivity(intent);
//					
//				}
//			});
		
		SNoticeListrowsBean dataItem = (SNoticeListrowsBean) getItem(position);
		
		holder.detailTextView.setText(dataItem.notice);
		String startTime = "";
		if( dataItem.startTime != null && dataItem.startTime.isEmpty() == false ){
			if( dataItem.startTime.length() > 3 ){
				startTime = dataItem.startTime.substring(0, dataItem.startTime.length()-3);
			}		
		}
		String endTime = "";
		if( dataItem.endTime != null && dataItem.endTime.isEmpty() == false ){
			if( dataItem.endTime.length() > 3 ){
				endTime = dataItem.endTime.substring(0, dataItem.endTime.length()-3);
			}		
		}
		holder.startTimeTextView.setText(convertView.getContext().getResources().getString(R.string.plan_begin_time)
				+ startTime);
		holder.endTimeTextView.setText(convertView.getContext().getResources().getString(R.string.plan_end_time)
				+ endTime);
		holder.publishTipTextView.setText("");
		
//		//该滚动字幕已经发布
//		if( mPublishIds != null && mPublishIds.contains(String.valueOf(dataItem.id))){
//			holder.publishTipTextView.setText("已发布");
//			holder.publishTipTextView.setTextColor(Color.rgb(54, 167, 57));
//			if( dataItem.endTime != null ){
////				Log.d("notice adapter","dataItem.endTime:"+dataItem.endTime);
//				Calendar calendar = Calendar.getInstance();
//				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm);
//				try {
//					calendar.setTime(sdf.parse(dataItem.endTime));
//					Calendar curCalendar = Calendar.getInstance();
//					if(  TimeUtils.isTimeBeforeTilMinute(calendar, curCalendar) ){//该滚动字幕已经过期
//						holder.publishTipTextView.setTextColor(Color.rgb(217, 174, 4));
//					}
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}
		
		if(dataItem.status == SNoticeListrowsBean.STATUS_AUDIT_THROUGH ||dataItem.status == SNoticeListrowsBean.STATUS_AUDIT_TOBE){
			if(isShowPublishFlag){
				holder.publishTipTextView.setText("已发布");
				holder.publishTipTextView.setTextColor(Color.rgb(54, 167, 57));
				holder.publishTipTextView.setVisibility(View.VISIBLE);
			}else{
				holder.publishTipTextView.setVisibility(View.GONE);
			}
			if( dataItem.endTime != null && dataItem.endTime.isEmpty() == false ){
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm);
				try {
					calendar.setTime(sdf.parse(dataItem.endTime));
					Calendar curCalendar = Calendar.getInstance();
					if( TimeUtils.isTimeBeforeTilMinute(calendar,curCalendar)){
						holder.publishTipTextView.setTextColor(Color.rgb(217, 174, 4));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
			}
					
		}else if(dataItem.status == SNoticeListrowsBean.STATUS_AUDIT_NOTTHROUGH){
			holder.publishTipTextView.setText("发布失败");
			if(isShowPublishFlag){
				holder.publishTipTextView.setVisibility(View.VISIBLE);
				holder.publishTipTextView.setTextColor(Color.rgb(201, 54, 0));
			}else{
				holder.publishTipTextView.setVisibility(View.GONE);
			}
		}else{
			holder.publishTipTextView.setVisibility(View.GONE);
		}
//		holder.checkBox.setChecked(selectMaterails.contains(dataItem));
		
//		if (!mEditable){
//			holder.checkBox.setVisibility(View.GONE);
//		}
		
		return convertView;
	}

	public class ViewHolder{
		TextView detailTextView;
		TextView startTimeTextView;
		TextView endTimeTextView;
		TextView publishTipTextView;
//		CheckBox checkBox;
		FrameLayout frameLayout;
	}	

}
