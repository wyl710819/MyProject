package com.routon.inforelease;



import com.routon.common.BaseFragment;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 设置界面
 * @author xiaolp
 *
 */
public class BluetoothFragment extends BaseFragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_bluetooth, container, false);
	}
	
	@Override  
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("BluetoothFragment","onActivityCreated");
		
		this.initTitleBar(R.string.menu_bluetooth_text);

		startRemoteControl();
	}
	
	@Override  
	public void onStart(){
		super.onStart();
		Log.d("BluetoothFragment","onStart");
	}
	
	@Override  
	public void onResume(){
		super.onResume();
		Log.d("BluetoothFragment","onResume");
	}
	
	
	
	public void startRemoteControl(){
		Intent intent = new Intent();
		intent.putExtra("isLib", true);
		intent.setComponent(new ComponentName(this.getActivity().getPackageName(),"com.routon.remotecontrol.MainActivity"));
		getActivity().startActivity(intent);
	}
}
