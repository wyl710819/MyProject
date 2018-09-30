package com.routon.smartcampus.gradetrack;

import java.util.List;

import com.routon.edurelease.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StudentNameListAdapter extends BaseAdapter{
	
	private List<String> names;
	private Context context;
	
	public StudentNameListAdapter(Context context,List<String> names)
	{
		this.context = context;
		this.names = names;
	}

	@Override
	public int getCount() {
		return names.size();
	}

	@Override
	public Object getItem(int position) {
		return names.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		MyViewHolder viewHolder = null;
		if(convertView == null)
		{
			view = LayoutInflater.from(context).inflate(R.layout.list_item_student_name, parent,false);
			viewHolder = new MyViewHolder(view);
			view.setTag(viewHolder);
		}
		else {
			view = convertView;
			viewHolder = (MyViewHolder) view.getTag();
		}
		viewHolder.textViewName.setText(names.get(position));
		return view;
	}
	
	class MyViewHolder
	{
		TextView textViewName;
		
		public MyViewHolder(View view)
		{
			textViewName = (TextView)view.findViewById(R.id.txt_student_name);
		}
	}

}
