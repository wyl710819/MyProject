package com.routon.smartcampus.answerrelease;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.network.SmartCampusUrlUtils;

public class FragmentPreemptStuImgView extends Fragment{

	private static final String TAG="Fragment_AnswerTrueStuImgView";
	private GridViewAdapter stuImgGridAdapter;
	private GridView stuGridView;
	private TextView alreadyPreempt;
	private TextView nonPreempt;
	private TextView tvAnsTimeChart;
	private TextView tvPreemptFlower;
	private AnswerMainActivity answerMainActivity;
	private List<String> popupMenuItemList = new ArrayList<String>();
	
	// 1701服务测试
	private static final String ACTION_ANSWER_START = "qa_start";
	private static final String ACTION_ANSWER_SIGNIN = "qa_report";
	private static final String ACTION_ANSWER_END = "qa_end";
	private static final String ACTION_BT_CMD = "bt_cmd";
	public static final String S1701_IS_BUSY = "S1701 is running another work,please wait!";
	public static final String S1701_BUSY_TIMEOUT = "S1701 is running another work,overtime!";
	public static final String S1701_IS_RUNNING = "S1701 is running now!";
	public static final String S1701_IS_BUSY_ANSWER = "S1701 is busy!";
	 
	//计时器
	private int timeTad;
	private Timer mTimer;
	private static final int UPDATETIME = 1;//答题计时
	private String answwerTimeDown;
	int j;
	int min;
	int sec;
	int minTens;
	int minOnes;
	int secTens;
	int secOnes;
	View view;
	private List<Integer> timerNumbers=new ArrayList<Integer>();
	private ImageView imgMinTens;
	private ImageView imgMinOnes;
	private ImageView imgSecTens;
	private ImageView imgSecOnes;
	
