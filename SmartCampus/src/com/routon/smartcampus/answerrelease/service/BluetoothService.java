package com.routon.smartcampus.answerrelease.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.routon.widgets.Toast;

public class BluetoothService extends Service{
	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothDevice bondDevice;
	public static BluetoothSocket mSocket;
	private AnotherConnectThread mAnotherConnectThread;
	private ConnectedThread mConnectedThread;
	public Handler mHandler;
	public Context context;
	private IntentFilter intentFilter;
	private BluetoothReceiver receiver;
	private String s1701Mac;
	private String s1701Status;
	private int s1701Bondstate;
	private SharedPreferences preferences;
	
	private int totalAnswer = 0;//收到的答案数据个数或考勤数据个数
	public static boolean flag = false;//蓝牙连接成功标志
	public byte[] mReceiveBytes;//缓存数据
	
	public static final String SERVICE_NAME = "com.routon.attenceqaservice";
	public static final String EXTRA_COMMAND = "command";
	public static final int START_SERVICE = 1;
	public static final int RECEIVE_OK = 1;
	public static final String S1701_IS_ATTENCE_FINISH = "attence_finish";
	public static final String S1701_BUSY_TIMEOUT = "S1701 is running another work,overtime!";
	public static final String S1701_IS_RUNNING = "S1701 is running now!";
	public static final String S1701_IS_ANSWER_FINISH = "qa_finish";
	private static final UUID S_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private final String TAG = "BluetoothService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Log.d(TAG, "服务启动成功");
			Log.d(TAG, mBluetoothAdapter.getAddress());
		} 
		else 
		{
			Log.d(TAG, "请打开蓝牙");
			turnOnBluetooth();
		}
		preferences = getSharedPreferences("save_mac", MODE_PRIVATE);
		context = this;
		mHandler = new MyHandler();
		receiver = new BluetoothReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction(Broadcast.BLUETOOTH_STATE_CHANGED);
		intentFilter.addAction(Broadcast.RECEIVE_ATTENCEQA_ACTION);
		intentFilter.addAction(Broadcast.ACTION_NOTIFY_SERVICE_CONNNECT);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Broadcast.ACTION_NOTIFY_SERVICE_DISCONNECT);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(receiver != null)
		{
			unregisterReceiver(receiver);
		}
		cancleConnectThread();
	}
	
	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Utils.READ_FINISH) {
				int infoLen = msg.arg1 - 1;// 减去CMD的一个字节后面就是获取得到的info的字节长度
				Log.i(TAG, "infoLen is " + infoLen);
				BluetoothSocket socket = (BluetoothSocket) msg.obj;
				Bundle bundle = msg.getData();
				byte[] readData = bundle.getByteArray("data");
				Log.d(TAG, "info:"+new String(readData,0,msg.arg1)+" info_length:"+readData.length);
				byte cmd = readData[0];
				Log.d(TAG, "cmd:"+cmd);
				byte[] info = getInfo(readData,infoLen, 1);// 新协议是不存储info字节流的长度的,直接去读取info的内容
				if (info == null) {
					Log.i(TAG, "info is null!");
					return;
				}
				Log.d(TAG, "new_info:"+new String(info)+" new_info_length:"+info.length);
				try {
					handleCmd(cmd, info, socket);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "UFT-8 is not supportEncoding!!");
					e.printStackTrace();
				}
			}  
			else if (msg.what == Utils.ROUTON_GENERAL_ACK) {
				totalAnswer = 0;
				Bundle bundle = (Bundle) msg.obj;
				byte[] data = bundle.getByteArray("data");
				Log.d(TAG, "\n");
				Log.d(TAG, "接收的广播数据是:" + new String(data));
				byte cmd = (byte) 0xFE;
				byte[] writeData = setSendData(cmd, data);
				mReceiveBytes = writeData;
				Log.i(TAG, "mReceiveBytes:"+new String(mReceiveBytes));
				if(mConnectedThread != null)
					mConnectedThread.write(writeData);
				else Log.d(TAG, "连接断开");
			}
			else if(msg.what == Utils.LOST_BONDED_S1701)
			{
				Log.d(TAG, "配对取消，断开连接");
				cancleConnectThread();
			}
			else if (msg.what == Utils.CONNECT_STATE_CHANGED) {
				boolean isConnect = (Boolean) msg.obj;
				Intent sendBtStatusIntent = new Intent(Broadcast.BT_CONNECT_STATE_CHANGED);
				if(isConnect)
				{
					Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show();
					sendBtStatusIntent.putExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS, Broadcast.S1701_STATUS_CONNECTED);
				}
				else
				{	
					Toast.makeText(context, "连接断开", Toast.LENGTH_SHORT).show();
					sendBtStatusIntent.putExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS, Broadcast.S1701_STATUS_CONNECT_NONE);
				}
				sendBroadcast(sendBtStatusIntent);
			}
		}
	}
	
	private boolean turnOnBluetooth() {
		Log.d(TAG, "本机蓝牙没有开启，开启中。。。！");
		if (mBluetoothAdapter.enable() == false) {
			Log.d(TAG, "本机蓝牙开启失败！！！！！");
			return false;
		}
		Log.d(TAG, "本机蓝牙开启成功！！！！");
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				mBluetoothAdapter.startDiscovery();
			}
		}, 2000);
		return true;
	}
	
	 /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class AnotherConnectThread extends Thread {
    	private final int TRY_TIMES = 100;
        private  BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private int trytime = 0;
        private boolean mConnected = false;
        private boolean cancel = false;
        private boolean isReconnected = false;
        public AnotherConnectThread(BluetoothDevice device) {
            mmDevice = device;
            upDateBluetoothDevice(mmDevice.getAddress(), "连接中");
        }
        
        public AnotherConnectThread(BluetoothDevice device,boolean isReconnected) {
            mmDevice = device;
            this.isReconnected = isReconnected;
            upDateBluetoothDevice(mmDevice.getAddress(), "连接中");
        }

        synchronized void user_cancel()
        {
        	cancel = true;
        }
        
        synchronized boolean is_canceled()
        {
        	return cancel == true;
        }
        
        public void run() {
        	Log.d(TAG, "Begin ConnectThread");
            while (trytime < TRY_TIMES) {
                if (is_canceled())
                {
                	break;
                }
                synchronized (BluetoothService.this) {
                	BluetoothSocket tmp = null;
                	try {
        				if(Build.VERSION.SDK_INT>=10)
        					tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(S_UUID);
        				else tmp = mmDevice.createRfcommSocketToServiceRecord(S_UUID);
        			} catch (Exception e) {
        				Log.d(TAG, "Error creating socket");
        			}
                    mmSocket = tmp;
                }
                // Always cancel discovery because it will slow down a connection
                if(mBluetoothAdapter.isDiscovering())
                {
                	mBluetoothAdapter.cancelDiscovery();
                }
                Log.i(TAG, "trytime:" + trytime);
                if( mmSocket == null ){
                	break;
                }
                // Make a connection to the BluetoothSocket
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();
                } catch (IOException e) {
                    // Close the socket
                    try {
                        Log.e(TAG, "connect failed");
                        mmSocket.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "unable to close() socket during connection failure", e2);
                    }
                    if (is_canceled())
                    {
                    	Log.e(TAG, "is_canceled()");
                    	break;
                    }
                    else
                    {
                        trytime++;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        continue;
                    }
                }
                if (trytime < TRY_TIMES)
                {
                	Message msMessage = new Message();
                	msMessage.what = Utils.CONNECT_STATE_CHANGED;
                	msMessage.obj = true;
                	mHandler.sendMessage(msMessage);
                    mConnected = true;
                    flag = true;
                    upDateBluetoothDevice(mmDevice.getAddress(), "已连接");
                    preferences.edit().putString("S1701_name", mmDevice.getName()).commit();//保存默认配对的S1701的名字
					preferences.edit().putString("S1701_mac", mmDevice.getAddress()).commit();//保存默认配对的S1701的Mac地址
                    break;
                }
                else
                {
                	Message msMessage = new Message();
                	msMessage.what = Utils.CONNECT_STATE_CHANGED;
                	msMessage.obj = false;
                	mHandler.sendMessage(msMessage);
                    mConnected = false;
                    flag = false;
                    upDateBluetoothDevice(mmDevice.getAddress(), "未连接");
                }

            }
            // Reset the ConnectThread because we're done
   
            synchronized (BluetoothService.this) {
                mAnotherConnectThread = null;
            }
            if (!mConnected||is_canceled())
            {
                Log.d(TAG, "连接失败!");
                upDateBluetoothDevice(mmDevice.getAddress(), "未连接");
            }else {
                // Start the connected thread
                connected(mmSocket, mmDevice,isReconnected);
            }
            Log.d(TAG,"ConnectThread over!!!");
        }

		public void cancel() {

			user_cancel();
			if (mmSocket == null)
				return;

			synchronized (BluetoothService.this) {

				if (mAnotherConnectThread != null) 
				{
					mAnotherConnectThread.interrupt();
				}
				try 
				{
					Log.e(TAG, "cancel close");
					if (mmSocket != null) {
						mmSocket.close();
					}
				} catch (IOException e) {
					Log.e(TAG, "close() of connect socket failed", e);
				}
			}
		}
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothDevice mmdevice;
		private boolean cancel = false;
        public ConnectedThread(BluetoothSocket socket,BluetoothDevice dev,boolean isReconnected) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mmdevice = dev;
            if(isReconnected)
            {
            	if(mReceiveBytes != null)
        		{
        			write(mReceiveBytes);
        			try {
    					Log.d(TAG, "发送缓存命令:"+new String(mReceiveBytes,1,mReceiveBytes.length-1));
    				} catch (Exception e) {
    					// TODO: handle exception
    				}
        		}
            }
            else {
				queryTerminalId();
			}
        }

        synchronized void user_cancel()
        {
        	cancel  = true;
        }
        
        synchronized boolean is_canceled()
        {
        	return cancel == true;
        }

		public void run() {
			Log.i(TAG, "Begin ConnectedThread");
			byte[] mByte = new byte[1024];
			int len;
			// Keep listening to the InputStream while connected
			while (true) {
				try {
					while ((len = mmInStream.read(mByte)) != -1) {
						Log.d(TAG, "\n");
						Log.d(TAG, "Remote device "+ mmSocket.getRemoteDevice().getName());
						Log.d(TAG, "Receive from remote device:"+ new String(mByte, 0, len));
						byte[] a = "#".getBytes();//byte[] a只有1位长度 
						Log.d(TAG, "#:"+a);
						int total = 0;
						for (int i = 0; i < len; i++) {
							if (mByte[i] == a[0]) {
								Message msg = mHandler.obtainMessage();
								msg.what = Utils.READ_FINISH;
								msg.arg1 = i + 1 - total;
								msg.obj = mmSocket;
								byte[] sendByte = new byte[i + 1 - total];
								for (int j = 0; j < i + 1 - total; j++) {
									sendByte[j] = mByte[j + total];
								}
								Log.d(TAG, new String(sendByte, 0,sendByte.length));
								Bundle bundle = new Bundle();
								bundle.putByteArray("data", sendByte);
								msg.setData(bundle);
								mHandler.sendMessage(msg);
								total = i + 1;
							}
						}
					}
				} catch (IOException e) {
					Message msMessage = new Message();
					msMessage.what = Utils.CONNECT_STATE_CHANGED;
                	msMessage.obj = false;
                	mHandler.sendMessage(msMessage);
					Log.e(TAG, "disconnected", e);
					upDateBluetoothDevice(mmdevice.getAddress(), "未连接");
					flag = false;
					if (is_canceled()) {
						break;
					} else {
						reConnect(mmdevice);
					}
					break;
				}
			}
			Log.d(TAG, "ConnectedThread over!!!");
		}

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                if(mReceiveBytes == buffer)
				{
					Log.i(TAG, "remove mReceiveBytes:"+new String(buffer));
					mReceiveBytes = null;
				}
                Log.d(TAG, "传递数据:" + String.valueOf(buffer[0]) + "-"
						+ new String(buffer, 1, buffer.length - 1));
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                reConnect(mmdevice);
            }
        }

        public void cancel() {
        	user_cancel();
        	if( mmSocket == null ) return;
            try {
            	Log.e(TAG, "ConnectedThread close");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,boolean isReconnected) {
        // Cancel the thread that completed the connection
        if (mAnotherConnectThread != null) {mAnotherConnectThread.cancel(); mAnotherConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket,device,isReconnected);
        mConnectedThread.start();
    }
    
    private void reConnect(BluetoothDevice device)
    {
    	connect(device, true);
    	
    }
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private synchronized void connect(BluetoothDevice device, boolean isReconnected) {
        // Cancel any thread attempting to make a connection
        if (mAnotherConnectThread != null) {mAnotherConnectThread.cancel(); mAnotherConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mAnotherConnectThread = new AnotherConnectThread(device,isReconnected);
        mAnotherConnectThread.start();
    }
    
 // 暂定以UTF-8进行传输,看后期调试的时候会不会出现编码的问题
 	public void handleCmd(byte cmd, byte[] info, BluetoothSocket socket) throws UnsupportedEncodingException {
 		// cmd为命令字
 		int key = (int) (cmd & 0xFF);// 一个字节存储的int类型,那么就不用去区分大头和小头
 		switch (key) {
 		case 0xFE:// 透传命令,本服务不做处理,直接发送广播出去:
 			// info的内容为action:XXX;data:XXX
 			String infos = new String(info, "utf-8");
 			Log.d(TAG, "\n");
 			Log.i(TAG, "infos is " + infos);
 			String actionWithName = infos.split("\\;")[0];
 			if (actionWithName == null)
 				return;
 			Log.i(TAG, "actionWithName is " + actionWithName);

 			if (!actionWithName.split("\\:")[0].equals("action") || actionWithName.split("\\:").length != 2) {
 				return;
 			}

 			String action = actionWithName.split("\\:")[1];
 			if (action == null)// 说明这个时候得不到action的值,那么就应该去断掉本次socket链接(更改下方案，暂时先不处理)
 			{
 				return;
 			}
 			Log.i(TAG, "action is " + action);
 			Intent intent = new Intent(action);
 			// 直接传输字节流,不必将其转换为String类型再将其传输出去
 			String dataWithName = null;
 			if (infos.length() > actionWithName.length())
 				dataWithName = infos.substring(actionWithName.length() + 1);
 			if (dataWithName == null || dataWithName.split("\\:").length == 1) {
 				Log.d(TAG, "data is null");
 				if(action.equals(S1701_IS_ATTENCE_FINISH) || action.equals(S1701_IS_ANSWER_FINISH)){
 					sendBroadcast(intent);
 					break;
 				}
 			} else {
 				String dataWithoutName = dataWithName.substring(dataWithName.split("\\:")[0].length() + 1);
 				Log.d(TAG, "data is " + dataWithoutName);
 				if(action.equals(S1701_IS_ATTENCE_FINISH) || action.equals(S1701_IS_ANSWER_FINISH)){
 					intent.putExtra("data", dataWithoutName);
 					sendBroadcast(intent);
 					break;
 				}
 			}
 			if (dataWithName != null && dataWithName.split("\\:").length != 1) {
 				if (action.equals(Broadcast.QA_REPORT_ACTION)) {
 					totalAnswer++;
 					Log.d(TAG, "The "+totalAnswer+" answer");
 					String data = dataWithName.substring(dataWithName.split("\\:")[0].length()+1);
 					intent.putExtra("data",data);
 				}
 				else if(action.equals(Broadcast.ATTENCE_REPORT_ACTION))
				{
 					totalAnswer++;
					Log.d(TAG, "The "+totalAnswer+" attence");
					String dataWithoutName = dataWithName.substring(dataWithName.split("\\:")[0].length() + 1);
					int length = dataWithoutName.split("\\&").length;
					if(length > 1){
						String macWithName = dataWithoutName.split("\\&")[1];
						String mac = macWithName.split("\\=")[1];
						Log.d(TAG, "attence mac="+mac);
						intent.putExtra("data",mac);	
					}else{
						String mac = dataWithName.substring(dataWithName.split("\\:")[0].length() + 1);
						Log.d(TAG, "attence mac="+mac);
						intent.putExtra("data",mac);	
					}
				}
 				else if(action.equals(Broadcast.ACTION_RECEIVE_S1701_TID))
 				{
 					String tid = dataWithName.substring(9);
 					Log.d(TAG, "tid:"+tid);
 					intent.putExtra("data", tid);
 				}
 			} 
 			else 
 			{
 				if(action.equals(Broadcast.QA_REPORT_ACTION)&&dataWithName != null && dataWithName.split("\\:").length == 1)
 				{
 					Log.d(TAG, "答题结束");
 					totalAnswer = 0;
 					/*Intent qaEnd = new Intent(Broadcast.QA_STOP_ACTION);
 					sendBroadcast(qaEnd);*/
 					return;
 				}
 				else if(action.equals(Broadcast.ATTENCE_REPORT_ACTION)&&dataWithName != null && dataWithName.split("\\:").length == 1)
				{
					Log.d(TAG, "考勤结束");
					totalAnswer = 0;
					Intent attenceEnd = new Intent(Broadcast.ATTENCE_STOP_ACTION);
					sendBroadcast(attenceEnd);
					return;
				}
 			}
 			sendBroadcast(intent);
 			break;
 		default:
 			break;
 		}
 	}
    
 	private class BluetoothReceiver extends BroadcastReceiver
 	{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, action);
			if (action.equals(Broadcast.BLUETOOTH_STATE_CHANGED)) 
			{
				Log.d(TAG, "bluetooth state_change.....");
				int i = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
				Log.d(TAG, "bluetoothstate is : " + i);
				if (i == BluetoothAdapter.STATE_ON) {
					Log.d(TAG, "bluetooth is on.....");
				} else if (i == BluetoothAdapter.STATE_OFF) {
					Log.d(TAG, "bluetooth is off.....restarting ibeacon.....");
					turnOnBluetooth();
				}
			}
			
			else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) 
			{
				bondDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
				int oldState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
				Log.d(TAG, "bondState = "+bondState+"   oldState="+oldState);
				if(bondState == BluetoothDevice.BOND_BONDED/*&&device.getName().contains(targetBtName)*/)
				{
					Log.d(TAG, "配对成功");
					if(bondDevice.getAddress().equals(s1701Mac))
					{
						Log.d(TAG, "开始连接");
						preferences.edit().putString("S1701_name", bondDevice.getName()).commit();//保存默认配对的S1701的名字
						preferences.edit().putString("S1701_mac", mBluetoothDevice.getAddress()).commit();//保存默认配对的S1701的Mac地址
						mAnotherConnectThread = new AnotherConnectThread(mBluetoothDevice);
						mAnotherConnectThread.start();
					}
				}
				else if(bondState == BluetoothDevice.BOND_NONE)
				{
					Log.d(TAG, "配对失败");
					upDateBluetoothDevice(bondDevice.getAddress(), "未连接");
				}
			}
			
			else if(action.equals(Broadcast.RECEIVE_ATTENCEQA_ACTION))
			{
				// 增加对应用程序透明传输返回数据的广播监听
				Message msg = mHandler.obtainMessage();
				msg.what = Utils.ROUTON_GENERAL_ACK;// 200代表着透明传输对返回数据的直接传出处理
				Bundle bundle = new Bundle();
				byte[] data = intent.getStringExtra("data").getBytes();
				Log.d(TAG,"data:"+new String(data));
				if (data == null)
				{
					Log.d(TAG, "data is null!!");
					return;
				}
				bundle.putByteArray("data", data);
				msg.obj = bundle;
				mHandler.sendMessage(msg);
			}
			
			else if(action.equals(Broadcast.ACTION_NOTIFY_SERVICE_CONNNECT))
			{
				s1701Mac = intent.getStringExtra(Broadcast.EXTRA_S1701_MAC);
				s1701Status = intent.getStringExtra(Broadcast.EXTRA_S1701_CONNECT_STATUS);
				s1701Bondstate = intent.getIntExtra(Broadcast.EXTRA_S1701_BONDSTATE, 0);
				Log.d(TAG, "connect mac:"+s1701Mac+" status:"+s1701Status+" bondstate:"+s1701Bondstate);
				if(TextUtils.isEmpty(s1701Mac))
				{
					Log.d(TAG, "连接设备mac地址为空");
					return;
				}
				mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(s1701Mac);	
				if(s1701Status.equals("已连接"))
				{
					cancleConnectThread();
					upDateBluetoothDevice(s1701Mac, "未连接");
				}
				else if(s1701Status.equals("连接中"))
				{
					cancleConnectThread();
					upDateBluetoothDevice(s1701Mac, "未连接");
				}
				else if(s1701Status.equals("未连接"))
				{
					cancleConnectThread();
					upDateBluetoothDevice(s1701Mac, "连接中");
					if(s1701Bondstate == BluetoothDevice.BOND_BONDED)
					{
						connectS1701();
					}
					else if(s1701Bondstate == BluetoothDevice.BOND_NONE)
					{
						startPairing();
					}
					else if(s1701Bondstate == BluetoothDevice.BOND_BONDING)
					{
						boolean unpairedFlag = false;
						try {
							unpairedFlag = ClsUtils.cancelBondProcess(mBluetoothDevice.getClass(), mBluetoothDevice);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if(unpairedFlag == true)
						{
							Log.d(TAG, "unpaired succedd");
							startPairing();
						}
						else {
							Log.d(TAG, "unpaired failed");
						}
					}
				}
			}
			
			else if(action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Broadcast.ACTION_NOTIFY_SERVICE_DISCONNECT))
			{
				cancleConnectThread();
			}
		}
 		
 	}
 	
 	
	public byte[] setSendData(byte cmd, byte[] info) {
		int len = 0;
		if (info == null)
			return null;
		else
			len = info.length;

		byte[] writeData = new byte[len + 1];
		writeData[0] = cmd;
		for (int i = 1; i < writeData.length; i++) {
			writeData[i] = info[i - 1];
		}
		return writeData;

	}
	
	// 得到附加的信息
	// data为获取数据的来源
	// len是获取新的字节流的长度
	// offset是原数据获取的偏移量
	public byte[] getInfo(byte[] data, int len, int offset) {
		if (len <= 1) {
			return null;
		}
		byte[] info = new byte[len - 1];
		for (int i = 0; i < len - 1; i++) {
			info[i] = data[i + offset];
		}
		return info;

	}

	/**
	 * 更新设备连接状态给BluetoothFragment
	 * @param status
	 */
	public void upDateBluetoothDevice(String mac, String status)
	{
		Intent intent = new Intent(Broadcast.ACTION_BT_CONNECT_STATE_CHANGED);
		intent.putExtra("btdevice_address", mac);
		intent.putExtra("btdevice_status", status);
		sendBroadcast(intent);
	}
	
	/**
	 * 与制定mac设备连接，当前设备是绑定状态
	 * @param mac mac地址
	 */
	public void connectS1701()
	{
		Log.d(TAG, "开始连接");
		mAnotherConnectThread = new AnotherConnectThread(mBluetoothDevice);
		mAnotherConnectThread.start();
	}
	
	/**
	 * 停止当前连接线程
	 */
	public void cancleConnectThread()
	{
		if(mAnotherConnectThread != null)
		{
			mAnotherConnectThread.cancel();
			mAnotherConnectThread = null;
		}
		if(mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
	}
	
	/**
	 * 蓝牙配对操作
	 * @return 配对状态
	 */
	@SuppressLint("NewApi") public boolean startPairing() {
		if (!mBluetoothDevice.createBond()) {
			Log.d(TAG, "create bond failed");
			return false;
		}
		return true;
	}
	
	/**
	 * 向S1701查询终端ID
	 */
	public void queryTerminalId()
	{
		Log.d(TAG, "query s1701 tid");
		Intent intent = new Intent(Broadcast.RECEIVE_ATTENCEQA_ACTION);
		intent.putExtra("data", Broadcast.ACTION_QUERY_S1701_TID);
		sendBroadcast(intent);
	}
}
