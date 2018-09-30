package com.routon.smartcampus.attendance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.smartcampus.answerrelease.FragmentAnswerTrueStuImgView;
import com.routon.smartcampus.answerrelease.PopupList;
import com.routon.smartcampus.answerrelease.service.Broadcast;
import com.routon.smartcampus.answerrelease.service.BtDevice;
import com.routon.smartcampus.coursetable.CourseDataUtil.TimeTable;
import com.routon.smartcampus.coursetable.CourseTableHelper;
import com.routon.smartcampus.coursetable.CourseTableHelper.ErrorListener;
import com.routon.smartcampus.coursetable.CourseTableHelper.Listener;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.StudentHelper;
import com.routon.widgets.Toast;

public class AttenceStartFragment extends Fragment implements OnClickListener, OnItemClickListener{
	
	private AttenceActivity attenceActivity;
	private ProgressDialog mWaitDialog;
	private ImageView refreshImg;
	private RecyclerView classRev;
	private MyScrollView mainScroll;
	private LinearLayout tagLayout;
	private LinearLayout numLayout;
	private AttenceDegreeCircle attenceDegreeCircle;
	private ImageButton startAttenceBtn;
	private ImageView loadingImg;
	private ImageView uploadAttenceImg;
	private StudentLine studentLine;
	private TextView donotAttenceTagTv;
	private TextView donotAttenceNumTv;
	private TextView sumAttenceNumTv;
	private TextView reallyAttenceNumTv;
	private MyGridView donotAttenceStuGv;
	
	private PopupList popupList;
	private int centerPosition;
	private RotateAnimation rotationAnimator;
	private ObjectAnimator btRotationAnimator;
	private LinearLayoutManager layoutManager;
	private List<String> popupMenuItemList = new ArrayList<String>();
	private ArrayList<BtDevice> mBtDevices;
	private BluetoothAdapter bluetoothAdapter;
	private int userId;
	public int teacherId = 0;
	private int lesson = -1;
	private Timer overTimer;
	private int overTime;
	private Timer internetAttenceTimer;
	private String urlGroupId;
	private AttendanceActivityReceiver receiver;
	private IntentFilter filter;
	private List<TimeTable> timetables;
	private List<String> amCourses;
	private List<String> pmCourses;
	private Calendar calendar;
	private ClassDeviceAdapter adapter;
	private int tidTimes;
	private boolean isRunAttence=false;
	private GridViewAdapter gridViewAdapter;
	private int mSelClassIndex;
	private int mGetStudentListDataNum;
	private String savedConnectedMac = "";
	private ClassStudentData[] mAllStudentDataList;
	private List<Integer> errorCodes;
	private List<String> errorMsgs;
	private ArrayList<AttendanceBean> studentList;
	private ArrayList<AttendanceBean> realityStudentLists;//出勤学生数据
	private ArrayList<AttendanceBean> absentStudentLists;//缺勤学生数据
	
