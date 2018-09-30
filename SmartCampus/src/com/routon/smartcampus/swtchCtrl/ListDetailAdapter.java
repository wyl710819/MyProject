package com.routon.smartcampus.swtchCtrl;

import java.util.List;

import com.routon.edurelease.R;
import com.routon.inforelease.json.TerminalListSwtchBean;
import com.routon.inforelease.json.TerminalListdatasBean;
import com.routon.smartcampus.answerrelease.service.BtDevice;
import com.routon.smartcampus.attendance.ClassDeviceListener;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class ListDetailAdapter extends RecyclerView.Adapter<ListDetailAdapter.ClassHolder>{
	
	
	public interface ListDetailListener {
		
		void onItemClick(View view, int position);		
		void onSwtchClick(TerminalListdatasBean terminal, int swtchValue);
	}	
	
	class ClassHolder extends ViewHolder{
		TextView tvTitle;
		ImageView leftSwtch;
		ImageView midSwtch;
		ImageView rightSwtch;
		public ClassHolder(View view){
			super(view);
			tvTitle = (TextView)view.findViewById(R.id.title);
			leftSwtch = (ImageView)view.findViewById(R.id.leftSwitch);
			midSwtch = (ImageView)view.findViewById(R.id.midSwitch);
			rightSwtch = (ImageView)view.findViewById(R.id.rightSwitch);
		}
	}
	
	private Context context;
	private TerminalGroup mGroups;
	private ListDetailListener listener;
	
	public ListDetailAdapter(Context context, TerminalGroup mGroups){
		this.context = context;
		this.mGroups = mGroups;
//		this.btDevices = btDevices;
	}
	
	public void setListGradeListener(ListDetailListener listener){
		this.listener = listener;
	}

	public void setTerminalGroup(TerminalGroup mGroups)
	{
		this.mGroups = mGroups;
	}
	@Override
	public int getItemCount() {
		int count = 0;
		if (this.mGroups != null)
		{
			count = this.mGroups.terminals.size();
			
		}
		return count;
	}

	private void setSwtchsImage(TerminalListdatasBean itemData,ClassHolder holder)
	{
		if (itemData.mswtchs != null)
		{
			for (TerminalListSwtchBean swtch:itemData.mswtchs)
			{
				if (swtch.swtch == 1)
				{
					if (swtch.status == 1)
					{
						holder.leftSwtch.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_green,null));
					}else
					{
						holder.leftSwtch.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_red,null));		
					}		
					holder.leftSwtch.setVisibility(View.VISIBLE);
				}
				else if (swtch.swtch == 2)
				{
					if (swtch.status == 1)
					{
						holder.midSwtch.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_green,null));
					}else
					{
						holder.midSwtch.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_red,null));		
					}		
					holder.midSwtch.setVisibility(View.VISIBLE);
				}
				else if (swtch.swtch == 3)
				{
					if (swtch.status == 1)
					{
						holder.rightSwtch.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_green,null));
					}else
					{
						holder.rightSwtch.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_red,null));		
					}
					holder.rightSwtch.setVisibility(View.VISIBLE);
				}
				
			}
		}
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
	@Override
	public void onBindViewHolder(ClassHolder holder, final int position) {
		final TerminalListdatasBean itemData = mGroups.terminals.get(position);
		String online = itemData.txtTerminalState == 1 ? "在线" : "离线";
		String title = itemData.bsgroup + " " + online;
		holder.tvTitle.setText(title);
	
		holder.leftSwtch.setVisibility(View.INVISIBLE);
		holder.midSwtch.setVisibility(View.INVISIBLE);
		holder.rightSwtch.setVisibility(View.INVISIBLE);
	
		
		setSwtchsImage(itemData,holder);
		
		holder.leftSwtch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				int toStatus = swtchTerminalStatus(itemData, 1);
				if (listener != null)
				{
					listener.onSwtchClick(itemData, 1);
				}
				ImageView imageView = (ImageView) v;
				if (toStatus == 1)
				{
					imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_green,null));
				}else if (toStatus == 0)
				{
					imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_red,null));		
				}
				
			}
		});
		holder.midSwtch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int toStatus =  swtchTerminalStatus(itemData, 2);
				if (listener != null)
				{
					listener.onSwtchClick(itemData, 2);
				}
				
				ImageView imageView = (ImageView) v;
				if (toStatus == 1)
				{
					imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_green,null));
				}else if (toStatus == 0)
				{
					imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_red,null));		
				}
			}
		});
		holder.rightSwtch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int toStatus = swtchTerminalStatus(itemData, 3);
				if (listener != null)
				{
					listener.onSwtchClick(itemData, 3);
				}
				
				ImageView imageView = (ImageView) v;
				if (toStatus == 1)
				{
					imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_green,null));
				}else if (toStatus == 0)
				{
					imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.swtch_red,null));		
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

	@Override
	public ClassHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_swtch_ctrl_detail, arg0, false);
		return new ClassHolder(view);
	}
	
}

