package com.routon.smartcampus.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.routon.edurelease.R;
import com.routon.widgets.Toast;

public class ZigzagLineView extends View {

	private Paint xLinePaint;
	private Paint hLinePaint;
	private Paint gradePaint;
	private Paint coursesPaint;
	private Paint linePaint;
	private Paint backPaint;
	
	private Rect mRect;
	private Rect mRectBottomText;
	private double lastX;
	private double lastY;
	private float yStepsDistance;
	private String[] yStepsOne;
	private String[] yStepsSum;
	private String[] ySteps;
	private String[] dateList;
	private int[] colors;
	private List<int[]> achievementList = new ArrayList<int[]>();
	private List<String> courses = new ArrayList<String>();
	private int type;
	private int selectIndex;
	private float currentRange;
	
	private final int ALL_COURSE = 1;
	private final int ONE_COURSE = 2;
	private final int backColor = Color.parseColor("#EAE4D9");
	private boolean isBadge=false;

	public ZigzagLineView(Context context) {
		super(context);
		init();
	}

	public ZigzagLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
 
		colors = new int[] { 
				Color.parseColor("#0000F8"),
				Color.parseColor("#F80098"),
				Color.parseColor("#A85800"),
				Color.parseColor("#00C800"),
				Color.parseColor("#6000F8"),
				Color.parseColor("#0090F8"),
				 Color.parseColor("#F80000"),
				 Color.parseColor("#F89800"),
				 Color.parseColor("#009898"),
				 Color.parseColor("#C80090"),
				Color.parseColor("#00F8F8"), 
				Color.parseColor("#F80068"), 
				Color.parseColor("#F8F800"),
				Color.parseColor("#00f800"),
				Color.parseColor("#F800F8"),
				Color.parseColor("#C898F8"),
				Color.parseColor("#666666"),
				Color.parseColor("#000000")};

		xLinePaint = new Paint();
		hLinePaint = new Paint();
		gradePaint = new Paint();
		coursesPaint = new Paint();
		linePaint = new Paint();
		backPaint = new Paint();
		
		mRect = new Rect();
		mRectBottomText = new Rect();
		linePaint.setAntiAlias(true);

		xLinePaint.setColor(Color.DKGRAY);
		hLinePaint.setColor(Color.LTGRAY);
		gradePaint.setColor(Color.BLACK);
		coursesPaint.setColor(Color.BLACK);
		backPaint.setColor(backColor);
		backPaint.setAntiAlias(true);
		
		linePaint.setStrokeWidth(5);

	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = getWidth();
		int height = dp2px(getResources().getDimension(R.dimen.grade_zigzag_height));
		// 绘制底部
		canvas.drawLine(dp2px(30), height + dp2px(5), width - dp2px(30), height
				+ dp2px(5), xLinePaint);

		int leftHeight = height;

		int hPerHeight = leftHeight / ySteps.length;

		hLinePaint.setTextAlign(Align.CENTER);
		
		// 绘制背景
		if(type == ALL_COURSE)
		{
			Rect sumRect = new Rect(dp2px(30), hPerHeight+dp2px(5),
					width-dp2px(30), yStepsSum.length*hPerHeight+dp2px(5));
			Rect OneRect = new Rect(dp2px(30), (yStepsSum.length+1)*hPerHeight+dp2px(5),
					width-dp2px(30), height + dp2px(5));
			canvas.drawRect(sumRect, backPaint);
			canvas.drawRect(OneRect, backPaint);
		}
		// 绘制虚线
		for (int i = 0; i < ySteps.length-1; i++) {
			canvas.drawLine(dp2px(30), (i+1) * hPerHeight + dp2px(5), width
					- dp2px(30), (i+1) * hPerHeight + dp2px(5), hLinePaint);
		}

		// 绘制左部文本
		gradePaint.setTextAlign(Align.RIGHT);
		gradePaint.setTextSize(sp2px(12));
		gradePaint.setAntiAlias(true);
		gradePaint.setStyle(Paint.Style.FILL);

