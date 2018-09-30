package com.routon.inforelease.offline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.routon.widgets.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.routon.common.CustomTitleActivity;
import com.routon.inforelease.HttpClientDownloader;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;
import com.routon.inforelease.TerminalListHelper;
import com.routon.inforelease.json.PlanListrowsBean;
import com.routon.inforelease.json.TerminalListBean;
import com.routon.inforelease.json.TerminalListBeanParser;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.ScannerActivity;
import com.routon.inforelease.plan.create.velloyExpand.CookieJsonRequest;
import com.routon.remotecontrol.BluetoothChatService;
import com.routon.remotecontrol.adapter.util.AdapterManager;
import com.routon.remotecontrol.adapter.util.BluetoothDeviceNew;
import com.routon.remotecontrol.adapter.util.TouchObject;
import com.routon.remotecontrol.listener.SearchDeviceBtnClickListener;
import com.routon.remotecontrol.receiver.ScanBluetoothReceiver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;


public class DeviceSelectActivity extends CustomTitleActivity implements OnItemClickListener {
    /**
     * Called when the activity is first created.
     */
    public String TAG = "DeviceSelectActivity";
    public static final int REQUEST_ENABLE = 10000;   //打开蓝牙    请求码
    private static final int REQUEST_QRCODE_SCAN = 2;
    private AdapterManager mAdapterManager;     //Adapter管理器
    private BluetoothAdapter mBluetoothAdapter;
    private TouchObject mTouchObject;       //当前操作对象
    ListView mDeviceListView;

    BluetoothChatService mChatService;

    private boolean mFilterMsg = false;
    
//    private List<TerminalListdatasBean> mTerminalList = new ArrayList<TerminalListdatasBean>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
        	mBluetoothAdapter.enable();
        }
     
        mChatService = BluetoothChatService.getInstance();
        mChatService.init(this, myHandler);

        mTouchObject = new TouchObject();
      
        //实例化Adapter管理器并设置到Application
        mAdapterManager = new AdapterManager(this);

        initViews();

        //findBonedDevice();        
        
        String curConnectMac = mChatService.getConnectedAddress();
        Log.v(TAG,"onCreate curConnectMac "+curConnectMac);
        if (curConnectMac != null) {
	        mTouchObject.mConnectDevice = curConnectMac;
	        updateDeviceConnectStatus(mTouchObject.mConnectDevice, BluetoothChatService.MESSAGE_DEVICE_CONNECT_OK);
        }
        
        //pair & connect
        initPairFilter();
        
//        getTerminalList();
        findBonedDevice();
    }
    
