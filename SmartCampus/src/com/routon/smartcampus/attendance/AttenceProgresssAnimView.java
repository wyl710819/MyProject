package com.routon.smartcampus.attendance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class AttenceProgresssAnimView extends View{

	private int alreadyComeCount;//已到人数
	private int allStudentCount;//总人数
	private String className;//班级名称
	private Paint paintLine;//画指针
	private Paint paint;//画圆点
	private Paint paintArc;//画进度条圆弧线
	private RectF rect;//圆的外界矩形
	private static Context mContext;
	public AttenceProgresssAnimView(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
		initData();
	}
	
	

	public AttenceProgresssAnimView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		// TODO Auto-generated constructor stub
		initData();
	}
	private void initData() {
		// TODO Auto-generated method stub
		rect=new RectF(dp2px(54),dp2px(88),dp2px(306),dp2px(340));
	}
	public AttenceProgresssAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		paintLine=new Paint();
		paint=new Paint();
		paintArc=new Paint();
//		rect=new RectF(165,258,915,1008);
		mContext=context;

	}
	public void setProgressData(int alreadyComeCount ){
		this.alreadyComeCount=alreadyComeCount;
		invalidate();
	}
	public void setClassInfoData(String className,int allStudentCount){
		this.className=className;
		this.allStudentCount=allStudentCount;
		invalidate();
	}
	
	public float getSweepAngle(int cnt1,int cnt2){
		float sweepAngle=cnt1/(float)cnt2*360;
		return sweepAngle;
		
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		super.onDraw(canvas);
		
		paintLine.setColor(Color.parseColor("#2e1702"));
		paintLine.setAntiAlias(true);
		paintLine.setStrokeWidth(4);
		Float sweepAngle =getSweepAngle(alreadyComeCount, allStudentCount);  
	    canvas.rotate(sweepAngle, dp2px(180), dp2px(214));  
	    canvas.drawLine(dp2px(180),dp2px(211)-dp2px(180)*0.45f,dp2px(180), dp2px(220) , paintLine);  
	  
		paint.setColor(Color.parseColor("#0023a3"));
		paint.setAntiAlias(true);
		canvas.drawCircle(dp2px(180), dp2px(213), dp2px(6), paint);
		
		
		paintArc.setColor(Color.parseColor("#2260ad"));
		paintArc.setStrokeWidth(dp2px(8));
		paintArc.setStyle(Style.STROKE);
		paintArc.setAntiAlias(true);
		canvas.drawArc(rect, -90-sweepAngle, sweepAngle, false, paintArc); 
	}//
	
	 /** 
	  * 将dip或dp值转换为px值，保证尺寸大小不变 
	  */  
	 public static int dp2px( float dpValue) {  
	     final float scale = mContext.getResources().getDisplayMetrics().density;  
	     return (int) (dpValue * scale + 0.5f);  
	 }  

	 public static int px2dp(float pxValue) {    
	       final float scale = mContext.getResources().getDisplayMetrics().density;    
	       return (int) (pxValue / scale + 0.5f);    
	   }    


}