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

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.routon.edurelease.R;
import com.routon.smartcampus.face.FaceRecognizeMgr;

public class FragmentOptionLv extends Fragment {

	private ListView optionLv;
	private List<OptionContentBean> optionList = new ArrayList<OptionContentBean>();
	private OptionContentBean bean;

	View view;
	private ProgressDialog progressDialog;
	private GridViewAdapter stuImgGridAdapter;
	private TextView alreadyAnswer;
	private TextView nonAnswer;
	private TextView tvAnsTimeChart;
	private TextView tvPercentChart;
	private TextView tvOptionChart;

	// 1701
	private int viewPosition = 0;
	String itemAnswer = null;
	int itemCount = 0;
	int startAnswerBroadCount = 0;
	int startPreemptBroadCount = 0;
	int startDecisionBroadCount = 0;
	int answerButType = 0;
	int decisionButType = 0;
	private List<String> optionSidList;
	private AnswerOptionAdapter optionLvAdapter;
	// 加载学生列表数据
	private ArrayList<StudentBean> studentList;
	private FaceRecognizeMgr mFaceRecongnizeMgr = null;
	private static final String TAG = "Fragment_optionLv";
	private int classId = 0;
	private int ansMode = 0;
	private int answerOrDecision = 0;
	private GridView stuGridView;
	private boolean isDoAnswer = false;
	private ArrayList<StudentBean> optionStudentList;
	// 传递答对学生信息
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private int questionId = 0;
	// 计时器

	private int timeTad;
	private Timer mTimer;
	private static final int UPDATETIME = 1;// 答题计时
	private String answerTotalTime;
	private String answwerTimeDown;
	int j;
	int min;
	int sec;
	int minTens;
	int minOnes;
	int secTens;
	int secOnes;
	private List<Integer> timerNumbers;
	private ImageView imgMinTens;
	private ImageView imgMinOnes;
	private ImageView imgSecTens;
	private ImageView imgSecOnes;
	// 加载图像数据
	private ArrayList<String> yAnsTimeLineChartList = new ArrayList<String>();// 时间柱状图y坐标集合
	private ArrayList<String> xAnsTimeLineChartList = new ArrayList<String>();// 时间柱状图x坐标集合
	private int xScale = 6;// 将x坐标分为6份
	private List<String> answerTimeList = new ArrayList<String>();
	private AnsTimeLineChartView ansTimeLineChartView;
	private LineChartView lineChartView;
	private PieChartView pieChartView;
	private ArrayList<String> answerList = new ArrayList<String>();
	private ArrayList<String> answerPercentList = new ArrayList<String>();
	private TextView tvPercentTitle;
	int ansTime;

	AnswerMainActivity answerMainActivity;

