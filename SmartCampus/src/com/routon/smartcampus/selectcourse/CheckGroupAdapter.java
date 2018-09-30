package com.routon.smartcampus.selectcourse;

import java.util.ArrayList;
import java.util.HashMap;

import com.routon.edurelease.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CheckGroupAdapter extends BaseAdapter{

	ArrayList<SelectSubjectsBean> mDatas;
	Context mContext;
	private static HashMap<Integer,Boolean> isSelected;  
	
	public CheckGroupAdapter(ArrayList<SelectSubjectsBean> datas,Context context)
	{
		mDatas = datas;
		mContext = context;
		initData();
	}
	private void initData(){  
		isSelected = new HashMap<Integer,Boolean>();
        for(int i=0; i<mDatas.size();i++) {  
            isSelected.put(i,false);  
        }  
    }  
	public ArrayList<SelectSubjectsBean> getDatas(){
		return mDatas;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
public void setChecked(int pos,boolean checked)
{
	getIsSelected().put(pos, checked); 
}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.checkgroup_item,null);
			holder.cb = (CheckBox)convertView.findViewById(R.id.checkbox);
			convertView.setTag(holder);
		
		}else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.cb.setText(mDatas.get(position).subjectName);
		holder.cb.setChecked(isSelected.get(position));  
		holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener (){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				Log.v("onCheckedChanged", "holder.cb.isChecked():"+holder.cb.isChecked());
				getIsSelected().put(position, holder.cb.isChecked()); 
			}});
		
		
		return convertView;
	}
	public HashMap<Integer,Boolean> getIsSelected()
	{
		return isSelected;
	}
	static class ViewHolder
	{
		CheckBox cb;
	}
}
