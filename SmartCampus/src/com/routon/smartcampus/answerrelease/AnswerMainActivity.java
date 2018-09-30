package com.routon.smartcampus.answerrelease;

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
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.BaseFragmentActivity;
import com.routon.edurelease.R;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.json.GroupListData;
import com.routon.inforelease.plan.create.GroupInfo;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.inforelease.util.DataResponse;
import com.routon.smartcampus.answerrelease.service.BluetoothService;
import com.routon.smartcampus.answerrelease.service.Broadcast;
import com.routon.smartcampus.answerrelease.service.BtDevice;
import com.routon.smartcampus.attendance.ClassDeviceListener;
import com.routon.smartcampus.coursetable.CourseDataUtil.TimeTable;
import com.routon.smartcampus.face.FaceRecognizeMgr;
import com.routon.smartcampus.network.SmartCampusUrlUtils;
import com.routon.smartcampus.utils.StudentHelper;

public class AnswerMainActivity extends BaseFragmentActivity implements
		OnClickListener {

	private int classId = 0;
//	private ArrayList<Integer> classGroupIdList;
//	private ArrayList<String> mClassList;
	private static final String TAG = "AnswerMainActivity";
	private ProgressDialog progressDialog;
	private ImageView backMenu;
	private RelativeLayout rlStartAnswer;
	private BluetoothAdapter bluetoothAdapter;
	private AnswerActivityReceiver receiver;
	private IntentFilter filter;
	boolean isDoAnswer = true;
	private int teacherId = 0;
	private ArrayList<StudentBean> studentList = new ArrayList<StudentBean>();
	private boolean stopConnected = false;
	private Timer timer;
	private int timeTad;
	private Timer mTimer;
	private String urlGroupId;
	private String myStuString;
	private boolean foundClass = false;
	private int userId;
	private boolean isDownLoadSuccess = false;

	// 答题数据上传
	Date myDate;
	private static String answerTime = null;
	private static String answerTimeString = null;
	private String answerDate = null;
	private static String nonceTime = null;
	private Calendar calendar;
	private boolean isExist = false;
	private List<TimeTable> timetables;
	private List<String> amCourses;
	private List<String> pmCourses;
	private String currDay;
	private int timeDelay = 0;

	private RecyclerView classRev;
	private ClassDeviceAdapter adapter;
	private ArrayList<BtDevice> mBtDevices;
	private int tidTimes;
	private final int REQUEST_COARSE_LOCATION_PERMISSIONS = 0;
	private static final int ANSWERDELAY = 1;// 结束答题等待3秒
	private static final String ACTION_ANSWER_END = "qa_end";
	private static final String ACTION_BT_CMD = "bt_cmd";
	public static final String ACTION_RECEIVE_S1701_TID = "terminal_id_ack";
	private ImageView refreshBlueTooth;
	private ObjectAnimator rotationAnimator;
	private LinearLayoutManager layoutManager;
	private int centerPosition;
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "服务启动");
		}
	};
	
	private BluetoothDevice mDevice;
	private String mStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 进入答题时不让手机自动休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_answer_main);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// 启动服务
		Intent intent = new Intent(this, BluetoothService.class);
		startService(intent);
		// 实例化获取图片全路径
		FaceRecognizeMgr.getInstance(this);
		initView();
		initData();
		doDiscovery();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	

	private void initData() {
		// TODO Auto-generated method stub
		mBtDevices = new ArrayList<>();
		adapter = new ClassDeviceAdapter(this, mBtDevices);
		timetables = new ArrayList<TimeTable>();
		amCourses = new ArrayList<String>();
		pmCourses = new ArrayList<String>();
		calendar = Calendar.getInstance();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		currDay = df.format(date);
		receiver = new AnswerActivityReceiver();
		filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(Broadcast.BT_CONNECT_STATE_CHANGED);
		filter.addAction(Broadcast.BLUETOOTH_STATE_CHANGED);
		filter.addAction(Broadcast.ACTION_RECEIVE_S1701_TID);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		classRev.setAdapter(adapter);
		setRecycleView();
		getSavedTeacherId();
	}

	private void initView() {
		// TODO Auto-generated method stub

		backMenu = (ImageView) findViewById(R.id.img_anwswer_back);
		classRev = (RecyclerView)findViewById(R.id.rec_class_answer_device_info);
		classRev.setLayoutManager(new LinearLayoutManager(this));
		refreshBlueTooth=(ImageView) findViewById(R.id.img_answer_bluetooth_refresh);
//		setMoveBackEnable(true);
		//刷新蓝牙搜索
		refreshBlueTooth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doDiscovery();
			}
		});
		backMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    Intent stopIntent = new Intent(AnswerMainActivity.this, BluetoothService.class);
	             //调用stopService()方法-传入Intent对象,以此停止服务
	            stopService(stopIntent);
				AnswerMainActivity.this.finish();
				overridePendingTransition(R.animator.slide_in_left,
						R.animator.slide_out_right);
			}
		});
	}

	/* 获取recycleview中当前显示的第一个item的position */
	private int getPosition() {
		layoutManager = (LinearLayoutManager) classRev
				.getLayoutManager();
		int position = layoutManager.findFirstVisibleItemPosition();
		return position;
	}

	private void setRecycleView() {
		final RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
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
                int itemHeight = StudentHelper.dp2px(AnswerMainActivity.this, 40);
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
    	adapter.setClassDeviceListener(new ClassDeviceListener() {
			
			

			@Override
			public void onItemSearchClick(View view, int position) {
				
			}
			
			@Override
			public void onItemClick(View view, int position) {
				BluetoothDevice device = mBtDevices.get(position).getDevice();
				mDevice=device;
				if(device == null){
					Toast.makeText(AnswerMainActivity.this, "没有搜索到该班级蓝牙设备", Toast.LENGTH_SHORT).show();
				}else {
					showMyProgressDialog();
					if (bluetoothAdapter.isDiscovering()) {
						bluetoothAdapter.cancelDiscovery();
					}
					int bondstate = device.getBondState();
					String mac = device.getAddress();
					String status = mBtDevices.get(position).getStatus();
					mStatus=status;
					String name = mBtDevices.get(position).getName();
					Log.d(TAG, "the select name:" + name + " mac:" + mac + " status:" + status + " bondstate:" + bondstate);
					Intent intent = new Intent(Broadcast.ACTION_NOTIFY_SERVICE_CONNNECT);
					intent.putExtra(Broadcast.EXTRA_S1701_MAC, mac);
					intent.putExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS, status);
					intent.putExtra(Broadcast.EXTRA_S1701_BONDSTATE, bondstate);
					sendBroadcast(intent);
				}
			}
		});
    }
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		
		
		Intent stopIntent = new Intent(AnswerMainActivity.this, BluetoothService.class);
        //调用stopService()方法-传入Intent对象,以此停止服务
        stopService(stopIntent);

	}


	// 切换到后台30s后发送结束答题指令并关闭蓝牙连接
