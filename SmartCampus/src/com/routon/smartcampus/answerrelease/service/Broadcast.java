package com.routon.smartcampus.answerrelease.service;

public class Broadcast {
	public static final String ACTION_SERVICE_START = "attenceqa.service.start";
	
	//蓝牙开始扫描
	public static final String BT_START_DISCOVERY = "answer_bt_start_discovery";
	
	// 系统重启后的广播
	public static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
	
	// 蓝牙状态的更改广播
	public static final String BLUETOOTH_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED";
	
	// 蓝牙连接状态变化广播
	public static final String BLE_CONNECT_STATE = "com.routon.ble.connect.state";
	
	public static final String RECEIVE_ATTENCEQA_ACTION = "bt_cmd";
	
	public static final String ATTENCE_START_ACTION = "attence_start";
	
	public static final String ATTENCE_REPORT_ACTION = "attence_report";
	
	public static final String ATTENCE_STOP_ACTION = "attence_finish";
	
	//考勤中,答题结束
	public static final String ATTENCE_ON_QA_FINISH_ACTION = "attence_on_qa_finish";
	
	public static final String QA_START_ACTION = "qa_start";
	
	public static final String QA_REPORT_ACTION = "qa_report";
	
	public static final String QA_STOP_ACTION = "qa_stop";
	
	public static final String QA_ACTIVITY_START_ACTION = "qa_activity_start";
	
	//答题页面在前台
	public static final String QA_IS_IN_FOREGROUND="qa_activity_foreground";	
	//蓝牙连接状态变化广播
	public static final String BT_CONNECT_STATE_CHANGED = "com.routon.action.s1701_connect_status";
	//蓝牙连接状态变化字段
	public static final String EXTRA_S1701_CONNECT_STATUS = "extra_s1701_connect_status";
	//设置应用查询连接状态广播
	public static final String ACTION_QUERY_S1701_CONNECT_STATUS = "com.routon.action.query_s1701_connect_status";
	
	public static final int S1701_STATUS_CONNECT_NONE = 0;  // 未连接
	public static final int S1701_STATUS_CONNECTING = 1; // 连接中
	public static final int S1701_STATUS_CONNECT_FAILED = 2; // 连接失败
	public static final int S1701_STATUS_CONNECTED = 3; // 已连接
	
	public static final String QA_STOP_SEND = "3";//通知答题应用答题结束
	//通知服务进行连接操作
	public static final String ACTION_NOTIFY_SERVICE_CONNNECT = "com.routon.action.s1701.connect";
	//蓝牙配对状态字段
	public static final String EXTRA_S1701_BONDSTATE = "extra_s1701_bondstate";
	//蓝牙地址字段
	public static final String EXTRA_S1701_MAC = "extra_s1701_mac";
	//服务通知页面更新状态
	public static final String ACTION_BT_CONNECT_STATE_CHANGED = "com.routon.action.s1701_connect_status_changed";
	//答题应用切换到后台时通知服务关闭蓝牙连接
	public static final String ACTION_NOTIFY_SERVICE_DISCONNECT = "com.routon.action.s1701.disconnect";
	
	//向S1701查询终端ID
	public static final String ACTION_QUERY_S1701_TID = "action:terminal_id_req";
	
	//S1701返回终端ID
	public static final String ACTION_RECEIVE_S1701_TID = "terminal_id_ack";
	
	//通知蓝牙自动连接
	public static final String ACTION_AUTO_CONNECT = "com.routon.action.autoconnect";
	
	//自动连接的S1701名称
	public static final String EXTRA_AUTO_CONNECT_S1701 = "extra_autoconnect_s1701";
	
}