	//接收按钮传递数据
	private int ansMode = 0;
	private int questionId = 0;
	private ArrayList<StudentBean> studentList;
	private FaceRecognizeMgr mFaceRecongnizeMgr = null;
	private List<StudentBean> studentPreemList=new ArrayList<StudentBean>();
	private List<StudentBean> studentAnswerTimeList=new ArrayList<StudentBean>();
	private int startPreemptBroadCount=0;
	//加载图像数据
	private ArrayList<String> yAnsTimeLineChartList = new ArrayList<String>();// 时间柱状图y坐标集合
	private ArrayList<String> xAnsTimeLineChartList = new ArrayList<String>();// 时间柱状图x坐标集合
	private int xScale = 6;// 将x坐标分为6份
	private List<String> answerTimeList = new ArrayList<String>();
	private AnsTimeLineChartView ansTimeLineChartView;
	int ansTime;
	private ProgressDialog progressDialog;
	private List <Integer> studentIds=new ArrayList<Integer>();
	List<NameValuePair> params;
	private String badgeId;
	private String bonuspoint;
	private ArrayList<Badge> flowersList;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState)  
    {
		view=inflater.inflate(R.layout.fragment_answer_preempt_stulist, container,false);
		
		Bundle bundle=getArguments();
		if(bundle!=null){
			studentList=bundle.getParcelableArrayList("studentList");
			ansMode = bundle.getInt("ansMode");
			questionId = bundle.getInt("questionId");
//			Log.d(TAG,studentList.get(0).empName);
		}
		//注册广播
		registerRefreshListener();
		
		initView();
		initData();
		//清除上一题数据
		ClearPreemptData();
		//开始计时
		startTimer();
		// 给1701发送开始抢答广播
	    if(studentList==null||studentList.size()==0){
	    	Toast.makeText(answerMainActivity, "获取学生信息失败！", Toast.LENGTH_SHORT).show();
	    	return view;
	    }  
		Intent intent = new Intent();
		intent.setAction(ACTION_BT_CMD);
		intent.putExtra("data", "action:" + ACTION_ANSWER_START + ";data:qid=" + questionId + "&type=1");
		answerMainActivity.sendBroadcast(intent);
		return view;
		
		
    }
	
	private void initData() {
		// TODO Auto-generated method stub
		timerNumbers.add(R.drawable.timer_number0);
		timerNumbers.add(R.drawable.timer_number1);
		timerNumbers.add(R.drawable.timer_number2);
		timerNumbers.add(R.drawable.timer_number3);
		timerNumbers.add(R.drawable.timer_number4);
		timerNumbers.add(R.drawable.timer_number5);
		timerNumbers.add(R.drawable.timer_number6);
		timerNumbers.add(R.drawable.timer_number7);
		timerNumbers.add(R.drawable.timer_number8);
		timerNumbers.add(R.drawable.timer_number9);
	}

	private void initView() {
		// TODO Auto-generated method stub
		stuGridView=(GridView) view.findViewById(R.id.gv_preempt_stu);
		alreadyPreempt=(TextView) view.findViewById(R.id.already_preempt);
		nonPreempt=(TextView) view.findViewById(R.id.non_preempt);
		imgMinTens=(ImageView) view.findViewById(R.id.img_min_tens);
		imgMinOnes=(ImageView) view.findViewById(R.id.img_min_ones);
		imgSecTens=(ImageView) view.findViewById(R.id.img_sec_tens);
		imgSecOnes=(ImageView) view.findViewById(R.id.img_sec_ones);
		tvAnsTimeChart=(TextView) view.findViewById(R.id.time_chart);
		tvPreemptFlower=(TextView) view.findViewById(R.id.preempt_flower_btn);
		tvAnsTimeChart.setClickable(false);
		tvPreemptFlower.setClickable(false);
		stuGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				if(ansMode!=0){
					Toast.makeText(answerMainActivity, "正在抢答中...", Toast.LENGTH_SHORT).show();
					return ;
				}
				showPopupList(view,position);
//				Toast.makeText(answerMainActivity, "你点击了第"+position+"项", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * showPopList
	 * */
	private PopupList popupList;
	private int stuId;
	public void showPopupList(final View anchorView,final int contextPosition) {
		if(popupMenuItemList.size()==0){
			popupMenuItemList.add("授予小红花");
		}
		int[] location = new int[2];
		anchorView.getLocationOnScreen(location);
		final float x = location[0] + anchorView.getWidth() / 2-8;
		final float y = location[1] + anchorView.getHeight() / 3;
		if(studentIds!=null&&studentIds.size()>0){
			stuId=studentIds.get(contextPosition);
		}
		
		popupList = new PopupList(answerMainActivity); 
		popupList.setTextSize(sp2px((20)));
		View popupView=popupList.getIndicatorView();
		popupView.setFocusable(true);
		popupView.requestFocus();
		
		popupList.showPopupListWindow(anchorView, contextPosition,x,y, popupMenuItemList, new PopupList.PopupListListener() {
			 @Override
			 public void onPopupListClick(View contextView, int contextPosition, int position) { 
				 switch (position) {
				 case 0://授予小红花
					 if(studentPreemList==null||studentPreemList.size()==0){
							Toast.makeText(answerMainActivity, "无抢答学生", Toast.LENGTH_SHORT).show();
						}else{
//							AwardFlower(stuId);//颁发小红花
							getBadgeListData(stuId);
						}
					 break;
				 default:
					break;
				 }
			 }

			@Override
			public boolean showPopupList(View adapterView, View contextView,
					int contextPosition) {
				// TODO Auto-generated method stub
				return true;
			}
		});
	}
	
	public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }   
	public static int px2sp(Context context, float pxValue) {
		  final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		  return (int) (pxValue / fontScale + 0.5f);
		 }
	/**
	 * 注册广播
	 * */
	private void registerRefreshListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_ANSWER_SIGNIN);
		filter.addAction(ACTION_ANSWER_END);
		filter.addAction(S1701_IS_BUSY);
		filter.addAction(S1701_BUSY_TIMEOUT);
		filter.addAction(S1701_IS_RUNNING);
		filter.addAction(S1701_IS_BUSY_ANSWER);
		answerMainActivity.registerReceiver(mDataChangedListener, filter);
	}

	/**
	 * 接受广播数据
	 * */
	private BroadcastReceiver mDataChangedListener = new BroadcastReceiver() {

		private boolean isContains;

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			// 1701服务测试
			if (action.equals(ACTION_ANSWER_SIGNIN)) {
				Map<String, String> dataMap = null;
				String data = intent.getStringExtra("data");
				if (data != null) {
					Log.d(TAG, "data :" + data);
					dataMap = transStringToMap(data);
				} else {
					Log.d(TAG, "data : null");
					return;
				}
				if(ansMode==2){//抢答
						
					studentPreemList.clear();
					
						for (int i = 0; i < studentList.size(); i++) {
							if (studentList.get(i).mac.equals(dataMap.get("sid"))) {
								//studentList.get(i).result=dataMap.get("answ");
								if(dataMap.get("tim")!=null&&!dataMap.get("tim").equals("")&&!dataMap.get("tim").equals("0")&&dataMap.get("tim").equals(studentList.get(i).tim)){
									return;
								}
								else if(dataMap.get("tim")!=null&&!dataMap.get("tim").equals("")&&!dataMap.get("tim").equals("0")){    //isNumber(dataMap.get("tim"))
									studentList.get(i).tim=dataMap.get("tim");
							        //studentAnswerTime.get(i).tim=dataMap.get("tim");
									studentAnswerTimeList.add(studentList.get(i));
									answerTimeList.add(studentList.get(i).tim);
								}
								
								
							}
							
						}
						startPreemptBroadCount++;
						Log.d(TAG,"收到抢答广播数"+startPreemptBroadCount);
						Collections.sort(studentAnswerTimeList,new Comparator<StudentBean>() {

					            @Override
					            public int compare(StudentBean o1, StudentBean o2) {
					                // TODO Auto-generated method stub
					            	
					            		return Integer.parseInt(o1.tim)-Integer.parseInt(o2.tim);
					            	
					            }
					        });
						 if(studentAnswerTimeList.size()<6){
						    	for(int j=0;j<studentAnswerTimeList.size();j++){
						    		studentPreemList.add(studentAnswerTimeList.get(j));
							     }
						    }else{
						    	for(int j=0;j<6;j++){
						    		studentPreemList.add(studentAnswerTimeList.get(j));
							     }
						    }
							 
						     stuImgGridAdapter = new GridViewAdapter(answerMainActivity, studentPreemList);
						     
//						     stuImgGridAdapter.notifyDataSetChanged();
							 stuGridView.setAdapter(stuImgGridAdapter);
							 
							 int nonAnswerCount=studentList.size()-startPreemptBroadCount;
							 alreadyPreempt.setText("已答:"+startPreemptBroadCount+"位同学");
							 nonPreempt.setText("未答："+(studentList.size()-startPreemptBroadCount)+"位同学");
//							 answerCountOnTime.setText("已答:"+startPreemptBroadCount+" "+"未答:"+nonAnswerCount);
						if(studentPreemList.size()==6){
							ansMode=0;
//							AnswerMainActivity answerMainActivity=(AnswerMainActivity) getActivity();
							answerMainActivity.StopPreempt();
							ShowStopPreemptDetialData();
						}
				  }
			  }
			else if (action.equals(S1701_IS_BUSY) || action.equals(S1701_IS_BUSY_ANSWER)) {
				if (action.equals(S1701_IS_BUSY)) {
					Toast.makeText(answerMainActivity, "正在自动考勤中,请稍候...", Toast.LENGTH_SHORT).show();
				} else if (action.equals(S1701_IS_BUSY_ANSWER)) {
					Toast.makeText(answerMainActivity, "正在统计上一题数据,请稍候...", Toast.LENGTH_SHORT).show();
				}
				closeTimer();
				answerMainActivity.IsBusy();
			}
		 }
	};
	
	public static Map<String, String> transStringToMap(String mapString) {
		Map map = new HashMap();
		java.util.StringTokenizer items;
		for (StringTokenizer entrys = new StringTokenizer(mapString, "&"); entrys.hasMoreTokens(); map.put(
				items.nextToken(), items.hasMoreTokens() ? ((Object) (items.nextToken())) : null))
			items = new StringTokenizer(entrys.nextToken(), "=");
		return map;
	}
	// 开始计时
	public void startTimer() {
		timeTad = 0;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				timeTad++;

				Message msg = new Message();
				msg.what = UPDATETIME;
				msg.arg1 = timeTad;
				handler.sendMessage(msg);
			}
		}, 0, 1000);
	}
	
	//关闭计时器
	public void closeTimer(){
		if(mTimer!=null){
			mTimer.cancel();
			mTimer.purge();
		}
	}
	// 计时器
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATETIME:
				j = msg.arg1;
				min = j / 60;
				sec = j % 60;
				if(min<10&&sec<10){
//					answerTotalTime = ("0"+min + ":" +"0" +sec);
					minTens=0;
					minOnes=min;
					secTens=0;
					secOnes=sec;
					imgMinTens.setBackgroundResource(timerNumbers.get(0));
					imgSecTens.setBackgroundResource(timerNumbers.get(0));
					for(int i=0;i<timerNumbers.size();i++){
						if(minOnes==i){
							
							imgMinOnes.setBackgroundResource(timerNumbers.get(i));
						}if(secOnes==i){
							imgSecOnes.setBackgroundResource(timerNumbers.get(i));
							
						}
					}
					
				}else if(min<10&&sec>=10){
					minTens=0;
					minOnes=min;
					secTens=sec/10;
					secOnes=sec%10;
					imgMinTens.setBackgroundResource(timerNumbers.get(0));
					for(int i=0;i<timerNumbers.size();i++){
						if(minOnes==i){
							
							imgMinOnes.setBackgroundResource(timerNumbers.get(i));
						}if(secOnes==i){
							imgSecOnes.setBackgroundResource(timerNumbers.get(i));
							
						}if(secTens==i){
							imgSecTens.setBackgroundResource(timerNumbers.get(i));
						}
					}
					
//					answerTotalTime = ("0"+min + ":" +sec);
				}else if(min>=10&&sec<10){
					minTens=min/10;
					minOnes=min%10;
					secTens=0;
					secOnes=sec;
					imgSecTens.setBackgroundResource(timerNumbers.get(0));
					for(int i=0;i<timerNumbers.size();i++){
						if(minOnes==i){
							
							imgMinOnes.setBackgroundResource(timerNumbers.get(i));
						}if(secOnes==i){
							imgSecOnes.setBackgroundResource(timerNumbers.get(i));
							
						}if(secTens==i){
							imgMinTens.setBackgroundResource(timerNumbers.get(i));
						}
					}
//					answerTotalTime = (min + ":" +"0" +sec);
				}else {
					minTens=min/10;
					minOnes=min%10;
					secTens=sec/10;
					secOnes=sec%10;
					for(int i=0;i<timerNumbers.size();i++){
						if(minOnes==i){
							
							imgMinOnes.setBackgroundResource(timerNumbers.get(i));
						}if(secOnes==i){
							imgSecOnes.setBackgroundResource(timerNumbers.get(i));
							
						}if(secTens==i){
							imgSecTens.setBackgroundResource(timerNumbers.get(i));
						}if(minTens==i){
							imgMinTens.setBackgroundResource(timerNumbers.get(i));
						}
					}
//					answerTotalTime = (min + ":"+sec);
				}
				
			}
		};
	};
	
	
	/**
	 * 发送结束答题广播
	 * */
	public void sendStopPeeemptBroadCast(){
		
		Intent intent=new Intent();
		intent.setAction(ACTION_BT_CMD);
		intent.putExtra("data", "action:"+ACTION_ANSWER_END);
		answerMainActivity.sendBroadcast(intent);
	}
	/**
	 * 清除上一题数据
	 * */
	public void ClearPreemptData(){
		tvPreemptFlower.setClickable(false);
		tvAnsTimeChart.setClickable(false);
		if(studentList!=null){
			alreadyPreempt.setText("已答:"+startPreemptBroadCount+"位同学");
			nonPreempt.setText("未答:"+(studentList.size()-startPreemptBroadCount)+"位同学");
			if(studentList.size()>0){
				for (int i = 0; i < studentList.size(); i++) {
					studentList.get(i).tim="0";
				}
			}
			
		}if(studentPreemList!=null){
			studentPreemList.clear();
			if(stuImgGridAdapter!=null){
				stuImgGridAdapter.notifyDataSetChanged();
			}
		}if(studentAnswerTimeList!=null){
			studentAnswerTimeList.clear();
		}if(yAnsTimeLineChartList!=null){
			yAnsTimeLineChartList.clear();
			xAnsTimeLineChartList.clear();
		}if(answerTimeList!=null){
			answerTimeList.clear();
		}
		
	}
	/** 
	 * 抢答结束后显示其他数据以及图表
	 * */
	public void ShowStopPreemptDetialData(){
		startPreemptBroadCount=0;
		// 计算时间曲线图x坐标的集合
		ansTime=j;
	    getXCurveList();
	    tvPreemptFlower.setClickable(true);
		// 计算时间曲线图y坐标的集合
		getYCurveList();
		//设置显示图像按钮可点
		tvAnsTimeChart.setClickable(true);
		tvAnsTimeChart.setOnClickListener(new OnClickListener() {
					
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ShowChartPopupWindow();
				}
			});
		if(studentPreemList!=null&&studentPreemList.size()>0){
			for(int i=0;i<studentPreemList.size();i++){
				studentIds.add(studentPreemList.get(i).sid);
			}
		}
		tvPreemptFlower.setClickable(true);
		tvPreemptFlower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(studentPreemList==null||studentPreemList.size()==0){
					Toast.makeText(answerMainActivity, "无抢答学生", Toast.LENGTH_SHORT).show();
				}else{
//					AwardFlower();//颁发小红花
				}
				
			}
		});
		ansMode=0;
		/*
		if(studentAnswerTimeList!=null&&studentAnswerTimeList.size()!=0){
			//Collections.sort(studentAnswerTimeList,new sortAnswerTime()); 
			 Collections.sort(studentAnswerTimeList,new Comparator<StudentBean>() {

		            @Override
		            public int compare(StudentBean o1, StudentBean o2) {
		                // TODO Auto-generated method stub
		            	
		            		return Integer.parseInt(o1.tim)-Integer.parseInt(o2.tim);
		            	
		                
		            }
		        });
			 Log.d(TAG, "收到抢答:"+studentAnswerTimeList.size()+"条");
			// Log.d(TAG, "答题人信息:"+studentAnswerTimeList.get(0).name+" "+studentAnswerTimeList.get(0).mac+" 时间:"+studentAnswerTimeList.get(0).tim);
		    if(studentAnswerTimeList.size()<6){
		    	for(int j=0;j<studentAnswerTimeList.size();j++){
		    		studentPreemList.add(studentAnswerTimeList.get(j));
			     }
		    }else{
		    	for(int j=0;j<6;j++){
		    		studentPreemList.add(studentAnswerTimeList.get(j));
			     }
		    }
			 
			 gridPreemAdapter = new PreemptResAdapter(AnswerActivity.this, studentPreemList);
			 gridPreemAdapter.baseUrl=baseUrl;
			 preemGridView.setAdapter(gridPreemAdapter);
			 gridPreemAdapter.notifyDataSetChanged();
			 
			 preemptStudentRl.setVisibility(View.VISIBLE);
			 startPreemptBroadCount=0;
			 
			 startAnswerBut.setFocusable(true);
			 decisionBut.setFocusable(true);
				
				

		}
	*/}
	/**
	 * 显示答题分布图
	 * */
	public void ShowChartPopupWindow(){
		View popupView=answerMainActivity.getLayoutInflater().inflate(R.layout.popupwindow_chart_detail, null);
		
		ansTimeLineChartView=(AnsTimeLineChartView)popupView. findViewById(R.id.ans_time_lineview);		
				
//		nsTimeLineChartView.setAnsTimeLineViewData(
//						xAnsTimeLineChartList, yAnsTimeLineChartList);
		
		 PopupWindow window = new PopupWindow(popupView,990,
	        		(int)(this.getResources().getDimension(R.dimen.course_detail_window_height)),true);
	     window.setAnimationStyle(R.style.PopupAnimation);
	     window.setFocusable(true);
	     window.setOutsideTouchable(true);
	     window.update();
	     window.setBackgroundDrawable(new BitmapDrawable());
	     window.showAtLocation(view, Gravity.CENTER, 0, 0);
	     window.setOnDismissListener(new PopupWindow.OnDismissListener() {
	     @Override
	     public void onDismiss() {
	     WindowManager.LayoutParams lp = answerMainActivity.getWindow().getAttributes();
	     	lp.alpha = 1f;
	     	answerMainActivity.getWindow().setAttributes(lp);
	             
	            }
	          });
	        WindowManager.LayoutParams lp =answerMainActivity.getWindow().getAttributes();  
	        lp.alpha = 0.4f;  
	        answerMainActivity.getWindow().setAttributes(lp);
	}
	/**
	 * 获取答题时间x,y坐标集合
	 * */
	private void getYCurveList() {
		// TODO Auto-generated method stub
		int yArray[] = new int[xAnsTimeLineChartList.size() - 1];
		List<String> yListTemp = new ArrayList<String>();
		for (int i = 0; i < answerTimeList.size(); i++) {
			for (int k = 1; k < xAnsTimeLineChartList.size(); k++) {
				if (Integer.parseInt(answerTimeList.get(i)) <= Integer
						.parseInt(xAnsTimeLineChartList.get(k)) * 1000) {
					// yCurveList.add(i+1);
					yArray[k - 1]++;
					break;
				}
			}

		}
		yAnsTimeLineChartList.add("0");
		for (int j = 0; j < yArray.length; j++) {

			yAnsTimeLineChartList.add(String.valueOf(yArray[j]));
		}
		yListTemp.addAll(yAnsTimeLineChartList);
		Collections.sort(yListTemp);
		

	}

	private void getXCurveList() {
		// TODO Auto-generated method stub

		if (ansTime > 5) {
			if(ansTime>5&&ansTime<10){
				for (int i = 0; i <= ansTime; i++) {
					xAnsTimeLineChartList.add(String.valueOf(i));
				}
			}else if(ansTime>=10){
				
				    if((ansTime%5)<(ansTime/5)){
				    	for (int i = 0; i <= 6; i++) {
				    		xAnsTimeLineChartList.add(String.valueOf((ansTime / 5) * i));
				    	}
				    }else{
				    	for (int i = 0; i <= 7; i++) {
				    		xAnsTimeLineChartList.add(String.valueOf((ansTime / 5) * i));
				    	}

					}
				
			}
		
		} else {
			for (int k = 0; k <= 5; k++) {

				xAnsTimeLineChartList.add(String.valueOf(k));
			}

		}
	}
	public void getValueFromActivity(Bundle bundle){
		// 从activity传过来的Bundle
		if (bundle != null) {
			ansMode = bundle.getInt("ansMode");
			questionId = bundle.getInt("questionId");
			studentList=bundle.getParcelableArrayList("studentList");
		}
		Intent intent = new Intent();
		intent.setAction(ACTION_BT_CMD);
		intent.putExtra("data", "action:" + ACTION_ANSWER_START + ";data:qid=" + questionId + "&type=1");
		answerMainActivity.sendBroadcast(intent);
	}
	
	protected void AwardFlower(int stuId,String badgeId,String bonuspoint) {
		// TODO Auto-generated method stub
		showMyProgressDialog();
		String urlString;
		urlString = SmartCampusUrlUtils.getBadugeIssueURl();
		params=new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("studentIds", String.valueOf(stuId)));
		params.add(new BasicNameValuePair("badgeId", badgeId));
		params.add(new BasicNameValuePair("count", String.valueOf(1)));
		params.add(new BasicNameValuePair("title", "回答正确"));
		params.add(new BasicNameValuePair("remark", "课堂上认真听讲、积极思考，回答问题正确"));
		params.add(new BasicNameValuePair("bonuspoint", bonuspoint));
		
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
//						if (progressDialog != null && progressDialog.isShowing()) {
//							progressDialog.dismiss();
//						}
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
									Toast.makeText(answerMainActivity, "颁发成功", Toast.LENGTH_SHORT).show();
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(answerMainActivity);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(answerMainActivity, response.getString("msg"), Toast.LENGTH_LONG)
										.show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						Toast.makeText(answerMainActivity, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();
						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	private void getBadgeListData(final int stuId) {
		String urlString = SmartCampusUrlUtils.getBadgeListUrl();
//		showProgressDialog();
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
//						hideProgressDialog();
						try {
							if (response.getInt("code") == 0) {

								flowersList = new ArrayList<Badge>();
								JSONArray array = response.getJSONArray("datas");
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									Badge flower = new Badge(obj);
									
									flowersList.add(flower);
									
								}
								if(flowersList!=null){
									badgeId=String.valueOf(flowersList.get(0).id);
									bonuspoint=String.valueOf(flowersList.get(0).bonuspoint);
								}
								AwardFlower(stuId,badgeId,bonuspoint);
//								refreshFlowerList(flowersList);
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(getActivity());
								hideMyProgressDialog();
							} else {// 失败
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(answerMainActivity,response.getString("msg"), Toast.LENGTH_SHORT).show();
								hideMyProgressDialog();
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");
						if( InfoReleaseApplication.showNetWorkFailed(answerMainActivity) == true ){
							Toast.makeText(answerMainActivity, "获取数据失败", Toast.LENGTH_SHORT).show();
							hideMyProgressDialog();
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	public String listToString(List<Integer> list, char separator) {   
		StringBuilder sb = new StringBuilder();    
		for (int i = 0; i < list.size(); i++) { 
			
				sb.append(list.get(i)).append(separator);   
			
			
		}   
		return sb.toString().substring(0,sb.toString().length()-1);
	}
	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(answerMainActivity, "", "...loading...");
		}
	}
	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	@Override
	@Deprecated
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.answerMainActivity=(AnswerMainActivity) activity;
	};
	public int sp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                value, getResources().getDisplayMetrics());
    }
}
