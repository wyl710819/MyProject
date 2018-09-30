package com.routon.common;

import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import com.routon.widgets.Toast;

import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.R;

public class BaseFragmentActivity extends FragmentActivity { 
    //手指上下滑动时的最小速度
    private static final int YSPEED_MIN = 1000;

    //滑动时的最小距离
    private static final int XDISTANCE_MIN = 80;

    //上滑或下滑时的最小距离
    private static final int YDISTANCE_MIN = 100;

    //按下时的横坐标。
    private float xDown;

    //按下时的纵坐标。
    private float yDown;

    //移动时的横坐标。
    private float xMove;

    //移动时的纵坐标。
    private float yMove;

    //用于计算手指滑动的速度。
    private VelocityTracker mVelocityTracker;

	private boolean isBack;
	private boolean mMoveBackEnable = false;
	
	private View mTouchUnDealView = null;
	
	public void setTouchUnDealView(View view){
		mTouchUnDealView = view;
	}
	
	public void setMoveBackEnable(boolean enable){
		mMoveBackEnable = enable;
	}

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	 if( mMoveBackEnable == false ){
    		 return super.dispatchTouchEvent(event);
    	 }
    	 if( mTouchUnDealView != null ){
    		 Rect r = new Rect();
    		 mTouchUnDealView.getGlobalVisibleRect(r);
    		 if( r.contains((int)(event.getX()), (int)(event.getY())) ){
    			 return super.dispatchTouchEvent(event);
    		 }
    	 }
         createVelocityTracker(event);
         switch (event.getAction()) {
         case MotionEvent.ACTION_DOWN:
               xDown = event.getRawX();
               yDown = event.getRawY();
               break;
         case MotionEvent.ACTION_MOVE:
               xMove = event.getRawX();
               yMove= event.getRawY();
               //滑动的距离
               int distanceX = (int) (xMove - xDown);
               int distanceY= (int) (yMove - yDown);
               //获取顺时速度
               int ySpeed = getScrollVelocity();
               //关闭Activity需满足以下条件：
               if(distanceX > XDISTANCE_MIN &&(distanceY<YDISTANCE_MIN&&distanceY>-YDISTANCE_MIN)&& ySpeed < YSPEED_MIN) {
            	   isBack = true;
            	   finish();
               }
               break;
         case MotionEvent.ACTION_UP:
               recycleVelocityTracker();
               break;
         default:
               break;
         }
         return isBack ? false : super.dispatchTouchEvent(event);
   }
    
	@Override    
    public void onBackPressed() {    
        super.onBackPressed();    
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);         
    }  

   
   private void createVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
               mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
   }

   
   private void recycleVelocityTracker() {
         mVelocityTracker.recycle();
         mVelocityTracker = null;
   }

   
   private int getScrollVelocity() {
         mVelocityTracker.computeCurrentVelocity(1000);
         int velocity = (int) mVelocityTracker.getYVelocity();
         return Math.abs(velocity);
   }
   
   protected void returnToLogin() {
		InfoReleaseApplication.returnToLogin(this);
   }
	
	protected void reportToast(int resId) {
		String text = this.getResources().getString(resId);
		reportToast(text);
	}
	
	protected void reportToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
}
