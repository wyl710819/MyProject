package com.routon.smartcampus.swtchCtrl;

import java.util.List;

import com.routon.edurelease.R;
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

public class ListGradeAdapter extends RecyclerView.Adapter<ListGradeAdapter.ClassHolder>{
	
	
	public interface ListGradeListener {
		
		void onItemClick(View view, int position);
		
		void onItemSwitchClick(View view,int position, boolean isChecked);
	}	
	
	class ClassHolder extends ViewHolder{
		TextView tvTitle;
//		Switch swtchView;

		public ClassHolder(View view){
			super(view);
			tvTitle = (TextView)view.findViewById(R.id.title);
//			swtchView = (Switch)view.findViewById(R.id.switchView);
//			imgClassInfo = (ImageView)view.findViewById(R.id.img_class_device_info);
//			imgClassSearch = (ImageView)view.findViewById(R.id.img_class_device_search);
		}
	}
	
	private Context context;
	private List<TerminalGroup> mGroups;
	private ListGradeListener listener;

	
	public ListGradeAdapter(Context context, List<TerminalGroup> mGroups){
		this.context = context;
		this.mGroups = mGroups;
//		this.btDevices = btDevices;
	}
	
	public void setListGradeListener(ListGradeListener listener){
		this.listener = listener;
	}
	public void setListGradeData(List<TerminalGroup> mGroups){
		this.mGroups = mGroups;
	}

	@Override
	public int getItemCount() {
		if (mGroups != null)
		{
			return mGroups.size();
		}
		return 0;
	}

	@Override
	public void onBindViewHolder(ClassHolder holder, final int position) {
		final TerminalGroup itemData = mGroups.get(position);
		holder.tvTitle.setText(itemData.pName);
		

		boolean allDown = SwtchCtrlDataRequest.isSwtcherAllDown(itemData);
		
//		holder.swtchView.setOnCheckedChangeListener(null);
//		holder.swtchView.setChecked(!allDown);
//	
//		holder.swtchView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				// TODO Auto-generated method stub
//				if(listener != null){
//					listener.onItemSwitchClick(buttonView,position, isChecked);
//				}
//				
//			}
//		});
		holder.itemView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				if(listener != null){
					listener.onItemClick(v, position);
				}
			}
		});

	}

	@Override
	public ClassHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_swtch_ctrl_toplist, arg0, false);
		return new ClassHolder(view);
	}
	
}

