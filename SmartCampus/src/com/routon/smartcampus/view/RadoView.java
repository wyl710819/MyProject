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
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class RadoView extends View {
	
	private int count;
    private int[] radius = new int[8];
    private int maxRadius = radius[radius.length - 1];
    private int[] marks;
    private String[] keys;
    private String[] grade= new String[]{"20", "40", "60", "80", "100","120","140","160"};
    
    private LinkedHashMap<String, Integer> studentMap = new LinkedHashMap<String, Integer>();

    private Paint paintLine;
    private Paint paintMarkPoint;
    private Paint paintText;
    private Paint gradeText;

    private Rect mRect;
    
    private double x;
    private double y;
    private double lastX;
    private double lastY;
    private double[] lastXs=new double[grade.length];
    private double[] lastYs=new double[grade.length];
    private double x2;
    private double y2;
    private double lastX2;
    private double lastY2;
	private int width;
	private int widthTag;
	private LinkedHashMap<String, Integer> averageMap= new LinkedHashMap<String, Integer>();
	 private int[] markS ;
	private int iTag=0;
	private final int setLength = 45;
	private final String TAG = "RadoView";
	public RadoView(Context context) {
		super(context);
		mRect = new Rect();
	}

	public RadoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();  
        initRadius(context);
        widthTag = (width-(radius[radius.length-1]*2))/2;
        mRect = new Rect();
	}

	public RadoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initRadius(context);
		mRect = new Rect();
	}

	public void initRadius(Context context){
		for(int i=0;i<radius.length;i++){
			radius[i] = dip2px(getContext(), getResources().getDimension(R.dimen.grade_rado_per_length)*(i+1));
		}
		maxRadius = radius[radius.length - 1];
		/*radius = new int[]{dip2px(getContext(),18), dip2px(getContext(),36), dip2px(getContext(),54), dip2px(getContext(),72), dip2px(getContext(),90),dip2px(getContext(),108)
	    		,dip2px(getContext(),126),dip2px(getContext(), 144)};*/
	}
	
	@SuppressLint("DrawAllocation")
	  @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	        
	        if (count<=0) {
				return;
			}
	        double littleAngle = 360 / count;
	        Iterator iterator = studentMap.entrySet().iterator();
	        int j = 0;
	        while (iterator.hasNext()) {
	            Map.Entry entry = (Map.Entry) iterator.next();
	            String key = (String) entry.getKey();
	            Integer value = (Integer) entry.getValue();
	            marks[j] = value*maxRadius/Integer.valueOf(grade[grade.length-1]);
	            keys[j] = key;
	            j++;
	        }
	        
	        Iterator iterators = averageMap.entrySet().iterator();
	        int k = 0;
	        while (iterators.hasNext()) {
	            Map.Entry entry = (Map.Entry) iterators.next();
	            String keys = (String) entry.getKey();
	            Integer values = (Integer) entry.getValue();
	            markS[k] = values*maxRadius/Integer.valueOf(grade[grade.length-1]);
//	            keys[k] = key;
	            k++;
	        }

	        paintLine = new Paint();
	        paintLine.setColor(Color.parseColor("#cccccc"));
	        paintLine.setStyle(Paint.Style.STROKE);
	        paintLine.setStrokeWidth(3);

	        paintText = new Paint();
	        paintText.setColor(Color.BLACK);
	        paintText.setTextSize(45);
	        paintText.setAntiAlias(true);
	        
	        gradeText = new Paint();
	        gradeText.setColor(Color.parseColor("#666666"));
	        gradeText.setTextSize(40);
	        iTag = 0;
	        for (int i = 0; i < radius.length; i++) {
	            drawStroke(canvas, littleAngle, radius[i]);
	        }

	        
	        
	        //分数点
	        paintMarkPoint = new Paint();
	        paintMarkPoint.setColor(Color.parseColor("#3F51B5"));
	        paintMarkPoint.setStyle(Paint.Style.FILL);

	        //评分点的连线
	        Paint paintMarkLine = new Paint();
	        paintMarkLine.setAntiAlias(true);
	        paintMarkLine.setColor(Color.parseColor("#5B9BD5"));
	        paintMarkLine.setStyle(Paint.Style.STROKE);
	        paintMarkLine.setStrokeWidth(10);
//	        paintMarkLine.setAlpha(100);

	        Path path = new Path();
	        path.reset();
	        double xTag = getPointX(littleAngle * 0, marks[0])+5;
	        double yTag = getPointY(littleAngle * 0, marks[0]);
	        for (int i = 0; i < count; i++) {
	            x = getPointX(littleAngle * i, marks[i])+widthTag;
	            y = getPointY(littleAngle * i, marks[i])+widthTag;
	            canvas.drawCircle((float) x, (float) y, 0, paintMarkPoint);//点
	            if (i == 0) {
	                path.moveTo((float) x, (float) y);
	            } else {
	                path.lineTo((float) x, (float) y);
	            }
	            if (i==count-1) {
	            	path.lineTo((float)xTag+widthTag, (float)yTag+widthTag);
				}
	            lastX = x;
	            lastY = y;
	        }
	        canvas.drawPath(path, paintMarkLine);
	        
	        
	        Paint paintMarkLine2 = new Paint();
	        paintMarkLine2.setAntiAlias(true);
	        paintMarkLine2.setColor(Color.parseColor("#ED7D31"));
	        paintMarkLine2.setStyle(Paint.Style.STROKE);
	        paintMarkLine2.setStrokeWidth(10);
