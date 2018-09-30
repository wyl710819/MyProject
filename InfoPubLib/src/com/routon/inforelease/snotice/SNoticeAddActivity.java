package com.routon.inforelease.snotice;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.ad.pkg.SNoticePkgTools;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.offline.OfflinePackageMgr;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.CommonBundleName;
import com.routon.inforelease.util.DataResponse;
import com.routon.inforelease.util.PublishStateUtils;
import com.routon.inforelease.util.TimeUtils;
import com.routon.inforelease.widget.AutoScrollTextView;
import com.routon.inforelease.widget.GroupSelActivity;
import com.routon.inforelease.widget.SettingItem;
import com.routon.json.BaseBean;
import com.routon.json.BaseBeanParser;

//滚动字母编辑和新增界面
public class SNoticeAddActivity extends CustomTitleActivity {
	private static final String TAG = "SNoticeAddActivity";
	private EditText info;
	private Context mContext;
	private String mStartByType;
	private String groupsId;
	private String noticeText;
	private int noticeId;
	private boolean mEditable=true;  
	private AutoScrollTextView mPreviewMsgTextView;
	private TextView mNumberTipTextView;
	private static final int MAX_LENGTH = 64;
	private boolean mAuditAuthority;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		mContext = this;
		
		setContentView(R.layout.activity_snotice_add);
		