//    private void getTerminalList() {
//    	String urlString = UrlUtils.getTerminalListUrl(1, 1, 200, null, null, null, null, null, null);
//		Log.d(TAG, "URL:" + urlString);
//  
//        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "正在获取终端列表……");  
//  
//        CookieJsonRequest jsonObjectRequest = new CookieJsonRequest(  
//                Request.Method.GET, urlString, null, new Response.Listener<JSONObject>() {  
//                    @Override  
//                    public void onResponse(JSONObject response) {  
//                        Log.d(TAG, "response="+response);  
//                        if (progressDialog.isShowing() && progressDialog != null) {  
//                            progressDialog.dismiss();  
//                        }
//						try {
//							TerminalListBean bean = TerminalListBeanParser.parseTerminalListBean(response);
//							if (bean == null || bean.datas == null || bean.datas.size() == 0) {
//								return;
//							}
//							
//							mTerminalList.addAll(bean.datas);
//							
//							findBonedDevice();
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}                        
//                    }  
//                },   
//                new Response.ErrorListener() {  
//                    @Override  
//                    public void onErrorResponse(VolleyError arg0) {  
//                    	Log.e(TAG, "sorry,Error"); 
//                    	Toast.makeText(DeviceSelectActivity.this, "网络连接失败!", Toast.LENGTH_LONG).show();
//                    	if (progressDialog.isShowing() && progressDialog != null) {  
//                            progressDialog.dismiss();  
//                        }  
//                    }  
//                });  
//        
//        jsonObjectRequest.setCookie(HttpClientDownloader.getInstance().getCookie());
//        InfoReleaseApplication.requestQueue.add(jsonObjectRequest); 
//    }
    
    private void initViews() {
        mDeviceListView = (ListView) findViewById(R.id.deviceListView);
        findViewById(R.id.btn_scan_qrcode).setOnClickListener(mOnBtnClickedListener);
        findViewById(R.id.searchDeviceBtn).setOnClickListener(mOnBtnClickedListener);

		// title bar		
		this.initTitleBar("请选择要发送的终端");
		this.setTitleNextImageBtnClickListener(R.drawable.next, mOnBtnClickedListener);

        mDeviceListView.setAdapter(mAdapterManager.getDeviceListAdapter());

        //添加监听器
        mDeviceListView.setOnItemClickListener(this);
    }

    void updateDeviceConnectStatus(String device,int msgCode)
    {
        Message msg = myHandler.obtainMessage(msgCode);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatService.DEVICE_ADDRESS, device);
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }
    @Override
    protected void onResume() {
    	Log.v(TAG,"onResume");
        super.onResume();
        mFilterMsg = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterFilter();
//      if (mChatService != null)
//      mChatService.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //release
        mFilterMsg = true;

    }


    private void unregisterFilter() {
        if (mPairStateChangeReceiver != null) {
            unregisterReceiver(mPairStateChangeReceiver);
            mPairStateChangeReceiver = null;
        }
    }

    private void initPairFilter() {
        // 注册Receiver来获取蓝牙设备相关的结果
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mPairStateChangeReceiver, intent);
    }

    private Handler myHandler = new Handler() {


        public void handleMessage(Message msg) {

            if (mFilterMsg) {
                Log.v(TAG, "devicename handleMessage  mFilterMsg ");
                return;
            }
            switch (msg.what) {
                case BluetoothChatService.MESSAGE_DEVICE_CONNECT_OK: {
                    Bundle data = msg.getData();
                    String devicename = data.getString(BluetoothChatService.DEVICE_ADDRESS);
                    Log.v(TAG, "devicename connected " + devicename);
                    mTouchObject.mConnectDevice = devicename;
                    mTouchObject.mConnectStatus = TouchObject.CONNECT_OK;
                    mAdapterManager.setDeviceStatus(mTouchObject);
                    mAdapterManager.updateDeviceAdapter();
                    mTouchObject.mLstConnctedDevice = devicename;
//                    mySendMessage("test");
                }

                break;
                case BluetoothChatService.MESSAGE_DEVICE_CONNECT_FAIL: {
                    Bundle data = msg.getData();
                    String devicename = data.getString(BluetoothChatService.DEVICE_ADDRESS);
                    Log.v(TAG, "devicename fail " + devicename);
                    mTouchObject.mConnectDevice = devicename;
                    mTouchObject.mConnectStatus = TouchObject.CONNECT_PAIR;
                    mAdapterManager.setDeviceStatus(mTouchObject);
                    mAdapterManager.updateDeviceAdapter();
                    break;
                }
                
                case BluetoothChatService.MESSAGE_DEVICE_CONNECTTING: {
                    Bundle data = msg.getData();
                    String devicename = data.getString(BluetoothChatService.DEVICE_ADDRESS);
                    Log.v(TAG, "devicename connecting " + devicename);
                    mTouchObject.mConnectDevice = devicename;
                    mTouchObject.mConnectStatus = TouchObject.CONNECT_ING;
                    mAdapterManager.setDeviceStatus(mTouchObject);
                    mAdapterManager.updateDeviceAdapter();
                    break;
                } 
                case BluetoothChatService.MESSAGE_DEVICE_CONNECT_LOST: {
                    Bundle data = msg.getData();
                    String devicename = data.getString(BluetoothChatService.DEVICE_ADDRESS);
                    Log.v(TAG, "devicename lost " + devicename);
                    break;
                }
                default:
                    Log.v(TAG, "case " + msg.what);
//                    Log.v(TAG,"connect fail "+);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private BroadcastReceiver mPairStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	Log.v(TAG, "pair state change action: " + action);
            //取得状态改变的设备，更新设备列表信息 （配对状态）
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            mAdapterManager.changeDevice(mTouchObject.clickDeviceItemId, device);
            mAdapterManager.updateDeviceAdapter();
//            mTouchObject.clickDeviceItemId=0;
            if( device == null ) return;
            switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDING:
                    Log.d("BlueToothTestActivity", "正在配对......");
                    break;
                case BluetoothDevice.BOND_BONDED:
                    Log.d("BlueToothTestActivity", "完成配对");
                    // 连接设备
                    //添加连接设别的代码
//                    Message msg=new Message();
//                    mHandler.sendEmptyMessage(BOND_STATE_OK);

                    break;
                case BluetoothDevice.BOND_NONE:
                    Log.d("BlueToothTestActivity", "取消配对");
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE) {
            //请求为 "打开蓝牙"
            if (resultCode == RESULT_OK) {
                //打开蓝牙成功
                findBonedDevice();
                beginDiscovery();
            } else {
                //打开蓝牙失败
                Toast.makeText(this, "打开蓝牙失败！", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 0) {
            if (resultCode == 0) {
                findBonedDevice();
                //mSearchDeviceBtnClickListener.beginDiscovery();
            }
        } else if (requestCode == REQUEST_QRCODE_SCAN) {
        	if (resultCode == RESULT_OK) {
        		String mac = data.getStringExtra("blueMac");
        		String termId = data.getStringExtra("termId");

        		checkAuthorizationWthBtMac(mac, termId);
        		
//        		findBonedDevice();
//        		mChatService.connectBlueMACDevice(mac);
//        		finish();
        	}
        }
    }
    
    private void startAdOfflineReleaseActivity(String btMac,String termId){
    	Intent intent = new Intent(this, AdOfflineReleaseActivity.class);
		intent.putExtra("blueMac", btMac);
		intent.putExtra("termId", termId);
		intent.putExtra("start_by", "scanner");
		intent.putExtra("data", this.getIntent().getSerializableExtra("data"));
		intent.putExtra("path", this.getIntent().getStringExtra("path"));
		intent.putExtra("type", this.getIntent().getIntExtra("type", 0));        	
		intent.putExtra("plan_id", this.getIntent().getStringExtra("plan_id"));
		intent.putExtra("plan_name", this.getIntent().getStringExtra("plan_name"));
		startActivity(intent);
		
		this.finish();
    }
    
    private void checkAuthorizationWthBtMac(final String btMac, final String termId) {
//    	if (!hasAuth(btMac, termId)) {
//    		Toast.makeText(this, "您无权管理这台终端", Toast.LENGTH_SHORT).show();
//    		return;
//    	}
    	Log.d(TAG,"checkAuthorizationWthBtMac btMac:"+btMac+",termId:"+termId);
    	TerminalListHelper.getTerminalAuth(this,btMac,termId,true,new TerminalListHelper.Response() {
			
			@Override
			public void onSuccess(String text, TerminalListdatasBean data) {
				// TODO Auto-generated method stub
				startAdOfflineReleaseActivity(btMac,termId);
			}
			
			@Override
			public void onFailed(String text) {
				// TODO Auto-generated method stub
				Toast.makeText(DeviceSelectActivity.this, text, Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onError(String text) {
				// TODO Auto-generated method stub
				Toast.makeText(DeviceSelectActivity.this, text, Toast.LENGTH_SHORT).show();
			}
		});
		
	}

    /**
     * Finding Boned Devices
     */

    public void findBonedDevice() {

        if (null == mBluetoothAdapter) {
            //取得蓝牙适配器
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        mAdapterManager.clearDevice();
        mAdapterManager.updateDeviceAdapter();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        //获取可配对蓝牙设备  
        Set<BluetoothDevice> device = mBluetoothAdapter.getBondedDevices();

        if (device.size() > 0) { //存在已经配对过的蓝牙设备
            for (BluetoothDevice btd : device) {
            	if( btd.getAddress() != null && btd.getName() != null ){
	            	BluetoothDeviceNew dev = new BluetoothDeviceNew();
	            	TouchObject Obj = new TouchObject();
	            	Obj.mConnectDevice = btd.getAddress();
	            	Obj.mConnectStatus = TouchObject.CONNECT_PAIR;
	            	dev.mDevice = btd;
	            	dev.mTObj = Obj; 
	            	Log.v(TAG,"add paired :"+Obj.mConnectDevice+"name:"+btd.getName());
	                mAdapterManager.addBonedDevice(dev);
	                mAdapterManager.updateDeviceAdapter();
            	}
            }
        }
    }

//    /**
//     * 改变按钮显示文字
//     */
//
//    public void changeSearchBtnText() {
//        mSearchDeviceBtn.setText("重新搜索");
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        mTouchObject.clickDeviceItemId = position;
        mTouchObject.bluetoothDevice = (BluetoothDeviceNew) mAdapterManager.getDeviceListAdapter().getItem(position);
        Log.i(TAG, mTouchObject.bluetoothDevice.getName() + mTouchObject.bluetoothDevice + " position=" + position);
        BluetoothDeviceNew btDevNew = mTouchObject.bluetoothDevice;
        BluetoothDevice btDev = btDevNew.mDevice;
        if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
            //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
            Method createBondMethod = null;
            try {
                Boolean returnValue = false;
                createBondMethod = BluetoothDevice.class
                        .getMethod("createBond");

                Log.d("BlueToothTestActivity", "开始配对");
                try {
                    returnValue = (Boolean) createBondMethod.invoke(btDev);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }


        } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
//            connect(btDev);
        	
        //	Log.v(TAG,"mChatService.stop()");
            if (mChatService != null) {
                mChatService.stop();
            }
      //      Log.v(TAG,"mChatService.stop() over");
        
            updateDeviceConnectStatus(mTouchObject.mConnectDevice, BluetoothChatService.MESSAGE_DEVICE_CONNECT_FAIL);
//            mChatService.connect(btDev, true);
//            mTouchObject.mConnectDevice = btDev.getAddress();
            //update lst 

            mTouchObject.mConnectDevice = btDev.getAddress();
            mChatService.connectBlueMACDevice(btDev.getAddress());
			updateDeviceConnectStatus(mTouchObject.mConnectDevice, BluetoothChatService.MESSAGE_DEVICE_CONNECTTING);
	
//            mTouchObject.mConnectDevice = btDev.getAddress();
//            mChatService.connect(btDev, true);
//            myHandler.post(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					 BluetoothDevice btDev = mTouchObject.;
//				  	}
//			});
          
      

        }
//		Intent mIntent = new Intent(MainActivity.this,KeyboardActivity.class);
//		startActivityForResult(mIntent, 0);
    }

	private ScanBluetoothReceiver mScanBluetoothReceiver;  //蓝牙扫描监听器
	private AlertDialog mAlertDialog;   //确定打开蓝牙 dialog
	private ProgressDialog mProgressDialog;

    private void startSearch() {
			//清空蓝牙设备列表
			mAdapterManager.clearDevice();
			mAdapterManager.updateDeviceAdapter();
			if(null == mBluetoothAdapter){
				//取得蓝牙适配器
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			}
			if(!mBluetoothAdapter.isEnabled()){
				//蓝牙未打开, 打开蓝牙
				if(null == mAlertDialog){
					mAlertDialog = new AlertDialog.Builder(DeviceSelectActivity.this)
											.setTitle("打开蓝牙")
											.setPositiveButton("确定", new Dialog.OnClickListener(){
												@Override
												public void onClick(DialogInterface dialog,
														int which) {
													//发送请求，打开蓝牙
													Intent startBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
													startActivityForResult(startBluetoothIntent, 0);
												}
					
											})
											.setNeutralButton("取消", new Dialog.OnClickListener(){

												@Override
												public void onClick(DialogInterface dialog,
														int which) {
													mAlertDialog.dismiss();
												}
					
											}).create();
				}
				mAlertDialog.setMessage("蓝牙未打开，是否打开？");
				mAlertDialog.show();
			}else {
				//蓝牙已打开， 开始搜索设备
			//	Log.d("fdddgdfg", "qqqqq");
				beginDiscovery();
				new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.i("MYLOG","cancelDiscovery");
						mBluetoothAdapter.cancelDiscovery();
					}}, 30000);
				Log.i("BluetoothDemo", "begin");
			}
		}
	
	/**
	 * 开始搜索设备...
	 */
	public void beginDiscovery() {
		if(null == mProgressDialog){
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("搜索设备中...");
		}
		mProgressDialog.show();
		
		findBonedDevice();
	
		//注册蓝牙扫描监听器
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
   
//		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);     
//		intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);     
//		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//		intentFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
		
		if(null == mScanBluetoothReceiver){
			mScanBluetoothReceiver = new ScanBluetoothReceiver();
		}
		this.registerReceiver(mScanBluetoothReceiver, intentFilter);
		mBluetoothAdapter.startDiscovery();//
	}
	
	private View.OnClickListener mOnBtnClickedListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if( v.getId() == R.id.next_step ){
				if (mChatService.getConnectedAddress() != null && mAdapterManager.getDeviceListAdapter().getCount() > 0) {
					
					Intent intent = new Intent(DeviceSelectActivity.this, AdOfflineReleaseActivity.class);
					intent.putExtra("path", getIntent().getStringExtra("path"));
					intent.putExtra("data", getIntent().getSerializableExtra("data"));
	        		intent.putExtra("type", getIntent().getIntExtra("type", 0));
	        		intent.putExtra("start_by", "deviceSelect");
					intent.putExtra("blueMac", mChatService.getConnectedAddress());
					intent.putExtra("plan_id", getIntent().getStringExtra("plan_id"));
					intent.putExtra("plan_name", getIntent().getStringExtra("plan_name"));
			
						        		
					startActivity(intent);
					finish();
				} else {
					reportToast("请先连接设备或扫描二维码");
				}
			}else if( v.getId() == R.id.btn_scan_qrcode ){
				Intent intent = new Intent(DeviceSelectActivity.this, ScannerActivity.class);
//				startActivity(intent);
				//finish();
				startActivityForResult(intent, REQUEST_QRCODE_SCAN);
			}else if( v.getId() == R.id.searchDeviceBtn ){
				startSearch();
			}
		}
	};
	
	class ScanBluetoothReceiver extends BroadcastReceiver {
		
		public ScanBluetoothReceiver(){
		}

		@Override
		public void onReceive(Context context, final Intent intent) {
			if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
				//扫描到蓝牙
				//取得扫描到的蓝牙，添加到设备列表，更新列表
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				BluetoothDeviceNew dev = new BluetoothDeviceNew();
	        	TouchObject Obj = new TouchObject();
	        	Obj.mConnectDevice = bluetoothDevice.getAddress();
	   
	        	dev.mDevice = bluetoothDevice;
	        	dev.mTObj = Obj; 
	        	
	        	BluetoothChatService service = BluetoothChatService.getInstance(); 
	        	String device = service.getConnectedAddress();
	        	
				if(bluetoothDevice.getBondState() ==BluetoothDevice.BOND_BONDED )
				{

					if (device != null && device.equals(bluetoothDevice.getAddress()))
					{
						Obj.mConnectStatus = TouchObject.CONNECT_OK;
					}else
					{
						Obj.mConnectStatus = TouchObject.CONNECT_PAIR;
					}
			     	
					mAdapterManager.addBonedDevice(dev);
					Log.i("ScanBluetoothReceiver", "ACTION_FOUND PAIRED "+bluetoothDevice.getName());
				}
				else
				{
					Obj.mConnectStatus = TouchObject.CONNECT_NO_PAIR;
					mAdapterManager.addDevice(dev);
					Log.i("ScanBluetoothReceiver", "ACTION_FOUND NO PAIR "+bluetoothDevice.getName());
				}
				
//				Log.v("ScanBluetoothReceiver","add paired :"+Obj.mConnectDevice+"name:"+btd.getName());
				mAdapterManager.updateDeviceAdapter();
			}else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
				//扫描设备结束
				Log.i("ScanBluetoothReceiver", "bluetooth discovery is over");
				if (null != mProgressDialog){
					mProgressDialog.dismiss();
				}
				//取消监听
				unregisterReceiver(this);
			}else
			{
				Log.i("ScanBluetoothReceiver", "other action:"+intent.getAction());
			}
		}

	}

//	private String getLast4TermId(String str) {
//		if (str != null && str.length() >= 4) {
//			return str.substring(str.length() - 4);
//		}
//		
//		return null;
//	}
}