//	        paintMarkLine2.setAlpha(100);
	        
	        Path path2 = new Path();
	        path2.reset();
	        double xTag2 = getPointX(littleAngle * 0, markS[0])+5;
	        double yTag2 = getPointY(littleAngle * 0, markS[0]);
	        for (int i = 0; i < count; i++) {
	            x2 = getPointX(littleAngle * i, markS[i])+widthTag;
	            y2 = getPointY(littleAngle * i, markS[i])+widthTag;
	            canvas.drawCircle((float) x2, (float) y2, 0, paintMarkPoint);//点
	            if (i == 0) {
	                path2.moveTo((float) x2, (float) y2);
	            } else {
	                path2.lineTo((float) x2, (float) y2);
	            }
	            if (i==count-1) {
	            	path2.lineTo((float)xTag2+widthTag, (float)yTag2+widthTag);
				}
	            lastX2 = x2;
	            lastY2 = y2;
	        }
	        canvas.drawPath(path2, paintMarkLine2);
	        
	        
	        for (int i = 0; i < lastXs.length; i++) {
		        textTag=i;
	            if (textTag>=4) {
	        		canvas.drawText(grade[textTag], (float) (lastXs[i]-90), (float) (lastYs[i]+30 ), gradeText);
				}else {
					canvas.drawText(grade[textTag], (float) (lastXs[i]-70), (float) (lastYs[i]+30 ), gradeText);
				}
				}
	    }

	   //Math.sin()计算参数的是弧度
	    private void drawStroke(Canvas canvas, double littleAngle, double radius) {
	    	 double tagX=getPointX(littleAngle * 0, radius)+widthTag;
	         double tagY=getPointY(littleAngle * 0, radius)+widthTag;
	        for (int i = 0; i < count; i++) {
	            Paint paint = new Paint();
	            paint.setColor(Color.BLACK);
	            paint.setStyle(Paint.Style.FILL);
	            x = getPointX(littleAngle * i, radius)+widthTag;
	            y = getPointY(littleAngle * i, radius)+widthTag;
	           
	            canvas.drawPoint((float) x, (float) y, paint);
//	            canvas.drawLine((float) maxRadius, (float) maxRadius, (float) x, (float) y, paintLine);//圆心与角的连接线
	            if (i > 0) {
	                canvas.drawLine((float) lastX, (float) lastY, (float) x, (float) y, paintLine);//画雷达线
	            }
	            if (i == (count - 1)) {
	                canvas.drawLine((float) x, (float) y, (float) tagX, (float)tagY, paintLine);
	            }
	            lastX = x;
	            lastY = y;
	            if (i == 0) {
	            	lastXs[iTag]=lastX;
	            	lastYs[iTag]=lastY;
//	            	if (textTag==4 || textTag==5) {
//	            		canvas.drawText(grade[textTag], (float) (lastX-80), (float) (lastY+10 ), gradeText);
//					}else {
//						canvas.drawText(grade[textTag], (float) (lastX-60), (float) (lastY+10 ), gradeText);
//					}
	            	iTag+=1;
				}
	            
	            if (radius == maxRadius) {
	                //如果是最外层的园，则加上文字
	            	paintText.getTextBounds(keys[i], 0, keys[i].length(), mRect);
	            	switch (getQr(littleAngle*i)) {
					case 1:
						if (isBadge) {
							x-=dip2px(getContext(),10);
						}
						double angle = 2*Math.PI/360*littleAngle*i;
						canvas.drawText(keys[i], (float) (x +setLength*Math.sin(angle)-mRect.width()/2), 
								(float) (y - setLength*Math.cos(angle)+mRect.height()/2), paintText);
						/*Log.d(TAG, "x="+x+" y="+y+"   txtX="+(x +setLength*Math.sin(angle)-mRect.width()/2
								+" txtY="+(y - setLength*Math.cos(littleAngle*i)+mRect.height()/2)));
						Log.d(TAG, "newtxtX="+(x +setLength*Math.sin(littleAngle*i)
								+" newtxtY="+(y - setLength*Math.cos(littleAngle*i))));*/
						break;
					case 2:
						double angle2 = 2*Math.PI/360*(180-littleAngle*i);
						canvas.drawText(keys[i], (float) (x +setLength*Math.sin(angle2)-mRect.width()/2), 
								(float) (y + setLength*Math.cos(angle2)+mRect.height()/2), paintText);
						break;
					case 3:
						if (isBadge) {
							x+=dip2px(getContext(),10);
						}
						double angle3 = 2*Math.PI/360*(littleAngle*i-180);
						canvas.drawText(keys[i], (float) (x -setLength*Math.sin(angle3)-mRect.width()/2), 
								(float) (y + setLength*Math.cos(angle3)+mRect.height()/2), paintText);
						break;
					case 4:
						double angle4 = 2*Math.PI/360*(360-littleAngle*i);
						canvas.drawText(keys[i], (float) (x -setLength*Math.sin(angle4)-mRect.width()/2), 
								(float) (y - setLength*Math.cos(angle4)+mRect.height()/2), paintText);
						break;

					default:
						break;
					}
	            	/*if (i==0) {
	            		canvas.drawText(keys[i], (float) (lastX - 50), (float) (lastY - 30), paintText);
					}else if (i==1) {
						canvas.drawText(keys[i], (float) (lastX+5 ), (float) (lastY+20 ), paintText);
					}else if (i==2) {
						canvas.drawText(keys[i], (float) (lastX+5 ), (float) (lastY+50 ), paintText);
					}else if (i==3) {
						canvas.drawText(keys[i], (float) (lastX-115 ), (float) (lastY+50 ), paintText);
					}else if (i==4) {
						canvas.drawText(keys[i], (float) (lastX-115 ), (float) (lastY+20 ), paintText);
					}else if (i==5) {
						canvas.drawText(keys[i], (float) (lastX-115 ), (float) (lastY+20 ), paintText);
					}else if (i==6) {
						canvas.drawText(keys[i], (float) (lastX-115 ), (float) (lastY+20 ), paintText);
					}else if (i==7) {
						canvas.drawText(keys[i], (float) (lastX-115 ), (float) (lastY+20 ), paintText);
					}*/
	            	
	            }
	        }
	    }

	    private int textTag=0;
		private boolean isBadge=false;
	    /**
	     * 得到需要计算的角度
	     */
	    private double getNewAngle(double angle) {
	        double res = angle;
	        if (angle >= 0 && angle <= 90) {
	            res = 90 - angle;
	        } else if (angle > 90 && angle <= 180) {
	            res = angle - 90;
	        } else if (angle > 180 && angle <= 270) {
	            res = 270 - angle;
	        } else if (angle > 270 && angle <= 360) {
	            res = angle - 270;
	        }
	        return res;
	    }


	    /**
	     * 若以圆心为原点，返回该角度顶点的所在象限
	     */
	    private int getQr(double angle) {
	        int res = 0;
	        if (angle >= 0 && angle <= 90) {
	            res = 1;
	        } else if (angle > 90 && angle <= 180) {
	            res = 2;
	        } else if (angle > 180 && angle <= 270) {
	            res = 3;
	        } else if (angle > 270 && angle <= 360) {
	            res = 4;
	        }
	        return res;
	    }

	    /**
	     * 返回多边形顶点X坐标
	     */
	    private double getPointX(double angle, double radius) {
	        double newAngle = getNewAngle(angle);
	        double res = 0;
	        double width = radius * Math.cos(newAngle / 180 * Math.PI);
	        int qr = getQr(angle);
	        switch (qr) {
	            case 1:
	            case 2:
	                res = maxRadius + width;
	                break;
	            case 3:
	            case 4:
	                res = maxRadius - width;
	                break;
	            default:
	                break;
	        }
	        return res;
	    }


	    /**
	     * 返回多边形顶点Y坐标
	     */
	    private double getPointY(double angle, double radius) {
	        double newAngle = getNewAngle(angle);
	        double height = radius * Math.sin(newAngle / 180 * Math.PI);
	        double res = 0;
	        int qr = getQr(angle);
	        switch (qr) {
	            case 1:
	            case 4:
	                res = maxRadius - height;
	                break;
	            case 2:
	            case 3:
	                res = maxRadius + height;
	                break;
	            default:
	                break;
	        }
	        return res;
	    }

	    public void setStudentMap(LinkedHashMap<String, Integer> studentMap) {
	        this.studentMap = studentMap;
	        count=studentMap.size();
	        marks = new int[count];
	        keys = new String[count];
	    }
	    
	    public void setAverageMap(LinkedHashMap<String, Integer> averageMap) {
	        this.averageMap = averageMap;
	        count=averageMap.size();
	        markS = new int[count];
	        keys = new String[count];
	    }
	    
	    public void setGrade(String[] grade) {
	    	isBadge = true;
	        this.grade = grade;
//	        lastXs=new double[grade.length-1];
//	        lastYs=new double[grade.length-1];
	    }

	    
	    public static int dip2px(Context context,float dpValue) {

	    	final float scale = context.getResources().getDisplayMetrics().density;

	    	return (int) (dpValue * scale +0.5f);

	    	}

}
