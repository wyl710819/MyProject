package com.routon.smartcampus.view;

import com.routon.edurelease.R;
import com.routon.smartcampus.student.IIndexBarFilter;
import com.routon.smartcampus.student.IPinnedHeader;
import com.routon.smartcampus.student.StudentItemAdapter;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class InitialIndexGridView extends GridView implements IIndexBarFilter{

	private Context mContext;
	private IPinnedHeader mAdapter;
	 
	private View mHeaderView;
	private View mIndexBarView;
	private View mPreviewTextView;

	private boolean mHeaderVisibility=false;
	private boolean mPreviewVisibility=false;
	private boolean mIndexBarVisibility=true;
	    
	    
	private int mHeaderViewWidth;
	private int 	mHeaderViewHeight;
	private int 	mIndexBarViewWidth;
	private int 	mIndexBarViewHeight;
	private int 	mIndexBarViewMargin;    	
	private int 	mPreviewTextViewWidth;
	private int 	mPreviewTextViewHeight;
	    
	private float mIndexBarY;


	public InitialIndexGridView(Context context) {
        super(context);
        this.mContext = context;
    }

    
    public InitialIndexGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    
    public InitialIndexGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }
    
    @Override
    public void setAdapter(ListAdapter adapter) {
    	this.mAdapter = (StudentItemAdapter)adapter;
    	super.setAdapter(adapter);
    }
   
   
   public void setIndexBarView(View indexBarView) {
		mIndexBarViewMargin = (int)mContext.getResources().getDimension(R.dimen.index_bar_view_margin);
		this.mIndexBarView = indexBarView;
	}
	

	public void setPreviewView(View previewTextView) {
		this.mPreviewTextView=previewTextView;
	}
	
   
   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       super.onMeasure(widthMeasureSpec, heightMeasureSpec);

       if (mHeaderView != null) {           
       	measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
           mHeaderViewWidth = mHeaderView.getMeasuredWidth();
           mHeaderViewHeight = mHeaderView.getMeasuredHeight();
       }  
  
       if (mIndexBarView != null && mIndexBarVisibility) {           
       	measureChild(mIndexBarView, widthMeasureSpec, heightMeasureSpec);
       	mIndexBarViewWidth = mIndexBarView.getMeasuredWidth();
       	mIndexBarViewHeight = mIndexBarView.getMeasuredHeight();
       } 
      
       if (mPreviewTextView != null && mPreviewVisibility) {           
	       	measureChild(mPreviewTextView, widthMeasureSpec, heightMeasureSpec);
	       	mPreviewTextViewWidth = mPreviewTextView.getMeasuredWidth();
	       	mPreviewTextViewHeight = mPreviewTextView.getMeasuredHeight();
       } 
   }

   
   @Override
   protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
   	super.onLayout(changed, left, top, right, bottom);

   	if (mHeaderView != null) {
           mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
           configureHeaderView(getFirstVisiblePosition());
       }
   	        
   	if (mIndexBarView != null && mIndexBarVisibility) {
   		mIndexBarView.layout(getMeasuredWidth()- mIndexBarViewMargin - mIndexBarViewWidth, mIndexBarViewMargin
   				, getMeasuredWidth()- mIndexBarViewMargin, getMeasuredHeight()- mIndexBarViewMargin);
       }
   	
   	if (mPreviewTextView != null && mPreviewVisibility) {
   		mPreviewTextView.layout(mIndexBarView.getLeft()-mPreviewTextViewWidth, (int)mIndexBarY-(mPreviewTextViewHeight/2)
  				, mIndexBarView.getLeft(), (int)(mIndexBarY-(mPreviewTextViewHeight/2))+mPreviewTextViewHeight);
       }
   }
   
   
   public void setIndexBarVisibility(Boolean isVisible) {
       if(isVisible) {
           mIndexBarVisibility=true;
       }
       else {
           mIndexBarVisibility=false;
       }
   }
   
   
   private void setPreviewTextVisibility(Boolean isVisible) {
   	if(isVisible) {
   	    mPreviewVisibility=true;
   	}
   	else {
   	    mPreviewVisibility=false;       
   	}
   }
   
  
	public void configureHeaderView(int position) {
       if (mHeaderView == null) {
           return;
       }

       int state = mAdapter.getPinnedHeaderState(position);
       
       switch (state) {
           
           case IPinnedHeader.PINNED_HEADER_GONE: 
               mHeaderVisibility = false;
               break;            
           case IPinnedHeader.PINNED_HEADER_VISIBLE: 
               if (mHeaderView.getTop() != 0) {
                   mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
               }
               mAdapter.configurePinnedHeader(mHeaderView, position);
               mHeaderVisibility = true;
               break;            
           case IPinnedHeader.PINNED_HEADER_PUSHED_UP: 
               View firstView = getChildAt(0);
               int bottom = firstView.getBottom();
               int headerHeight = mHeaderView.getHeight();
               int y;
               if (bottom < headerHeight) {
                   y = (bottom - headerHeight);
               }
               else {
                   y = 0;
               }
              
               if (mHeaderView.getTop() != y) {
                   mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
               }
               mAdapter.configurePinnedHeader(mHeaderView, position); 
               mHeaderVisibility = true;
               break;
       }
   }

   @Override
   protected void dispatchDraw(Canvas canvas) {
   	super.dispatchDraw(canvas);
      
       if (mHeaderView != null && mHeaderVisibility) {
           drawChild(canvas, mHeaderView, getDrawingTime()); 
       }
   	if (mIndexBarView != null && mIndexBarVisibility) {  
   		drawChild(canvas, mIndexBarView, getDrawingTime()); 
   	}
       if (mPreviewTextView != null && mPreviewVisibility) { 
       	drawChild(canvas, mPreviewTextView, getDrawingTime()); 
       }
   }
	


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		
		if (mIndexBarView != null && ((IndexBarView)mIndexBarView).onTouchEvent(event)) {
		    setPreviewTextVisibility(true);
		   
			return false;
			
		}
		else {
		    setPreviewTextVisibility(false);
			return super.onTouchEvent(event);
		}
	}

	
	@Override
	public void filterList(float indexBarY, int position,String previewText) {
		this.mIndexBarY=indexBarY;

		if(mPreviewTextView instanceof TextView)
			((TextView)mPreviewTextView).setText(previewText);
		
		setSelection(position);
	}
	
	@Override
	public void setOnItemClickListener(android.widget.AdapterView.OnItemClickListener listener) {
		// TODO Auto-generated method stub
		super.setOnItemClickListener(listener);
	}
	
	
}

