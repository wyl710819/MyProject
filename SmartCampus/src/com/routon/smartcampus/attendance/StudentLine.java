package com.routon.smartcampus.attendance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class StudentLine extends View{
	private Paint paint;
	private Path path;
	private int indexX;
	
	public StudentLine(Context context){
		this(context, null);
	}
	
	public StudentLine(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public StudentLine(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void init(){
		paint = new Paint();
		paint.setColor(Color.parseColor("#cccccc"));
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
		path = new Path();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(indexX <= 0){
			return;
		}
		path.reset();
		path.moveTo(getLeft(), getHeight()-2);
		path.lineTo(indexX-10, getHeight()-2);
		path.lineTo(indexX, getHeight()-34);
		path.lineTo(indexX+10,  getHeight()-2);
		path.lineTo(getRight(), getHeight()-2);
		canvas.drawPath(path, paint);
		
	}
	
	public void setIndexX(int indexX){
		this.indexX = indexX;
		invalidate();
	}
}
