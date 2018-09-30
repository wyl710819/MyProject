package com.routon.smartcampus.answerrelease;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class JudgePieChartView extends View{
	
	private Paint mPaint;// 画笔，在此处定义，避免多次创建
	private Double[] inputData;// 输入数据，用于确定占比
	private String[] answerItem;
	private RectF rect;// 饼状图的外接矩形
	/**
	 * 对外接口，获取输入数据,并将输入的数据转化位百分比数字
	 */

	double sumTotal;
	double answerTotal;
	public JudgePieChartView(Context context){
		this(context, null);
		init();
	}
	
	public JudgePieChartView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
		init();
	}

	public JudgePieChartView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	public void init(){
		mPaint = new Paint();
		rect = new RectF();
	}
	/**
	 * 获取传入的数据
	 * */
	public Boolean setPieChartData(List<Integer> optionList) {

		sumTotal = 0;
		answerTotal = 0;
		answerItem = new String[optionList.size()];
		if (optionList.size() > 0) {
			this.inputData = new Double[optionList.size()];
			for (int i = 0; i < optionList.size(); i++) {
				// Log.i("xxx",inputData1[i]+"---");
				sumTotal = optionList.get(i)+ sumTotal;
				if (i == optionList.size() - 2) {
					answerTotal = sumTotal;
				}
			}
			for (int j = 0; j < optionList.size(); j++) {
				this.inputData[j] = (double) Math
						.round(optionList.get(j) / sumTotal * 100 * 10) / 10;
			}
			invalidate();
			return true;
		} else {
			return false;
		}

	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		// 保证输入数据的有效性
		if (inputData == null) {
			return;
		}
		if ((inputData.length == 0)) {
			return;
		}
		// 将数据转换为角度
		int num = inputData.length;
		float[] angles = new float[num];
		// int[] colors=new int[num];
		Double sum = 0.0;
		for (int i = 0; i < num; i++) {
			sum += inputData[i];
		}
		if (sum == 0) {
			return;
		}
	    float angleSum = 0;
		for (int i = 0; i < num; i++) {
			angles[i] = (float) (inputData[i] * 360 / sum);
			angleSum += angles[i];

		}
		int colors[] = { Color.parseColor("#a4fa03"),
				Color.parseColor("#e20018"), Color.parseColor("#666666")};
		// 画出饼状图
		float startAngle = 0;
		float sweepAngle = 0;
		// 饼状图外接矩形
		rect.set(0, 0, width, height);
		float averAngle;
		
		// 设置圆弧画笔
		for (int i = 0; i < num; i++) {
			mPaint.setColor(colors[i]);
			mPaint.setAntiAlias(true);
			mPaint.setStrokeWidth(30);
			RectF rectArc;
			startAngle += sweepAngle;
			sweepAngle = angles[i];
			// float aver=(2*startAngle+sweepAngle)/2;
			averAngle = (float) (((startAngle * 2 + sweepAngle) / 2) / 360 * (2 * Math.PI));
			rectArc = new RectF(0, 0, dp2px(180), dp2px(180));
			canvas.drawArc(rectArc, startAngle, sweepAngle, true,
					mPaint);
		
		}
		// 画内圆环
			mPaint.setColor(Color.BLACK);
			// Rect rectInCircle=new Rect(60,60,180,180);
			mPaint.setAntiAlias(true);
			canvas.drawCircle((float)(dp2px(90)), (float)(dp2px(90)),(float) (dp2px(60)), mPaint);
	}
	private int dp2px(double value) {
		float v = getContext().getResources().getDisplayMetrics().density;
		return (int) (v * value + 0.5f);
	}

	private int sp2px(int value) {
		float v = getContext().getResources().getDisplayMetrics().scaledDensity;
		return (int) (v * value + 0.5f);
	}
}
