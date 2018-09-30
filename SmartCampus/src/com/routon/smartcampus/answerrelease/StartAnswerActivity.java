package com.routon.smartcampus.answerrelease;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.smartcampus.answerrelease.AnswerContentAdapter.AwardFlowerClickListener;
import com.routon.smartcampus.answerrelease.AnswerContentAdapter.OPtionOnClickListener;
import com.routon.smartcampus.answerrelease.AnswerLineviewContentAdapter.LineViewPtionOnClickListener;
import com.routon.smartcampus.answerrelease.LineChartView.OnChartClickListener;
import com.routon.smartcampus.answerrelease.service.BluetoothService;
import com.routon.smartcampus.answerrelease.service.Broadcast;
import com.routon.smartcampus.attendance.MyGridView;
import com.routon.smartcampus.attendance.StudentLine;
import com.routon.smartcampus.coursetable.CourseTableHelper;
import com.routon.smartcampus.coursetable.TeacherCourseBean;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.view.HorizontalListView;
import com.routon.widgets.Toast;

public class StartAnswerActivity extends BaseFragmentActivity implements
		OnClickListener {
	private ArrayList<StudentBean> studentList = new ArrayList<StudentBean>();
	private LinearLayout selectLinear;
	private LinearLayout judgeLinear;
	private LinearLayout selectContentLinear;
	private LinearLayout judgeContentLinear;
	private LinearLayout lineviewLinear;
	private LinearLayout piechartviewLinear;
	private ImageView imgSelect;
	private ImageView imgJudge;
	private TextView tvSelect;
	private TextView tvJudge;
	private ImageView imgStartAnswer;
	private TextView tvAnswerTime;
	private int answerType = 0;// 0表示答题 1表示判断
	private boolean isAnswering = false;
	private int timeTad;
	private Timer mTimer;
	private static final int UPDATETIME = 1;// 答题计时
	private static final int ANSWERDELAY = 2;// 结束答题之后计时3秒后才能再答题
	private int timeDelay = 0;
	private Timer mTimerDelay;
	private int time = 0;
	private int min;
	private int sec;
	private List<OptionContentBean> optionList;
	private List<OptionContentBean> optionTempList;
	private ArrayList<String> answerXList;
	private ArrayList<String> answerTimeLineviewXList;//固定1到10
	private AnswerContentAdapter answerAdapter;
	private AnswerLineviewContentAdapter answerLineviewAdapter;
	private AnswerTimeLineviewAdapter answerTimeLineviewAdapter;
	private HorizontalListView answerListView;
	private HorizontalListView answerLineviewListView;
	private HorizontalListView answerTimeLineviewListView;
	private HorizontalListView answerTimeJudgeLineviewListView;
	private int mPosition = -1;// 记录点击答案的位置
	private ImageView imgJudgeTrue;
	private ImageView imgJudgeFalse;
	private View judgeTrueBg;
	private View judgeFalseBg;
	private ArrayList<String> answerList = new ArrayList<String>();
	private ArrayList<Integer> answerPercentList = new ArrayList<Integer>();
	private LineChartView lineChartView;
	private AnsTimeLineChartView timeLineChartView;
	private MyPageAdapter myPageAdapter;
	private MyPageAdapter myJudgePageAdapter;
	private ViewPager selectLineViewPager;
	private ViewPager judgeLinewViewPager;
	private StudentLine studentLine;
	private StudentLine studentLine2;
	private List<Integer> indexXList = new ArrayList<Integer>();
	private List<Integer> judgeList = new ArrayList<Integer>();
//	private JudgePieChartView piechartView;
	private AnswerDegreeCircle piechartView;
	private ImageView judgeTure;
	private ImageView judgeFalse;
	private ImageView judgeNon;
	private GridViewAdapter selectStuAdapter;
	private GridViewAdapter judgeStuAdapter;
	private MyGridView selectStuGridView;
	private MyGridView judgeStuGridView;

	private int questionId = 0;
	private ImageView backMenu;
	private static final String TAG = "StartAnswerActivity";
	int startAnswerBroadCount = 0;
	int startJudgeBroadCount = 0;
	private int viewPosition = 0;
	private int nonAnswerStu = 0;
	private ArrayList<StudentBean> optionStudentList = new ArrayList<StudentBean>();
	private ArrayList<StudentBean> optionRightStudentList = new ArrayList<StudentBean>();
	private int nonJudgeStu = 0;
	private TextView tvJudgeTrueStu;
	private TextView tvJudgeFalseStu;
	private TextView tvNonJudgeStu;
	private ImageView imgCommitJudgeTrue;
	private ImageView imgCommitJudgeFalse;
	private ArrayList<StudentBean> judgeTrueList = new ArrayList<StudentBean>();
	private ArrayList<StudentBean> judgeFalseList = new ArrayList<StudentBean>();
	private ArrayList<StudentBean> nonJudgeList = new ArrayList<StudentBean>();
	private ArrayList<Badge> flowersList;// 小红花列表
	private String badgeId;
	private String bonuspoint;
	private ProgressDialog progressDialog;
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	private List<Integer> studentIdList = new ArrayList<Integer>();
	private ArrayList<View> viewContainer = new ArrayList<View>();// 自定义柱状图容器
	private View lineViewRoot;
	private View timeLineViewRoot;
	private View pieChartViewRoot;
	private ArrayList<View> judgeViewContatiner=new ArrayList<View>();
	// 加载图像数据
	private ArrayList<String> yAnsTimeLineChartList = new ArrayList<String>();// 时间柱状图y坐标集合
	private ArrayList<String> xAnsTimeLineChartList = new ArrayList<String>();// 时间柱状图x坐标集合
	private ArrayList<String> yRightAnsTimeLineChartList=new ArrayList<String>();//点击正确答案绘制响应柱状图
	private ArrayList<String> rightAnswerTimeList=new ArrayList<String>();
	private int ansTime;
	private List<String> answerTimeList = new ArrayList<String>();
	private LinearLayout pointGroup;
	private LinearLayout judgePointGroup;
	private TextView tvChartTitle;
	private int space;//时间分布图间隔
	private float avgAnswerTime;
	private int currentChart=0;//当前显示的图表，0表示显示的是答案分布图，1表示时间分布图
	private String rightAnswer="";
	//errcode处理
	private List<Integer> errorCodes;
	private List<String> errorMsgs;
	// 1701服务测试
	private static final String ACTION_ANSWER_START = "qa_start";
	private static final String ACTION_ANSWER_SIGNIN = "qa_report";
	private static final String ACTION_ANSWER_END = "qa_end";
	private static final String ACTION_ANSWER_FINISH = "qa_finish";
	private static final String ACTION_BT_CMD = "bt_cmd";
	public static final String S1701_IS_BUSY = "S1701 is running another work,please wait!";
	public static final String S1701_BUSY_TIMEOUT = "S1701 is running another work,overtime!";
	public static final String S1701_IS_RUNNING = "S1701 is running now!";
	public static final String S1701_IS_BUSY_ANSWER = "S1701 is busy!";
	public static final String ACTION_RECEIVE_S1701_TID = "terminal_id_ack";
	private BluetoothDevice mDevice;
	private String mStatus="";
	private boolean isConnectedOff=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 进入答题时不让手机自动休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_start_answer_main);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		studentList = bundle.getParcelableArrayList("studentList");
		classId = bundle.getInt("classId");
		teacherId = bundle.getInt("teacherId");
		mDevice = bundle.getParcelable("device");
		mStatus = bundle.getString("status");
		
		nonAnswerStu = studentList.size();
		nonJudgeStu = studentList.size();
		mCourseTableHelper = new CourseTableHelper(this);
		calendar=Calendar.getInstance();
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// 注册广播
		registerRefreshListener();
		initView();
		initData();

	}

	private void initView() {
		// TODO Auto-generated method stub
		lineViewRoot = LayoutInflater.from(this).inflate(
				R.layout.answer_select_line_view, null, false);
		timeLineViewRoot = LayoutInflater.from(this).inflate(
				R.layout.answer_select_time_line_view, null, false);
		pieChartViewRoot = LayoutInflater.from(this).inflate(
				R.layout.answer_judge_line_view, null, false);
		selectLineViewPager = (ViewPager) findViewById(R.id.select_viewpager);
		judgeLinewViewPager=(ViewPager) findViewById(R.id.judge_viewpager);
		pointGroup = (LinearLayout) findViewById(R.id.pointgroup);
		judgePointGroup=(LinearLayout) findViewById(R.id.judge_pointgroup);
		backMenu = (ImageView) findViewById(R.id.img_start_anwswer_back);
		selectLinear = (LinearLayout) findViewById(R.id.select_linear);
		judgeLinear = (LinearLayout) findViewById(R.id.judge_linear);
		selectContentLinear = (LinearLayout) findViewById(R.id.answer_select_content);
		judgeContentLinear = (LinearLayout) findViewById(R.id.answer_judge_content);
		imgSelect = (ImageView) findViewById(R.id.img_select);
		imgJudge = (ImageView) findViewById(R.id.img_judge);
		tvSelect = (TextView) findViewById(R.id.tv_select);
		tvJudge = (TextView) findViewById(R.id.tv_judge);
		tvAnswerTime = (TextView) findViewById(R.id.tv_answer_time);
		imgStartAnswer = (ImageView) findViewById(R.id.start_answer_btn);
		answerListView = (HorizontalListView) findViewById(R.id.select_content_listview);
		answerLineviewListView = (HorizontalListView) findViewById(R.id.lineview_option_listview);
		answerTimeLineviewListView=(HorizontalListView) findViewById(R.id.anstime_lineview_listview);
		answerTimeJudgeLineviewListView=(HorizontalListView) findViewById(R.id.anstime_judge_lineview_listview);
		imgJudgeTrue = (ImageView) findViewById(R.id.img_judge_true);
		imgJudgeFalse = (ImageView) findViewById(R.id.img_judge_false);
		judgeTrueBg = findViewById(R.id.judge_ture_bg);
		judgeFalseBg = findViewById(R.id.judge_false_bg);
		lineviewLinear = (LinearLayout) findViewById(R.id.lineviewLinear);
		lineChartView = (LineChartView) lineViewRoot
				.findViewById(R.id.lineView);
		timeLineChartView = (AnsTimeLineChartView) timeLineViewRoot
				.findViewById(R.id.time_lineView);
		studentLine = (StudentLine) findViewById(R.id.answer_student_line_index);
		studentLine2 = (StudentLine) findViewById(R.id.answer_student_line_index2);
//		piechartView = (JudgePieChartView)pieChartViewRoot.findViewById(R.id.piechartView);
		piechartView = (AnswerDegreeCircle)pieChartViewRoot.findViewById(R.id.piechartView);
		piechartviewLinear = (LinearLayout) findViewById(R.id.piechartviewLinear);
		judgeTure = (ImageView) findViewById(R.id.img_judge_true_circle);
		judgeFalse = (ImageView) findViewById(R.id.img_judge_false_circle);
		judgeNon = (ImageView) findViewById(R.id.img_non_judge);
		selectStuGridView = (MyGridView) findViewById(R.id.gv_select_answer_student);
		judgeStuGridView = (MyGridView) findViewById(R.id.gv_judge_answer_student);
		tvJudgeTrueStu = (TextView) findViewById(R.id.tv_judge_true_stu);
		tvJudgeFalseStu = (TextView) findViewById(R.id.tv_judge_false_stu);
		tvNonJudgeStu = (TextView) findViewById(R.id.tv_nonjudge_stu);
		imgCommitJudgeTrue = (ImageView) findViewById(R.id.img_commit_judge_true_btn);
		imgCommitJudgeFalse = (ImageView) findViewById(R.id.img_commit_judge_false_btn);
		tvChartTitle=(TextView) findViewById(R.id.chart_title);
		judgeChartTitle = (TextView) findViewById(R.id.judge_chart_title);
		selectLinear.setOnClickListener(this);
		judgeLinear.setOnClickListener(this);
		imgStartAnswer.setOnClickListener(this);
		imgJudgeTrue.setOnClickListener(this);
		imgJudgeFalse.setOnClickListener(this);
		judgeTure.setOnClickListener(this);
		judgeFalse.setOnClickListener(this);
		judgeNon.setOnClickListener(this);
		imgCommitJudgeTrue.setOnClickListener(this);
		imgCommitJudgeFalse.setOnClickListener(this);
		setMoveBackEnable(true);
		tvJudgeTrueStu.setText(0 + "人");
		tvJudgeFalseStu.setText(0 + "人");
		tvNonJudgeStu.setText(studentList.size() + "人");
		imgStartAnswer.setOnClickListener(new MyStartClickListener());
//		setTouchUnDealView(selectLineViewPager);
//		setTouchUnDealView(judgeLinewViewPager);
		setMoveBackEnable(false);
		backMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SaveQuestionId(questionId);
				Intent intent = new Intent();
				intent.setAction(ACTION_BT_CMD);
				intent.putExtra("data", "action:" + ACTION_ANSWER_END);
				sendBroadcast(intent);
				StartAnswerActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left,
						R.animator.slide_out_right);
			}
		});
		// 柱状图的点击事件
		lineChartView.setOnChartClickListener(new OnChartClickListener() {

			@Override
			public void onClick(int pos) {
				// TODO Auto-generated method stub
				// Toast.makeText(StartAnswerActivity.this, "你点击了第"+pos+"柱子",
				// Toast.LENGTH_LONG).show();
				updateClickView(pos, 1);
			}
		});

	}

	private void initData() {
		// TODO Auto-generated method stub
		// 获取保存本地的questionId
		questionId = GetQuestionId();
		if (questionId == 255) {
			questionId = 0;
		}
		errorCodes = new ArrayList<>();
		errorMsgs = new ArrayList<>();
		getErrorMsg();
		
//		selectStuGridView.setVisibility(View.VISIBLE);
//		optionRightStudentList.add(new StudentBean());
//		selectStuAdapter = new GridViewAdapter(
//				StartAnswerActivity.this, optionRightStudentList);
//		selectStuGridView.setAdapter(selectStuAdapter);
		
		judgeList.add(0);
		judgeList.add(0);
		judgeList.add(studentList.size());
//		piechartView.setPieChartData(judgeList);
		piechartView.setDegree(0);
		piechartView.setReverseDegree(0);
		answerList.add("A");// 柱状图的条目
		answerList.add("B");
		answerList.add("C");
		answerList.add("D");
		answerList.add("E");
		answerList.add("F");
		answerList.add("未选");

		answerPercentList.add(0);
		answerPercentList.add(0);
		answerPercentList.add(0);
		answerPercentList.add(0);
		answerPercentList.add(0);
		answerPercentList.add(0);
		answerPercentList.add(studentList.size());
		indexXList.add(216);
		indexXList.add(342);
		indexXList.add(466);
		indexXList.add(590);
		indexXList.add(724);
		indexXList.add(850);
		indexXList.add(972);
		lineChartView.setLineViewData(answerList, answerPercentList, -1);
		// 添加柱状图到viewPager里
		viewContainer.add(lineViewRoot);
		viewContainer.add(timeLineViewRoot);
		
		
		
		judgeViewContatiner.add(pieChartViewRoot);
		judgeViewContatiner.add(timeLineViewRoot);
		myPageAdapter = new MyPageAdapter(StartAnswerActivity.this,
				viewContainer);
		//判断题图像容器
		myJudgePageAdapter=new MyPageAdapter(StartAnswerActivity.this, judgeViewContatiner);
		selectLineViewPager.setAdapter(myPageAdapter);
		
		optionList = new ArrayList<OptionContentBean>();
		answerXList = new ArrayList<String>();// 柱状图下横坐标集合
		answerXList.add("A");
		answerXList.add("B");
		answerXList.add("C");
		answerXList.add("D");
		answerXList.add("E");
		answerXList.add("F"); 
		answerXList.add("未选");
		answerXList.add("-1");
		//答题时间分布图默认固定分十个间隔
		answerTimeLineviewXList=new ArrayList<String>();
		for(int i=0;i<=10;i++){
			answerTimeLineviewXList.add(String.valueOf(i));
		}
		answerTimeLineviewAdapter=new AnswerTimeLineviewAdapter(StartAnswerActivity.this, answerTimeLineviewXList);
		answerTimeLineviewListView.setAdapter(answerTimeLineviewAdapter);
		answerTimeJudgeLineviewListView.setAdapter(answerTimeLineviewAdapter);
		answerAdapter = new AnswerContentAdapter(StartAnswerActivity.this,
				answerXList);
		answerLineviewAdapter = new AnswerLineviewContentAdapter(
				StartAnswerActivity.this, answerXList);
		answerListView.setAdapter(answerAdapter);
		answerLineviewListView.setAdapter(answerLineviewAdapter);
		// 监听颁发小红花按钮点击事件
		answerAdapter.setAwardClickListener(new AwardFlowerClickListener() {

			@Override
			public void awardClick(View v, int position) {
				// TODO Auto-generated method stub
				if (!isAnswering) {
//					if(currentChart==0){
						getBadgeListData();
						rightAnswer=answerXList.get(position);
						
//					}
				} else {
					Toast.makeText(StartAnswerActivity.this, "正在答题中，请先结束答题!",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		answerAdapter.setClickListener(new OPtionOnClickListener() {

			@Override
			public void optionClick(View v, int position,
					ImageView answerTrueBtn, TextView optionView) {
				// TODO Auto-generated method stub
				
				if(currentChart==0){
					updateClickView(position, 0);
					getXCurveList();
        			getYCurveList();
        			
        			mPosition=position;
					yRightAnsTimeLineChartList.clear();
					rightAnswerTimeList.clear();
					
					answerXList.set(answerXList.size() - 1, String.valueOf(mPosition));
					answerAdapter.notifyDataSetChanged();
					rightAnswer=answerXList.get(position);
					for(int i=0;i<studentList.size();i++){
						if(rightAnswer.equals(studentList.get(i).result)){
							rightAnswerTimeList.add(studentList.get(i).tim);
						}
					}
					getRightAnsYCurveList();
					timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
        			
				}
				else if(currentChart==1){
					updateClickView(position, 0);
					mPosition=position;
					yRightAnsTimeLineChartList.clear();
					rightAnswerTimeList.clear();
					
					answerXList.set(answerXList.size() - 1, String.valueOf(mPosition));
					answerAdapter.notifyDataSetChanged();
					rightAnswer=answerXList.get(position);
					for(int i=0;i<studentList.size();i++){
						if(rightAnswer.equals(studentList.get(i).result)){
							rightAnswerTimeList.add(studentList.get(i).tim);
						}
					}
					getRightAnsYCurveList();
					timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
				}
			}
		});
		answerLineviewAdapter
				.setClickListener(new LineViewPtionOnClickListener() {

					@Override
					public void optionClick(View v, int position, View bgView,
							TextView optionView) {
						// TODO Auto-generated method stub
						updateClickView(position, 1);
					}
				});
		//添加指示器
		for(int i=0;i<viewContainer.size();i++){
			 // 制作底部小圆点
            ImageView pointImage = new ImageView(this);
            pointImage.setImageResource(R.drawable.shape_point_selector);

            // 设置小圆点的布局参数
            int PointSize = getResources().getDimensionPixelSize(R.dimen.point_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(PointSize, PointSize);

            if (i > 0) {
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.point_margin);
                pointImage.setSelected(false);
            } else {
                pointImage.setSelected(true);
            }
            pointImage.setLayoutParams(params);
            // 添加到容器里
            pointGroup.addView(pointImage);
//            judgePointGroup.addView(pointImage);
		}
		//滑动到时间分布图时显示该柱状图数据
		selectLineViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			 int lastPosition;
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				  // 修改position
                position = position % viewContainer.size();
                // 设置当前页面选中
                pointGroup.getChildAt(position).setSelected(true);
                 //设置前一页不选中
                pointGroup.getChildAt(lastPosition).setSelected(false);
                // 替换位置
                lastPosition = position;
                if(selectLineViewPager.getCurrentItem()==1){
                	tvChartTitle.setText("时间分布图");
                	currentChart=1;
                	xAnsTimeLineChartList.clear();
                	yAnsTimeLineChartList.clear();
                	studentLine.setVisibility(View.GONE);
                	selectStuGridView.setVisibility(View.VISIBLE);
                	answerLineviewListView.setVisibility(View.GONE);
                	answerTimeLineviewListView.setVisibility(View.VISIBLE);
        			getXCurveList();
        			getYCurveList();
        			timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
        		}else{
        			tvChartTitle.setText("选项分布图");
        			currentChart=0;
        			studentLine.setVisibility(View.VISIBLE);
                	answerLineviewListView.setVisibility(View.VISIBLE);
                	answerTimeLineviewListView.setVisibility(View.GONE);
                	selectStuGridView.setVisibility(View.VISIBLE);
        		}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		judgeLinewViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			int lastPosition;
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				  // 修改position
              position = position % judgeViewContatiner.size();
              // 设置当前页面选中
//              judgePointGroup.getChildAt(position).setSelected(true);
//               //设置前一页不选中
//              judgePointGroup.getChildAt(lastPosition).setSelected(false);
              // 替换位置
              lastPosition = position;
              if(judgeLinewViewPager.getCurrentItem()==1){
            	  judgeChartTitle.setText("时间分布图");
              	currentChart=1;
              	xAnsTimeLineChartList.clear();
              	yAnsTimeLineChartList.clear();
              	studentLine2.setVisibility(View.GONE);
              	judgeStuGridView.setVisibility(View.VISIBLE);
              	answerTimeJudgeLineviewListView.setVisibility(View.VISIBLE);
      			getXCurveList();
      			getYCurveList();
      			timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
      		}else{
      			judgeChartTitle.setText("选项分布图");
      			currentChart=0;
      			studentLine2.setVisibility(View.VISIBLE);
      			answerTimeJudgeLineviewListView.setVisibility(View.GONE);
              	judgeStuGridView.setVisibility(View.VISIBLE);
      		  }
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
//		getClassCourse(classId,getDate(),getDate());
		
	}

	// 点击答案选项更新视图
	protected void updateClickView(int position, int type) {
		// TODO Auto-generated method stub

		initAnswerList();
		if (type == 0) {// 0代表点击的是上面正确答案列表。1代表点击的是下面查看学生选项列表
			mPosition = position;
			studentIdList.clear();
			optionRightStudentList.clear();
			answerXList.set(answerXList.size() - 1, String.valueOf(mPosition));
			answerAdapter.notifyDataSetChanged();
			studentLine.setIndexX(indexXList.get(position));
			lineChartView.setLineViewData(answerList, answerPercentList,
					position);// 更新柱状图
			getRightAnswerList(mPosition, 0);
		} else if (type == 1) {
			optionStudentList.clear();
			mPosition = position;
			answerXList.set(answerXList.size() - 1, String.valueOf(mPosition));// 更新柱状图下标集合
			answerLineviewAdapter.notifyDataSetChanged();
			studentLine.setIndexX(indexXList.get(position));
			getRightAnswerList(mPosition, 1);
//			selectStuAdapter.notifyDataSetChanged();
		}

	}

	/**
	 * 获取不同选项学生列表
	 * */
	public void getRightAnswerList(int position, int type) {

		optionRightStudentList.clear();
		if (optionTempList.get(position).optionName != null
				&& !optionTempList.get(position).optionName.equals("")) {
			for (int i = 0; i < studentList.size(); i++) {

				if (studentList.get(i).result != null
						&& !studentList.get(i).result.equals("")) {
					if (studentList.get(i).result.equals(answerXList
							.get(position))) {
						if (type == 0) {
							optionRightStudentList.add(studentList.get(i));
						} else if (type == 1) {
							optionStudentList.add(studentList.get(i));
						}
					}
				}
			}
		} else {
			if (position < 6) {
				if (!optionTempList.get(position).isExistThisAnswer) {
					if (type == 0) {
						optionRightStudentList.clear();
					} else if (type == 1) {
						optionStudentList.clear();
					}
				}
			} else {
				
				for (int i = 0; i < studentList.size(); i++) {
					if (studentList.get(i).result == null
							|| studentList.get(i).result.equals("")) {
						if (type == 0) {
							optionRightStudentList.add(studentList.get(i));
						} else if (type == 1) {
							optionStudentList.add(studentList.get(i));
						}
					}
				}
			}

		}
//		if (optionStudentList.size() == 0) {
//			selectStuGridView.setVisibility(View.GONE);
//		} else if (optionStudentList.size() > 0) {
//			selectStuGridView.setVisibility(View.VISIBLE);
//		}
		if (optionRightStudentList.size() > 0 ) {
			for (int i = 0; i < optionRightStudentList.size(); i++) {
				studentIdList.add(optionRightStudentList.get(i).sid);
				
			}
		}
		if (type == 0) {
			if (optionRightStudentList.size()<=0) {
				optionRightStudentList.add(new StudentBean());
			}
			
			selectStuGridView.setVisibility(View.VISIBLE);
			selectStuAdapter = new GridViewAdapter(
					StartAnswerActivity.this, optionRightStudentList);
			selectStuGridView.setAdapter(selectStuAdapter);
		} else {
			selectStuGridView.setVisibility(View.VISIBLE);
			selectStuAdapter = new GridViewAdapter(StartAnswerActivity.this,
					optionStudentList);
			selectStuGridView.setAdapter(selectStuAdapter);
		}
	}

	// 初始化每个选项的数据，没有人选的项占空位
	public void initAnswerList() {
		optionTempList = new ArrayList<OptionContentBean>();
		for (int i = 0; i < 7; i++) {// ABCDEF固定6个选项,最后一个为未选择的
			OptionContentBean bean = new OptionContentBean();
			optionTempList.add(bean);
		}
		for (int i = 0; i < optionList.size(); i++) {
			getOptionList(optionList.get(i).optionName, optionList.get(i));
		}
	}

	// 为存在的答案列表占位置
	public void getOptionList(String answer, OptionContentBean bean) {
		switch (answer) {
		case "A":
			bean.isExistThisAnswer = true;
			optionTempList.set(0, bean);
			break;
		case "B":
			bean.isExistThisAnswer = true;
			optionTempList.set(1, bean);
			break;
		case "C":
			bean.isExistThisAnswer = true;
			optionTempList.set(2, bean);
			break;
		case "D":
			bean.isExistThisAnswer = true;
			optionTempList.set(3, bean);
			break;
		case "E":
			bean.isExistThisAnswer = true;
			optionTempList.set(4, bean);
			break;
		case "F":
			bean.isExistThisAnswer = true;
			optionTempList.set(5, bean);
			break;
		}
	}
	
	//获取当前时间
	private String getTime(){
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(d);
	}
	//获取当前日期
		private String getDate(){
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			return sdf.format(d);
		}
		
	//获取当前日期时分
	private String getHours(){
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			return sdf.format(d);
	}

	// 开始答题监听
	public class MyStartClickListener implements OnClickListener {


		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (questionId >= 255) {
				questionId = 0;
			}
			
			if (!isAnswering) {
				//获取当前时间
				
				answerTime = getTime();
				isUpLoadSuccess=false;
				ClearAnswerData();
				questionId++;
				Intent intent = new Intent();
				intent.setAction(ACTION_BT_CMD);
				if (answerType == 0) {
					intent.putExtra("data", "action:" + ACTION_ANSWER_START
							+ ";data:qid=" + questionId + "&type=0");
				} else if (answerType == 1) {
					intent.putExtra("data", "action:" + ACTION_ANSWER_START
							+ ";data:qid=" + questionId + "&type=2");
				}
				sendBroadcast(intent);
				startTimer();
				isAnswering = true;
				imgStartAnswer.setImageResource(R.drawable.stop_answer_btn);
				
//				piechartView.setDegree(180.0f);
//				piechartView.setReverseDegree(180.0f);
			} else {
				imgStartAnswer.setClickable(false);
				imgStartAnswer.setImageResource(R.drawable.start_answer_gray_btn);
				startTimerDelay();
				startJudgeBroadCount = 0;
				Intent intent = new Intent();
				intent.setAction(ACTION_BT_CMD);
				intent.putExtra("data", "action:" + ACTION_ANSWER_END);
				sendBroadcast(intent);
				closeTimer();
				nonJudgeStu = studentList.size();
				nonAnswerStu = studentList.size();
				isAnswering = false;
				for (int i = 0; i < studentList.size(); i++) {
					
					if (studentList.get(i).result != null) {
						if (studentList.get(i).result.equals("Y")) {
							judgeTrueList.add(studentList.get(i));
						} else if (studentList.get(i).result.equals("N")) {
							judgeFalseList.add(studentList.get(i));
						}
					} else {
						nonJudgeList.add(studentList.get(i));
					}
				}
			}
			
      			getXCurveList();
      			getYCurveList();
      			timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.select_linear:
			if (!isAnswering) {
				tvAnswerTime.setText("00 : 00");
				answerType = 0;
				selectLineViewPager.setAdapter(myPageAdapter);
				selectContentLinear.setVisibility(View.VISIBLE);
				selectStuGridView.setVisibility(View.VISIBLE);
				judgeContentLinear.setVisibility(View.GONE);
				lineviewLinear.setVisibility(View.VISIBLE);
				studentLine.setVisibility(View.VISIBLE);
				studentLine2.setVisibility(View.GONE);
				piechartviewLinear.setVisibility(View.GONE);
				judgeStuGridView.setVisibility(View.GONE);
				imgSelect.setImageResource(R.drawable.answer_select_pressed);
				tvSelect.setTextColor(Color.parseColor("#fcff00"));
				imgJudge.setImageResource(R.drawable.answer_judge_normal);
				tvJudge.setTextColor(Color.parseColor("#666666"));
				
				ClearAnswerData();
				answerTimeList.clear();
				getXCurveList();
      			getYCurveList();
      			timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
			} else {
				Toast.makeText(StartAnswerActivity.this, "正在答题中...",
						Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.judge_linear:
			if (!isAnswering) {
				
				
				answerType = 1;
				judgeLinewViewPager.setAdapter(myJudgePageAdapter);
				tvAnswerTime.setText("00 : 00");
				judgeTrueBg.setVisibility(View.INVISIBLE);
				judgeFalseBg.setVisibility(View.INVISIBLE);
				selectContentLinear.setVisibility(View.GONE);
				judgeContentLinear.setVisibility(View.VISIBLE);
				lineviewLinear.setVisibility(View.GONE);
				piechartviewLinear.setVisibility(View.VISIBLE);
				studentLine2.setVisibility(View.VISIBLE);
				studentLine.setVisibility(View.GONE);
				judgeStuGridView.setVisibility(View.VISIBLE);
				selectStuGridView.setVisibility(View.GONE);
				imgJudge.setImageResource(R.drawable.answer_judge_pressed);
				tvJudge.setTextColor(Color.parseColor("#fcff00"));
				imgSelect.setImageResource(R.drawable.answer_select_normal);
				tvSelect.setTextColor(Color.parseColor("#666666"));
				
				
				
				ClearAnswerData();
				answerTimeList.clear();
				getXCurveList();
      			getYCurveList();
      			timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
			} else {
				Toast.makeText(StartAnswerActivity.this, "正在答题中...",
						Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.img_judge_true:// 点击显示回答正确颁发小红花按钮
			
//			if(currentChart==1){
				
				judgeTrueBg.setVisibility(View.VISIBLE);
				judgeFalseBg.setVisibility(View.INVISIBLE);
				imgCommitJudgeTrue.setVisibility(View.VISIBLE);
				imgCommitJudgeFalse.setVisibility(View.INVISIBLE);
				imgJudgeTrue.setImageResource(R.drawable.img_judge_true);
				imgJudgeFalse.setImageResource(R.drawable.img_judge_false_gray);
				yRightAnsTimeLineChartList.clear();
				rightAnswerTimeList.clear();
				rightAnswer="Y";
				for(int i=0;i<studentList.size();i++){
					if(rightAnswer.equals(studentList.get(i).result)){
						rightAnswerTimeList.add(studentList.get(i).tim);
					}
				}
				getRightAnsYCurveList();
				timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
			/*}else if(currentChart==0){
				judgeTrueBg.setVisibility(View.VISIBLE);
				judgeFalseBg.setVisibility(View.INVISIBLE);
				imgCommitJudgeTrue.setVisibility(View.VISIBLE);
				imgCommitJudgeFalse.setVisibility(View.INVISIBLE);
				imgJudgeTrue.setImageResource(R.drawable.img_judge_true);
				imgJudgeFalse.setImageResource(R.drawable.img_judge_false_gray);*/
				studentLine2.setIndexX(170);
				judgeStuAdapter = new GridViewAdapter(StartAnswerActivity.this,
						judgeTrueList);
				judgeStuGridView.setAdapter(judgeStuAdapter);
//			}
			
			break;
		case R.id.img_judge_false:// 点击显示回答错误颁发小红花按钮
//			if(currentChart==1){
				
				judgeTrueBg.setVisibility(View.INVISIBLE);
				judgeFalseBg.setVisibility(View.VISIBLE);
				imgCommitJudgeTrue.setVisibility(View.INVISIBLE);
				imgCommitJudgeFalse.setVisibility(View.VISIBLE);
				imgJudgeTrue.setImageResource(R.drawable.img_judge_true_gray);
				imgJudgeFalse.setImageResource(R.drawable.img_judge_false);
				
				yRightAnsTimeLineChartList.clear();
				rightAnswerTimeList.clear();
				rightAnswer="N";
				for(int i=0;i<studentList.size();i++){
					if(rightAnswer.equals(studentList.get(i).result)){
						rightAnswerTimeList.add(studentList.get(i).tim);
					}
				}
				getRightAnsYCurveList();
				timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
			/*}else if(currentChart==0){
				judgeTrueBg.setVisibility(View.INVISIBLE);
				judgeFalseBg.setVisibility(View.VISIBLE);
				imgCommitJudgeTrue.setVisibility(View.INVISIBLE);
				imgCommitJudgeFalse.setVisibility(View.VISIBLE);
				imgJudgeTrue.setImageResource(R.drawable.img_judge_true_gray);
				imgJudgeFalse.setImageResource(R.drawable.img_judge_false);*/
				studentLine2.setIndexX(520);
				judgeStuAdapter = new GridViewAdapter(StartAnswerActivity.this,
						judgeFalseList);
				judgeStuGridView.setAdapter(judgeStuAdapter);
//			}
			
			break;
		case R.id.img_commit_judge_true_btn:// 点击颁发答案为正确选项的学生小红花
			if (!isAnswering) {
				studentIdList.clear();
				if (judgeTrueList.size() > 0) {
					for (int i = 0; i < judgeTrueList.size(); i++) {
						studentIdList.add(judgeTrueList.get(i).sid);
					}
				}
				getBadgeListData();
			} else {
				Toast.makeText(StartAnswerActivity.this, "正在答题中，请先结束答题!",
						Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.img_commit_judge_false_btn:// 点击颁发答案为错误选项的学生小红花
			if (!isAnswering) {
				studentIdList.clear();
				if (judgeFalseList.size() > 0) {
					for (int i = 0; i < judgeFalseList.size(); i++) {
						studentIdList.add(judgeFalseList.get(i).sid);
					}
				}
				getBadgeListData();
			} else {
				Toast.makeText(StartAnswerActivity.this, "正在答题中，请先结束答题!",
						Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.img_judge_true_circle:// 点击正确按钮查看选择正确答案的具体学生信息
			studentLine2.setIndexX(170);
			judgeStuAdapter = new GridViewAdapter(StartAnswerActivity.this,
					judgeTrueList);
			judgeStuGridView.setAdapter(judgeStuAdapter);
			break;
		case R.id.img_judge_false_circle:
			studentLine2.setIndexX(520);
			judgeStuAdapter = new GridViewAdapter(StartAnswerActivity.this,
					judgeFalseList);
			judgeStuGridView.setAdapter(judgeStuAdapter);
			break;
		case R.id.img_non_judge:
			studentLine2.setIndexX(860);
			judgeStuAdapter = new GridViewAdapter(StartAnswerActivity.this,
					nonJudgeList);
			judgeStuGridView.setAdapter(judgeStuAdapter);
			break;
		}
	}

	/**
	 * 清除上一题数据
	 * */
	public void ClearAnswerData() {
		if (studentList != null) {
			if (studentList.size() > 0) {

				for (int i = 0; i < studentList.size(); i++) {
					studentList.get(i).result = null;
					studentList.get(i).tim=null;
					studentList.get(i).answerTime="";
					studentList.get(i).answerStatus=-1;
				}
			}
		}
		if (optionList != null) {
			optionList.clear();
		}
		studentLine.setIndexX(0);// 初始化标记线
		studentLine2.setIndexX(0);
		if (answerPercentList != null) {
			answerPercentList.clear();
			answerPercentList.add(0);
			answerPercentList.add(0);
			answerPercentList.add(0);
			answerPercentList.add(0);
			answerPercentList.add(0);
			answerPercentList.add(0);
			answerPercentList.add(studentList.size());
			lineChartView.setLineViewData(answerList, answerPercentList, -1);
		}
		if (judgeList != null) {
			judgeList.clear();
			judgeList.add(0);
			judgeList.add(0);
			judgeList.add(studentList.size());
//			piechartView.setPieChartData(judgeList);
			piechartView.setDegree(0);
			piechartView.setReverseDegree(0);
		}
		optionStudentList.clear();
		optionRightStudentList.clear();
		if (selectStuAdapter != null) {
			optionRightStudentList.add(new StudentBean());
			selectStuAdapter = new GridViewAdapter(
					StartAnswerActivity.this, optionRightStudentList);
			selectStuGridView.setAdapter(selectStuAdapter);
		}
		judgeTrueList.clear();
		judgeFalseList.clear();
		nonJudgeList.clear();
		if (judgeStuAdapter != null) {
			judgeStuAdapter.notifyDataSetChanged();
		}
		if (studentIdList != null) {
			studentIdList.clear();
		}
		
		
		if(answerTimeList!=null){
			answerTimeList.clear();
		}
		if(yRightAnsTimeLineChartList!=null){
			yRightAnsTimeLineChartList.clear();
			rightAnswerTimeList.clear();
		}
		// 初始化答题未答人数
		tvJudgeTrueStu.setText(0 + "人");
		tvJudgeFalseStu.setText(0 + "人");
		tvNonJudgeStu.setText(studentList.size() + "人");
		// 选项列表状态
		mPosition = -1;
		answerXList.set(answerXList.size() - 1, String.valueOf(mPosition));// 更新柱状图下标集合
		answerAdapter.notifyDataSetChanged();
		answerLineviewAdapter.notifyDataSetChanged();
		// 隐藏判断题的颁发小红花按钮
		imgCommitJudgeTrue.setVisibility(View.INVISIBLE);
		imgCommitJudgeFalse.setVisibility(View.INVISIBLE);
	}

	/**
	 * 获取小红花列表
	 * */
	private void getBadgeListData() {
		String urlString = SmartCampusUrlUtils.getBadgeListUrl();
		// showProgressDialog();
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						// hideProgressDialog();
						try {
							if (response.getInt("code") == 0) {

								flowersList = new ArrayList<Badge>();
								JSONArray array = response
										.getJSONArray("datas");
								int len = array.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) array.get(i);
									Badge flower = new Badge(obj);

									flowersList.add(flower);

								}
								if (flowersList != null) {
									badgeId = String.valueOf(flowersList.get(0).id);
									bonuspoint = String.valueOf(flowersList
											.get(0).bonuspoint);
								}
								AwardFlower(badgeId, bonuspoint);
								// refreshFlowerList(flowersList);
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication
										.returnToLogin(StartAnswerActivity.this);
								hideMyProgressDialog();
							} else {// 失败
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(StartAnswerActivity.this,
										response.getString("msg"),
										Toast.LENGTH_SHORT).show();
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
						if (InfoReleaseApplication
								.showNetWorkFailed(StartAnswerActivity.this) == true) {
							Toast.makeText(StartAnswerActivity.this, "获取数据失败",
									Toast.LENGTH_SHORT).show();
							hideMyProgressDialog();
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance()
				.getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	/**
	 * 颁发回答正确小红花
	 * */
	protected void AwardFlower(String badgeId, String bonuspoint) {
		// TODO Auto-generated method stub
		if (studentIdList.size() == 0) {
			Toast.makeText(StartAnswerActivity.this, "无学生数据!",
					Toast.LENGTH_SHORT).show();
			hideMyProgressDialog();
			return;
		}
		String urlString;
		params.clear();
		urlString = SmartCampusUrlUtils.getBadugeIssueURl();
		params.add(new BasicNameValuePair("studentIds", listToString(
				studentIdList, ',')));
		params.add(new BasicNameValuePair("badgeId", badgeId));
		params.add(new BasicNameValuePair("count", String.valueOf(1)));
		params.add(new BasicNameValuePair("title", "回答正确"));
		params.add(new BasicNameValuePair("remark", "课堂上认真听讲、积极思考，回答问题正确"));
		params.add(new BasicNameValuePair("bonuspoint", bonuspoint));
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						// if (progressDialog != null &&
						// progressDialog.isShowing()) {
						// progressDialog.dismiss();
						// }
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								Toast.makeText(StartAnswerActivity.this,
										"颁发成功", Toast.LENGTH_SHORT).show();
								// isAwardFlowerSuccess=true;
//								CommitTrueAnswerStuInfo(getAnswerInfo(),getCourseName());
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication
										.returnToLogin(StartAnswerActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(StartAnswerActivity.this,
										response.getString("msg"),
										Toast.LENGTH_LONG).show();
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
						Toast.makeText(StartAnswerActivity.this, "网络连接失败!",
								Toast.LENGTH_LONG).show();
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance()
				.getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	//获取学生答题数据
	private String getAnswerInfo() {
		 ArrayList<StudentBean> answerStudentList = new ArrayList<StudentBean>();
		JSONArray jsonArray = new JSONArray();
		
		for (int i = 0; i < studentList.size(); i++) {
			if (studentList.get(i).result!=null && !studentList.get(i).result.equals("")) {
				StudentBean bean=new StudentBean();
				bean.sid=studentList.get(i).sid;
				bean.answerTime=studentList.get(i).answerTime;
				bean.answerStatus=0;
				answerStudentList.add(bean);
			}
		}
		for (int i = 0; i < answerStudentList.size(); i++) {
			if (studentIdList.contains(answerStudentList.get(i).sid)) {
				answerStudentList.get(i).answerStatus=1;
			}
		}
		
		for (int i = 0; i < answerStudentList.size(); i++) {
			JSONObject answerObj = new JSONObject();
			try {
				answerObj.put("sid", answerStudentList.get(i).sid);
				answerObj.put("status", answerStudentList.get(i).answerStatus);
				answerObj.put("answerTime", answerStudentList.get(i).answerTime);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			jsonArray.put(answerObj);
		}
		
		return jsonArray.toString();
	}

	//获取课程名称
		private String getCourseName(){
			String courseName=""; 
			for (int i = 0; i < courseBeans.size(); i++) {
				for (int j = 0; j < courseBeans.get(i).courseList.size(); j++) {
					String classTime=courseBeans.get(i).courseList.get(j).classTime;
					if (classTime!=null && classTime.contains("-")) {
						String[] times=classTime.split("-");
						if (isBelongTime(getHours(), times[0], times[1])) {
							courseName=courseBeans.get(i).courseList.get(j).course;
						}
					}
					
				}
			}
			
			return courseName;
			
		}

	private boolean isUpLoadSuccess=false;
	
	private List<TeacherCourseBean> courseBeans= new ArrayList<TeacherCourseBean>();
	//获取当天的课程表数据
	public void getClassCourse(int classGroupId, String startDate, String endDate) {
		String urlString = SmartCampusUrlUtils.getClassWeekAttenceUrl(String.valueOf(classGroupId), startDate, endDate);
		Log.d(TAG, "url="+urlString);
		showMyProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								courseBeans.clear();
								JSONObject datas = response.optJSONObject("datas");
								JSONArray jsonArray = datas.optJSONArray("atts");
								int len = jsonArray.length();
								for (int i = 0; i < len; i++) {
									JSONObject obj = (JSONObject) jsonArray.get(i);
									TeacherCourseBean bean = new TeacherCourseBean(obj);
									courseBeans.add(bean);
								}
							} else if (response.getInt("code") == -2) {
								InfoReleaseApplication.returnToLogin(StartAnswerActivity.this);
							} else {
								Log.e(TAG, response.getString("msg"));
								// showToast(response.getString("msg"));
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,Error");

						if (InfoReleaseApplication.showNetWorkFailed(StartAnswerActivity.this) == true) {
							// showToast("按日期查询作业失败!");
						}
						hideMyProgressDialog();

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	
	public static boolean isBelongTime(String nowTime, String beginTime,
			String endTime) {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		Date nowDate = null;
		Date beginDate = null;
		Date endDate = null;
		try {
			nowDate = df.parse(nowTime);
			beginDate = df.parse(beginTime);
			endDate = df.parse(endTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return nowDate.getTime() >= beginDate.getTime()
				&& nowDate.getTime() <= endDate.getTime();
	}
	
	private String answerTime="";
	/**
	 * 提交回答正确学生信息到平台
	 * */
	private void CommitTrueAnswerStuInfo(String studentAnswerInfo,String courseName) {
		if(courseName==null ||courseName.equals("")){
			Toast.makeText(StartAnswerActivity.this, "课间时间,获取课程信息失败,答题数据无法上报平台", Toast.LENGTH_LONG).show();
			return;
		}
		if(isUpLoadSuccess){
			Toast.makeText(StartAnswerActivity.this, "已上传过该数据，请勿重复!", Toast.LENGTH_LONG).show();
			return;
		}
		if(classId==0){
			Toast.makeText(StartAnswerActivity.this, "获取班级分组信息失败", Toast.LENGTH_LONG).show();
			return;
		}
		showMyProgressDialog();
		
		Log.d(TAG,"questionId="+questionId+"      courseName="+courseName+"      answerTime="+answerTime+"      rightAnswer="+rightAnswer+"      classId="+classId+"      studentAnswerInfo="+studentAnswerInfo);
		
		String urlString = "";
	    urlString = SmartCampusUrlUtils.getAnswereleaseUrl(questionId,
	    		courseName,rightAnswer,
				String.valueOf(classId),answerTime,studentAnswerInfo,teacherId);
	    Log.d(TAG,"CommitUrl="+urlString);
	    
	    
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideMyProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								Toast.makeText(StartAnswerActivity.this, "已将学生答题数据上报平台!", Toast.LENGTH_SHORT).show();
								isUpLoadSuccess=true;
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(StartAnswerActivity.this);
								Toast.makeText(StartAnswerActivity.this, "登录已失效!", Toast.LENGTH_SHORT).show();
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(StartAnswerActivity.this, response.getString("msg"), Toast.LENGTH_LONG)
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
						Toast.makeText(StartAnswerActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
						hideMyProgressDialog();
						
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	    
	}
	
	/**
	 * 拼接小红花
	 * */
	public String listToString(List<Integer> list, char separator) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {

			sb.append(list.get(i)).append(separator);

		}
		if (list.size() == 0) {
			return null;
		} else {
			return sb.toString().substring(0, sb.toString().length() - 1);
		}

	}

	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(StartAnswerActivity.this, "",
					"...loading...");
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
		filter.addAction(ACTION_ANSWER_FINISH);
		filter.addAction(S1701_IS_BUSY);
		filter.addAction(S1701_BUSY_TIMEOUT);
		filter.addAction(S1701_IS_RUNNING);
		filter.addAction(S1701_IS_BUSY_ANSWER);
//		filter.addAction(ACTION_RECEIVE_S1701_TID);
		registerReceiver(mDataChangedListener, filter);
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
				if (answerType == 0 && qid == questionId) {// 答题
					// 如果答题结束状态不再接受数据
					isContains = false;
					for (int i = 0; i < studentList.size(); i++) {
						if (studentList.get(i).mac != null) {
							if (studentList.get(i).mac.equals(dataMap
									.get("sid"))) {
								if (dataMap.get("answ") != null
										&& dataMap.get("answ").equals(
												studentList.get(i).result)) {
									return;

								} else if (dataMap.get("answ") != null) {
									studentList.get(i).result = dataMap
											.get("answ");
									studentList.get(i).answerTime=getTime();
									if (dataMap.get("tim") != null
											&& !dataMap.get("tim").equals("")
											&& !dataMap.get("tim").equals("0")) {
										studentList.get(i).tim = dataMap
												.get("tim");
										answerTimeList.add(studentList.get(i).tim);
									}
								}

							}
						}

					}

					startAnswerBroadCount++;
					Log.d(TAG, "收到答题广播数" + startAnswerBroadCount);

					if (optionList == null) {
						optionList = new ArrayList<OptionContentBean>();
					}

					if (optionList.size() == 0) {
						viewPosition = 0;
						isContains = false;
						if (dataMap.get("answ") != null) {
							optionList.add(new OptionContentBean(dataMap
									.get("answ"), 1, viewPosition, 1));
						} else {
							Log.d(TAG, "答案为空:" + dataMap.get("name") + ":"
									+ dataMap.get("sid"));
							return;
						}

					} else {
						for (int i = 0; i < optionList.size(); i++) {

							if (optionList.get(i).optionName.equals(dataMap
									.get("answ"))) {
								isContains = true;
								optionList.get(i).setSelCount(
										optionList.get(i).selCount + 1);

							}
						}

						if (!isContains) {
							if (dataMap.get("answ") != null) {
								viewPosition = viewPosition + 1;
								optionList.add(new OptionContentBean(dataMap
										.get("answ"), 1, viewPosition, 1));
								Collections.sort(optionList, new sortAnswer());

								// isContains = false;
							} else {
								Log.d(TAG, dataMap.get("sid") + ":" + "答案为空");
							}

						}

					}
					ShowLineView(dataMap.get("answ"));
					if (optionList.size() == studentList.size()) {// 收到所有答案自动停止答题
						imgStartAnswer.setClickable(false);
						imgStartAnswer.setImageResource(R.drawable.start_answer_gray_btn);
						startTimerDelay();
						Intent intentBoard = new Intent();
						intent.setAction(ACTION_BT_CMD);
						intent.putExtra("data", "action:" + ACTION_ANSWER_END);
						sendBroadcast(intentBoard);
						closeTimer();
						isAnswering = false;
						imgStartAnswer.setImageResource(R.drawable.start_answer_btn);
						
						getXCurveList();
		      			getYCurveList();
		      			timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);
					}

				} else if (answerType == 1 && qid == questionId) {// 判断
					isContains = false;
					for (int i = 0; i < studentList.size(); i++) {
						if (studentList.get(i).mac != null) {
							if (studentList.get(i).mac.equals(dataMap
									.get("sid"))) {
								if (dataMap.get("vote") != null
										&& dataMap.get("vote").equals(
												studentList.get(i).result)) {
									return;
								} else if (dataMap.get("vote") != null) {
									studentList.get(i).answerTime=getTime();
									studentList.get(i).result = dataMap
											.get("vote");
									studentList.get(i).tim = String.valueOf(ansTime*1000);
								}
								startJudgeBroadCount++;
								Log.d(TAG, "收到判断广播数" + startJudgeBroadCount);
							}
						}

					}
					answerTimeList.add(String.valueOf(ansTime*1000));
					

					if (optionList == null) {

						optionList = new ArrayList<OptionContentBean>();
					}

					if (optionList.size() == 0) {
						viewPosition = 0;
						isContains = false;
						if (dataMap.get("vote") != null) {
							optionList.add(new OptionContentBean(dataMap
									.get("vote"), 1, viewPosition, 3));

						}

					} else {
						for (int i = 0; i < optionList.size(); i++) {
							if (optionList.get(i).optionName.equals(dataMap
									.get("vote"))) {

								isContains = true;
								optionList.get(i).setSelCount(
										optionList.get(i).selCount + 1);
								// View view =
								// optionLv.getChildAt(optionList.get(i).viewPosition);
							}
						}

						if (!isContains) {
							if (dataMap.get("vote") != null) {
								viewPosition = viewPosition + 1;
								optionList.add(new OptionContentBean(dataMap
										.get("vote"), 1, viewPosition, 3));
								Collections
										.sort(optionList, new sortDecision());
								// isContains = false;
							}
						}
					}
					
					for (int i = 0; i < optionList.size(); i++) {
						if (optionList.get(i).optionName.equals("Y")) {
							tvJudgeTrueStu.setText(optionList.get(i).selCount
									+ "人");
						} else if (optionList.get(i).optionName.equals("N")) {
							tvJudgeFalseStu.setText(optionList.get(i).selCount
									+ "人");
						}
					}
					tvNonJudgeStu.setText(studentList.size()
							- startJudgeBroadCount + "人");
					ShowChartView(dataMap.get("vote"));
					if (startJudgeBroadCount == studentList.size()) {// 收到所有答案自动停止答题
						
						imgStartAnswer.setClickable(false);
						imgStartAnswer.setImageResource(R.drawable.start_answer_gray_btn);
						startTimerDelay();
						startJudgeBroadCount = 0;
						Intent intentBoard = new Intent();
						intent.setAction(ACTION_BT_CMD);
						intent.putExtra("data", "action:" + ACTION_ANSWER_END);
						sendBroadcast(intentBoard);
						closeTimer();
						nonJudgeStu = studentList.size();
						nonAnswerStu = studentList.size();
						isAnswering = false;
						imgStartAnswer
								.setImageResource(R.drawable.start_answer_btn);
						for (int i = 0; i < studentList.size(); i++) {
							if (studentList.get(i).result != null) {
								if (studentList.get(i).result.equals("Y")) {
									judgeTrueList.add(studentList.get(i));
								} else if (studentList.get(i).result
										.equals("N")) {
									judgeFalseList.add(studentList.get(i));
								}
							} else {
								nonJudgeList.add(studentList.get(i));
							}
						}
					}

				}
			} else if (action.equals(S1701_IS_BUSY)
					|| action.equals(ACTION_ANSWER_FINISH)) {
				i++;
				Log.d(TAG, "广播次数:" + i);
				String data = intent.getStringExtra("data");
				if (!TextUtils.isEmpty(data)) {
					String errorCode = data.substring(6);
					String errorMsg = getCheckErrorCode(Integer.valueOf(errorCode));
					if(!TextUtils.isEmpty(errorMsg)){
						Toast.makeText(StartAnswerActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
					}
				}
				closeTimer();
				isAnswering=false;
				imgStartAnswer.setImageResource(R.drawable.start_answer_btn);
				startJudgeBroadCount=0;
				
				getXCurveList();
      			getYCurveList();
      			timeLineChartView.setAnsTimeLineViewData(xAnsTimeLineChartList, yAnsTimeLineChartList,yRightAnsTimeLineChartList,avgAnswerTime);

				// answerMainActivity.IsBusy();
			}else if (action.equals(ACTION_RECEIVE_S1701_TID)) {
			}
		}

		
		private void ShowChartView(String vote) {
			// TODO Auto-generated method stub
			if (vote.equals("Y")) {
				loadChartViewData(0, vote);
			} else if (vote.equals("N")) {
				loadChartViewData(1, vote);
			}
		}

		private void loadChartViewData(int pos, String vote) {
			// TODO Auto-generated method stub
			int count = 0;
			if (optionList.size() > 0) {
				for (int i = 0; i < optionList.size(); i++) {
					if (optionList.get(i).optionName.equals(vote)) {
						count = optionList.get(i).selCount;
					}
				}
			}
			judgeList.set(pos, count);
			nonJudgeStu = nonJudgeStu - 1;
			judgeList.set(2, nonJudgeStu);// 未到人数减去1
//			piechartView.setPieChartData(judgeList);
			piechartView.setDegree(360.0f*(float)judgeList.get(0)/(float)studentList.size());
			piechartView.setReverseDegree(360.0f*(float)judgeList.get(1)/(float)studentList.size());
		}
	};
	//查询errorMsg
			private void getErrorMsg(){
				String urlString = SmartCampusUrlUtils.getErrorMsgUrl();
				JsonObjectRequest request = new JsonObjectRequest(urlString, null, 
						new Response.Listener<JSONObject>() {

							@Override
							public void onResponse(JSONObject response) {
								Log.d(TAG, "getErrorMsg response="+response);
								int code = response.optInt("code");
								String msg = response.optString("msg");
								if(code == 0){
									JSONArray datas = response.optJSONArray("datas");
									for(int i=0;i<datas.length();i++){
										JSONObject data = datas.optJSONObject(i);
										errorCodes.add(data.optInt("code"));
										errorMsgs.add(data.optString("msg"));
									}
								}else {
									Toast.makeText(StartAnswerActivity.this, msg, Toast.LENGTH_SHORT).show();
								}
							}
						}, 
						new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError error) {
								Toast.makeText(StartAnswerActivity.this, "查询error失败", Toast.LENGTH_SHORT).show();
							}
						});
				InfoReleaseApplication.requestQueue.add(request);
			}
			
			private String getCheckErrorCode(int errorCode){
				String errorMsg = "";
				for(int i=0;i<errorCodes.size();i++){
					if(errorCodes.get(i) == errorCode){
						errorMsg = errorMsgs.get(i);
						break;
					}
				}
				return errorMsg;
			}
	public void ShowLineView(String answer) {
		switch (answer) {
		case "A":
			loadLineViewData(0, answer);
			break;
		case "B":
			loadLineViewData(1, answer);
			break;
		case "C":
			loadLineViewData(2, answer);
			break;
		case "D":
			loadLineViewData(3, answer);
			break;
		case "E":
			loadLineViewData(4, answer);
			break;
		case "F":
			loadLineViewData(5, answer);
			break;
		}
	}

	public void loadLineViewData(int pos, String ans) {
		int count = 0;
		if (optionList.size() > 0) {
			for (int i = 0; i < optionList.size(); i++) {
				if (optionList.get(i).optionName.equals(ans)) {
					count = optionList.get(i).selCount;
				}
			}
		}
		answerPercentList.set(pos, count);
		nonAnswerStu = nonAnswerStu - 1;
		answerPercentList.set(6, nonAnswerStu);// 未选人数减去1
		lineChartView.setLineViewData(answerList, answerPercentList, -1);
	}

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
			return sort * (-1);

		}

	}

	public static Map<String, String> transStringToMap(String mapString) {
		Map map = new HashMap();
		java.util.StringTokenizer items;
		for (StringTokenizer entrys = new StringTokenizer(mapString, "&"); entrys
				.hasMoreTokens(); map.put(items.nextToken(),
				items.hasMoreTokens() ? ((Object) (items.nextToken())) : null))
			items = new StringTokenizer(entrys.nextToken(), "=");
		return map;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent();
		intent.setAction(ACTION_BT_CMD);
		intent.putExtra("data", "action:" + ACTION_ANSWER_END);
		sendBroadcast(intent);
		Intent stopIntent = new Intent(StartAnswerActivity.this,
				BluetoothService.class);
		// 调用stopService()方法-传入Intent对象,以此停止服务
		stopService(stopIntent);
		if (mDataChangedListener != null) {
			unregisterReceiver(mDataChangedListener);
		}

		if (mTimer != null) {
			mTimer.cancel();
		}
		SaveQuestionId(questionId);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Intent intent = new Intent();
		intent.setAction(ACTION_BT_CMD);
		intent.putExtra("data", "action:" + ACTION_ANSWER_END);
		sendBroadcast(intent);
		SaveQuestionId(questionId);
	}

	/**
	 * 获取答题时间x,y坐标集合
	 * */
	private void getYCurveList() {
		// TODO Auto-generated method stub
		yAnsTimeLineChartList.clear();
		avgAnswerTime=0;
		int yArray[] = new int[xAnsTimeLineChartList.size() - 1];
		List<String> yListTemp = new ArrayList<String>();
		for (int i = 0; i < answerTimeList.size(); i++) {
			avgAnswerTime+=Float.parseFloat(answerTimeList.get(i));
			for (int k = 1; k < xAnsTimeLineChartList.size(); k++) {
				if (Integer.parseInt(answerTimeList.get(i)) <= Integer
						.parseInt(xAnsTimeLineChartList.get(k)) * 1000) {
					// yCurveList.add(i+1);
					yArray[k - 1]++;
					break;
				}
			}

		}
		avgAnswerTime=avgAnswerTime/answerTimeList.size();
		yAnsTimeLineChartList.add("0");
		for (int j = 0; j < yArray.length; j++) {

			yAnsTimeLineChartList.add(String.valueOf(yArray[j]));
		}
		yListTemp.addAll(yAnsTimeLineChartList);
		Collections.sort(yListTemp);

	}

	private void getXCurveList() {
		// TODO Auto-generated method stub
		xAnsTimeLineChartList.clear();
		if (ansTime <= 10) {
			space=1;
			for (int i = 1; i <= 10; i++) {
				xAnsTimeLineChartList.add(String.valueOf(i));
			}
		} else {
			if(ansTime%10==0){
				space=ansTime/10;
			}else{
				space=ansTime/10+1;
			}
			for (int i = 1; i <= 10; i++) {
				if (ansTime % 10 == 0) {
					xAnsTimeLineChartList.add(String.valueOf(ansTime / 10*i));
				} else {
					xAnsTimeLineChartList.add(String.valueOf((ansTime / 10+1)*i));
				}

			}
		}
		for(int i=0;i<=10;i++){
			answerTimeLineviewXList.set(i,String.valueOf(i*space));
		}
		answerTimeLineviewAdapter.notifyDataSetChanged();
	}
    //点击获取正确答案的列表
	private void getRightAnsYCurveList() {
		// TODO Auto-generated method stub
		int yArray[] = new int[xAnsTimeLineChartList.size() - 1];
		List<String> yListTemp = new ArrayList<String>();
		for (int i = 0; i < rightAnswerTimeList.size(); i++) {
			for (int k = 1; k < xAnsTimeLineChartList.size(); k++) {
				if (Integer.parseInt(rightAnswerTimeList.get(i)) <= Integer
						.parseInt(xAnsTimeLineChartList.get(k)) * 1000) {
					// yCurveList.add(i+1);
					yArray[k - 1]++;
					break;
				}
			}
		}
		yRightAnsTimeLineChartList.clear();
		yRightAnsTimeLineChartList.add("0");
		for (int j = 0; j < yArray.length; j++) {
			yRightAnsTimeLineChartList.add(String.valueOf(yArray[j]));
		}
		yListTemp.addAll(yRightAnsTimeLineChartList);
		Collections.sort(yListTemp);

	}
	public void startTimerDelay() {
		timeDelay = 0;
		mTimerDelay = new Timer();
		mTimerDelay.schedule(new TimerTask() {

			@Override
			public void run() {
				timeDelay++;
				Message msg = new Message();
				msg.what = ANSWERDELAY;
				msg.arg2 = timeDelay;
				handler.sendMessage(msg);
			}
		}, 0, 1000);
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
				ansTime=msg.arg1;
				time = msg.arg1;
				min = time / 60;
				sec = time % 60;
				if (min < 10 && sec < 10) {
					tvAnswerTime.setText("0" + min + " : 0" + sec);
				} else if (min < 10 && sec >= 10) {
					tvAnswerTime.setText("0" + min + " : " + sec);
				} else if (min >= 10 && sec < 10) {
					tvAnswerTime.setText(min + " : 0" + sec);
				} else {
					tvAnswerTime.setText(min + " : " + sec);
				}
				break;
			case ANSWERDELAY:
				time = msg.arg2;
				if (time > 3) {
					imgStartAnswer.setClickable(true);
					imgStartAnswer.setImageResource(R.drawable.start_answer_btn);
					if (mTimerDelay != null) {
						mTimerDelay.cancel();
						mTimerDelay.purge();
					}
					time = 0;
				}
				break;
			}
		};
	};
	private CourseTableHelper mCourseTableHelper;
	private Calendar calendar;
	private int classId=0;
	private TextView judgeChartTitle;

	/**
	 * 缓存答题id
	 * */
	public void SaveQuestionId(int questionId) {
		SharedPreferences.Editor editor = getSharedPreferences(
				"QuestionIdData", MODE_PRIVATE).edit();
		editor.putInt("questionId", questionId);
		editor.commit();
	}

	public int GetQuestionId() {
		SharedPreferences prf = getSharedPreferences("QuestionIdData",
				MODE_PRIVATE);
		int qId = prf.getInt("questionId", 0);
		if (qId == 255) {
			qId = 1;
		}
		return qId;
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
//		if (stopConnected) {
//			if (isConnectedOff && mDevice!=null) {
//				Log.e(TAG, "onRestart----stopConnected:" + stopConnected);
//				int bondstate = mDevice.getBondState();
//				String mac = mDevice.getAddress();
//				Intent intent = new Intent(Broadcast.ACTION_NOTIFY_SERVICE_CONNNECT);
//				intent.putExtra(Broadcast.EXTRA_S1701_MAC, mac);
//				intent.putExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS, mStatus);
//				intent.putExtra(Broadcast.EXTRA_S1701_BONDSTATE, bondstate);
//				sendBroadcast(intent);
//				isConnectedOff=false;
//			}
//		}
		
//		stopConnected = false;
		
//		Log.d(TAG, "onRestart----stopConnected:" + stopConnected);
	}
	
	private boolean stopConnected = false;
	private Timer timer;
	private BluetoothAdapter bluetoothAdapter;
	private int teacherId;
	
	// 切换到后台30s后发送结束答题指令并关闭蓝牙连接
		/*@Override
		protected void onStop() {
			super.onStop();
			SaveQuestionId(questionId);
			if (bluetoothAdapter.isDiscovering()) {
				bluetoothAdapter.cancelDiscovery();
			}
			stopConnected = true;
			Log.d(TAG, "onStop----stopConnected:" + stopConnected);
			timer = new Timer();
			timer.schedule(new TimerTask() {
	
				

				@Override
				public void run() {
					if (stopConnected) {
						Intent intent = new Intent();
						intent.setAction(ACTION_BT_CMD);
						intent.putExtra("data", "action:" + ACTION_ANSWER_END);
						sendBroadcast(intent);
						Intent disIntent = new Intent(
								Broadcast.ACTION_NOTIFY_SERVICE_DISCONNECT);
						sendBroadcast(disIntent);
						isConnectedOff = true;
					}
				}
			}, 30000);
		}*/
	
}