	private String answerTime;
	private String currDay = null;
	private String nonceTime = null;
	// 1701服务测试
	private static final String ACTION_ANSWER_START = "qa_start";
	private static final String ACTION_ANSWER_SIGNIN = "qa_report";
	private static final String ACTION_ANSWER_END = "qa_end";
	private static final String ACTION_BT_CMD = "bt_cmd";
	public static final String S1701_IS_BUSY = "S1701 is running another work,please wait!";
	public static final String S1701_BUSY_TIMEOUT = "S1701 is running another work,overtime!";
	public static final String S1701_IS_RUNNING = "S1701 is running now!";
	public static final String S1701_IS_BUSY_ANSWER = "S1701 is busy!";

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_answer_option_list, container, false);
		Bundle bundle = getArguments();// 从activity传过来的Bundle
		if (bundle != null) {
			classId = (bundle.getInt("classId"));
			ansMode = bundle.getInt("ansMode");
			answerOrDecision = ansMode;
			questionId = bundle.getInt("questionId");
			isDoAnswer = bundle.getBoolean("isDoAnswer");
			answerTime = bundle.getString("answerTime");
			currDay = bundle.getString("currDay");
			nonceTime = bundle.getString("nonceTime");
			studentList = bundle.getParcelableArrayList("studentList");

		}

		Log.d(TAG, "classId:" + classId);

		initView();
		initData();
		// 清除上一题记录
		ClearAnswerData();
		// 注册广播
		registerRefreshListener();
		// 开始计时
		startTimer();
		// 给1701发送开始答题广播
		if (studentList == null || studentList.size() == 0) {
			Toast.makeText(answerMainActivity, "获取学生信息失败！", Toast.LENGTH_SHORT).show();
			return view;
		}
		Intent intent = new Intent();
		intent.setAction(ACTION_BT_CMD);
		if (ansMode == 1) {
			intent.putExtra("data", "action:" + ACTION_ANSWER_START + ";data:qid=" + questionId + "&type=0");
		} else if (ansMode == 3) {
			intent.putExtra("data", "action:" + ACTION_ANSWER_START + ";data:qid=" + questionId + "&type=2");
		}
		answerMainActivity.sendBroadcast(intent);

		return view;

	}

	private void initView() {
		// TODO Auto-generated method stub
		optionLv = (ListView) view.findViewById(R.id.lv_option);
		optionLv.setOnItemClickListener(new optionLvOnItemClickListener());

		stuGridView = (GridView) view.findViewById(R.id.gv_stu);
		alreadyAnswer = (TextView) view.findViewById(R.id.already_answer);
		nonAnswer = (TextView) view.findViewById(R.id.non_answer);
		tvAnsTimeChart = (TextView) view.findViewById(R.id.time_chart);
		tvPercentChart = (TextView) view.findViewById(R.id.percent_chart);
		tvOptionChart = (TextView) view.findViewById(R.id.count_chart);

		imgMinTens = (ImageView) view.findViewById(R.id.img_min_tens);
		imgMinOnes = (ImageView) view.findViewById(R.id.img_min_ones);
		imgSecTens = (ImageView) view.findViewById(R.id.img_sec_tens);
		imgSecOnes = (ImageView) view.findViewById(R.id.img_sec_ones);
       
        tvAnsTimeChart.setClickable(false);
    	tvPercentChart.setClickable(false);
    	tvOptionChart.setClickable(false);
        
		
		if (ansMode == 3) {
			tvAnsTimeChart.setVisibility(View.INVISIBLE);
		} else if (ansMode == 1) {
			tvAnsTimeChart.setVisibility(View.VISIBLE);
		}

	}

	private void initData() {
		// TODO Auto-generated method stub
		timerNumbers = new ArrayList<Integer>();
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

	public class optionLvOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			if (isDoAnswer) {
				Toast.makeText(answerMainActivity, "答题中，请先结束答题！", Toast.LENGTH_SHORT).show();
				return;
			}

			optionStudentList = new ArrayList<StudentBean>();

			if (optionList.get(position).optionName != null && !optionList.get(position).optionName.equals("")) {
				for (int i = 0; i < studentList.size(); i++) {

					if (studentList.get(i).result != null && !studentList.get(i).result.equals("")) {
						if (studentList.get(i).result.equals(optionList.get(position).optionName)) {
							optionStudentList.add(studentList.get(i));
						}
					}
				}
			} else {
				for (int i = 0; i < studentList.size(); i++) {
					if (studentList.get(i).result == null || studentList.get(i).result.equals("")) {
						optionStudentList.add(studentList.get(i));
					}
				}
			}
			// 点击显示对应选项的人数
			showAnswerTrueStuFragment();
		}

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

	// 关闭计时器
	public void closeTimer() {
		if (mTimer != null) {
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
				if (min < 10 && sec < 10) {
					// answerTotalTime = ("0"+min + ":" +"0" +sec);
					minTens = 0;
					minOnes = min;
					secTens = 0;
					secOnes = sec;
					imgMinTens.setBackgroundResource(timerNumbers.get(0));
					imgSecTens.setBackgroundResource(timerNumbers.get(0));
					for (int i = 0; i < timerNumbers.size(); i++) {
						if (minOnes == i) {

							imgMinOnes.setBackgroundResource(timerNumbers.get(i));
						}
						if (secOnes == i) {
							imgSecOnes.setBackgroundResource(timerNumbers.get(i));

						}
					}

				} else if (min < 10 && sec >= 10) {
					minTens = 0;
					minOnes = min;
					secTens = sec / 10;
					secOnes = sec % 10;
					imgMinTens.setBackgroundResource(timerNumbers.get(0));
					for (int i = 0; i < timerNumbers.size(); i++) {
						if (minOnes == i) {

							imgMinOnes.setBackgroundResource(timerNumbers.get(i));
						}
						if (secOnes == i) {
							imgSecOnes.setBackgroundResource(timerNumbers.get(i));

						}
						if (secTens == i) {
							imgSecTens.setBackgroundResource(timerNumbers.get(i));
						}
					}

					// answerTotalTime = ("0"+min + ":" +sec);
				} else if (min >= 10 && sec < 10) {
					minTens = min / 10;
					minOnes = min % 10;
					secTens = 0;
					secOnes = sec;
					imgSecTens.setBackgroundResource(timerNumbers.get(0));
					for (int i = 0; i < timerNumbers.size(); i++) {
						if (minOnes == i) {

							imgMinOnes.setBackgroundResource(timerNumbers.get(i));
						}
						if (secOnes == i) {
							imgSecOnes.setBackgroundResource(timerNumbers.get(i));

						}
						if (secTens == i) {
							imgMinTens.setBackgroundResource(timerNumbers.get(i));
						}
					}
					// answerTotalTime = (min + ":" +"0" +sec);
				} else {
					minTens = min / 10;
					minOnes = min % 10;
					secTens = sec / 10;
					secOnes = sec % 10;
					for (int i = 0; i < timerNumbers.size(); i++) {
						if (minOnes == i) {

							imgMinOnes.setBackgroundResource(timerNumbers.get(i));
						}
						if (secOnes == i) {
							imgSecOnes.setBackgroundResource(timerNumbers.get(i));

						}
						if (secTens == i) {
							imgSecTens.setBackgroundResource(timerNumbers.get(i));
						}
						if (minTens == i) {
							imgMinTens.setBackgroundResource(timerNumbers.get(i));
						}
					}
					// answerTotalTime = (min + ":"+sec);
				}

				// answerCountTimeText.setText(answerTotalTime);
			}
		};
	};

	/**
	 * 打开答对学生列表
	 * */
	public void showAnswerTrueStuFragment() {
		// TODO Auto-generated method stub
		FragmentAnswerTrueStuImgView fragmentAnswerTrueStuImgView = new FragmentAnswerTrueStuImgView();
		Bundle bundle = new Bundle();
		bundle.putInt("classId", classId);
		bundle.putInt("answerOrDecision", answerOrDecision);
		bundle.putInt("questionId", questionId);
		bundle.putString("answerTime", answerTime);
		bundle.putString("currDay", currDay);
		bundle.putString("nonceTime", nonceTime);
		bundle.putParcelableArrayList("optionStudentList", optionStudentList);
		fragmentAnswerTrueStuImgView.setArguments(bundle);
		fragmentManager = answerMainActivity.getSupportFragmentManager();
		transaction = fragmentManager.beginTransaction();
		transaction.setCustomAnimations(R.anim.fragement_slide_right_to_left, 0);
		transaction.add(R.id.framlayout_optionLv, fragmentAnswerTrueStuImgView, "FragmentAnswerTrueStuImgView");
		transaction.addToBackStack(null);
		transaction.commit();
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
		private int qid = 0;

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
					if (dataMap.get("qid") != null) {
						qid = Integer.parseInt(dataMap.get("qid"));
					}

				} else {
					Log.d(TAG, "data : null");
					return;
				}
				if (ansMode == 1 && qid == questionId) {// 答题
					// 如果答题结束状态不再接受数据
					isContains = false;
					for (int i = 0; i < studentList.size(); i++) {
						if (studentList.get(i).mac.equals(dataMap.get("sid"))) {
							if (dataMap.get("answ") != null && dataMap.get("answ").equals(studentList.get(i).result)) {
								return;

							} else if (dataMap.get("answ") != null) {
								studentList.get(i).result = dataMap.get("answ");
								if (dataMap.get("tim") != null && !dataMap.get("tim").equals("")
										&& !dataMap.get("tim").equals("0")) {
									studentList.get(i).tim = dataMap.get("tim");
									answerTimeList.add(studentList.get(i).tim);

								}
							}

						}
					}

					startAnswerBroadCount++;
					Log.d(TAG, "收到答题广播数" + startAnswerBroadCount);

					if (optionList == null) {
						optionSidList = new ArrayList<String>();
						optionList = new ArrayList<OptionContentBean>();
						optionLvAdapter = new AnswerOptionAdapter(answerMainActivity, optionList);
					}

					if (optionList.size() == 0) {
						viewPosition = 0;
						isContains = false;
						if (dataMap.get("answ") != null) {
							optionList.add(new OptionContentBean(dataMap.get("answ"), 1, viewPosition, 1));
							optionLvAdapter = new AnswerOptionAdapter(answerMainActivity, optionList);
							optionLv.setAdapter(optionLvAdapter);
							optionLvAdapter.notifyDataSetChanged();
						} else {
							Log.d(TAG, "答案为空:" + dataMap.get("name") + ":" + dataMap.get("sid"));
							return;
						}

					} else {
						for (int i = 0; i < optionList.size(); i++) {

							if (optionList.get(i).optionName.equals(dataMap.get("answ"))) {
								isContains = true;
								optionList.get(i).setSelCount(optionList.get(i).selCount + 1);
								optionLvAdapter.notifyDataSetChanged();

							}
						}

						if (!isContains) {
							if (dataMap.get("answ") != null) {
								viewPosition = viewPosition + 1;
								optionList.add(new OptionContentBean(dataMap.get("answ"), 1, viewPosition, 1));
								optionLvAdapter.notifyDataSetChanged();
								Collections.sort(optionList, new sortAnswer());

								// isContains = false;
							} else {
								Log.d(TAG, dataMap.get("sid") + ":" + "答案为空");
							}

						}
					}
					int nonAnswerCount = studentList.size() - startAnswerBroadCount;
					alreadyAnswer.setText("已答:" + startAnswerBroadCount + "位同学");
					nonAnswer.setText("未答:" + nonAnswerCount + "位同学");
					if(startAnswerBroadCount==studentList.size()){
						ansMode=0;
						answerMainActivity.StopAnswer(1);
						ShowStopAnswerDetialData();
					}

				} else if (ansMode == 3 && qid == questionId) {// 决策

					isContains = false;

					for (int i = 0; i < studentList.size(); i++) {
						if (studentList.get(i).mac.equals(dataMap.get("sid"))) {
							if (dataMap.get("vote") != null && dataMap.get("vote").equals(studentList.get(i).result)) {
								return;
							} else if (dataMap.get("vote") != null) {
								studentList.get(i).result = dataMap.get("vote");
							}
						}
					}
					startDecisionBroadCount++;
					int nonAnswerCount = studentList.size() - startDecisionBroadCount;
					// answerCountOnTime.setText("已选:"+startDecisionBroadCount+" "+"未选:"+nonAnswerCount);
					Log.d(TAG, "收到决策广播数" + startDecisionBroadCount);

					if (optionList == null) {

						optionSidList = new ArrayList<String>();
						optionList = new ArrayList<OptionContentBean>();
						optionLvAdapter = new AnswerOptionAdapter(answerMainActivity, optionList);
					}

					if (optionList.size() == 0) {
						viewPosition = 0;
						isContains = false;
						if (dataMap.get("vote") != null) {

							optionList.add(new OptionContentBean(dataMap.get("vote"), 1, viewPosition, 3));

							optionLvAdapter = new AnswerOptionAdapter(answerMainActivity, optionList);
							optionLv.setAdapter(optionLvAdapter);
							optionLvAdapter.notifyDataSetChanged();
						}

						optionLvAdapter.notifyDataSetChanged();
					} else {
						for (int i = 0; i < optionList.size(); i++) {
							if (optionList.get(i).optionName.equals(dataMap.get("vote"))) {

								isContains = true;
								optionList.get(i).setSelCount(optionList.get(i).selCount + 1);
								// View view =
								// optionLv.getChildAt(optionList.get(i).viewPosition);
								optionLvAdapter.notifyDataSetChanged();
							}
						}

						if (!isContains) {
							if (dataMap.get("vote") != null) {
								viewPosition = viewPosition + 1;
								optionList.add(new OptionContentBean(dataMap.get("vote"), 1, viewPosition, 3));
								optionLvAdapter.notifyDataSetChanged();
								 Collections.sort(optionList,new
								 sortDecision());
								// isContains = false;
							}
						}
					}
					int nonDecisionCount = studentList.size() - startDecisionBroadCount;
					alreadyAnswer.setText("已答:" + startDecisionBroadCount + "位同学");
					nonAnswer.setText("未答:" + nonDecisionCount + "位同学");
					if(startDecisionBroadCount==studentList.size()){
						ansMode=0;
						answerMainActivity.StopAnswer(3);
						ShowStopAnswerDetialData();
					}

				}
			} else if (action.equals(S1701_IS_BUSY) || action.equals(S1701_IS_BUSY_ANSWER)) {
				i++;
				Log.d(TAG, "广播次数:" + i);
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

	int i = 0;

	// 对类对象进行排序
	class sortAnswer implements Comparator<OptionContentBean> {
		int sort;

		@Override
		public int compare(OptionContentBean ans1, OptionContentBean ans2) {
			// TODO Auto-generated method stub
			if (ans1.getOptionName() != null) {
				sort = ans1.getOptionName().compareTo(ans2.getOptionName());
			}
			return sort;

		}

	}
	// 对类对象进行排序
	 class sortDecision implements Comparator<OptionContentBean> {
			int sort;

			@Override
			public int compare(OptionContentBean ans1, OptionContentBean ans2) {
				// TODO Auto-generated method stub
				if (ans1.getOptionName() != null) {
					sort = ans1.getOptionName().compareTo(ans2.getOptionName());
				}
				return sort*(-1);

			}

		}

	public static Map<String, String> transStringToMap(String mapString) {
		Map map = new HashMap();
		java.util.StringTokenizer items;
		for (StringTokenizer entrys = new StringTokenizer(mapString, "&"); entrys.hasMoreTokens(); map.put(
				items.nextToken(), items.hasMoreTokens() ? ((Object) (items.nextToken())) : null))
			items = new StringTokenizer(entrys.nextToken(), "=");
		return map;
	}

	/**
	 * 发送结束答题广播
	 * */
	public void sendStopAnswerBroadCast() {

		Intent intent = new Intent();
		intent.setAction(ACTION_BT_CMD);
		intent.putExtra("data", "action:" + ACTION_ANSWER_END);
		answerMainActivity.sendBroadcast(intent);
	}

	/**
	 * 清除上一题数据
	 * */
	public void ClearAnswerData() {
		if (studentList != null) {
			alreadyAnswer.setText("已答:" + startAnswerBroadCount + "位同学");
			nonAnswer.setText("未答:" + (studentList.size() - startAnswerBroadCount) + "位同学");
			if (studentList.size() > 0) {

				for (int i = 0; i < studentList.size(); i++) {
					studentList.get(i).result = "";
				}
			}
		}
		if (optionList != null) {
			optionList.clear();
			if (optionLvAdapter != null) {
				optionLvAdapter.notifyDataSetChanged();
			}

		}
		if (answerList != null) {
			answerPercentList.clear();
			answerList.clear();
		}
		if (yAnsTimeLineChartList != null) {
			xAnsTimeLineChartList.clear();
			yAnsTimeLineChartList.clear();
		}
		if (answerTimeList != null) {
			answerTimeList.clear();
		}

	}

	/**
	 * 答题或者决策结束后显示其他数据以及图表
	 * */
	public void ShowStopAnswerDetialData() {
		ansTime = j;
		int notAnswerStudent = 0;
		startAnswerBroadCount = 0;
		startDecisionBroadCount = 0;
		isDoAnswer = false;
		if (studentList != null) {
			if (studentList.size() > 0) {
				for (int i = 0; i < studentList.size(); i++) {
					if (studentList.get(i).result == null || studentList.get(i).result.equals("")) {
						notAnswerStudent = notAnswerStudent + 1;
					}

				}
			}
		}

		int totalAnswer = 0;
		if (optionList.size() > 0) {
			for (int j = 0; j < optionList.size(); j++) {
				totalAnswer += optionList.get(j).selCount;
				Log.d(TAG, optionList.get(j).optionName + ":" + optionList.get(j).selCount);
			}
		}

		Log.d(TAG, "收到答案:" + totalAnswer + "条");
		if (optionLvAdapter == null) {
			optionLvAdapter = new AnswerOptionAdapter(answerMainActivity, optionList);
		}

		optionList.add(new OptionContentBean("", notAnswerStudent, optionList.size(), ansMode));
		optionLvAdapter.notifyDataSetChanged();
		// 计算时间曲线图x坐标的集合
		getXCurveList();
		// 计算时间曲线图y坐标的集合
		getYCurveList();
		// 获取柱状图数据
		getLineChartViewData();
		// 设置显示图像按钮可点
		tvAnsTimeChart.setClickable(true);
		tvOptionChart.setClickable(true);
		tvPercentChart.setClickable(true);
		tvAnsTimeChart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ShowChartPopupWindow(1);
			}
		});
		tvPercentChart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ShowChartPopupWindow(2);
			}
		});
		tvOptionChart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ShowChartPopupWindow(3);
			}

		});

		ansMode = 0;

	}

	/**
	 * 显示绘图popupwindow
	 * */
	public void ShowChartPopupWindow(int viewType) {

		View popupView = answerMainActivity.getLayoutInflater().inflate(R.layout.popupwindow_chart_detail, null);
		ansTimeLineChartView = (AnsTimeLineChartView) popupView.findViewById(R.id.ans_time_lineview);
		lineChartView = (LineChartView) popupView.findViewById(R.id.lineView);
		pieChartView = (PieChartView) popupView.findViewById(R.id.piechart);
		tvPercentTitle = (TextView) popupView.findViewById(R.id.percent_title);
		switch (viewType) {
		case 1:
//			ansTimeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList);
			ansTimeLineChartView.setVisibility(View.VISIBLE);
			lineChartView.setVisibility(View.GONE);
			pieChartView.setVisibility(View.GONE);
			tvPercentTitle.setVisibility(View.GONE);
			break;
		case 2:
			pieChartView.setInputData(optionList, -1);
			ansTimeLineChartView.setVisibility(View.GONE);
			lineChartView.setVisibility(View.GONE);
			pieChartView.setVisibility(View.VISIBLE);
			tvPercentTitle.setVisibility(View.VISIBLE);
			tvPercentTitle.setTextColor(Color.RED);
			tvPercentTitle.setTextSize(18);
			tvPercentTitle.setText("百分比图");
			break;
		case 3:
//			lineChartView.setLineViewData(answerList, answerPercentList);
			lineChartView.setVisibility(View.VISIBLE);
			ansTimeLineChartView.setVisibility(View.GONE);
			pieChartView.setVisibility(View.GONE);
			tvPercentTitle.setVisibility(View.GONE);
			break;
		default:
			break;
		}

		PopupWindow window = new PopupWindow(popupView, (int) (this.getResources().getDimension(R.dimen.course_detail_window_width)),
				(int) (this.getResources().getDimension(R.dimen.course_detail_window_height)), true);
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
		WindowManager.LayoutParams lp = answerMainActivity.getWindow().getAttributes();
		lp.alpha = 0.4f;
		answerMainActivity.getWindow().setAttributes(lp);
	}

	private void getLineChartViewData() {
		// TODO Auto-generated method stub
		for (int i = 0; i < optionList.size(); i++) {
			if (i != optionList.size() - 1) {
				answerList.add(optionList.get(i).optionName);
			} else {
				answerList.add("未选");
			}

			answerPercentList.add(String.valueOf(optionList.get(i).selCount));
		}

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
				if (Integer.parseInt(answerTimeList.get(i)) <= Integer.parseInt(xAnsTimeLineChartList.get(k)) * 1000) {
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
			if (ansTime > 5 && ansTime < 10) {
				for (int i = 0; i <= ansTime; i++) {
					xAnsTimeLineChartList.add(String.valueOf(i));
				}
			} else if (ansTime >= 10) {

				if ((ansTime % 5) < (ansTime / 5)) {
					for (int i = 0; i <= 6; i++) {
						xAnsTimeLineChartList.add(String.valueOf((ansTime / 5) * i));
					}
				} else {
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

	@Override
	@Deprecated
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.answerMainActivity = (AnswerMainActivity) activity;
	};

	/**
	 * 获取activity传递的值
	 * */
	public void getValueFromActivity(Bundle bundle) {
		// 从activity传过来的Bundle
		if (bundle != null) {
			classId = (bundle.getInt("classId"));
			ansMode = bundle.getInt("ansMode");
			answerOrDecision = ansMode;
			questionId = bundle.getInt("questionId");
			isDoAnswer = bundle.getBoolean("isDoAnswer");
			answerTime = bundle.getString("answerTime");
			currDay = bundle.getString("currDay");
			nonceTime = bundle.getString("nonceTime");
			studentList = bundle.getParcelableArrayList("studentList");
		}
		Intent intent = new Intent();
		intent.setAction(ACTION_BT_CMD);
		if (ansMode == 1) {
			tvAnsTimeChart.setVisibility(View.VISIBLE);
			intent.putExtra("data", "action:" + ACTION_ANSWER_START + ";data:qid=" + questionId + "&type=0");
		} else if (ansMode == 3) {
			tvAnsTimeChart.setVisibility(View.INVISIBLE);
			intent.putExtra("data", "action:" + ACTION_ANSWER_START + ";data:qid=" + questionId + "&type=2");
		}
		if(isDoAnswer){
			tvAnsTimeChart.setClickable(false);
    		tvPercentChart.setClickable(false);
    		tvOptionChart.setClickable(false);
		}
		answerMainActivity.sendBroadcast(intent);
	}
}
