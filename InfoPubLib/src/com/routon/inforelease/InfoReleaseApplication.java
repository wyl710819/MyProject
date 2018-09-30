package com.routon.inforelease;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.routon.widgets.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.routon.inforelease.json.AuthenobjBean;
import com.routon.inforelease.net.UrlUtils;
import com.routon.inforelease.plan.create.LruBitmapCache;

public class InfoReleaseApplication extends Application {
	public static boolean isEduPlatform = false;
	
	//家长版不用心跳
	public static boolean mFamilyVersion = false;
	
	public static final String TAG = "InfoReleaseApplication";
	public static RequestQueue requestQueue;
	public static int memoryCacheSize;
	public static ImageLoader mImageLoader;
	
	public static AuthenobjBean authenobjData;
	public static final String USERINFO = "userinfo";
	public static final String TAG_NAME = "name";
	public static final String TAG_PWD = "pwd";
	public static final String TAG_PARENT_NAME = "ParentPhone";
	public static final String TAG_PARENT_PWD = "ParentVerifyNum";
	public static final String TAG_PORTRAIT = "portrait";
	
	public static final String TAG_IS_PARENT = "is_parent";
	//自动登陆
	public static final String TAG_AUTOCHECK = "auto_ischeck";
	//记住密码
	public static final String TAG_ISCHECK = "ischeck";
	public static final String LOGIN_ACCOUNT_UPDATE = "login_account_update";
	
	public static boolean getClassInfoPrivilege(){
		return isEduPlatform;
	}
	
	public static void saveUserInfo(Activity activity,String name,String pwd){
		if( activity == null  )  return;
		SharedPreferences sharedPreferences = activity.getSharedPreferences(USERINFO, Context.MODE_PRIVATE); //私有数据	
		Editor editor = sharedPreferences.edit();//获取编辑器
		if( name != null ){
			editor.putString(TAG_NAME,name);
		}
		if( pwd != null ){
			editor.putString(TAG_PWD, pwd);
		}
		editor.commit();
	}
	
	public static void saveParentUserInfo(Activity activity,String name,String pwd){
		if( activity == null  )  return;
		SharedPreferences sharedPreferences = activity.getSharedPreferences(USERINFO, Context.MODE_PRIVATE); //私有数据	
		Editor editor = sharedPreferences.edit();//获取编辑器
		editor.putString(TAG_PARENT_NAME,name);
		editor.putString(TAG_PARENT_PWD, pwd);
		editor.putBoolean("ParentFirstLogin_"+name, false);
		editor.commit();
	}
	
	//添加定时器代码，当程序在前台时，10分钟向平台发送一次登陆请求
	private static int HEARTBRAT_TIMER = 600000;
	//启动定时器
	public void startHeartBeatTimer(){
		mHandler.sendEmptyMessageDelayed(0, HEARTBRAT_TIMER);
	}
	
	//关闭定时器
	public void stopHeartBeatTimer(){
		mHandler.removeMessages(0);
	}
	
	private void loginIn(){
		//家长版不需要登录，这里如果是家长版，直接不处理登录流程
		if( mFamilyVersion == true ){
			return;
		}
//		Log.d(TAG,"loginIn");
		SharedPreferences sharedPreferences = this.getBaseContext().getSharedPreferences(InfoReleaseApplication.USERINFO, Context.MODE_PRIVATE); //私有数据
		String name = sharedPreferences.getString(InfoReleaseApplication.TAG_NAME,"");
		String pwd = sharedPreferences.getString(InfoReleaseApplication.TAG_PWD,"");
		HttpClientDownloader.getInstance().loginIn(UrlUtils.getAuthUrl(name,pwd), null, 0);
	}
	
