package com.routon.smartcampus.guideview;

import com.routon.inforelease.util.ImageUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MyMaskView extends RelativeLayout implements View.OnKeyListener, View.OnClickListener{
	 private View mTargetView = null;
	 private int mMaskAlpha = 150;
	 private int mExitAnimationId = -1;
	 private int mEnterAnimationId = -1;
	 public static interface OnVisibilityChangedListener {
		    void onShown();
		    void onDismiss();
	 }
	 private OnVisibilityChangedListener mOnVisibilityChangedListener = null;
	 
	 private ImageView mFocusView = null;
	 public MyMaskView(Context context) {
	     super(context);
	     
	     mFocusView = new ImageView(context);
	 }
	 
	 public void setTargetView(View view){
		 mTargetView = view;
	 }
	 
	 public void setOnVisibilityChangedListener(OnVisibilityChangedListener listener){
		 mOnVisibilityChangedListener = listener;
	 }
	 
	 public void setAlpha(int alpha) {
		 mMaskAlpha = alpha;
	 }
	 
	 public void setAnimations(int showAni,int exitAni){
		 mExitAnimationId = exitAni;
		 mEnterAnimationId = showAni;
	 }
	 
	 public void dismiss(){
		 final ViewGroup vp = (ViewGroup) this.getParent();
	     if (vp == null) {
	      return;
	     }
		 if ( mExitAnimationId != -1 ) {
		      // mMaskView may leak if context is null
		      Context context = this.getContext();
		      assert context != null;

		      Animation anim = AnimationUtils.loadAnimation(context, mExitAnimationId);
		      assert anim != null;
		      anim.setAnimationListener(new Animation.AnimationListener() {
		        @Override 
		        public void onAnimationStart(Animation animation) {

		        }

		        @Override 
		        public void onAnimationEnd(Animation animation) {
		          vp.removeView(MyMaskView.this);
		          if (mOnVisibilityChangedListener != null) {
		            mOnVisibilityChangedListener.onDismiss();
		          }
		        }

		        @Override public void onAnimationRepeat(Animation animation) {

		        }
		      });
		      this.startAnimation(anim);
		    } else {
		      vp.removeView(this);
		      if (mOnVisibilityChangedListener != null) {
		        mOnVisibilityChangedListener.onDismiss();
		      }
		    }
	 }
	 
	 public void show(Activity activity) {
		 int bgColor = Color.argb(mMaskAlpha, 0, 0, 0);
		 this.setBackgroundColor(bgColor);
		 
		 Bitmap bitmap = ImageUtils.getBitmapFromView(mTargetView);
		 
		 Rect rect = new Rect();
		 mTargetView.getGlobalVisibleRect(rect);
		 
		 int[] location = new int[2];
		 mTargetView.getLocationInWindow(location);
		 
		 int offsetX = rect.left - location[0];
		 int offsetY = rect.top - location[1];
		 
		 Bitmap newBitmap = null;
		 if( bitmap != null ){
			 newBitmap = Bitmap.createBitmap(bitmap, offsetX, offsetY, rect.width(), rect.height());  
		 }
		 
//		 Log.d("MyMaskView","location[1]:"+location[1]+",rect top:"+rect.top);
		 
		 ViewGroup content = (ViewGroup) activity.findViewById(android.R.id.content);
		 if (this.getParent() == null) {
		      content.addView(this);
		 }
		 
		// For removing the height of status bar we need the root content view's
		    // location on screen
	    int parentX = 0;
	    int parentY = 0;
	    final int[] loc = new int[2];
	    content.getLocationInWindow(loc);
	    parentY = loc[1];//通知栏的高度
	    if (parentY == 0) {
	      Class<?> localClass;
	      try {
	        localClass = Class.forName("com.android.internal.R$dimen");
	        Object localObject = localClass.newInstance();
	        int i5 =
	            Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
	        parentY = activity.getResources().getDimensionPixelSize(i5);
	      } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	      } catch (IllegalAccessException e) {
	        e.printStackTrace();
	      } catch (InstantiationException e) {
	        e.printStackTrace();
	      } catch (NumberFormatException e) {
	        e.printStackTrace();
	      } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	      } catch (SecurityException e) {
	        e.printStackTrace();
	      } catch (NoSuchFieldException e) {
	        e.printStackTrace();
	      }
	    }
	    
		
	    rect.offset(-parentX, -parentY);
		LayoutParams params = new RelativeLayout.LayoutParams(rect.width(),rect.height());
		params.leftMargin = rect.left;
		params.topMargin = rect.top;
		mFocusView.setImageBitmap(newBitmap);
		
		mFocusView.setOnClickListener(this);
		 
		this.addView(mFocusView, params);
		
		if (mEnterAnimationId != -1) {
	        Animation anim = AnimationUtils.loadAnimation(activity, mEnterAnimationId);
	        assert anim != null;
	        anim.setAnimationListener(new Animation.AnimationListener() {
	          @Override public void onAnimationStart(Animation animation) {

	          }

	          @Override public void onAnimationEnd(Animation animation) {
	            if (mOnVisibilityChangedListener != null) {
	              mOnVisibilityChangedListener.onShown();
	            }
	          }

	          @Override public void onAnimationRepeat(Animation animation) {

	          }
	        });
	        this.startAnimation(anim);
	     } else {
	        if (mOnVisibilityChangedListener != null) {
	          mOnVisibilityChangedListener.onShown();
	        }
	     }
	 }
	 
	 @Override 
	 public boolean onKey(View v, int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {		    
	        dismiss();
	        return true;
	    }
	    return false;
	  }

	  @Override 
	  public void onClick(View v) {
	      dismiss();
	  }
}