//	@Override
//	protected void onStop() {
//		super.onStop();
//		SaveQuestionId(questionId);
//		if (bluetoothAdapter.isDiscovering()) {
//			bluetoothAdapter.cancelDiscovery();
//		}
//		stopConnected = true;
//		Log.d(TAG, "onStop----stopConnected:" + stopConnected);
//		timer = new Timer();
//		timer.schedule(new TimerTask() {
//
//			@Override
//			public void run() {
//				if (stopConnected) {
//					Intent intent = new Intent();
//					intent.setAction(ACTION_BT_CMD);
//					intent.putExtra("data", "action:" + ACTION_ANSWER_END);
//					sendBroadcast(intent);
//					Intent disIntent = new Intent(
//							Broadcast.ACTION_NOTIFY_SERVICE_DISCONNECT);
//					sendBroadcast(disIntent);
//				}
//			}
//		}, 30000);
//	}

	@Override
	protected void onRestart() {
		super.onRestart();
		stopConnected = false;
		Log.d(TAG, "onRestart----stopConnected:" + stopConnected);
	}
	/**
	 * 获取班级信息
	 * */
	private void getClassListData() {

		GroupListData.getClassListData(AnswerMainActivity.this,
				new DataResponse.Listener<ArrayList<GroupInfo>>() {

					@Override
					public void onResponse(ArrayList<GroupInfo> classGroups) {
						// TODO Auto-generated method stub

						for (int i = 0; i < classGroups.size(); i++) {
							mBtDevices.add(new BtDevice(classGroups.get(i).getName(), classGroups.get(i).getId()));
						}				
						if (mBtDevices.size() > 0) {
							for (int i = 0; i < mBtDevices.size(); i++) {
								BtDevice btDevice = mBtDevices.get(i);
								Log.d(TAG, "The " + i + " groupId:" + btDevice.getGroupId());
								Log.d(TAG, "The " + i + " className:" + btDevice.getClassName());
								//CourseTableHelper courseTableHelper = new CourseTableHelper(AttendanceMainActivity.this);
								//getClassTables(groupId, mClassList.get(i), courseTableHelper);
							}
							adapter.notifyDataSetChanged();
						}

					}
				}, new DataResponse.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						Toast.makeText(AnswerMainActivity.this,
								error.toString(), Toast.LENGTH_SHORT).show();
					}
				}, new DataResponse.SessionInvalidListener() {

					@Override
					public void onSessionInvalidResponse() {
						// TODO Auto-generated method stub
						Toast.makeText(AnswerMainActivity.this,
								R.string.session_invalid_msg,
								Toast.LENGTH_SHORT).show();
					}
				});

	}


	/**
	 * 获取班级学生列表
	 * */
	private void getStudentListDataFromUrl(final String groupIds) {// 获取学生列表
		// showMyProgressDialog();
		String urlString = SmartCampusUrlUtils.getStudentListUrl()
				+ "&groupIds=" + groupIds;
		// showMyProgressDialog();
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);

						try {
							if (response.getInt("code") == 0) {
								hideMyProgressDialog();
								// Toast.makeText(getApplicationContext(),
								// "获取学生数据成功!", Toast.LENGTH_SHORT).show();
								isDownLoadSuccess = true;
								if (studentList != null
										&& studentList.size() > 0) {
									studentList.clear();
								}
								JSONArray array = response
										.optJSONArray("datas");
								if (array != null) {
									int len = array.length();
									for (int i = 0; i < len; i++) {
										JSONObject obj = (JSONObject) array
												.get(i);
										StudentBean bean = new StudentBean(obj);
										studentList.add(bean);
									}
								}
								Log.d(TAG, "学生列表:" + studentList.size());
								hideMyProgressDialog();
								Intent intentStart=new Intent(AnswerMainActivity.this,StartAnswerActivity.class);
								Bundle bundle=new Bundle();
								bundle.putParcelableArrayList("studentList", studentList);
								bundle.putInt("classId",classId);
								bundle.putInt("teacherId",teacherId);
								bundle.putParcelable("device", mDevice);
								bundle.putString("status", mStatus);
								intentStart.putExtras(bundle);
								startActivity(intentStart);
								AnswerMainActivity.this.finish();
								// downloadStudentImage(studentdatalist);
								// initStudentList(studentdatalist, null);
							} else if (response.getInt("code") == -2) {

								hideMyProgressDialog();
							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(AnswerMainActivity.this,
										response.getString("msg"),
										Toast.LENGTH_LONG).show();
								hideMyProgressDialog();
							}

						} catch (JSONException e) {
							Toast.makeText(getApplicationContext(), "获取学生数据失败",
									Toast.LENGTH_SHORT).show();
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						hideMyProgressDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance()
				.getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);

	}

	private void showMyProgressDialog() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(AnswerMainActivity.this, "",
					"...loading...");
			progressDialog.setCancelable(true);
		}
	}

	private void hideMyProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	/**
	 * 点击事件监听
	 * */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.rl_startanswer_btn:
			if (studentList == null || studentList.size() == 0) {
				if (isDownLoadSuccess) {
					Toast.makeText(AnswerMainActivity.this,
							"无学生数据，请检查连接的设备是否属于本班级!", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(AnswerMainActivity.this, "读取学生数据中，请稍候!",
							Toast.LENGTH_SHORT).show();
				}

				return;
			}
			// startBtn.setText("开始");
			startTimer();
			rlStartAnswer.setClickable(false);
		}
	}

	/**
	 * 停止抢答
	 * */
	public void StopPreempt() {
		// startBtn.setText("开始");
		Toast.makeText(AnswerMainActivity.this, "抢答结束", Toast.LENGTH_SHORT)
				.show();
	}

	public void StopAnswer(int mode) {
		// startBtn.setText("开始");
		if (mode == 1) {
			Toast.makeText(AnswerMainActivity.this, "答题结束", Toast.LENGTH_SHORT)
					.show();
		} else if (mode == 3) {
			Toast.makeText(AnswerMainActivity.this, "投票结束", Toast.LENGTH_SHORT)
					.show();
		}

	}

	/**
	 * 收到1701 isBusy命令进行处理
	 * */
	public void IsBusy() {
	}

	private void setBtDiscoveryAnim() {
		RotateAnimation mRotateAnimation = new RotateAnimation(0f, 360f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateAnimation.setRepeatMode(RotateAnimation.RESTART);
		mRotateAnimation.setDuration(1000);
		mRotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
		mRotateAnimation.setStartTime(RotateAnimation.START_ON_FIRST_FRAME);
	}

	public void cancleBtDiscoveryAnim() {
	}


	@SuppressLint("Override")
	@Override
	public void onRequestPermissionsResult(int arg0, @NonNull String[] arg1,
			@NonNull int[] grantResults) {
		switch (arg0) {
		case REQUEST_COARSE_LOCATION_PERMISSIONS: {
			if (grantResults.length == 1
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				continueDoDiscovery();
			} else {
				Toast.makeText(this, "授权失败，无法开启扫描蓝牙功能!", Toast.LENGTH_LONG)
						.show();
			}
			return;
		}
		}
	}

	public void doDiscovery() {
		if (Build.VERSION.SDK_INT >= 23) {
			int hasPermission = ActivityCompat.checkSelfPermission(AnswerMainActivity.this,
					Manifest.permission.ACCESS_COARSE_LOCATION);
			if (hasPermission == PackageManager.PERMISSION_GRANTED) {
				continueDoDiscovery();
				return;
			}
			ActivityCompat.requestPermissions(AnswerMainActivity.this,
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
						btDevice.setDevice(null);
						adapter.notifyDataSetChanged();
					}
				}
			}
		}
	}

	class AnswerActivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "action:" + action);

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
			}else if(action.equals(Broadcast.BT_CONNECT_STATE_CHANGED)){
					int status2 = intent.getIntExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS,
							Broadcast.S1701_STATUS_CONNECT_NONE);
					if (status2 != Broadcast.S1701_STATUS_CONNECTED) {
						tidTimes = 0;
					}
			}else if (action.equals(Broadcast.ACTION_BT_CONNECT_STATE_CHANGED)) {
				String btdeviceAddress = intent.getStringExtra("btdevice_address");
				String status = intent.getStringExtra("btdevice_status");
				
				if(status.equals("未连接")){
					tidTimes = 0;
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
				if(status.equals("已连接")&&studentList.size()>0){
//					Intent intentStart=new Intent(AnswerMainActivity.this,StartAnswerActivity.class);
//					Bundle bundle=new Bundle();
//					bundle.putParcelableArrayList("studentList", studentList);
//					intentStart.putExtras(bundle);
//					startActivity(intentStart);
				}
			} else if (action.equals(ACTION_RECEIVE_S1701_TID)) {// 连接
				String myTerminalId = null;// 缓存的终端id
				String terminalId = null;
				String groupId = null;
				String myClassId = null;
				tidTimes++;
				Log.d(TAG, "tidTimes="+tidTimes);
				if(tidTimes >= 2){
					return;
				}
				terminalId = intent.getStringExtra("data");// 获取连接的终端id
				if (terminalId != null) {
					GetclassIdFromUrl(terminalId);
				} else {
					Toast.makeText(AnswerMainActivity.this, "未获取到终端id",
							Toast.LENGTH_SHORT).show();
				}
			}
			else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				Log.d(TAG, "搜索结束");
				if(rotationAnimator!=null){
					rotationAnimator.cancel();
				}
			}else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
				Log.d(TAG, "开始搜索");
				refreshBlueTooth.clearAnimation();
				rotationAnimator = ObjectAnimator.ofFloat(refreshBlueTooth, "rotation", 0, 360);
				rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
				rotationAnimator.setInterpolator(new LinearInterpolator());
				rotationAnimator.setDuration(1000);
				rotationAnimator.start();
			}
		}
	}

	/**
	 * 缓存terminalId
	 * */
	public void SaveClassId(String terminalId, String groupId) {
		SharedPreferences.Editor editor = getSharedPreferences("SaveClassId",
				MODE_PRIVATE).edit();
		editor.putString(terminalId, groupId);
		editor.commit();
	}

	public String GetClassId(String terminalId) {
		SharedPreferences prf = getSharedPreferences("SaveClassId",
				MODE_PRIVATE);
		String classId = null;
		classId = prf.getString(terminalId, null);
		return classId;

	}

	/**
	 * 缓存studentList
	 * */
	public void SaveStudentList(String stu, String terminalId, String groupId) {
		SharedPreferences.Editor editor = getSharedPreferences("SaveStudent",
				MODE_PRIVATE).edit();
		editor.putString(terminalId + groupId + "studentList", stu);
		editor.commit();
	}

	public String GetStudentList(String terminalId, String groupId) {
		SharedPreferences prf = getSharedPreferences("SaveStudent",
				MODE_PRIVATE);
		String stu = null;
		stu = prf.getString(terminalId + groupId + "studentList", null);
		return stu;

	}


	/**
	 * 获取分组id
	 * */

	public void GetclassIdFromUrl(final String terminalId) {
		String urlString = SmartCampusUrlUtils.getClassIdUrl(terminalId);
		Log.d(TAG, "urlString=" + urlString);
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						Log.d(TAG, "response=" + response);

						try {
							if (response.getInt("code") == 0) {
								JSONObject obj = response.optJSONObject("obj");
								if (obj != null) {
									urlGroupId = obj.getString("groupId");
									if (urlGroupId != null) {
										classId = Integer.parseInt(urlGroupId);
										getStudentListDataFromUrl(urlGroupId);
									}

								}
							}else {
								Toast.makeText(AnswerMainActivity.this, response.optString("msg"), Toast.LENGTH_SHORT).show();
								hideMyProgressDialog();
							}
						} catch (JSONException e) {
							Toast.makeText(getApplicationContext(), "获取学生数据失败",
									Toast.LENGTH_SHORT).show();
							e.printStackTrace();
							hideMyProgressDialog();
						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.d(TAG, "onErrorResponse=" + arg0.getMessage());
						hideMyProgressDialog();
					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance()
				.getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
	}

	/**
	 * 注册广播
	 * */
	// 开始计时
	public void startTimer() {
		timeTad = 0;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				timeTad++;

				Message msg = new Message();
				msg.what = ANSWERDELAY;
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
			case ANSWERDELAY:

				timeDelay = msg.arg1;
				if (timeDelay > 3) {
					rlStartAnswer.setClickable(true);
					closeTimer();
				}
			}
		};
	};


	/**
	 * 获取teacherId
	 * 
	 * @param userId
	 * @param type
	 */
	private void getTeacherId(int userId, int type) {
		// TODO Auto-generated method stub
		String urlString = SmartCampusUrlUtils.getCourseNameUrl(
				String.valueOf(userId), String.valueOf(type));
		CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(
				Request.Method.POST, urlString, null,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						Log.d(TAG, "response=" + response);
						try {
							if (response.getInt("code") == 0) {
								JSONObject obj = response
										.getJSONObject("datas");
								teacherId = obj.optInt("sid");
								getSharedPreferences("saved_teacherId",
										Context.MODE_PRIVATE)
										.edit()
										.putInt(String
												.valueOf(AnswerMainActivity.this.userId),
												teacherId).commit();
								Log.d(TAG, teacherId + "老师id");
								getClassListData();
							} else if (response.getInt("code") == -2) {

								InfoReleaseApplication
										.returnToLogin(AnswerMainActivity.this);

							} else {
								Log.e(TAG, response.getString("msg"));
								Toast.makeText(AnswerMainActivity.this,
										"查询老师信息失败!", Toast.LENGTH_SHORT).show();

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e(TAG, "sorry,getTeacherId Error: " + arg0);
						if (InfoReleaseApplication
								.showNetWorkFailed(AnswerMainActivity.this) == true) {
							Toast.makeText(AnswerMainActivity.this,
									"查询老师信息失败!", Toast.LENGTH_SHORT).show();
						}

					}
				});

		jsonObjectRequest.setCookie(HttpClientDownloader.getInstance()
				.getCookie());
		InfoReleaseApplication.requestQueue.add(jsonObjectRequest);
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
		int savedTeacherId = getSharedPreferences("saved_teacherId",
				Context.MODE_PRIVATE).getInt(String.valueOf(userId), 0);
		if (savedTeacherId == 0) {
			getTeacherId(userId, 2);
		} else {
			teacherId = savedTeacherId;
			Log.d(TAG, teacherId + "老师id");// 18327
			getClassListData();
		}
	}
}
