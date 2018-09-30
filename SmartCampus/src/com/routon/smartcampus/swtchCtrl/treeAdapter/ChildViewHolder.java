package com.routon.smartcampus.swtchCtrl.treeAdapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.routon.inforelease.json.TerminalListSwtchBean;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.smartcampus.swtchCtrl.ListDetailAdapter.ListDetailListener;
import com.routon.edurelease.R;

public class ChildViewHolder extends BaseViewHolder {

    private Context mContext;
    private View view;
    
	TextView tvTitle;
	ImageView leftSwtch;
	ImageView midSwtch;
	ImageView rightSwtch;
	private View mDivider;

//	private ListDetailListener listener;
	
    public ChildViewHolder(Context mContext, View itemView) {
        super(itemView);
        this.mContext = mContext;
        this.view = itemView;
        
        tvTitle = (TextView)view.findViewById(R.id.title);
		leftSwtch = (ImageView)view.findViewById(R.id.leftSwitch);
		midSwtch = (ImageView)view.findViewById(R.id.midSwitch);
		rightSwtch = (ImageView)view.findViewById(R.id.rightSwitch);
		mDivider = view.findViewById(R.id.divide);
		
    }

    public void bindView(final DataBean dataBean, final int pos, final ListDetailListener childClickLister){

      
//      
		final TerminalListdatasBean itemData = dataBean.mTerminaldata;
		String online = itemData.txtTerminalState == 1 ? "在线" : "离线";
		String title = itemData.bsgroup + " " + online;
		tvTitle.setText(title);
	
		leftSwtch.setVisibility(View.INVISIBLE);
		midSwtch.setVisibility(View.INVISIBLE);
		rightSwtch.setVisibility(View.INVISIBLE);
	
		if (dataBean.isLast)
		{
			mDivider.setVisibility(View.VISIBLE);
		}else
		{
			mDivider.setVisibility(View.INVISIBLE);
		}
		setSwtchsImage(itemData);
		
		leftSwtch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				int toStatus = swtchTerminalStatus(itemData, 1);
				if (childClickLister != null)
				{
					childClickLister.onSwtchClick(itemData, 1);
				}
				ImageView imageView = (ImageView) v;
				if (toStatus == 1)
				{
					imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_green,null));
				}else if (toStatus == 0)
				{
					imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_red,null));		
				}
				
			}
		});
		midSwtch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int toStatus =  swtchTerminalStatus(itemData, 2);
				if (childClickLister != null)
				{
					childClickLister.onSwtchClick(itemData, 2);
				}
				
				ImageView imageView = (ImageView) v;
				if (toStatus == 1)
				{
					imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_green,null));
				}else if (toStatus == 0)
				{
					imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_red,null));		
				}
			}
		});
		rightSwtch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int toStatus = swtchTerminalStatus(itemData, 3);
				if (childClickLister != null)
				{
					childClickLister.onSwtchClick(itemData, 3);
				}
				
				ImageView imageView = (ImageView) v;
				if (toStatus == 1)
				{
					imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_green,null));
				}else if (toStatus == 0)
				{
					imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_red,null));		
				}
			}
		});
//		holder.itemView.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//
//				if(listener != null){
//					listener.onItemClick(v, position);
//				}
//			}
//		});

	

        
    }
    
    private int swtchTerminalStatus(final TerminalListdatasBean itemData,int swtch)
	{
		int newStatus = -1;
		for (TerminalListSwtchBean item :itemData.mswtchs)
		{
			if (item.swtch == swtch)
			{

				int toStatus = item.status == 1 ? 0 : 1;
				item.status = toStatus;
				newStatus = toStatus;
			}
		}
		return newStatus;
	}
    
    private void setSwtchsImage(TerminalListdatasBean itemData)
	{
		if (itemData.mswtchs != null)
		{
			for (TerminalListSwtchBean swtch:itemData.mswtchs)
			{
				if (swtch.swtch == 1)
				{
					if (swtch.status == 1)
					{
						leftSwtch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_green,null));
					}else
					{
						leftSwtch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_red,null));		
					}		
					leftSwtch.setVisibility(View.VISIBLE);
				}
				else if (swtch.swtch == 2)
				{
					if (swtch.status == 1)
					{
						midSwtch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_green,null));
					}else
					{
						midSwtch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_red,null));		
					}		
					midSwtch.setVisibility(View.VISIBLE);
				}
				else if (swtch.swtch == 3)
				{
					if (swtch.status == 1)
					{
						rightSwtch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_green,null));
					}else
					{
						rightSwtch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.swtch_red,null));		
					}
					rightSwtch.setVisibility(View.VISIBLE);
				}
				
			}
		}
	}
}