	private static final String TAG="AttenceStartFragment";
	private static final String ACTION_BT_CMD = "bt_cmd";
	private static final String ACTION_RECEIVE_S1701_TID = "terminal_id_ack";
	private static final String ACTION_ATTENCE_START = "attence_start";//考勤开始
	private static final String ACTION_ATTENCE_REPORT = "attence_report";//打卡数据
	private static final String ACTION_ATTENCE_FINISH = "attence_finish";//考勤结束
	private static final String ACTION_GET_ALL_STUDENT = "get_student_finish";//获取学生数据完成
	private static final int MAX_ROUND = 6;//S1705最多轮询次数
	private static final float ROUND_TIME = 0.5f;//S1705每条广播时间
	private static final int MAX_MAC_ROUND = 8;//每条广播最多带多少MAC(后三位)
	private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 0;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		attenceActivity = (AttenceActivity)context;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container , Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 进入答题时不让手机自动休眠
		attenceActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		View view = inflater.inflate(R.layout.fragment_main_attendance, container, false);	
		initView(view);
		initData();
		return view;
    }

	private void initData() {
		errorCodes = new ArrayList<>();
		errorMsgs = new ArrayList<>();
		mBtDevices = new ArrayList<>();
		studentList=new ArrayList<AttendanceBean>();
		realityStudentLists = new ArrayList<>();
		absentStudentLists = new ArrayList<>();
		timetables = new ArrayList<TimeTable>();
		amCourses = new ArrayList<String>();
		pmCourses = new ArrayList<String>();
		adapter = new ClassDeviceAdapter(attenceActivity, mBtDevices);
		calendar = Calendar.getInstance();
		receiver = new AttendanceActivityReceiver();
		filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(Broadcast.ACTION_BT_CONNECT_STATE_CHANGED);
		filter.addAction(Broadcast.ACTION_RECEIVE_S1701_TID);
		filter.addAction(ACTION_ATTENCE_REPORT);
		filter.addAction(ACTION_ATTENCE_FINISH);
		filter.addAction(Broadcast.BT_CONNECT_STATE_CHANGED);
		filter.addAction(ACTION_GET_ALL_STUDENT);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		attenceActivity.registerReceiver(receiver, filter);
		classRev.setAdapter(adapter);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		setRecycleView();
		getErrorMsg();
		getSavedTeacherId();
	}

	private void initView(View view) {
		refreshImg = (ImageView)view.findViewById(R.id.img_bluetooth_refresh);
		classRev = (RecyclerView)view.findViewById(R.id.rev_class_device_info);
		classRev.setLayoutManager(new LinearLayoutManager(attenceActivity));
		tagLayout = (LinearLayout)view.findViewById(R.id.ll_class_attence_tag);
		numLayout = (LinearLayout)view.findViewById(R.id.ll_class_attence_num);
		attenceDegreeCircle = (AttenceDegreeCircle)view.findViewById(R.id.attence_degree_circle);
		startAttenceBtn = (ImageButton)view.findViewById(R.id.ibtn_start_attendance);
		loadingImg = (ImageView)view.findViewById(R.id.img_attendance_loading);
		uploadAttenceImg = (ImageView)view.findViewById(R.id.img_attence_result_upload);
		studentLine = (StudentLine)view.findViewById(R.id.student_line_index);
		donotAttenceTagTv = (TextView)view.findViewById(R.id.tv_donot_attence_tag);
		donotAttenceNumTv = (TextView)view.findViewById(R.id.tv_donot_attence_num);
		sumAttenceNumTv = (TextView)view.findViewById(R.id.tv_should_attence_num);
		reallyAttenceNumTv = (TextView)view.findViewById(R.id.tv_really_attence_num);
		donotAttenceStuGv = (MyGridView)view.findViewById(R.id.gv_donot_attence_student);
		mainScroll = (MyScrollView)view.findViewById(R.id.scroll_attence_start);
		mainScroll.setTouchUnDealView(classRev);
		refreshImg.setOnClickListener(this);
		startAttenceBtn.setOnClickListener(this);
		donotAttenceTagTv.setOnClickListener(this);
		donotAttenceNumTv.setOnClickListener(this);
		uploadAttenceImg.setOnClickListener(this);
		setVisible(View.INVISIBLE);
	}
	
    private void setRecycleView() {
    	final RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(attenceActivity) {
    		@Override 
    		protected int getVerticalSnapPreference() {
    		    return LinearSmoothScroller.SNAP_TO_START;
    		}

			@Override
			public PointF computeScrollVectorForPosition(int arg0) {
				return null;
			}
    	};	
    	classRev.addOnScrollListener(new RecyclerView.OnScrollListener() {
    		private int totalDy;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "newState="+newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                	//classRev.smoothScrollToPosition(centerPosition-2);
                	smoothScroller.setTargetPosition(centerPosition-2);
                	layoutManager.startSmoothScroll(smoothScroller);
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalDy += dy;
                int itemHeight = StudentHelper.dp2px(attenceActivity, 40);
                int range = totalDy % itemHeight;
                //这个位置表示目前在屏幕显示的中间item的位置
                centerPosition = getPosition() + 2;
                if(range > itemHeight/2){
                	centerPosition += 1;
                }
                //Log.d(TAG, "centerPosition=" +centerPosition);
                for (int i = 0; i < mBtDevices.size(); i++) {
                    if (i != centerPosition-2) {
                    	mBtDevices.get(i).setChecked(false);
                    } else {
                    	mBtDevices.get(i).setChecked(true);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    
    private void setVisible(int visibility){
    	if(visibility == View.VISIBLE){
    		startAttenceBtn.setVisibility(View.INVISIBLE);
    		numLayout.setVisibility(visibility);
    		sumAttenceNumTv.setText(studentList.size()+"人");
    		reallyAttenceNumTv.setText(realityStudentLists.size()+"人");
    		donotAttenceTagTv.setVisibility(View.INVISIBLE);
    		donotAttenceNumTv.setVisibility(View.INVISIBLE);
    		studentLine.setVisibility(View.INVISIBLE);
    		donotAttenceStuGv.setVisibility(View.GONE);
			tagLayout.setVisibility(visibility);
			loadingImg.setVisibility(visibility);
			attenceDegreeCircle.setVisibility(visibility);
			rotationAnimator = new RotateAnimation(0,360, RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
			rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
			rotationAnimator.setInterpolator(new LinearInterpolator());
			rotationAnimator.setDuration(1000);
			loadingImg.startAnimation(rotationAnimator);
    	}else if(visibility == View.INVISIBLE){
			numLayout.setVisibility(visibility);
			tagLayout.setVisibility(visibility);
			loadingImg.clearAnimation();
			loadingImg.setVisibility(visibility);
			startAttenceBtn.setVisibility(View.VISIBLE);
			attenceDegreeCircle.setVisibility(View.INVISIBLE);
			studentLine.setVisibility(View.INVISIBLE);
			donotAttenceStuGv.setVisibility(View.GONE);
		}
    }
    
    /*获取recycleview中当前显示的第一个item的position*/
    private int getPosition() {
        layoutManager = (LinearLayoutManager) classRev.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        return position;
    }

	public void doDiscovery() {
		if (Build.VERSION.SDK_INT >= 23) {
			int hasPermission = ActivityCompat.checkSelfPermission(attenceActivity,
					Manifest.permission.ACCESS_COARSE_LOCATION);
			if (hasPermission == PackageManager.PERMISSION_GRANTED) {
				continueDoDiscovery();
				return;
			}
			ActivityCompat.requestPermissions(attenceActivity,
					new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
					REQUEST_COARSE_LOCATION_PERMISSIONS);
		} else {
			continueDoDiscovery();
		}
	}
	public void continueDoDiscovery() {
		if (!bluetoothAdapter.isDiscovering()) {
			Log.d(TAG, "start scanning...");
			bluetoothAdapter.startDiscovery();
		} else {
			bluetoothAdapter.cancelDiscovery();
			bluetoothAdapter.startDiscovery();
			Log.d(TAG, "restart scanning...");
			if(mBtDevices != null){
				for(BtDevice btDevice : mBtDevices){
					if(btDevice.getDevice() != null){
						if(!btDevice.getDevice().getAddress().equals(savedConnectedMac)){
							btDevice.setDevice(null);
							adapter.notifyDataSetChanged();
						}
					}
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(receiver != null){
			attenceActivity.unregisterReceiver(receiver);
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_bluetooth_refresh:
			/*if(savedConnectedMac.equals("")){
				doDiscovery();
			}else {
				Toast.makeText(AttendanceMainActivity.this, "蓝牙已连接", Toast.LENGTH_SHORT).show();
			}*/
			doDiscovery();
			break;
		case R.id.ibtn_start_attendance:
			startAttence();
			break;
		case R.id.tv_donot_attence_tag:
			showDonotAttenceStudent();
			break;
		case R.id.tv_donot_attence_num:
			showDonotAttenceStudent();
			break;
		case R.id.img_attence_result_upload:
			CommitAttenceInfo(lesson);
			break;
		default:
			break;
		}
	}
	
	public void startAttence(){
		BtDevice selectBtDevice = null;
		for(BtDevice btDevice : mBtDevices){
			if(btDevice.isChecked() == true){
				selectBtDevice = btDevice;
			}
		}
		//当前选择班级没有S1701
		if(selectBtDevice.getDevice() == null){
			Toast.makeText(attenceActivity, "网络考勤中", Toast.LENGTH_SHORT).show();
			absentStudentLists.clear();
			realityStudentLists.clear();
			attenceDegreeCircle.setDegree(0);
			setVisible(View.VISIBLE);
			doInternetAttence();
			return;
		}
		if (bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}
		//当前选择班级的S1701已连接
		if(selectBtDevice.getDevice().getAddress().equals(savedConnectedMac)){
			sendStartAttence();
			setVisible(View.VISIBLE);
			return;
		}
		//showProgressDialog();
		setVisible(View.VISIBLE);
		int bondstate = selectBtDevice.getDevice().getBondState();
		String mac = selectBtDevice.getDevice().getAddress();
		String status = selectBtDevice.getStatus();
		String name = selectBtDevice.getName();
		Log.d(TAG, "the select name:" + name + " mac:" + mac + " status:" + status + " bondstate:" + bondstate);
		Intent intent = new Intent(Broadcast.ACTION_NOTIFY_SERVICE_CONNNECT);
		intent.putExtra(Broadcast.EXTRA_S1701_MAC, mac);
		intent.putExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS, status);
		intent.putExtra(Broadcast.EXTRA_S1701_BONDSTATE, bondstate);
		attenceActivity.sendBroadcast(intent);
	}
	
	public void sendStartAttence(){
		
		for (int i = 0; i < studentList.size(); i++) {
			studentList.get(i).attenceType=0;
		}
		
		Intent newIntent = new Intent();
		newIntent.setAction(ACTION_BT_CMD);
		newIntent.putExtra("data", "action:" + ACTION_ATTENCE_START);
		attenceActivity.sendBroadcast(newIntent);
		startAttenceTimer();
	}
	
	//网络考勤
	public void doInternetAttence(){
		getStudentListDataFromUrl(String.valueOf(getSelectBtDevice().getGroupId()), false);
		notifyInternetAttence(String.valueOf(getSelectBtDevice().getGroupId()));
	}
	
	public void showDonotAttenceStudent(){
		if(studentLine.getVisibility() == View.VISIBLE){
			studentLine.setVisibility(View.INVISIBLE);
			studentLine.setIndexX(donotAttenceNumTv.getLeft()+donotAttenceNumTv.getWidth()/2+StudentHelper.dp2px(attenceActivity, 50));
			donotAttenceStuGv.setVisibility(View.GONE);
		}else {
			studentLine.setVisibility(View.VISIBLE);
			studentLine.setIndexX(donotAttenceNumTv.getLeft()+donotAttenceNumTv.getWidth()/2+StudentHelper.dp2px(attenceActivity, 50));
			donotAttenceStuGv.setVisibility(View.VISIBLE);
		}
	}
	
	class AttendanceActivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "action:"+action);
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
				if (device.getName() == null
						|| (!device.getName().contains("S1701_") && !device.getName().contains("s1701_"))) {
					return;
				}
				String className = device.getName().split("_")[1];
				Log.d(TAG, "device name:" + device.getName() + "   device mac:" + device.getAddress()
						+" className:"+className);
				for(int i = 0;i<mBtDevices.size();i++){
					BtDevice btDevice = mBtDevices.get(i);
					if(btDevice.getClassName().equals(className)){
						btDevice.setDevice(device);
						btDevice.setRssi(rssi);
						adapter.notifyDataSetChanged();
					}
				}
			}else if (action.equals(Broadcast.ACTION_BT_CONNECT_STATE_CHANGED)) {
				String btdeviceAddress = intent.getStringExtra("btdevice_address");
				String status = intent.getStringExtra("btdevice_status");
				if(status.equals("未连接")){
					tidTimes = 0;
					savedConnectedMac = "";
					for (int i = 0; i < mBtDevices.size(); i++) {
						BtDevice btDevice = mBtDevices.get(i);
						if(btDevice.isChecked() && btDevice.getDevice().getAddress().equals(btdeviceAddress)){
							hideProgressDialog();
							setVisible(View.INVISIBLE);
							Toast.makeText(attenceActivity, "连接失败", Toast.LENGTH_SHORT).show();
						}
					}
				}else if(status.equals("已连接")){
					savedConnectedMac = btdeviceAddress;
				}
				Log.d(TAG, "address:" + btdeviceAddress + "   status:" + status);
				for (int i = 0; i < mBtDevices.size(); i++) {
					BtDevice btDevice = mBtDevices.get(i);
					if(btDevice.getDevice() != null){
						if (btDevice.getDevice().getAddress().equals(btdeviceAddress)) {
							btDevice.setStatus(status);
							adapter.notifyDataSetChanged();
						}
					}
				}
			}else if (action.equals(ACTION_RECEIVE_S1701_TID)) {
				tidTimes++;
				Log.d(TAG, "tidTimes="+tidTimes);
				if(tidTimes >= 2){
					return;
				}
				String terminalId = intent.getStringExtra("data");// 获取连接的终端id
				if (terminalId != null) {
					GetclassIdFromUrl(terminalId);					
				} else {
					Toast.makeText(attenceActivity, "未获取到终端id", Toast.LENGTH_SHORT).show();
					setVisible(View.INVISIBLE);
					hideProgressDialog();
				}
			}else if (action.equals(ACTION_ATTENCE_REPORT)) {
				if(!isRunAttence){
					return;
				}
				String data = intent.getStringExtra("data");
				Log.d(TAG,"data:"+data);
				if(data!=null&&studentList.size()>0){
					for(int i=0;i<studentList.size();i++){
						if(studentList.get(i).mac.equals(data)&&!realityStudentLists.contains(studentList.get(i))){
							realityStudentLists.add(studentList.get(i));
							absentStudentLists.remove(studentList.get(i));
							studentList.get(i).attenceType=1;
							Log.d(TAG,"出勤学生信息:"+studentList.get(i).empName);
							reallyAttenceNumTv.setText(realityStudentLists.size()+"人");
							attenceDegreeCircle.setDegree(360.0f*(float)realityStudentLists.size()/(float)studentList.size());
						}
					}
				}
				
			}else if (action.equals(ACTION_ATTENCE_FINISH)) {
				String data = intent.getStringExtra("data");
				if(!TextUtils.isEmpty(data)){
					String errorCode = data.substring(6);
					Log.d(TAG, "error="+errorCode);
					String errorMsg = getCheckErrorCode(Integer.valueOf(errorCode));
					if(!TextUtils.isEmpty(errorMsg)){
						Toast.makeText(attenceActivity, errorMsg, Toast.LENGTH_SHORT).show();
					}
				}
				isRunAttence=false;
				if(overTimer!=null){
					overTimer.cancel();
				}
				if(rotationAnimator != null){
					rotationAnimator.cancel();
				}
				loadingImg.setImageResource(R.drawable.ic_attence_finish);
				loadingImg.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						loadingImg.setVisibility(View.GONE);
						loadingImg.setImageResource(R.drawable.ic_attendance_loading);
						startAttenceBtn.setVisibility(View.VISIBLE);
					}
				}, 3000);
				donotAttenceTagTv.setVisibility(View.VISIBLE);
	    		donotAttenceNumTv.setVisibility(View.VISIBLE);
	    		donotAttenceNumTv.setText(absentStudentLists.size()+"人");
	    		gridViewAdapter = new GridViewAdapter(attenceActivity, absentStudentLists);
	    		donotAttenceStuGv.setAdapter(gridViewAdapter);
	    		donotAttenceStuGv.setOnItemClickListener(AttenceStartFragment.this);
			}else if(action.equals(Broadcast.BT_CONNECT_STATE_CHANGED)){//S1701连接成功
				int status = intent.getIntExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS,
						Broadcast.S1701_STATUS_CONNECT_NONE);
				if (status == Broadcast.S1701_STATUS_CONNECTED) {
					
				}
			}else if(action.equals(ACTION_GET_ALL_STUDENT)){
				sendStartAttence();
			}else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				Log.d(TAG, "搜索结束");
				if(btRotationAnimator != null){
					btRotationAnimator.cancel();
				}
			}else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
				Log.d(TAG, "开始搜索");
				if(btRotationAnimator != null){
					btRotationAnimator.cancel();
				} 
				btRotationAnimator = ObjectAnimator.ofFloat(refreshImg, "rotation", 0, 360);
				btRotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
				btRotationAnimator.setInterpolator(new LinearInterpolator());
				btRotationAnimator.setDuration(1000);
				btRotationAnimator.start();
			}
		}
	}
	
	/**
	 * 获取分组id
	 * */
	public void GetclassIdFromUrl(final String terminalId) {
		String urlString = SmartCampusUrlUtils.getClassIdUrl(terminalId);
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

				@Override
				public void onResponse(JSONObject response) {
				Log.d(TAG, "response=" + response);
				int code = response.optInt("code");
				if(code == 0){
					JSONObject obj = response.optJSONObject("obj");
					urlGroupId = obj.optString("groupId");
					if (!TextUtils.isEmpty(urlGroupId)) {								
						studentList.clear();
						absentStudentLists.clear();
						realityStudentLists.clear();						
						getStudentListDataFromUrl(urlGroupId, true);
					}
				}else {
					Toast.makeText(attenceActivity, response.optString("msg"), Toast.LENGTH_SHORT).show();
					hideProgressDialog();
					setVisible(View.INVISIBLE);
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
				hideProgressDialog();
				setVisible(View.INVISIBLE);
			}
		});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void getClassListData() {
		showProgressDialog();
		GroupListData.getClassListData(attenceActivity, new DataResponse.Listener<ArrayList<GroupInfo>>() {


			@Override
			public void onResponse(ArrayList<GroupInfo> classGroups) {
				hideProgressDialog();
				ArrayList<Integer> classGroupIdList = new ArrayList<Integer>();
				ArrayList<String> classList = new ArrayList<String>();
				for (int i = 0; i < classGroups.size(); i++) {
					mBtDevices.add(new BtDevice(classGroups.get(i).getName(), classGroups.get(i).getId()));
					classList.add(classGroups.get(i).getName());
					classGroupIdList.add(classGroups.get(i).getId());
				}				
				if (mBtDevices.size() > 0) {
					for (int i = 0; i < mBtDevices.size(); i++) {
						BtDevice btDevice = mBtDevices.get(i);
						Log.d(TAG, "The " + i + " groupId:" + btDevice.getGroupId());
						Log.d(TAG, "The " + i + " className:" + btDevice.getClassName());
					}
					adapter.notifyDataSetChanged();
				}
				if (classGroups.size() > 0) {
					doDiscovery();
					// 获取当前班级
					getCurrentClass(classGroupIdList);
					if (classList.size()>0) {
						mSelClassIndex = 0;
					}
				} 
			}
		}, new DataResponse.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				if( AttenceStartFragment.this == null ){
					return;
				}
				hideProgressDialog();
			}
		}, new DataResponse.SessionInvalidListener() {

			@Override
			public void onSessionInvalidResponse() {
				// TODO Auto-generated method stub
				if(AttenceStartFragment.this == null ){
					return;
				}
				hideProgressDialog();
			}
		});
	}
	
	public void hideProgressDialog(){
		if( mWaitDialog != null ){
			mWaitDialog.dismiss();
			mWaitDialog = null;
		}
	}
	
	public void showProgressDialog(){
		if( mWaitDialog == null ){
			mWaitDialog = ProgressDialog.show(attenceActivity, "", "...正在获取学生列表...");
		}
	}
	
	public String getClassName(String groupId){
		String className = "";
		for(BtDevice btDevice : mBtDevices){
			if(btDevice.getGroupId() == Integer.valueOf(groupId)){
				className = btDevice.getClassName();
				break;
			}
		}
		return className;
	}

	/**
	 * 获取班级学生列表
	 * */
	private void getStudentListDataFromUrl(final String groupIds, final boolean sendFlag) {// 获取学生列表
		String urlString = SmartCampusUrlUtils.getStudentListUrl() + "&groupIds=" + groupIds;
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);

						try {
							if (response.getInt("code") == 0) {
								hideProgressDialog();
								if (studentList != null && studentList.size() > 0) {
									studentList.clear();
								}
								JSONArray array = response.optJSONArray("datas");
								if (array != null) {
									int len = array.length();
									for (int i = 0; i < len; i++) {
										JSONObject obj = (JSONObject) array.get(i);
										AttendanceBean bean = new AttendanceBean(obj);
										studentList.add(bean);
										absentStudentLists.add(bean);
									}
								}
								Log.d(TAG, "学生列表:" + studentList.size());
								sumAttenceNumTv.setText(studentList.size()+"人");
								if(sendFlag){
									Intent intent = new Intent(ACTION_GET_ALL_STUDENT);
									attenceActivity.sendBroadcast(intent);
								}
								CourseTableHelper courseTableHelper = new CourseTableHelper(attenceActivity);
								getClassTables(Integer.valueOf(groupIds), getClassName(groupIds), courseTableHelper);
								// downloadStudentImage(studentdatalist);
								// initStudentList(studentdatalist, null);
							} else if (response.getInt("code") == -2) {
								hideProgressDialog();
								setVisible(View.INVISIBLE);
								Toast.makeText(attenceActivity, response.getString("msg"), Toast.LENGTH_LONG).show();
							} else {
								Log.e(TAG, response.getString("msg"));
								setVisible(View.INVISIBLE);
								Toast.makeText(attenceActivity, response.getString("msg"), Toast.LENGTH_LONG).show();
								hideProgressDialog();
							}

						} catch (JSONException e) {
							Toast.makeText(attenceActivity, "获取学生数据失败", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
							setVisible(View.INVISIBLE);
							hideProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						hideProgressDialog();
						setVisible(View.INVISIBLE);
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	/**
	 * 获取每个班的作息时间表和课程表
	 */
	private void getClassTables(final int groupId, final String className, final CourseTableHelper courseTableHelper) {
		courseTableHelper.getSchoolAttendance(groupId, new Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.d(TAG, "response:" + response);
				Calendar mCalendar=Calendar.getInstance();
				courseTableHelper.getCourseTableAboutXmls(groupId,mCalendar, new Listener<String>() {

					private Date myDate;

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "下载表成功");
						Log.d(TAG, className + " response:" + response);
						timetables = courseTableHelper.getCourseData(calendar, true,true);
						amCourses = courseTableHelper.getAmCourseTimeAndName(timetables);
						pmCourses = courseTableHelper.getPmCourseTimeAndName(timetables);
						List<String> beginTimeList = new ArrayList<String>();
						List<String> endTimeList = new ArrayList<String>();
						List<String> courseNameList = new ArrayList<String>();
						if (amCourses != null && amCourses.size() > 0) {
							for (int i = 0; i < amCourses.size(); i++) {
								if(amCourses.get(i).lastIndexOf("-")>0){
									beginTimeList.add(amCourses.get(i).substring(0, amCourses.get(i).lastIndexOf("-") - 1));
									endTimeList.add(amCourses.get(i).substring(amCourses.get(i).lastIndexOf("-") + 2,
											amCourses.get(i).lastIndexOf("-") + 7));
									courseNameList.add(amCourses.get(i).substring(amCourses.get(i).lastIndexOf(" "),
											amCourses.get(i).length()));
								}
								
							}
						}
						if (pmCourses != null && pmCourses.size() > 0) {
							for (int i = 0; i < pmCourses.size(); i++) {
								if(pmCourses.get(i).lastIndexOf("-")>0){
								beginTimeList.add(pmCourses.get(i).substring(0, pmCourses.get(i).lastIndexOf("-") - 1));
								endTimeList.add(pmCourses.get(i).substring(pmCourses.get(i).lastIndexOf("-") + 2,
										pmCourses.get(i).lastIndexOf("-") + 7));
								courseNameList.add(pmCourses.get(i).substring(pmCourses.get(i).lastIndexOf(" "),
										pmCourses.get(i).length()));
								}
							}
						}
						myDate = new Date();
						SimpleDateFormat dfff = new SimpleDateFormat("HH:mm");
						String nowTime = dfff.format(myDate);
						int courseIndex = getCourseIndex(nowTime, beginTimeList, endTimeList, courseNameList);
						lesson = courseIndex;
						Log.d(TAG, "当前是第" + courseIndex + "节课" + " 班级:" + className);
						if (courseIndex == -1) {
							Log.d(TAG, "当前不是上课时间");
						} else {
							courseIndex--;
							Log.d(TAG, "当前teacherId:" + teacherId + " 该班级当前上课teacherId:"
									+ timetables.get(courseIndex).teacherId);
							if (TextUtils.isEmpty(timetables.get(courseIndex).teacherId)) {
								Log.d(TAG, "该班级当前上课teacherId为空");
								return;
							}
							if (timetables.get(courseIndex).teacherId.equals(String.valueOf(teacherId))) {
								Log.d(TAG, "当前老师在" + className + "上课");
								Intent intent = new Intent(Broadcast.ACTION_AUTO_CONNECT);
								intent.putExtra(Broadcast.EXTRA_AUTO_CONNECT_S1701, className);
								attenceActivity.sendBroadcast(intent);
							}
						}
					}
				}, new ErrorListener() {

					@Override
					public void onResponse(String errorMsg) {
						// Toast.makeText(AnswerMainActivity.this, errorMsg,
						// Toast.LENGTH_SHORT).show();
						Log.d(TAG, "errorMsg:" + errorMsg);
					}
				});
			}
		});
	}

	/**
	 * 
	 * @param beginTimeList
	 * @param endTimeList
	 * @param courseNameList
	 * @return 当前时间课程序号，-1表示当前不是上课时间
	 */
	private int getCourseIndex(String nowTime, List<String> beginTimeList, List<String> endTimeList,
			List<String> courseNameList) {
		// TODO Auto-generated method stub
		int index = -1;
		//lesson从1开始
		for (int i = 1; i <= beginTimeList.size(); i++) {
			if (FragmentAnswerTrueStuImgView.isBelongTime(nowTime, beginTimeList.get(i-1), endTimeList.get(i-1))) {
				index = i;
				break;
			}
		}
		return index;
	}

	
	/**
	 * 获取teacherId
	 * 
	 * @param userId
	 * @param type
	 */
	private void getTeacherId(int userId, int type) {
		// TODO Auto-generated method stub
		String urlString = SmartCampusUrlUtils.getCourseNameUrl(String.valueOf(userId), String.valueOf(type));
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						Log.d(TAG, "response=" + response);
						try {
							if (response.getInt("code") == 0) {
								JSONObject obj = response.getJSONObject("datas");
								teacherId = obj.optInt("sid");
								attenceActivity.getSharedPreferences("saved_teacherId", Context.MODE_PRIVATE).edit()
										.putInt(String.valueOf(AttenceStartFragment.this.userId), teacherId).commit();
								Log.d(TAG, teacherId + "老师id");
								getClassListData();
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication.returnToLogin(attenceActivity);

							} else {
								getClassListData();
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(attenceActivity, "查询老师信息失败!", Toast.LENGTH_SHORT).show();

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,getTeacherId Error: " + arg0);
						if (InfoReleaseApplication.showNetWorkFailed(attenceActivity) == true) {
							Toast.makeText(attenceActivity, "查询老师信息失败!", Toast.LENGTH_SHORT).show();
						}

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	//通知1701考勤
	public void notifyInternetAttence(String groupId){
		String urlString = SmartCampusUrlUtils.getNotifyAttenceUrlString(groupId);
		CookieJsonRequest request = new CookieJsonRequest(Method.GET, urlString, null, 
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "notify response="+response);
						int code = response.optInt("code");
						if(code == 0){
							if(internetAttenceTimer != null){
								internetAttenceTimer.cancel();
							}
							internetAttenceTimer = new Timer();
							internetAttenceTimer.schedule(new TimerTask() {
								
								@Override
								public void run() {
									getStudentAttendanceListData();
								}
							}, 10000, 10000);
						}else {
							Toast.makeText(attenceActivity, response.optString("msg"), Toast.LENGTH_SHORT).show();
							setVisible(View.INVISIBLE);
						}
					}
				}, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(attenceActivity, "发起网络考勤失败", Toast.LENGTH_SHORT).show();
						setVisible(View.INVISIBLE);
					}
				});
		request.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(request);
	}
	
	//查询班级某节课的考勤信息
	private void getStudentAttendanceListData() {	
		absentStudentLists.clear();
		BtDevice btDevice = getSelectBtDevice();
		final int groupId = btDevice.getGroupId();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String timeStr = sdf.format(new Date());
		String urlString = SmartCampusUrlUtils.getAttendResultUrl(String.valueOf(groupId),timeStr, String.valueOf(lesson));
		Log.d(TAG,"getStudentAttendanceListData urlString:"+urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							Log.d(TAG, "response=" + response);
							if(internetAttenceTimer != null){
								internetAttenceTimer.cancel();
							}
							if(rotationAnimator != null){
								rotationAnimator.cancel();
							}
							loadingImg.setImageResource(R.drawable.ic_attence_finish);
							loadingImg.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									loadingImg.setVisibility(View.GONE);
									loadingImg.setImageResource(R.drawable.ic_attendance_loading);
									startAttenceBtn.setVisibility(View.VISIBLE);
								}
							}, 3000);
							donotAttenceTagTv.setVisibility(View.VISIBLE);
				    		donotAttenceNumTv.setVisibility(View.VISIBLE);
							int code = response.optInt("code");
							if( code == 0 ){//成功
								StudentAttendanceBean bean = StudentAttendanceBean.parseStudentAttendanceBean(response.optJSONObject("datas"));
								if( bean != null && bean.studentlist != null && bean.studentlist.size() > 0 ){
									absentStudentLists.addAll(bean.studentlist);
								}else{
									Toast.makeText(attenceActivity, "无缺勤学生", Toast.LENGTH_SHORT).show();
								}
								initStudentGridView();
							}else if ( code == -2) {
								InfoReleaseApplication.returnToLogin(attenceActivity);
							} else {
								String msg = response.optString("msg");
								if( msg == null || msg.isEmpty() == true ){
									Toast.makeText(attenceActivity, "获取缺勤数据失败", Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(attenceActivity, msg, Toast.LENGTH_LONG).show();
								}
							}

						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
							if(internetAttenceTimer != null){
								internetAttenceTimer.cancel();
							}
							setVisible(View.INVISIBLE);
							String msg = arg0.getMessage();
							if( msg == null || msg.isEmpty() == true ){
								Toast.makeText(attenceActivity, "获取考勤数据失败", Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(attenceActivity, msg, Toast.LENGTH_LONG).show();
							}
						}
					});

			jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
			InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
		
	}
	
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
							Toast.makeText(attenceActivity, msg, Toast.LENGTH_SHORT).show();
						}
					}
				}, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(attenceActivity, "查询error失败", Toast.LENGTH_SHORT).show();
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
	
	private void initStudentGridView(){
		
		realityStudentLists.addAll(studentList);
		for(int i=0;i<absentStudentLists.size();i++){
			AttendanceBean attendanceBean = absentStudentLists.get(i);
			for(int j=0;j<realityStudentLists.size();j++){
				if(realityStudentLists.get(j).sid == attendanceBean.sid){
					realityStudentLists.remove(j);
				}
			}
		}
		AttendanceRankingAdapter adapter = new AttendanceRankingAdapter(attenceActivity, 
				absentStudentLists);
		donotAttenceStuGv.setAdapter(adapter);
		sumAttenceNumTv.setText(studentList.size()+"人");
		attenceDegreeCircle.setDegree(360.0f*(float)realityStudentLists.size()/(float)studentList.size());
		donotAttenceNumTv.setText(absentStudentLists.size()+"人");
		reallyAttenceNumTv.setText(realityStudentLists.size()+"人");
	}
	
	/**
	 * 如果teacherId没保存本地，要重新取
	 */
	public void getSavedTeacherId() {
		userId = InfoReleaseApplication.authenobjData.userId;
		if (userId == 0) {
			Log.d(TAG, "userId is null");
			return;
		}
		Log.d(TAG, "userId:" + userId);
		int savedTeacherId = attenceActivity.getSharedPreferences("saved_teacherId", Context.MODE_PRIVATE).getInt(
				String.valueOf(userId), 0);
		if (savedTeacherId == 0) {
			getTeacherId(userId, 2);
		} else {
			teacherId = savedTeacherId;
			Log.d(TAG, teacherId + "老师id");// 18327
			getClassListData();
		}
	}
	
	/**
	 * 根据学生数量来计算发四轮广播所需时间
	 * @param count 班级人数
	 * @return S1701广播总时间(s)
	 */
	private int getMaxBleTime(int count)
	{
		if(count <= MAX_MAC_ROUND)
		{
			return (int) Math.ceil(MAX_ROUND * ROUND_TIME);
		}
		else {
			return (int)(Math.ceil((MAX_ROUND * count) / MAX_MAC_ROUND) * ROUND_TIME);
		}
	}
	
	private void startAttenceTimer()
	{
		final int rangeTime = getMaxBleTime(studentList.size())+10;
		Log.d(TAG, "超时时间:"+rangeTime);
		isRunAttence = true;
		absentStudentLists.clear();
		realityStudentLists.clear();
		attenceDegreeCircle.setDegree(0);
		absentStudentLists.addAll(studentList);
		overTime = 0;
		overTimer = new Timer();
		overTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				overTime++;
				if(overTime>=rangeTime&&isRunAttence&&overTimer!=null)
				{
					Intent intent = new Intent(ACTION_ATTENCE_FINISH);
					attenceActivity.sendBroadcast(intent);
					overTimer.cancel();
				}
			}
		}, 0,1000);
				
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(absentStudentLists.get(position).attenceType==0){
			int sid=absentStudentLists.get(position).sid;
			showPopupList(view,position,sid);
		}
	}
	
	public void showPopupList(final View anchorView,final int contextPosition,final int sid) {
		if(popupMenuItemList.size()==0){
			popupMenuItemList.add("判定出勤");
		}
		int[] location = new int[2];
		anchorView.getLocationOnScreen(location);
		final float x = location[0] + anchorView.getWidth() / 2-8;
		final float y = location[1] + anchorView.getHeight() / 3;
		popupList = new PopupList(attenceActivity); 
		popupList.setTextSize(StudentHelper.sp2px(attenceActivity, 20));
		View popupView=popupList.getIndicatorView();
		popupView.setFocusable(true);
		popupView.requestFocus();
		
		popupList.showPopupListWindow(anchorView, contextPosition,x,y, popupMenuItemList, new PopupList.PopupListListener() {
			 @Override
			 public void onPopupListClick(View contextView, int contextPosition, int position) { 
				 switch (position) {
				 case 0://判定出勤
					 
					 CommitOneAttenceInfo(lesson, sid,contextPosition);
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
	
	
	//单人判定出勤
	private void CommitOneAttenceInfo(int lesson, int stuId, final int position){
		if (lesson == -1) {
			Toast.makeText(attenceActivity, "当前不是上课时间!", Toast.LENGTH_SHORT).show();
			return;
		}
		String urlString = SmartCampusUrlUtils.getCommitAttecnceInfoUrl(String.valueOf(InfoReleaseApplication.authenobjData.userId),
				String.valueOf(lesson),String.valueOf(stuId));
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String day = simpleDateFormat.format(calendar.getTime());
		urlString += "&day="+day;
		Log.d(TAG, "urlString=" + urlString);
		showProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								
								absentStudentLists.get(position).attenceType=1;
								 realityStudentLists.add(absentStudentLists.get(position));
								 absentStudentLists.remove(absentStudentLists.get(position));
								 studentList.clear();
								 studentList.addAll(absentStudentLists);
								 studentList.addAll(realityStudentLists);
								
								Toast.makeText(attenceActivity, "上报考勤数据成功!", Toast.LENGTH_SHORT).show();
								gridViewAdapter.notifyDataSetChanged();
								sumAttenceNumTv.setText(studentList.size()+"人");
					    		reallyAttenceNumTv.setText(realityStudentLists.size()+"人");
					    		donotAttenceNumTv.setText(absentStudentLists.size()+"人");
							}
						} catch (JSONException e) {
							Toast.makeText(attenceActivity, "上报考勤数据失败", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
							hideProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if( InfoReleaseApplication.showNetWorkFailed(attenceActivity) == true ){
							Toast.makeText(attenceActivity, "上报考勤数据失败", Toast.LENGTH_SHORT).show();
						}
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						hideProgressDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	//班级考勤数据提交
	private void CommitAttenceInfo(int lesson){
		if(TextUtils.isEmpty(savedConnectedMac)){
			Toast.makeText(attenceActivity, "还未选择班级连接!", Toast.LENGTH_SHORT).show();
			return;
		}
		if (lesson == -1) {
			Toast.makeText(attenceActivity, "当前不是上课时间!", Toast.LENGTH_SHORT).show();
			return;
		}
		if(isRunAttence){
			Toast.makeText(attenceActivity, "当前还未考勤完毕!", Toast.LENGTH_SHORT).show();
			return;
		}
		String urlString = SmartCampusUrlUtils.getCommitAllAttecnceInfoUrl(String.valueOf(lesson), urlGroupId);
		Date date = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeDateFormat = new SimpleDateFormat("hh:mm:ss");
		String day = simpleDateFormat.format(date);
		String time = timeDateFormat.format(date);
		urlString += "&day="+day;
		for(AttendanceBean attendanceBean : realityStudentLists){
			if(!TextUtils.isEmpty(attendanceBean.mac)){
				urlString +="&devtype=1";
				urlString +="&devid="+attendanceBean.mac;
				urlString +="&attdtime="+time;
			}
		}
		Log.d(TAG, "urlString=" + urlString);
		showProgressDialog();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						hideProgressDialog();
						try {
							if (response.getInt("code") == 0) {
								Toast.makeText(attenceActivity, "上报考勤数据成功!", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(attenceActivity, response.optString("msg"), Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							Toast.makeText(attenceActivity, "上报考勤数据失败", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
							hideProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if( InfoReleaseApplication.showNetWorkFailed(attenceActivity) == true ){
							Toast.makeText(attenceActivity, "上报考勤数据失败", Toast.LENGTH_SHORT).show();
						}
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						hideProgressDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	
	private void getCurrentClass(final ArrayList<Integer> classGroupIdList) {
		String urlString = SmartCampusUrlUtils.getCurrentClass();
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);
						if( AttenceStartFragment.this == null ){
							return;
						}
						int code = response.optInt("code");
						int updateIndex = 0;
						if (code == 0) {
							JSONObject data = response.optJSONObject("datas");
							if (data != null) {
								int groupId = data.optInt("groupId");
								// Log.d(TAG, "groupId="+groupId);
								for (int i = 0; i < classGroupIdList.size(); i++) {
									if (classGroupIdList.get(i).intValue() == groupId) {
										updateIndex = i;
										break;
									}
								}
							}
							// Log.d(TAG, "updateIndex="+updateIndex);
						} else if (code == -2) {
							hideProgressDialog();
							InfoReleaseApplication.returnToLogin(attenceActivity);
						} else {
							hideProgressDialog();
						}
						getAllClassStudentListData(classGroupIdList, updateIndex);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if( AttenceStartFragment.this == null ){
							return;
						}
						hideProgressDialog();
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						// 获取当前班级失败，获取所有学生数据
						getAllClassStudentListData(classGroupIdList, 0);
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}
	private void getAllClassStudentListData(ArrayList<Integer> classGroupIdList, int updateIndex) {
		if (classGroupIdList == null)
			return;
		mAllStudentDataList = new ClassStudentData[classGroupIdList.size()];
		AttendanceApplication.mAllStudentDataList=mAllStudentDataList;
		mSelClassIndex = updateIndex;
		// 一次取下所有学生的数据
		for (int i = 0; i < classGroupIdList.size(); i++) {
			mAllStudentDataList[i] = new ClassStudentData();
			mAllStudentDataList[i].groupId = String.valueOf(classGroupIdList.get(i));
			getStudentListData(classGroupIdList.get(i).toString(), i, updateIndex);
		}
	}

	private void getStudentListData(final String groupIds, final int group_index, final int update_index) {// 获取指定班级学生列表
		String urlString = SmartCampusUrlUtils.getStudentListUrl() + "&groupIds=" + groupIds;
//		showProgressDialog();
		mGetStudentListDataNum++;
		Log.d(TAG, "getStudentListData urlString:" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

			
					@Override
					public void onResponse(JSONObject response) {
						if( AttenceStartFragment.this == null ){
							return;
						}
						Log.d(TAG, "response mGetStudentListDataNum:" + mGetStudentListDataNum + ",group_index:"
								+ group_index + ",update_index：" + update_index + "，response=" + response);
						mGetStudentListDataNum--;

						try {
							if (response.getInt("code") == 0) {
								ArrayList<AttendanceBean> studentdatalist = new ArrayList<AttendanceBean>();
								JSONArray array = response.optJSONArray("datas");
								if (array != null) {
									int len = array.length();
									for (int i = 0; i < len; i++) {
										JSONObject obj = (JSONObject) array.get(i);
										AttendanceBean bean = new AttendanceBean(obj);
										studentdatalist.add(bean);
										// Log.d(TAG, "emp name=" +
										// bean.empName+",image save
										// path:"+bean.imgSavePath);

									}
//									downloadStudentImage(studentdatalist);
									mAllStudentDataList[group_index].studentdatalist = studentdatalist;
								}
							} else if (response.getInt("code") == -2) {
								hideProgressDialog();
								InfoReleaseApplication.returnToLogin(attenceActivity);
							} else {
								hideProgressDialog();
								Log.e(TAG, response.getString("msg"));
							}
							// 全部学生数据获取完毕
							if (mGetStudentListDataNum == 0) {
								
								if (mAllStudentDataList[update_index] == null
										|| mAllStudentDataList[update_index].studentdatalist == null
										|| mAllStudentDataList[update_index].studentdatalist.size() == 0) {
//									initStudentList(null, null);
//									reportToast("该班级未录入学生数据!");
								} else {
//									initStudentList(mAllStudentDataList[update_index].studentdatalist, null);
//									getAgentList(mAllStudentDataList[update_index].groupId);
								}
							}

						} catch (JSONException e) {
							e.printStackTrace();
							hideProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						if( AttenceStartFragment.this == null ){
							return;
						}
						hideProgressDialog();
						Log.d(TAG, "onErrorResponse=" + arg0 + ",group_index:" + group_index + ",update_index："
								+ update_index);
						mGetStudentListDataNum--;
						// 全部学生数据获取完毕
						if (mGetStudentListDataNum == 0) {
							
						}
						if (group_index == update_index) {
							// 先判断网络状况
							if (true == InfoReleaseApplication.showNetWorkFailed(attenceActivity)) {
//								reportToast(R.string.get_student_data_failed);
							}
						}
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}
	
	//获取选中的班级
	public BtDevice getSelectBtDevice(){
		BtDevice btDevice = null;
		for(int i=0;i<mBtDevices.size();i++){
			if(mBtDevices.get(i).isChecked()){
				btDevice = mBtDevices.get(i);
			}
		}
		return btDevice;
	}
}
