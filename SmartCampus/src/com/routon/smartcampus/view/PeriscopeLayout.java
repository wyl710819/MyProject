package com.routon.smartcampus.view;

import java.util.Random;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.routon.inforelease.InfoReleaseApplication;
import com.routon.inforelease.plan.PlanListFragment;
import com.routon.inforelease.widget.BitmapCache;
import com.routon.edurelease.R;
import com.routon.smartcampus.bean.StudentBean;
import com.routon.smartcampus.student.StudentListFragment;
import com.routon.smartcampus.utils.FlowerUtil;
import com.squareup.picasso.Picasso;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PeriscopeLayout extends RelativeLayout {

    private Interpolator line = new LinearInterpolator();
    private Interpolator acc = new AccelerateInterpolator();
    private Interpolator dce = new DecelerateInterpolator();
    private Interpolator accdec = new AccelerateDecelerateInterpolator();
    private Interpolator[] interpolators;

    private int mHeight;
    private int mWidth;
    private LayoutParams lp;
    private Random random = new Random();

    private int dHeight;
    private int dWidth;
	private float endX;
	private float endY;
	private float viewWidth;
	private Context mContext;
	private int mBadgeCount;

    public PeriscopeLayout(Context context) {
        super(context);
        init();
    }

    public PeriscopeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PeriscopeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
//
//    public PeriscopeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init();
//    }

    private void init() {

    	
        interpolators = new Interpolator[4];
        interpolators[0] = line;
        interpolators[1] = acc;
        interpolators[2] = dce;
        interpolators[3] = accdec;

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }


    public void addHeart(float x, float y,float viewWidth, Context context, String imgUrl, int badgeCount,String name, StudentBean bean) {
    	
    	this.endX=x;
    	this.endY=y;
    	this.viewWidth=viewWidth;
    	this.mContext=context;
    	this.mBadgeCount=badgeCount;

    	
    	mImageLoader = new ImageLoader(InfoReleaseApplication.requestQueue, new BitmapCache(mContext));
    	
//    	Drawable img = getResources().getDrawable(R.drawable.pl_blue);
//        dHeight = img.getIntrinsicHeight();
//        dWidth = img.getIntrinsicWidth();
        lp = new LayoutParams(60, 60);
    	
        ImageView imageView = new ImageView(getContext());
        
        FlowerUtil.loadFlower(mContext, imageView, name,imgUrl);
//        ImageListener listener = ImageLoader.getImageListener(imageView, 0, 0);  
//		mImageLoader.get(imgUrl, listener);
//        Picasso.with(getContext()).load(imgUrl).into(imageView);
		
//        imageView.setImageDrawable(img);
        lp.setMargins((int)endX-40, (int)endY-150, 0, 0);
        imageView.setLayoutParams(lp);

        addView(imageView);

        Animator set = getAnimator(imageView);
        set.addListener(new AnimEndListener(imageView, bean));
        set.start();
    }

    private Animator getAnimator(View target) {
        AnimatorSet set = getEnterAnimtor(target);

        ValueAnimator bezierValueAnimator = getBezierValueAnimator(target);

        AnimatorSet finalSet = new AnimatorSet();
//        finalSet.playSequentially(set);
        finalSet.playSequentially(set, bezierValueAnimator);
//        finalSet.setInterpolator(interpolators[random.nextInt(4)]);
        finalSet.setTarget(target);
        return finalSet;
    }

    private AnimatorSet getEnterAnimtor(final View target) {

         
        ObjectAnimator alpha = ObjectAnimator.ofFloat(target, View.ALPHA, 0.2f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.2f, 1f);
        AnimatorSet enter = new AnimatorSet();
        enter.setDuration(1);
        enter.setInterpolator(new LinearInterpolator());
        enter.playTogether(alpha, scaleX, scaleY);
//        target.setAlpha(1 - animation.getAnimatedFraction());
        enter.setTarget(target);
        return enter;
    }

    private ValueAnimator getBezierValueAnimator(View target) {

        BezierEvaluator evaluator = new BezierEvaluator(getPointF(2), getPointF(1));

        ValueAnimator animator = ValueAnimator.ofObject(evaluator,  new PointF(random.nextInt((int) ((endX-20+viewWidth)-(endX-viewWidth)))+(endX-viewWidth), 0),new PointF(endX-40, endY-150));
        animator.addUpdateListener(new BezierListener(target));
        animator.setTarget(target);
        animator.setDuration(600);
        return animator;
    }

    /**
     * ��ȡ�м������ ��
     *
     * @param scale
     */
    private PointF getPointF(int scale) {

        PointF pointF = new PointF();
      
		pointF.x = random.nextInt((int) ((endX-20+viewWidth)-(endX-viewWidth)))+(endX-viewWidth);
        pointF.y = random.nextInt((int) endY)/scale ;
        return pointF;
    }

    private class BezierListener implements ValueAnimator.AnimatorUpdateListener {

        private View target;

        public BezierListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            PointF pointF = (PointF) animation.getAnimatedValue();
            target.setX(pointF.x);
            target.setY(pointF.y);
           
            
            ScaleAnimation scaleAnimation = new ScaleAnimation(1,0.5f,1,0.5f,  
                    Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            AnimationSet enter = new AnimationSet(true);
            scaleAnimation.setDuration(500);
           
            enter.addAnimation(scaleAnimation);
            target.startAnimation(enter);
        }
    }

    private int tag=0;
	private ImageLoader mImageLoader;
	
	public OnHeartAnimationUpdateListener animationUpdateListener;
	public void setOnHeartAnimationUpdateListener(OnHeartAnimationUpdateListener listener){
		animationUpdateListener = listener;
	}
	
    private class AnimEndListener extends AnimatorListenerAdapter {
        private View target;
		private StudentBean bean;

        public AnimEndListener(View target, StudentBean bean) {
            this.target = target;
            this.bean = bean;
        }

        @Override
        public void onAnimationStart(Animator animation) {
        	// TODO Auto-generated method stub
        	super.onAnimationStart(animation);
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            removeView((target));
            tag=tag+1;
            if (tag==mBadgeCount) {
            	tag=0;
            	Intent intent = new Intent(StudentListFragment.ACTION_ANIMATION_END);
    			mContext.sendBroadcast(intent);
			}
            
            if(animationUpdateListener != null){
            	animationUpdateListener.onAnimationEnd(bean);
            }            
        }
    }
    
    public interface OnHeartAnimationUpdateListener {
    	public void onAnimationEnd(StudentBean bean);
    }
    
}