		for (int i = 0; i < ySteps.length; i++) {
			canvas.drawText(ySteps[i], dp2px(25), dp2px(9) + (i+1) * hPerHeight,
					gradePaint);

		}

		int xAxisLength = width - dp2px(100);
		int columCount = dateList.length - 1;
		int step = 0;
		int txtStep = xAxisLength/5;
		if(columCount>0)
		 step = xAxisLength / columCount;
		else step = xAxisLength/5;

		// 绘制底部文本
		coursesPaint.setTextAlign(Align.RIGHT);
		coursesPaint.setTextSize(sp2px(12));
		coursesPaint.setAntiAlias(true);
		coursesPaint.setStyle(Paint.Style.FILL);

		for (int i = 0; i < dateList.length; i++) {
			coursesPaint.getTextBounds(dateList[i], 0, dateList[i].length(), mRect);
			canvas.drawText(dateList[i], dp2px(50) + step * i+mRect.width()/2, height
					+ dp2px(23), coursesPaint);

		}

		for (int i = 0; i < achievementList.size(); i++) {
			linePaint.setColor(i<18 ? colors[i] : colors[17]);
			coursesPaint.setTextSize(sp2px(16));
			
			if (isBadge) {//小红花图表
				if(i<=3 && courses.size()>0)
				{
					canvas.drawCircle((float) (dp2px(20) + txtStep * i*1.6),
							height+dp2px(50), 8, linePaint);// 注释点
					coursesPaint.getTextBounds(courses.get(i), 0, courses.get(i).length(), mRectBottomText);
					canvas.drawText(courses.get(i),(float) (dp2px(30) + txtStep * i*1.6+mRectBottomText.width()), 
							height+dp2px(50)+mRectBottomText.height()/2, coursesPaint);//注释文字
				}
				else if(i<=7 && courses.size()>0)
				{
					canvas.drawCircle((float) (dp2px(20) + txtStep * (i-4)*1.6),
							height+dp2px(80), 8, linePaint);// 注释点
					coursesPaint.getTextBounds(courses.get(i), 0, courses.get(i).length(), mRectBottomText);
					canvas.drawText(courses.get(i),(float) (dp2px(30) + txtStep * (i-4)*1.6+mRectBottomText.width()), 
							height+dp2px(80)+mRectBottomText.height()/2, coursesPaint);//注释文字
				}
				else if(i<=11 && courses.size()>0)
				{
					canvas.drawCircle((float) (dp2px(20) + txtStep * (i-8)*1.6),
							height+dp2px(110), 8, linePaint);// 注释点
					coursesPaint.getTextBounds(courses.get(i), 0, courses.get(i).length(), mRectBottomText);
					canvas.drawText(courses.get(i),(float) (dp2px(30) + txtStep * (i-8)*1.6+mRectBottomText.width()), 
							height+dp2px(110)+mRectBottomText.height()/2, coursesPaint);//注释文字
				}
			}else {//成绩图表
				if(i<=4 && courses.size()>0)
				{
					canvas.drawCircle((float) (dp2px(50) + txtStep * i),
							height+dp2px(50), 8, linePaint);// 注释点
					coursesPaint.getTextBounds(courses.get(i), 0, courses.get(i).length(), mRectBottomText);
					canvas.drawText(courses.get(i),(float) (dp2px(60) + txtStep * i+mRectBottomText.width()), 
							height+dp2px(50)+mRectBottomText.height()/2, coursesPaint);//注释文字
				}
				else if(i<=9 && courses.size()>0)
				{
					canvas.drawCircle((float) (dp2px(50) + txtStep * (i-5)),
							height+dp2px(80), 8, linePaint);// 注释点
					coursesPaint.getTextBounds(courses.get(i), 0, courses.get(i).length(), mRectBottomText);
					canvas.drawText(courses.get(i),(float) (dp2px(60) + txtStep * (i-5)+mRectBottomText.width()), 
							height+dp2px(80)+mRectBottomText.height()/2, coursesPaint);//注释文字
				}
				else if(i<=14 && courses.size()>0)
				{
					canvas.drawCircle((float) (dp2px(50) + txtStep * (i-10)),
							height+dp2px(110), 8, linePaint);// 注释点
					coursesPaint.getTextBounds(courses.get(i), 0, courses.get(i).length(), mRectBottomText);
					canvas.drawText(courses.get(i),(float) (dp2px(60) + txtStep * (i-10)+mRectBottomText.width()), 
							height+dp2px(110)+mRectBottomText.height()/2, coursesPaint);//注释文字
				}
			}
			
			
			lastX = 0;
			lastY = 0;
			for (int j = 0; j < achievementList.get(i).length; j++) {
				int value = achievementList.get(i)[j];
				if(value == 0)//数据为0代表该项没有成绩，跳过该点
					continue;
				int rh = (int) (height - hPerHeight*(value-Integer.valueOf(ySteps[ySteps.length-1]))/currentRange);
				if (isBadge) {
					if(type == ALL_COURSE &&( i == achievementList.size()-1 || i == achievementList.size()-2))//总分项
						rh = (int) (hPerHeight + hPerHeight*(Integer.valueOf(ySteps[0])-value)/yStepsDistance);
				}else {
					if(type == ALL_COURSE && i == achievementList.size()-1)//总分项
						rh = (int) (hPerHeight + hPerHeight*(Integer.valueOf(ySteps[0])-value)/yStepsDistance);
				}
				
				
				if(type == ALL_COURSE && j == selectIndex )//当前选择项
				{
					Rect rect = new Rect(dp2px(50) + step * j-8, rh + dp2px(2.5)-8, 
							dp2px(50) + step * j+8, rh + dp2px(2.5)+8);
					canvas.drawRect(rect, linePaint);// 点
				}
				else {
					canvas.drawCircle((float) (dp2px(50) + step * j),
							(float) (rh + dp2px(2.5)), 8, linePaint);// 点
				}
				if (j > 0 && lastX != 0 && lastY != 0) {
					canvas.drawLine((float) lastX, (float) lastY,
							(float) dp2px(50) + step * j, (float) rh
									+ dp2px(2.5), linePaint);// 线
				}
				lastX = dp2px(50) + step * j;
				lastY = rh + dp2px(2.5);
			}

		}

	}

	private int dp2px(double value) {
		float v = getContext().getResources().getDisplayMetrics().density;
		return (int) (v * value + 0.5f);
	}

	private int sp2px(int value) {
		float v = getContext().getResources().getDisplayMetrics().scaledDensity;
		return (int) (v * value + 0.5f);
	}
	
	public void setData(List<int[]> achievementList,String[] dateList,List<String> courses,int selectIndex, boolean isBadge)
	{
		this.isBadge=isBadge;
		this.achievementList = achievementList;
		for(int i =0;i<dateList.length;i++)
		{
			dateList[i] = dateList[i].split(" ")[0];
			if (!isBadge) {
				dateList[i] = dateList[i].split("-")[1]+"."+dateList[i].split("-")[2];
			}
			
		}
		this.dateList = dateList;
		this.courses = courses;
		this.selectIndex = dateList.length-1-selectIndex;
		if(courses.size() > 1)//综合考试
		{
			yStepsDistance = 50.0f;
			type = ALL_COURSE;
			List<Integer> yStepsList = new ArrayList<Integer>();
			List<Integer> gradesList = new ArrayList<Integer>();
			for(int i=0;i<achievementList.size()-(isBadge ? 2 : 1);i++)
			{
				int[] grades = achievementList.get(i);
				for(int j=0;j<grades.length;j++)
				{
					if(!gradesList.contains(grades[j]))
						gradesList.add(grades[j]);
				}
			}
			int[] nowGrades = new int[gradesList.size()];
			for(int i=0;i<nowGrades.length;i++)
				nowGrades[i] = gradesList.get(i);
			int[] oneSteps = getYSteps(nowGrades);
			for(int i=0;i<oneSteps.length;i++)
			{
				if(!yStepsList.contains(oneSteps[i]))
					yStepsList.add(oneSteps[i]);
			}
			Collections.sort(yStepsList);
			Collections.reverse(yStepsList);
			yStepsOne = new String[yStepsList.size()];
			for(int i=0;i<yStepsList.size();i++)
				yStepsOne[i] = String.valueOf(yStepsList.get(i));
			
			
			int[] sumGrades = null;
			if (isBadge) {
				int[] sumGrade1 = achievementList.get(achievementList.size()-1);
				int[] sumGrade2 = achievementList.get(achievementList.size()-2);
				sumGrades=concat(sumGrade1,sumGrade2);
			}else {
				sumGrades = achievementList.get(achievementList.size()-1);
			}
			
			int[] sumYSteps = getYSteps(sumGrades,50);
			yStepsSum = new String[sumYSteps.length];
			for(int i=0;i<sumYSteps.length;i++)
				yStepsSum[i] = String.valueOf(sumYSteps[i]);
			ySteps = new String[yStepsOne.length+yStepsSum.length];
			System.arraycopy(yStepsSum, 0, ySteps, 0, yStepsSum.length);
			System.arraycopy(yStepsOne, 0, ySteps, yStepsSum.length, yStepsOne.length);
		}
		else if(courses.size() == 1)//单科考试
		{
			type = ONE_COURSE;
			yStepsOne = new String[] {"160","140","120","100", "80", "60","40","20","0" };
			ySteps = yStepsOne;
			yStepsDistance = 20.0f;
			currentRange = 20.0f;
		}
		else {//没有成绩
			Toast.makeText(getContext(), "当前科目该同学没有成绩", Toast.LENGTH_LONG).show();
			ySteps = yStepsOne;
			yStepsDistance = 20.0f;
		}
		invalidate();
	}
	
	public int[] getYSteps(int[] grades,int range)
	{
		int maxGrades = 0;
		int minGrades = 0;
		for(int i=0;i<grades.length;i++)
		{
			if(grades[i]>maxGrades)
				maxGrades = grades[i];
			if(grades[i] != 0 && minGrades == 0)
				minGrades = grades[i];
			if(grades[i]<minGrades && grades[i] != 0)
				minGrades = grades[i];
		}
		int lengthMax = maxGrades/range+1;
		int lengthMin = minGrades/range;
		int[] ySteps = new int[lengthMax-lengthMin+1];
		for(int i=0;i<ySteps.length;i++)
		{
			ySteps[i] = range*lengthMax-range*i;
		}
		return ySteps;
	}
	
	public int[] getYSteps(int[] grades)
	{
		int maxGrades = 0;
		int minGrades = 0;
		for(int i=0;i<grades.length;i++)
		{
			if(grades[i]>maxGrades)
				maxGrades = grades[i];
			if(grades[i] != 0 && minGrades == 0)
				minGrades = grades[i];
			if(grades[i]<minGrades && grades[i] != 0)
				minGrades = grades[i];
		}
		if(maxGrades - minGrades>=60)
			currentRange = 20;
		else if(maxGrades - minGrades >=30)
			currentRange = 10;
		else currentRange = 5;
		int lengthMax = (int) (maxGrades/currentRange+1);
		int lengthMin = (int) (minGrades/currentRange);
		int[] ySteps = new int[lengthMax-lengthMin+1];
		for(int i=0;i<ySteps.length;i++)
		{
			ySteps[i] = (int) (currentRange*lengthMax-currentRange*i);
		}
		return ySteps;
	}
	
	private int[] concat(int[] a, int[] b) {
		int[] c= new int[a.length+b.length];
		  System.arraycopy(a, 0, c, 0, a.length);
		  System.arraycopy(b, 0, c, a.length, b.length);
		  return c;
		}
}
