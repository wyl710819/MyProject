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

public class AnswerLineviewContentAdapter extends BaseAdapter{

	private Context mContext;
	private List<String> optionList;
	private int mPosition=-1;
	//点击事件回调
	private LineViewPtionOnClickListener mClickListener = null;

	public interface LineViewPtionOnClickListener {
		public void optionClick(View v, int position,View bgView,TextView optionView);
	}
	public void setClickListener(LineViewPtionOnClickListener mClickListener) {
		this.mClickListener = mClickListener;
	}
	public AnswerLineviewContentAdapter(Context context,List<String> optionList){
		mContext=context;
		this.optionList=optionList;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return optionList.size()-1;
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
			convertView = View.inflate(mContext, R.layout.lineview_select_content_item, null);
			holder.bgView=convertView.findViewById(R.id.line_select_content_bg);
			holder.tvOption=(TextView) convertView.findViewById(R.id.tv_select_content);
			convertView.setTag(holder);
		}
		else{
			holder=(ViewHolder) convertView.getTag();
		}
		
		
		if(position==6){
			holder.tvOption.setTextSize(14);
		}else{
			holder.tvOption.setTextSize(18);
		}
		holder.tvOption.setText(optionList.get(position));
		if(Integer.valueOf(optionList.get(7))==position){
			holder.tvOption.setTextColor(Color.parseColor("#a4fa03"));
//			holder.bgView.setVisibility(View.VISIBLE);
//			holder.tvOption.setBackgroundResource(R.drawable.answer_green_bg);
		}else{
			holder.tvOption.setTextColor(Color.parseColor("#cccccc"));
//			holder.bgView.setVisibility(View.INVISIBLE);
//			holder.tvOption.setBackgroundResource(R.drawable.answer_gray_bg);
		}
		holder.tvOption.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mClickListener!=null){
					mClickListener.optionClick(v, position,holder.bgView,holder.tvOption);
				}
			}
		});
		return convertView;
	}
	public class ViewHolder{
		private View bgView;
		private TextView tvOption;
	}

}
