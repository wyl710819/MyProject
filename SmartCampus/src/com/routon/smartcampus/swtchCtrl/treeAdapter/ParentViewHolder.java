package com.routon.smartcampus.swtchCtrl.treeAdapter;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.routon.edurelease.R;



public class ParentViewHolder extends BaseViewHolder {

    private Context mContext;
    private View view;

//    private RelativeLayout containerLayout;
//    private TextView parentLeftView;
//    private TextView parentRightView;
//    private ImageView expand;
//    private View parentDashedView;
	private LinearLayout containerLayout;
	private TextView tvTitle;

    private ImageView expand;
    private View mDivider;

    public ParentViewHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        this.view = itemView;
        
        
        tvTitle = (TextView) view.findViewById(R.id.title);
        expand = (ImageView) view.findViewById(R.id.expand);
        containerLayout = (LinearLayout) view.findViewById(R.id.container);
        mDivider = view.findViewById(R.id.divide);
    }

    public void bindView(final DataBean dataBean, final int pos, final ItemClickListener listener){
     
    
    	if (dataBean.type != DataBean.PARENT_ITEM)
    	{
    		return;
    	}
		tvTitle.setText(dataBean.pName);
		

	

        if (dataBean.isExpand) {
            expand.setRotation(90);
            
        } else {
            expand.setRotation(0);
         
        }

//        if (dataBean.isExpand)
//        {
//        	new Handler().post(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					 listener.onExpandChildren(dataBean);                
//		             dataBean.isExpand = true;
//		             rotationExpandIcon(0, 90);
//		             mDivider.setVisibility(View.INVISIBLE);
//				}
//			});
//        	
//        }
        
        //父布局OnClick监听
        containerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    if (dataBean.isExpand) {
                        listener.onHideChildren(dataBean);
                        dataBean.isExpand = false;
                        rotationExpandIcon(90, 0);
                        mDivider.setVisibility(View.VISIBLE);
                    } else {
                        listener.onExpandChildren(dataBean);                
                        dataBean.isExpand = true;
                        rotationExpandIcon(0, 90);
                        mDivider.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void rotationExpandIcon(float from, float to) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);//属性动画
            valueAnimator.setDuration(500);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    expand.setRotation((Float) valueAnimator.getAnimatedValue());
                }
            });
            valueAnimator.start();
        }
    }
}
