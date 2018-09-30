package com.routon.smartcampus.answerrelease;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.routon.edurelease.R;

public class AnswerTimeLineviewAdapter extends BaseAdapter{

	private Context mContext;
	private List<String> optionList;

	public AnswerTimeLineviewAdapter(Context context,List<String> optionList){
		mContext=context;
		this.optionList=optionList;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return optionList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return optionList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView = View.inflate(mContext, R.layout.lineview_anstime_content_item, null);
			holder.tvOption=(TextView) convertView.findViewById(R.id.tv_select_content);
			convertView.setTag(holder);
		}
		else{
			holder=(ViewHolder) convertView.getTag();
		}
		
		holder.tvOption.setTextSize(14);
		
		holder.tvOption.setText(optionList.get(position));
		holder.tvOption.setTextColor(Color.parseColor("#cccccc"));
		return convertView;
	}
	public class ViewHolder{
		private TextView tvOption;
	}

}
