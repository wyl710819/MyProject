package com.routon.smartcampus.exchangecourse;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SwapHelper {

	public interface swapAniListener{
		void swapStart();
		void swapFinish();
	}
	/**
	 * 创建拖动的镜像
	 * 
	 * @param bitmap
	 * @param downX
	 *            按下的点相对父控件的X坐标
	 * @param downY
	 *            按下的点相对父控件的X坐标
	 */
	public static void swapViews(final RelativeLayout parentview,final View startview,final View endview,final swapAniListener listener){
		final View maskView = new ImageView(startview.getContext());
		int  screenW =  ((WindowManager) startview.getContext()  
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()  
                .getWidth();  
		int  screenH =  ((WindowManager) startview.getContext()  
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();  
		final RelativeLayout.LayoutParams maskParam = new RelativeLayout.LayoutParams(screenW,screenH);
		maskView.setFocusable(true);
		maskView.setEnabled(true);
		maskView.setClickable(true);
		maskParam.width = screenW;
		maskParam.height = screenH;
		parentview.addView(maskView, maskParam);
		
		final int[] parentLocation = new int[2];
		parentview.getLocationOnScreen(parentLocation);
		
		final int[] location = new int[2];
		startview.getLocationOnScreen(location);
		location[0] = location[0] - parentLocation[0];
		location[1] = location[1] - parentLocation[1];
		final RelativeLayout.LayoutParams startParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		Log.d("SwapHelper","location:"+location[0]);
		startParam.leftMargin = location[0];
		startParam.topMargin = location[1];
		final ImageView startImageView = new ImageView(startview.getContext());
		startview.setDrawingCacheEnabled(true);
		startImageView.setImageBitmap(startview.getDrawingCache());
		parentview.addView(startImageView, startParam);
		
		final int[] endLocation = new int[2];
		endview.getLocationOnScreen(endLocation);
		final RelativeLayout.LayoutParams endParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		endLocation[0] = endLocation[0] - parentLocation[0];
		endLocation[1] = endLocation[1] - parentLocation[1];
		endParam.leftMargin = endLocation[0];
		endParam.topMargin = endLocation[1];
 
		final ImageView endImageView = new ImageView(endview.getContext());
		endview.setDrawingCacheEnabled(true);
		endImageView.setImageBitmap(endview.getDrawingCache());
		parentview.addView(endImageView, endParam);
		
		startview.setVisibility(View.INVISIBLE);
		endview.setVisibility(View.INVISIBLE);
		
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
		valueAnimator.setDuration(800).start();
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
		       @Override
		       public void onAnimationUpdate(ValueAnimator animation) {
		              Float value = (Float) animation.getAnimatedValue();
		              startParam.leftMargin = (int) (location[0] + (endLocation[0]-location[0])*value);
		              startParam.topMargin = (int) (location[1] + (endLocation[1]-location[1])*value);
		              parentview.updateViewLayout(startImageView, startParam);
		              endParam.leftMargin = (int) (endLocation[0] + (location[0]-endLocation[0])*value);
		              endParam.topMargin = (int) (endLocation[1] + (location[1]-endLocation[1])*value);
		              parentview.updateViewLayout(endImageView, endParam);
		       }
		});
		valueAnimator.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				// TODO Auto-generated method stub
				parentview.removeView(endImageView);
				parentview.removeView(maskView);
				parentview.removeView(startImageView);
				endview.setDrawingCacheEnabled(false);
				startview.setDrawingCacheEnabled(false);
				endview.setVisibility(View.VISIBLE);
				startview.setVisibility(View.VISIBLE);
				if( listener != null ){
					listener.swapFinish();
				}
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				// TODO Auto-generated method stub
				if( listener != null ){
					listener.swapStart();
				}
			}
			
		});

	}

}
