package com.routon.smartcampus.attendance;

import com.routon.edurelease.R;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class AttenceDegreeCircle extends View{
	
	private float degree;
	private float lastDegree;
	private int backColor;
	private int degreeColor;
	private int circleWidth;
	private int circleStrokeWidth;
	
	private ValueAnimator animator;
	private RectF rect;
	private RectF textRect;
	private Paint paint;
	private Paint textPaint;
	private Paint circlePaint;
	
	private static final String TAG = "AttenceDegreeCircle";
	
	public AttenceDegreeCircle(Context context){
		this(context, null);
	}
	
	public AttenceDegreeCircle(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public AttenceDegreeCircle(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.attenceDegreeCircle, 0, 0);
		backColor = array.getColor(R.styleable.attenceDegreeCircle_circle_back_color, Color.parseColor("#666666"));
		degreeColor = array.getColor(R.styleable.attenceDegreeCircle_circle_degree_color, Color.parseColor("#a4fa03"));
		circleWidth = array.getDimensionPixelSize(R.styleable.attenceDegreeCircle_circle_width, 100);
		circleStrokeWidth = array.getDimensionPixelSize(R.styleable.attenceDegreeCircle_circle_stroke_width, 30);
		array.recycle();
		init();
	}
	
	public void init(){
		lastDegree = 0;
		paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(circleStrokeWidth);
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(38);
		circlePaint = new Paint();
		circlePaint.setColor(degreeColor);
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Style.FILL);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		rect = new RectF(getWidth()/2-circleWidth, getHeight()/2-circleWidth,
				getWidth()/2+circleWidth, getHeight()/2+circleWidth);
		paint.setColor(backColor);
		canvas.drawArc(rect, 0, 360, false, paint);
		if(degree > 0){
			paint.setColor(degreeColor);
			canvas.drawArc(rect, 270+lastDegree, degree-lastDegree, false, paint);
			canvas.drawArc(rect, 270, lastDegree, false, paint);
			canvas.drawCircle(getWidth()/2, getHeight()/2-circleWidth, circleStrokeWidth/2, circlePaint);
			drawCircle(canvas);
			lastDegree = degree;
		}
		drawText(canvas);
	}
	
	public void drawCircle(Canvas canvas){
		float x = (float) (circleWidth*Math.sin(degree*Math.PI/180)+getWidth()/2);
		float y = (float) (getHeight()/2-(circleWidth *Math.cos(degree*Math.PI/180)));
		Log.d(TAG, "x="+x+"   y="+y+"   centerX="+getWidth()/2+"   centerY="+(getHeight()/2-circleWidth-circleStrokeWidth));
		canvas.drawCircle(x, y, circleStrokeWidth/2, circlePaint);
	}
	
	public void drawText(Canvas canvas){
		int textX = getWidth()/2+circleWidth*9/10;
		int textY = getHeight()/2+circleWidth*9/10;
		textRect = new RectF(textX-12,textY-12,textX+12,textY+12);
		textPaint.setColor(degreeColor);
		canvas.drawRect(textRect, textPaint);
		textPaint.setColor(Color.WHITE);
		canvas.drawText("实到人数", textX+20, textY+15, textPaint);
	}
	
	@SuppressLint("NewApi") public void setDegree(float iDegree){
		if(iDegree == 0){
			lastDegree = 0;
			degree = 0;
		}
		if(animator != null && animator.isRunning()){
			lastDegree = (float) animator.getAnimatedValue();
			animator.pause();
		}
		animator = ValueAnimator.ofFloat(lastDegree, iDegree);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				degree = (float) animation.getAnimatedValue();
				postInvalidate();
			}
		});
		animator.setInterpolator(new LinearInterpolator());
		animator.setDuration((long) ((iDegree-lastDegree)*20));
		animator.start();
	}

}
