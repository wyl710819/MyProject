package com.routon.smartcampus.answerrelease;

import com.routon.edurelease.R;

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

public class AnswerDegreeCircle extends View{

	private float degree;
	private float degree2;
	private float lastDegree;
	private float lastDegree2;
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
	private Paint circlePaint2;
	
	private static final String TAG = "AttenceDegreeCircle";
	
	public AnswerDegreeCircle(Context context){
		this(context, null);
	}
	
	public AnswerDegreeCircle(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public AnswerDegreeCircle(Context context, AttributeSet attrs,
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
		circlePaint2 = new Paint();
		circlePaint2.setColor(Color.parseColor("#E2061A"));
		circlePaint2.setAntiAlias(true);
		circlePaint2.setStyle(Style.FILL);
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
			canvas.drawArc(rect, 270,lastDegree, false, paint);
			canvas.drawCircle(getWidth()/2, getHeight()/2-circleWidth, 0, circlePaint);
			drawCircle(canvas);
			lastDegree = degree;
		}
		
		if(degree2 > 0){
			paint.setColor(Color.parseColor("#E2061A"));
			canvas.drawArc(rect, 270-lastDegree2, -(degree2-lastDegree2), false, paint);
			canvas.drawArc(rect, 270,-lastDegree2, false, paint);
			canvas.drawCircle(getWidth()/2, getHeight()/2-circleWidth, 0, circlePaint2);//画轨迹
			drawCircle2(canvas);
			lastDegree2 = degree2;
		}
//		drawText(canvas);
	}
	
	public void drawCircle(Canvas canvas){//画球
		float x = (float) (circleWidth*Math.sin(degree*Math.PI/180)+getWidth()/2);
		float y = (float) (getHeight()/2-(circleWidth *Math.cos(degree*Math.PI/180)));
		canvas.drawCircle(x, y, circleStrokeWidth/2, circlePaint);
	}
	
	public void drawCircle2(Canvas canvas){//画球
		float x = (float) (circleWidth*Math.sin(-degree2*Math.PI/180)+getWidth()/2);
		float y = (float) (getHeight()/2-(circleWidth *Math.cos(degree2*Math.PI/180)));
		canvas.drawCircle(x, y, circleStrokeWidth/2, circlePaint2);
	}
	
	/*public void drawText(Canvas canvas){
		int textX = getWidth()/2+circleWidth*9/10;
		int textY = getHeight()/2+circleWidth*9/10;
		textRect = new RectF(textX-12,textY-12,textX+12,textY+12);
		textPaint.setColor(degreeColor);
		canvas.drawRect(textRect, textPaint);
		textPaint.setColor(Color.WHITE);
		canvas.drawText("实到人数", textX+20, textY+15, textPaint);
	}*/
	
	@SuppressLint("NewApi") public void setDegree(float iDegree){
		if (iDegree>10) {
			iDegree=iDegree-10;
		}
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
		animator.setDuration(500);
		animator.start();
	}
	private ValueAnimator reverseAnimator;
	@SuppressLint("NewApi")
	public void setReverseDegree(float iDegree) {
		if (iDegree>10) {
			iDegree=iDegree-10;
		}
		
		if(iDegree == 0){
			lastDegree2 = 0;
			degree2 = 0;
		}
		if(reverseAnimator != null && reverseAnimator.isRunning()){
			lastDegree2 = (float) reverseAnimator.getAnimatedValue();
			reverseAnimator.pause();
		}
		reverseAnimator = ValueAnimator.ofFloat(lastDegree2, iDegree);
		reverseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				degree2 = (float) animation.getAnimatedValue();
				postInvalidate();
			}
		});
		reverseAnimator.setInterpolator(new LinearInterpolator());
		reverseAnimator.setDuration(500);
		reverseAnimator.start();
	}

}
