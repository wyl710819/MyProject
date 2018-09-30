package com.routon.edurelease.receiver;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author sj
 * @version 2018年1月11日 上午8:07:42
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String TYPE = "type"; //这个type是为了Notification更新信息的，这个不明白的朋友可以去搜搜，很多
    
    public final String TAG = "NotificationBroadcastReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int type = intent.getIntExtra(TYPE, -1);

        if (type != -1) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(type);
        }

        if (action.equals("notification_clicked")) {
            //处理点击事件
        	Log.i(TAG, "------------notification_clicked");
        }
        
        if (action.equals("notification_cancelled")) {
            //处理滑动清除和点击删除事件
        	Log.i(TAG, "------------notification_cancelled");
        }
    }
    
    
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        //判断app进程是否存活
//        if(SystemUtils.isAppAlive(context, "com.routon.edurelease")){
//            //如果存活的话，就直接启动DetailActivity，但要考虑一种情况，就是app的进程虽然仍然在
//            //但Task栈已经空了，比如用户点击Back键退出应用，但进程还没有被系统回收，如果直接启动
//            //DetailActivity,再按Back键就不会返回MainActivity了。所以在启动
//            //DetailActivity前，要先启动MainActivity。
//            Log.i("NotificationReceiver", "the app process is alive");
//            Intent mainIntent = new Intent(context, MyLoginActivity.class);
//            //将MainAtivity的launchMode设置成SingleTask, 或者在下面flag中加上Intent.FLAG_CLEAR_TOP,
//            //如果Task栈中有MainActivity的实例，就会把它移到栈顶，把在它之上的Activity都清理出栈，
//            //如果Task栈不存在MainActivity实例，则在栈顶创建
//            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            Intent detailIntent = new Intent(context, MainActivity.class);
//
//            Intent[] intents = {mainIntent, detailIntent};
//
//            context.startActivities(intents);
//        }else {
//            //如果app进程已经被杀死，先重新启动app，将DetailActivity的启动参数传入Intent中，参数经过
//            //SplashActivity传入MainActivity，此时app的初始化已经完成，在MainActivity中就可以根据传入             //参数跳转到DetailActivity中去了
//            Log.i("NotificationReceiver", "the app process is dead");
//            Intent launchIntent = context.getPackageManager().
//                    getLaunchIntentForPackage("com.routon.edurelease");
//            launchIntent.setFlags(
//                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//            context.startActivity(launchIntent);
//        }
//    }

}
