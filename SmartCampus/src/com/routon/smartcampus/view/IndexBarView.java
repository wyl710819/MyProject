package com.routon.smartcampus.view;

import java.util.ArrayList;
import java.util.Locale;

import com.routon.edurelease.R;
import com.routon.smartcampus.student.IIndexBarFilter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class IndexBarView extends View{
    private float mIndexbarMargin;

    private  float mSideIndexY;

    public boolean mIsIndexing = false;

    private int mCurrentSectionPosition = -1;

    private  ArrayList<Integer> mListSections;

    private ArrayList<String> mListItems;

    private  Paint mIndexPaint;

    private Context mContext;

    
    private IIndexBarFilter mIndexBarFilter;

    
    public IndexBarView(Context context) {
        super(context);
        this.mContext = context;
    }

    
    public IndexBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }
    

    public IndexBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }
    

    public void setData(InitialIndexGridView listView, ArrayList<String> listItems,ArrayList<Integer> listSections) {
        this.mListItems = listItems;
        this.mListSections = listSections;
        
        mIndexBarFilter = listView;

        mIndexbarMargin = mContext.getResources().getDimension(R.dimen.index_bar_view_margin);

        mIndexPaint = new Paint();
        mIndexPaint.setColor(mContext.getResources().getColor(R.color.black));
        mIndexPaint.setAntiAlias(true);
        mIndexPaint.setTextSize(mContext.getResources().getDimension(R.dimen.index_bar_view_text_size));
    }

    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mListSections != null && mListSections.size() > 1) {
            float sectionHeight = (getMeasuredHeight() - 2 * mIndexbarMargin)/ mListSections.size();
            float paddingTop = (sectionHeight - (mIndexPaint.descent() - mIndexPaint.ascent())) / 2;

            for (int i = 0; i < mListSections.size(); i++) {
                float paddingLeft = (getMeasuredWidth() - mIndexPaint.measureText(getSectionText(mListSections.get(i)))) / 2;

                canvas.drawText(getSectionText(mListSections.get(i)),
                        paddingLeft,
                        mIndexbarMargin + (sectionHeight * i) + paddingTop + mIndexPaint.descent(),
                        mIndexPaint);
            }
        }
        super.onDraw(canvas);
    }

    
    public String getSectionText(int sectionPosition) {
        return mListItems.get(sectionPosition).substring(0, 1).toUpperCase(Locale.getDefault());
    }

    
    private  boolean contains(float x, float y) {
        return (x >= getLeft() && y >= getTop() && y <= getTop() + getMeasuredHeight());
    }

    
    private void filterListItem(float sideIndexY) {
        mSideIndexY = sideIndexY;

        mCurrentSectionPosition = (int) (((mSideIndexY) - getTop() - mIndexbarMargin) /
                                    ((getMeasuredHeight() - (2 * mIndexbarMargin)) / mListSections.size()));

        if (mCurrentSectionPosition >= 0 && mCurrentSectionPosition < mListSections.size()) {
            int position = mListSections.get(mCurrentSectionPosition);
            String previewText = mListItems.get(position).substring(0, 1).toUpperCase(Locale.getDefault());
            mIndexBarFilter.filterList(mSideIndexY, position, previewText);
        }
    }

    
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            
            case MotionEvent.ACTION_DOWN:
                if (contains(ev.getX(), ev.getY())) {
                    mIsIndexing = true;
                    filterListItem(ev.getY());
                    return true;
                }
                else {
                    mCurrentSectionPosition = -1;
                    return false;
                }
            case MotionEvent.ACTION_MOVE:
                if (mIsIndexing) {
                    if (contains(ev.getX(), ev.getY())) {
                        filterListItem(ev.getY());
                        return true;
                    }
                    else {
                        mCurrentSectionPosition = -1;
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsIndexing) {
                    mIsIndexing = false;
                    mCurrentSectionPosition = -1;
                }
                break;
        }
        return false;
    }
}
