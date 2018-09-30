package com.routon.smartcampus.view;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.routon.edurelease.R;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class HistogramView extends View{
	private Paint xLinePaint;// 坐标轴 轴线 画笔：
    private Paint hLinePaint;// 坐标轴水平内部 虚线画笔
    private Paint gradePaint;// 文本画笔
    private Paint coursesPaint;// 文本画笔
    private Paint paint;// 矩形画笔
    private Rect mRect;//文字矩形
    private float setDistance;//两个课程间的间隔
    private int[] studentGrade;
    private int[] averageGrade;
//    private int[] aniProgress;
//    private final int TRUE = 1;
//    private int[] text;
    // 坐标轴左侧的数标
    private String[] yStepsAll = new String[] {"600","550","500","450", "400",
			"350", "300", "250", "200", "150", "100", "50", "0" };
	private String[] yStepsOne = new String[] {"150","120","90","60", "30", "0" };
	private String[] ySteps;
	private float currentRange = 30.0f;
    // 坐标轴底部
    private String[] courses;
	private LinkedHashMap<String, Integer> studentMap= new LinkedHashMap<String, Integer>();
//    private int flag;
//    private HistogramAnimation ani;
	private LinkedHashMap<String, Integer> averageMap= new LinkedHashMap<String, Integer>();
    private boolean isBadge=false;
	private Context mContext;
	private int windowW;
	private int windowH;
 
    public HistogramView(Context context) {
        super(context);
        this.mContext=context;
        init();
    }
 
    public HistogramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        init();
    }
 
    private void init() {
 
    	WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        windowW = outMetrics.widthPixels;
        windowH = outMetrics.heightPixels;
    	
 
        xLinePaint = new Paint();
        hLinePaint = new Paint();
        gradePaint = new Paint();
        coursesPaint= new Paint();
        paint = new Paint();
        mRect = new Rect();
        
        xLinePaint.setColor(Color.DKGRAY);
        hLinePaint.setColor(Color.LTGRAY);
        gradePaint.setColor(Color.BLACK);
        coursesPaint.setColor(Color.BLACK);
 
    }
 
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	// TODO Auto-generated method stub
//    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	
    	// TODO Auto-generated method stub
        int width = 0;
        int height = 0;
        //获得宽度MODE
        int modeW = MeasureSpec.getMode(widthMeasureSpec);
        //获得宽度的值
        if (modeW == MeasureSpec.AT_MOST) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (modeW == MeasureSpec.EXACTLY) {
            width = widthMeasureSpec;
        }
        if (modeW == MeasureSpec.UNSPECIFIED) {
            width = windowW;
        }
        //获得高度MODE
        int modeH = MeasureSpec.getMode(height);
        //获得高度的值
        if (modeH == MeasureSpec.AT_MOST) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        if (modeH == MeasureSpec.EXACTLY) {
            height = heightMeasureSpec;
        }
        if (modeH == MeasureSpec.UNSPECIFIED) {
            //ScrollView和HorizontalScrollView
            height = dp2px(350);
        }
        //设置宽度和高度
        
    	
    	
    	if (isBadge) {
    		if (courses.length<4) {
    			setMeasuredDimension(width, height);
			}else {
				
				setMeasuredDimension(courses.length*width/4, height);
			}
		}else {
			setMeasuredDimension(width, height);
		}
    	
    }
 
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
 
        int columCount = courses.length + 1;
        
        int width = getWidth();
        int height = dp2px((int)getResources().getDimension(R.dimen.grade_histogram_height));
        // 绘制底部
        canvas.drawLine(dp2px(30), height+dp2px(5) , width - dp2px(30), height+dp2px(5)
                , xLinePaint);
 
        int leftHeight = height ;
 
        int hPerHeight = leftHeight / (ySteps.length-1);
 
        hLinePaint.setTextAlign(Align.CENTER);
        // 绘制虚线
        for (int i = 0; i < ySteps.length-1; i++) {
            canvas.drawLine(dp2px(30),  i * hPerHeight+dp2px(5), width
                    - dp2px(30), i * hPerHeight+dp2px(5), hLinePaint);
        }
 
     // 绘制左部文本
        gradePaint.setTextAlign(Align.RIGHT);
        gradePaint.setTextSize(sp2px(12));
        gradePaint.setAntiAlias(true);
        gradePaint.setStyle(Paint.Style.FILL);
        
        for (int i = 0; i < ySteps.length; i++) {
            canvas.drawText(ySteps[i], dp2px(25), dp2px(12) + i * hPerHeight,
            		gradePaint);
        }
 
       
        int xAxisLength = width - dp2px(30);
       
        int step = xAxisLength / columCount;
 
     // 绘制底部文本
        coursesPaint.setTextSize(sp2px(16));
        coursesPaint.setAntiAlias(true);
        coursesPaint.setStyle(Paint.Style.FILL);
        
        for (int i = 0; i < columCount - 1; i++) {
        	if (isBadge) {
        		coursesPaint.getTextBounds(courses[i], 0, courses[i].length(), mRect);
        		
        		if (i%2!=0 && columCount>4) {
        			
        			canvas.drawText(courses[i], (float)(dp2px(30)/2-2.5 + step * (i + 1)-mRect.width()/2), height
                            + dp2px(40), coursesPaint);
				}else {
					canvas.drawText(courses[i], (float)(dp2px(30)/2-2.5 + step * (i + 1)-mRect.width()/2), height
	                        + dp2px(23), coursesPaint);
				}
                
			}else {
				coursesPaint.getTextBounds(courses[i], 0, courses[i].length(), mRect);
	            canvas.drawText(courses[i], (float)(dp2px(30)/2-2.5 + step * (i + 1)-mRect.width()/2), height
	                    + dp2px(23), coursesPaint);
			}
            
        }
 
        // 绘制矩形
        if (studentGrade != null && studentGrade.length > 0) {
            for (int i = 0; i < studentGrade.length; i++) {
                int value = studentGrade[i];
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor("#5B9BD5"));
                paint.setTextSize(40);
                Rect rect = new Rect();
                rect.left = step * (i + 1)-5;
                rect.right = (dp2px(30) + step * (i + 1))-55;
                int rh = (int) (leftHeight - hPerHeight*value/currentRange);
                if (isBadge) {
                	rh = (int) (leftHeight - (hPerHeight*value/currentRange));
				}
                
                rect.top = rh+dp2px(5) ;
                rect.bottom = height+dp2px(5);
 
                canvas.drawRect(rect, paint);
                canvas.drawText(String.valueOf(value), rect.left, rect.top-5, paint);
            }
            
            for (int i = 0; i < averageGrade.length; i++) {
                int value = averageGrade[i];
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor("#ED7D31"));
                paint.setTextSize(40);
                Rect rect = new Rect();
 
                int leftTag;
                if (isBadge) {
                	leftTag=35;
				}else {
					leftTag=30;
				}
                
                rect.left = (int) ((dp2px(leftTag) + step * (i + 1))-55+setDistance);
                rect.right = (int) ((dp2px(leftTag) + step * (i + 1))-55+setDistance+dp2px(30)-50);
                int rh = (int) (leftHeight - hPerHeight*value/currentRange );
                if (isBadge) {
                	rh = (int) (leftHeight - (hPerHeight*value/currentRange));
				}
                rect.top = rh+dp2px(5) ;
                rect.bottom = height+dp2px(5);
 
                canvas.drawRect(rect, paint);
                canvas.drawText(String.valueOf(value), rect.left, rect.top-5, paint);
            }
            
            
            
        }
 
    }
 
    private int dp2px(int value) {
        float v = getContext().getResources().getDisplayMetrics().density;
        return (int) (v * value + 0.5f);
    }
 
    private int sp2px(int value) {
        float v = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (v * value + 0.5f);
    }
 
    
    public void setStudentMap(LinkedHashMap<String, Integer> studentMap) {
    	
    	
        this.studentMap = studentMap;
        
        studentGrade = new int[studentMap.size()]; 
        courses = new String[studentMap.size()];
        
        Iterator iterator = studentMap.entrySet().iterator();
        int j = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Integer value = (Integer) entry.getValue();
            studentGrade[j] = value;
            courses[j] = key;
            j++;
        }
    }
    
    public void setAverageMap(LinkedHashMap<String, Integer> averageMap) {
    	
        this.averageMap = averageMap;
        
        averageGrade = new int[averageMap.size()]; 
        courses = new String[averageMap.size()];
        if(averageMap.size()>7)
        	setDistance = 95-dp2px(30);
        else setDistance = 105-dp2px(30);
        Iterator iterator = averageMap.entrySet().iterator();
        int j = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Integer value = (Integer) entry.getValue();
            averageGrade[j] = value;
            courses[j] = key;
            j++;
        }
    }
    
    public void setYSteps(boolean isOneCourse)
    {
    	/*if(isOneCourse)
    		ySteps = yStepsOne;
    	else ySteps = yStepsAll;*/
    	ySteps = yStepsOne;
    }

	public void setSteps(String[] steps) {
		currentRange=Integer.valueOf(steps[0])/5;
		isBadge = true;
		yStepsOne=steps;
		
	}
    
}
