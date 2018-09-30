package com.routon.smartcampus.gradetrack;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ViewFliperAdapter extends BaseAdapter{
	
	private Context context;
	private ArrayList<String> fliperTitles;
	private boolean isShowSum;
	
	public ViewFliperAdapter(Context context,ArrayList<String> fliperTitles,boolean isShowSum)
	{
		this.context = context;
		this.fliperTitles = fliperTitles;
		this.isShowSum = isShowSum;
	}

	@Override
	public int getCount() {
		return fliperTitles.size();
	}

	@Override
	public Object getItem(int position) {
		return fliperTitles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHoler viewHoler = null;
		if(convertView == null)
		{
			view = LayoutInflater.from(context).inflate(R.layout.list_flipper_txt, parent,false);
			viewHoler = new ViewHoler(view);
			view.setTag(viewHoler);
		}
		else {
			view = convertView;
			viewHoler = (ViewHoler) view.getTag();
		}
		viewHoler.textView.setText(fliperTitles.get(position));
		if(position == 0 && isShowSum)
			viewHoler.textView.setBackgroundResource(R.drawable.background_subject_sum);
		else viewHoler.textView.setBackgroundResource(R.drawable.background_subject_one);
		return view;
	}
	
	class ViewHoler
	{
		TextView textView;
		
		public ViewHoler(View view)
		{
			textView = (TextView)view.findViewById(R.id.txt_list_flipper);
		}
	}

}
