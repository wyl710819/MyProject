package com.routon.smartcampus.answerrelease;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

public class LineChartView extends View {
	private Paint pRect;
	private int AvgValue;
	private int WidValue;
	private int WidDip = 100;
	private int maxYValue;
	private int avgYValue;
	private int ySize;// y坐标间隔个数
	private RectF mRect = null; // 绘图矩形区域
	private int num = 7;
	private int position=-1;
	private ArrayList<String> answerList = new ArrayList<String>();
	private ArrayList<Integer> answerPercentList = new ArrayList<Integer>();
    private OnChartClickListener listener;
    private List<Float>yHeightList=new ArrayList<Float>();

	public LineChartView(Context context) {
		super(context);
	}

	public LineChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setLineViewData(ArrayList<String> answerList,
			ArrayList<Integer> answerPercentList,int position) {
		this.answerList = answerList;
		this.answerPercentList = answerPercentList;
		this.position=position;
		List<Integer> yListTemp = new ArrayList<Integer>();
		for (int i = 0; i < answerPercentList.size(); i++) {
			yListTemp.add(answerPercentList.get(i));
		}

		Collections.sort(yListTemp);
		if (answerPercentList.size() > 0) {
			if (yListTemp.get(yListTemp.size() - 1) >= 10) {
				// maxYValue=yListTemp.get(yListTemp.size()-1)+yListTemp.get(yListTemp.size()-1)/5;
				avgYValue = yListTemp.get(yListTemp.size() - 1) / 5;
				maxYValue = yListTemp.get(yListTemp.size() - 1) / 5 * 6;
				ySize = 6;
			} else if (7 < yListTemp.get(yListTemp.size() - 1)
					&& yListTemp.get(yListTemp.size() - 1) < 10) {
				ySize = yListTemp.get(yListTemp.size() - 1);
				maxYValue = yListTemp.get(yListTemp.size() - 1);
				avgYValue = 1;
			} else {
				ySize = 7;
				maxYValue = 7;
				avgYValue = 1;
			}
		}

		Log.d("TAG", "答题柱状图y:" + answerPercentList + "答题柱状图x:" + answerList
				+ "y最大值:" + maxYValue + "y平均值:" + avgYValue + "yListTemp:"
				+ yListTemp);
		yHeightList.clear();
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

		if (specMode == MeasureSpec.EXACTLY) { // fill_parent
			result = specSize;
		} else if (specMode == MeasureSpec.AT_MOST) { // wrap_content
			result = Math.min(result, specSize);
		}
		return result;
	}

