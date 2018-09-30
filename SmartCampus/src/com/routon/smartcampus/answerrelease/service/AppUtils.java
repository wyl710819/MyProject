package com.routon.smartcampus.answerrelease.service;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;

public class AppUtils {
	
	private final static String TAG = "AppUtils";
	
	/**  
     * 判断某一Service是否正在运行  
     *  
     * @param context     上下文  
     * @param serviceName Service的全路径： 包名 + service的类名  
     * @return true 表示正在运行，false 表示没有运行  
     */  
    public static boolean isServiceRunning(Context context, String serviceName) {  
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);  
        if (runningServiceInfos.size() <= 0) {  
            return false;  
        }  
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {  
            if (serviceInfo.service.getClassName().equals(serviceName)) {  
                return true;  
            }  
        }  
        return false;  
    }  
    
    /**
    * 判断某个界面是否在前台 
    * 
    * @param activity 要判断的Activity 
    * @return 是否在前台显示 
    */  
   public static boolean isForeground(Activity activity) {  
       return isForeground(activity, activity.getClass().getName());  
   }  
 
   /** 
    * 判断某个界面是否在前台 
    * 
    * @param context   Context 
    * @param className 进程名
    * @return 是否在前台显示 
    */  
   public static boolean isForeground(Context context, String className) {  
	   ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
       List<RunningAppProcessInfo> runnings = am.getRunningAppProcesses();
       for(RunningAppProcessInfo running : runnings){
           if(running.processName.equals(className)){
               if(running.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
                       || running.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE){
                   //前台显示
            	   return true;
               }
               else{
                   //后台显示
            	   return false;
               }
           }
       }
       return false;
   }
}
