package com.routon.smartcampus.answerrelease;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

public class AnsTimeLineChartView extends View {
	private Paint pRect;
	private int AvgValue;
	private int WidValue;
	private int WidDip = 100;
	private int maxYValue;
	private int avgYValue;
	private int ySize;//y坐标间隔个数
	private RectF mRect = null;  //绘图矩形区域
	private static final String TAG="ANSLINECHART";
	private ArrayList<String>answerTimeList=new ArrayList<String>();
	private ArrayList<String>answerTimePercentList=new ArrayList<String>();
	private DisplayMetrics dm;
	private int screenHeight;
	private float avgAnsTime=0;
	private String avgAnsTimeStr="0";
	private ArrayList<String>yRightAnsTimePercentList=new ArrayList<String>();
	public AnsTimeLineChartView(Context context) {
		super(context);
		initData();
	}
	public AnsTimeLineChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData();
	}
	private void initData() {
		// TODO Auto-generated method stub
		 dm = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenHeight=dm.heightPixels;
		
	}
	public AnsTimeLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public void setAnsTimeLineViewData(ArrayList<String> answerTimeList,ArrayList<String> answerTimePercentList,ArrayList<String>yRightAnsTimePercentList,float avgAnsTime){
		this.answerTimeList=answerTimeList;
		this.answerTimePercentList=answerTimePercentList;
		this.avgAnsTime=avgAnsTime;
		this.yRightAnsTimePercentList=yRightAnsTimePercentList;
		avgAnsTime=avgAnsTime/1000;
		DecimalFormat df = new DecimalFormat("0.0");  
		if(Float.isNaN(avgAnsTime)){
			avgAnsTimeStr="0";
		}else{
			avgAnsTimeStr=df.format(avgAnsTime);
		}
		List<Integer> yListTemp = new ArrayList<Integer>();
		for(int i=0;i<answerTimePercentList.size();i++){
			yListTemp.add(Integer.parseInt(answerTimePercentList.get(i)));
		}
		
		Collections.sort(yListTemp);
		if(answerTimePercentList.size()>0){
			if(yListTemp.get(yListTemp.size()-1)>=10){
//				 maxYValue=yListTemp.get(yListTemp.size()-1)+yListTemp.get(yListTemp.size()-1)/5;
				avgYValue=yListTemp.get(yListTemp.size()-1)/5;
				maxYValue=yListTemp.get(yListTemp.size()-1)/5*7;
				ySize=7;
				 }
			else if(7<yListTemp.get(yListTemp.size()-1)&&yListTemp.get(yListTemp.size()-1)<10){
				ySize=yListTemp.get(yListTemp.size()-1);
				maxYValue=yListTemp.get(yListTemp.size()-1);
				avgYValue=1;
			}else{
			     ySize=7;
				 maxYValue=7;
				 avgYValue=1;
		  }
		}
		 
		
		Log.d(TAG, "时间柱状图y:"+answerTimePercentList+"时间柱状图x:"+answerTimeList+"y最大值:"+maxYValue+"y平均值:"+avgYValue);
		invalidate();
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measureWidth(widthMeasureSpec);
		int height = measureHeight(heightMeasureSpec);
		setMeasuredDimension(measureWidth(widthMeasureSpec),
	    measureHeight(heightMeasureSpec));
		
	}
	
	private int measureWidth(int measureSpec) {
		int result = 100;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		if (specMode == MeasureSpec.EXACTLY) { //fill_parent
		result = specSize;
		} else if (specMode == MeasureSpec.AT_MOST) { //wrap_content
		result = Math.min(result, specSize);
		}			
		return result;
	}
	
	private int measureHeight(int measureSpec) {
		int result = 100;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		if (specMode == MeasureSpec.EXACTLY) { //fill_parent 或者指定常量
			result = specSize;
		} else if (specMode == MeasureSpec.AT_MOST) { //wrap_content
			result = Math.min(result, specSize);          //找最小值
		}			
		return result;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		
		
		super.onDraw(canvas);
		mRect = new RectF(0, 0, getWidth(), getHeight());
		AvgValue = (getHeight()-WidDip)/(ySize+1);
		WidValue = (getWidth()-WidDip)/9;
		
		Paint paintTitle = new Paint();
		paintTitle.setTextSize(sp2px(19));
		paintTitle.setColor(Color.parseColor("#a4fa03"));
		paintTitle.setAntiAlias(true);
//		paintTitle.setShadowLayer(1, 3, 3, Color.BLACK);
		canvas.drawText("平均答题时间:"+avgAnsTimeStr+"s", 330, 60, paintTitle);
		Paint paint = new Paint(); 
		Paint paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);  //虚线画笔
		// Anti-aliasing
		paint.setAntiAlias(true);
		paint.setColor(Color.parseColor("#666666"));
		canvas.drawLine(mRect.left + WidDip, mRect.bottom, mRect.left
				+ WidDip, WidValue, paint); // startx,starty,stopx,stopy
		// Draw the X轴
		canvas.drawLine(mRect.left + WidDip, mRect.bottom,
				mRect.right, mRect.bottom, paint);

		
		paint.setAntiAlias(true); 
		paint.setStyle(Paint.Style.STROKE);   
		
		
		paintLine.setStyle(Paint.Style.STROKE);
		paintLine.setColor(Color.parseColor("#666666"));
		PathEffect effects = new DashPathEffect(new float[] {5,5,5,5}, 1); 
		paintLine.setPathEffect(effects);

		
		for(int i=0;i<ySize;i++)
		{
			Path path = new Path();    
			path.moveTo(mRect.left + WidDip, mRect.top + AvgValue * (i + 1)+WidValue);
			path.lineTo(mRect.right, mRect.top + AvgValue * (i + 1)+WidValue);
		    canvas.drawPath(path, paintLine);
		    paint.setTextSize(sp2px(16));   
		    
		    
//方法  1		   
//		    FontMetrics metrics = paint.getFontMetrics(); //获取字体信息
//		    
//		    float fontHeight = metrics.bottom - metrics.top;
//		    
//			canvas.drawText(""+(7-i)*100, 0, mRect.top+AvgValue*(i+1)+fontHeight/4, paint);
		    
		    
//方法 2
		    Rect bounds1 = new Rect();  
		    paint.getTextBounds(""+(ySize-i)*avgYValue, 0, 1, bounds1);  
		    int fontHeight = bounds1.height();
		    canvas.drawText(""+(ySize-i)*avgYValue, 30, mRect.top+AvgValue*(i+1)+fontHeight/2+WidValue, paint);
		    
		}
		
		
		Rect bounds = new Rect();
		paint.getTextBounds("人数", 0, 1, bounds);
		int fontHeight = bounds.height();
		canvas.drawText("人数", 0, mRect.top + fontHeight+WidValue-10, paint);

		
		//画矩形
		pRect = new Paint();
		pRect.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了   
		pRect.setColor(Color.parseColor("#999999"));
		pRect.setStyle(Paint.Style.FILL);//设置填满   
		for(int i=0;i<answerTimeList.size();i++)
		{

			 Log.i("left top  right bottom", ""+WidValue*(2*i+1)+"  "+(4-i)*getHeight()/4+"  "+WidValue*(2*i)+" "
			 		+ " "+getHeight());
			
			 float yHeight=(float)(Integer.parseInt(answerTimePercentList.get(i)))*((float)(AvgValue*ySize))/maxYValue;
			 if(Integer.parseInt(answerTimePercentList.get(i))>0){
				 pRect.setColor(Color.parseColor("#999999"));
					canvas.drawRect(105 + i * (mRect.right - mRect.left - WidValue+65)
							/ (answerTimeList.size() + 1), getHeight()
							- yHeight+2, 115 + (2 * i + 1)
							* (mRect.right - mRect.left - WidValue+50)
							/ (2 * (answerTimeList.size() + 1)), getHeight()
							- 1, pRect);// 长方形
			 }
			 //绘制正确答案柱状图
			 if(yRightAnsTimePercentList.size()>0){
				 float yRightHeight=(float)(Integer.parseInt(yRightAnsTimePercentList.get(i)))*((float)(AvgValue*ySize))/maxYValue;
				 if(Integer.parseInt(yRightAnsTimePercentList.get(i))>0){
					 pRect.setColor(Color.parseColor("#a4fa03"));
					 canvas.drawRect(105 + i * (mRect.right - mRect.left - WidValue+65)
								/ (yRightAnsTimePercentList.size() + 1), getHeight()
								- yRightHeight+2, 115 + (2 * i + 1)
								* (mRect.right - mRect.left - WidValue+50)
								/ (2 * (answerTimeList.size() + 1)), getHeight()
								- 1, pRect);// 长方形
				 }
			 }
			 paint.setTextSize(sp2px(14));
			 Rect countRect=new Rect();
			 if(Integer.valueOf(answerTimePercentList.get(i))>0){
				 paint.getTextBounds(answerTimePercentList.get(i), 0, answerTimePercentList.get(i).length(), countRect);
				 String numberStr=null;
				 int numberX;
				 if(yRightAnsTimePercentList.size()>0){
					 numberStr=String.valueOf("/"+answerTimePercentList.get(i));
//					 numberStr="/66";
					 if (Integer.valueOf(answerTimePercentList.get(i))>=10) {
//					 if (11>=10) {
						 numberX=110;
					}else {
						numberX=115;
					}
					 paint.setColor(Color.parseColor("#a4fa03"));
					 int numberTagX=80;
					 if (Integer.valueOf(yRightAnsTimePercentList.get(i))>=10) {
//					 if (11>=10) {
						 numberTagX=65;
					}else {
						numberTagX=85;
					}
					 
					 canvas.drawText(
								String.valueOf(yRightAnsTimePercentList.get(i)),
//							 "55",
								numberTagX+ i* (mRect.right - mRect.left - WidValue+50)/ (answerTimeList.size() + 1)
										+ ((mRect.right - mRect.left - WidValue+50)
										/ (2 * (answerTimeList.size() + 1)) - countRect.width()) / 2, getHeight()
										- yHeight - 10, paint);	
				 }else{
					 numberStr=answerTimePercentList.get(i);
//					 numberStr="55";
					 if (Integer.valueOf(answerTimePercentList.get(i))>=10) {
//					 if (11>=10) {
						 numberX=100;
					}else {
						numberX=115;
					}
					 
				 }
				 paint.setColor(Color.parseColor("#666666"));
				 canvas.drawText(
							numberStr,
							numberX+ i* (mRect.right - mRect.left - WidValue+50)/ (answerTimeList.size() + 1)
									+ ((mRect.right - mRect.left - WidValue+50)
									/ (2 * (answerTimeList.size() + 1)) - countRect.width()) / 2, getHeight()
									- yHeight - 10, paint);
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

}