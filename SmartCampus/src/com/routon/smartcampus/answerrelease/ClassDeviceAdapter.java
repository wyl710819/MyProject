package com.routon.smartcampus.answerrelease;

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
import android.widget.ImageView;
import android.widget.TextView;

public class ClassDeviceAdapter extends RecyclerView.Adapter<ClassDeviceAdapter.ClassHolder>{
	
	private Context context;
	private List<BtDevice> btDevices;
	private ClassDeviceListener listener;
	
	public ClassDeviceAdapter(Context context, List<BtDevice> btDevices){
		this.context = context;
		this.btDevices = btDevices;
	}
	
	public void setClassDeviceListener(ClassDeviceListener listener){
		this.listener = listener;
	}

	@Override
	public int getItemCount() {
		return btDevices.size()+4;
	}

	@Override
	public void onBindViewHolder(ClassHolder holder, final int position) {
		if(position <=1 || position >=getItemCount()-2){
			holder.tvClassName.setText("");
			holder.imgClassInfo.setVisibility(View.INVISIBLE);
		}else {
			final BtDevice btDevice = btDevices.get(position-2);
			holder.tvClassName.setText(btDevice.getClassName());
			holder.imgClassInfo.setVisibility(View.VISIBLE);
			if(btDevice.isChecked() == true){
				holder.tvClassName.setTextSize(19);
				holder.tvClassName.setTextColor(Color.parseColor("#e1e1e1"));
				if(btDevice.getDevice() != null){
					if(btDevice.getStatus().equals("已连接")){
						holder.imgClassInfo.setImageResource(R.drawable.ic_class_connected);
					}else {
						holder.imgClassInfo.setImageResource(getSignalImageRes(btDevice.getRssi()));
					}
				}else {
					holder.imgClassInfo.setVisibility(View.INVISIBLE);
				}
			}else {
				holder.tvClassName.setTextSize(17);
				holder.tvClassName.setTextColor(Color.parseColor("#666666"));
				holder.imgClassInfo.setVisibility(View.INVISIBLE);
			}
			holder.itemView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!btDevice.isChecked()){
						return;
					}
					if(listener != null){
						listener.onItemClick(v, position-2);
					}
				}
			});
		}
	}

	@Override
	public ClassHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_class_device_info, arg0, false);
		return new ClassHolder(view);
	}
	
	class ClassHolder extends ViewHolder{
		TextView tvClassName;
		ImageView imgClassInfo;
		public ClassHolder(View view){
			super(view);
			tvClassName = (TextView)view.findViewById(R.id.tv_class_device_name);
			imgClassInfo = (ImageView)view.findViewById(R.id.img_class_device_info);
		}
	}
	
	private int getSignalImageRes(short rssid) {
		int resId = R.drawable.ic_bluetooth_signal_4;
		if (rssid > -70) {
			resId = R.drawable.ic_bluetooth_signal_1;
		} else if (rssid > -80) {
			resId = R.drawable.ic_bluetooth_signal_2;
		} else if (rssid > -90) {
			resId = R.drawable.ic_bluetooth_signal_3;
		}else {
			resId = R.drawable.ic_bluetooth_signal_4;
		}
		return resId;
	}

}


