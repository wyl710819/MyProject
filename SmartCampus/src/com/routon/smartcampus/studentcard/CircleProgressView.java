package com.routon.smartcampus.studentcard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleProgressView extends View {

    private static final String TAG = "CircleProgressBar";

    private int mMaxProgress = 100;

    private final int mCircleLineStrokeWidth = 16;

    private final int mTxtStrokeWidth = 2;

    // 画圆所在的距形区域
    private final RectF mRectF;
    private final Paint mPaint;

    private final Context mContext;
    
    private int mStepDone = 0;
    private int mStepGoal = 0;
    private int mProgress = 0;
    private boolean mShowProgressTextInCenter = false;

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mRectF = new RectF();
        mPaint = new Paint();
    }
    
    public CircleProgressView(Context context) {
        super(context);

        mContext = context;
        mRectF = new RectF();
        mPaint = new Paint();
    }
    
    public void setStep(int stepDone, int stepGoal) {
        this.mStepDone = stepDone;
        this.mStepGoal = stepGoal;
        int progess = (stepDone * 100) / stepGoal;
        if (progess > 100) {
            setProgress(100);
        } else {
            setProgress(progess);
        }
        mShowProgressTextInCenter = false;
    }
    
    public void showProgressTextInCenter(){
    	mShowProgressTextInCenter = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        // 设置画笔相关属性
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.rgb(0xff, 0xff, 0xff));
        canvas.drawColor(Color.TRANSPARENT);
        mPaint.setStrokeWidth(mCircleLineStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);       
        
        // 位置
        mRectF.left = mCircleLineStrokeWidth / 2; // 左上角x
        mRectF.top = mCircleLineStrokeWidth / 2; // 左上角y
        mRectF.right = width - mCircleLineStrokeWidth / 2; // 左下角x
        mRectF.bottom = height - mCircleLineStrokeWidth / 2; // 右下角y

        // 绘制圆圈，进度条背景
        canvas.drawArc(mRectF, -90, 360, false, mPaint);
        mPaint.setColor(Color.rgb(0xf8, 0x60, 0x30));
        canvas.drawArc(mRectF, -90, ((float) mProgress / mMaxProgress) * 360, false, mPaint);

        // 绘制进度文案显示
//        mPaint.setStrokeWidth(mTxtStrokeWidth);
//        String text = mProgress + "%";
//        int textHeight = height / 4;
//        mPaint.setTextSize(textHeight);
//        int textWidth = (int) mPaint.measureText(text, 0, text.length());
//        mPaint.setStyle(Paint.Style.FILL);
//        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + textHeight / 2, mPaint);
        String text = mProgress+"%";
        if( mShowProgressTextInCenter == false ){
        	mPaint.setStrokeWidth(mTxtStrokeWidth);
        	text = mStepDone + "步";
        }
        int textHeight = height / 5;
        mPaint.setTextSize(textHeight);
        int textWidth = (int) mPaint.measureText(text, 0, text.length());
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + textHeight / 4, mPaint);
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        this.invalidate();
    }
}