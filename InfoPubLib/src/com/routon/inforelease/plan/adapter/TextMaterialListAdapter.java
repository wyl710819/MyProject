package com.routon.inforelease.plan.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.routon.inforelease.R;
import com.routon.inforelease.json.PlanMaterialrowsBean;

public class TextMaterialListAdapter extends MaterialListAdapter {
	private final String TAG = "TextSelAdapter";
	
	public TextMaterialListAdapter(Context context, List<PlanMaterialrowsBean> datas) {
		super(context, datas);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder(); 
			convertView = mInflater.inflate(R.layout.text_select_item, null);

			holder.textView = (TextView)convertView.findViewById(R.id.text_view);
			holder.checkBox = (CheckBox)convertView.findViewById(R.id.item_select);
			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					PlanMaterialrowsBean dataItem = (PlanMaterialrowsBean) getItem(position);
					
					if(selectMaterails.contains(dataItem) && isChecked == false){
						selectMaterails.remove(dataItem);
					}
					
					if(isChecked && !selectMaterails.contains(dataItem)){
						selectMaterails.add(dataItem);
					}
				}
			});
			
			convertView.setTag(holder);
			
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		PlanMaterialrowsBean dataItem = (PlanMaterialrowsBean) getItem(position);
		
		holder.textView.setText(dataItem.thumbnail);
		holder.checkBox.setChecked(selectMaterails.contains(dataItem));
		
		if (!mEditable)
			holder.checkBox.setVisibility(View.GONE);

		return convertView;
	}

	public class ViewHolder{
		TextView textView;
		CheckBox checkBox;
	}	
}