	private int measureHeight(int measureSpec) {
		int result = 100;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) { // fill_parent 或者指定常量
			result = specSize;
		} else if (specMode == MeasureSpec.AT_MOST) { // wrap_content
			result = Math.min(result, specSize); // 找最小值
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		mRect = new RectF(0, 0, getWidth(), getHeight());
		AvgValue = (getHeight() - WidDip) / (ySize + 1);
		WidValue = (getWidth() - WidDip) / 9;
		Paint paintTitle = new Paint();
		paintTitle.setTextSize(sp2px(19));
		paintTitle.setColor(Color.parseColor("#666666"));
		paintTitle.setAntiAlias(true);
		// paintTitle.setShadowLayer(1, 3, 3, Color.BLACK);
		Paint paint = new Paint();
		Paint paintLine = new Paint(Paint.ANTI_ALIAS_FLAG); // 虚线画笔
		// Anti-aliasing
		paint.setAntiAlias(true);
		// Draw the Y轴
		paint.setColor(Color.parseColor("#666666"));
		canvas.drawLine(mRect.left + WidDip, mRect.bottom, mRect.left
				+ WidDip, WidValue, paint); // startx,starty,stopx,stopy
		// Draw the X轴
		canvas.drawLine(mRect.left + WidDip, mRect.bottom ,
				mRect.right, mRect.bottom, paint);

		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		paintLine.setStyle(Paint.Style.STROKE);
		paintLine.setColor(Color.parseColor("#666666"));
		PathEffect effects = new DashPathEffect(new float[] { 5, 5, 5, 5 }, 1);
		paintLine.setPathEffect(effects);

		for (int i = 0; i < ySize; i++) {
			Path path = new Path();
			path.moveTo(mRect.left + WidDip, mRect.top + AvgValue * (i + 1)+WidValue);
			path.lineTo(mRect.right, mRect.top + AvgValue * (i + 1)+WidValue);
			canvas.drawPath(path, paintLine);

			paint.setTextSize(sp2px(16)); // 设置为条码宽

	

			// 方法 2
			Rect bounds1 = new Rect();
			if((ySize-i)!=0){
				paint.getTextBounds("" + (ySize - i) * avgYValue, 0, 1, bounds1);
				int fontHeight = bounds1.height();
				canvas.drawText("" + (ySize - i) * avgYValue, 30, mRect.top
						+ AvgValue * (i + 1) + fontHeight / 2+WidValue, paint);
			}
		}

		// 画矩形
		pRect = new Paint();
		pRect.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
		pRect.setColor(Color.parseColor("#999999"));
		pRect.setStyle(Paint.Style.FILL);// 设置填满
		for (int i = 0; i < answerList.size(); i++) {

			Log.i("left top  right bottom", "" + WidValue * (2 * i + 1) + "  "
					+ (4 - i) * getHeight() / 4 + "  " + WidValue * (2 * i)
					+ " " + " " + getHeight());
			
			float yHeight = (float) (answerPercentList.get(i))
					* ((float) (AvgValue * ySize)) / maxYValue;
			yHeightList.add(yHeight);
			if (answerPercentList.size() > 0) {
				if(i==position){
					pRect.setColor(Color.parseColor("#a4fa03"));
				}else{
					pRect.setColor(Color.parseColor("#999999"));
				}
				canvas.drawRect(180 + i * (mRect.right - mRect.left - WidValue+50)
						/ (answerList.size() + 1), getHeight()
						- yHeight+2, 190 + (2 * i + 1)
						* (mRect.right - mRect.left - WidValue+50)
						/ (2 * (answerList.size() + 1)), getHeight()
						- 1, pRect);// 长方形
			}
			

			paint.setTextSize(sp2px(16));
			Rect countRect = new Rect();
			paint.setColor(Color.parseColor("#cccccc"));
			paint.getTextBounds(String.valueOf(answerPercentList.get(i)), 0, String.valueOf(answerPercentList
					.get(i)).length(), countRect);
			canvas.drawText(
					String.valueOf(answerPercentList.get(i)),
					180
							+ i
							* (mRect.right - mRect.left - WidValue+50)
							/ (answerList.size() + 1)
							+ ((mRect.right - mRect.left - WidValue+50)
									/ (2 * (answerList.size() + 1)) - countRect
										.width()) / 2, getHeight()
							- yHeight - 10, paint);
		}

	}
	/**
	 * 添加柱状图点击事件
	 * */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                //获取点击坐标
                float x = event.getX();
                float y = event.getY();
                //判断点击点的位置
                float leftx = 0;
                float rightx = 0;
 
                for (int i = 0; i < num; i++) {
                    leftx = 180 + i * (mRect.right - mRect.left - WidValue+50)
    						/ (answerList.size() + 1);
                    rightx = 190 + (2 * i + 1)
    						* (mRect.right - mRect.left - WidValue+50)
    						/ (2 * (answerList.size() + 1));
                    if (x < leftx||x>rightx) {
                        continue;
                    }
                    if (leftx <= x && x <= rightx) {
                        //获取点击的柱子区域的y值
                        float top = getHeight()- yHeightList.get(i)+2;
                        float bottom = getHeight()- 1;
 
 
                        if (y >= top && y <= bottom) {
                            //判断是否设置监听
                            //将点击的第几条柱子，点击柱子顶部的坐值，用于弹出dialog提示数据，还要返回百分比currentHeidht = Float.parseFloat(data[num - 1 - i])
                            if(listener != null) {
                                Log.e("ss","x" + x +";y:" + y);
                                listener.onClick(i);
                            }
                            break;
 
                        }
                    }
 
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
                Log.e("touch", "ACTION_MOVE");
                break;
 
            case MotionEvent.ACTION_UP:
                Log.e("touch", "ACTION_UP");
                break;
        }
 
        return true;
    }
 
    /**
     * 柱子点击时的监听接口
     */
    public interface OnChartClickListener {
        void onClick(int pos);
 
    }
 
    /**
     * 设置柱子点击监听的方法
     * @param listener
     */
    public void setOnChartClickListener(OnChartClickListener listener)  {
        this.listener = listener;
    }
	private int sp2px(int value) {
		float v = getContext().getResources().getDisplayMetrics().scaledDensity;
		return (int) (v * value + 0.5f);
	}

}