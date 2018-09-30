package com.routon.inforelease.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

//public class StrokeTextView extends TextView{
//	public StrokeTextView(Context context) {  
//        super(context);  
//    }  
//
//    public StrokeTextView(Context context, AttributeSet attrs) {  
//        super(context, attrs);   
//    }  
//
//    public StrokeTextView(Context context, AttributeSet attrs, int defStyle) {  
//        super(context, attrs, defStyle);  
//
//    }
//    
//    TextPaint strokePaint = null;
//    
//   
//    
//    @Override
//	public void onDraw(Canvas canvas) {
//
//		// lazy load
//		if (strokePaint == null) {
//			strokePaint = new TextPaint();
//		}
//		// 复制原来TextViewg画笔中的一些参数
//		TextPaint paint = getPaint();
//		strokePaint.setTextSize(paint.getTextSize());
//		strokePaint.setTypeface(paint.getTypeface());
//		strokePaint.setFlags(paint.getFlags());
//		strokePaint.setAlpha(paint.getAlpha());
//
//		// 自定义描边效果
//		strokePaint.setStyle(Style.STROKE);
//		strokePaint.setColor(Color.WHITE);
//		strokePaint.setStrokeWidth(paint.getTextSize()/2);
//
//		String text = getText().toString();
//		//在文本底层画出带描边的文本
//		canvas.drawText(text, (getWidth() - strokePaint.measureText(text)) / 2,
//				getBaseline(), strokePaint);
//		super.onDraw(canvas);
//	}
//
//}



public class StrokeTextView extends TextView {  
    private TextView outlineTextView = null;  
   
    public StrokeTextView(Context context) {  
        super(context);  
      
        outlineTextView = new TextView(context);  
        init();  
    }  
    
    int getOppsiteColor(int colorInt){
  	  int red = (colorInt & 0xff0000) >> 16; 
      int green = (colorInt & 0x00ff00) >> 8; 
      int blue = (colorInt & 0x0000ff); 
      return  Color.rgb(255-red, 255-green, 255-blue);
   }

    public StrokeTextView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
      
        outlineTextView = new TextView(context, attrs);  
        init();  
    }  

    public StrokeTextView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
      
        outlineTextView = new TextView(context, attrs, defStyle);  
        init();  
    }  

    public void init() {  
        TextPaint paint = outlineTextView.getPaint();  
        paint.setStrokeWidth(3);  //描边宽度  
        paint.setStyle(Style.STROKE);  
        outlineTextView.setTextColor(Color.parseColor("#000000"));  //描边颜色  
        outlineTextView.setGravity(getGravity());  
    }  

    @Override  
    public void setLayoutParams (ViewGroup.LayoutParams params) {  
        super.setLayoutParams(params);  
        outlineTextView.setLayoutParams(params);  
    }  

    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
      
        //设置轮廓文字  
        CharSequence outlineText = outlineTextView.getText();  
        if (outlineText == null || !outlineText.equals(this.getText())) {  
            outlineTextView.setText(getText());  
            postInvalidate();  
       }  
        outlineTextView.measure(widthMeasureSpec, heightMeasureSpec);  
    }    
    
    @Override  
    public void setMaxWidth(int width){
    	super.setMaxWidth(width);
    	outlineTextView.setMaxWidth(width);
    }
    
    public void setTextSize(int unit,float size){
    	super.setTextSize(unit, size);
    	outlineTextView.getPaint().setStrokeWidth(this.getTextSize()/10);
    }
    
    @Override  
    public void setTextColor(int color){
    	super.setTextColor(color);
    	outlineTextView.setTextColor(getOppsiteColor(color));
    }

    @Override  
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {  
        super.onLayout(changed, left, top, right, bottom);  
        outlineTextView.layout(left, top, right, bottom);  
    }  

   @Override  
    protected void onDraw(Canvas canvas) {    	   
       outlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,this.getTextSize());  
       outlineTextView.getPaint().setStrokeWidth(this.getTextSize()/(10*getContext().getResources().getDisplayMetrics().density));
       outlineTextView.draw(canvas);    
       super.onDraw(canvas);
    }  
}