		String startTime = null;
		String endTime = null;
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			groupsId = bundle.getString("groupsId");
			noticeText = bundle.getString("noticeText");
			noticeId = bundle.getInt("noticeId");
			mStartByType = bundle.getString("start_by_type");
			mEditable = bundle.getBoolean("isEditable");
			mAuditAuthority = bundle.getBoolean(CommonBundleName.AuditSchoolNoticeAuthority);
			//传递数据都按照yyyy-MM-dd HH:mm:ss，显示时如果不需要ss，则在显示时去掉
			startTime = bundle.getString("startTime");
			endTime = bundle.getString("endTime");
		}
		GroupListData.getClassListData(this, new DataResponse.Listener<ArrayList<GroupInfo>>() {

			@Override
			public void onResponse(ArrayList<GroupInfo> response) {
				// TODO Auto-generated method stub
				if( groupsId == null ){
					if( InfoReleaseApplication.authenobjData.headTeacherClasses != null 
							&& InfoReleaseApplication.authenobjData.headTeacherClasses.length == 1 ){
						//一个班级班主任，默认选中作为班主任的班级
						groupsId = InfoReleaseApplication.authenobjData.headTeacherClasses[0];
					}else if( response != null && response.size() == 1 ){
						groupsId = String.valueOf(response.get(0).getId());
					}
				}
			}
		}, null, null);
		
		((TextView)(findViewById(R.id.info_tip))).setText(Html.fromHtml(getResources().getString(R.string.scroll_text_must)));
		info = (EditText) findViewById(R.id.add_text_info);
		info.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LENGTH)});
		
		mNumberTipTextView = (TextView)findViewById(R.id.numbers_tip_textview);
		if( noticeText != null ){
			mNumberTipTextView.setText(noticeText.length()+"/"+MAX_LENGTH);
		}else{
			mNumberTipTextView.setText("0/"+MAX_LENGTH);
		}
		
		initTitleBar();
		
		SettingItem item =  (SettingItem) findViewById(R.id.groupitem);
		if( mAuditAuthority == false ){
			item.setMoreClicked(true);
		}
		item.setName(Html.fromHtml(getResources().getString(R.string.publish_group_must)));
		item.setInfo("0");
		item.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if( mAuditAuthority == false ){
					Intent intent = new Intent();
					intent.setClass(SNoticeAddActivity.this, GroupSelActivity.class);
					intent.putExtra("select_param", groupsId);
					intent.putExtra(CommonBundleName.GROUP_SEL_HEADTEACHERS, true);
					SNoticeAddActivity.this.startActivityForResult(intent, 0);
				}
			}
		});
		updateGroupTip();	
		
		mPreviewMsgTextView = (AutoScrollTextView) findViewById(R.id.previewMsgTextView);
		mPreviewMsgTextView.setVisibility(View.VISIBLE);
		mPreviewMsgTextView.init(getWindowManager());
		
		initEditText();
		
		initStartAndEndTime(startTime,endTime);
		
		resetPreviewMsg();
	}
	
	private int isDateAfter(DatePicker datepicker,TimePicker timepicker){
		//当前时间
		Calendar curTime = Calendar.getInstance();
		Calendar newTime = Calendar.getInstance();
		newTime.set(datepicker.getYear(), datepicker.getMonth(), datepicker.getDayOfMonth(), 
				timepicker.getCurrentHour(), timepicker.getCurrentMinute());
		return newTime.compareTo(curTime);
	}
	
	private void showDatePicker(final boolean startTime) {
		//不能设置小于当前时间的时间
		 Calendar curTime = Calendar.getInstance();
		 Calendar initTime = null;
		 if( startTime == true ){
			 initTime = mStartTime;
		 }else{
			 initTime = mEndTime;
		 }
		
		 Calendar showTime = curTime;
		 if( initTime != null ){
			 if( TimeUtils.isTimeBeforeTilMinute(curTime, initTime)  ){
				 showTime = initTime;
			 }
		 }
         
		 View view = View.inflate(this, R.layout.date_time_picker, null);  
         final DatePicker datePicker = (DatePicker)view.findViewById(R.id.new_act_date_picker);  
         final TimePicker timePicker = (TimePicker)view.findViewById(R.id.new_act_time_picker);  
         
         // Init DatePicker  
         datePicker.setCalendarViewShown(false);
         datePicker.setMinDate(curTime.getTimeInMillis());
         datePicker.init(showTime.get(Calendar.YEAR), showTime.get(Calendar.MONTH), 
        		 showTime.get(Calendar.DAY_OF_MONTH), null);
           
         // Init TimePicker   
         timePicker.setIs24HourView(true); 
         timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
				//不能小于当前时间
				if( isDateAfter(datePicker,timePicker) < 0 ){
					Calendar curTime = Calendar.getInstance();
					timePicker.setCurrentHour(curTime.get(Calendar.HOUR_OF_DAY));
					timePicker.setCurrentMinute(curTime.get(Calendar.MINUTE));
				}
				
			}
		 });
         timePicker.setCurrentHour(showTime.get(Calendar.HOUR_OF_DAY));
         timePicker.setCurrentMinute(showTime.get(Calendar.MINUTE));
           
         // Build DateTimeDialog  
         AlertDialog.Builder builder = new AlertDialog.Builder(SNoticeAddActivity.this);  
        
         builder.setView(view);  
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {  
             @Override  
             public void onClick(DialogInterface dialog, int which) {  
            	 //确认修改时间
 				 Calendar confirmTime = Calendar.getInstance();
 				 confirmTime.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
 						timePicker.getCurrentHour(),timePicker.getCurrentMinute()); 
 				 
 				 if( startTime == true ){
 					 //修改开始时间
 					 mStartTime = confirmTime;
 					 mStartTimeItem.setInfo(mShowSdf.format(mStartTime.getTime()));
 					//设置起始时间时，没有设置结束时间，默认结束时间为起始时间后一天
 					 if( mEndTime == null ){
	 					 Calendar endCalendar = Calendar.getInstance();
	 					 endCalendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
	 	 						timePicker.getCurrentHour(),timePicker.getCurrentMinute());
	 					 endCalendar.add(Calendar.DATE,1);
	 					//修改结束时间
	 					 mEndTime = endCalendar;
	 					 mEndTimeItem.setInfo(mShowSdf.format(mEndTime.getTime()));
 					 }
 				 }else{//结束时间设置
 					//设置结束时间时，如果没有设置起始时间，默认起始时间为当前时间
 					 if( mStartTime == null ){
 						 final Calendar startCalendar = Calendar.getInstance();
	 					//修改起始时间
	 					mStartTime = startCalendar;
	 					mStartTimeItem.setInfo(mShowSdf.format(mStartTime.getTime()));
 					 }
 					 if( TimeUtils.isTimeBeforeTilMinute(confirmTime, mStartTime) ){
 						 reportToast("结束时间不能早于开始时间");
 	 					 return;
 	 				 }	
 					 //修改结束时间
 					 mEndTime = confirmTime;
 					 mEndTimeItem.setInfo(mShowSdf.format(mEndTime.getTime()));
 				 }
             }  
         });  
         builder.setNegativeButton(android.R.string.cancel, null); 
         builder.show();  
	}
	
	private SettingItem mStartTimeItem = null;
	private SettingItem mEndTimeItem = null;
	private Calendar mStartTime = null;
	private Calendar mEndTime = null;
	
	private SimpleDateFormat mShowSdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyy_MM_dd_HH_mm);
	
	
	private void initStartAndEndTime(String startTime,String endTime){
		mStartTimeItem = ((SettingItem) findViewById(R.id.starttime_item));
		mStartTimeItem.setName(R.string.begin_time);
		mStartTimeItem.setMoreClicked(true);
		
		mStartTime = TimeUtils.getFormatCalendar(startTime, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		mEndTime = TimeUtils.getFormatCalendar(endTime, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
		
		if( mStartTime != null ){		
			mStartTimeItem.setInfo(mShowSdf.format(mStartTime.getTime()));
		}
		mStartTimeItem.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showDatePicker(true);
			}
		});
		mEndTimeItem = ((SettingItem) findViewById(R.id.endtime_item));
		mEndTimeItem.setName(R.string.end_time);
		mEndTimeItem.setMoreClicked(true);
		if( mEndTime != null ){		
			mEndTimeItem.setInfo(mShowSdf.format(mEndTime.getTime()));
		}
		mEndTimeItem.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showDatePicker(false);
			}
		});
		mEndTimeItem.hideBottomLine();