	private Handler mHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	loginIn();
	    	//10分钟向平台发送一次登陆请求
	    	mHandler.sendEmptyMessageDelayed(0, HEARTBRAT_TIMER);
	    }
	};

	private  int mStartActivityCount = 0;  
	@Override
	public void onCreate() {
		super.onCreate();
		// 不必为每一次HTTP请求都创建一个RequestQueue对象，在application中初始化
		requestQueue = Volley.newRequestQueue(this);
		// 计算内存缓存
		memoryCacheSize = getMemoryCacheSize();
		mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new LruBitmapCache()); // 初始化一个loader对象，可以进行自定义配置
		
		if( mFamilyVersion == false ){
			registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {  
		              
		            @Override  
		            public void onActivityStarted(Activity activity) {  
		            	if( mStartActivityCount == 0 ){
		            		Log.v(TAG, ">>>>>>>>>>>>>>>>>>>切到前台  lifecycle");  
		            		//先调用一次登陆接口
		            		loginIn();
		            		startHeartBeatTimer();
		            	}
		            	mStartActivityCount++;
		            }  
		  
		            @Override  
		            public void onActivityResumed(Activity activity) {                
		            }  
		  
		            @Override  
		            public void onActivityPaused(Activity activity) {  
		            }  
		            @Override  
		            public void onActivityStopped(Activity activity) {  
		            	mStartActivityCount--;  
		                if (mStartActivityCount == 0) {  
		                    Log.v(TAG, ">>>>>>>>>>>>>>>>>>>切到后台  lifecycle");  
		                    stopHeartBeatTimer();
		                } else {  
		                }             
		            }  
		  
		            @Override  
		            public void onActivityDestroyed(Activity activity) {  
		            }
	
					@Override
					public void onActivityCreated(Activity arg0, Bundle arg1) {
						// TODO Auto-generated method stub
						
					}
	
					@Override
					public void onActivitySaveInstanceState(Activity arg0,
							Bundle arg1) {
						// TODO Auto-generated method stub
						
					}         
		        });  
		}
	}
	
	public static void returnToLogin(Activity activity){
		returnToLogin(activity,true);
	}
	
	private static ComponentName mLoginComponentName = null;
	public static void setLoginComponent(ComponentName componentName){
		mLoginComponentName = componentName;
	}
	
	//重新登陆
	public static void returnToLogin(Activity activity,boolean sessinValidMsg){
		if( activity == null ){
			return;
		}
		//sessinValidMsg为true，则是会话失效，重新发出登陆请求即可，否则就是跳转到登陆界面
		if( sessinValidMsg == false ){
			//返回登陆界面		
	  		Intent intent = new Intent();
	  		if( mLoginComponentName != null ){
	  			intent.setComponent(mLoginComponentName);
	  		}else{
	  			intent.setClass(activity, LoginActivity.class);
	  		}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivity(intent);
			activity.finish();
		}else{
			SharedPreferences sharedPreferences = activity.getSharedPreferences(InfoReleaseApplication.USERINFO, Context.MODE_PRIVATE); //私有数据
			String name = sharedPreferences.getString(InfoReleaseApplication.TAG_NAME,"");
			String pwd = sharedPreferences.getString(InfoReleaseApplication.TAG_PWD,"");
			HttpClientDownloader.getInstance().loginIn(UrlUtils.getAuthUrl(name,pwd), null, 0);
			Toast.makeText(activity, R.string.session_invalid_msg, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void showNetDataFailedTip(Context context){
		if( context == null ){
			return;
		}
		Toast.makeText(context, R.string.get_netdata_failed, Toast.LENGTH_LONG).show();
	}
	
	//return false 已经处理，return true 不是网络问题，未处理
	public static boolean showNetWorkFailed(Context context){
		if( context == null ){
			return false;
		}
		boolean networkState = HttpClientDownloader.isNetworkConnected(context);
		if( networkState == false ){
			Toast.makeText(context, R.string.network_failed, Toast.LENGTH_LONG).show();
		}
		return networkState;
	}

	/**
	 * @description
	 *
	 * @param context
	 * @return 得到需要分配的缓存大小
	 */
	public int getMemoryCacheSize() {
		// Get memory class of this device, exceeding this amount will throw an
		// OutOfMemory exception.
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		return maxMemory / 10;
	}
}
