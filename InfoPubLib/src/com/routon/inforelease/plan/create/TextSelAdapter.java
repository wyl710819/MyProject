package com.routon.inforelease.plan.create;

import java.util.ArrayList;

import com.routon.inforelease.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TextSelAdapter extends BaseAdapter{
	private final String TAG = "TextSelAdapter";
	private LayoutInflater mInflater; 
	private Context mContext; 
	private ArrayList<MaterialItem> datas = null;

	public ArrayList<MaterialItem> selectMaterails = new ArrayList<MaterialItem>(); 
	
	public TextSelAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	public void setDatas(ArrayList<MaterialItem> datas){
		this.datas = datas;
	}
	
	@Override
	public int getCount() {
		if(datas == null){
			return 0;
		}

		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		if(datas == null){
			return null;
		}
		
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder(); 
			convertView = mInflater.inflate(R.layout.text_select_item, null);

			holder.textView = (TextView)convertView.findViewById(R.id.text_view);
			holder.checkBox = (CheckBox)convertView.findViewById(R.id.item_select);
			
			convertView.setTag(holder);
			
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
				
		holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				MaterialItem dataItem = datas.get(position);
				
				if(isItemInSelectList(selectMaterails, dataItem) && isChecked == false){
//					selectMaterails.remove(dataItem);
					removeItemInList(dataItem);
				}
				
				if(isChecked && !isItemInSelectList(selectMaterails, dataItem)){
					selectMaterails.add(dataItem);
				}
			}
		});
		
		MaterialItem dataItem = datas.get(position);
		
		holder.textView.setText(dataItem.getContent());
		holder.checkBox.setChecked(isItemInSelectList(selectMaterails, dataItem));

//		Log.i(TAG, "----------------holder.textView:"+holder.textView.getText());
		return convertView;
	}

	public class ViewHolder{
		TextView textView;
		CheckBox checkBox;
	}
	
	private boolean isItemInSelectList(ArrayList<MaterialItem> lists, MaterialItem item){
		for (MaterialItem materialItem : lists) {
			if(materialItem.getId() == item.getId()){
				Log.i(TAG, "materialItem id: "+ item.getId());
				return true;
			}
		}
		
		return false;
	}
	
	private void removeItemInList(MaterialItem item){
		for (MaterialItem materialItem : selectMaterails) {
			if(materialItem.getId() == item.getId()){
				selectMaterails.remove(materialItem);
				return;
			}
		}
	}
}