//		starttimeItem.setInfo(String.valueOf(groups.length));
	}
	
	private void initEditText(){
		info.addTextChangedListener(new TextWatcher(){  
  
            @Override  
            public void beforeTextChanged(CharSequence s, int start, int count,  
                    int after) {  
                Log.d(TAG, "beforeTextChanged:" + s + "-" + start + "-" + count + "-" + after);  
                  
            }  
  
            @Override  
            public void onTextChanged(CharSequence s, int start, int before,  
                    int count) {  
                Log.d(TAG, "onTextChanged:" + s + "-" + "-" + start + "-" + before + "-" + count);  
                noticeText = s.toString();
                if( noticeText.length() >= MAX_LENGTH ){
                	reportToast("最多输入"+MAX_LENGTH+"个字");
                }
                mNumberTipTextView.setText(noticeText.length()+"/"+MAX_LENGTH);
                //同步更新预览文字信息
                resetPreviewMsg();
            }

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				 Log.d(TAG, "afterTextChanged");  
			}  
              
        }); 
	}
	
	private void initTitleBar(){
        TextView editTitleView = (TextView) findViewById(R.id.add_text_title);
        if (noticeId!=0) {
        	if (mEditable) {
        		this.initTitleBar("滚动字幕编辑");
			}else {
				this.initTitleBar("滚动字幕");
				editTitleView.setText("滚动字幕");
				info.setEnabled(false);
			}
        	
		}else {
			this.initTitleBar("添加滚动字幕");
		}
        if (noticeText!=null) {
			info.setText(noticeText);
		}
		
		if (!mEditable && noticeId!=0) {
			
		}else{
			this.setTitleNextBtnClickListener("完成",new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(info.getText() != null){
						String textInfo = info.getText().toString().trim();
						if(textInfo.length() != 0){	
							if( false == checkTime()){
								return;
							}
							Log.i(TAG, "info:"+textInfo);
							if (mStartByType!=null&&mStartByType.equals("offline_plan")) {//离线
								// TODO Auto-generated method stub
								
								SNoticePkgTools.startMakeNewOfflineSNotice(textInfo, OfflinePackageMgr.getOfflinePkgDir(mContext, OfflinePackageMgr.TYPE_SNOTICE));
								Intent intent = new Intent(SNoticeListFragment.ACTION_SNOTICE_LIST_CHANGED);
								sendBroadcast(intent);
								finish();
								
							}else if (noticeId!=0) {//修改滚动字幕
								List<Integer> groupsIdList=new ArrayList<Integer>();
								if (groupsId!=null) {
									String[] groupsIds=groupsId.split(",");
									for (int i = 0; i < groupsIds.length; i++) {
										groupsIdList.add(Integer.parseInt(groupsIds[i]));
									}
								}
								String startStr = mStartTimeItem.getInfo();
								if( startStr != null && startStr.isEmpty() == false ){
									startStr = startStr+":00";
								}
								String endStr = mEndTimeItem.getInfo();
								if( endStr != null && endStr.isEmpty() == false ){
									endStr = endStr+":00";
								}
								editNotice(noticeId,info.getText().toString(),groupsIdList,startStr,
										endStr);
								
							}else{//添加滚动字幕							
								addPlanGroup();							
							}
							
							
							return;
						}
						
					}
					
					reportToast(R.string.no_text_error);
				}
			});
		}
	}
	
	private boolean checkTime(){
		//可以不设置起始时间和结束时间
		 if( mStartTime == null || mEndTime == null ){
				return true;
		 }
		 if( TimeUtils.isTimeBeforeTilMinute(mEndTime,mStartTime)){
			 reportToast("结束时间不能早于开始时间");
			 return false;
		 }
		 return true;
	}
	
	//更新预览文字信息
	private void resetPreviewMsg(){
		mPreviewMsgTextView.setText(noticeText);
		mPreviewMsgTextView.setTextSize(18);		
		mPreviewMsgTextView.setTextColor(android.graphics.Color.BLUE);
		mPreviewMsgTextView.init(getWindowManager());
		mPreviewMsgTextView.startScroll();
	}
	
	public void addPlanGroup(){
		List<Integer> groupsIdList=new ArrayList<Integer>();
		if (groupsId!=null) {
			String[] groupsIds=groupsId.split(",");
			for (int i = 0; i < groupsIds.length; i++) {
				groupsIdList.add(Integer.parseInt(groupsIds[i]));
			}
		}
		if(groupsIdList == null || groupsIdList.size() == 0){
			reportToast("未选择投放分组!");
			return;
		}
		
		String startStr = mStartTimeItem.getInfo();
		if( startStr != null && startStr.isEmpty() == false ){
			startStr = startStr+":00";
		}
		String endStr = mEndTimeItem.getInfo();
		if( endStr != null && endStr.isEmpty() == false ){
			endStr = endStr+":00";
		}
		sendText(info.getText().toString(),groupsIdList,startStr,endStr);		
	}
	
	private void sendPublishRequestWithoutAnswer(final String ids) {
		String urlString = UrlUtils.getSNoticePublishUrl(ids);
		Log.i(TAG, "URL:" + urlString);

		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.GET, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
					}
				});
		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void sendText(String notice, List<Integer> groupIds,String startTime,String endTime){
		Log.v(TAG, "snoticeadd: " + notice);
		Log.v(TAG, "snoticeadd: " + groupIds);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("notice", notice));// + "_mobile");
		
		for (int id : groupIds) {
			 params.add(new BasicNameValuePair("groupIds",  Integer.toString(id)));
		}
		//yyyy-MM-dd HH:mm:ss
		params.add(new BasicNameValuePair("startTime", startTime));
		params.add(new BasicNameValuePair("endTime", endTime));
		Log.v(TAG, "startTime: " + startTime);
	
        showProgressDialog();
  
        String urlString = UrlUtils.getSNoticeAddUrl();
        Log.i(TAG, "URL:"+urlString);
        
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.POST, urlString, params, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                        Log.d(TAG, "response="+response);  
                       hideProgressDialog();
                        try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if (bean.code == 1) {
								notifySNoticeListChanged();
								finish();
							} else if (bean.code == -2) {
								returnToLogin();
							} else {
								if( bean.msg != null && bean.msg.isEmpty() == false ){
									reportToast(bean.msg);
								}else{			
									reportToast("添加滚动字幕失败");
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {
                    	if( InfoReleaseApplication.showNetWorkFailed(mContext) == true ){
                    		reportToast("获取数据失败!");
                    	}
                    	hideProgressDialog(); 
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	private void updateGroupTip(){
		if( groupsId != null ){
			String[] groups = groupsId.split(",");
			((SettingItem) findViewById(R.id.groupitem)).setInfo(String.valueOf(groups.length));		
		}
	}

	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			//收到分组选择数据
			if (requestCode == 0) {
				if (resultCode == Activity.RESULT_OK) {
					groupsId = data.getStringExtra("groupids");
					updateGroupTip();
				}
			}
			super.onActivityResult(requestCode, resultCode, data);
	}

	private void editNotice(final int noticeId2, String editable, List<Integer> groupsId2,String startTime,String endTime) {
		// TODO Auto-generated method stub
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("notice", editable));
		for (int id : groupsId2) {
			 params.add(new BasicNameValuePair("groupIds",  Integer.toString(id)));
		}
		//时间格式yyyy-MM-dd HH:mm:ss
		params.add(new BasicNameValuePair("startTime", startTime));
		params.add(new BasicNameValuePair("endTime", endTime));
		
		params.add(new BasicNameValuePair("id",Integer.toString(noticeId2) ));
	
		showProgressDialog();
		  
        String urlString = UrlUtils.getSNoticeEditUrl();
        
        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
                Request.Method.POST, urlString, params, new Response.Listener<JSONObject>() {  
                    @Override  
                    public void onResponse(JSONObject response) {  
                    	hideProgressDialog();
                        try {
							BaseBean bean = BaseBeanParser.parseBaseBean(response);
							if (bean.code == 1) {
								
								//
								if( mAuditAuthority == true ){
									sendPublishRequestWithoutAnswer(String.valueOf(noticeId));
								}
								//滚动字幕修改，删除发布记录
								PublishStateUtils.removeData(SNoticeAddActivity.this, PublishStateUtils.NOTICE_SAVE_IDS_FILE, String.valueOf(noticeId2));
								notifySNoticeListChanged();
								finish();
							} else if (bean.code == -2) {
								returnToLogin();
							} else {
								if( bean.msg != null && bean.msg.isEmpty() == false ){
									reportToast(bean.msg);
								}else{
									reportToast("修改滚动字幕失败");
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        
                    }  
                },   
                new Response.ErrorListener() {  
                    @Override  
                    public void onErrorResponse(VolleyError arg0) {
                    	if( InfoReleaseApplication.showNetWorkFailed(mContext) == true ){
                    		reportToast("获取数据失败!");
                    	}
                    	hideProgressDialog();
                    }  
                });
        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
        InfoReleaseApplication.requestQueue.add(jsonObjectRequest);  
	}
	
	private void notifySNoticeListChanged() {
		Intent intent = new Intent(SNoticeListFragment.ACTION_SNOTICE_LIST_CHANGED);
		sendBroadcast(intent);
	}

	private String encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }
	
}
